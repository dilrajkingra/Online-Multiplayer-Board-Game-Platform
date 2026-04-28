import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import leaderboard_logic.*; 
import auth_logic.Player;
import auth_logic.PlayerData;

public class LeaderboardPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> gameSelector;
    private JLabel userRankLabel; // NEW: Shows "Your Rank: X"
    private final String[] columns = {"Rank", "Player Name", "Rating", "Status"};

    public LeaderboardPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(MainGui.BG_COL);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- Header ---
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setOpaque(false);
        JLabel title = new JLabel("Global Leaderboard");
        title.setForeground(MainGui.T_COL);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));

        String[] games = {"Chess", "Go", "Tic Tac Toe"};
        gameSelector = new JComboBox<>(games);
        gameSelector.setPreferredSize(new Dimension(150, 30));
        gameSelector.addActionListener(e -> refreshTable());

        headerPanel.add(title);
        headerPanel.add(Box.createHorizontalStrut(20));
        headerPanel.add(gameSelector);
        add(headerPanel, BorderLayout.NORTH);

        // --- Table ---
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- Footer (Smart Back Button + Rank) ---
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        // 1. Personal Rank Label (Hidden by default)
        userRankLabel = new JLabel(""); 
        userRankLabel.setForeground(Color.ORANGE);
        userRankLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        footer.add(userRankLabel, BorderLayout.WEST);

        // 2. Smart Back Button
        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> {
            // THE HARD CAVEAT FIX:
            if (MainGui.isLoggedIn()) {
                MainGui.showCard("LOBBY"); // If logged in, go to lobby
            } else {
                MainGui.showCard("LANDING"); // If guest, go to landing
            }
        });
        
        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnWrapper.setOpaque(false);
        btnWrapper.add(backBtn);
        footer.add(btnWrapper, BorderLayout.EAST);

        add(footer, BorderLayout.SOUTH);

        // Refresh on show
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent e) {
                refreshTable();
            }
        });
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        String selectedGame = (String) gameSelector.getSelectedItem();
        
        try {
            // 1. Fill Table
            List<Player> topPlayers = Leaderboard.getTopPlayers(selectedGame, 20);
            int rank = 1;
            for (Player p : topPlayers) {
                int rating = RankingAlgorithm.getPlayerRating(p, selectedGame);
                tableModel.addRow(new Object[]{rank++, p.getUsername(), rating, p.getStatus()});
            }

            // 2. Update Personal Rank (If Logged In)
            if (MainGui.isLoggedIn()) {
                String currentUser = MainGui.getCurrentUsername();
                // Find our user in the mock database
                Player me = PlayerData.players.stream()
                        .filter(p -> p.getUsername().equalsIgnoreCase(currentUser))
                        .findFirst()
                        .orElse(null);

                if (me != null) {
                    int myRating = RankingAlgorithm.getPlayerRating(me, selectedGame);
                    userRankLabel.setText("Your " + selectedGame + " Rating: " + myRating);
                } else {
                    // New user (not in mock data yet)
                    userRankLabel.setText("Your " + selectedGame + " Rating: 1200 (Unranked)");
                }
            } else {
                userRankLabel.setText(""); // Hide if guest
            }

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}