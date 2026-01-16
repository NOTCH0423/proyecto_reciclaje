package com.reciclaje.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import com.reciclaje.core.Chatbot;
import com.reciclaje.core.Responder;
import com.reciclaje.core.RuleBasedResponder;

public class ChatbotApp {

    private final Chatbot chatbot;
    private JTextPane conversationArea;
    private JPanel messagesPanel;
    private JScrollPane conversationScroll;
    private StringBuilder conversationHistory = new StringBuilder();
    private JTextField inputField;
    private JComboBox<String> aiSelector;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Logger log = Logger.getLogger(ChatbotApp.class.getName());
    private javax.swing.JLabel statusLabel;
    private JButton saveButton;
    private JButton settingsButton;
    private JButton sendButton;
    private com.reciclaje.util.ConversationLogger logger;
    private static final Color PRIMARY_COLOR = new Color(76, 175, 80);
    private static final Color SECONDARY_COLOR = new Color(66, 133, 244);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color USER_MSG_COLOR = new Color(200, 230, 255);      // Light blue
    private static final Color USER_TEXT_COLOR = new Color(0, 51, 102);         // Dark blue
    private static final Color BOT_MSG_COLOR = new Color(230, 245, 220);        // Light green
    private static final Color BOT_TEXT_COLOR = new Color(0, 100, 0);           // Dark green

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatbotApp::new);
    }

    public ChatbotApp() {
        JFrame frame = new JFrame("♻️ Chatbot Educativo sobre Reciclaje");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(800, 650);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(BACKGROUND_COLOR);

        // messages panel with bubble components
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(Color.WHITE);
        messagesPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        conversationScroll = new JScrollPane(messagesPanel);
        conversationScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        conversationScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        JPanel inputPanel = createInputPanel();
        JPanel topBar = createTopBar();
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.add(conversationScroll, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        frame.setLayout(new BorderLayout());
        frame.add(topBar, BorderLayout.NORTH);
        frame.add(mainPanel, BorderLayout.CENTER);

        Responder responder = selectResponder();
        chatbot = new Chatbot(responder);
        logger = new com.reciclaje.util.ConversationLogger();
        // Set selector initial value based on available keys
        // restore saved provider if exists, otherwise infer from env
        String saved = com.reciclaje.config.Config.getSavedProvider();
        if (saved != null && !saved.isBlank()) {
            aiSelector.setSelectedItem(saved);
            introText(saved);
        } else {
            String openaiKey = System.getenv("OPENAI_API_KEY");
            if (openaiKey != null && !openaiKey.isBlank()) {
                aiSelector.setSelectedItem("OpenAI (gpt-3.5-turbo)");
                introText("OpenAI (gpt-3.5-turbo)");
            } else {
                aiSelector.setSelectedItem("Reglas");
                introText("Reglas");
            }
        }

        sendButton.addActionListener(this::onSend);
        inputField.addActionListener(this::onSend);

        aiSelector.addItemListener((ItemEvent evt) -> { if (evt.getStateChange() == ItemEvent.SELECTED) onProviderChange(evt); });
        saveButton.addActionListener(evt -> onSaveSnapshot());
        settingsButton.addActionListener(evt -> showSettingsDialog());

        // status label for async operations
        statusLabel = new javax.swing.JLabel("Escribiendo...");
        statusLabel.setForeground(Color.DARK_GRAY);
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setVisible(false);
        JPanel bottomInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomInfo.setBackground(BACKGROUND_COLOR);
        bottomInfo.add(statusLabel);
        frame.add(bottomInfo, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setBackground(PRIMARY_COLOR);
        topBar.setBorder(new EmptyBorder(12, 15, 12, 15));

        JLabel titleLabel = new JLabel("♻️ Asistente de Reciclaje");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlPanel.setOpaque(false);

        aiSelector = new JComboBox<>(new String[]{"Reglas", "OpenAI (gpt-3.5-turbo)"});
        aiSelector.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        saveButton = new JButton("💾 Guardar");
        styleButton(saveButton, new Color(100, 200, 100));

        settingsButton = new JButton("⚙️ Ajustes");
        styleButton(settingsButton, new Color(120, 120, 120));

        controlPanel.add(aiSelector);
        controlPanel.add(saveButton);
        controlPanel.add(settingsButton);

        topBar.add(titleLabel, BorderLayout.WEST);
        topBar.add(controlPanel, BorderLayout.EAST);

        return topBar;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(245, 245, 245));
        sidebar.setBorder(new EmptyBorder(10, 10, 10, 10));
        sidebar.setPreferredSize(new Dimension(220, 0));

        JLabel lbl = new JLabel("Accesos Rápidos");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(Color.DARK_GRAY);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(lbl);
        sidebar.add(Box.createVerticalStrut(15));

        addSidebarButton(sidebar, "📅 Calendario", "dias de recoleccion");
        addSidebarButton(sidebar, "♻️ ¿Qué reciclo?", "que se puede reciclar");
        addSidebarButton(sidebar, "🏪 Puntos de acopio", "puntos de acopio");
        addSidebarButton(sidebar, "💡 Consejos", "dame consejos");
        addSidebarButton(sidebar, "😄 Chiste", "cuéntame un chiste");
        addSidebarButton(sidebar, "❓ Quiz", "quiz");

        sidebar.add(Box.createVerticalGlue());
        
        JButton clearBtn = new JButton("🗑️ Limpiar Chat");
        styleButton(clearBtn, new Color(220, 100, 100));
        clearBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearBtn.setMaximumSize(new Dimension(200, 35));
        clearBtn.addActionListener(e -> {
            messagesPanel.removeAll();
            messagesPanel.revalidate();
            messagesPanel.repaint();
            conversationHistory.setLength(0);
            addBotMessage("Chat limpiado. ¿En qué más puedo ayudarte?");
        });
        sidebar.add(clearBtn);

        return sidebar;
    }

    private void addSidebarButton(JPanel panel, String label, String query) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(200, 35));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> sendMessage(query));
        
        // Add hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(230, 240, 255));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.WHITE);
            }
        });
        
        panel.add(btn);
        panel.add(Box.createVerticalStrut(10));
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        inputField.setBackground(Color.WHITE);
        inputField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        inputField.setMargin(new Insets(8, 8, 8, 8));

        sendButton = new JButton("➤ Enviar");
        styleButton(sendButton, SECONDARY_COLOR);

        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);

        return panel;
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
    }

        private void introText(String provider) {
        // reset visual messages
        messagesPanel.removeAll();
        conversationHistory.setLength(0);
        addBotMessage("¡Hola! Soy tu asistente educativo sobre reciclaje ♻️");
        String status = "Modo: " + provider;
        addBotMessage(status);
        addBotMessage("Puedo ayudarte con:<br/>" +
            "📅 Calendarios de recolección<br/>" +
            "♻️ Materiales reciclables<br/>" +
            "🏪 Puntos de acopio cercanos<br/>" +
            "💡 Consejos de sostenibilidad");
        addBotMessage("Escribe tu pregunta y presiona 'Enviar' para comenzar...");
        }

    private void addBotMessage(String text) {
        addBubble("<html>🤖 <b>Asistente</b>:&nbsp;" + text + "</html>", BOT_MSG_COLOR, BOT_TEXT_COLOR, true);
    }

    private void addUserMessage(String text) {
        addBubble("<html>👤 <b>Tú</b>:&nbsp;" + text + "</html>", USER_MSG_COLOR, USER_TEXT_COLOR, false);
    }
    private void addBubble(String htmlText, Color bgColor, Color textColor, boolean isBot) {
        // add a timestamp and append to plain-history (strip HTML tags)
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        String htmlWithTime = htmlText + "<br/><span style='font-size:11px;color:#666;margin-top:4px;'>" + time + "</span>";

        String plain = htmlWithTime.replaceAll("<.*?>", "").trim();
        conversationHistory.append(plain).append(System.lineSeparator());

        // create bubble component
        BubblePanel bubble = new BubblePanel(htmlWithTime, bgColor, textColor);
        bubble.setBackground(bgColor);

        JPanel wrapper = new JPanel(new FlowLayout(isBot ? FlowLayout.LEFT : FlowLayout.RIGHT));
        wrapper.setOpaque(false);
        wrapper.add(bubble);

        messagesPanel.add(wrapper);
        messagesPanel.add(Box.createVerticalStrut(6));
        messagesPanel.revalidate();
        messagesPanel.repaint();

        // scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar v = conversationScroll.getVerticalScrollBar();
            v.setValue(v.getMaximum());
        });

        if (logger != null) {
            logger.logLine(plain);
        }
    }

    private Responder selectResponder() {
        String sel = aiSelector != null ? (String) aiSelector.getSelectedItem() : "Reglas";
        if ("OpenAI (gpt-3.5-turbo)".equals(sel)) {
            String key = System.getenv("OPENAI_API_KEY");
            if (key == null || key.isBlank()) key = com.reciclaje.config.Config.getOpenAIKey();
            if (key != null && !key.isBlank()) return new com.reciclaje.ai.OpenAIResponder(key, null);
            return new RuleBasedResponder();
        }
        return new RuleBasedResponder();
    }
    private void onProviderChange(ItemEvent evt) {
        String selected = (String) evt.getItem();
        // persist selection
        com.reciclaje.config.Config.saveProvider(selected);
        if ("OpenAI (gpt-3.5-turbo)".equals(selected)) {
            String key = System.getenv("OPENAI_API_KEY");
            if (key == null || key.isBlank()) key = com.reciclaje.config.Config.getOpenAIKey();
            if (key == null || key.isBlank()) {
                JOptionPane.showMessageDialog(null,
                        "No hay API key configurada (OPENAI_API_KEY). Continuaré con respuestas por reglas.",
                        "IA no disponible", JOptionPane.WARNING_MESSAGE);
                aiSelector.setSelectedItem("Reglas");
                chatbot.setResponder(new RuleBasedResponder());
                introText("Reglas");
                return;
            }
            chatbot.setResponder(new com.reciclaje.ai.OpenAIResponder(key, null));
            introText("OpenAI (gpt-3.5-turbo)");
            return;
        }
        chatbot.setResponder(new RuleBasedResponder());
        introText("Reglas");
    }


    private void onSaveSnapshot() {
        Path file = logger.saveSnapshot(conversationHistory.toString());
        JOptionPane.showMessageDialog(null,
                "Historial guardado en: " + file.toAbsolutePath(),
                "Guardado", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSettingsDialog() {
        String existing = System.getenv("OPENAI_API_KEY");
        if (existing == null || existing.isBlank()) existing = com.reciclaje.config.Config.getOpenAIKey();
        JPasswordField pf = new JPasswordField(40);
        if (existing != null) pf.setText(existing);
        JPanel panel = new JPanel(new BorderLayout(5,5));
        panel.add(new JLabel("OpenAI API Key:"), BorderLayout.NORTH);
        panel.add(pf, BorderLayout.CENTER);
        int result = JOptionPane.showConfirmDialog(null, panel, "Ajustes - API Key", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String key = new String(pf.getPassword()).trim();
            com.reciclaje.config.Config.saveOpenAIKey(key);
            JOptionPane.showMessageDialog(null, "Clave guardada.", "Ajustes", JOptionPane.INFORMATION_MESSAGE);
            String sel = aiSelector != null ? (String) aiSelector.getSelectedItem() : "Reglas";
            if ("OpenAI (gpt-3.5-turbo)".equals(sel)) {
                if (key != null && !key.isBlank()) {
                    chatbot.setResponder(new com.reciclaje.ai.OpenAIResponder(key, null));
                    introText("OpenAI (gpt-3.5-turbo)");
                } else {
                    chatbot.setResponder(new RuleBasedResponder());
                    introText("Reglas");
                }
            }
        }
    }

    private void onSend(ActionEvent e) {
        String user = inputField.getText().trim();
        if (user.isEmpty()) {
            return;
        }
        sendMessage(user);
    }

    private void sendMessage(String user) {
        addUserMessage(user);
        inputField.setText(""); // Clear immediately
        inputField.setEnabled(false);
        sendButton.setEnabled(false);
        // show typing indicator in the footer
        statusLabel.setVisible(true);

        // run responder in background
        executor.submit(() -> {
            String botResponse = chatbot.reply(user);
            // update UI on EDT
            javax.swing.SwingUtilities.invokeLater(() -> {
                // remove the last 'escribiendo...' line by replacing last lines
                try {
                    // append actual response
                    addBotMessage(botResponse);
                } finally {
                    inputField.setEnabled(true);
                    sendButton.setEnabled(true);
                    inputField.requestFocusInWindow();
                    statusLabel.setVisible(false);
                }
            });
        });
    }
}