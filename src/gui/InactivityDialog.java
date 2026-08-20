package gui;

import tools.InactivityMonitor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class InactivityDialog extends JDialog {

    private final JLabel label = new JLabel("", SwingConstants.CENTER);

    public InactivityDialog(JFrame owner, final InactivityMonitor monitor) {
        super(owner, "Are you still there?", false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        add(label, BorderLayout.CENTER);
        JButton continueButton = new JButton("Continue working");
        continueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.add(continueButton);
        add(south, BorderLayout.SOUTH);
        setSize(340, 130);
        setLocationRelativeTo(owner);
        setAlwaysOnTop(true);
        setModal(true);
    }

    public void setSecondsLeft(int seconds) {
        label.setText("The program will close in " + seconds + " second" + (seconds == 1 ? "" : "s") + "...");
    }
}
