package inkball;

import processing.core.PImage;
import processing.core.PApplet;

/**
 * Represents an abstract tile on the game board. 
 * A tile has a position on the grid and a sprite image associated with it.
 * Subclasses must provide an implementation for the {@code draw} method to 
 * render the tile on the game board.
 */
public abstract class Tile implements GameObject{
    protected int x, y;  // Position on the grid
    protected PImage sprite;  // Image for the tile

    /**
     * Constructs a new {@code Tile} at the specified position with the given sprite.
     *
     * @param x the x-coordinate of the tile on the grid
     * @param y the y-coordinate of the tile on the grid
     * @param sprite the sprite image used to draw the tile
     */
    public Tile(int x, int y, PImage sprite) {
        this.x = x;
        this.y = y;
        this.sprite = sprite;
    }

    /**
     * Draws the tile on the game board using the provided {@link PApplet} context.
     * Subclasses must provide their own implementation of this method.
     *
     * @param app the {@link PApplet} instance used for rendering
     * @param cellSize the size of each cell on the game board
     * @param topBar the height of the top bar in the game window
     */
    public abstract void draw(PApplet app, int cellSize, int topBar);

    /**
     * Returns the x-coordinate of the tile on the game grid.
     *
     * @return the x-coordinate of the tile
     */
    public float getX() {
        return x;
    }
    /**
     * Returns the y-coordinate of the tile on the game grid.
     *
     * @return the y-coordinate of the tile
     */
    public float getY() {
        return y;
    }
}
