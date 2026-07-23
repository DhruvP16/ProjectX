
/*
    RulesWindow.java
    - Creates seperate window that displays the Battleship game rules
*/


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URL;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

public class RulesWindow extends JFrame
{
    public RulesWindow()
    {
        super("Battleship Rules");

        // Read only, HTML interpreted, 
        JEditorPane rulesArea = new JEditorPane();
        rulesArea.setEditable(false);
        rulesArea.setContentType("text/html");

        // If html file not found
        try
        {
            URL rulesPage = RulesWindow.class.getResource("rules.html");

            if (rulesPage == null)
            {
                throw new IOException("rules.html was not found");
            }

            rulesArea.setPage(rulesPage);
        }
        catch (IOException exception)
        {
            JOptionPane.showMessageDialog(this,
                "The rules page could not be loaded.",
                "Rules Error", JOptionPane.ERROR_MESSAGE);

            rulesArea.setText("<html><body><h1>Battleship Rules</h1>" +
                "<p>The rules page could not be loaded.</p></body></html>");
        }

        add(new JScrollPane(rulesArea), BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(620, 520));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
