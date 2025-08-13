# Snake Game

A classic Snake game implemented in Java using Swing GUI framework.

## Features

- **Arrow Key Controls**: Use arrow keys to control the snake's direction
- **Score Tracking**: Score increases by 10 points for each food eaten
- **Collision Detection**: Game ends when snake hits walls or itself
- **Restart Option**: Play again after game over
- **Smooth Gameplay**: 100ms game loop for responsive controls

## Controls

- **↑ (Up Arrow)**: Move snake up
- **↓ (Down Arrow)**: Move snake down
- **← (Left Arrow)**: Move snake left
- **→ (Right Arrow)**: Move snake right
- **Spacebar**: Restart game (when game is over)

## How to Run

### Prerequisites
- Java 8+ (prefer Java 11 or newer)

### Quick start (Windows)
1. Download or clone the repository.
2. Double-click `run.bat`, or run in a terminal:
   ```bat
   run.bat
   ```

### Quick start (macOS/Linux)
1. Make the script executable once:
   ```bash
   chmod +x run.sh
   ```
2. Run:
   ```bash
   ./run.sh
   ```

The script compiles sources to `bin/`, copies `src/sounds/` to `bin/sounds/`, and launches the game.

### Build a runnable JAR
- Windows:
  ```bat
  build-jar.bat
  ```
- macOS/Linux:
  ```bash
  chmod +x build-jar.sh
  ./build-jar.sh
  ```

Then run the jar:
```bash
java -jar dist/SnakeGame.jar
```

## Game Rules

- Control the snake to eat red food dots
- Each food eaten increases your score and snake length
- Avoid hitting the walls or the snake's own body
- Try to get the highest score possible!

## Game Window

- **Window Size**: 600x600 pixels (game area) + 50 pixels (score display)
- **Grid Size**: 25x25 pixel units
- **Snake**: Green head with darker green body
- **Food**: Red circular dots
- **Background**: Black

Enjoy playing Snake!
