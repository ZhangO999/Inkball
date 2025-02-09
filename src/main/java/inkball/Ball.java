package inkball;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * Represents a ball, with attributes like position, velocity,
 * color, and behavior such as collision detection and attraction to holes.
 */
public class Ball implements GameObject {
    private static final int MIN_X = 0;
    private static final int MIN_Y = App.TOPBAR;
    private static final float MAX_SPEED = 5.0f;
    private static final float MIN_SPEED = 0.1f;
    private static float DEFAULT_RADIUS = 12;
    private static final float BALLSIZE_THRESH = 3; // Threshold below which ball counts as captured
    private static final float DIST_THRESH = 15; // Distance threshold for capturing the ball

    private static float ATTRACTION_RADIUS = 32;
    private static float ATTRACTION_FORCE = 0.005f; // 0.5% attraction force

    private float radius;
    private float x, y;
    private float dx, dy;
    private PImage ballSprite;
    private int colorIndex;
    private double EPSILON = 0.00001;
    private boolean isCaptured = false;
    private App app;

    /**
     * Constructs a Ball with the specified position, velocity, sprite, color, and game context.
     *
     * @param startX the initial x-coordinate of the ball
     * @param startY the initial y-coordinate of the ball
     * @param speedX the initial x-velocity of the ball
     * @param speedY the initial y-velocity of the ball
     * @param ballSprite the sprite image for the ball
     * @param colorIndex the color index of the ball
     * @param app the game context
     */
    public Ball(float startX, float startY, float speedX, float speedY, PImage ballSprite, int colorIndex, App app) {
        this.radius = DEFAULT_RADIUS;
        this.x = startX;
        this.y = startY;
        this.dx = speedX;
        this.dy = speedY;
        this.ballSprite = ballSprite;
        this.app = app;
        this.colorIndex = colorIndex; // Default to grey
    }

    /**
     * Handles collision with walls by adjusting the ball's velocity and color.
     *
     * @param velocity the current velocity (x or y) of the ball
     * @param pos the current position (x or y) of the ball
     * @param col the current column of the ball on the board
     * @param row the current row of the ball on the board
     * @param isHorizontal true if the collision check is horizontal, false if vertical
     * @return the updated velocity after the collision
     */
    private float handleCollision(float velocity, float pos, int col, int row, boolean isHorizontal) {
        // Determine the part of the ball boundary in the dir of movement.
        float frontEdge;
        if (velocity > 0) {
            frontEdge = pos + radius; // moving 'forward' (right or down)
        } else {
            frontEdge = pos - radius; // moving 'backward' (left or up)
        }

        // Identify the target tile based on the dir of movement
        int targetCol;
        int targetRow;
        if (isHorizontal) {
            targetCol = getColumn(frontEdge);
            targetRow = row;
        } else {
            targetCol = col;
            targetRow = getRow(frontEdge);
        }
        // Flip the velocity if there is a collision with a wall:
        if (isWall(targetCol, targetRow)) {
            // Change color based on wall color
            char tileChar = app.board[targetRow][targetCol];
            changeColor(tileChar);
            return -velocity;
        }
        return velocity;
    }

    /**
     * Attracts the ball towards a specified hole and handles capture logic if applicable.
     *
     * @param hole the hole to attract the ball towards
     */
    public void attractToHole(Hole hole) {
        float holeCenterX = hole.getCenterX(app);
        float holeCenterY = hole.getCenterY(app);

        // Calculate the vector from the ball to the hole's center
        float dx = holeCenterX - x;
        float dy = holeCenterY - y;

        // Calculate the distance between the ball and the hole's center
        float distance = PApplet.dist(x, y, holeCenterX, holeCenterY);

        // Apply attraction if the ball is within 32 pixels
        if (distance <= ATTRACTION_RADIUS) {
            // Calculate the attraction force (0.5% of the vector)
            float attractionX = ATTRACTION_FORCE * dx;
            float attractionY = ATTRACTION_FORCE * dy;

            // Add the attraction force to the ball's velocity
            this.dx += attractionX;
            this.dy += attractionY;

            // Shrink the ball proportionally to the distance
            float shrinkFactor = PApplet.map(distance, 0, App.CELLSIZE, 0.2f, 1);
            this.radius = Ball.DEFAULT_RADIUS * shrinkFactor;

            // Check if the ball is captured (aligned with the hole or shrunk below a size)
            if (distance < DIST_THRESH || this.radius < BALLSIZE_THRESH) {
                // Capture the ball: Remove it from the game
                app.balls.remove(this);
                handleCapture(hole);
            }
        }
    }

