package gui;

import listeners.DataListener;
import model.Airport;
import model.Data;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class AirportFilterPanel extends JPanel implements DataListener {

    private final Data data;
    private final JPanel checkboxPanel = new JPanel();

    public AirportFilterPanel(Data data) {
        this.data = data;
        data.addModelListener(this);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Visible airports"));
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(checkboxPanel);
        scroll.setPreferredSize(new Dimension(230, 100));
        add(scroll, BorderLayout.CENTER);
        rebuild();
    }

    private void rebuild() {
        checkboxPanel.removeAll();
        for (Airport a : data.getAirports()) {
            final Airport airport = a;
            JCheckBox box = new JCheckBox(a.toString(), a.isVisible());
            box.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    data.setAirportVisible(airport, e.getStateChange() == ItemEvent.SELECTED);
                }
            });
            checkboxPanel.add(box);
        }
        checkboxPanel.revalidate();
        checkboxPanel.repaint();
    }

    @Override
    public void dataChanged() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (checkboxPanel.getComponentCount() != data.getAirports().size()) {
                    rebuild();
                }
            }
        });
    }
}
