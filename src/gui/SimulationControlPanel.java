package gui;

import exceptions.SimulationException;
import listeners.SimulationListener;
import model.Aircraft;
import model.Time;
import simulation.SimulationEngine;
import simulation.SimulationState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class SimulationControlPanel extends JPanel implements SimulationListener {

    private final SimulationEngine engine;
    private final JButton startButton = new JButton("Start");
    private final JButton pauseButton = new JButton("Pause");
    private final JButton resetButton = new JButton("Reset");
    private final JLabel statusLabel = new JLabel("Time: 00:00 | Waiting: 0 | Flying: 0 | Landed: 0");

    public SimulationControlPanel(SimulationEngine engine) {
        this.engine = engine;
        engine.addSimulationListener(this);
        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(startButton);
        add(pauseButton);
        add(resetButton);
        add(statusLabel);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    engine.start();
                } catch (SimulationException ex) {
                    JOptionPane.showMessageDialog(SimulationControlPanel.this,
                            ex.getMessage(), "Cannot start simulation", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (engine.getState() == SimulationState.RUNNING) {
                    engine.pause();
                } else {
                    engine.resume();
                }
            }
        });
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                engine.reset();
            }
        });
        updateButtons(SimulationState.STOPPED);
    }

    private void updateButtons(SimulationState state) {
        startButton.setEnabled(state == SimulationState.STOPPED);
        pauseButton.setEnabled(state != SimulationState.STOPPED);
        resetButton.setEnabled(state != SimulationState.STOPPED);
        pauseButton.setText(state == SimulationState.PAUSED ? "Resume" : "Pause");
    }

    @Override
    public void simulationTick(final int simMinutes, final List<Aircraft> snapshot) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                int waiting = 0, flying = 0, landed = 0;
                for (Aircraft a : snapshot) {
                    if (a.getState() == Aircraft.State.WAITING) waiting++;
                    else if (a.getState() == Aircraft.State.FLYING) flying++;
                    else landed++;
                }
                statusLabel.setText("Time: " + new Time(simMinutes).format()
                        + " | Waiting: " + waiting + " | Flying: " + flying + " | Landed: " + landed);
            }
        });
    }

    @Override
    public void simulationStateChanged(final SimulationState state) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateButtons(state);
                if (state == SimulationState.STOPPED) {
                    statusLabel.setText("Time: 00:00 | Waiting: 0 | Flying: 0 | Landed: 0");
                }
            }
        });
    }

    @Override
    public void simulationError(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(SimulationControlPanel.this,
                        message, "Simulation error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
