package rpg.components;

import rpg.engine.Component;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Set;

/**
 * Component that holds collision information for an entity.
 * Includes collision bounds, layers, and collision response settings.
 */
public class CollisionComponent extends Component {
    // Collision bounds (relative to entity position)
    private Rectangle bounds;
    
    // Collision layers for selective collision detection
    private int collisionLayer;
    private Set<Integer> collidesWith;
    
    // Collision properties
    private boolean isSolid;
    private boolean isTrigger;
    private boolean isStatic;
    
    // Collision response
    private boolean canPush;
    private boolean canBePushed;
    private float mass;
    
    // Collision state
    private boolean wasColliding;
    private Set<Integer> currentCollisions;
    
    public CollisionComponent() {
        this.bounds = new Rectangle(0, 0, 32, 32); // Default 32x32 bounds
        this.collisionLayer = 0;
        this.collidesWith = new HashSet<>();
        this.isSolid = true;
        this.isTrigger = false;
        this.isStatic = false;
        this.canPush = false;
        this.canBePushed = true;
        this.mass = 1.0f;
        this.wasColliding = false;
        this.currentCollisions = new HashSet<>();
        
        // By default, collide with all layers
        for (int i = 0; i < 32; i++) {
            collidesWith.add(i);
        }
    }
    
    public CollisionComponent(int width, int height) {
        this();
        this.bounds = new Rectangle(0, 0, width, height);
    }
    
    public CollisionComponent(int x, int y, int width, int height) {
        this();
        this.bounds = new Rectangle(x, y, width, height);
    }
    
    // Bounds methods
    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }
    
    public void setBounds(int x, int y, int width, int height) {
        this.bounds.setBounds(x, y, width, height);
    }
    
    public void setBounds(Rectangle bounds) {
        this.bounds = new Rectangle(bounds);
    }
    
    public Rectangle getWorldBounds() {
        TransformComponent transform = getEntity().getComponent(TransformComponent.class);
        if (transform != null) {
            return new Rectangle(
                (int)(transform.x + bounds.x),
                (int)(transform.y + bounds.y),
                bounds.width,
                bounds.height
            );
        }
        return new Rectangle(bounds);
    }
    
    // Layer methods
    public int getCollisionLayer() {
        return collisionLayer;
    }
    
    public void setCollisionLayer(int layer) {
        this.collisionLayer = layer;
    }
    
    public Set<Integer> getCollidesWith() {
        return new HashSet<>(collidesWith);
    }
    
    public void setCollidesWith(Set<Integer> layers) {
        this.collidesWith = new HashSet<>(layers);
    }
    
    public void addCollisionLayer(int layer) {
        this.collidesWith.add(layer);
    }
    
    public void removeCollisionLayer(int layer) {
        this.collidesWith.remove(layer);
    }
    
    public boolean canCollideWith(int layer) {
        return collidesWith.contains(layer);
    }
    
    public boolean canCollideWith(CollisionComponent other) {
        return this.canCollideWith(other.collisionLayer) && 
               other.canCollideWith(this.collisionLayer);
    }
    
    // Collision properties
    public boolean isSolid() {
        return isSolid;
    }
    
    public void setSolid(boolean solid) {
        this.isSolid = solid;
    }
    
    public boolean isTrigger() {
        return isTrigger;
    }
    
    public void setTrigger(boolean trigger) {
        this.isTrigger = trigger;
    }
    
    public boolean isStatic() {
        return isStatic;
    }
    
    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }
    
    // Physics properties
    public boolean canPush() {
        return canPush;
    }
    
    public void setCanPush(boolean canPush) {
        this.canPush = canPush;
    }
    
    public boolean canBePushed() {
        return canBePushed;
    }
    
    public void setCanBePushed(boolean canBePushed) {
        this.canBePushed = canBePushed;
    }
    
    public float getMass() {
        return mass;
    }
    
    public void setMass(float mass) {
        this.mass = Math.max(0.1f, mass); // Minimum mass to avoid division by zero
    }
    
    // Collision state
    public boolean wasColliding() {
        return wasColliding;
    }
    
    public void setWasColliding(boolean wasColliding) {
        this.wasColliding = wasColliding;
    }
    
    public Set<Integer> getCurrentCollisions() {
        return new HashSet<>(currentCollisions);
    }
    
    public void addCurrentCollision(int entityId) {
        currentCollisions.add(entityId);
    }
    
    public void removeCurrentCollision(int entityId) {
        currentCollisions.remove(entityId);
    }
    
    public void clearCurrentCollisions() {
        currentCollisions.clear();
    }
    
    public boolean isCollidingWith(int entityId) {
        return currentCollisions.contains(entityId);
    }
    
    // Collision detection helper
    public boolean intersects(CollisionComponent other) {
        if (other == null || !canCollideWith(other)) {
            return false;
        }
        
        Rectangle thisBounds = getWorldBounds();
        Rectangle otherBounds = other.getWorldBounds();
        
        return thisBounds.intersects(otherBounds);
    }
    
    @Override
    public void update(float deltaTime) {
        // Update collision state
        wasColliding = !currentCollisions.isEmpty();
    }
}