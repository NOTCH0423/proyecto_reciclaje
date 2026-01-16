package com.reciclaje.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class BubblePanel extends JPanel {

    private final JLabel content;

    public BubblePanel(String htmlText, Color bg, Color fg) {
        setOpaque(false);
        setLayout(null);
        content = new JLabel(htmlText);
        content.setOpaque(false);
        content.setForeground(fg);
        content.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        content.setVerticalAlignment(SwingConstants.TOP);
        content.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        add(content);
        // initial approximate size; actual sizing happens in doLayout
        setPreferredSize(new Dimension(400, 1));
    }

    @Override
    public void doLayout() {
        int w = getWidth() > 0 ? getWidth() : 360;
        // let label compute wrapped preferred size for given width
        content.setSize(w - 1, Short.MAX_VALUE);
        java.awt.Dimension pref = content.getPreferredSize();
        content.setBounds(0, 0, pref.width, pref.height);
        setPreferredSize(new Dimension(pref.width, pref.height));
        super.doLayout();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int arc = 18;
        int width = getWidth();
        int height = content.getHeight() + 8;
        // draw rounded rect background behind the content
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, Math.max(width, 40), Math.max(height, 24), arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (content != null) content.setBackground(new Color(0,0,0,0));
    }
}
