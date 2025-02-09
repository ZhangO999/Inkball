package inkball;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import processing.core.PApplet;
import processing.core.PImage;

public class HoleTest {
    private Hole hole;
    private PImage mockSprite;

    @BeforeEach
    public void setup() {
        // Mocking a PImage for testing
        mockSprite = new PImage();

        // Initialize the Hole at position (2, 3) with color index 1 (orange)
        hole = new Hole(2, 3, 1, mockSprite);
    }

    @Test
    public void testInitialColorIndex() {
        // Test to verify that the hole's initial color index is correctly set to 1 (orange).
        assertEquals(1, hole.getColorIndex(), "Color index should be 1 (orange)");
    }

    @Test
    public void testGetCenterX() {
        // Test to verify the calculated center X-coordinate of the hole based on its position.
        float expectedX = 2 * App.CELLSIZE + App.CELLSIZE;

        // Verify the center X position of the hole
        assertEquals(expectedX, hole.getCenterX(null), 0.01, "Center X should match the expected value");
    }

    @Test
    public void testGetCenterY() {
        // Test to verify the calculated center Y-coordinate of the hole based on its position.
        float expectedY = 3 * App.CELLSIZE + App.TOPBAR + App.CELLSIZE;

        // Verify the center Y position of the hole
        assertEquals(expectedY, hole.getCenterY(null), 0.01, "Center Y should match the expected value");
    }

    @Test
    public void testDrawMethod() {
        // Test to ensure that calling the `draw` method for the hole does not throw any exceptions.
        PApplet applet = new PApplet();
        applet.setup();

        try {
            // Call the draw method to ensure it runs without errors
            hole.draw(applet, App.CELLSIZE, App.TOPBAR);
        } catch (Exception e) {
            fail("Drawing the hole threw an exception: " + e.getMessage());
        }
    }
}
