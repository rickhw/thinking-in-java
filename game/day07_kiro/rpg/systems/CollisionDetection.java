package rpg.systems;

import java.awt.Rectangle;
import java.awt.geom.Point2D;

/**
 * Utility class containing various collision detection algorithms.
 * Provides methods for AABB, circle, and other collision detection types.
 */
public class CollisionDetection {
    
    /**
     * Axis-Aligned Bounding Box (AABB) collision detection
     * @param rect1 First rectangle
     * @param rect2 Second rectangle
     * @return true if rectangles intersect
     */
    public static boolean aabbCollision(Rectangle rect1, Rectangle rect2) {
        return rect1.x < rect2.x + rect2.width &&
               rect1.x + rect1.width > rect2.x &&
               rect1.y < rect2.y + rect2.height &&
               rect1.y + rect1.height > rect2.y;
    }
    
    /**
     * Circle collision detection
     * @param x1 Center X of first circle
     * @param y1 Center Y of first circle
     * @param radius1 Radius of first circle
     * @param x2 Center X of second circle
     * @param y2 Center Y of second circle
     * @param radius2 Radius of second circle
     * @return true if circles intersect
     */
    public static boolean circleCollision(float x1, float y1, float radius1,
                                        float x2, float y2, float radius2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float distanceSquared = dx * dx + dy * dy;
        float radiusSum = radius1 + radius2;
        return distanceSquared <= radiusSum * radiusSum;
    }
    
    /**
     * Circle vs Rectangle collision detection
     * @param circleX Center X of circle
     * @param circleY Center Y of circle
     * @param radius Radius of circle
     * @param rect Rectangle to check against
     * @return true if circle intersects rectangle
     */
    public static boolean circleRectangleCollision(float circleX, float circleY, float radius, Rectangle rect) {
        // Find the closest point on the rectangle to the circle center
        float closestX = Math.max(rect.x, Math.min(circleX, rect.x + rect.width));
        float closestY = Math.max(rect.y, Math.min(circleY, rect.y + rect.height));
        
        // Calculate distance from circle center to closest point
        float dx = circleX - closestX;
        float dy = circleY - closestY;
        float distanceSquared = dx * dx + dy * dy;
        
        return distanceSquared <= radius * radius;
    }
    
    /**
     * Point in rectangle collision detection
     * @param pointX X coordinate of point
     * @param pointY Y coordinate of point
     * @param rect Rectangle to check against
     * @return true if point is inside rectangle
     */
    public static boolean pointInRectangle(float pointX, float pointY, Rectangle rect) {
        return pointX >= rect.x && pointX <= rect.x + rect.width &&
               pointY >= rect.y && pointY <= rect.y + rect.height;
    }
    
    /**
     * Point in circle collision detection
     * @param pointX X coordinate of point
     * @param pointY Y coordinate of point
     * @param circleX Center X of circle
     * @param circleY Center Y of circle
     * @param radius Radius of circle
     * @return true if point is inside circle
     */
    public static boolean pointInCircle(float pointX, float pointY, 
                                      float circleX, float circleY, float radius) {
        float dx = pointX - circleX;
        float dy = pointY - circleY;
        float distanceSquared = dx * dx + dy * dy;
        return distanceSquared <= radius * radius;
    }
    
    /**
     * Calculate the overlap between two rectangles on the X axis
     * @param rect1 First rectangle
     * @param rect2 Second rectangle
     * @return Overlap amount (positive if overlapping, 0 if touching, negative if separated)
     */
    public static float getOverlapX(Rectangle rect1, Rectangle rect2) {
        float left = Math.max(rect1.x, rect2.x);
        float right = Math.min(rect1.x + rect1.width, rect2.x + rect2.width);
        return right - left;
    }
    
    /**
     * Calculate the overlap between two rectangles on the Y axis
     * @param rect1 First rectangle
     * @param rect2 Second rectangle
     * @return Overlap amount (positive if overlapping, 0 if touching, negative if separated)
     */
    public static float getOverlapY(Rectangle rect1, Rectangle rect2) {
        float top = Math.max(rect1.y, rect2.y);
        float bottom = Math.min(rect1.y + rect1.height, rect2.y + rect2.height);
        return bottom - top;
    }
    
