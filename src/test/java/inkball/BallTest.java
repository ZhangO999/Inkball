package inkball;
import processing.core.PApplet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;

public class BallTest {
    static Ball ball;
    static App app;

    @BeforeAll
    public static void setUp() {
        app = new App(); 
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.configPath = "config.json"; 
    }

    @Test
    public void testFrameRate() {
        // Test to verify the app setup completes without issues.
        app.setup();
    }

    @Test
    public void testVelocityExceedsMaxSpeed() {
        // Test to ensure the ball's speed does not exceed MAX_SPEED after scaling.
        ball.setDx(10);
        ball.setDy(0);

        ball.capVelocity();

        float newSpeed = (float) Math.sqrt(ball.getDx() * ball.getDx() + ball.getDy() * ball.getDy());
        assertTrue(newSpeed <= ball.getMaxSpeed(), "The speed should not exceed MAX_SPEED.");
    }

    @Test
    public void testDxIsScaledCorrectly() {
        // Test to verify the horizontal velocity (Dx) is scaled correctly when exceeding MAX_SPEED.
        ball.setDx(10);
        ball.setDy(0);

        float initialSpeed = (float) Math.sqrt(10 * 10);
        float expectedScalingFactor = ball.getMaxSpeed() / initialSpeed;

        ball.capVelocity();

        assertEquals(10 * expectedScalingFactor, ball.getDx(), 0.0001, "Dx should be scaled correctly.");
    }

    @Test
    public void testDyIsScaledCorrectly() {
        // Test to verify the vertical velocity (Dy) is scaled correctly when exceeding MAX_SPEED.
        ball.setDx(0);
        ball.setDy(10);

        float initialSpeed = (float) Math.sqrt(10 * 10);
        float expectedScalingFactor = ball.getMaxSpeed() / initialSpeed;

        ball.capVelocity();

        assertEquals(10 * expectedScalingFactor, ball.getDy(), 0.0001, "Dy should be scaled correctly.");
    }

    @Test
    public void testSpeedIsCappedToMaxSpeed() {
        // Test to ensure the ball's speed is capped to MAX_SPEED when its speed exceeds the limit.
        ball.setDx(6);
        ball.setDy(8); // Initial speed is 10 (hypotenuse)

        ball.capVelocity();

        float newSpeed = (float) Math.sqrt(ball.getDx() * ball.getDx() + ball.getDy() * ball.getDy());
        assertEquals(ball.getMaxSpeed(), newSpeed, 0.0001, "The speed should be capped to MAX_SPEED.");
    }

    @Test
    public void testVelocityScalingWithBothDxAndDy() {
        // Test to verify both Dx and Dy are scaled correctly when the ball's speed exceeds MAX_SPEED.
        ball.setDx(6);
        ball.setDy(8); // Initial speed is 10

        float initialSpeed = (float) Math.sqrt(6 * 6 + 8 * 8);
        float expectedScalingFactor = ball.getMaxSpeed() / initialSpeed;

        ball.capVelocity();

        assertEquals(6 * expectedScalingFactor, ball.getDx(), 0.0001, "Dx should be scaled correctly.");
        assertEquals(8 * expectedScalingFactor, ball.getDy(), 0.0001, "Dy should be scaled correctly.");
    }

}
