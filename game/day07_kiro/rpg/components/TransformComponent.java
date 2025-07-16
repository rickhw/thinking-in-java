package rpg.components;

import rpg.engine.Component;

/**
 * Component that holds position, rotation, and scale information for an entity.
 */
public class TransformComponent extends Component {
    public float x;
    public float y;
    public float rotation;
    public float scaleX;
    public float scaleY;
    
    public TransformComponent() {
        this(0, 0);
    }
    
    public TransformComponent(float x, float y) {
        this.x = x;
        this.y = y;
        this.rotation = 0;
        this.scaleX = 1.0f;
        this.scaleY = 1.0f;
    }
    
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public void translate(float dx, float dy) {
        this.x += dx;
        this.y += dy;
    }
    
    public void setScale(float scale) {
        this.scaleX = scale;
        this.scaleY = scale;
    }
    
    public void setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }
}