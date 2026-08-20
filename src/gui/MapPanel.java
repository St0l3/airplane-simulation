package gui;

import listeners.DataListener;
import model.Aircraft;
import model.Airport;
import model.Data;
import model.Time;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class MapPanel extends JPanel implements DataListener {

    private static final int MARGIN = 0;
    private static final int SCALE = 2;
    private static final int SQUARE = 10;
    private static final int CIRCLE = 8;
    private static final int CLICK_TOLERANCE = 8;

    private final Data data;
    private final Timer blinkTimer;
    private boolean blinkOn = true;

    private List<Aircraft> aircrafts = new ArrayList<>();
    private int simMinutes = -1;
    private boolean showAircraft = false;

    public MapPanel(Data data) {
        this.data = data;
        data.addModelListener(this);
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(2 * MARGIN + 360 * SCALE, 2 * MARGIN + 180 * SCALE));

        blinkTimer = new Timer(400, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                blinkOn = !blinkOn;
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    public void setShowAircraft(boolean showAircraft) {
        this.showAircraft = showAircraft;
    }

    public void setSimulationSnapshot(int simMinutes, List<Aircraft> snapshot) {
        this.simMinutes = simMinutes;
        this.aircrafts = snapshot;
        repaint();
    }

    private void handleClick(int px, int py) {
        Airport hit = null;
        for (Airport a : data.getAirports()) {
            if (!a.isVisible()) continue;
            int ax = toPixelX(a.getX());
            int ay = toPixelY(a.getY());
            if (Math.abs(px - ax) <= SQUARE / 2 + CLICK_TOLERANCE
                    && Math.abs(py - ay) <= SQUARE / 2 + CLICK_TOLERANCE) {
                hit = a;
                break;
            }
        }
        if (hit == null) return;
        if (hit.equals(data.getSelectedAirport())) {
            data.setSelectedAirport(null);
        } else {
            data.setSelectedAirport(hit);
        }
    }

    private int toPixelX(double x) {
        return MARGIN + (int) Math.round((x + 180) * SCALE);
    }

    private int toPixelY(double y) {
        return MARGIN + (int) Math.round((90 - y) * SCALE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Airport selected = data.getSelectedAirport();

        for (Airport a : data.getAirports()) {
            if (!a.isVisible()) continue;
            int ax = toPixelX(a.getX());
            int ay = toPixelY(a.getY());
            boolean isSelected = a.equals(selected);
            if (isSelected && blinkOn) {
                g.setColor(Color.RED);
            } else {
                g.setColor(Color.GRAY);
            }
            g.fillRect(ax - SQUARE / 2, ay - SQUARE / 2, SQUARE, SQUARE);
            g.setColor(Color.BLACK);
            g.drawString(a.getCode(), ax + SQUARE, ay + 4);
        }

        if (showAircraft) {
            g.setColor(Color.BLUE);
            for (Aircraft a : aircrafts) {
                if (a.getState() != Aircraft.State.FLYING) continue;
                int ax = toPixelX(a.getCurX());
                int ay = toPixelY(a.getCurY());
                g.fillOval(ax - CIRCLE / 2, ay - CIRCLE / 2, CIRCLE, CIRCLE);
            }
            if (simMinutes >= 0) {
                g.setColor(Color.BLACK);
                g.drawString("Sim time: " + new Time(simMinutes).format(), 10, 20);
            }
        }
    }

    @Override
    public void dataChanged() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (data.getSelectedAirport() != null) {
                    if (!blinkTimer.isRunning()) {
                        blinkOn = true;
                        blinkTimer.start();
                    }
                } else {
                    blinkTimer.stop();
                }
                repaint();
            }
        });
    }
}
