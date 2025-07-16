package rpg.components;

import rpg.engine.Component;

/**
 * Component that holds movement information for an entity.
 * Includes velocity, acceleration, and movement constraints.
 */
public class MovementComponent extends Component {
    // Velocity
    public float velocityX;
    public float velocityY;
    
    // Acceleration
    public float accelerationX;
    public float accelerationY;
    
    // Movement constraints
    public float maxSpeed;
    public float friction;
    public boolean canMove;
    
    // Movement bounds (optional)
    public float minX;
    public float minY;
    public float maxX;
    public float maxY;
    public boolean hasBounds;
    
    public MovementComponent() {
        this.velocityX = 0;
        this.velocityY = 0;
        this.accelerationX = 0;
        this.accelerationY = 0;
        this.maxSpeed = Float.MAX_VALUE;
        this.friction = 0;
        this.canMove = true;
        this.hasBounds = false;
    }
    
    public MovementComponent(float maxSpeed) {
        this();
        this.maxSpeed = maxSpeed;
    }
    
    public MovementComponent(float maxSpeed, float friction) {
        this(maxSpeed);
        this.friction = friction;
    }
    
    // Velocity methods
    public void setVelocity(float x, float y) {
        this.velocityX = x;
        this.velocityY = y;
    }
    
    public void addVelocity(float x, float y) {
        this.velocityX += x;
        this.velocityY += y;
    }
    
    public float getSpeed() {
        return (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY);
    }
    
    public void limitSpeed() {
        float speed = getSpeed();
        if (speed > maxSpeed) {
            float ratio = maxSpeed / speed;
            velocityX *= ratio;
            velocityY *= ratio;
        }
    }
    
    // Acceleration methods
    public void setAcceleration(float x, float y) {
        this.accelerationX = x;
        this.accelerationY = y;
    }
    
    public void addAcceleration(float x, float y) {
        this.accelerationX += x;
        this.accelerationY += y;
    }
    
    // Movement bounds
    public void setBounds(float minX, float minY, float maxX, float maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.hasBounds = true;
    }
    
    public void removeBounds() {
        this.hasBounds = false;
    }
    
    public boolean isWithinBounds(float x, float y) {
        if (!hasBounds) return true;
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }
    
    public void clampToBounds(TransformComponent transform) {
        if (!hasBounds || transform == null) return;
        
        if (transform.x < minX) transform.x = minX;
        if (transform.x > maxX) transform.x = maxX;
        if (transform.y < minY) transform.y = minY;
        if (transform.y > maxY) transform.y = maxY;
    }
    
    // Apply friction
    public void applyFriction(float deltaTime) {
        if (friction > 0) {
            float frictionForce = friction * deltaTime;
            
            // Apply friction to velocity
            if (velocityX > 0) {
                velocityX = Math.max(0, velocityX - frictionForce);
            } else if (velocityX < 0) {
                velocityX = Math.min(0, velocityX + frictionForce);
            }
            
            if (velocityY > 0) {
                velocityY = Math.max(0, velocityY - frictionForce);
            } else if (velocityY < 0) {
                velocityY = Math.min(0, velocityY + frictionForce);
            }
        }
    }
    
    @Override
    public void update(float deltaTime) {
        if (!canMove) return;
        
        // Apply acceleration to velocity
        velocityX += accelerationX * deltaTime;
        velocityY += accelerationY * deltaTime;
        
        // Limit speed
        limitSpeed();
        
        // Apply friction
        applyFriction(deltaTime);
        
        // Reset acceleration (forces need to be applied each frame)
        accelerationX = 0;
        accelerationY = 0;
    }
}