package inkball;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * Represents a tile that fades over time and becomes inactive.
 * A {@code TimedTile} starts fully opaque and gradually becomes transparent.
 * Once fully transparent, it no longer interacts with balls.
 */
public class TimedTile extends Tile {
    private float alpha; // Transparency value (0 - 255)
    private boolean active; // Whether the tile can still collide with balls
    private static final float ALPHA_DECREMENT = 0.5f; // Rate of transparency reduction

    /**
     * Constructs a {@code TimedTile} at the specified position with the given sprite.
     * The tile starts fully opaque and active for collisions.
     *
     * @param x the x-coordinate of the tile on the game board
     * @param y the y-coordinate of the tile on the game board
     * @param sprite the sprite image used to draw the tile
     */
    public TimedTile(int x, int y, PImage sprite) {
        super(x, y, sprite);
        this.alpha = 255; // Start fully opaque
        this.active = true; // Initially, collisions are enabled
    }

    /**
     * Draws the tile on the game board with the current transparency (alpha) value.
     * If the tile is fully transparent, it is not drawn.
     *
     * @param app the {@link PApplet} instance used for rendering
     * @param cellSize the size of each cell on the game board
     * @param topBar the height of the top bar in the game window
     */
    public void draw(PApplet app, int cellSize, int topBar) {
        if (alpha > 0) {
            app.tint(255, 255, 255, alpha); // Apply the transparency
            app.image(sprite, x * cellSize, y * cellSize + topBar, cellSize, cellSize);
            app.noTint(); // Reset tint after drawing
        }
    }

    /**
     * Updates the transparency (alpha) of the tile.
     * Once the tile becomes fully transparent, it becomes inactive.
     */
    public void updateAlpha() {
        if (alpha > 0) {
            alpha -= ALPHA_DECREMENT; // Decrease alpha value
            if (alpha <= 0) {
                alpha = 0;
                active = false; // Disable collisions when fully transparent
            }
        }
    }

    /**
     * Checks if the tile is still active and able to collide with balls.
     *
     * @return {@code true} if the tile is active, {@code false} otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Returns the current transparency (alpha) value of the tile.
     *
     * @return the alpha value, ranging from 0 (fully transparent) to 255 (fully opaque)
     */
    public float getAlpha() {
        return alpha;
    }
}