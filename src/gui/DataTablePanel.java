package gui;

import listeners.DataListener;
import model.Airport;
import model.Data;
import model.Flight;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class DataTablePanel extends JPanel implements DataListener {

    private final Data data;
    private final DefaultTableModel airportModel;
    private final DefaultTableModel flightModel;
    private final JTable airportTable;
    private final JTable flightTable;

    public DataTablePanel(Data data) {
        this.data = data;
        data.addModelListener(this);
        setLayout(new GridLayout(1, 2, 5, 5));

        airportModel = new DefaultTableModel(new String[]{"Code", "Name", "X", "Y"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        flightModel = new DefaultTableModel(new String[]{"From", "To", "Departure", "Duration (min)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        airportTable = new JTable(airportModel);
        flightTable = new JTable(flightModel);

        add(buildSide("Airports", airportTable, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedAirport();
            }
        }));
        add(buildSide("Flights", flightTable, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedFlight();
            }
        }));
        refresh();
    }

    private JPanel buildSide(String title, JTable table, ActionListener removeAction) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JButton removeButton = new JButton("Remove selected");
        removeButton.addActionListener(removeAction);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(removeButton);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private void removeSelectedAirport() {
        int row = airportTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an airport in the table first.", "Nothing selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        List<Airport> airports = data.getAirports();
        if (row >= airports.size()) return;
        Airport a = airports.get(row);
        int answer = JOptionPane.showConfirmDialog(this,
                "Remove airport " + a.getCode() + " and all its flights?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            data.removeAirport(a);
        }
    }

    private void removeSelectedFlight() {
        int row = flightTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a flight in the table first.", "Nothing selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        List<Flight> flights = data.getFlights();
        if (row >= flights.size()) return;
        data.removeFlight(flights.get(row));
    }

    private void refresh() {
        airportModel.setRowCount(0);
        for (Airport a : data.getAirports()) {
            airportModel.addRow(new Object[]{a.getCode(), a.getName(), a.getX(), a.getY()});
        }
        flightModel.setRowCount(0);
        for (Flight f : data.getFlights()) {
            flightModel.addRow(new Object[]{f.getFrom().getCode(), f.getTo().getCode(),
                    f.getDepartureTime().format(), f.getDurationInMinutes()});
        }
    }

    @Override
    public void dataChanged() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        });
    }
}
