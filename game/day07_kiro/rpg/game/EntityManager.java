package rpg.game;

import rpg.engine.Component;
import rpg.engine.Entity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
}