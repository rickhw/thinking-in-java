package rpg.systems;

import rpg.components.CollisionComponent;
import rpg.components.TransformComponent;
import rpg.components.MovementComponent;
import rpg.engine.Entity;

import java.awt.Rectangle;
import java.awt.geom.Point2D;

/**
 * Handles collision response calculations and entity separation.
 * Provides various collision response strategies for different types of collisions.
 */
public class CollisionResponse {
    
    /**
     * Collision response types
     */
    public enum ResponseType {
        NONE,           // No response
        STOP,           // Stop movement
        BOUNCE,         // Bounce off
        SLIDE,          // Slide along surface
        PUSH,           // Push other entity
        SEPARATE        // Separate entities
    }
    
    /**
     * Calculate and apply collision response between two entities
     * @param entityA First entity
     * @param entityB Second entity
     * @param responseType Type of response to apply
     */
    public static void applyCollisionResponse(Entity entityA, Entity entityB, ResponseType responseType) {
        CollisionComponent collisionA = entityA.getComponent(CollisionComponent.class);
        CollisionComponent collisionB = entityB.getComponent(CollisionComponent.class);
        TransformComponent transformA = entityA.getComponent(TransformComponent.class);
        TransformComponent transformB = entityB.getComponent(TransformComponent.class);
        MovementComponent movementA = entityA.getComponent(MovementComponent.class);
        MovementComponent movementB = entityB.getComponent(MovementComponent.class);
        
        if (collisionA == null || collisionB == null || transformA == null || transformB == null) {
            return;
        }
        
        switch (responseType) {
            case STOP:
                applyStopResponse(movementA, movementB);
                break;
            case BOUNCE:
                applyBounceResponse(entityA, entityB, collisionA, collisionB, movementA, movementB);
                break;
            case SLIDE:
                applySlideResponse(entityA, entityB, collisionA, collisionB, movementA, movementB);
                break;
            case PUSH:
                applyPushResponse(entityA, entityB, collisionA, collisionB, transformA, transformB, movementA, movementB);
                break;
            case SEPARATE:
                applySeparateResponse(entityA, entityB, collisionA, collisionB, transformA, transformB);
                break;
            case NONE:
            default:
                // No response
                break;
        }
    }
    
    /**
     * Stop movement response - entities stop when they collide
     */
    private static void applyStopResponse(MovementComponent movementA, MovementComponent movementB) {
        if (movementA != null) {
            movementA.velocityX = 0;
            movementA.velocityY = 0;
        }
        if (movementB != null) {
            movementB.velocityX = 0;
            movementB.velocityY = 0;
        }
    }
    
    /**
     * Bounce response - entities bounce off each other
     */
    private static void applyBounceResponse(Entity entityA, Entity entityB,
                                          CollisionComponent collisionA, CollisionComponent collisionB,
                                          MovementComponent movementA, MovementComponent movementB) {
        if (movementA == null || movementB == null) return;
        
        Rectangle boundsA = collisionA.getWorldBounds();
        Rectangle boundsB = collisionB.getWorldBounds();
        
        // Calculate collision normal
        Point2D.Float normal = calculateCollisionNormal(boundsA, boundsB);
        
        // Apply bounce based on masses
        float massA = collisionA.getMass();
        float massB = collisionB.getMass();
        float totalMass = massA + massB;
        
        if (!collisionA.isStatic()) {
            float velocityChange = 2 * massB / totalMass;
            movementA.velocityX = -movementA.velocityX * velocityChange * normal.x;
            movementA.velocityY = -movementA.velocityY * velocityChange * normal.y;
        }
        
        if (!collisionB.isStatic()) {
            float velocityChange = 2 * massA / totalMass;
            movementB.velocityX = -movementB.velocityX * velocityChange * normal.x;
            movementB.velocityY = -movementB.velocityY * velocityChange * normal.y;
        }
    }
    
    /**
     * Slide response - entities slide along collision surface
     */
    private static void applySlideResponse(Entity entityA, Entity entityB,
                                         CollisionComponent collisionA, CollisionComponent collisionB,
                                         MovementComponent movementA, MovementComponent movementB) {
        if (movementA == null) return;
        
        Rectangle boundsA = collisionA.getWorldBounds();
        Rectangle boundsB = collisionB.getWorldBounds();
        
        // Calculate collision normal
        Point2D.Float normal = calculateCollisionNormal(boundsA, boundsB);
        
        // Project velocity onto surface (perpendicular to normal)
        float dotProduct = movementA.velocityX * normal.x + movementA.velocityY * normal.y;
        
        if (!collisionA.isStatic()) {
            movementA.velocityX -= dotProduct * normal.x;
            movementA.velocityY -= dotProduct * normal.y;
        }
    }
    
