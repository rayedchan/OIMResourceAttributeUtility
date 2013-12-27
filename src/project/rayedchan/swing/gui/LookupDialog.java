package project.rayedchan.swing.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

/**
 * @author rayedchan
 * Dialog for Lookup Utility.
 */
public class LookupDialog extends JDialog
{
    private JTextField lookupNameFld;
    private JLabel lookupNameLbl;
    private JTextField fileNameFld;
    private JLabel fileNameLbl;
    private JButton btnSubmit;
    private JButton btnCancel;
    
    /*
     * Constructor
     * @param   parent  Frame of parent
     */
    public LookupDialog(Frame parent)
    {
        super(parent, "Lookup Utility", true);
        initUI(parent);
    }
    
    /*
     * Creates the GUI.
     * @param   parent  Frame of parent
     */
    public final void initUI(Frame parent)
    {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        cs.fill = GridBagConstraints.HORIZONTAL;
 
        lookupNameLbl = new JLabel("Lookup Name: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(lookupNameLbl, cs);
 
        lookupNameFld= new JTextField();
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(lookupNameFld, cs);
        panel.setBorder(new LineBorder(Color.GRAY));
           
        lookupNameLbl = new JLabel("File Name: ");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(lookupNameLbl, cs);
 
        lookupNameFld= new JTextField();
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(lookupNameFld, cs);
        panel.setBorder(new LineBorder(Color.GRAY));
 
        btnSubmit = new JButton("Submit");
        btnSubmit.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e) 
            {
            }
        });
        
        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e) 
            {
                dispose();
            }
        });
        
        JPanel bp = new JPanel();
        bp.add(btnSubmit);
        bp.add(btnCancel);
 
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);
 
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }
}
