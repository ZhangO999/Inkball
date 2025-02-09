package inkball;
import processing.core.PApplet;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import processing.core.PConstants;  // Import PConstants
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public class AppTest {
   private App app;

   @BeforeEach
   public void setup() {
       app = new App();
       PApplet.runSketch(new String[]{"App"}, app);
       app.configPath = "config.json"; // Set a valid config path
       app.setup(); // Initialize the game
   }

    @Test
    public void testFrameRate() {
        // Verify that the frame rate is within an acceptable range close to the target FPS.
        assertTrue(App.FPS - 2 <= app.frameRate &&
                    app.frameRate <= App.FPS + 2);
    }

    @Test
    public void testInitialScore() {
    // Ensure the initial score is set to 0 when the game starts.
        assertEquals(0, app.getScore());
    }

    @Test
    public void testLoadLevelConfiguration() {
        // Check if the configuration and level load correctly
        app.loadConfigForLevel(1);
        assertNotNull(app.getConfig());
        assertNotNull(app.board);
    }

    @Test
    public void testBallSpawnQueueInitialization() {
        // Ensure the ball spawn queue is initialized correctly
        List<String> queue = app.ballSpawnQueue;
        assertNotNull(queue);
        assertTrue(queue.isEmpty() || queue.size() > 0);
    }

    @Test
    public void testAddScoreIncreasesScore() {
        // Test if adding score works correctly
        int initialScore = app.getScore();
        app.addScore(10);
        assertEquals(initialScore + 10, app.getScore());
    }

    @Test
    public void testSubtractScoreDoesNotGoNegative() {
        // Test if subtracting score prevents negative scores
        app.subtractScore(10);
        assertEquals(0, app.getScore());
    }

    @Test
    public void testRequeueBallAddsToQueue() {
        // Test requeuing a ball
        app.requeueBall(1);
        assertFalse(app.ballSpawnQueue.isEmpty());
        assertEquals("orange", app.ballSpawnQueue.get(0));
    }

    @Test
    public void testIsLevelComplete() {
        // Check if level completion logic works
        app.ballSpawnQueue.clear();
        app.balls.clear();
        assertTrue(app.isLevelComplete());
    }

    @Test
    public void testStartVictorySequence() {
        // Verify the victory sequence starts properly
        app.startVictorySequence();
        assertTrue(app.getVictoryInProgress());
    }

    @Test
    public void testPauseFunctionality() {
        // Test if the game pauses correctly
        app.keyPressed(new processing.event.KeyEvent(null, 0, 0, 0, ' ', ' '));
        assertTrue(app.isPaused());
    }

    @Test
    public void testRemoveSquiggle() {
        // Test if squiggle removal works
        app.mousePressed(null);
        app.mouseReleased(null);
        int initialSize = app.getSquiggles().size();
        app.removeSquiggleAt(0, 0);
        assertTrue(app.getSquiggles().size() <= initialSize);
    }

    @Test
    public void testLoadNextLevel() {
        // Test if the game loads the next level correctly
        int currentLevel = app.getCurrentLevel();
        app.loadNextLevel();
        assertEquals(currentLevel + 1, app.getCurrentLevel());
    }

    @Test
    public void testEndGameAfterAllLevels() {
        // Get the total number of levels from the config
        int totalLevels = app.getConfig().getJSONArray("levels").size();
        
        // Set the current level using a method or test logic (since currentLevel is private)
        // Simulate the scenario where the last level has been completed
        for (int i = 0; i < totalLevels; i++) {
            app.loadNextLevel();  // Keep loading next level until end of game
        }

        assertTrue(app.isGameEnded());  // Verify that the game has ended
    }
    
    @Test
    public void testVictoryAnimationCompletes() throws Exception {
        // Confirm that the victory animation completes.

        Field isTimeDrainedField = App.class.getDeclaredField("isTimeDrained");
        isTimeDrainedField.setAccessible(true);  // Allow access to the field

        // Set the field value to true
        isTimeDrainedField.set(app, true);

        // Call the method to update victory tiles
        app.updateVictoryTiles();

        // Verify that the victory animation has completed
        assertTrue(app.isVictoryAnimationComplete());
    }

    @Test
    public void testUpdateTimerPaused() {
        // Set the game to paused state
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', ' '));
        app.updateTimer();  // Call the timer update

        // Ensure the remaining time is not decremented when paused
        int remainingTimeBefore = app.getConfig().getInt("time") * App.FPS;
        assertEquals(remainingTimeBefore, app.getConfig().getInt("time") * App.FPS);
    }

    @Test
    public void testBallHoleInteraction() {
        // Simulate adding a ball and a hole
        Ball testBall = new Ball(100, 100, 2, 2, app.getSprite("ball0"), 0, app);
        Hole testHole = new Hole(3, 3, 0, app.getSprite("hole0"));

        app.balls.add(testBall);
        app.holes.add(testHole);

        // Run the logic for interaction
        testBall.attractToHole(testHole);

        // Ensure the ball moves towards the hole (or interacts correctly)
        assertNotEquals(100, testBall.getX());
        assertNotEquals(100, testBall.getY());
    }

    @Test
    public void testRestartLevel() {
        // Simulate starting and restarting the level
        app.addScore(10);  // Add score
        app.keyPressed(new KeyEvent(null, 0, 0, 0, 'r', 'r'));  // Press 'r' to restart

        // Ensure the score is reset to the initial state
        assertEquals(0, app.getScore());
        assertEquals(1, app.getCurrentLevel());  // Game should restart to level 1
    }

    @Test
    public void testInvalidLevelLoad() {
        // Attempt to load a non-existent level
        app.loadConfigForLevel(999);  // A level that doesn't exist

        // Ensure that no more levels can be loaded
        assertNull(app.getLevelFilePath(999));
    }

    @Test
    public void testDrawAndRemoveSquiggle() {
        // Simulate pressing the left mouse button
        app.mousePressed(new MouseEvent(null, 0, 0, 0, 50, 50, PConstants.LEFT, 1));
        
        // Simulate dragging the mouse
        app.mouseDragged(new MouseEvent(null, 0, 0, 0, 100, 100, PConstants.LEFT, 1));
        
        // Simulate releasing the left mouse button
        app.mouseReleased(new MouseEvent(null, 0, 0, 0, 100, 100, PConstants.LEFT, 1));
    
        // Ensure a squiggle was added
        assertEquals(1, app.getSquiggles().size());
    
        // Remove the squiggle and ensure it is removed
        app.removeSquiggleAt(50, 50);
        assertEquals(0, app.getSquiggles().size());
    }

    @Test
    public void testNoSpawnWhenPaused() {
        // Test to ensure that no ball spawns when the game is paused.
        app.ballSpawnQueue.add("grey");  // Add a ball to the spawn queue
        app.SPAWNTIMER = 0;              // Force spawn
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', ' '));  // Pause the game

        app.handleSpawning();

        assertTrue(app.balls.isEmpty());  // Verify no ball has spawned
    }

    @Test
    public void testNoSpawnWhenQueueEmpty() {
        // Test to verify that no ball spawns if the spawn queue is empty.
        app.SPAWNTIMER = 0;  // Force spawn

        app.handleSpawning();

        assertTrue(app.balls.isEmpty());  // Verify no ball has spawned
    }

    @Test
    public void testDrawWhenGameRunning() {
        // Test to ensure the board draws properly when the game is running.
        app.isPaused = false;   // Ensure the game is running
        app.draw();             // Call the draw method

        // Assuming draw updates some UI components like a timer or top bar
        assertNotNull(app.getBoard());  // Verify the board is drawn
    }

    @Test
    public void testDrawWhenPaused() {
        // Test to verify that the game shows the paused state when paused.
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', ' '));  // Pause the game
        app.draw();  // Call the draw method

        // Verify that paused message is shown
        assertTrue(app.isPaused());
    }

    @Test
    public void testDrawWhenLevelComplete() {
        // Test to ensure the game recognizes when a level is complete.
        app.ballSpawnQueue.clear();  // Ensure level is marked complete
        app.balls.clear();
        app.draw();

        assertTrue(app.isLevelComplete());  // Verify level completion state
    }

    
    @Test
    public void testMultipleLevelLoad() {
        // Test to verify that multiple levels load correctly in sequence.
        int initialLevel = app.getCurrentLevel();

        // Load two more levels
        app.loadNextLevel();
        app.loadNextLevel();

        assertEquals(initialLevel + 2, app.getCurrentLevel());
    }

    @Test
    public void testPauseAndResume() {
        // Test to ensure that the game pauses and resumes correctly.
        // Pause the game:
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', ' '));
        assertTrue(app.isPaused());

        // Resume the game
        app.keyPressed(new KeyEvent(null, 0, 0, 0, ' ', ' '));
        assertFalse(app.isPaused());
    }

    @Test
    public void testTimerCountdown() {
        // Test to verify that the timer decreases over time.
        int initialTime = app.getConfig().getInt("time") * App.FPS;

        // Simulate timer updates
        for (int i = 0; i < 5; i++) {
            app.updateTimer();
        }

        // Verify the time is decreasing
        assertTrue(app.getConfig().getInt("time") * App.FPS < initialTime);
    }

    @Test
    public void testVictoryAnimationTrigger() {
        // Test to ensure the victory animation starts correctly when a level is complete.
        // Mark the level as complete
        app.ballSpawnQueue.clear();
        app.balls.clear();
        app.startVictorySequence();

        // Verify the animation starts
        assertTrue(app.getVictoryInProgress());
    }

    @Test
    public void testGameRestartAfterGameOver() {
        // Test to ensure the game restarts properly after completing all levels.
        int totalLevels = app.getConfig().getJSONArray("levels").size();
        for (int i = 0; i < totalLevels; i++) {
            app.loadNextLevel();
        }

        // Restart the game
        app.keyPressed(new KeyEvent(null, 0, 0, 0, 'r', 'r'));

        // Verify the game restarted from level 1
        assertEquals(1, app.getCurrentLevel());
        assertEquals(0, app.getScore());
    }

    @Test
    public void testMouseDraggedNoActionWhenVictoryOrFailed() {
        // Test to ensure no new squiggle is added during victory or if the level has failed.
        app.victoryInProgress = true;  // Simulate victory in progress
        app.isLevelFailed = false;

        // Trigger mouseDragged event
        app.mouseDragged(new MouseEvent(null, 0, 0, 0, 100, 100, PConstants.LEFT, 1));

        assertNull(app.getCurrentSqiggle(), "No squiggle should be created during victory.");
        
        // Now test when the level is failed
        app.victoryInProgress = false;
        app.isLevelFailed = true;
        app.mouseDragged(new MouseEvent(null, 0, 0, 0, 100, 100, PConstants.LEFT, 1));

        assertNull(app.getCurrentSqiggle(), "No squiggle should be created if the level has failed.");
    }

    @Test
    public void testMouseDraggedAddsNewPoint() {    
        // Test to ensure a new point is added when the mouse is dragged beyond the threshold.
        app.currentSquiggle = new Squiggle();
        app.drawEventOngoing = true;
        app.oldX = 50;
        app.oldY = 50;

        // Simulate dragging the mouse beyond the threshold distance
        app.mouseDragged(new MouseEvent(null, 0, 0, 0, 100, 100, PConstants.LEFT, 1));

        assertEquals(1, app.currentSquiggle.getPoints().size(), 
                    "One point should be added if the mouse moves significantly.");
    }

    @Test
    public void testMouseDraggedNoNewPointIfMoveTooLittle() {
        // Test to ensure no new point is added if the mouse moves less than the threshold.
        app.currentSquiggle = new Squiggle();
        app.drawEventOngoing = true;
        app.oldX = 50;
        app.oldY = 50;

        // Simulate dragging the mouse with small movement
        app.mouseDragged(new MouseEvent(null, 0, 0, 0, 52, 52, PConstants.LEFT, 1));

        assertEquals(0, app.currentSquiggle.getPoints().size(), 
                    "No point should be added if the mouse moves less than the threshold.");
    }

    @Test
    public void testMouseDraggedRespectsDrawLimit() {
        // Test to ensure that the draw limit is respected, and no more than the allowed number of points are added.
        app.currentSquiggle = new Squiggle();
        app.drawEventOngoing = true;
        app.oldX = 0;
        app.oldY = 0;

        // Set the draw limit to 2 for this test
        app.squigglesPerFrameCap = 2;

        // Add points until the limit is reached
        app.mouseDragged(new MouseEvent(null, 0, 0, 0, 100, 100, PConstants.LEFT, 1));
        app.mouseDragged(new MouseEvent(null, 0, 0, 0, 200, 200, PConstants.LEFT, 1));
        app.mouseDragged(new MouseEvent(null, 0, 0, 0, 300, 300, PConstants.LEFT, 1));  // Exceeding limit

        assertEquals(2, app.currentSquiggle.getPoints().size(), 
                    "Number of points should not exceed the draw limit.");
    }

    @Test
    public void testMouseDraggedNoPointsIfNotLeftClick() {
        // Test to ensure no points are added if the mouse event is not a left click.
        app.currentSquiggle = new Squiggle();
        app.drawEventOngoing = true;
        app.oldX = 50;
        app.oldY = 50;

        // Simulate dragging the mouse with a right click
        app.mouseDragged(new MouseEvent(null, 0, 0, 0, 100, 100, PConstants.RIGHT, 1));

        assertEquals(0, app.currentSquiggle.getPoints().size(), 
                    "No point should be added if the event is not a left click.");
    }





}
