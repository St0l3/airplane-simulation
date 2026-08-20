package gui;

import exceptions.FileException;
import exceptions.ValidationException;
import io.DataManager;
import listeners.DataListener;
import listeners.InactivityListener;
import listeners.SimulationListener;
import model.Aircraft;
import model.Data;
import simulation.SimulationEngine;
import simulation.SimulationState;
import tools.InactivityMonitor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

public class MainFrame extends JFrame implements InactivityListener, DataListener, SimulationListener {

    private final Data data = new Data();
    private final InactivityMonitor monitor = new InactivityMonitor(this);
    private final SimulationEngine engine = new SimulationEngine(data);

    private final MapPanel mapPanel = new MapPanel(data);
    private final JLabel statusLabel = new JLabel("Ready");
    private final JLabel inactivityLabel = new JLabel();

    private final JButton addAirportButton = new JButton("Add Airport");
    private final JButton addFlightButton = new JButton("Add Flight");

    private InactivityDialog inactivityDialog;
    private boolean selectionPausesMonitor = false;
    private boolean simulationPausesMonitor = false;

    public MainFrame() {
        setTitle("Stoletovo Letenje");
        setSize(1000, 700);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        buildMenuBar();
        buildTabs();

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.add(statusLabel, BorderLayout.WEST);
        inactivityLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        statusBar.add(inactivityLabel, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);

        data.addModelListener(this);
        engine.addSimulationListener(this);

        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) {
                int eventType = event.getID();
                if(eventType==MouseEvent.MOUSE_EXITED||
                        eventType==MouseEvent.MOUSE_ENTERED) return;
                monitor.registerAction();
            }
        }, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });

        monitor.start();
        setVisible(true);
    }

    private void buildTabs() {
        JTabbedPane tabs = new JTabbedPane();

        JPanel dataTab = new JPanel(new BorderLayout(5, 5));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        addAirportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openAddAirportDialog();
            }
        });
        addFlightButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openAddFlightDialog();
            }
        });
        toolbar.add(addAirportButton);
        toolbar.add(addFlightButton);
        dataTab.add(toolbar, BorderLayout.NORTH);
        dataTab.add(new DataTablePanel(data), BorderLayout.CENTER);
        tabs.addTab("Data", dataTab);

        JPanel mapTab = new JPanel(new BorderLayout(5, 5));
        mapPanel.setShowAircraft(true);
        mapTab.add(new SimulationControlPanel(engine), BorderLayout.NORTH);
        mapTab.add(new JScrollPane(mapPanel), BorderLayout.CENTER);
        mapTab.add(new AirportFilterPanel(data), BorderLayout.EAST);
        tabs.addTab("Map & Simulation", mapTab);

        add(tabs, BorderLayout.CENTER);
    }

    private void openAddAirportDialog() {
        final JDialog dialog = new JDialog(this, "Add Airport", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        AirportFormPanel form = new AirportFormPanel(data);
        form.setOnSuccess(new Runnable() {
            @Override
            public void run() {
                dialog.dispose();
            }
        });
        dialog.add(form);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void openAddFlightDialog() {
        final JDialog dialog = new JDialog(this, "Add Flight", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        final FlightFormPanel form = new FlightFormPanel(data);
        form.setOnSuccess(new Runnable() {
            @Override
            public void run() {
                dialog.dispose();
            }
        });
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                data.removeModelListener(form);
            }
        });
        dialog.add(form);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");

        JMenuItem saveCsv = new JMenuItem("Save as CSV");
        saveCsv.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveToFile("csv");
            }
        });
        file.add(saveCsv);

        JMenuItem saveJson = new JMenuItem("Save as JSON");
        saveJson.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveToFile("json");
            }
        });
        file.add(saveJson);
        file.addSeparator();

        JMenuItem load = new JMenuItem("Load from file");
        load.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadFromFile();
            }
        });
        file.add(load);
        file.addSeparator();

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exitApplication();
            }
        });
        file.add(exit);

        bar.add(file);
        setJMenuBar(bar);
    }

    private void saveToFile(String extension) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save as " + extension.toUpperCase());
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith("." + extension)) {
            f = new File(f.getParentFile(), f.getName() + "." + extension);
        }
        try {
            DataManager.save(data, f);
            statusLabel.setText("Saved to " + f.getName());
        } catch (FileException ex) {
            showError(ex.getMessage());
        }
    }

    private void loadFromFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Load from CSV or JSON");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        if (!data.getAirports().isEmpty() || !data.getFlights().isEmpty()) {
            int answer = JOptionPane.showConfirmDialog(this,
                    "Loading a file will replace the current data. Continue?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) return;
        }
        try {
            Data loaded = DataManager.load(f);
            engine.reset();
            data.replaceAll(loaded);
            statusLabel.setText("Loaded " + data.getAirports().size() + " airports and "
                    + data.getFlights().size() + " flights from " + f.getName());
        } catch (FileException ex) {
            showError(ex.getMessage());
        } catch (ValidationException ex) {
            showError("Invalid data in '" + f.getName() + "': " + ex.getMessage());
        }
    }

    private void exitApplication() {
        monitor.stop();
        engine.reset();
        dispose();
        System.exit(0);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void dataChanged() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                boolean selected = data.getSelectedAirport() != null;
                if (selected && !selectionPausesMonitor) {
                    selectionPausesMonitor = true;
                    monitor.pause();
                } else if (!selected && selectionPausesMonitor) {
                    selectionPausesMonitor = false;
                    monitor.resume();
                }
            }
        });
    }

    @Override
    public void simulationTick(final int simMinutes, final List<Aircraft> snapshot) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                mapPanel.setSimulationSnapshot(simMinutes, snapshot);
            }
        });
    }

    @Override
    public void simulationStateChanged(final SimulationState state) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                boolean stopped = state == SimulationState.STOPPED;
                addAirportButton.setEnabled(stopped);
                addFlightButton.setEnabled(stopped);
                boolean running = state == SimulationState.RUNNING;
                if (running && !simulationPausesMonitor) {
                    simulationPausesMonitor = true;
                    monitor.pause();
                } else if (!running && simulationPausesMonitor) {
                    simulationPausesMonitor = false;
                    monitor.resume();
                }
            }
        });
    }

    @Override
    public void simulationError(String message) {
    }

    @Override
    public void countdownStarted(final int secondsLeft) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (inactivityDialog == null) {
                    inactivityDialog = new InactivityDialog(MainFrame.this, monitor);
                }
                inactivityDialog.setSecondsLeft(secondsLeft);
                inactivityDialog.setVisible(true);
            }
        });
    }

    @Override
    public void countdownTick(final int secondsLeft) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (inactivityDialog != null) {
                    inactivityDialog.setSecondsLeft(secondsLeft);
                }
            }
        });
    }

    @Override
    public void countdownCancelled() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (inactivityDialog != null) {
                    inactivityDialog.setVisible(false);
                }
            }
        });
    }

    @Override
    public void idleCountdown(final int secondsLeft, final boolean paused) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (paused) {
                    inactivityLabel.setText("Auto-close: paused");
                } else {
                    inactivityLabel.setText("Auto-close in " + secondsLeft + "s");
                }
            }
        });
    }

    @Override
    public void timeout() {
        System.exit(0);
    }
}
