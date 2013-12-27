package project.rayedchan.swing.gui;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;

/**
 * @author rayedchan
 */
public class WelcomeFrame extends JFrame
{
    JLabel exit;
    JMenuBar mbar;
    JLabel label;

    public WelcomeFrame()
    {
        setTitle("Welcome");
        setSize(400,400);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        mbar=new JMenuBar();
        mbar.setLayout(new FlowLayout());

        label=new JLabel("You are logged in.");
        mbar.add(label);
        exit=new JLabel("<html><a href=''>Exit</a></html>");
        exit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exit.setFont(new Font("Tahoma",Font.PLAIN,13));

        exit.addMouseListener(new MouseAdapter(){
        public void mouseClicked(MouseEvent ae)
        {
        System.exit(0);
        }
        });

        mbar.add(exit);
        setJMenuBar(mbar);
        setExtendedState(MAXIMIZED_BOTH);
    }
}
