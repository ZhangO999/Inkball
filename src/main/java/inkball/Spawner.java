package inkball;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * Represents a spawner tile, responsible for spawning balls.
 * The spawner extends the {@link Tile} class and provides additional functionality
 * to convert color names to their corresponding indices.
 */
public class Spawner extends Tile {
    /**
     * Constructs a new {@code Spawner} at the specified position with the given sprite.
     *
     * @param app the game context
     * @param x the x-coordinate of the spawner on the game board
     * @param y the y-coordinate of the spawner on the game board
     * @param sprite the sprite image used to draw the spawner
     */
    public Spawner(App app, int x, int y, PImage sprite) {
        super(x, y, sprite);
    }

    /**
     * Converts a ball color name to its corresponding index.
     * Supported colors are grey, orange, blue, green, and yellow.
     * If the color name is unrecognized, it defaults to grey (index 0).
     *
     * @param color the name of the color (case-insensitive)
     * @return the index corresponding to the color, or 0 if unknown
     */
    public int getColorIndex(String color) {
        switch (color.toLowerCase()) {
            case "grey": return 0;
            case "orange": return 1;
            case "blue": return 2;
            case "green": return 3;
            case "yellow": return 4;
            default: return 0; // Default to grey if unknown
        }
    }
    
    /**
     * Draws the spawner on the game board using the provided {@link PApplet} context.
     *
     * @param app the {@link PApplet} instance used for rendering
     * @param cellSize the size of each cell on the game board
     * @param topBar the height of the top bar in the game window
     */
    @Override
    public void draw(PApplet app, int cellSize, int topBar) {
        app.image(sprite, x * cellSize, y * cellSize + topBar, cellSize, cellSize);
    }
    
}
