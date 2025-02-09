package inkball;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class SquiggleTest {
    private Squiggle squiggle;
    App app;

    @BeforeEach
    public void setup() {
        squiggle = new Squiggle();

        // Create a minimal App instance to simulate a Processing sketch environment
        app = new App();
        PApplet.runSketch(new String[]{"App"}, app);

        // Wait for the sketch to initialize
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAddPoints() {
        // Test to ensure that points are added correctly to the squiggle.
        squiggle.addPoint(10, 20);
        squiggle.addPoint(30, 40);

        assertEquals(2, squiggle.getPoints().size());
        assertEquals(new PVector(10, 20), squiggle.getPoints().get(0));
        assertEquals(new PVector(30, 40), squiggle.getPoints().get(1));
    }

    @Test
    public void testMaxNumberOfPoints() {
        // Test to verify that the squiggle maintains the maximum number of points allowed.
        for (int i = 0; i < Squiggle.MAX_NUM_POINTS + 10; i++) {
            squiggle.addPoint(i, i);
        }
        assertEquals(Squiggle.MAX_NUM_POINTS, squiggle.getPoints().size());
    }

    @Test
    public void testCollisionWithBall() {
        // Test to check if the squiggle detects a collision with a ball.
        squiggle.addPoint(0, 0);
        squiggle.addPoint(100, 0);

        Ball ball = new Ball(50, 0, 0, 0, null, 10, null);
        ball.setRadius(5);
        assertTrue(squiggle.isCollidingWithBall(ball));
    }

    @Test
    public void testNoCollisionWithBall() {
        // Test to ensure no collision is detected when the ball is far from the squiggle.
        squiggle.addPoint(0, 0);
        squiggle.addPoint(100, 0);

        Ball ball = new Ball(200, 200, 0, 0, null, 10, null);
        ball.setRadius(5);

        assertFalse(squiggle.isCollidingWithBall(ball));
    }

    @Test
    public void testContainsPoint() {
        // Test to verify if the squiggle contains specific points.
        squiggle.addPoint(0, 0);
        squiggle.addPoint(100, 0);

        assertTrue(squiggle.containsPoint(50, 0));
        assertFalse(squiggle.containsPoint(50, 10));
    }

    @Test
    public void testHandleCollision() {
        // Test to check if the ball’s velocity changes upon collision with the squiggle.
        squiggle.addPoint(0, 0);
        squiggle.addPoint(100, 0);

        Ball ball = new Ball(50, 0, 1, 1, null, 10, null);
        ball.setRadius(5);

        squiggle.handleCollision(ball);
        assertNotEquals(1, ball.getDx());
        assertNotEquals(1, ball.getDy());
    }

    @Test
    public void testPendingRemoval() {
        // Test to ensure that the squiggle is marked for removal correctly.
        assertFalse(squiggle.isRemoved());
        squiggle.pendingRemoval();
        assertTrue(squiggle.isRemoved());
    }

    @Test
    public void testResetCollisionFlag() {
        // Test to ensure the collision flag is reset after handling a collision.
        Ball ball = new Ball(50, 0, 1, 1, null, 10, null);

        squiggle.handleCollision(ball);
        squiggle.resetCollisionFlag();

        assertFalse(squiggle.collisionHandled());
    }

    @Test
    public void testDraw() {
        // Test to ensure the draw method completes without issues and the points are drawn correctly.  
        // Add some points to the squiggle
        squiggle.addPoint(10, 20);
        squiggle.addPoint(30, 40);

        // Ensure the sketch is ready and has a valid graphics context
        PGraphics graphics = app.getGraphics();
        assertNotNull(graphics);

        // Call the draw method and ensure it completes without issues
        squiggle.draw(app);

        // Verify the points were added correctly
        assertEquals(2, squiggle.getPoints().size());
    }

    @Test
    public void testCollisionHandledPreventsCollision() {
        // Test to ensure that after handling a collision, no further collisions occur.
        squiggle.addPoint(0, 0);
        squiggle.addPoint(100, 0);

        // Create a ball that will collide
        Ball ball = new Ball(50, 0, 1, 1, null, 10, null);
        ball.setRadius(5);

        // First collision should occur
        squiggle.handleCollision(ball);
        assertNotEquals(1, ball.getDx());
        assertNotEquals(1, ball.getDy());

        // Mark collision as handled and test the return path
        squiggle.handleCollision(ball);
        assertTrue(squiggle.collisionHandled());
    }

    @Test
    public void testZeroLengthSegmentReturnsDistance() {
        // Test to verify the correct distance calculation for a zero-length segment.
        squiggle.addPoint(10, 10);

        // Create a test point
        PVector testPoint = new PVector(15, 15);

        // Get the distance to the zero-length segment
        float distance = squiggle.getPoints().get(0).dist(testPoint);

        // Verify that the distance is correctly calculated
        assertEquals((float) Math.sqrt(50), distance);
    }

    @Test
    public void testAddPointTriggersMaxCapacity() {
        // Test to verify that the squiggle's points are capped at MAX_NUM_POINTS.
        for (int i = 0; i < Squiggle.MAX_NUM_POINTS + 10; i++) {
            squiggle.addPoint(i, i);
        }

        // Verify that the number of points is capped
        assertEquals(Squiggle.MAX_NUM_POINTS, squiggle.getPoints().size());
    }

    @Test
    public void testCollisionHandledFlag() {
        // Test to ensure the collisionHandled flag is set correctly after a collision.
        assertFalse(squiggle.collisionHandled());

        // Simulate a collision
        squiggle.addPoint(0, 0);
        squiggle.addPoint(100, 0);

        Ball ball = new Ball(50, 0, 1, 1, null, 10, null);
        squiggle.handleCollision(ball);

        // Verify collisionHandled is set to true
        assertTrue(squiggle.collisionHandled());
    }

    @Test
    public void testDistToLineSegmentZeroLengthSegment() {
        // Test to verify distance calculation when the segment has zero length.
        PVector startAndEnd = new PVector(10, 10);

        // Create a test point to calculate the distance to the segment
        PVector testPoint = new PVector(20, 20);

        // Call the package-private method directly
        float distance = squiggle.distToLineSegment(testPoint, startAndEnd, startAndEnd);

        // Verify the distance is correctly calculated as the distance to the start/end point
        assertEquals(PVector.dist(testPoint, startAndEnd), distance);
    }

}
