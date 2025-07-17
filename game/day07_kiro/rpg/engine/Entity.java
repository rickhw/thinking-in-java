package rpg.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * Base Entity class for the Entity-Component-System architecture.
 * Entities are containers for components and have a unique ID.
 */
public class Entity {
    private static int nextId = 1;
    
    private final int id;
    private final Map<Class<? extends Component>, Component> components;
    private boolean active;
    
    public Entity() {
        this.id = nextId++;
        this.components = new HashMap<>();
        this.active = true;
    }
    
    /**
     * Get the unique ID of this entity.
     */
    public int getId() {
        return id;
    }
    
    /**
     * Check if this entity is active.
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     * Set the active state of this entity.
     */
    public void setActive(boolean active) {
        this.active = active;
    }
    
    /**
     * Add a component to this entity.
     */
    public <T extends Component> void addComponent(T component) {
        components.put(component.getClass(), component);
        component.setEntity(this);
        component.onAttach();
    }
    
    /**
     * Get a component of the specified type from this entity.
     */
    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponent(Class<T> componentType) {
        return (T) components.get(componentType);
    }
    
    /**
     * Remove a component of the specified type from this entity.
     */
    public <T extends Component> void removeComponent(Class<T> componentType) {
        Component component = components.remove(componentType);
        if (component != null) {
            component.onDetach();
            component.setEntity(null);
        }
    }
    
    /**
     * Check if this entity has a component of the specified type.
     */
    public boolean hasComponent(Class<? extends Component> componentType) {
        return components.containsKey(componentType);
    }
    
    /**
     * Get all components attached to this entity.
     */
    public Map<Class<? extends Component>, Component> getComponents() {
        return new HashMap<>(components);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Entity entity = (Entity) obj;
        return id == entity.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
    
    @Override
    public String toString() {
        return "Entity{id=" + id + ", active=" + active + ", components=" + components.size() + "}";
    }
}