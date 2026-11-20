package org.example.ui;


import org.example.http.ApiException;
import org.example.http.BackendUnavailableException;
import org.example.http.TrafficApiClient;
import org.example.model.TrafficStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.*;

public class MainFrame extends JFrame {
    private final TrafficApiClient apiClient = new TrafficApiClient();
    private final ScheduledExecutorService poller = Executors.newSingleThreadScheduledExecutor();

    private final TrafficLightPanel lightPanel = new TrafficLightPanel();
    private final JLabel connectionLabel = new JLabel("Connecting to backend...");
    private final JLabel countdownLabel = new JLabel("0 s", SwingConstants.CENTER);
    private final JLabel modeLabel = new JLabel("Mode: STOPPED");
    private final JLabel serverMessageLabel = new JLabel("Status: Waiting for server");
    private final JLabel actionFeedbackLabel = new JLabel(" ");

    public MainFrame() {
        super("Traffic Light Controller");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(640, 680);
        setMinimumSize(new Dimension(600, 640));
        setLocationRelativeTo(null);

        initLayout();
        startPolling();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                poller.shutdownNow();
            }
        });
    }

    private void initLayout() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(14, 16, 14, 16));
        root.setBackground(new Color(240, 242, 245));
        setContentPane(root);

        // Top: Connection indicator bar
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        connectionLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        connectionLabel.setOpaque(true);
        connectionLabel.setBorder(new EmptyBorder(8, 12, 8, 12));
        connectionLabel.setBackground(new Color(229, 231, 235));
        connectionLabel.setForeground(new Color(55, 65, 81));
        topPanel.add(connectionLabel, BorderLayout.CENTER);
        root.add(topPanel, BorderLayout.NORTH);

        // Center: Lights (left) + Telemetry (right)
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 24);
        centerPanel.add(lightPanel, gbc);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setPreferredSize(new Dimension(280, 280));

        JLabel timerTitle = new JLabel("COUNTDOWN");
        timerTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        timerTitle.setForeground(new Color(107, 114, 128));
        timerTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        countdownLabel.setFont(new Font("SansSerif", Font.BOLD, 56));
        countdownLabel.setForeground(new Color(17, 24, 39));
        countdownLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        modeLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        modeLabel.setForeground(new Color(31, 41, 55));
        modeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        serverMessageLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        serverMessageLabel.setForeground(new Color(75, 85, 99));
        serverMessageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        actionFeedbackLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        actionFeedbackLabel.setForeground(new Color(185, 28, 28));
        actionFeedbackLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(timerTitle);
        infoPanel.add(countdownLabel);
        infoPanel.add(Box.createVerticalStrut(18));
        infoPanel.add(modeLabel);
        infoPanel.add(Box.createVerticalStrut(12));
        infoPanel.add(serverMessageLabel);
        infoPanel.add(Box.createVerticalStrut(16));
        infoPanel.add(actionFeedbackLabel);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        centerPanel.add(infoPanel, gbc);
        root.add(centerPanel, BorderLayout.CENTER);

        // Bottom: Control buttons
        JPanel controlsWrapper = new JPanel(new GridLayout(2, 1, 8, 8));
        controlsWrapper.setOpaque(false);

        JPanel cycleRow = new JPanel(new GridLayout(1, 6, 6, 0));
        cycleRow.setOpaque(false);

        JButton startBtn = createButton("Start", new Color(22, 163, 74), Color.WHITE);
        JButton pauseBtn = createButton("Pause", new Color(217, 119, 6), Color.WHITE);
        JButton resumeBtn = createButton("Resume", new Color(37, 99, 235), Color.WHITE);
        JButton stopBtn = createButton("Stop", new Color(75, 85, 99), Color.WHITE);
        JButton resetBtn = createButton("Reset", new Color(100, 116, 139), Color.WHITE);
        JButton emergencyBtn = createButton("Emergency", new Color(220, 38, 38), Color.WHITE);

        startBtn.addActionListener(e -> runAction(apiClient::start));
        pauseBtn.addActionListener(e -> runAction(apiClient::pause));
        resumeBtn.addActionListener(e -> runAction(apiClient::resume));
        stopBtn.addActionListener(e -> runAction(apiClient::stop));
        resetBtn.addActionListener(e -> runAction(apiClient::reset));
        emergencyBtn.addActionListener(e -> runAction(apiClient::emergency));

        cycleRow.add(startBtn);
        cycleRow.add(pauseBtn);
        cycleRow.add(resumeBtn);
        cycleRow.add(stopBtn);
        cycleRow.add(resetBtn);
        cycleRow.add(emergencyBtn);

        JPanel manualRow = new JPanel(new GridLayout(1, 3, 8, 0));
        manualRow.setOpaque(false);

        JButton manualRedBtn = createButton("Manual RED", new Color(185, 28, 28), Color.WHITE);
        JButton manualYellowBtn = createButton("Manual YELLOW", new Color(202, 138, 4), Color.WHITE);
        JButton manualGreenBtn = createButton("Manual GREEN", new Color(21, 128, 61), Color.WHITE);

        manualRedBtn.addActionListener(e -> runAction(() -> apiClient.setManualLight("RED")));
        manualYellowBtn.addActionListener(e -> runAction(() -> apiClient.setManualLight("YELLOW")));
        manualGreenBtn.addActionListener(e -> runAction(() -> apiClient.setManualLight("GREEN")));

        manualRow.add(manualRedBtn);
        manualRow.add(manualYellowBtn);
        manualRow.add(manualGreenBtn);

        controlsWrapper.add(cycleRow);
        controlsWrapper.add(manualRow);
        root.add(controlsWrapper, BorderLayout.SOUTH);
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBackground(bg);
        button.setForeground(fg);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(80, 38));
        return button;
    }

    private void startPolling() {
        poller.scheduleAtFixedRate(() -> {
            try {
                TrafficStatus status = apiClient.getStatus();
                SwingUtilities.invokeLater(() -> applyStatus(status));
            } catch (BackendUnavailableException e) {
                SwingUtilities.invokeLater(() -> showDisconnected(e.getMessage()));
            } catch (ApiException e) {
                SwingUtilities.invokeLater(() -> showFeedback("Error (" + e.getStatusCode() + "): " + e.getMessage()));
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    private void runAction(Callable<TrafficStatus> action) {
        new SwingWorker<TrafficStatus, Void>() {
            @Override
            protected TrafficStatus doInBackground() throws Exception {
                return action.call();
            }

            @Override
            protected void done() {
                try {
                    TrafficStatus status = get();
                    actionFeedbackLabel.setText(" ");
                    applyStatus(status);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof ApiException apiEx) {
                        showFeedback("Rejected (" + apiEx.getStatusCode() + "): " + apiEx.getMessage());
                    } else if (cause instanceof BackendUnavailableException offlineEx) {
                        showDisconnected(offlineEx.getMessage());
                    } else {
                        showFeedback("Unexpected error: " + cause.getMessage());
                    }
                }
            }
        }.execute();
    }

    private void applyStatus(TrafficStatus status) {
        connectionLabel.setText("Connected to Backend (http://localhost:8080)");
        connectionLabel.setBackground(new Color(220, 252, 231));
        connectionLabel.setForeground(new Color(22, 101, 52));

        lightPanel.setActiveLight(status.getCurrentLight());
        countdownLabel.setText(status.getRemainingSeconds() + " s");
        modeLabel.setText("Mode: " + status.getMode());
        serverMessageLabel.setText("<html>Status: " + escapeHtml(status.getMessage()) + "</html>");

        if (status.isEmergency()) {
            modeLabel.setForeground(new Color(185, 28, 28));
        } else {
            modeLabel.setForeground(new Color(31, 41, 55));
        }
    }

    private void showDisconnected(String errorText) {
        connectionLabel.setText("Disconnected: " + errorText);
        connectionLabel.setBackground(new Color(254, 226, 226));
        connectionLabel.setForeground(new Color(153, 27, 27));
    }

    private void showFeedback(String message) {
        actionFeedbackLabel.setText("<html>" + escapeHtml(message) + "</html>");
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}