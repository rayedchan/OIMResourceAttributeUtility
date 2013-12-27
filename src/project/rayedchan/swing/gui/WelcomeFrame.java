package project.rayedchan.swing.gui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * @author rayedchan
 * Displays the options for utility.
 */
public class WelcomeFrame extends JFrame
{
    private JButton lookupBtn;
    private JButton processFormFieldBtn;
    private JButton processTaskBtn;
    private JButton reconFieldBtn;
    private JButton rfToPffMappingBtn;
    
    /*
     * Constructor
     */
    public WelcomeFrame()
    {
        initUI();
    }
     
    /*
     * Handles the creation of the JFrame and all it's components
     */
    public final void initUI()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(new Insets(40, 60, 40, 60)));
             
        //Panel Components
        lookupBtn = new JButton("Lookups");
        processFormFieldBtn = new JButton("Process Form Fields");
        processTaskBtn = new JButton("Process Tasks");
        reconFieldBtn = new JButton("Reconciliation Fields");
        rfToPffMappingBtn = new JButton("Recon Field - Form Field Mapping");
        
        //Add functionality to buttons
        lookupBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) 
            {
                LookupDialog loginDlg = new LookupDialog(WelcomeFrame.this);
                loginDlg.setVisible(true);
            }
        });
        
        //Add buttons to panel
        panel.add(lookupBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(processFormFieldBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(processTaskBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(reconFieldBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(rfToPffMappingBtn);

        add(panel); //Add panel to JFrame
        pack();

        //Set JFrame Properties
        setTitle("Utility Options");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
}
