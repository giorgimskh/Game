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
- Java 8 or higher installed on your system

### Compile and Run

1. **Navigate to the project directory:**
   ```bash
   cd Game
   ```

2. **Compile the Java files:**
   ```bash
   javac -d . src/*.java
   ```

3. **Run the game:**
   ```bash
   java App
   ```

### Alternative: Run from src directory
```bash
cd src
javac *.java
java App
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
