import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import chess_logic.*;

public class ChessGamePanel extends JPanel {

    private static final Logger LOG = Logger.getLogger(ChessGamePanel.class.getName());

    // Board constants
    private static final int BOARD_SIZE   = 8;
    private static final int MARGIN       = 20;
    private static final Color LIGHT_SQ       = new Color(240, 217, 181);
    private static final Color DARK_SQ        = new Color(181, 136, 99);
    private static final Color HIGHLIGHT_COLOR = new Color(100, 255, 100, 150);

    // Logic
    private ChessGame game;

    // Selection state
    private int selectedFile = -1;
    private int selectedRank = -1;
    private List<ChessMove> currentLegalMoves = new ArrayList<>();

    // GUI components
    private BoardView boardView;
    private JLabel statusLabel;
    private JLabel vsLabel;
    private JTextArea chatHistory;
    private JTextField chatInput;
    private final Runnable onExitCallback;

    public ChessGamePanel(String player1Name, String player2Name, Runnable onExitCallback) {
        this.onExitCallback = onExitCallback;
        setLayout(new BorderLayout());
        setBackground(MainGui.BG_COL);

        // ── Top status bar ────────────────────────────────────────────────────
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(MainGui.BG_COL);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        vsLabel = new JLabel(player1Name + " (White) vs. " + player2Name + " (Black)", SwingConstants.CENTER);
        vsLabel.setForeground(Color.ORANGE);
        vsLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        statusLabel = new JLabel("Turn: WHITE");
        statusLabel.setForeground(MainGui.T_COL);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        topPanel.add(statusLabel, BorderLayout.WEST);
        topPanel.add(vsLabel,     BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ── Board ─────────────────────────────────────────────────────────────
        boardView = new BoardView();
        add(boardView, BorderLayout.CENTER);

        // ── Chat panel ────────────────────────────────────────────────────────
        add(createChatPanel(), BorderLayout.EAST);

        // ── Bottom bar ────────────────────────────────────────────────────────
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(MainGui.BG_COL);
        JButton leaveBtn = createStyledButton("Leave Match");
        bottomPanel.add(leaveBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        leaveBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to resign and leave?", "Resign?", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) onExitCallback.run();
        });

        startNewGame();
    }

    public void startNewGame() {
        game = new ChessGame();
        resetSelection();
        updateUIState();
        appendSystemMessage("Game started. White moves first.");
    }

    // ── Chat ──────────────────────────────────────────────────────────────────

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

    // ── Selection / move ──────────────────────────────────────────────────────

    private void resetSelection() {
        selectedFile = -1;
        selectedRank = -1;
        currentLegalMoves.clear();
        repaint();
    }

    private void updateUIState() {
        GameStatus status = game.getStatus();
        chess_logic.Color side = game.getSideToMove();

        String txt = "Turn: " + side;
        if (status == GameStatus.CHECKMATE) {
            txt = "CHECKMATE! Winner: " + side.opposite();
            appendSystemMessage(txt);
            JOptionPane.showMessageDialog(this, txt);
        } else if (status == GameStatus.STALEMATE) {
            txt = "Draw by Stalemate";
            appendSystemMessage(txt);
            JOptionPane.showMessageDialog(this, txt);
        }

        statusLabel.setText(txt);
        boardView.repaint();
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(MainGui.BTN_COL);
        btn.setForeground(MainGui.T_COL);
        btn.setFocusPainted(false);
        return btn;
    }

    // ── Board rendering ───────────────────────────────────────────────────────

    private class BoardView extends JPanel {
        private int cellSize;

        BoardView() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (game.getStatus() != GameStatus.RUNNING) return;

                    int c = (e.getX() - MARGIN) / cellSize;
                    int r = 7 - ((e.getY() - MARGIN) / cellSize);

                    if (c < 0 || c >= BOARD_SIZE || r < 0 || r >= BOARD_SIZE) return;
                    handleSquareClick(c, r);
                }
            });
        }

        private void handleSquareClick(int file, int rank) {
            if (selectedFile != -1) {
                boolean moveMade = game.makeMove(selectedFile, selectedRank, file, rank);
                if (moveMade) {
                    resetSelection();
                    updateUIState();
                    return;
                }
            }

            Piece p = game.getBoardSnapshot()[file][rank];
            if (p != null && p.getColor() == game.getSideToMove()) {
                selectedFile = file;
                selectedRank = rank;
                currentLegalMoves = game.getLegalMovesFrom(file, rank);
                repaint();
            } else {
                resetSelection();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int side = Math.min(getWidth(), getHeight());
            cellSize = (side - 2 * MARGIN) / BOARD_SIZE;

            Piece[][] board = game.getBoardSnapshot();

            for (int r = 0; r < BOARD_SIZE; r++) {
                for (int c = 0; c < BOARD_SIZE; c++) {
                    int logicRank = 7 - r;
                    int logicFile = c;

                    int x = MARGIN + c * cellSize;
                    int y = MARGIN + r * cellSize;

                    g2.setColor(((r + c) % 2 == 0) ? LIGHT_SQ : DARK_SQ);
                    g2.fillRect(x, y, cellSize, cellSize);

                    if (logicFile == selectedFile && logicRank == selectedRank) {
                        g2.setColor(HIGHLIGHT_COLOR);
                        g2.fillRect(x, y, cellSize, cellSize);
                    }

                    for (ChessMove m : currentLegalMoves) {
                        if (m.getToFile() == logicFile && m.getToRank() == logicRank) {
                            g2.setColor(HIGHLIGHT_COLOR);
                            g2.fillOval(x + cellSize / 3, y + cellSize / 3, cellSize / 3, cellSize / 3);
                        }
                    }

                    Piece p = board[logicFile][logicRank];
                    if (p != null) drawPiece(g2, p, x, y, cellSize);
                }
            }
        }

        private void drawPiece(Graphics2D g2, Piece p, int x, int y, int size) {
            String symbol;
            switch (p.getType()) {
                case KING:   symbol = "♚"; break;
                case QUEEN:  symbol = "♛"; break;
                case ROOK:   symbol = "♜"; break;
                case BISHOP: symbol = "♝"; break;
                case KNIGHT: symbol = "♞"; break;
                default:     symbol = "♟"; break;
            }

            g2.setFont(new Font("Serif", Font.PLAIN, size));
            g2.setColor(p.getColor() == chess_logic.Color.WHITE ? Color.WHITE : Color.BLACK);

            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (size - fm.stringWidth(symbol)) / 2;
            int ty = y + (size - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(symbol, tx, ty);
        }
    }
}