    /**
     * Calculate the minimum translation vector to separate two overlapping rectangles
     * @param rect1 First rectangle
     * @param rect2 Second rectangle
     * @return Point2D representing the MTV (x, y components)
     */
    public static Point2D.Float calculateMTV(Rectangle rect1, Rectangle rect2) {
        if (!aabbCollision(rect1, rect2)) {
            return new Point2D.Float(0, 0);
        }
        
        float overlapX = getOverlapX(rect1, rect2);
        float overlapY = getOverlapY(rect1, rect2);
        
        // Choose the axis with minimum overlap for separation
        if (Math.abs(overlapX) < Math.abs(overlapY)) {
            // Separate horizontally
            float direction = (rect1.x < rect2.x) ? -1 : 1;
            return new Point2D.Float(overlapX * direction, 0);
        } else {
            // Separate vertically
            float direction = (rect1.y < rect2.y) ? -1 : 1;
            return new Point2D.Float(0, overlapY * direction);
        }
    }
    
    /**
     * Check if a rectangle is completely inside another rectangle
     * @param inner Rectangle to check if inside
     * @param outer Rectangle to check against
     * @return true if inner is completely inside outer
     */
    public static boolean rectangleContains(Rectangle outer, Rectangle inner) {
        return outer.x <= inner.x &&
               outer.y <= inner.y &&
               outer.x + outer.width >= inner.x + inner.width &&
               outer.y + outer.height >= inner.y + inner.height;
    }
    
    /**
     * Calculate distance between two points
     * @param x1 X coordinate of first point
     * @param y1 Y coordinate of first point
     * @param x2 X coordinate of second point
     * @param y2 Y coordinate of second point
     * @return Distance between points
     */
    public static float distance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Calculate squared distance between two points (faster than distance)
     * @param x1 X coordinate of first point
     * @param y1 Y coordinate of first point
     * @param x2 X coordinate of second point
     * @param y2 Y coordinate of second point
     * @return Squared distance between points
     */
    public static float distanceSquared(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return dx * dx + dy * dy;
    }
    
    /**
     * Line segment intersection test
     * @param x1 Start X of first line
     * @param y1 Start Y of first line
     * @param x2 End X of first line
     * @param y2 End Y of first line
     * @param x3 Start X of second line
     * @param y3 Start Y of second line
     * @param x4 End X of second line
     * @param y4 End Y of second line
     * @return true if line segments intersect
     */
    public static boolean lineIntersection(float x1, float y1, float x2, float y2,
                                         float x3, float y3, float x4, float y4) {
        float denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (Math.abs(denom) < 1e-10) return false; // Lines are parallel
        
        float t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / denom;
        float u = -((x1 - x2) * (y1 - y3) - (y1 - y2) * (x1 - x3)) / denom;
        
        return t >= 0 && t <= 1 && u >= 0 && u <= 1;
    }
    
    /**
     * Ray-rectangle intersection test
     * @param rayX Ray origin X
     * @param rayY Ray origin Y
     * @param rayDirX Ray direction X (normalized)
     * @param rayDirY Ray direction Y (normalized)
     * @param rect Rectangle to test against
     * @return Distance to intersection, or Float.MAX_VALUE if no intersection
     */
    public static float rayRectangleIntersection(float rayX, float rayY, 
                                               float rayDirX, float rayDirY, Rectangle rect) {
        float invDirX = 1.0f / rayDirX;
        float invDirY = 1.0f / rayDirY;
        
        float t1 = (rect.x - rayX) * invDirX;
        float t2 = (rect.x + rect.width - rayX) * invDirX;
        float t3 = (rect.y - rayY) * invDirY;
        float t4 = (rect.y + rect.height - rayY) * invDirY;
        
        float tmin = Math.max(Math.min(t1, t2), Math.min(t3, t4));
        float tmax = Math.min(Math.max(t1, t2), Math.max(t3, t4));
        
        if (tmax < 0 || tmin > tmax) {
            return Float.MAX_VALUE; // No intersection
        }
        
        return tmin < 0 ? tmax : tmin;
    }
}