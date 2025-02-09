package inkball;

import processing.core.PApplet;

/**
 * Represents a game object.
 * Any class that implements this interface must define how the object is drawn 
 * and provide access to its position on the game board.
 */
public interface GameObject {
    /**
     * Draws the game object on the game board using the provided {@link PApplet} context.
     *
     * @param app the {@link PApplet} instance used for drawing
     * @param cellSize the size of each cell on the board
     * @param topBar the height of the top bar in the game window
     */
    void draw(PApplet app, int cellSize, int topBar);  
    /**
     * Returns the x-coordinate of the game object on the game board.
     *
     * @return the x-coordinate of the object
     */
    float getX();            
    /**
     * Returns the y-coordinate of the game object on the game board.
     *
     * @return the y-coordinate of the object
     */
    float getY();            
}
