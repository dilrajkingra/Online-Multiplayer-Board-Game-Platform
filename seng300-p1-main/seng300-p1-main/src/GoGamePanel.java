import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import go_logic.*; // current package name; may be changed to omg. later

public class GoGamePanel extends JPanel {

    private GoGame game;
    private final int boardSize = 9; 

    private BoardView boardView;
    private JLabel statusLabel;
    private JLabel scoreLabel;
    private JLabel vsLabel;
    private JButton passBtn;
    private JTextArea chatHistory; // For chat prototype, to be replaced
    private JTextField chatInput;  // For chat prototype, to be replaced
    private Runnable onExitCallback; 

    public GoGamePanel(String player1Name, String player2Name, Runnable onExitCallback) {
        this.onExitCallback = onExitCallback;
        
        setLayout(new BorderLayout());
        setBackground(MainGui.BG_COL);

        // 1. Top Status Bar (Updated layout)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(MainGui.BG_COL);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Create a central "VS" label
        vsLabel = new JLabel(player1Name + " (Black) vs. " + player2Name + " (White)", SwingConstants.CENTER);
        vsLabel.setForeground(Color.ORANGE); // distinct color
        vsLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        statusLabel = new JLabel("Turn: BLACK");
        statusLabel.setForeground(MainGui.T_COL);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        scoreLabel = new JLabel("Captured: B=0 | W=0");
        scoreLabel.setForeground(Color.GRAY);
        scoreLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Add components to top bar
        topPanel.add(statusLabel, BorderLayout.WEST);
        topPanel.add(vsLabel, BorderLayout.CENTER); // Add the names here!
        topPanel.add(scoreLabel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Game board
        boardView = new BoardView();
        add(boardView, BorderLayout.CENTER);

        // Chat location at the moment, could be changed
        add(createChatPanel(), BorderLayout.EAST);

        // Bottom options
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(MainGui.BG_COL);

        passBtn = createStyledButton("Pass Turn");
        JButton leaveBtn = createStyledButton("Leave Match");

        bottomPanel.add(passBtn);
        bottomPanel.add(leaveBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        passBtn.addActionListener(e -> {
            game.pass();
            updateUIState();
            checkGameOver();
        });

        leaveBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to resign and leave?", 
                "Resign?", JOptionPane.YES_NO_OPTION);
            
            if (choice == JOptionPane.YES_OPTION) {
                game.resign(); 
                onExitCallback.run(); 
            }
        });
        
