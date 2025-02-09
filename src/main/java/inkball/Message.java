package inkball;

import processing.core.PApplet;

/**
 * Represents an abstract message element. 
 * This class defines the basic structure for displaying messages as part of the user interface.
 * Subclasses should provide specific messages, such as end-game messages or pause notifications.
 */
public abstract class Message extends UIElement {
    protected String text;
    /**
     * Constructs a new {@code Message} with the specified position, size, and text content.
     *
     * @param x the x-coordinate of the message's top-left corner
     * @param y the y-coordinate of the message's top-left corner
     * @param width the width of the message box
     * @param height the height of the message box
     * @param text the text content of the message
     */
    public Message(int x, int y, int width, int height, String text) {
        super(x, y, width, height);
        this.text = text;
    }

    /**
     * Draws the message on the screen using the provided {@link PApplet} context.
     * The message is centered within the defined element area.
     *
     * @param app the {@link PApplet} instance used for rendering
     */
    @Override
    public void draw(PApplet app) {
        app.pushStyle();
        app.fill(0); // Black text
        app.textSize(24);
        app.textAlign(PApplet.CENTER, PApplet.CENTER);
        app.text(text, x + width / 2, y + height / 2); // Center text in the element
        app.popStyle();
    }
}
