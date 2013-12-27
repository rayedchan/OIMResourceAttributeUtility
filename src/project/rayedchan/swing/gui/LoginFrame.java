package project.rayedchan.swing.gui;

import Thor.API.Operations.tcLookupOperationsIntf;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import project.rayedchan.services.OIMClientResourceAttr;

/**
 * @author rayedchan
 * User Login graphical user interface (GUI). A user must enter a OIM username 
 * and a password. OIM client will handle the authentication process. 
 */
public class LoginFrame extends JFrame
{
    JTextField usernameFld, oimServerURLFld;
    JPasswordField passwordFld;
    JButton loginBtn, cancelBtn;
    JLabel usernameLbl, passwordLbl, oimServerURLLbl;
    
    /*
     * Constructor: Handles the creation of the JFrame and all it's components
     */
    public LoginFrame()
    {
        //JFrame properties
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //make sure the program exits when the frame closes
        setTitle("Login");
        setLocationRelativeTo(null); //This will center the JFrame in the middle of the screen
        setVisible(true);
        setLayout(new GridLayout(4,2)); //Create a three rows by 2 columns layout
        
        //Labels for the textfield components
        oimServerURLLbl = new JLabel("OIM Server URL: ");
        usernameLbl = new JLabel("User ID: ");
        passwordLbl = new JLabel("Password: ");
             
        //TextField input components
        oimServerURLFld = new JTextField();
        usernameFld = new JTextField();
        passwordFld = new JPasswordField();
        
        //Buttons components
        loginBtn=new JButton("Login");
        cancelBtn=new JButton("Cancel");
           
        //Add components to JFrame
        add(oimServerURLLbl);
        add(oimServerURLFld);
        add(usernameLbl);
        add(usernameFld);
        add(passwordLbl);
        add(passwordFld);
        add(loginBtn);
        add(cancelBtn);
       
        //Initially focus on User Id Field
        oimServerURLFld.requestFocus();
        
        //Causes this Window to be sized to fit the preferred size and layouts of its subcomponents.
        pack();
        
        //Add event for the cancel button
        cancelBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                System.exit(0);
            }
        });
        
        //Add event for the login button
        loginBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                try 
                {
                    //Get values from input field
                    String oimServerURL = oimServerURLFld.getText(); 
                    String username = usernameFld.getText();
                    char[] password = passwordFld.getPassword(); 
                    System.out.printf("%s\n%s\n%s\n", oimServerURL, username, password);
                    
                    OIMClientResourceAttr oimClientResAttr = new OIMClientResourceAttr(oimServerURL,username, password); //Authentication Test 
                    oimClientResAttr.getOIMClient().getService(tcLookupOperationsIntf.class); //Test OIM service access in order to validate OIM URL.
                    dispose();
                    new WelcomeFrame();
                    System.out.println("Authentication successful.");
                } 
                
                catch(LoginException ex) 
                {
                    Logger.getLogger(LoginFrame.class.getName()).log(Level.SEVERE, null, ex);
                    errorDialogMessage("Invalid username or password");
                }
                              
                catch(oracle.iam.platform.utils.NoSuchServiceException ex)
                {
                    Logger.getLogger(LoginFrame.class.getName()).log(Level.SEVERE, null, ex);
                    errorDialogMessage("Invalid OIM URL");
                }
                 
                catch(Exception ex)
                {
                    Logger.getLogger(LoginFrame.class.getName()).log(Level.SEVERE, null, ex);
                    errorDialogMessage("Generic Exception");
                }
            }
            
            private void errorDialogMessage(String message)
            {
                JOptionPane.showMessageDialog(LoginFrame.this, message);
                 
                //Reset Password field
                passwordFld.setText("");
            }
        });
    }
}
