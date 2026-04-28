# OMG — Online Multiplayer Board Game Platform

**SENG 300 — Fall 2025**

---

## Project Description

OMG is a desktop multiplayer board game platform built in Java/Swing. Players can create accounts, log in, queue for matchmaking, join private lobbies, and play Chess, Go, and Tic-Tac-Toe. An in-game chat panel, a global leaderboard, and player profiles are included.

---

## Technology Stack

| Concern | Choice |
|---|---|
| Language | Java 22 (JDK 22) |
| GUI | Java Swing (`javax.swing`) |
| Build | Gradle 8+ (`build.gradle`) |
| Testing | JUnit 5 (Jupiter) + JUnit 4 (vintage, for legacy tests) |
| Persistence | `java.util.Properties` files on disk |
| Authentication | SHA-256 + random salt (JDK `MessageDigest`) |
| Logging | `java.util.logging` |

---

## Getting Started

### Prerequisites

- JDK 22 installed and on your `PATH`
- Gradle 8+ **or** use the included Gradle wrapper once added (`./gradlew`)

### Build and run

```bash
# From the project root (where build.gradle lives)
./gradlew run
```

### Run all tests

```bash
./gradlew test
```

Test results are written to `build/reports/tests/test/index.html`.

### Compile manually (without Gradle)

```bash
# From the src/ directory
javac -d out $(find . -name "*.java")
java -cp out MainGui
```

---

## Project Structure

```
seng300-p1-main/
├── build.gradle              # Gradle build — dependencies, source sets, test config
├── settings.gradle           # Project name
├── src/
│   ├── MainGui.java          # Entry point + all screen navigation (CardLayout)
│   ├── AuthService.java      # Registration & login (SHA-256 hashed passwords)
│   ├── ChessGamePanel.java   # Chess board UI + chat
│   ├── GoGamePanel.java      # Go board UI + chat
│   ├── TicTacToeGamePanel.java  # Tic-Tac-Toe board UI + chat
│   ├── LeaderboardPanel.java # Global leaderboard screen
│   ├── auth_logic/
│   │   ├── Player.java       # Account model
│   │   ├── PlayerStats.java  # Per-game ratings and win/loss/tie counts
│   │   ├── PlayerData.java   # In-memory player list + file persistence
│   │   └── WriterDatabase.java  # Persistence facade
│   ├── chess_logic/          # Full chess engine (move gen, check, castling, promotion)
│   ├── go_logic/             # Go engine (liberty/capture detection, suicide prevention)
│   ├── TicTacToe_Logic/      # Tic-Tac-Toe engine (win detection, draw)
│   ├── matchmaking/
│   │   ├── Matchmaker.java   # Rating-based queue (sorted insert, O(log n))
│   │   ├── LobbyManager.java # Private lobbies with UUID-based IDs
│   │   ├── Lobby.java        # Lobby model
│   │   ├── Match.java        # Match result (two players + game type)
│   │   ├── Player.java       # Matchmaking player (id, name, rating)
│   │   └── GameType.java     # Enum: CHESS | GO | TIC_TAC_TOE
│   └── leaderboard_logic/
│       ├── Leaderboard.java      # Top-N query
│       ├── RankingAlgorithm.java # Rating lookup by game
│       ├── LeaderboardConfig.java  # K-factor constants
│       └── AdminControls.java    # Leaderboard reset (admin use)
└── test/
    ├── AuthServiceTest.java    # 9 JUnit 5 tests — register, authenticate, edge cases
    ├── MatchmakerTest.java     # 9 JUnit 5 tests — queue, match, rating window, sort order
    └── LobbyManagerTest.java   # 11 JUnit 5 tests — create, join, full, null guards
```

---

## Features

### Authentication
- Account registration with username, email, and password
- Passwords hashed with SHA-256 + a random 16-byte salt (stored in `users.properties`)
- Login validates against stored hash — plaintext passwords are never persisted
- Minimum 6-character password and basic email format enforced at registration

### Games
| Game | Status |
|---|---|
| Chess | Full rules — castling, en passant, pawn promotion, check/checkmate/stalemate detection |
| Go (9×9) | Stone placement, group/liberty capture, suicide prevention, pass, resign |
| Tic-Tac-Toe | Complete win/draw detection, fully playable UI |

All games include an in-game chat panel and a resign/leave confirmation dialog.

### Matchmaking
- Rating-based queue with a ±200 rating window
- Players inserted in sorted order at enqueue time — no re-sort on every match scan
- Private lobbies with 6-character UUID-derived IDs (no stack-overflow collision risk)
- Bot injection for single-player testing when no real opponent is available

### Persistence
- Player data (ratings, win/loss/tie counts) saved to `playerdata.properties` on disk
- Survives application restarts
- Leaderboard populated from persisted data; new registrations automatically added

### Leaderboard
- Top-20 players per game type (Chess, Go, Tic-Tac-Toe)
- Shows logged-in player's current rating at the bottom
- Smart back button — returns to lobby if logged in, landing page if guest

---

## Data Files

Two files are created automatically on first run in the working directory:

| File | Contents |
|---|---|
| `users.properties` | Usernames → `salt:hash:email` (do not edit manually) |
| `playerdata.properties` | Serialised player ratings and stats |

---

## Known Limitations / Future Work

- **No real networking** — multiplayer is simulated locally; a WebSocket or TCP server is needed for true online play
- **Single-player only** — both sides are controlled on the same machine
- **No bcrypt** — SHA-256 + salt is used because the project has no external dependencies; bcrypt would be preferable in a production deployment
- **Friends list is a stub** — displays mock data, no real friend management
- **No account deletion or password reset** flow

---

## Iteration History

| Iteration | Scope |
|---|---|
| 1 | Requirements analysis, use case descriptions (UC-1 – UC-8), initial class structure, architecture diagram — all integration points were stubs |
| 2 | Full game engines (Chess, Go, Tic-Tac-Toe), Swing UI, matchmaking queue, leaderboard, real authentication, file persistence, Gradle build, JUnit test suite |
