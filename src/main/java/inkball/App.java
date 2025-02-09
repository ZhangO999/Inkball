package inkball;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import java.util.*;

/**
 * The main application class.
 * Responsible for managing the game state, drawing the UI, handling input events,
 * and orchestrating level loading and progression.
 */
public class App extends PApplet {

    public static final int CELLSIZE = 32; // Size of each tile
    public static final int TOPBAR = 64; // 64
    public static int WIDTH = 576; // Window width (576 = 18 * 32)
    public static int HEIGHT = 640; // Window height (576 + 64 for top bar)
    public static final int FPS = 30; // 30 default

    private int score = 0;
    private int remainingTime;
    private boolean isTimeUp = false;
    boolean isLevelFailed = false;
    private boolean isTimeDraining;
    private boolean isGameEnded = false;

    boolean victoryInProgress = false;
    private boolean isTimeDrained = false;
    private boolean isVictoryAnimationComplete = false;
    private int victoryframeCounter = 0;
    private int topLeftStep = 0; // Tracks movement of the first victory tile
    private int bottomRightStep = 0; // Tracks movement of the second victory tile

    private int currentLevel = 1;

    private double scoreIncreaseModifier;
    private double scoreDecreaseModifier;

    public String configPath;
    private JSONObject config;
    boolean isPaused = false;
    private int scoreAtLevelStart = 0;
    private PausedMessage pausedMessage;
    private EndMessage endMessage;
    private TimeUpMessage timeUpMessage;

    // Sprites
    private HashMap<String, PImage> sprites = new HashMap<>(); // Name : PImage
    public List<TimedTile> timedTiles;

    // Gameboard
    public char[][] board;

    public List<Hole> holes;
    public List<Ball> balls;
    
    public List<Spawner> spawners;

    // Spawn-related fields
    public List<String> ballSpawnQueue;
    public int SPAWNINTERVAL;
    public int SPAWNTIMER;

    // For ballSpawnQueue UI:
    private int offsetX = 0; // variable for animating the gradual shift in the ball Queue
    private boolean isShifting = false;
    boolean drawEventOngoing = false;
    float oldX, oldY;
    private List<Squiggle> squiggles;
    Squiggle currentSquiggle = null;
    private int drawLimitCounter = 0;
    int squigglesPerFrameCap = 5;

    /**
     * Default constructor that sets the configuration path.
     */
    public App() {
        this.configPath = "config.json";
    }

    /**
     * Sets up the size of the game window.
     */
    @Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    /**
     * Initializes the game state, loads sprites, and sets up the first level.
     */
    @Override
    public void setup() {
        frameRate(FPS);
        loadSprites(); // Load the sprites
        holes = new ArrayList<>();
        balls = new ArrayList<>();
        ballSpawnQueue = new ArrayList<>();
        spawners = new ArrayList<>();
        squiggles = new ArrayList<>();
        timedTiles = new ArrayList<>();

        isPaused = false;
        isTimeUp = false;
        isLevelFailed = false;
        isTimeDraining = false;
        isTimeDrained = false;
        isVictoryAnimationComplete = false;
        victoryInProgress = false;
        isGameEnded = false;
        squiggles.clear(); // Clear existing squiggles
        currentSquiggle = null; // Reset current squiggle
        drawEventOngoing = false; // Ensure no ongoing drawing events
        pausedMessage = new PausedMessage(WIDTH / 2 - 100, TOPBAR / 2 - 20, 200, 40);
        endMessage = new EndMessage(WIDTH / 2 - 100, TOPBAR / 2 - 20, 200, 40);
        timeUpMessage = new TimeUpMessage(WIDTH / 2 - 100, TOPBAR / 2 - 20, 200, 40);
        scoreAtLevelStart = score;
        offsetX = 0;
        isShifting = false;

        // Load the config and the level corresponding to currentLevel
        config = loadJSONObject(configPath); // parse level 1 data from config.
        loadConfigForLevel(currentLevel);
        String levelFile = getLevelFilePath(currentLevel);
        loadLevel(levelFile);
    }


