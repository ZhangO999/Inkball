package inkball;

import processing.core.PApplet;
import processing.core.PVector;
import java.util.LinkedList;

/**
 * Represents a 'squiggle' or player-drawn line.
 * A squiggle consists of multiple points connected as a line, which can interact with balls
 * by detecting collisions and reflecting their movement.
 */
public class Squiggle {
    private LinkedList<PVector> points;
    private float lineWidth;
    private boolean removed;
    private boolean collisionHandled = false;
    public static final int MAX_NUM_POINTS = 600; // Why would you need a line this long??!?!?

    /**
     * Constructs a new {@code Squiggle} with default settings.
     * Initializes an empty list of points and sets the default line width.
     */
    public Squiggle() {
        points = new LinkedList<>();
        lineWidth = 10;
        removed = false;
    }

    /**
     * Checks if the squiggle is colliding with the specified ball.
     *
     * @param ball the ball to check for collisions
     * @return {@code true} if the ball is colliding with the squiggle, {@code false} otherwise
     */
    public boolean isCollidingWithBall(Ball ball) {
        PVector ballCenter = new PVector(ball.getX(), ball.getY());
        for (int i = 1; i < points.size(); i++) {
            PVector p1 = points.get(i - 1);
            PVector p2 = points.get(i);
            if (distToLineSegment(ballCenter, p1, p2) <= ball.getRadius()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles the collision of the squiggle with the specified ball.
     * Reflects the ball's movement if a collision occurs.
     *
     * @param ball the ball involved in the collision
     */
    public void handleCollision(Ball ball) {
        if (collisionHandled)
            return;

        PVector ballCenter = getBallCenter(ball);

        for (int i = 1; i < points.size(); i++) {
            PVector p1 = points.get(i - 1);
            PVector p2 = points.get(i);

            if (distToLineSegment(ballCenter, p1, p2) <= ball.getRadius()) {
                reflectBall(ball, p1, p2);
                return;
            }
        }
    }

    /**
     * Resets the collision flag, allowing future collisions to be handled again.
     */
    public void resetCollisionFlag() {
        collisionHandled = false;
    }

    /**
     * Calculates the shortest distance from a point to a line segment.
     *
     * @param point the point to measure distance from
     * @param start the starting point of the line segment
     * @param end the ending point of the line segment
     * @return the shortest distance from the point to the line segment
     */
    float distToLineSegment(PVector point, PVector start, PVector end) {
        float lengthSquared = PVector.dist(start, end) * PVector.dist(start, end);

        if (lengthSquared == 0.0) { // If the segment length is 0, treat it as a point
            return PVector.dist(point, start);
        }

        // Calculate the projection factor 't' of 'point' onto the segment's line
        PVector startToPoint = PVector.sub(point, start);
        PVector startToEnd = PVector.sub(end, start);
        float t = PVector.dot(startToPoint, startToEnd) / lengthSquared;

        t = PApplet.constrain(t, 0, 1); // normalize to [0,1]
        PVector proj = PVector.add(start, PVector.mult(startToEnd, t));
        // Return the distance from the original point to the projected point on the
        // segment
        return PVector.dist(point, proj);

    }

    /**
     * Checks if the specified point lies on the squiggle within half the line width.
     *
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     * @return {@code true} if the point lies on the squiggle, {@code false} otherwise
     */
    public boolean containsPoint(float x, float y) {
        PVector point = new PVector(x, y);
        for (int i = 1; i < points.size(); i++) {
            PVector p1 = points.get(i - 1);
            PVector p2 = points.get(i);
            if (distToLineSegment(point, p1, p2) <= lineWidth / 2) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reflects the ball's velocity based on the line segment it collides with.
     *
     * @param ball the ball to reflect
     * @param p1 the starting point of the line segment
     * @param p2 the ending point of the line segment
     */
    private void reflectBall(Ball ball, PVector p1, PVector p2) {
        PVector lineVector = PVector.sub(p2, p1); // Line segment vector
        PVector normal = getNormalVector(lineVector); // Normal vector to the line segment

        PVector ballVelocity = new PVector(ball.getDx(), ball.getDy());
        PVector newTrajectory = computeNewTraj(ballVelocity, normal);

        // Update the ball's velocity with the reflected values
        ball.setDx(newTrajectory.x);
        ball.setDy(newTrajectory.y);
    }

    /**
     * Calculates the unit normal vector to a given line segment.
     *
     * @param lineVector the vector representing the line segment
     * @return the normalized normal vector to the line segment
     */
    private PVector getNormalVector(PVector lineVector) { // returns unit normal
        PVector normal = new PVector(-lineVector.y, lineVector.x);
        return normal.normalize(); // Ensure the vector has unit length
    }

    /**
     * Computes the new trajectory of the ball after reflecting off a normal vector.
     *
     * @param velocity the current velocity of the ball
     * @param normal the normal vector of the line segment
     * @return the new trajectory vector after reflection
     */
    private PVector computeNewTraj(PVector velocity, PVector normal) {
        return PVector.sub(velocity, PVector.mult(normal, 2 * velocity.dot(normal)));
    }

    // Getters and setters:

    public void pendingRemoval() {
        removed = true;
    }

    public boolean isRemoved() {
        return removed;
    }

    public boolean collisionHandled() {
        return collisionHandled;
    }

    /**
     * Retrieves the center of the specified ball as a {@link PVector}.
     *
     * @param ball the ball to get the center of
     * @return the center of the ball as a {@link PVector}
     */
    private PVector getBallCenter(Ball ball) {
        return new PVector(ball.getX(), ball.getY());
    }

    public LinkedList<PVector> getPoints() {
        return points;
    }

    /**
     * Adds a new point to the squiggle. If the maximum number of points is exceeded,
     * the oldest point is removed to maintain the size limit.
     *
     * @param x the x-coordinate of the new point
     * @param y the y-coordinate of the new point
     */
    public void addPoint(float x, float y) {
        points.add(new PVector(x, y));
        if (points.size() > MAX_NUM_POINTS) {
            points.removeFirst();
        }
    }

    /**
     * Draws the squiggle on the game board using the provided {@link App} context.
     *
     * @param app the {@link App} instance used for rendering
     */
    public void draw(App app) {
        app.pushStyle(); // Save the current stroke and strokeWeight settings

        app.stroke(0);
        app.strokeWeight(lineWidth);

        for (int i = 0; i < points.size() - 1; i++) {
            PVector p1 = points.get(i);
            PVector p2 = points.get(i + 1);
            app.line(p1.x, p1.y, p2.x, p2.y);
        }
        app.popStyle(); // Restore previous settings
    }
}
