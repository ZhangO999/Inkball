package inkball;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import processing.core.PApplet;
import processing.core.PImage;

public class SpawnerTest {
    private Spawner spawner;
    private App app;

    @BeforeEach
    public void setup() {
        app = new App();
        PImage sprite = new PImage(); // Mock sprite image
        spawner = new Spawner(app, 5, 10, sprite);
    }

    @Test
    public void testGetColorIndex() {
        // Test to ensure that the correct color index is returned for valid color names.
        assertEquals(0, spawner.getColorIndex("grey"));
        assertEquals(1, spawner.getColorIndex("orange"));
        assertEquals(2, spawner.getColorIndex("blue"));
        assertEquals(3, spawner.getColorIndex("green"));
        assertEquals(4, spawner.getColorIndex("yellow"));
    }

    @Test
    public void testGetColorIndexWithInvalidColor() {
        // Test to verify that the color index defaults to 0 (grey) for invalid color names.
        assertEquals(0, spawner.getColorIndex("unknown"));
        assertEquals(0, spawner.getColorIndex("purple")); // Default should be grey (0)
    }

    @Test
    public void testDraw() {
        // Test to ensure the `draw` method executes without throwing any exceptions.
        PApplet applet = new PApplet();
        applet.setup();
        
        try {
            spawner.draw(applet, 32, 64); // Using 32x32 cell size with top bar offset
        } catch (Exception e) {
            fail("Drawing the spawner threw an exception: " + e.getMessage());
        }
    }

    @Test
    public void testSpawnerCoordinates() {
        // Test to verify that the spawner is initialized with the correct X and Y coordinates.
        assertEquals(5, spawner.getX());
        assertEquals(10, spawner.getY());
    }
}