    /**
     * Loads the sprites required for the game from the resources folder.
     */
    private void loadSprites() {
        // Load wall sprites using relative paths
        for (int i = 0; i <= 4; i++) {
            sprites.put("wall" + i, loadImage("src/main/resources/inkball/wall" + i + ".png"));
        }

        // Load spawner sprite
        sprites.put("entrypoint", loadImage("src/main/resources/inkball/entrypoint.png"));

        // Load ball and hole sprites
        for (int i = 0; i <= 4; i++) {
            sprites.put("ball" + i, loadImage("src/main/resources/inkball/ball" + i + ".png"));
            sprites.put("hole" + i, loadImage("src/main/resources/inkball/hole" + i + ".png"));
        }

        // Load tile sprite
        sprites.put("tile", loadImage("src/main/resources/inkball/tile.png"));
    }

    /**
     * Loads the configuration data for the specified level. Then 
     * loads the ball colors from the configuration and adds them to the spawn queue.
     *
     * @param level level the level for which the configuration will be loaded
     */
    public void loadConfigForLevel(int level) {
        JSONArray levels = config.getJSONArray("levels");

        if (level <= levels.size()) {
            JSONObject levelConfig = levels.getJSONObject(level - 1); // Level config

            // Extract ball colors for the current level
            JSONArray ballColors = levelConfig.getJSONArray("balls");
            for (int i = 0; i < ballColors.size(); i++) {
                ballSpawnQueue.add(ballColors.getString(i));
            }

            remainingTime = levelConfig.getInt("time") * FPS;
            SPAWNINTERVAL = levelConfig.getInt("spawn_interval");
            SPAWNTIMER = SPAWNINTERVAL * FPS;
            scoreIncreaseModifier = levelConfig.getDouble("score_increase_from_hole_capture_modifier");
            scoreDecreaseModifier = levelConfig.getDouble("score_decrease_from_wrong_hole_modifier");

            System.out.println("Level " + level + " loaded with " + remainingTime / FPS + " seconds.");
        } else {
            System.out.println("No more levels available.");
        }
    }

    /**
     * Retrieves the file path for the given level.
     *
     * @param level the level to retrieve the path for
     * @return the path of the level file
     */
    String getLevelFilePath(int level) {
        JSONArray levels = config.getJSONArray("levels");

        if (level <= levels.size()) {
            return levels.getJSONObject(level - 1).getString("layout");
        } else {
            System.out.println("No more levels to load.");
            return null; // Handle end of game
        }
    }

    /** 
     * Loads the specified level file and initializes the board.
     *
     * @param levelFilePath the path to the level file
     */
    void loadLevel(String levelFilePath) {
        // Load the level file and initialize the board
        if (levelFilePath == null) {
            return;
        }

        String[] lines = loadStrings(levelFilePath); // loadStrings (PApplet method) returns String[] of all the lines
                                                     // in level1.txt
        board = new char[18][18];

        for (int row = 0; row < lines.length; row++) {
            for (int col = 0; col < lines[row].length(); col++) {
                char tileChar = lines[row].charAt(col);
                board[row][col] = tileChar;

                if (tileChar == 'T') {
                    PImage sprite = getSprite("wall0"); // Use grey wall sprite for all timed tiles
                    timedTiles.add(new TimedTile(col, row, sprite)); // Add to timed tiles list
                }

                else if (tileChar == 'H') {
                    char holeColor = lines[row].charAt(col + 1); // e.g., H1 means hole1
                    PImage holeSprite = getSprite("hole" + holeColor);
                    // Create a new Hole object and add it to the list
                    holes.add(new Hole(col, row, Character.getNumericValue(holeColor), holeSprite));
                    col++; // Skip the color number character
                } else if (tileChar == 'B') {
                    char ballColor = lines[row].charAt(col + 1); // e.g., B2 means ball2
                    int colorIndex = Character.getNumericValue(ballColor);
                    PImage ballSprite = getSprite("ball" + ballColor);

                    // Place the ball directly at the given coordinates
                    float ballX = col * CELLSIZE + CELLSIZE / 2;
                    float ballY = row * CELLSIZE + CELLSIZE / 2 + TOPBAR;

                    // Generate random initial velocities (-2 or 2)
                    float randomDx = Math.random() < 0.5 ? -2 : 2;
                    float randomDy = Math.random() < 0.5 ? -2 : 2;
                    balls.add(new Ball(ballX, ballY, randomDx, randomDy, ballSprite, colorIndex, this));
                    col++; // Skip the color number character
                } else if (tileChar == 'S') {
                    // Create a new Spawner object and add it to its corresponding object list:
                    Spawner spawner = new Spawner(this, col, row, getSprite("entrypoint"));
                    spawners.add(spawner);
                }
            }
        }
    }

