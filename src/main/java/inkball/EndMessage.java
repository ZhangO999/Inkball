package inkball;

/**
 * Represents the end message displayed when the game finishes.
 * This class extends the {@link Message} class and sets a predefined message 
 * indicating that the game has ended.
 */
public class EndMessage extends Message {
    /**
     * Constructs an {@code EndMessage} with the specified position and size.
     *
     * @param x the x-coordinate of the message's top-left corner
     * @param y the y-coordinate of the message's top-left corner
     * @param width the width of the message box
     * @param height the height of the message box
     */
    public EndMessage(int x, int y, int width, int height) {
        super(x, y, width, height, "=== ENDED ===");
    }
}
