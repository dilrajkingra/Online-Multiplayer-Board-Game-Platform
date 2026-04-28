import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;
import matchmaking.*;

public class MainGui {

    private static final Logger LOG = Logger.getLogger(MainGui.class.getName());

    // UI color palette
    public static final Color BG_COL  = new Color(20, 20, 20);
    public static final Color T_COL   = new Color(228, 228, 228);
    public static final Color BTN_COL = new Color(35, 35, 35);

    private static JFrame frame;
    private static JPanel mainPanel;
    private static CardLayout cardLayout;

    private static String currentUsername = "Guest";
    private static String currentBio      = "Ready to play some games!";
    private static JLabel lobbyWelcomeLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainGui::start);
    }

    public static boolean isLoggedIn() {
        return !currentUsername.equals("Guest");
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static void logout() {
        currentUsername = "Guest";
        currentBio      = "Ready to play some games!";
        showCard("LANDING");
    }

    static void start() {
        frame = new JFrame("OMG -- Online Multiplayer Board Game Platform");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel  = new JPanel(cardLayout);
        frame.setContentPane(mainPanel);

        mainPanel.add(createLandingPanel(),  "LANDING");
        mainPanel.add(createLoginPanel(),    "LOGIN");
        mainPanel.add(createLobbyPanel(),    "LOBBY");
        mainPanel.add(createProfilePanel(),  "PROFILE");
        mainPanel.add(createRegisterPanel(), "REGISTER");
        mainPanel.add(new LeaderboardPanel(), "LEADERBOARD");

        cardLayout.show(mainPanel, "LANDING");
        frame.setVisible(true);
    }

    public static void showCard(String cardName) {
        cardLayout.show(mainPanel, cardName);
    }

    // ── Shared widget factory ─────────────────────────────────────────────────

    private static JButton styledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(BTN_COL);
        button.setForeground(T_COL);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    // ── Screens ───────────────────────────────────────────────────────────────

    private static JPanel createLandingPanel() {
        JPanel base = new JPanel(new BorderLayout(16, 16));
        base.setBackground(BG_COL);

        JLabel title = new JLabel("Online Multiplayer Board Game Platform", SwingConstants.CENTER);
        title.setForeground(T_COL);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        base.add(title, BorderLayout.NORTH);

        JButton loginBtn = styledButton("Log In");
        JButton regBtn   = styledButton("Register");
        JButton lbrdBtn  = styledButton("View Leaderboard");

        loginBtn.addActionListener(e -> showCard("LOGIN"));
        regBtn.addActionListener(e -> showCard("REGISTER"));
        lbrdBtn.addActionListener(e -> showCard("LEADERBOARD"));

        JPanel startButtons = new JPanel();
        startButtons.setOpaque(false);
        startButtons.setLayout(new BoxLayout(startButtons, BoxLayout.Y_AXIS));
        startButtons.add(Box.createVerticalGlue());
        startButtons.add(loginBtn);
        startButtons.add(Box.createVerticalStrut(10));
        startButtons.add(regBtn);
        startButtons.add(Box.createVerticalStrut(10));
        startButtons.add(lbrdBtn);
        startButtons.add(Box.createVerticalGlue());

        base.add(startButtons, BorderLayout.CENTER);
        return base;
    }

    private static JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COL);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.gridx  = 0;

        JLabel header = new JLabel("Player Login", SwingConstants.CENTER);
        header.setForeground(T_COL);
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        gbc.gridy = 0; panel.add(header, gbc);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(T_COL);
        gbc.gridy = 1; panel.add(userLabel, gbc);

        JTextField userField = new JTextField(20);
        gbc.gridy = 2; panel.add(userField, gbc);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(T_COL);
        gbc.gridy = 3; panel.add(passLabel, gbc);

        JPasswordField passField = new JPasswordField(20);
        gbc.gridy = 4; panel.add(passField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        JButton backBtn   = styledButton("Back");
        JButton submitBtn = styledButton("Login");
        buttonPanel.add(backBtn);
        buttonPanel.add(submitBtn);

        gbc.gridy = 5; gbc.insets = new Insets(20, 5, 5, 5);
        panel.add(buttonPanel, gbc);

        submitBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(panel,
                        "Please enter both a username and password.",
                        "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (username.equalsIgnoreCase("Guest")) {
                JOptionPane.showMessageDialog(panel,
                        "Login failed: invalid credentials.",
                        "Auth Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!AuthService.authenticate(username, password)) {
                LOG.warning("Failed login attempt for username: " + username);
                JOptionPane.showMessageDialog(panel,
                        "Login failed: incorrect username or password.",
                        "Auth Error", JOptionPane.ERROR_MESSAGE);
                passField.setText("");
                return;
            }

            currentUsername = username;
            if (lobbyWelcomeLabel != null) {
                lobbyWelcomeLabel.setText("Welcome, " + currentUsername + "!");
            }
            userField.setText("");
            passField.setText("");
            LOG.info("User logged in: " + username);
            showCard("LOBBY");
        });

        backBtn.addActionListener(e -> showCard("LANDING"));
        return panel;
    }

    private static JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COL);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.gridx  = 0;

        JLabel header = new JLabel("Create Account", SwingConstants.CENTER);
        header.setForeground(T_COL);
        header.setFont(new Font("SansSerif", Font.BOLD, 20));
        gbc.gridy = 0; panel.add(header, gbc);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(T_COL);
        gbc.gridy = 1; panel.add(userLabel, gbc);
        JTextField userField = new JTextField(20);
        gbc.gridy = 2; panel.add(userField, gbc);

        JLabel emailLabel = new JLabel("Email Address:");
        emailLabel.setForeground(T_COL);
        gbc.gridy = 3; panel.add(emailLabel, gbc);
        JTextField emailField = new JTextField(20);
        gbc.gridy = 4; panel.add(emailField, gbc);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(T_COL);
        gbc.gridy = 5; panel.add(passLabel, gbc);
        JPasswordField passField = new JPasswordField(20);
        gbc.gridy = 6; panel.add(passField, gbc);

        JLabel confirmLabel = new JLabel("Confirm Password:");
        confirmLabel.setForeground(T_COL);
        gbc.gridy = 7; panel.add(confirmLabel, gbc);
        JPasswordField confirmField = new JPasswordField(20);
        gbc.gridy = 8; panel.add(confirmField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        JButton backBtn = styledButton("Cancel");
        JButton regBtn  = styledButton("Sign Up");
        buttonPanel.add(backBtn);
        buttonPanel.add(regBtn);

        gbc.gridy = 9; gbc.insets = new Insets(20, 5, 5, 5);
        panel.add(buttonPanel, gbc);

        regBtn.addActionListener(e -> {
            String u   = userField.getText().trim();
            String mail = emailField.getText().trim();
            String p1  = new String(passField.getPassword());
            String p2  = new String(confirmField.getPassword());

            if (u.isEmpty() || mail.isEmpty() || p1.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please fill in all fields.",
                        "Registration Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (u.equalsIgnoreCase("Guest")) {
                JOptionPane.showMessageDialog(panel, "Cannot use that username.",
                        "Registration Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!mail.contains("@")) {
                JOptionPane.showMessageDialog(panel, "Please enter a valid email address.",
                        "Registration Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!p1.equals(p2)) {
                JOptionPane.showMessageDialog(panel, "Passwords do not match.",
                        "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (p1.length() < 6) {
                JOptionPane.showMessageDialog(panel, "Password must be at least 6 characters.",
                        "Registration Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (AuthService.userExists(u)) {
                JOptionPane.showMessageDialog(panel, "That username is already taken.",
                        "Registration Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean ok = AuthService.register(u, mail, p1);
            if (!ok) {
                JOptionPane.showMessageDialog(panel, "Registration failed. Please try again.",
                        "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LOG.info("New account registered: " + u);
            JOptionPane.showMessageDialog(panel,
                    "Account created successfully!\nPlease log in with your new credentials.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            userField.setText(""); emailField.setText("");
            passField.setText(""); confirmField.setText("");
            showCard("LOGIN");
        });

        backBtn.addActionListener(e -> showCard("LANDING"));
        return panel;
    }

    private static JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COL);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        JLabel header = new JLabel("Edit Profile", SwingConstants.CENTER);
        header.setForeground(T_COL);
        header.setFont(new Font("SansSerif", Font.BOLD, 24));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(header, gbc);

        JLabel userLabel = new JLabel("Display Name:");
        userLabel.setForeground(T_COL);
        gbc.gridy = 1; gbc.gridwidth = 1; panel.add(userLabel, gbc);

        JTextField userField = new JTextField(20);
        gbc.gridx = 1; panel.add(userField, gbc);

        JLabel bioLabel = new JLabel("Bio / Status:");
        bioLabel.setForeground(T_COL);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(bioLabel, gbc);

        JTextArea bioArea = new JTextArea(3, 20);
        bioArea.setLineWrap(true);
        JScrollPane bioScroll = new JScrollPane(bioArea);
        gbc.gridx = 1; panel.add(bioScroll, gbc);

        JLabel statsLabel = new JLabel("Stats: 5 Wins | 2 Losses");
        statsLabel.setForeground(Color.GRAY);
        gbc.gridx = 1; gbc.gridy = 3; panel.add(statsLabel, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        JButton cancelBtn = styledButton("Cancel");
        JButton saveBtn   = styledButton("Save Changes");
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);

        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent e) {
                userField.setText(currentUsername);
                bioArea.setText(currentBio);
            }
        });

        saveBtn.addActionListener(e -> {
            String newName = userField.getText().trim();
            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Username cannot be empty.");
                return;
            }
            currentUsername = newName;
            currentBio      = bioArea.getText().trim();
            if (lobbyWelcomeLabel != null) {
                lobbyWelcomeLabel.setText("Welcome, " + currentUsername + "!");
            }
            JOptionPane.showMessageDialog(panel, "Profile updated successfully!");
            showCard("LOBBY");
        });

        cancelBtn.addActionListener(e -> showCard("LOBBY"));
        return panel;
    }

    private static JPanel createLobbyPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BG_COL);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ── Profile header ────────────────────────────────────────────────────
        JPanel profilePanel = new JPanel(new BorderLayout());
        profilePanel.setOpaque(false);

        JPanel textWrapper = new JPanel();
        textWrapper.setLayout(new BoxLayout(textWrapper, BoxLayout.Y_AXIS));
        textWrapper.setOpaque(false);

        lobbyWelcomeLabel = new JLabel("Welcome, " + currentUsername + "!", SwingConstants.CENTER);
        lobbyWelcomeLabel.setForeground(T_COL);
        lobbyWelcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        lobbyWelcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statsLabel = new JLabel("Rank: 1200 | Wins: 5 | Losses: 2", SwingConstants.CENTER);
        statsLabel.setForeground(Color.GRAY);
        statsLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        textWrapper.add(lobbyWelcomeLabel);
        textWrapper.add(Box.createVerticalStrut(5));
        textWrapper.add(statsLabel);

        JButton editProfileBtn = new JButton("Edit Profile");
        editProfileBtn.setBackground(BTN_COL);
        editProfileBtn.setForeground(T_COL);
        editProfileBtn.setFocusPainted(false);
        editProfileBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        editProfileBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel editBtnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        editBtnWrapper.setOpaque(false);
        editBtnWrapper.add(editProfileBtn);

        profilePanel.add(textWrapper,   BorderLayout.CENTER);
        profilePanel.add(editBtnWrapper, BorderLayout.EAST);
        panel.add(profilePanel, BorderLayout.NORTH);

        // ── Dashboard ─────────────────────────────────────────────────────────
        JPanel dashboard = new JPanel(new GridBagLayout());
        dashboard.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(10, 10, 10, 10);
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        String[] gameOptions = {"Go (9x9)", "Chess", "Tic-Tac-Toe"};
        JComboBox<String> gameSelector = new JComboBox<>(gameOptions);
        gameSelector.setBackground(BTN_COL);
        gameSelector.setForeground(T_COL);
        gameSelector.setFont(new Font("SansSerif", Font.BOLD, 14));
        gameSelector.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), "Select Game Mode",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("SansSerif", Font.PLAIN, 12), Color.GRAY));

        JButton queueBtn  = styledButton("Find Match (Queue)");
        queueBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
        queueBtn.setBackground(new Color(40, 100, 40));

        JButton createBtn = styledButton("Create Private Lobby");
        JButton lbrdBtn   = styledButton("View Leaderboard");
        JButton friendsBtn = styledButton("Friends List");

        JPanel joinPanel = new JPanel(new BorderLayout(5, 0));
        joinPanel.setOpaque(false);
        JTextField lobbyIdField = new JTextField("Enter Lobby ID...");
        JButton joinBtn = styledButton("Join");
        joinPanel.add(lobbyIdField, BorderLayout.CENTER);
        joinPanel.add(joinBtn,      BorderLayout.EAST);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        dashboard.add(gameSelector, gbc);
        gbc.gridx = 1; dashboard.add(queueBtn, gbc);

        gbc.gridx = 0; gbc.gridy = 1; dashboard.add(createBtn, gbc);
        gbc.gridx = 1; dashboard.add(joinPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 2; dashboard.add(lbrdBtn, gbc);
        gbc.gridx = 1; dashboard.add(friendsBtn, gbc);

        panel.add(dashboard, BorderLayout.CENTER);

        // ── Footer ────────────────────────────────────────────────────────────
        JButton logoutBtn = styledButton("Log Out");
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(logoutBtn);
        panel.add(footer, BorderLayout.SOUTH);

        // ── Action listeners ──────────────────────────────────────────────────

        editProfileBtn.addActionListener(e -> showCard("PROFILE"));

        friendsBtn.addActionListener(e -> {
            String[] mockFriends = {
                "Alice (Online – In Lobby)",
                "Bob (Online – Playing Chess)",
                "Charlie (Offline)",
                "Dave (Offline)"
            };
            JOptionPane.showMessageDialog(panel, new JList<>(mockFriends),
                    "Friends List", JOptionPane.PLAIN_MESSAGE);
        });

        Matchmaker matchmaker = new Matchmaker();

        queueBtn.addActionListener(e -> {
            String selectedGameStr = (String) gameSelector.getSelectedItem();

            GameType gameType;
            if (selectedGameStr.startsWith("Go")) {
                gameType = GameType.GO;
            } else if (selectedGameStr.equals("Chess")) {
                gameType = GameType.CHESS;
            } else {
                gameType = GameType.TIC_TAC_TOE;
            }

            queueBtn.setText("Queuing for " + gameType + "...");
            queueBtn.setEnabled(false);
            gameSelector.setEnabled(false);

            Timer t = new Timer(1500, evt -> {
                try {
                    matchmaking.Player me  = new matchmaking.Player("u1", currentUsername, 1200);
                    matchmaking.Player bot = new matchmaking.Player("bot", "AutoBot", 1200);

                    matchmaker.enqueue(me, gameType);
                    matchmaking.Match match = matchmaker.findMatch();

                    if (match == null) {
                        LOG.info("No opponent found — injecting bot.");
                        matchmaker.enqueue(bot, gameType);
                        match = matchmaker.findMatch();
                    }

                    if (match != null) {
                        LOG.info("Match created: " + match);

                        queueBtn.setText("Find Match (Queue)");
                        queueBtn.setEnabled(true);
                        gameSelector.setEnabled(true);

                        String p1 = match.getPlayer1().getName();
                        String p2 = match.getPlayer2().getName();

                        if (gameType == GameType.GO) {
                            GoGamePanel goPanel = new GoGamePanel(p1, p2, () -> showCard("LOBBY"));
                            mainPanel.add(goPanel, "GAME");
                            goPanel.startNewGame();
                        } else if (gameType == GameType.CHESS) {
                            ChessGamePanel chessPanel = new ChessGamePanel(p1, p2, () -> showCard("LOBBY"));
                            mainPanel.add(chessPanel, "GAME");
                        } else {
                            TicTacToeGamePanel tttPanel = new TicTacToeGamePanel(p1, p2, () -> showCard("LOBBY"));
                            mainPanel.add(tttPanel, "GAME");
                        }

                        showCard("GAME");
                    } else {
                        queueBtn.setText("Find Match (Queue)");
                        queueBtn.setEnabled(true);
                        gameSelector.setEnabled(true);
                        JOptionPane.showMessageDialog(panel, "No match found. Please try again.");
                    }

                } catch (Exception ex) {
                    LOG.severe("Matchmaking error: " + ex.getMessage());
                    queueBtn.setText("Find Match (Queue)");
                    queueBtn.setEnabled(true);
                    gameSelector.setEnabled(true);
                    JOptionPane.showMessageDialog(panel,
                            "An error occurred during matchmaking. Please try again.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            t.setRepeats(false);
            t.start();
        });

        logoutBtn.addActionListener(e -> logout());
        lbrdBtn.addActionListener(e -> showCard("LEADERBOARD"));

        return panel;
    }
}
