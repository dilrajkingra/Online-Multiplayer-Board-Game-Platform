import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import TicTacToe_Logic.*;

public class TicTacToeGamePanel extends JPanel {

    private static final Logger LOG = Logger.getLogger(TicTacToeGamePanel.class.getName());
    private static final int GRID = 3;
    private static final Color BG_BOARD   = new Color(45, 45, 45);
    private static final Color LINE_COLOR = new Color(200, 200, 200);
    private static final Color X_COLOR    = new Color(220, 80,  80);
    private static final Color O_COLOR    = new Color(80,  160, 220);

    private TicTacToeGame game;
    private BoardView boardView;
    private JLabel statusLabel;
    private JLabel vsLabel;
    private JTextArea chatHistory;
    private JTextField chatInput;
    private final String player1Name;
    private final String player2Name;
    private final Runnable onExitCallback;

    public TicTacToeGamePanel(String player1Name, String player2Name, Runnable onExitCallback) {
        this.player1Name   = player1Name;
        this.player2Name   = player2Name;
        this.onExitCallback = onExitCallback;

        setLayout(new BorderLayout());
        setBackground(MainGui.BG_COL);

        // ── Top bar ──────────────────────────────────────────────────────────
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(MainGui.BG_COL);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        vsLabel = new JLabel(player1Name + " (X) vs. " + player2Name + " (O)", SwingConstants.CENTER);
        vsLabel.setForeground(Color.ORANGE);
        vsLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        statusLabel = new JLabel("Turn: X  (" + player1Name + ")", SwingConstants.LEFT);
        statusLabel.setForeground(MainGui.T_COL);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        topPanel.add(statusLabel, BorderLayout.WEST);
        topPanel.add(vsLabel,     BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ── Board ────────────────────────────────────────────────────────────
        boardView = new BoardView();
        add(boardView, BorderLayout.CENTER);

        // ── Chat panel ───────────────────────────────────────────────────────
        add(createChatPanel(), BorderLayout.EAST);

        // ── Bottom bar ───────────────────────────────────────────────────────
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(MainGui.BG_COL);

        JButton newGameBtn = styledButton("New Game");
        JButton leaveBtn   = styledButton("Leave Match");

        bottomPanel.add(newGameBtn);
        bottomPanel.add(leaveBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        newGameBtn.addActionListener(e -> startNewGame());

        leaveBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to leave?", "Leave?", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) onExitCallback.run();
        });

        startNewGame();
    }

    public void startNewGame() {
        game = new TicTacToeGame();
        appendSystemMessage("New game started. " + player1Name + " is X, " + player2Name + " is O.");
        refreshUI();
        if (boardView != null) boardView.repaint();
    }

    // ── Chat ─────────────────────────────────────────────────────────────────

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(260, 0));
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
        sendBtn.setPreferredSize(new Dimension(55, 25));

        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(sendBtn,   BorderLayout.EAST);
        panel.add(inputPanel, BorderLayout.SOUTH);

        java.awt.event.ActionListener send = e -> {
            String msg = chatInput.getText().trim();
            if (!msg.isEmpty()) {
                appendChatMessage("Me", msg);
                chatInput.setText("");
            }
        };
        chatInput.addActionListener(send);
        sendBtn.addActionListener(send);

        return panel;
    }

    private void appendChatMessage(String sender, String msg) {
        String ts = new SimpleDateFormat("HH:mm").format(new Date());
        chatHistory.append("[" + ts + "] " + sender + ": " + msg + "\n");
        chatHistory.setCaretPosition(chatHistory.getDocument().getLength());
    }

    private void appendSystemMessage(String msg) {
        if (chatHistory != null) chatHistory.append(">>> " + msg + "\n");
    }

    // ── UI state ──────────────────────────────────────────────────────────────

    private void refreshUI() {
        TicTacToeGameResult result = game.getResult();
        if (result == TicTacToeGameResult.IN_PROGRESS) {
            String name = (game.getCurrentPlayer() == TicTacToePlayer.X) ? player1Name : player2Name;
            statusLabel.setText("Turn: " + game.getCurrentPlayer() + "  (" + name + ")");
        } else {
            handleGameOver(result);
        }
        if (boardView != null) boardView.repaint();
    }

    private void handleGameOver(TicTacToeGameResult result) {
        String msg;
        if (result == TicTacToeGameResult.X_WINS) {
            msg = player1Name + " (X) wins!";
        } else if (result == TicTacToeGameResult.O_WINS) {
            msg = player2Name + " (O) wins!";
        } else {
            msg = "It's a draw!";
        }
        statusLabel.setText(msg);
        appendSystemMessage(msg);
        JOptionPane.showMessageDialog(this, msg, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JButton styledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(MainGui.BTN_COL);
        btn.setForeground(MainGui.T_COL);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Board rendering ───────────────────────────────────────────────────────

    private class BoardView extends JPanel {

        BoardView() {
            setBackground(BG_BOARD);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (game.getResult() != TicTacToeGameResult.IN_PROGRESS) return;

                    int w = getWidth()  / GRID;
                    int h = getHeight() / GRID;
                    int col = e.getX() / w; // x → column
                    int row = e.getY() / h; // y → row

                    if (col < 0 || col >= GRID || row < 0 || row >= GRID) return;

                    // TicTacToeGame.playMove(x, y) where x=col, y=row
                    boolean moved = game.playMove(col, row);
                    if (!moved) {
                        appendSystemMessage("That cell is already taken.");
                        return;
                    }
                    refreshUI();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int cellW = w / GRID;
            int cellH = h / GRID;

            // Grid lines
            g2.setColor(LINE_COLOR);
            g2.setStroke(new BasicStroke(3));
            for (int i = 1; i < GRID; i++) {
                g2.drawLine(i * cellW, 0, i * cellW, h);
                g2.drawLine(0, i * cellH, w, i * cellH);
            }

            // Pieces
            TicTacToeBoard board = game.getBoard();
            int margin = Math.min(cellW, cellH) / 6;

            for (int row = 0; row < GRID; row++) {
                for (int col = 0; col < GRID; col++) {
                    TicTacToePlayer p = board.getPlayerAt(row, col);
                    if (p == null) continue;

                    int x = col * cellW + margin;
                    int y = row * cellH + margin;
                    int size = Math.min(cellW, cellH) - 2 * margin;

                    if (p == TicTacToePlayer.X) {
                        g2.setColor(X_COLOR);
                        g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawLine(x, y, x + size, y + size);
                        g2.drawLine(x + size, y, x, y + size);
                    } else {
                        g2.setColor(O_COLOR);
                        g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawOval(x, y, size, size);
                    }
                }
            }
        }
    }
}
