package inkball;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import processing.core.PApplet;
import processing.core.PImage;

public class TimedTileTest {
    private TimedTile timedTile;
    private PImage mockSprite;

    @BeforeEach
    public void setup() {
        // Mocking a PImage for the test
        mockSprite = new PImage();
        timedTile = new TimedTile(3, 5, mockSprite); // Initializing at (3, 5) with mock sprite
    }

    @Test
    public void testInitialAlphaAndActiveState() {
        // Test to ensure that the initial alpha value is 255 (fully opaque) 
        // and the tile is active when initialized.
        assertEquals(255, timedTile.getAlpha()); // Should be fully opaque at the start
        assertTrue(timedTile.isActive()); // Tile should be active initially
    }

    @Test
    public void testAlphaDecreasesOverTime() {
        // Test if the alpha value decreases with updateAlpha method calls
        timedTile.updateAlpha();
        assertTrue(timedTile.getAlpha() < 255); // Alpha should decrease
    }

    @Test
    public void testBecomesInactiveWhenAlphaIsZero() {
        // Test to ensure that the tile becomes inactive when the alpha value is reduced to 0.
        while (timedTile.getAlpha() > 0) {
            timedTile.updateAlpha();
        }

        // After alpha is 0, the tile should be inactive
        assertFalse(timedTile.isActive());
        assertEquals(0, timedTile.getAlpha()); // Ensure alpha is exactly 0
    }

    @Test
    public void testAlphaDoesNotGoBelowZero() {
        // Test to verify that the alpha value does not drop below 0 
        // even after multiple updateAlpha() calls.
        for (int i = 0; i < 1000; i++) {
            timedTile.updateAlpha();
        }

        assertEquals(0, timedTile.getAlpha()); // Alpha should not go below 0
    }

    @Test
    public void testDrawMethodWithAlpha() {
        // Test to ensure the draw method works without exceptions when drawing a tile 
        // with a valid alpha value.
        PApplet applet = new PApplet();
        applet.setup();

        try {
            timedTile.draw(applet, 32, 64); // Test drawing with default alpha
        } catch (Exception e) {
            fail("Drawing the timed tile threw an exception: " + e.getMessage());
        }
    }

    @Test
    public void testDrawWithAlphaZero() {
        // Test to verify that the draw method does not throw any exceptions 
        // even when the tile's alpha value is 0.
        PApplet applet = new PApplet();
        applet.setup();

        // Reduce alpha to 0
        while (timedTile.getAlpha() > 0) {
            timedTile.updateAlpha();
        }

        // Ensure draw method works when alpha is 0 (should not be drawn)
        try {
            timedTile.draw(applet, 32, 64); // Drawing with alpha = 0
        } catch (Exception e) {
            fail("Drawing with alpha = 0 threw an exception: " + e.getMessage());
        }
    }
}