    // simple getter off our HashMap
    public PImage getSprite(String string) {
        return sprites.get(string);
    }

    public char[][] getBoard() {
        return board;
    }

    public int getScore() {
        return score;
    }

    public boolean getVictoryInProgress() {
        return victoryInProgress;
    }

    public boolean isVictoryAnimationComplete() {
        return isVictoryAnimationComplete;
    }

    public boolean isGameEnded() {
        return isGameEnded;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isTimeDrained() {
        return isTimeDrained;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public Squiggle getCurrentSqiggle() {
        return currentSquiggle;
    }

    public List<Squiggle> getSquiggles() {
        return squiggles;
    }

    public boolean isLevelComplete() {
        return ballSpawnQueue.isEmpty() && balls.isEmpty();
    }

    public int getScoreIncrease(int colorIndex) {
        String colorName = getColorName(colorIndex);
        int baseScoreChange = config.getJSONObject("score_increase_from_hole_capture").getInt(colorName);
        return (int) (baseScoreChange * scoreIncreaseModifier);
    }

    public int getScoreDecrease(int colorIndex) {
        String colorName = getColorName(colorIndex);
        int baseScoreChange = config.getJSONObject("score_decrease_from_wrong_hole").getInt(colorName);
        return (int) (baseScoreChange * scoreDecreaseModifier);
    }

    public JSONObject getConfig() {
        return config;
    }

    /**
     * Retrieves the color name corresponding to the given color index.
     *
     * @param colorIndex the index of the color
     * @return the name of the color, or null if the index is invalid
     */
    private String getColorName(int colorIndex) {
        switch (colorIndex) {
            case 0:
                return "grey";
            case 1:
                return "orange";
            case 2:
                return "blue";
            case 3:
                return "green";
            case 4:
                return "yellow";
            default:
                return null;
        }
    }

    /**
     * Adds a ball back to the spawn queue based on its color index.
     * If the queue was previously empty, the spawn timer is reset to avoid instant spawn.
     *
     * @param colorIndex the color index of the ball to requeue
     */
    public void requeueBall(int colorIndex) {
        String colorName = getColorName(colorIndex);
        boolean wasEmpty = ballSpawnQueue.isEmpty();
        ballSpawnQueue.add(colorName);
        if (wasEmpty) {
            // If the queue was empty, reset the spawn timer to avoid instant spawn
            SPAWNTIMER = SPAWNINTERVAL * FPS;
        }
    }

    /**
     * Increases the player's score by a given amount and prints the new score.
     *
     * @param amount the amount to increase the score by
     */
    public void addScore(int amount) {
        score += amount;
        System.out.println("Score increased by " + amount + ". Total score: " + score);
    }

    /**
     * Decreases the player's score by a given amount, ensuring it does not go negative.
     *
     * @param amount the amount to decrease the score by
     */
    public void subtractScore(int amount) {
        score -= amount;
        if (score < 0) {
            score = 0; // Ensure the score never goes negative
        }
        System.out.println("Score decreased by " + amount + ". Total score: " + score);
    }

    /**
     * Handles mouse press events. Initiates drawing or removes squiggles on right-click.
     *
     * @param e the mouse event
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if (isLevelFailed)
            return;
        if (mouseButton == RIGHT) {
            removeSquiggleAt(mouseX, mouseY);
            return; // Exit to prevent left-click logic from running
        }

        if (victoryInProgress)
            return;

        boolean clickedLeft = (mouseButton == LEFT);
        if (!clickedLeft)
            return; // Only respond to left-clicks
        drawEventOngoing = true; // initiate a draw event
        oldX = mouseX;
        oldY = mouseY;

        // Create a new squiggle and add the initial point where the mouse is pressed
        currentSquiggle = new Squiggle();
        currentSquiggle.addPoint(mouseX, mouseY);
        squiggles.add(currentSquiggle);
    }

    /**
     * Handles mouse dragging events to add points to the current squiggle.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        if (victoryInProgress || isLevelFailed)
            return;
        boolean clickedLeft = (mouseButton == LEFT);
        if (clickedLeft && drawEventOngoing && currentSquiggle != null) {
            if (drawLimitCounter < squigglesPerFrameCap) {
                // Only add a new point if the mouse has moved a significant distance
                if (PApplet.dist(oldX, oldY, mouseX, mouseY) > 5) { // Threshold to reduce points
                    currentSquiggle.addPoint(mouseX, mouseY);
                    oldX = mouseX;
                    oldY = mouseY;
                    drawLimitCounter++; // Increment counter for each new segment

                }
            }
        }
    }

    /**
     * Ends the current draw event when the mouse is released.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        drawEventOngoing = false;
        currentSquiggle = null; // Clear the current squiggle reference
    }

    /**
     * Handles key press events for restarting the game or toggling pause state.
     *
     * @param event the key event
     */
    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getKey() == 'r') {
            if (!victoryInProgress && currentLevel > config.getJSONArray("levels").size()) {
                // Game has ended, reset everything to start from level 1
                score = 0; // Reset the score
                currentLevel = 1; // Start from level 1
                victoryInProgress = false; // Ensure victory mode is off
                setup(); // Re-initialize the game
            } else {
                // Reset the current level if the game is still in progress
                score = scoreAtLevelStart; // Reset score to the level start score
                setup(); // Reload the current level
            }
        } else if (event.getKey() == ' ') {
            if (!isGameEnded || !isLevelFailed) {
                isPaused = !isPaused;
            } // Toggle pause state
            synchronized (this) {
                if (!isPaused) {
                    notify(); // Resume the drainTimeIntoScore thread
                }
            }
        }
    }