        startNewGame(); 
    }

    public void startNewGame() {
        this.game = new GoGame(boardSize);
        if (boardView != null) boardView.repaint();
        if (chatHistory != null) chatHistory.setText(""); // Clears chat for new game
        appendSystemMessage("Game Started. Good luck!"); // Adds start message to chat
        updateUIState();
    }

    // Chat functionality for testing purposes, to be replaced!
    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(280, 0));
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY));

        JLabel header = new JLabel("Match Chat", SwingConstants.CENTER);
        header.setForeground(MainGui.T_COL);
        header.setFont(new Font("SansSerif", Font.BOLD, 14));
        header.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(header, BorderLayout.NORTH);

        chatHistory = new JTextArea();
        chatHistory.setEditable(false);
        chatHistory.setBackground(new Color(30, 30, 30));
        chatHistory.setForeground(new Color(200, 200, 200));
        chatHistory.setFont(new Font("Monospaced", Font.PLAIN, 12));
        chatHistory.setLineWrap(true);
        chatHistory.setWrapStyleWord(true);
        
        JScrollPane scroll = new JScrollPane(chatHistory);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        inputPanel.setBackground(new Color(30, 30, 30));

        chatInput = new JTextField();
        chatInput.setBackground(new Color(50, 50, 50));
        chatInput.setForeground(Color.WHITE);
        chatInput.setCaretColor(Color.WHITE);
        
        JButton sendBtn = new JButton("Send");
        sendBtn.setBackground(new Color(60, 60, 60));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setFocusPainted(false);
        sendBtn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        sendBtn.setPreferredSize(new Dimension(50, 25));

        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);
        panel.add(inputPanel, BorderLayout.SOUTH);


        java.awt.event.ActionListener sendAction = e -> {
            String msg = chatInput.getText().trim();
            if (!msg.isEmpty()) {
                // Display locally
                appendChatMessage("Me", msg);
                
                // Clear input
                chatInput.setText("");

            }
        };

        chatInput.addActionListener(sendAction);
        sendBtn.addActionListener(sendAction);

        return panel;
    }

    // More chat testing, also to be replaced
    private void appendChatMessage(String sender, String msg) {
        String timestamp = new SimpleDateFormat("HH:mm").format(new Date());
        chatHistory.append("[" + timestamp + "] " + sender + ": " + msg + "\n");
        chatHistory.setCaretPosition(chatHistory.getDocument().getLength()); // Auto-scroll
    }

    private void appendSystemMessage(String msg) {
        if (chatHistory != null) {
            chatHistory.append(">>> " + msg + "\n");
        }
    }

    // Helpers for game logic

    private void updateUIState() {
        StoneColor current = game.getCurrentPlayer();
        if (statusLabel != null) statusLabel.setText("Turn: " + current);
        
        int bCaps = game.getCapturedStones(StoneColor.BLACK);
        int wCaps = game.getCapturedStones(StoneColor.WHITE);
        if (scoreLabel != null) scoreLabel.setText("Captured: B=" + bCaps + " | W=" + wCaps);

        if (boardView != null) boardView.repaint();
    }

    private void checkGameOver() {
        if (game.isGameOver()) {
            appendSystemMessage("Game Over!");
            JOptionPane.showMessageDialog(this, "Game Over!");
        }
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(MainGui.BTN_COL);
        btn.setForeground(MainGui.T_COL);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // Drawing the board in the code itself
    private class BoardView extends JPanel {
        private final int MARGIN = 30;
        private int cellSize;

        public BoardView() {
            setBackground(new Color(220, 179, 92)); 
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (game.isGameOver()) return;

                    int r = Math.round((float)(e.getY() - MARGIN) / cellSize);
                    int c = Math.round((float)(e.getX() - MARGIN) / cellSize);

                    try {
                        game.playMove(r, c); 
                        updateUIState();
                        checkGameOver();
                    } catch (IllegalMoveException ex) {
                        appendSystemMessage("Invalid move: " + ex.getMessage()); // Log error to chat, to be adjusted
                        JOptionPane.showMessageDialog(BoardView.this, 
                            ex.getMessage(), "Invalid Move", JOptionPane.WARNING_MESSAGE);
                    } catch (Exception outOfBounds) { }
                }
            });
        }

        // Code for graphics instead of resources, for now
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int side = Math.min(getWidth(), getHeight());
            cellSize = (side - 2 * MARGIN) / (boardSize - 1);

            int xOffset = MARGIN; 
            int yOffset = MARGIN;

            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            for (int i = 0; i < boardSize; i++) {
                g2.drawLine(xOffset, yOffset + i * cellSize, xOffset + (boardSize - 1) * cellSize, yOffset + i * cellSize);
                g2.drawLine(xOffset + i * cellSize, yOffset, xOffset + i * cellSize, yOffset + (boardSize - 1) * cellSize);
            }
            
            GoBoard board = game.getBoard();
            int stoneRadius = (int)(cellSize * 0.4);

            for (int r = 0; r < boardSize; r++) {
                for (int c = 0; c < boardSize; c++) {
                    try {
                        StoneColor color = board.getStone(r, c);
                        if (color != null) {
                            int x = xOffset + c * cellSize;
                            int y = yOffset + r * cellSize;
                            
                            if (color == StoneColor.BLACK) {
                                g2.setColor(Color.BLACK);
                                g2.fillOval(x - stoneRadius, y - stoneRadius, stoneRadius * 2, stoneRadius * 2);
                            } else {
                                g2.setColor(Color.WHITE);
                                g2.fillOval(x - stoneRadius, y - stoneRadius, stoneRadius * 2, stoneRadius * 2);
                                g2.setColor(Color.BLACK); 
                                g2.drawOval(x - stoneRadius, y - stoneRadius, stoneRadius * 2, stoneRadius * 2);
                            }
                        }
                    } catch (IndexOutOfBoundsException e) { }
                }
            }
        }
    }
}