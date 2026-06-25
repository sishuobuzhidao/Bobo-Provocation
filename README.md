# Bobo Provocation 波波挑衅

A two-player turn-based combat game with resource management and predictive AI, originally co-created with one of my primary school classmates in 2017 as a face-to-face hand-gesture game.

## Tech Stack

- **Backend**: Java 17 + Spring Boot 4.1.0 + Maven
- **Frontend**: HTML / CSS / JavaScript (vanilla)

## How to Run

### Prerequisites

- Java 17+
- Maven 3.6+

### Backend

```bash
cd bobo_provocation
./mvnw spring-boot:run
```

Backend runs on `http://localhost:8080`.

### Frontend

Open `frontend/index.html` in a browser, or serve the `frontend/` directory with any static file server:

```bash
cd frontend
python3 -m http.server 3000
```

Then open `http://localhost:3000` in your browser.

## Game Modes

- **Normal Mode**: Only legal moves are clickable (greyed-out buttons for unavailable moves)
- **Ultra Hard Mode**: All 15 buttons are clickable. Selecting an illegal move results in a random legal replacement as penalty

## AI Difficulty Levels

| Level | Strategy |
|-------|----------|
| Easy | Pure random move selection |
| Normal | Static weighted random selection |
| Hard | Context-aware weights with opponent tracking and learning mechanism |

## Project Structure

```
bobo_provocation/
├── frontend/
│   ├── index.html          # Main game page
│   ├── style.css           # Styles (warm dark theme)
│   └── game.js             # Frontend game logic
├── src/main/java/com/sishuo/bobo_provocation/
│   ├── BoboProvocationApplication.java   # Spring Boot entry point
│   ├── BoboController.java               # REST API controller
│   ├── BoboGame.java                     # Game session manager
│   ├── Player.java                       # Player state & move logic (15x15 result table)
│   ├── ComputerMove.java                 # AI weight adjustment engine
│   └── StatusDTO.java                    # Frontend data transfer object
└── pom.xml
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/startGame` | Start a new game (params: `startGame`, `aiDifficulty`) |
| POST | `/api/analyzeMove` | Submit a move and get round result (params: `gameStarted`, `move`, `isUltraHardModeOn`) |

## About the Game

The game was originally designed as a two-player face-to-face game where players clap twice and simultaneously make a hand gesture representing their move. Players accumulate resources (provocations and defenses) to unlock stronger moves, from basic punches to ultimate attacks like Freeze, Gorilla, Critical Hit, and Layoff.

The full rules are not publicly released.
