import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.awt.Color;
import chess_logic.*; // Current package name, may be changed to omg. later

public class ChessGamePanel extends JPanel {

    // Logic
    private ChessGame game;
    private final int BOARD_SIZE = 8;
    
    // State
    private int selectedFile = -1;
    private int selectedRank = -1;
    private List<ChessMove> currentLegalMoves = new ArrayList<>();

    // GUI components
    private BoardView boardView;
    private JLabel statusLabel;
    private JLabel vsLabel;
    private Runnable onExitCallback;

    // Colors for board and possible move indicator
    private static final Color LIGHT_SQ = new Color(240, 217, 181); // Cream
    private static final Color DARK_SQ  = new Color(181, 136, 99);  // Brown
    private static final Color HIGHLIGHT_COLOR = new Color(100, 255, 100, 150); // Green transparent

    public ChessGamePanel(String player1Name, String player2Name, Runnable onExitCallback) {
        this.onExitCallback = onExitCallback;
        setLayout(new BorderLayout());
        setBackground(MainGui.BG_COL);

        // 1. Top Status Bar
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(MainGui.BG_COL);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Create "VS" label
        vsLabel = new JLabel(player1Name + " (White) vs. " + player2Name + " (Black)", SwingConstants.CENTER);
        vsLabel.setForeground(Color.ORANGE);
        vsLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        statusLabel = new JLabel("Turn: WHITE");
        statusLabel.setForeground(MainGui.T_COL);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        topPanel.add(statusLabel, BorderLayout.WEST);
        topPanel.add(vsLabel, BorderLayout.CENTER); // Add names here!
        add(topPanel, BorderLayout.NORTH);

        // Game board
        boardView = new BoardView();
        add(boardView, BorderLayout.CENTER);

        // Chat stub, not functional
        add(createChatStub(), BorderLayout.EAST);

        // Bottom options
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(MainGui.BG_COL);
        JButton leaveBtn = createStyledButton("Leave Match");
        bottomPanel.add(leaveBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        leaveBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to resign and leave?", "Resign?", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                onExitCallback.run();
            }
        });

        startNewGame();
    }

    public void startNewGame() {
        this.game = new ChessGame();
        resetSelection();
        updateUIState();
    }

    private void resetSelection() {
        selectedFile = -1;
        selectedRank = -1;
        currentLegalMoves.clear();
        repaint();
    }

    private void updateUIState() {
        // [cite: 184] Check status
        GameStatus status = game.getStatus();
        chess_logic.Color side = game.getSideToMove();
        
        String txt = "Turn: " + side;
        if (status == GameStatus.CHECKMATE) {
            txt = "CHECKMATE! Winner: " + side.opposite();
            JOptionPane.showMessageDialog(this, txt);
        } else if (status == GameStatus.STALEMATE) {
            txt = "Draw by Stalemate";
            JOptionPane.showMessageDialog(this, txt);
        }
        
        statusLabel.setText(txt);
        boardView.repaint();
    }

    // Chat stub appearance
    private JPanel createChatStub() {
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(250, 0));
        p.setBackground(new Color(30, 30, 30));
        JLabel lbl = new JLabel("Chat (Stub)", SwingConstants.CENTER);
        lbl.setForeground(Color.GRAY);
        p.add(lbl);
        return p;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(MainGui.BTN_COL);
        btn.setForeground(MainGui.T_COL);
        btn.setFocusPainted(false);
        return btn;
    }

    // Board drawing
    private class BoardView extends JPanel {
        private final int MARGIN = 20;
        private int cellSize;

        public BoardView() {
            //  Mouse click logic
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (game.getStatus() != GameStatus.RUNNING) return;

                    int c = (e.getX() - MARGIN) / cellSize;
                    int r = 7 - ((e.getY() - MARGIN) / cellSize); 

                    if (c < 0 || c > 7 || r < 0 || r > 7) return;

                    handleSquareClick(c, r);
                }
            });
        }

        private void handleSquareClick(int file, int rank) {
            if (selectedFile != -1) {
                // Checks for valid move destination
                boolean moveMade = game.makeMove(selectedFile, selectedRank, file, rank);
                // refresh screen
                if (moveMade) {
                    resetSelection();
                    updateUIState();
                    return;
                }
            }

            // If no move made (or just starting selection), select the piece
            Piece p = game.getBoardSnapshot()[file][rank];
            if (p != null && p.getColor() == game.getSideToMove()) {
                selectedFile = file;
                selectedRank = rank;
                //  Get legal moves for highlighting options
                currentLegalMoves = game.getLegalMovesFrom(file, rank);
                repaint();
            } else {
                resetSelection();
            }
        }

        // Rather than using assets, I'm currently drawing things within the code
        // Can be adjusted to use assets if needed
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int side = Math.min(getWidth(), getHeight());
            cellSize = (side - 2 * MARGIN) / BOARD_SIZE;
            int xOffset = MARGIN;
            int yOffset = MARGIN;

            //  Get snapshot only for drawing
            Piece[][] board = game.getBoardSnapshot();

            for (int r = 0; r < BOARD_SIZE; r++) { // GUI row (0 is top)
                for (int c = 0; c < BOARD_SIZE; c++) {
                    // Logic rank conversion: GUI Row 0 -> Logic Rank 7 (Black side)
                    int logicRank = 7 - r; 
                    int logicFile = c;

                    int x = xOffset + c * cellSize;
                    int y = yOffset + r * cellSize;

                    // Draw square
                    if ((r + c) % 2 == 0) g2.setColor(LIGHT_SQ);
                    else g2.setColor(DARK_SQ);
                    g2.fillRect(x, y, cellSize, cellSize);

                    // Highlight selection
                    if (logicFile == selectedFile && logicRank == selectedRank) {
                        g2.setColor(HIGHLIGHT_COLOR);
                        g2.fillRect(x, y, cellSize, cellSize);
                    }

                    // Highlight legal Moves
                    for (ChessMove m : currentLegalMoves) {
                        if (m.getToFile() == logicFile && m.getToRank() == logicRank) {
                            g2.setColor(HIGHLIGHT_COLOR);
                            g2.fillOval(x + cellSize/3, y + cellSize/3, cellSize/3, cellSize/3);
                        }
                    }

                    // Draw piece
                    Piece p = board[logicFile][logicRank];
                    if (p != null) {
                        drawPiece(g2, p, x, y, cellSize);
                    }
                }
            }
        }

        private void drawPiece(Graphics2D g2, Piece p, int x, int y, int size) {
            String symbol = "";
            // Use unicode escapes to represent pieces
            switch (p.getType()) {
                case KING:   symbol = "\u265A"; break; // King
                case QUEEN:  symbol = "\u265B"; break; // Queen
                case ROOK:   symbol = "\u265C"; break; // Rook
                case BISHOP: symbol = "\u265D"; break; // Bishop
                case KNIGHT: symbol = "\u265E"; break; // Knight
                case PAWN:   symbol = "\u265F"; break; // Pawn
            }

            g2.setFont(new Font("Serif", Font.PLAIN, size));
            
            // Draw outline for visibility
            if (p.getColor() == chess_logic.Color.WHITE) {
                g2.setColor(Color.WHITE);
            } else {
                g2.setColor(Color.BLACK);
            }
            
            // Center text
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (size - fm.stringWidth(symbol)) / 2;
            int ty = y + (size - fm.getHeight()) / 2 + fm.getAscent();
            
            g2.drawString(symbol, tx, ty);
        }
    }
}