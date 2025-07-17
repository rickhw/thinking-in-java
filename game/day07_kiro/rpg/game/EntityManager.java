package rpg.game;

import rpg.engine.Component;
import rpg.engine.Entity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.io.*;

/**
 * Manages all entities in the game world.
 * Provides methods for creating, destroying, and querying entities.
 */
public class EntityManager {
    private final Map<Integer, Entity> entities;
    private final List<Entity> entitiesToAdd;
    private final List<Entity> entitiesToRemove;
    
    public EntityManager() {
        this.entities = new ConcurrentHashMap<>();
        this.entitiesToAdd = new ArrayList<>();
        this.entitiesToRemove = new ArrayList<>();
    }
    
    /**
     * Create a new entity and add it to the manager.
     */
    public Entity createEntity() {
        Entity entity = new Entity();
        entitiesToAdd.add(entity);
        return entity;
    }
    
    /**
     * Remove an entity from the manager.
     */
    public void removeEntity(Entity entity) {
        if (entity != null) {
            entitiesToRemove.add(entity);
        }
    }
    
    /**
     * Remove an entity by ID.
     */
    public void removeEntity(int entityId) {
        Entity entity = entities.get(entityId);
        if (entity != null) {
            removeEntity(entity);
        }
    }
    
    /**
     * Get an entity by ID.
     */
    public Entity getEntity(int entityId) {
        return entities.get(entityId);
    }
    
    /**
     * Get all entities.
     */
    public Collection<Entity> getAllEntities() {
        return new ArrayList<>(entities.values());
    }
    
    /**
     * Get all active entities.
     */
    public List<Entity> getActiveEntities() {
        return entities.values().stream()
                .filter(Entity::isActive)
                .collect(Collectors.toList());
    }
    
    /**
     * Find entities that have a specific component type.
     */
    public <T extends Component> List<Entity> getEntitiesWithComponent(Class<T> componentType) {
        return entities.values().stream()
                .filter(entity -> entity.isActive() && entity.hasComponent(componentType))
                .collect(Collectors.toList());
    }
    
    /**
     * Find entities that have all of the specified component types.
     */
    @SafeVarargs
    public final List<Entity> getEntitiesWithComponents(Class<? extends Component>... componentTypes) {
        return entities.values().stream()
                .filter(entity -> {
                    if (!entity.isActive()) return false;
                    for (Class<? extends Component> componentType : componentTypes) {
                        if (!entity.hasComponent(componentType)) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Update the entity manager. This processes pending additions and removals.
     * Should be called once per frame.
     */
    public void update() {
        // Add pending entities
        for (Entity entity : entitiesToAdd) {
            entities.put(entity.getId(), entity);
        }
        entitiesToAdd.clear();
        
        // Remove pending entities
        for (Entity entity : entitiesToRemove) {
            entities.remove(entity.getId());
            // Clean up components
            for (Component component : entity.getComponents().values()) {
                component.onDetach();
            }
        }
        entitiesToRemove.clear();
    }
    
    /**
     * Get the number of entities managed by this manager.
     */
    public int getEntityCount() {
        return entities.size();
    }
    
    /**
     * Clear all entities.
     */
    public void clear() {
        for (Entity entity : entities.values()) {
            for (Component component : entity.getComponents().values()) {
                component.onDetach();
            }
        }
        entities.clear();
        entitiesToAdd.clear();
        entitiesToRemove.clear();
    }
    
    /**
     * Serialize all entities to a file for save functionality.
     * Note: This is a basic implementation. Components must be serializable.
     */
    public void saveToFile(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            // Save entity count
            oos.writeInt(entities.size());
            
            // Save each entity
            for (Entity entity : entities.values()) {
                saveEntity(oos, entity);
            }
        }
    }
    
    /**
     * Deserialize entities from a file for load functionality.
     * Note: This is a basic implementation. Components must be serializable.
     */
    public void loadFromFile(String filename) throws IOException, ClassNotFoundException {
        clear(); // Clear existing entities
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            int entityCount = ois.readInt();
            
            for (int i = 0; i < entityCount; i++) {
                Entity entity = loadEntity(ois);
                if (entity != null) {
                    entities.put(entity.getId(), entity);
                }
            }
        }
    }
    
    private void saveEntity(ObjectOutputStream oos, Entity entity) throws IOException {
        // Save entity ID and active state
        oos.writeInt(entity.getId());
        oos.writeBoolean(entity.isActive());
        
        // Save components
        Map<Class<? extends Component>, Component> components = entity.getComponents();
        oos.writeInt(components.size());
        
        for (Map.Entry<Class<? extends Component>, Component> entry : components.entrySet()) {
            oos.writeObject(entry.getKey().getName());
            oos.writeObject(entry.getValue());
        }
    }
    
    private Entity loadEntity(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        try {
            // Read entity ID (for future use if needed)
            ois.readInt();
            boolean isActive = ois.readBoolean();
            
            // Create entity
            Entity entity = new Entity();
            entity.setActive(isActive);
            
            // Load components
            int componentCount = ois.readInt();
            for (int i = 0; i < componentCount; i++) {
                // Read component class name (for future use if needed)
                ois.readObject();
                Component component = (Component) ois.readObject();
                
                if (component != null) {
                    entity.addComponent(component);
                }
            }
            
            return entity;
        } catch (Exception e) {
            System.err.println("Error loading entity: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Create a snapshot of the current entity state for debugging or rollback.
     */
    public Map<Integer, Entity> createSnapshot() {
        Map<Integer, Entity> snapshot = new HashMap<>();
        for (Map.Entry<Integer, Entity> entry : entities.entrySet()) {
            // Note: This creates a shallow copy. For deep copy, would need to clone components
            snapshot.put(entry.getKey(), entry.getValue());
        }
        return snapshot;
    }
    
    /**
     * Update all components of all entities.
     * This should be called from the main game loop.
     */
    public void updateComponents(float deltaTime) {
        for (Entity entity : entities.values()) {
            if (entity.isActive()) {
                for (Component component : entity.getComponents().values()) {
                    component.update(deltaTime);
                }
            }
        }
    }
}