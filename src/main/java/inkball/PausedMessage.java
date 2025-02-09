package inkball;

/**
 * Represents a message that is displayed when the game is paused.
 * This class extends the {@link Message} class and sets a predefined message 
 * indicating that the game is paused.
 */
public class PausedMessage extends Message {
    /**
     * Constructs a {@code PausedMessage} with the specified position and size.
     * The width is adjusted to provide additional padding for the message.
     *
     * @param x the x-coordinate of the message's top-left corner
     * @param y the y-coordinate of the message's top-left corner
     * @param width the width of the message box (increased by 40 for padding)
     * @param height the height of the message box
     */
    public PausedMessage(int x, int y, int width, int height) {
        super(x, y, width + 40, height, "*** PAUSED ***");
    }
}