    /**
     * Removes a squiggle at the given coordinates if it exists.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    void removeSquiggleAt(float x, float y) {
        for (int i = squiggles.size() - 1; i >= 0; i--) {
            Squiggle squiggle = squiggles.get(i);
            if (squiggle.containsPoint(x, y)) {
                squiggles.remove(i);
                break; // Remove only the first squiggle found
            }
        }
    }

    /**
     * Updates the game timer, handling time-based level failure conditions.
     */
    void updateTimer() {
        System.out.println("updateTimer() running. Time: " + remainingTime);
        if (isPaused || isTimeDraining || isLevelFailed)
            return;
        if (remainingTime > 0) {
            remainingTime--;
        }

        if (remainingTime < FPS) {
            isTimeUp = true;
            isLevelFailed = true;
            // System.out.println("Time's up flag triggered.");
        }
    }

    /**
     * The main game loop that draws the board, updates entities, handles the UI, 
     * then initiates the victory sequence when the level is complete.
     */
    @Override
    public void draw() {
        background(200, 200, 200); // Set background color
        drawLimitCounter = 0;

        drawBoard(); // Draw the game board --> 1x1 tiles

        // Draw & update all spawners
        for (Spawner spawner : spawners) {
            spawner.draw(this, CELLSIZE, TOPBAR);
        }

        // Draw all holes
        for (Hole hole : holes) {
            hole.draw(this, CELLSIZE, TOPBAR);

            // Attract balls towards each hole
            for (int i = balls.size() - 1; i >= 0; i--) {
                Ball ball = balls.get(i);
                ball.attractToHole(hole); // Apply attraction logic
            }
        }
        // Draw and update timed tiles
        for (TimedTile tile : timedTiles) {
            if (!isPaused && !isLevelComplete() && !isLevelFailed) {
                tile.updateAlpha();
            } // Update alpha value}
            tile.draw(this, CELLSIZE, TOPBAR); // Draw the tile
        }

        drawTopBar();
        if (isPaused && !isLevelFailed && !isGameEnded) {
            pausedMessage.draw(this);
        } else {
            // Draw the top bar including the spawn queue and timer
            handleSpawning();
            if (!isLevelComplete()) {
                updateTimer();
            }
        }

        if (victoryInProgress && !isVictoryAnimationComplete) {
            // **Update and draw victory tiles**
            updateVictoryTiles();
            drawVictoryTiles();
        }

        if (!isLevelComplete()) {
            // Squiggle removal logic:
            if (!isLevelFailed) {
                for (int i = squiggles.size() - 1; i >= 0; i--) {
                    Squiggle squiggle = squiggles.get(i);
                    if (squiggle.isRemoved()) {
                        if (squiggle == currentSquiggle) {
                            currentSquiggle = null; // Clear the reference if the current line was removed
                        }
                        squiggles.remove(i);
                    } else {
                        squiggle.draw(this);
                    }
                    squiggle.resetCollisionFlag();
                }
            }

            for (Ball ball : balls) {
                if (!isPaused && !isLevelFailed) {
                    ball.tick();
                }

                for (int i = squiggles.size() - 1; i >= 0; i--) {
                    Squiggle squiggle = squiggles.get(i);
                    if (!squiggle.isRemoved() && squiggle.isCollidingWithBall(ball)) {
                        squiggle.handleCollision(ball);
                        squiggle.pendingRemoval(); // Mark the squiggle as removed after collision
                    }
                }
                ball.draw(this, CELLSIZE, TOPBAR);
                // System.out.println("Squiggle " + squiggles.size());
            }
        } else if (!isTimeUp) {
            isTimeUp = true; // Mark the level as finished
            startVictorySequence();
        }

        if (isLevelFailed) {
            timeUpMessage.draw(this);
        }

        if (isVictoryAnimationComplete) {
            checkVictoryAndLoadNextLevel(); // Load the next level
        }

        drawTopBar(); // Ensure the top bar is drawn

        if (isVictoryAnimationComplete && isTimeDrained) {
            if (currentLevel >= config.getJSONArray("levels").size()) {
                drawEndMessage();  
                isGameEnded = true;
            }
        }
    }

