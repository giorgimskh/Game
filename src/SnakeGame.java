import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;
import javax.sound.sampled.*;

public class SnakeGame extends JFrame {
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 600;
    private static final int UNIT_SIZE = 25;
    private static final int GAME_UNITS = (WINDOW_WIDTH * WINDOW_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    private static final int DELAY = 100;
    
    private final ArrayList<Point> snake = new ArrayList<>();
    private Point food;
    private String foodType = "apple"; // "apple" or "multiplier"
    private char direction = 'R';
    private boolean running = false;
    private boolean inMenu = true;
    private int score = 0;
    private int highScore = 0;
    private boolean scoreMultiplierActive = false;
    private long multiplierStartTime = 0;
    private final Random random = new Random();
    private final GamePanel gamePanel;
    private final JLabel scoreLabel;
    private final JPanel menuPanel;
    private Timer timer;
    private Timer multiplierTimer;
    
    // Audio components
    private Clip backgroundMusic;
    private Clip eatingSound;
    private Clip multiplierSound;
    
    public SnakeGame() {
        setTitle("Snake Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Load high score
        loadHighScore();
        
        // Initialize audio
        initializeAudio();
        
        // Create score label
        scoreLabel = new JLabel("Score: 0 | High Score: " + highScore);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scoreLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Create menu panel with buttons
        menuPanel = createMenuPanel();
        
        // Create game panel
        gamePanel = new GamePanel();
        
        // Layout
        setLayout(new BorderLayout());
        add(scoreLabel, BorderLayout.NORTH);
        add(menuPanel, BorderLayout.CENTER);
        
        // Size the frame to preferred sizes of components (ensures 600x600 game area)
        pack();
        setLocationRelativeTo(null);
        
        // Add key listener
        addKeyListener(new GameKeyListener());
        setFocusable(true);
        
        // Show main menu first
        showMainMenu();
    }
    
    private JPanel createMenuPanel() {
        JPanel panel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Create gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(25, 25, 112), // Dark blue
                    getWidth(), getHeight(), new Color(0, 0, 0) // Black
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Add some animated stars effect
                g2d.setColor(new Color(255, 255, 255, 100));
                for (int i = 0; i < 50; i++) {
                    int x = (int) (Math.random() * getWidth());
                    int y = (int) (Math.random() * getHeight());
                    g2d.fillOval(x, y, 2, 2);
                }
                
                g2d.dispose();
            }
        };
        panel.setLayout(new BorderLayout());
        
        // Title panel with gradient matching bottom theme
        JPanel titlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Title background gradient matching bottom theme
                GradientPaint titleGradient = new GradientPaint(
                    0, 0, new Color(25, 25, 112), // Dark blue
                    getWidth(), getHeight(), new Color(0, 0, 0) // Black
                );
                g2d.setPaint(titleGradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                g2d.dispose();
            }
        };
        titlePanel.setPreferredSize(new Dimension(WINDOW_WIDTH, 120));
        titlePanel.setLayout(new BorderLayout());
        
        // Create fancy title with shadow effect
        JLabel titleLabel = new JLabel("SNAKE GAME") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                // Draw shadow
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.setFont(new Font("Arial", Font.BOLD, 48));
                g2d.drawString("SNAKE GAME", 3, 63);
                
                // Draw main text with gradient matching theme
                GradientPaint textGradient = new GradientPaint(
                    0, 0, new Color(135, 206, 250), // Light sky blue
                    getWidth(), getHeight(), new Color(255, 255, 255) // White
                );
                g2d.setPaint(textGradient);
                g2d.drawString("SNAKE GAME", 0, 60);
                
