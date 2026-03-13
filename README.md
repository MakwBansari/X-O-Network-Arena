# X-O Network Arena - Run Instructions

This project is a terminal-based Java implementation of Tic Tac Toe using a Client-Server architecture.

## Requirements
- Java Development Kit (JDK) 8 or higher.
- SQLite JDBC Driver (included in the instructions if you need to download it).

## How to Run

### Step 1: Compile the Project
You can use the provided build scripts instead of running `javac` manually.

**On Windows (PowerShell):**
```powershell
.\build.ps1
```

**On Linux/Mac/Git Bash (Bash):**
```bash
chmod +x build.sh
./build.sh
```

If you prefer manual compilation:
```powershell
javac -d bin src/com/xonetwork/common/*.java src/com/xonetwork/db/*.java src/com/xonetwork/server/*.java src/com/xonetwork/client/*.java
```

### Step 2: Start the Server
In the same or a new terminal, use the run script for your OS:

**Windows (PowerShell):**
```powershell
.\run-server.ps1
```

**Manual/Other:**
```powershell
java -cp "bin;lib/*" com.xonetwork.server.GameServer
```

### Step 3: Connect Clients
Open two separate terminals for the two players and run:

**Windows (PowerShell):**
```powershell
.\run-client.ps1
```

**Manual/Other:**
```powershell
java -cp "bin" com.xonetwork.client.GameClient
```

## Features
- **Real-time Gameplay**: TCP-based communication via Java Sockets.
- **Multi-threading**: Server handles multiple clients concurrently.
### 🎮 Gameplay Instructions
1.  **Enter Name**: Each client will ask for your name.
2.  **Make a Move**: When it's your turn, enter the coordinates in **Letter-Number** format:
    - Row: **A, B, C**
    - Column: **1, 2, 3**
    - Example: `A1` (Top-Left), `B2` (Center), `C3` (Bottom-Right).
3.  **Chat**: You can chat anytime by typing `/chat <your message>`.
4.  **Replay**: After a game ends, type `REPLAY` to start a new round.
5.  **Exit**: Type `/exit` to close the game.
- **Database Persistence**: Match history and player statistics are stored in `xonetwork.db` (SQLite).
- **Match Logging**: Every move and result is logged in `match_history.log`.

## Project Structure (MVC)
- `com.xonetwork.common`: `GameBoard` and `GameLogic` (Model/Logic).
- `com.xonetwork.db`: `DatabaseManager` (Data Handling).
- `com.xonetwork.server`: `GameServer` and `ClientHandler` (Controller/Network).
- `com.xonetwork.client`: `GameClient` (View/Interface).