    /**
     * Push response - one entity pushes another
     */
    private static void applyPushResponse(Entity entityA, Entity entityB,
                                        CollisionComponent collisionA, CollisionComponent collisionB,
                                        TransformComponent transformA, TransformComponent transformB,
                                        MovementComponent movementA, MovementComponent movementB) {
        
        if (!collisionA.canPush() && !collisionB.canPush()) return;
        
        Rectangle boundsA = collisionA.getWorldBounds();
        Rectangle boundsB = collisionB.getWorldBounds();
        
        Point2D.Float mtv = CollisionDetection.calculateMTV(boundsA, boundsB);
        
        // Determine who pushes whom
        if (collisionA.canPush() && collisionB.canBePushed() && !collisionB.isStatic()) {
            transformB.x += mtv.x;
            transformB.y += mtv.y;
            
            if (movementB != null) {
                // Transfer some momentum
                float momentumTransfer = 0.5f;
                movementB.velocityX += movementA != null ? movementA.velocityX * momentumTransfer : 0;
                movementB.velocityY += movementA != null ? movementA.velocityY * momentumTransfer : 0;
            }
        } else if (collisionB.canPush() && collisionA.canBePushed() && !collisionA.isStatic()) {
            transformA.x -= mtv.x;
            transformA.y -= mtv.y;
            
            if (movementA != null) {
                // Transfer some momentum
                float momentumTransfer = 0.5f;
                movementA.velocityX += movementB != null ? movementB.velocityX * momentumTransfer : 0;
                movementA.velocityY += movementB != null ? movementB.velocityY * momentumTransfer : 0;
            }
        }
    }
    
    /**
     * Separate response - entities are separated to prevent overlap
     */
    private static void applySeparateResponse(Entity entityA, Entity entityB,
                                            CollisionComponent collisionA, CollisionComponent collisionB,
                                            TransformComponent transformA, TransformComponent transformB) {
        
        Rectangle boundsA = collisionA.getWorldBounds();
        Rectangle boundsB = collisionB.getWorldBounds();
        
        Point2D.Float mtv = CollisionDetection.calculateMTV(boundsA, boundsB);
        
        if (mtv.x == 0 && mtv.y == 0) return;
        
        // Calculate mass ratios for separation
        float massA = collisionA.getMass();
        float massB = collisionB.getMass();
        float totalMass = massA + massB;
        
        float ratioA = collisionA.isStatic() ? 0 : massB / totalMass;
        float ratioB = collisionB.isStatic() ? 0 : massA / totalMass;
        
        // Apply separation
        if (!collisionA.isStatic()) {
            transformA.x += mtv.x * ratioA;
            transformA.y += mtv.y * ratioA;
        }
        
        if (!collisionB.isStatic()) {
            transformB.x -= mtv.x * ratioB;
            transformB.y -= mtv.y * ratioB;
        }
    }
    
    /**
     * Calculate the collision normal between two rectangles
     * @param rectA First rectangle
     * @param rectB Second rectangle
     * @return Normalized collision normal
     */
    private static Point2D.Float calculateCollisionNormal(Rectangle rectA, Rectangle rectB) {
        float centerAX = rectA.x + rectA.width / 2.0f;
        float centerAY = rectA.y + rectA.height / 2.0f;
        float centerBX = rectB.x + rectB.width / 2.0f;
        float centerBY = rectB.y + rectB.height / 2.0f;
        
        float dx = centerBX - centerAX;
        float dy = centerBY - centerAY;
        
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length == 0) {
            return new Point2D.Float(1, 0); // Default normal
        }
        
        return new Point2D.Float(dx / length, dy / length);
    }
    
    /**
     * Get the appropriate response type based on collision components
     * @param collisionA First collision component
     * @param collisionB Second collision component
     * @return Appropriate response type
     */
    public static ResponseType getResponseType(CollisionComponent collisionA, CollisionComponent collisionB) {
        // Trigger zones don't have physical response
        if (collisionA.isTrigger() || collisionB.isTrigger()) {
            return ResponseType.NONE;
        }
        
        // If either can push, use push response
        if (collisionA.canPush() || collisionB.canPush()) {
            return ResponseType.PUSH;
        }
        
        // If both are solid, separate them
        if (collisionA.isSolid() && collisionB.isSolid()) {
            return ResponseType.SEPARATE;
        }
        
        // Default to stop response
        return ResponseType.STOP;
    }
}