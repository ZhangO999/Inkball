package inkball;

import processing.core.PApplet;

/**
 * Represents an abstract user interface element.
 * A {@code UIElement} defines the basic structure for UI components, including 
 * position and size. 
 */
public abstract class UIElement {
    protected int x, y;
    protected int width, height; // Size of the element

    /**
     * Constructs a {@code UIElement} with the specified position and size.
     *
     * @param x the x-coordinate of the top-left corner of the element
     * @param y the y-coordinate of the top-left corner of the element
     * @param width the width of the element
     * @param height the height of the element
     */
    public UIElement(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Draws the UI element on the screen using the provided {@link PApplet} context.
     * Subclasses must implement this method to define how the element is rendered.
     *
     * @param app the {@link PApplet} instance used for rendering
     */
    public abstract void draw(PApplet app);
}
