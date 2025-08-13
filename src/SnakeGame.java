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
    
    // Level system
    private int currentLevel = 1;
    private long gameStartTime;
    private long levelTimeLimit = 180000; // 3 minutes in milliseconds
    private Timer appleTimer; // For level 3+ apple disappearing
    private boolean appleVisible = true;
    
    // Bomb system for level 4
    private Point bomb;
    private Timer bombTimer;
    private boolean bombVisible = false;
    
    // AI Snake system for level 5
    private ArrayList<Point> aiSnake;
    private char aiDirection = 'L';
    private Timer aiMoveTimer;
    private boolean aiSnakeActive = false;
	private int aiScore = 0;

	// HUD components (separate, smoother UI)
	private JPanel hudPanel;
	private JLabel levelLabel;
	private JLabel playerScoreLabel;
	private JLabel aiScoreLabel;
	private JLabel highScoreLabel;
	private JLabel multiplierLabel;
    
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
		// Reset high score on every run as requested
		resetHighScoreOnStartup();
        
        // Initialize audio
        initializeAudio();
        
		// Create legacy score label (kept but not added to layout)
		scoreLabel = new JLabel("Score: 0 | High Score: " + highScore);
		scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
		scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
		scoreLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

		// Create modern HUD panel with separate labels
		hudPanel = new JPanel(new GridBagLayout());
		hudPanel.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(6, 10, 6, 10);
		gbc.gridy = 0;

		levelLabel = new JLabel();
		levelLabel.setFont(new Font("Arial", Font.BOLD, 14));
		gbc.gridx = 0; gbc.anchor = GridBagConstraints.WEST;
		hudPanel.add(levelLabel, gbc);

		playerScoreLabel = new JLabel();
		playerScoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
		gbc.gridx = 1; gbc.anchor = GridBagConstraints.CENTER;
		hudPanel.add(playerScoreLabel, gbc);

		aiScoreLabel = new JLabel();
		aiScoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
		gbc.gridx = 2; gbc.anchor = GridBagConstraints.CENTER;
		hudPanel.add(aiScoreLabel, gbc);

		highScoreLabel = new JLabel();
		highScoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
		gbc.gridx = 3; gbc.anchor = GridBagConstraints.EAST;
		hudPanel.add(highScoreLabel, gbc);

		multiplierLabel = new JLabel();
		multiplierLabel.setFont(new Font("Arial", Font.BOLD, 12));
		multiplierLabel.setForeground(new Color(0, 255, 127));
		gbc.gridx = 4; gbc.anchor = GridBagConstraints.EAST;
		hudPanel.add(multiplierLabel, gbc);
        
        // Create menu panel with buttons
        menuPanel = createMenuPanel();
        
        // Create game panel
        gamePanel = new GamePanel();
        
        // Layout
		setLayout(new BorderLayout());
		add(hudPanel, BorderLayout.NORTH);
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
        
        // Level selection buttons
        JButton level1Button = createStyledButton("LEVEL 1 - Desert Time Attack (3 min)", new Color(0, 150, 255), new Color(0, 100, 200));
        level1Button.addActionListener(e -> startLevel(1));
        
        JButton level2Button = createStyledButton("LEVEL 2 - Grass Speed Challenge", new Color(255, 150, 0), new Color(200, 100, 0));
        level2Button.addActionListener(e -> startLevel(2));
        
        JButton level3Button = createStyledButton("LEVEL 3 - Ocean Speed + Vanishing", new Color(255, 100, 100), new Color(200, 50, 50));
        level3Button.addActionListener(e -> startLevel(3));
        
        JButton level4Button = createStyledButton("LEVEL 4 - Forest Speed + Vanishing + Bomb", new Color(128, 0, 128), new Color(100, 0, 100));
        level4Button.addActionListener(e -> startLevel(4));
        
        JButton level5Button = createStyledButton("LEVEL 5 - Space AI Snake Battle + Bomb", new Color(255, 20, 147), new Color(199, 21, 133));
        level5Button.addActionListener(e -> startLevel(5));
        
        // Quit button with modern styling
        JButton quitButton = createStyledButton("QUIT", new Color(128, 128, 128), new Color(100, 100, 100));
        quitButton.addActionListener(e -> System.exit(0));
        
        // Add buttons to panel with spacing
        buttonsPanel.add(Box.createVerticalGlue());
        buttonsPanel.add(level1Button);
        buttonsPanel.add(Box.createVerticalStrut(20));
        buttonsPanel.add(level2Button);
        buttonsPanel.add(Box.createVerticalStrut(20));
        buttonsPanel.add(level3Button);
        buttonsPanel.add(Box.createVerticalStrut(20));
        buttonsPanel.add(level4Button);
        buttonsPanel.add(Box.createVerticalStrut(20));
        buttonsPanel.add(level5Button);
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
                g2d.setFont(new Font("Arial", Font.BOLD, 18));
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
        
        button.setPreferredSize(new Dimension(350, 50));
        button.setMaximumSize(new Dimension(350, 50));
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
        loadBackgroundMusic();
        loadEatingSound();
        loadMultiplierSound();
    }

	private AudioInputStream loadAudioFromResourcesOrFile(String relativePath) throws Exception {
		// First, try to load from classpath resources (inside JAR)
		try {
			ClassLoader cl = getClass().getClassLoader();
			java.net.URL url = cl.getResource(relativePath);
			if (url != null) {
				return AudioSystem.getAudioInputStream(url);
			}
		} catch (Exception ignored) {
		}
		// Fallback to filesystem paths (dev/local runs)
		File file = new File(relativePath);
		if (!file.exists()) {
			file = new File("src/" + relativePath);
			if (!file.exists()) {
				file = new File("bin/" + relativePath);
			}
		}
		return AudioSystem.getAudioInputStream(file);
	}
    
	private void loadBackgroundMusic() {
        try {
			AudioInputStream audioIn = loadAudioFromResourcesOrFile("sounds/background.wav");
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioIn);
            backgroundMusic.setFramePosition(0); // start at beginning
        } catch (UnsupportedAudioFileException e) {
            System.out.println("Unsupported audio file: " + e.getMessage());
        } catch (LineUnavailableException e) {
            System.out.println("Audio line unavailable: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("I/O error: " + e.getMessage());
        }
    }
    
	private void loadEatingSound() {
        try {
			AudioInputStream audioIn = loadAudioFromResourcesOrFile("sounds/eat.wav");
			eatingSound = AudioSystem.getClip();
			eatingSound.open(audioIn);
			System.out.println("Eating sound loaded");
        } catch (Exception e) {
            System.out.println("Failed to load eating sound: " + e.getMessage());
        }
    }
    
	private void loadMultiplierSound() {
        try {
			AudioInputStream audioIn = loadAudioFromResourcesOrFile("sounds/multiplier.wav");
			multiplierSound = AudioSystem.getClip();
			multiplierSound.open(audioIn);
			System.out.println("Multiplier sound loaded");
        } catch (Exception e) {
            System.out.println("Failed to load multiplier sound: " + e.getMessage());
        }
    }
    
    private void playEatingSound() {
        if (eatingSound != null) {
            eatingSound.setFramePosition(0); // rewind
            eatingSound.start();
        }
    }
    
    private void playMultiplierSound() {
        if (multiplierSound != null) {
            multiplierSound.setFramePosition(0);
            multiplierSound.start();
        }
    }
    
    private void startBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.setFramePosition(0); // rewind
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundMusic.start();
            System.out.println("Background music started");
        } else {
            System.out.println("Background music Clip is null");
        }
    }
    
    private void stopBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
        }
    }
    
    private void showMainMenu() {
        inMenu = true;
        running = false;
        if (timer != null) {
            timer.stop();
        }
        if (appleTimer != null) {
            appleTimer.stop();
        }
        if (aiMoveTimer != null) {
            aiMoveTimer.stop();
        }
        
        // Stop background music
        stopBackgroundMusic();
        
        // Switch to menu panel
        getContentPane().remove(gamePanel);
        getContentPane().add(menuPanel, BorderLayout.CENTER);
        menuPanel.revalidate();
        menuPanel.repaint();
        
		// Update HUD to show high score prominently in menu
		levelLabel.setText("");
		playerScoreLabel.setText("");
		aiScoreLabel.setText("");
		highScoreLabel.setText("High: " + highScore);
		multiplierLabel.setText("");
    }
    
    private void startLevel(int level) {
        currentLevel = level;
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
		aiScore = 0;
        scoreMultiplierActive = false;
        if (multiplierTimer != null) {
            multiplierTimer.stop();
        }
        
        // Set level-specific properties
        setLevelProperties();
        
        gameStartTime = System.currentTimeMillis();
        updateScoreLabel();
        
        spawnFood();
        running = true;
        
        // Start background music
        startBackgroundMusic();
        
        // Start game timer with level-specific speed
        if (timer != null) {
            timer.stop();
        }
        
        int gameDelay = getGameDelay();
        timer = new Timer(gameDelay, e -> gameLoop());
        timer.start();
        
        // Start apple timer for level 3+
        if (currentLevel >= 3) {
            startAppleTimer();
        }
        
        // Start bomb timer for level 4 and 5
        if (currentLevel == 4 || currentLevel == 5) {
            startBombTimer();
        }
        
        // Start AI snake for level 5
        if (currentLevel == 5) {
            startAISnake();
        }
    }
    
    private void setLevelProperties() {
        switch (currentLevel) {
            case 1:
                levelTimeLimit = 180000; // 3 minutes
                break;
            case 2:
                levelTimeLimit = 0; // No time limit
                break;
            case 3:
                levelTimeLimit = 0; // No time limit
                break;
            case 4:
                levelTimeLimit = 0; // No time limit
                break;
            case 5:
                levelTimeLimit = 0; // No time limit
                break;
        }
    }
    
    private int getGameDelay() {
        switch (currentLevel) {
            case 1:
                return 100; // Normal speed
            case 2:
                return 80; // Slightly faster
            case 3:
                return 70; // Faster
            case 4:
                return 60; // Fast + bomb challenge
            case 5:
                return 70; // Same pace as level 3 + AI + bomb challenge
            default:
                return 100;
        }
    }
    
    private void startAppleTimer() {
        if (appleTimer != null) {
            appleTimer.stop();
        }
        appleTimer = new Timer(4000, e -> {
            appleVisible = false;
            // For level 3+, spawn new apple elsewhere after 1 second
            if (currentLevel >= 3) {
                Timer spawnTimer = new Timer(1000, evt -> {
                    spawnFood();
                    appleVisible = true;
                });
                spawnTimer.setRepeats(false);
                spawnTimer.start();
            }
        });
        appleTimer.setRepeats(false);
        appleTimer.start();
        appleVisible = true;
    }
    
    private void startBombTimer() {
        if (bombTimer != null) {
            bombTimer.stop();
        }
        bombTimer = new Timer(4000, e -> {
            bombVisible = false;
            // Spawn new bomb elsewhere after 1 second
            Timer spawnTimer = new Timer(1000, evt -> {
                spawnBomb();
                bombVisible = true;
            });
            spawnTimer.setRepeats(false);
            spawnTimer.start();
        });
        bombTimer.setRepeats(false);
        bombTimer.start();
        spawnBomb();
        bombVisible = true;
    }
    
    private void spawnBomb() {
        int x, y;
        do {
            x = random.nextInt(WINDOW_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            y = random.nextInt(WINDOW_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
        } while (snake.contains(new Point(x, y)) && (food == null || !new Point(x, y).equals(food)));
        
        bomb = new Point(x, y);
    }
    
    private void startAISnake() {
        // Initialize AI snake
        aiSnake = new ArrayList<>();
        aiSnake.add(new Point(WINDOW_WIDTH - UNIT_SIZE * 4, WINDOW_HEIGHT - UNIT_SIZE * 4));
        aiSnake.add(new Point(WINDOW_WIDTH - UNIT_SIZE * 3, WINDOW_HEIGHT - UNIT_SIZE * 4));
        aiSnake.add(new Point(WINDOW_WIDTH - UNIT_SIZE * 2, WINDOW_HEIGHT - UNIT_SIZE * 4));
        aiSnake.add(new Point(WINDOW_WIDTH - UNIT_SIZE, WINDOW_HEIGHT - UNIT_SIZE * 4));
        
        aiDirection = 'L';
        aiSnakeActive = true;
        
        // Start AI movement timer
        if (aiMoveTimer != null) {
            aiMoveTimer.stop();
        }
        aiMoveTimer = new Timer(150, e -> moveAISnake());
        aiMoveTimer.start();
    }
    
    private void moveAISnake() {
        if (!aiSnakeActive || aiSnake == null || aiSnake.isEmpty()) return;
        
        Point aiHead = aiSnake.get(0);
        Point newAiHead = new Point(aiHead);
        
        // Simple AI: try to move towards food, avoid walls and obstacles
        Point target = food != null ? food : new Point(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2);
        
        // Calculate direction to target
        int dx = target.x - aiHead.x;
        int dy = target.y - aiHead.y;
        
        // Try to move in the direction of the target, but avoid walls
        char newDirection = aiDirection;
        
        if (Math.abs(dx) > Math.abs(dy)) {
            // Move horizontally
            if (dx > 0 && aiDirection != 'L') {
                newDirection = 'R';
            } else if (dx < 0 && aiDirection != 'R') {
                newDirection = 'L';
            }
        } else {
            // Move vertically
            if (dy > 0 && aiDirection != 'U') {
                newDirection = 'D';
            } else if (dy < 0 && aiDirection != 'D') {
                newDirection = 'U';
            }
        }
        
        // Check if the new direction is safe
        Point testHead = new Point(aiHead);
        switch (newDirection) {
            case 'U':
                testHead.y -= UNIT_SIZE;
                break;
            case 'D':
                testHead.y += UNIT_SIZE;
                break;
            case 'L':
                testHead.x -= UNIT_SIZE;
                break;
            case 'R':
                testHead.x += UNIT_SIZE;
                break;
        }
        
        // Wrap around walls
        if (testHead.x < 0) testHead.x = WINDOW_WIDTH - UNIT_SIZE;
        if (testHead.x >= WINDOW_WIDTH) testHead.x = 0;
        if (testHead.y < 0) testHead.y = WINDOW_HEIGHT - UNIT_SIZE;
        if (testHead.y >= WINDOW_HEIGHT) testHead.y = 0;
        
        // Check if the new position is safe (not colliding with player snake or itself)
        if (!snake.contains(testHead) && !aiSnake.contains(testHead)) {
            aiDirection = newDirection;
        } else {
            // Try alternative directions
            char[] alternatives = {'U', 'D', 'L', 'R'};
            for (char alt : alternatives) {
                if (alt != aiDirection && alt != getOppositeDirection(aiDirection)) {
                    Point altHead = new Point(aiHead);
                    switch (alt) {
                        case 'U': altHead.y -= UNIT_SIZE; break;
                        case 'D': altHead.y += UNIT_SIZE; break;
                        case 'L': altHead.x -= UNIT_SIZE; break;
                        case 'R': altHead.x += UNIT_SIZE; break;
                    }
                    
                    // Wrap around walls
                    if (altHead.x < 0) altHead.x = WINDOW_WIDTH - UNIT_SIZE;
                    if (altHead.x >= WINDOW_WIDTH) altHead.x = 0;
                    if (altHead.y < 0) altHead.y = WINDOW_HEIGHT - UNIT_SIZE;
                    if (altHead.y >= WINDOW_HEIGHT) altHead.y = 0;
                    
                    if (!snake.contains(altHead) && !aiSnake.contains(altHead)) {
                        aiDirection = alt;
                        break;
                    }
                }
            }
        }
        
        // Move AI snake
        Point finalHead = new Point(aiHead);
        switch (aiDirection) {
            case 'U':
                finalHead.y -= UNIT_SIZE;
                break;
            case 'D':
                finalHead.y += UNIT_SIZE;
                break;
            case 'L':
                finalHead.x -= UNIT_SIZE;
                break;
            case 'R':
                finalHead.x += UNIT_SIZE;
                break;
        }
        
        // Wrap around walls
        if (finalHead.x < 0) finalHead.x = WINDOW_WIDTH - UNIT_SIZE;
        if (finalHead.x >= WINDOW_WIDTH) finalHead.x = 0;
        if (finalHead.y < 0) finalHead.y = WINDOW_HEIGHT - UNIT_SIZE;
        if (finalHead.y >= WINDOW_HEIGHT) finalHead.y = 0;
        
		aiSnake.add(0, finalHead);

		// AI self-collision check (after moving)
		for (int i = 1; i < aiSnake.size(); i++) {
			if (finalHead.equals(aiSnake.get(i))) {
				killAISnake(30); // Small reward for AI self-destruct
				return;
			}
		}
        
		// Check if AI snake ate food
		if (finalHead.equals(food) && appleVisible) {
			// AI snake grows and scores
			aiScore += 10;
			spawnFood();
			updateScoreLabel();
		} else {
            // Remove tail
            aiSnake.remove(aiSnake.size() - 1);
        }
    }

	private void killAISnake(int rewardPoints) {
		aiSnakeActive = false;
		if (aiMoveTimer != null) {
			aiMoveTimer.stop();
		}
		score += Math.max(0, rewardPoints);
		updateScoreLabel();
	}

	private void resetHighScoreOnStartup() {
		try {
			highScore = 0;
			saveHighScore();
		} catch (Exception ignored) {
			// If saving fails, we still keep in-memory highScore at 0 for this run
		}
	}
    
    private char getOppositeDirection(char dir) {
        switch (dir) {
            case 'U': return 'D';
            case 'D': return 'U';
            case 'L': return 'R';
            case 'R': return 'L';
            default: return dir;
        }
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
        
        // For level 3, start apple timer
        if (currentLevel == 3) {
            startAppleTimer();
        }
    }
    
	private void gameLoop() {
        if (!running) return;
        
        // Check level-specific win/lose conditions
        if (checkLevelConditions()) {
            return;
        }
        
        move();
        checkCollision();
        checkFood();
        checkBombCollision();
        checkAIBombCollision();
        checkAICollision();
        
		// Update HUD each frame for smooth, separate UI (timer, scores, multiplier)
		updateScoreLabel();
        
        gamePanel.repaint();
    }
    
    private boolean checkLevelConditions() {
        // Level 1: Check time limit and score
        if (currentLevel == 1) {
            long elapsedTime = System.currentTimeMillis() - gameStartTime;
            if (elapsedTime >= levelTimeLimit) {
                if (score >= 300) {
                    levelComplete();
                } else {
                    levelFailed();
                }
                return true;
            }
        }
        
        // Level 2, 3, 4 & 5: Check if score reaches 300
        if (currentLevel == 2 || currentLevel == 3 || currentLevel == 4 || currentLevel == 5) {
            if (score >= 300) {
                levelComplete();
                return true;
            }
        }
        
        return false;
    }
    
    private void levelComplete() {
        running = false;
        timer.stop();
        if (appleTimer != null) {
            appleTimer.stop();
        }
        if (bombTimer != null) {
            bombTimer.stop();
        }
        if (aiMoveTimer != null) {
            aiMoveTimer.stop();
        }
        
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
            "Level " + currentLevel + " Complete!\nYour score: " + score + "\nHigh Score: " + highScore + "\nWould you like to play again?",
            "Level Complete!",
            JOptionPane.YES_NO_OPTION
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            startNewGame();
        } else {
            showMainMenu();
        }
    }
    
    private void levelFailed() {
        running = false;
        timer.stop();
        if (appleTimer != null) {
            appleTimer.stop();
        }
        if (bombTimer != null) {
            bombTimer.stop();
        }
        if (aiMoveTimer != null) {
            aiMoveTimer.stop();
        }
        
        // Stop multiplier timer
        if (multiplierTimer != null) {
            multiplierTimer.stop();
        }
        
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Level " + currentLevel + " Failed!\nTime's up! Your score: " + score + "\nYou need 300 points to win!\nWould you like to try again?",
            "Level Failed!",
            JOptionPane.YES_NO_OPTION
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            startNewGame();
        } else {
            showMainMenu();
        }
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
        if (snake.get(0).equals(food) && appleVisible) {
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
            
            updateScoreLabel();
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
		// Level and time (separate)
		String levelText = "Level " + currentLevel;
		if (currentLevel == 1) {
			long elapsedTime = System.currentTimeMillis() - gameStartTime;
			long remainingTime = Math.max(0, levelTimeLimit - elapsedTime);
			long minutes = remainingTime / 60000;
			long seconds = (remainingTime % 60000) / 1000;
			levelText += "  •  Time " + String.format("%02d:%02d", minutes, seconds);
		} else if (currentLevel == 2) {
			levelText += "  •  Grass";
		} else if (currentLevel == 3) {
			levelText += "  •  Ocean";
		} else if (currentLevel == 4) {
			levelText += "  •  Forest";
		} else if (currentLevel == 5) {
			levelText += "  •  Space";
		}
		levelLabel.setText(levelText);

		// Player score (separate)
		playerScoreLabel.setText("You: " + score);

		// AI score (separate, only visible in level 5)
		if (currentLevel == 5) {
			aiScoreLabel.setText("AI: " + aiScore);
			aiScoreLabel.setVisible(true);
		} else {
			aiScoreLabel.setText("");
			aiScoreLabel.setVisible(false);
		}

		// High score (separate)
		highScoreLabel.setText("High: " + highScore);

		// Multiplier status (separate)
		if (scoreMultiplierActive) {
			long remainingSecs = Math.max(0, 10 - ((System.currentTimeMillis() - multiplierStartTime) / 1000));
			multiplierLabel.setText("2x: " + remainingSecs + "s");
			multiplierLabel.setVisible(true);
		} else {
			multiplierLabel.setText("");
			multiplierLabel.setVisible(false);
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
    
    private void checkBombCollision() {
        if ((currentLevel == 4 || currentLevel == 5) && bomb != null && bombVisible) {
            Point head = snake.get(0);
            if (head.equals(bomb)) {
                gameOver();
            }
        }
    }
    
    private void checkAIBombCollision() {
        if ((currentLevel == 4 || currentLevel == 5) && bomb != null && bombVisible && aiSnakeActive && aiSnake != null) {
            Point aiHead = aiSnake.get(0);
            if (aiHead.equals(bomb)) {
                // AI snake dies from bomb
                aiSnakeActive = false;
                if (aiMoveTimer != null) {
                    aiMoveTimer.stop();
                }
                score += 30; // Bonus for AI dying from bomb
                updateScoreLabel();
            }
        }
    }
    
	private void checkAICollision() {
		if (currentLevel == 5 && aiSnakeActive && aiSnake != null) {
			Point playerHead = snake.get(0);
			Point aiHead = aiSnake.get(0);

			// Head-on collision: prioritize resolving as AI elimination for fairness
			if (playerHead.equals(aiHead)) {
				killAISnake(50);
				return;
			}

			// Player head hits AI body -> player dies
			for (int i = 0; i < aiSnake.size(); i++) {
				Point aiSegment = aiSnake.get(i);
				if (playerHead.equals(aiSegment)) {
					gameOver();
					return;
				}
			}

			// AI head hits any player segment -> AI dies
			for (Point playerSegment : snake) {
				if (aiHead.equals(playerSegment)) {
					killAISnake(50);
					return;
				}
			}
		}
	}
    
    private void gameOver() {
        running = false;
        timer.stop();
        if (appleTimer != null) {
            appleTimer.stop();
        }
        if (bombTimer != null) {
            bombTimer.stop();
        }
        if (aiMoveTimer != null) {
            aiMoveTimer.stop();
        }
        
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
    
    private void startNewGame() {
        startLevel(currentLevel);
    }
    
    private class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
			
			// Global high-quality rendering hints for smoother visuals
			Graphics2D base2d = (Graphics2D) g;
			base2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			base2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			base2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			base2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			base2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			base2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			
            if (running) {
                // Draw level-specific background theme
                drawBackgroundTheme(g);
				// Add a soft vignette overlay for a more sophisticated look
				drawVignetteOverlay((Graphics2D) g);
                
                // Ensure food exists; spawn if missing
                if (food == null) {
                    spawnFood();
                }
                
                // Draw apple food only if visible (for level 3+)
                if (appleVisible) {
                    drawApple(g, food);
                }
                
                // Draw bomb for level 4 and 5
                if ((currentLevel == 4 || currentLevel == 5) && bomb != null && bombVisible) {
                    drawBomb(g, bomb);
                }
                
                // Draw AI snake for level 5
                if (currentLevel == 5 && aiSnakeActive && aiSnake != null) {
                    drawAISnake(g);
                }
                
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
                
				// In-game on-canvas text minimized; HUD handles info separately
				// drawLevelUI(g);
            }
        }
        
		private void drawLevelUI(Graphics g) {
			// Intentionally minimal; HUD displays information separately for a smoother UI
		}
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
        }
        
        private void drawBackgroundTheme(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            switch (currentLevel) {
                case 1:
                    // Desert theme - sandy background with cactus
                    drawDesertTheme(g2d);
                    break;
                case 2:
                    // Grass theme - green background with grass patterns
                    drawGrassTheme(g2d);
                    break;
                case 3:
                    // Ocean theme - blue background with wave patterns
                    drawOceanTheme(g2d);
                    break;
                case 4:
                    // Forest theme - dark green background with tree patterns
                    drawForestTheme(g2d);
                    break;
                case 5:
                    // Space theme - dark background with stars and planets
                    drawSpaceTheme(g2d);
                    break;
                default:
                    // Default black background
                    g2d.setColor(Color.BLACK);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    break;
            }
            
            g2d.dispose();
        }
		
		private void drawVignetteOverlay(Graphics2D g2d) {
			int w = getWidth();
			int h = getHeight();
			int radius = Math.max(w, h);
			float[] dist = {0.6f, 1.0f};
			Color[] colors = {new Color(0, 0, 0, 0), new Color(0, 0, 0, 120)};
			RadialGradientPaint paint = new RadialGradientPaint(new Point(w / 2, h / 2), radius, dist, colors);
			Paint old = g2d.getPaint();
			g2d.setPaint(paint);
			g2d.fillRect(0, 0, w, h);
			g2d.setPaint(old);
		}
        
        private void drawDesertTheme(Graphics2D g2d) {
            // Sandy background gradient
            GradientPaint sandGradient = new GradientPaint(
                0, 0, new Color(238, 203, 173), // Light sand
                0, getHeight(), new Color(194, 178, 128) // Darker sand
            );
            g2d.setPaint(sandGradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Draw some cactus patterns
            g2d.setColor(new Color(34, 139, 34, 100)); // Semi-transparent green
            for (int i = 0; i < 8; i++) {
                int x = (i * 75) % getWidth();
                int y = (i * 60 + 30) % getHeight();
                drawCactus(g2d, x, y);
            }
        }
        
        private void drawGrassTheme(Graphics2D g2d) {
            // Grass background gradient
            GradientPaint grassGradient = new GradientPaint(
                0, 0, new Color(34, 139, 34), // Forest green
                0, getHeight(), new Color(0, 100, 0) // Dark green
            );
            g2d.setPaint(grassGradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Draw grass blade patterns
            g2d.setColor(new Color(50, 205, 50, 120)); // Semi-transparent lime green
            for (int i = 0; i < 12; i++) {
                int x = (i * 50) % getWidth();
                int y = (i * 40 + 20) % getHeight();
                drawGrassBlades(g2d, x, y);
            }
        }
        
        private void drawOceanTheme(Graphics2D g2d) {
            // Ocean background gradient
            GradientPaint oceanGradient = new GradientPaint(
                0, 0, new Color(135, 206, 235), // Sky blue
                0, getHeight(), new Color(0, 105, 148) // Deep blue
            );
            g2d.setPaint(oceanGradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Draw wave patterns
            g2d.setColor(new Color(255, 255, 255, 80)); // Semi-transparent white
            for (int i = 0; i < 10; i++) {
                int x = (i * 60) % getWidth();
                int y = (i * 50 + 25) % getHeight();
                drawWave(g2d, x, y);
            }
        }
        
        private void drawForestTheme(Graphics2D g2d) {
            // Forest background gradient
            GradientPaint forestGradient = new GradientPaint(
                0, 0, new Color(0, 100, 0), // Dark green
                0, getHeight(), new Color(25, 25, 112) // Dark blue
            );
            g2d.setPaint(forestGradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Draw tree patterns
            g2d.setColor(new Color(139, 69, 19, 100)); // Semi-transparent brown
            for (int i = 0; i < 6; i++) {
                int x = (i * 100) % getWidth();
                int y = (i * 80 + 40) % getHeight();
                drawTree(g2d, x, y);
            }
        }
        
        private void drawSpaceTheme(Graphics2D g2d) {
            // Space background gradient
            GradientPaint spaceGradient = new GradientPaint(
                0, 0, new Color(25, 25, 112), // Dark blue
                0, getHeight(), new Color(0, 0, 0) // Black
            );
            g2d.setPaint(spaceGradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Draw stars
            g2d.setColor(Color.WHITE);
            for (int i = 0; i < 100; i++) {
                int x = (i * 37) % getWidth();
                int y = (i * 73) % getHeight();
                g2d.fillOval(x, y, 2, 2);
            }
            
            // Draw some planets
            g2d.setColor(new Color(255, 165, 0, 150)); // Semi-transparent orange
            g2d.fillOval(50, 100, 40, 40);
            g2d.setColor(new Color(128, 128, 128, 150)); // Semi-transparent gray
            g2d.fillOval(500, 200, 30, 30);
        }
        
        private void drawCactus(Graphics2D g2d, int x, int y) {
            // Draw simple cactus shape
            g2d.setColor(new Color(34, 139, 34));
            g2d.fillRect(x, y, 8, 25);
            g2d.fillRect(x - 4, y + 8, 16, 8);
            g2d.fillRect(x - 2, y + 20, 12, 8);
        }
        
        private void drawGrassBlades(Graphics2D g2d, int x, int y) {
            // Draw simple grass blades
            g2d.setColor(new Color(50, 205, 50));
            for (int i = 0; i < 5; i++) {
                int bladeX = x + (i * 3);
                g2d.drawLine(bladeX, y + 15, bladeX - 2, y);
                g2d.drawLine(bladeX, y + 15, bladeX + 2, y);
            }
        }
        
        private void drawWave(Graphics2D g2d, int x, int y) {
            // Draw simple wave pattern
            g2d.setColor(new Color(255, 255, 255, 80));
            for (int i = 0; i < 3; i++) {
                int waveX = x + (i * 20);
                g2d.drawArc(waveX, y, 20, 10, 0, 180);
            }
        }
        
        private void drawTree(Graphics2D g2d, int x, int y) {
            // Draw simple tree
            g2d.setColor(new Color(139, 69, 19));
            g2d.fillRect(x, y + 20, 12, 30); // Trunk
            g2d.setColor(new Color(34, 139, 34));
            g2d.fillOval(x - 8, y, 28, 25); // Leaves
        }
        
        private void drawSnakeHead(Graphics g, Point head) {
            // Draw circular head with cool gradient effect and modern styling
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Create gradient for head
            GradientPaint headGradient = new GradientPaint(
                head.x, head.y, new Color(0, 255, 127), // Spring green
                head.x + UNIT_SIZE, head.y + UNIT_SIZE, new Color(0, 200, 100) // Darker green
            );
            g2d.setPaint(headGradient);
            g2d.fillOval(head.x, head.y, UNIT_SIZE, UNIT_SIZE);
            
            // Draw head border with glow effect
            g2d.setColor(new Color(0, 150, 75));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(head.x + 1, head.y + 1, UNIT_SIZE - 2, UNIT_SIZE - 2);
            
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
            g2d.setColor(Color.BLACK);
            g2d.fillOval(leftEyeX, leftEyeY, eyeSize, eyeSize);
            g2d.fillOval(rightEyeX, rightEyeY, eyeSize, eyeSize);
            
            g2d.dispose();
        }
        
        private void drawSnakeBody(Graphics g, Point body) {
            // Draw smaller circular body segments with overlap for intersection
            int overlap = UNIT_SIZE / 4; // Reduced overlap for smaller circles
            int circleSize = UNIT_SIZE - 4; // Make circles smaller
            
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Create gradient for body
            GradientPaint bodyGradient = new GradientPaint(
                body.x - overlap, body.y - overlap, new Color(30, 144, 255), // Dodger blue
                body.x + circleSize, body.y + circleSize, new Color(25, 25, 112) // Midnight blue
            );
            g2d.setPaint(bodyGradient);
            g2d.fillOval(body.x - overlap, body.y - overlap, circleSize + overlap, circleSize + overlap);
            
            // Calculate line dimensions first
            int lineHeight = Math.max(6, (circleSize + overlap) / 4);
            int lineX = body.x - overlap;
            int lineY = body.y - overlap + (circleSize + overlap) / 2 - lineHeight / 2;
            int lineWidth = circleSize + overlap;
            
            // Draw modern accent line through the center with gradient
            GradientPaint accentGradient = new GradientPaint(
                lineX, lineY, new Color(0, 255, 127), // Spring green
                lineX + lineWidth, lineY + lineHeight, new Color(0, 200, 100) // Darker green
            );
            g2d.setPaint(accentGradient);
            g2d.fillRect(lineX, lineY, lineWidth, lineHeight);
            
            // Draw modern border with glow effect
            g2d.setColor(new Color(25, 25, 112)); // Midnight blue
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(body.x - overlap, body.y - overlap, circleSize + overlap, circleSize + overlap);
            
            g2d.dispose();
        }
        
        private void drawSnakeTail(Graphics g, Point tail) {
            // Draw tail as a circle with modern theme like other body segments
            int overlap = UNIT_SIZE / 4;
            int circleSize = UNIT_SIZE - 4; // Same size as body circles
            
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Create gradient for tail
            GradientPaint tailGradient = new GradientPaint(
                tail.x - overlap, tail.y - overlap, new Color(30, 144, 255), // Dodger blue
                tail.x + circleSize, tail.y + circleSize, new Color(25, 25, 112) // Midnight blue
            );
            g2d.setPaint(tailGradient);
            g2d.fillOval(tail.x - overlap, tail.y - overlap, circleSize + overlap, circleSize + overlap);
            
            // Calculate line dimensions first
            int lineHeight = Math.max(6, (circleSize + overlap) / 4); // Same thickness as body
            int lineX = tail.x - overlap;
            int lineY = tail.y - overlap + (circleSize + overlap) / 2 - lineHeight / 2;
            int lineWidth = circleSize + overlap;
            
            // Draw modern accent line through the center with gradient
            GradientPaint accentGradient = new GradientPaint(
                lineX, lineY, new Color(0, 255, 127), // Spring green
                lineX + lineWidth, lineY + lineHeight, new Color(0, 200, 100) // Darker green
            );
            g2d.setPaint(accentGradient);
            g2d.fillRect(lineX, lineY, lineWidth, lineHeight);
            
            // Draw modern border with glow effect
            g2d.setColor(new Color(25, 25, 112)); // Midnight blue
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(tail.x - overlap, tail.y - overlap, circleSize + overlap, circleSize + overlap);
            
            g2d.dispose();
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
        
        private void drawBomb(Graphics g, Point bomb) {
            if (bomb == null) {
                return;
            }
            
            // Keep the bomb fully within its grid cell for visibility
            int bodyInset = Math.max(2, UNIT_SIZE / 10);
            int bodySize = UNIT_SIZE - bodyInset * 2;
            int bodyX = bomb.x + bodyInset;
            int bodyY = bomb.y + bodyInset;
            
            // Draw bomb body (black circle)
            g.setColor(Color.BLACK);
            g.fillOval(bodyX, bodyY, bodySize, bodySize);
            
            // Draw bomb border
            g.setColor(Color.DARK_GRAY);
            g.drawOval(bodyX, bodyY, bodySize, bodySize);
            
            // Draw fuse (red line)
            g.setColor(Color.RED);
            int fuseWidth = Math.max(2, UNIT_SIZE / 8);
            int fuseHeight = Math.max(8, UNIT_SIZE / 3);
            int fuseX = bodyX + bodySize / 2 - fuseWidth / 2;
            int fuseY = bodyY - fuseHeight;
            g.fillRect(fuseX, fuseY, fuseWidth, fuseHeight);
            
            // Draw fuse tip (orange)
            g.setColor(Color.ORANGE);
            int tipSize = Math.max(3, UNIT_SIZE / 6);
            int tipX = fuseX - tipSize / 2;
            int tipY = fuseY - tipSize / 2;
            g.fillOval(tipX, tipY, tipSize, tipSize);
            
            // Draw "BOOM!" text
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 10));
            g.drawString("BOOM!", bodyX + bodySize / 4, bodyY + bodySize / 2 + 4);
        }
        
        private void drawAISnake(Graphics g) {
            if (aiSnake == null || aiSnake.isEmpty()) return;
            
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            for (int i = 0; i < aiSnake.size(); i++) {
                Point segment = aiSnake.get(i);
                
                if (i == 0) {
                    // Draw AI snake head with cool gradient effect
                    GradientPaint headGradient = new GradientPaint(
                        segment.x, segment.y, new Color(255, 0, 0), // Bright red
                        segment.x + UNIT_SIZE, segment.y + UNIT_SIZE, new Color(139, 0, 0) // Dark red
                    );
                    g2d.setPaint(headGradient);
                    g2d.fillOval(segment.x, segment.y, UNIT_SIZE, UNIT_SIZE);
                    
                    // Draw head border with glow effect
                    g2d.setColor(new Color(100, 0, 0));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawOval(segment.x + 1, segment.y + 1, UNIT_SIZE - 2, UNIT_SIZE - 2);
                    
                    // Draw eyes with modern styling
                    g2d.setColor(Color.WHITE);
                    int eyeSize = UNIT_SIZE / 5;
                    int eyeOffset = UNIT_SIZE / 3;
                    g2d.fillOval(segment.x + eyeOffset, segment.y + eyeOffset, eyeSize, eyeSize);
                    g2d.fillOval(segment.x + UNIT_SIZE - eyeOffset - eyeSize, segment.y + eyeOffset, eyeSize, eyeSize);
                    
                    // Draw pupils with glow effect
                    g2d.setColor(Color.BLACK);
                    g2d.fillOval(segment.x + eyeOffset + 1, segment.y + eyeOffset + 1, eyeSize - 2, eyeSize - 2);
                    g2d.fillOval(segment.x + UNIT_SIZE - eyeOffset - eyeSize + 1, segment.y + eyeOffset + 1, eyeSize - 2, eyeSize - 2);
                } else {
                    // Draw AI snake body with gradient effect
                    GradientPaint bodyGradient = new GradientPaint(
                        segment.x, segment.y, new Color(139, 0, 0), // Dark red
                        segment.x + UNIT_SIZE, segment.y + UNIT_SIZE, new Color(100, 0, 0) // Darker red
                    );
                    g2d.setPaint(bodyGradient);
                    g2d.fillOval(segment.x, segment.y, UNIT_SIZE, UNIT_SIZE);
                    
                    // Draw body border with glow effect
                    g2d.setColor(new Color(80, 0, 0));
                    g2d.setStroke(new BasicStroke(1));
                    g2d.drawOval(segment.x + 1, segment.y + 1, UNIT_SIZE - 2, UNIT_SIZE - 2);
                }
            }
            
            g2d.dispose();
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
