package project.rayedchan.swing.gui;

import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * @author rayedchan
 */
public class WelcomeFrame extends JFrame
{
    public WelcomeFrame()
    {
        initUI();
    }
    
    public final void initUI()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(new Insets(40, 60, 40, 60)));
             
        //Panel Components
        JButton lookupBtn = new JButton("Lookups");
        JButton processFormFieldBtn = new JButton("Process Form Fields");
        JButton processTaskBtn = new JButton("Process Tasks");
        JButton reconFieldBtn = new JButton("Reconciliation Fields");
        JButton rfToPffMappingBtn = new JButton("Recon Field - Form Field Mapping");
        
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
        setTitle("Options");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
}
