package inkball;

import processing.core.PImage;
import processing.core.PApplet;

/**
 * Represents a hole. A hole can capture balls that match
 * its color or a neutral color. Holes are drawn as a 2x2 grid on the game board.
 */
public class Hole extends Tile {
    private int colorIndex; // Color of the hole

    /**
     * Constructs a new {@code Hole} at the specified coordinates with the given color and sprite.
     *
     * @param x the x-coordinate of the hole on the game board
     * @param y the y-coordinate of the hole on the game board
     * @param colorIndex the color index of the hole
     * @param sprite the sprite image used to draw the hole
     */
    public Hole(int x, int y, int colorIndex, PImage sprite) {
        super(x, y, sprite);
        this.colorIndex = colorIndex;
    }

    /**
     * Calculates the x-coordinate of the center of the hole.
     *
     * @param app the game context used to get the cell size
     * @return the x-coordinate of the center of the hole
     */
    public float getCenterX(App app) {
        return x * app.CELLSIZE + app.CELLSIZE;
    }

    /**
     * Calculates the y-coordinate of the center of the hole.
     *
     * @param app the game context used to get the cell size and top bar height
     * @return the y-coordinate of the center of the hole
     */
    public float getCenterY(App app) {
        return y * app.CELLSIZE + app.TOPBAR + app.CELLSIZE;
    }

    /**
     * Returns the color index of the hole.
     *
     * @return the color index of the hole
     */
    public int getColorIndex() {
        return colorIndex;
    }

    /**
     * Draws the hole as a 2x2 grid on the game board.
     *
     * @param app the {@link PApplet} instance used for drawing
     * @param cellSize the size of each cell on the game board
     * @param topBar the height of the top bar in the game window
     */
    public void draw(PApplet app, int cellSize, int topBar) {
        app.image(sprite, x * cellSize, y * cellSize + topBar, cellSize*2, cellSize*2);            // Top-left
    }
}