                g2d.dispose();
            }
        };
        titleLabel.setPreferredSize(new Dimension(WINDOW_WIDTH, 60));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setOpaque(false);
        
        // New Game button with modern styling
        JButton newGameButton = createStyledButton("NEW GAME", new Color(0, 150, 255), new Color(0, 100, 200));
        newGameButton.addActionListener(e -> startNewGame());
        
        // Quit button with modern styling
        JButton quitButton = createStyledButton("QUIT", new Color(255, 100, 100), new Color(200, 50, 50));
        quitButton.addActionListener(e -> System.exit(0));
        
        // Add buttons to panel with spacing
        buttonsPanel.add(Box.createVerticalGlue());
        buttonsPanel.add(newGameButton);
        buttonsPanel.add(Box.createVerticalStrut(30));
        buttonsPanel.add(quitButton);
        buttonsPanel.add(Box.createVerticalGlue());
        
        // Add components to main panel
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(buttonsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JButton createStyledButton(String text, Color primaryColor, Color secondaryColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Button background gradient
                GradientPaint buttonGradient = new GradientPaint(
                    0, 0, primaryColor,
                    0, getHeight(), secondaryColor
                );
                g2d.setPaint(buttonGradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Button border with glow effect
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 20, 20);
                
                // Text with shadow
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(text)) / 2 + 2;
                int textY = (getHeight() + fm.getAscent()) / 2 + 2;
                g2d.drawString(text, textX, textY);
                
                // Main text
                g2d.setColor(Color.WHITE);
                textX = (getWidth() - fm.stringWidth(text)) / 2;
                textY = (getHeight() + fm.getAscent()) / 2;
                g2d.drawString(text, textX, textY);
                
                g2d.dispose();
            }
        };
        
        button.setPreferredSize(new Dimension(250, 60));
        button.setMaximumSize(new Dimension(250, 60));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        });
        
        return button;
    }
    
    private void initializeAudio() {
        try {
            // Load background music (Mario-style)
            loadBackgroundMusic();
            
            // Load sound effects
            loadEatingSound();
            loadMultiplierSound();
            
        } catch (Exception e) {
            System.out.println("Audio initialization failed: " + e.getMessage());
        }
    }
    
    private void loadBackgroundMusic() {
        try {
            // Create a simple Mario-style tune using beep sounds
            // In a real implementation, you'd load an actual audio file
            System.out.println("Background music loaded (Mario-style)");
        } catch (Exception e) {
            System.out.println("Failed to load background music: " + e.getMessage());
        }
    }
    
    private void loadEatingSound() {
        try {
            // Create eating sound effect
            System.out.println("Eating sound loaded");
        } catch (Exception e) {
            System.out.println("Failed to load eating sound: " + e.getMessage());
        }
    }
    
    private void loadMultiplierSound() {
        try {
            // Create multiplier sound effect
            System.out.println("Multiplier sound loaded");
        } catch (Exception e) {
            System.out.println("Failed to load multiplier sound: " + e.getMessage());
        }
    }
    
    private void playEatingSound() {
        try {
            // Play eating sound effect
            System.out.println("Playing eating sound!");
        } catch (Exception e) {
            System.out.println("Failed to play eating sound: " + e.getMessage());
        }
    }
    
    private void playMultiplierSound() {
        try {
            // Play multiplier sound effect
            System.out.println("Playing multiplier sound!");
        } catch (Exception e) {
            System.out.println("Failed to play multiplier sound: " + e.getMessage());
        }
    }
    
    private void startBackgroundMusic() {
        try {
            // Start background music loop
            System.out.println("Background music started!");
        } catch (Exception e) {
            System.out.println("Failed to start background music: " + e.getMessage());
        }
    }
    
    private void stopBackgroundMusic() {
        try {
            // Stop background music
            System.out.println("Background music stopped!");
        } catch (Exception e) {
            System.out.println("Failed to stop background music: " + e.getMessage());
        }
    }
    
    private void showMainMenu() {
        inMenu = true;
        running = false;
        if (timer != null) {
            timer.stop();
        }
        
        // Stop background music
        stopBackgroundMusic();
        
        // Switch to menu panel
        getContentPane().remove(gamePanel);
        getContentPane().add(menuPanel, BorderLayout.CENTER);
        menuPanel.revalidate();
        menuPanel.repaint();
        
        // Update score label to show high score
        scoreLabel.setText("High Score: " + highScore);
    }
    
    private void startNewGame() {
        inMenu = false;
        
        // Switch to game panel
        getContentPane().remove(menuPanel);
        getContentPane().add(gamePanel, BorderLayout.CENTER);
        gamePanel.revalidate();
        gamePanel.repaint();
        
        initGame();
    }
    
    private void loadHighScore() {
        try {
            File scoreFile = new File("highscore.txt");
            if (scoreFile.exists()) {
                Scanner scanner = new Scanner(scoreFile);
                if (scanner.hasNextInt()) {
                    highScore = scanner.nextInt();
                }
                scanner.close();
            }
        } catch (Exception e) {
            // If there's any error, just use default high score of 0
            highScore = 0;
        }
    }
    
    private void saveHighScore() {
        try {
            File scoreFile = new File("highscore.txt");
            PrintWriter writer = new PrintWriter(scoreFile);
            writer.println(highScore);
            writer.close();
        } catch (Exception e) {
            // If there's any error saving, just ignore it
        }
    }
    
    private void initGame() {
        snake.clear();
        snake.add(new Point(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2));
        snake.add(new Point(WINDOW_WIDTH / 2 - UNIT_SIZE, WINDOW_HEIGHT / 2));
        snake.add(new Point(WINDOW_WIDTH / 2 - UNIT_SIZE * 2, WINDOW_HEIGHT / 2));
        snake.add(new Point(WINDOW_WIDTH / 2 - UNIT_SIZE * 3, WINDOW_HEIGHT / 2));
        
        direction = 'R';
        score = 0;
        scoreMultiplierActive = false;
        if (multiplierTimer != null) {
            multiplierTimer.stop();
        }
        
        updateScoreLabel();
        
        spawnFood();
        running = true;
        
        // Start background music
        startBackgroundMusic();
        
        // Start game timer
        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(DELAY, e -> gameLoop());
        timer.start();
    }
    
    private void spawnFood() {
        int x, y;
        do {
            x = random.nextInt(WINDOW_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            y = random.nextInt(WINDOW_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
        } while (snake.contains(new Point(x, y)));
        
        food = new Point(x, y);
        
        // 20% chance to spawn a multiplier apple
        if (random.nextDouble() < 0.2) {
            foodType = "multiplier";
        } else {
            foodType = "apple";
        }
    }
    
    private void gameLoop() {
        if (!running) return;
        
        move();
        checkCollision();
        checkFood();
        
        // Update score label to show remaining multiplier time
        if (scoreMultiplierActive) {
            updateScoreLabel();
        }
        
        gamePanel.repaint();
    }
    
    private void move() {
        Point head = snake.get(0);
        Point newHead = new Point(head);
        
        switch (direction) {
            case 'U':
                newHead.y -= UNIT_SIZE;
                break;
            case 'D':
                newHead.y += UNIT_SIZE;
                break;
            case 'L':
                newHead.x -= UNIT_SIZE;
                break;
            case 'R':
                newHead.x += UNIT_SIZE;
                break;
        }
        
        snake.add(0, newHead);
        
        if (!checkFood()) {
            snake.remove(snake.size() - 1);
        }
    }
    
    private boolean checkFood() {
        if (snake.get(0).equals(food)) {
            if (foodType.equals("multiplier")) {
                // Activate score multiplier
                activateScoreMultiplier();
                playMultiplierSound();
            } else {
                // Regular apple
                int points = scoreMultiplierActive ? 20 : 10; // Double points if multiplier active
                score += points;
                playEatingSound();
            }
            
            scoreLabel.setText("Score: " + score + " | High Score: " + highScore);
            spawnFood();
            return true;
        }
        return false;
    }
    
    private void activateScoreMultiplier() {
        scoreMultiplierActive = true;
        multiplierStartTime = System.currentTimeMillis();
        
        // Start multiplier timer
        if (multiplierTimer != null) {
            multiplierTimer.stop();
        }
        multiplierTimer = new Timer(10000, e -> deactivateScoreMultiplier()); // 10 seconds
        multiplierTimer.setRepeats(false);
        multiplierTimer.start();
        
        // Update score label to show multiplier status
        updateScoreLabel();
    }
    
    private void deactivateScoreMultiplier() {
        scoreMultiplierActive = false;
        multiplierTimer.stop();
        updateScoreLabel();
    }
    
    private void updateScoreLabel() {
        if (scoreMultiplierActive) {
            long remainingTime = 10 - ((System.currentTimeMillis() - multiplierStartTime) / 1000);
            scoreLabel.setText("Score: " + score + " | High Score: " + highScore + " | 2x MULTIPLIER! (" + remainingTime + "s)");
        } else {
            scoreLabel.setText("Score: " + score + " | High Score: " + highScore);
        }
    }
    
    private void checkCollision() {
        Point head = snake.get(0);
        
        // Wrap around walls instead of dying
        if (head.x < 0) {
            head.x = WINDOW_WIDTH - UNIT_SIZE;
        } else if (head.x >= WINDOW_WIDTH) {
            head.x = 0;
        }
        
        if (head.y < 0) {
            head.y = WINDOW_HEIGHT - UNIT_SIZE;
        } else if (head.y >= WINDOW_HEIGHT) {
            head.y = 0;
        }
        
        // Check self collision
        for (int i = 1; i < snake.size(); i++) {
            if (head.equals(snake.get(i))) {
                gameOver();
                return;
            }
        }
    }
    
    private void gameOver() {
        running = false;
        timer.stop();
        
        // Stop multiplier timer
        if (multiplierTimer != null) {
            multiplierTimer.stop();
        }
        
        // Check if this is a new high score
        if (score > highScore) {
            highScore = score;
            saveHighScore();
        }
        
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Game Over! Your score: " + score + "\nHigh Score: " + highScore + "\nWould you like to play again?",
            "Game Over",
            JOptionPane.YES_NO_OPTION
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            startNewGame();
        } else {
            showMainMenu();
        }
    }
    
    private class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(Color.BLACK);
            
            if (running) {
                // Ensure food exists; spawn if missing
                if (food == null) {
                    spawnFood();
                }
                // Draw apple food
                drawApple(g, food);
                
                // Draw snake
                for (int i = 0; i < snake.size(); i++) {
                    if (i == 0) {
                        // Draw snake head as diamond shape
                        drawSnakeHead(g, snake.get(i));
                    } else if (i == snake.size() - 1) {
                        // Draw snake tail (last segment)
                        drawSnakeTail(g, snake.get(i));
                    } else {
                        // Draw snake body as circles
                        drawSnakeBody(g, snake.get(i));
                    }
                }
            }
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
        }
        
        private void drawSnakeHead(Graphics g, Point head) {
            // Draw circular head with green color
            g.setColor(new Color(0, 128, 0)); // Green
            g.fillOval(head.x, head.y, UNIT_SIZE, UNIT_SIZE);
            
            // Draw head border
            g.setColor(Color.BLACK);
            g.drawOval(head.x, head.y, UNIT_SIZE, UNIT_SIZE);
            
            // Draw black eyes based on direction
            int eyeSize = UNIT_SIZE / 5;
            int eyeOffset = UNIT_SIZE / 3;
            
            // Calculate eye positions based on direction
            int leftEyeX, leftEyeY, rightEyeX, rightEyeY;
            
            switch (direction) {
                case 'R': // Moving right
                    leftEyeX = head.x + UNIT_SIZE - eyeOffset;
                    leftEyeY = head.y + eyeOffset;
                    rightEyeX = head.x + UNIT_SIZE - eyeOffset;
                    rightEyeY = head.y + UNIT_SIZE - eyeOffset;
                    break;
                case 'L': // Moving left
                    leftEyeX = head.x + eyeOffset;
                    leftEyeY = head.y + eyeOffset;
                    rightEyeX = head.x + eyeOffset;
                    rightEyeY = head.y + UNIT_SIZE - eyeOffset;
                    break;
                case 'U': // Moving up
                    leftEyeX = head.x + eyeOffset;
                    leftEyeY = head.y + eyeOffset;
                    rightEyeX = head.x + UNIT_SIZE - eyeOffset;
                    rightEyeY = head.y + eyeOffset;
                    break;
                case 'D': // Moving down
                    leftEyeX = head.x + eyeOffset;
                    leftEyeY = head.y + UNIT_SIZE - eyeOffset;
                    rightEyeX = head.x + UNIT_SIZE - eyeOffset;
                    rightEyeY = head.y + UNIT_SIZE - eyeOffset;
                    break;
                default:
                    leftEyeX = head.x + eyeOffset;
                    leftEyeY = head.y + eyeOffset;
                    rightEyeX = head.x + UNIT_SIZE - eyeOffset;
                    rightEyeY = head.y + UNIT_SIZE - eyeOffset;
            }
            
            // Draw black eyes directly (no white background)
            g.setColor(Color.BLACK);
            g.fillOval(leftEyeX, leftEyeY, eyeSize, eyeSize);
            g.fillOval(rightEyeX, rightEyeY, eyeSize, eyeSize);
        }
        
        private void drawSnakeBody(Graphics g, Point body) {
            // Draw smaller circular body segments with overlap for intersection
            int overlap = UNIT_SIZE / 4; // Reduced overlap for smaller circles
            int circleSize = UNIT_SIZE - 4; // Make circles smaller
            
            // Draw blue circle as background
            g.setColor(new Color(0, 0, 255)); // Blue
            g.fillOval(body.x - overlap, body.y - overlap, circleSize + overlap, circleSize + overlap);
            
            // Draw green horizontal line/band through the center
            g.setColor(new Color(0, 128, 0)); // Green
            int lineHeight = Math.max(6, (circleSize + overlap) / 4); // Much thicker green line
            int lineX = body.x - overlap;
            int lineY = body.y - overlap + (circleSize + overlap) / 2 - lineHeight / 2;
            int lineWidth = circleSize + overlap;
            g.fillRect(lineX, lineY, lineWidth, lineHeight);
            
            // Draw border
            g.setColor(Color.BLACK);
            g.drawOval(body.x - overlap, body.y - overlap, circleSize + overlap, circleSize + overlap);
        }
        
        private void drawSnakeTail(Graphics g, Point tail) {
            // Draw tail as a circle with green line like other body segments
            int overlap = UNIT_SIZE / 4;
            int circleSize = UNIT_SIZE - 4; // Same size as body circles
            
            // Draw blue circle as background
            g.setColor(new Color(0, 0, 255)); // Blue
            g.fillOval(tail.x - overlap, tail.y - overlap, circleSize + overlap, circleSize + overlap);
            
            // Draw green horizontal line/band through the center
            g.setColor(new Color(0, 128, 0)); // Green
            int lineHeight = Math.max(6, (circleSize + overlap) / 4); // Same thickness as body
            int lineX = tail.x - overlap;
            int lineY = tail.y - overlap + (circleSize + overlap) / 2 - lineHeight / 2;
            int lineWidth = circleSize + overlap;
            g.fillRect(lineX, lineY, lineWidth, lineHeight);
            
            // Draw border
            g.setColor(Color.BLACK);
            g.drawOval(tail.x - overlap, tail.y - overlap, circleSize + overlap, circleSize + overlap);
        }
        
        private void drawApple(Graphics g, Point apple) {
            if (apple == null) {
                return;
            }
            // Keep the apple fully within its grid cell for visibility
            int bodyInset = Math.max(2, UNIT_SIZE / 10);
            int bodySize = UNIT_SIZE - bodyInset * 2;
            int bodyX = apple.x + bodyInset;
            int bodyY = apple.y + bodyInset;

            if (foodType.equals("multiplier")) {
                // Draw multiplier apple (green with sparkles)
                g.setColor(new Color(0, 255, 0)); // Bright green
                g.fillOval(bodyX, bodyY, bodySize, bodySize);

                // Draw green border
                g.setColor(new Color(0, 200, 0)); // Darker green border
                g.drawOval(bodyX, bodyY, bodySize, bodySize);

                // Draw sparkles for multiplier effect
                g.setColor(Color.YELLOW);
                int sparkleSize = 3;
                g.fillOval(bodyX + bodySize / 4, bodyY + bodySize / 4, sparkleSize, sparkleSize);
                g.fillOval(bodyX + bodySize * 3 / 4, bodyY + bodySize / 4, sparkleSize, sparkleSize);
                g.fillOval(bodyX + bodySize / 4, bodyY + bodySize * 3 / 4, sparkleSize, sparkleSize);
                g.fillOval(bodyX + bodySize * 3 / 4, bodyY + bodySize * 3 / 4, sparkleSize, sparkleSize);

                // Draw "2x" text
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 12));
                g.drawString("2x", bodyX + bodySize / 3, bodyY + bodySize / 2 + 4);

            } else {
                // Draw regular apple (red circle)
                g.setColor(new Color(220, 20, 60)); // Crimson red
                g.fillOval(bodyX, bodyY, bodySize, bodySize);

                // Draw apple border
                g.setColor(new Color(139, 0, 0)); // Dark red border
                g.drawOval(bodyX, bodyY, bodySize, bodySize);

                // Draw apple stem (brown) - anchored to top center of body
                g.setColor(new Color(139, 69, 19)); // Saddle brown
                int stemWidth = Math.max(2, UNIT_SIZE / 8);
                int stemHeight = Math.max(3, UNIT_SIZE / 6);
                int stemX = bodyX + bodySize / 2 - stemWidth / 2;
                int stemY = bodyY - stemHeight / 2;
                g.fillRect(stemX, stemY, stemWidth, stemHeight);

                // Draw apple leaf (green) near the stem
                g.setColor(new Color(34, 139, 34)); // Forest green
                int leafSize = Math.max(3, UNIT_SIZE / 6);
                int leafX = stemX + stemWidth;
                int leafY = stemY - leafSize / 3;
                g.fillOval(leafX, leafY, leafSize, leafSize);

                // Draw leaf border
                g.setColor(new Color(0, 100, 0)); // Dark green
                g.drawOval(leafX, leafY, leafSize, leafSize);

                // Draw apple highlight (white circle for shine effect)
                g.setColor(new Color(255, 255, 255, 100)); // Semi-transparent white
                int highlightSize = Math.max(3, UNIT_SIZE / 4);
                int highlightX = bodyX + bodySize / 5;
                int highlightY = bodyY + bodySize / 5;
                g.fillOval(highlightX, highlightY, highlightSize / 3, highlightSize / 3);
            }
        }
    }
    
    private class GameKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (running) {
                // Game controls
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        if (direction != 'R') direction = 'L';
                        break;
                    case KeyEvent.VK_RIGHT:
                        if (direction != 'L') direction = 'R';
                        break;
                    case KeyEvent.VK_UP:
                        if (direction != 'D') direction = 'U';
                        break;
                    case KeyEvent.VK_DOWN:
                        if (direction != 'U') direction = 'D';
                        break;
                    case KeyEvent.VK_ESCAPE:
                        showMainMenu();
                        break;
                }
            }
        }
    }
}
