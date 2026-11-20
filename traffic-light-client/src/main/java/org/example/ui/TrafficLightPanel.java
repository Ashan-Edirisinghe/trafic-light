package org.example.ui;



import javax.swing.*;
import java.awt.*;

public class TrafficLightPanel extends JPanel {
    private String activeLight = "RED";

    public TrafficLightPanel() {
        setPreferredSize(new Dimension(200, 420));
        setMinimumSize(new Dimension(200, 420));
        setBackground(new Color(240, 242, 245));
    }

    public void setActiveLight(String activeLight) {
        this.activeLight = activeLight != null ? activeLight.toUpperCase() : "NONE";
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Housing box
        int boxWidth = 150;
        int boxHeight = 400;
        int boxX = (getWidth() - boxWidth) / 2;
        int boxY = (getHeight() - boxHeight) / 2;

        g2.setColor(new Color(35, 38, 43));
        g2.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 26, 26);
        g2.setColor(new Color(70, 75, 82));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 26, 26);

        int diameter = 108;
        int circleX = boxX + (boxWidth - diameter) / 2;

        // Top: RED
        drawLight(g2, circleX, boxY + 18, diameter,
                "RED".equals(activeLight),
                new Color(239, 68, 68),
                new Color(130, 45, 45));

        // Middle: YELLOW
        drawLight(g2, circleX, boxY + 146, diameter,
                "YELLOW".equals(activeLight),
                new Color(250, 204, 21),
                new Color(130, 105, 30));

        // Bottom: GREEN
        drawLight(g2, circleX, boxY + 274, diameter,
                "GREEN".equals(activeLight),
                new Color(34, 197, 94),
                new Color(40, 110, 65));

        g2.dispose();
    }

    private void drawLight(Graphics2D g2, int x, int y, int diameter,
                           boolean isActive, Color onColor, Color offColor) {
        if (isActive) {
            // Outer glow ring for active signal
            g2.setColor(new Color(onColor.getRed(), onColor.getGreen(), onColor.getBlue(), 80));
            g2.fillOval(x - 6, y - 6, diameter + 12, diameter + 12);
        }
        g2.setColor(isActive ? onColor : offColor);
        g2.fillOval(x, y, diameter, diameter);

        g2.setColor(new Color(20, 22, 25));
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(x, y, diameter, diameter);
    }
}