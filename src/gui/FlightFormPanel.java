package gui;

import exceptions.ValidationException;
import listeners.DataListener;
import model.Airport;
import model.Data;
import model.Flight;
import tools.Validator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FlightFormPanel extends JPanel implements DataListener {

    private final Data data;
    private final JComboBox<String> fromBox = new JComboBox<>();
    private final JComboBox<String> toBox = new JComboBox<>();
    private final JTextField timeField = new JTextField(6);
    private final JTextField durationField = new JTextField(6);

    private Runnable onSuccess;

    public FlightFormPanel(Data data) {
        this.data = data;
        data.addModelListener(this);
        setBorder(BorderFactory.createTitledBorder("New flight"));
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0;
        add(new JLabel("From:"), c);
        c.gridx = 1;
        add(fromBox, c);
        c.gridx = 0; c.gridy = 1;
        add(new JLabel("To:"), c);
        c.gridx = 1;
        add(toBox, c);
        c.gridx = 0; c.gridy = 2;
        add(new JLabel("Departure (HH:MM):"), c);
        c.gridx = 1;
        add(timeField, c);
        c.gridx = 0; c.gridy = 3;
        add(new JLabel("Duration (min):"), c);
        c.gridx = 1;
        add(durationField, c);

        JButton addButton = new JButton("Add flight");
        c.gridx = 1; c.gridy = 4;
        c.fill = GridBagConstraints.NONE;
        add(addButton, c);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addFlight();
            }
        });
        refreshBoxes();
    }

    public void setOnSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }

    private void addFlight() {
        try {
            String from = (String) fromBox.getSelectedItem();
            String to = (String) toBox.getSelectedItem();
            if (from == null || to == null) {
                throw new ValidationException("There are no airports yet. Add at least two airports first.");
            }
            Flight f = Validator.validateFlight(from, to, timeField.getText(), durationField.getText(), data);
            data.addFlight(f);
            timeField.setText("");
            durationField.setText("");
            if (onSuccess != null) {
                onSuccess.run();
            }
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid flight", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshBoxes() {
        String oldFrom = (String) fromBox.getSelectedItem();
        String oldTo = (String) toBox.getSelectedItem();
        fromBox.removeAllItems();
        toBox.removeAllItems();
        for (Airport a : data.getAirports()) {
            fromBox.addItem(a.getCode());
            toBox.addItem(a.getCode());
        }
        if (oldFrom != null) fromBox.setSelectedItem(oldFrom);
        if (oldTo != null) toBox.setSelectedItem(oldTo);
    }

    @Override
    public void dataChanged() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                refreshBoxes();
            }
        });
    }
}
