package inkball;

/**
 * Represents a message that is displayed when the game time runs out.
 * This class extends the {@link Message} class and displays a "TIME'S UP" message.
 */
public class TimeUpMessage extends Message {
    /**
     * Constructs a {@code TimeUpMessage} with the specified position and size.
     * The width is adjusted to provide additional padding for the message.
     *
     * @param x the x-coordinate of the message's top-left corner
     * @param y the y-coordinate of the message's top-left corner
     * @param width the width of the message box (increased by 40 for padding)
     * @param height the height of the message box
     */
    public TimeUpMessage(int x, int y, int width, int height) {
        super(x, y, width + 40, height, "=== TIME'S UP ===");
    }
}