    /**
     * Handles the logic for when the ball is captured by a hole, including score updates.
     *
     * @param hole the hole that captured the ball
     */
    private void handleCapture(Hole hole) {
        int holeColorIndex = hole.getColorIndex(); // Get the hole's color

        boolean isValidCapture = (colorIndex == holeColorIndex || colorIndex == 0 || holeColorIndex == 0);

        if (isValidCapture) {
            // Successful capture
            int scoreIncrease = app.getScoreIncrease(colorIndex);
            app.addScore(scoreIncrease);
            app.balls.remove(this); // Remove the ball from the game
        } else {
            // Unsuccessful capture
            int scoreDecrease = app.getScoreDecrease(colorIndex);
            app.subtractScore(scoreDecrease);
            app.requeueBall(colorIndex); // Re-add the ball to the queue
            app.balls.remove(this); // Remove the ball from active play
        }

    }

    /**
     * Caps the ball's velocity to ensure it stays within the defined speed limits.
     */
    void capVelocity() {
        // Ensure the velocity does not exceed the max speed
        float speed = PApplet.sqrt(dx * dx + dy * dy);
        if (speed > MAX_SPEED) {
            float scalingFactor = MAX_SPEED / speed;
            dx *= scalingFactor;
            dy *= scalingFactor;
        }
        // Prevent the ball from freezing at extremely low speeds
        if (speed < MIN_SPEED) {
            dx = Math.signum(dx) * MIN_SPEED;
            dy = Math.signum(dy) * MIN_SPEED;
        }
    }

    // Getters:
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getDx() {
        return dx;
    }

    public float getDy() {
        return dy;
    }

    public float getRadius() {
        return radius;
    }

    public float getMaxSpeed() {
        return MAX_SPEED;
    }

    public int getColorIndex() {
        return colorIndex;
    }

    private int getColumn(float pos) {
        return PApplet.constrain((int) (pos / App.CELLSIZE), 0, App.WIDTH - 1);
    }

    private int getRow(float pos) {
        return PApplet.constrain((int) ((pos - App.TOPBAR) / App.CELLSIZE), 0, App.HEIGHT - 1);
    }

    /**
     * Checks if the given position corresponds to a wall tile on the game board.
     *
     * @param col the column of the tile
     * @param row the row of the tile
     * @return true if the tile is a wall or active timed tile, false otherwise
     */
    private boolean isWall(int col, int row) {
        if (col < 0 || col >= app.board[0].length || row < 0 || row >= app.board.length) {
            return false; // Out of bounds
        }

        char tile = app.board[row][col];

        // Check for a regular wall or a timed tile
        if (tile == 'X' || (tile >= '1' && tile <= '4')) {
            return true;
        }

        // Check if the tile is a timed tile and still active
        for (TimedTile timedTile : app.timedTiles) {
            if (timedTile.getX() == col && timedTile.getY() == row && timedTile.isActive()) {
                return true;
            }
        }

        return false;
    }

    // Mutators:
    public void setDx(float dx) {
        this.dx = dx;
    }

    public void setDy(float dy) {
        this.dy = dy;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    /**
     * Moves the ball to its previous position if no movement occurs.
     *
     * @param currX the current x-position of the ball
     * @param currY the current y-position of the ball
     */
    private void moveIfNoCollision(float currX, float currY) {
        if (Math.abs(dx) < EPSILON) {
            x = currX; // Restore previous X position if there's no horizontal movement
        }
        if (Math.abs(dy) < EPSILON) {
            y = currY; // Restore previous Y position if there's no vertical movement
        }
    }

    /**
     * Checks if the ball has collided with the screen edges and adjusts its velocity (trajectory).
     */
    private void checkScreenEdgeCollision() {
        if (x - radius < MIN_X || x + radius > App.WIDTH) {
            dx *= -1;
        }
        if (y - radius < MIN_Y || y + radius > App.HEIGHT) {
            dy *= -1;
        }
    }

    /**
     * Changes the ball's color based on the tile it collided with.
     *
     * @param tile the tile character indicating the new color
     */
    private void changeColor(char tile) {
        if (tile >= '1' && tile <= '4') {
            colorIndex = Character.getNumericValue(tile);
            ballSprite = app.getSprite("ball" + colorIndex); // Update the sprite
        }
    }

    /**
     * Updates the ball's position and handles collision detection and screen boundaries.
     */
    public void tick() {
        if (isCaptured)
            return; // Stop if captured
        // Regular movement logic
        x += dx;
        y += dy;
        float currX = x;
        float currY = y;
        int col = getColumn(x);
        int row = getRow(y);

        dx = handleCollision(dx, x, col, row, true);
        dy = handleCollision(dy, y, col, row, false);

        moveIfNoCollision(currX, currY);
        checkScreenEdgeCollision();
        capVelocity();
    }

        /**
     * Draws the ball on the game board.
     *
     * @param app the PApplet context for drawing
     * @param cellSize the size of each cell on the board
     * @param topBar the height of the top bar
     */
    public void draw(PApplet app, int cellSize, int topBar) {
        if (!isCaptured) {
            app.image(ballSprite, x - radius, y - radius, radius * 2, radius * 2);
        }
    }
}