    /**
     * Starts the victory sequence when a level is completed.
     */
    void startVictorySequence() {
        if (victoryInProgress)
            return; // Prevent multiple triggers

        victoryInProgress = true;
        isTimeDraining = true;
        topLeftStep = 0;
        bottomRightStep = getPerimeterLength() / 2; // Opposite corner for the second tile
        scoreAtLevelStart = score; // Store current score before adding time bonus
        thread("drainTimeIntoScore"); // Start the time draining process
        // thread("runVictoryAnimation"); // Start the victory animation

    }

    /**
     * Gradually drains remaining time into the score during the victory sequence.
     */
    public void drainTimeIntoScore() {
        int frameCounter = 0;
        while (remainingTime > 0 && isTimeDraining) {
            synchronized (this) {
                if (isPaused) {
                    try {
                        wait(); // Pause the draining process
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                Thread.sleep(67); // 0.067 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            frameCounter++;

            if (frameCounter % 2 == 0) {
                remainingTime -= 30;
                score++;
            }

        }
        isTimeDraining = false;
        isTimeDrained = true;
        System.out.println("Time drained. Final Score: " + score);
        checkVictoryAndLoadNextLevel();

    }

    /**
     * Updates the positions of the victory tiles during the victory animation.
     */
    void updateVictoryTiles() {
        if (isPaused)
            return;
        victoryframeCounter++; // Track frames to control tile movement

        if (victoryframeCounter % 2 == 0) { // Move tiles every 2 frames (0.067 seconds)
            int perimeterLength = getPerimeterLength();

            // Increment the steps for both tiles
            topLeftStep = (topLeftStep + 1) % perimeterLength;
            bottomRightStep = (bottomRightStep + 1) % perimeterLength;

            // Check if both tiles have returned to their original positions
            if (isTimeDrained) {
                isVictoryAnimationComplete = true;
                victoryInProgress = false;
                System.out.println("Victory animation complete!");
            }
        }
    }

    /**
     * Helper method to extract the victory tile sprite tiles (yellow 'wall4.png') and 
     * draw two of them.
     */
    private void drawVictoryTiles() {
        PImage yellowTile = getSprite("wall4"); // Use yellow tile sprite

        // Get the positions for both tiles
        int[] topLeftPos = getTilePosition(topLeftStep); // Top-left tile
        int[] bottomRightPos = getTilePosition(bottomRightStep); // Bottom-right tile

        // Draw the top-left tile
        image(yellowTile,
                topLeftPos[0] * CELLSIZE,
                topLeftPos[1] * CELLSIZE + TOPBAR,
                CELLSIZE, CELLSIZE);

        // Draw the bottom-right tile
        image(yellowTile,
                bottomRightPos[0] * CELLSIZE,
                bottomRightPos[1] * CELLSIZE + TOPBAR,
                CELLSIZE, CELLSIZE);
    }

    private void drawEndMessage() {
        pushStyle();  // Save the current style settings
        fill(0);  // Black text
        textSize(24);
        textAlign(CENTER, CENTER);  // Center the text
        text("=== ENDED ===", WIDTH / 2, TOPBAR / 2);  // Draw in the middle of the top bar
        popStyle();  // Restore the previous style settings
    } 

    /**
     * Retrieves the total perimeter length of the game board.
     *
     * @return the perimeter length
     */
    private int getPerimeterLength() {
        int horizontalTiles = WIDTH / CELLSIZE;
        int verticalTiles = (HEIGHT - TOPBAR) / CELLSIZE;
        return 2 * (horizontalTiles + verticalTiles) - 4; // Exclude double-counted corners
    }

    /**
     * Calculates the tile position on the game board based on the step along the perimeter.
     *
     * @param step the step along the perimeter of the board
     * @return an array with the x and y coordinates of the tile
     */
    private int[] getTilePosition(int step) {
        int horizontalTiles = WIDTH / CELLSIZE;
        int verticalTiles = (HEIGHT - TOPBAR) / CELLSIZE;

        if (step < horizontalTiles) { // Top edge
            return new int[] { step, 0 };
        } else if (step < horizontalTiles + verticalTiles - 1) { // Right edge
            return new int[] { horizontalTiles - 1, step - horizontalTiles + 1 };
        } else if (step < 2 * horizontalTiles + verticalTiles - 2) { // Bottom edge
            return new int[] { 2 * horizontalTiles + verticalTiles - 3 - step, verticalTiles - 1 };
        } else { // Left edge
            return new int[] { 0, 2 * (horizontalTiles + verticalTiles) - 4 - step };
        }
    }

    /**
     * Checks if the current level has been completed and, if so, loads the next level.
     * This method is marked as synchronized to prevent multiple threads from
     * accessing it simultaneously, ensuring that the level loading process is
     * not interrupted or triggered multiple times.
     */
    private synchronized void checkVictoryAndLoadNextLevel() {
        if (isTimeDrained && isVictoryAnimationComplete) {
            loadNextLevel();
        }
    }

    /**
     * Loads the next level. If all levels are completed, the game ends.
     */
    void loadNextLevel() {
        currentLevel++;
        // System.out.println("Levels size is " + config.getJSONArray("levels").size() +
        // " and current level is " + currentLevel);
        if (currentLevel <= config.getJSONArray("levels").size()) {
            System.out.println("Level loaded: " + currentLevel);
            setup(); // Reset and load the next level
        } else {
            println("Congratulations! All levels completed.");
            victoryInProgress = false; // Stop interactions with the game
            isTimeDraining = false; // Stop any ongoing processes
        }
    }

    /**
     * Draws the top bar displaying the score and remaining time.
     */
    private void drawTopBar() {
        pushStyle(); // save current stroke and stroke weight settings
        // Add black background for ball queue and timer
        fill(0);
        drawBallQueue();
        // Display the score and time:
        fill(0); // black text
        textSize(20);
        textAlign(RIGHT, TOP); // Align to the top-right corner
        // Calculate X position for the top-right corner
        int scoreX = WIDTH - 10; // 10px padding from the right edge
        int scoreY = 10; // 10px from the top
        // Draw score directly above the timer
        text("Score: " + score, scoreX, scoreY); // Display score
        text("Time: " + (remainingTime / FPS) + "s", scoreX, scoreY + 25); // Display timer below the score
    }

    /**
     * Draws the queue of balls waiting to spawn, animating their movement, making them 
     * slide down gradually after the spawn timer hits zero. 
     */
    private void drawBallQueue() {
        int blackRectEnd = 154;
        int blackRectY = 15;
        int blackRectLeft = 15;
        int blackRectHeight = TOPBAR - 30;
        rect(blackRectLeft, blackRectY, blackRectEnd, blackRectHeight);
        clip(blackRectLeft, blackRectY, blackRectEnd, blackRectHeight);

        // Display up to 5 balls from the spawn queue
        int xPos = 20; // Start position for ball display
        int ballSize = 24; // 24 diameter = 12 radius
        int ballSpacing = 30;

        for (int i = 0; i < ballSpawnQueue.size(); i++) {
            String ballColor = ballSpawnQueue.get(i);
            int colorIndex = spawners.get(0).getColorIndex(ballColor);
            PImage ballSprite = getSprite("ball" + colorIndex);

            // If it is the leftmost ball and sliding, make it transparent
            if (i == 0 && isShifting) {
                tint(255, 255, 255, 0); // Full transparency for the leftmost ball
            } else {
                noTint(); // Reset to full opacity for other balls
            }

            // During sliding, gradually shift the balls left
            if (isShifting) {
                image(ballSprite, xPos - (int) offsetX, 20, ballSize, ballSize);
            } else {
                image(ballSprite, xPos, 20, ballSize, ballSize);
            }

            noTint();
            // image(ballSprite, xPos - (int) offsetX, 20, ballSize, ballSize);
            xPos += ballSpacing; // Spacing between each ball
        }

        // Update offsetX to animate the gradual leftward movement, only if shifting
        if (!isPaused && !isLevelFailed && isShifting) {
            offsetX += 1; // Move by 1 pixel per frame

            // If offsetX reaches the full ball spacing, stop shifting
            if (offsetX >= ballSpacing) {
                isShifting = false; // Stop shifting until the next spawn
                offsetX = 0; // Reset for the next shift

                ballSpawnQueue.remove(0);
            }
        }
        popStyle(); // restore previous stroke and stroke weight settings
        noClip();

        // Display the SPAWNTIMER as a countdown, stop showing after all balls are
        // spawned
        if (!ballSpawnQueue.isEmpty() && !isLevelFailed) {
            fill(0);
            textSize(20);
            text(String.format("%.1f", (float) SPAWNTIMER / FPS), blackRectEnd + 55, 22); // Timer next to ball queue
        }
    }

    /**
     * Draws the game board by iterating over the tiles and placing the appropriate sprites.
     */
    private void drawBoard() { // Loop over the board[][] array and assign sprites based on the character.
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                char tileChar = board[row][col];
                PImage sprite = null;
                final int currentRow = row; // Capture row into a final variable
                final int currentCol = col;

                // Skip timed tiles since they are drawn separately
                boolean isTimed = timedTiles.stream().anyMatch(t -> t.getX() == currentCol && t.getY() == currentRow);
                if (isTimed)
                    continue;

                switch (tileChar) {
                    case 'X': // Wall type 0
                        sprite = sprites.get("wall0");
                        break;
                    case '1': // Wall type 1
                        sprite = sprites.get("wall1");
                        break;
                    case '2': // Wall type 2
                        sprite = sprites.get("wall2");
                        break;
                    case '3': // Wall type 3
                        sprite = sprites.get("wall3");
                        break;
                    case '4': // Wall type 4
                        sprite = sprites.get("wall4");
                        break;
                    case 'S': // Spawner
                        sprite = sprites.get("entrypoint");
                        break;
                    case 'H': // Hole (Top-left part of a 2x2 hole)
                        sprite = sprites.get("hole0");
                        break;
                    case 'B': // Ball (Initially placed on board)
                        sprite = sprites.get("tile");
                        break;
                    case ' ':
                    default: // Empty space
                        sprite = sprites.get("tile");
                        break;
                }

                // Draw the sprite
                if (sprite != null) {
                    image(sprite, col * CELLSIZE, row * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
                }
            }
        }
    }

    /**
     * Handles ball spawning logic, including animation for the spawn queue.
     */
    void handleSpawning() {
        if (isPaused || isLevelFailed)
            return; // Skip spawning if the game is paused
        SPAWNTIMER--;
        if (SPAWNTIMER <= 0 && !ballSpawnQueue.isEmpty() && !isShifting) {
            // Reset offsetX to begin a new sliding animation
            offsetX = 0;
            isShifting = true;
            Random random = new Random();
            Spawner randomSpawner = spawners.get(random.nextInt(spawners.size()));
            String ballColor = ballSpawnQueue.get(0);

            // System.out.println(ballSpawnQueue);
            // Calculate the center of the spawner
            float centerX = randomSpawner.getX() * CELLSIZE + CELLSIZE / 2;
            float centerY = randomSpawner.getY() * CELLSIZE + CELLSIZE / 2 + TOPBAR;

            int colorIndex = randomSpawner.getColorIndex(ballColor);
            PImage ballSprite = getSprite("ball" + colorIndex);

            float randomDx = Math.random() < 0.5 ? -2 : 2;
            float randomDy = Math.random() < 0.5 ? -2 : 2;
            balls.add(new Ball(centerX, centerY, randomDx, randomDy, ballSprite, colorIndex, this));
            SPAWNTIMER = SPAWNINTERVAL * FPS;
        }
    }

    /**
     * The main entry point of the program that launches the Processing sketch.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        PApplet.main("inkball.App");
    }
}

