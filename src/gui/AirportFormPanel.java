package gui;

import exceptions.ValidationException;
import model.Airport;
import model.Data;
import tools.Validator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AirportFormPanel extends JPanel {

    private final Data data;
    private final JTextField codeField = new JTextField(6);
    private final JTextField nameField = new JTextField(14);
    private final JTextField xField = new JTextField(5);
    private final JTextField yField = new JTextField(5);

    private Runnable onSuccess;

    public AirportFormPanel(Data data) {
        this.data = data;
        setBorder(BorderFactory.createTitledBorder("New airport"));
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0;
        add(new JLabel("Code:"), c);
        c.gridx = 1;
        add(codeField, c);
        c.gridx = 0; c.gridy = 1;
        add(new JLabel("Name:"), c);
        c.gridx = 1;
        add(nameField, c);
        c.gridx = 0; c.gridy = 2;
        add(new JLabel("X (-180..180):"), c);
        c.gridx = 1;
        add(xField, c);
        c.gridx = 0; c.gridy = 3;
        add(new JLabel("Y (-90..90):"), c);
        c.gridx = 1;
        add(yField, c);

        JButton addButton = new JButton("Add airport");
        c.gridx = 1; c.gridy = 4;
        add(addButton, c);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addAirport();
            }
        });
    }

    public void setOnSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }

    private void addAirport() {
        try {
            Airport a = Validator.validateAirport(codeField.getText(), nameField.getText(),
                    xField.getText(), yField.getText(), data);
            data.addAirport(a);
            codeField.setText("");
            nameField.setText("");
            xField.setText("");
            yField.setText("");
            codeField.requestFocus();
            if (onSuccess != null) {
                onSuccess.run();
            }
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid airport", JOptionPane.ERROR_MESSAGE);
        }
    }
}
