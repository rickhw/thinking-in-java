package rpg.systems;

import rpg.engine.Entity;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * QuadTree implementation for efficient spatial partitioning of entities.
 * Used to optimize collision detection by reducing the number of collision checks needed.
 */
public class QuadTree {
    private static final int MAX_OBJECTS = 10;
    private static final int MAX_LEVELS = 5;
    
    private int level;
    private List<QuadTreeObject> objects;
    private Rectangle bounds;
    private QuadTree[] nodes;
    
    /**
     * Constructor for QuadTree
     * @param level Current level of the node (0 is root)
     * @param bounds Boundary rectangle of this node
     */
    public QuadTree(int level, Rectangle bounds) {
        this.level = level;
        this.objects = new ArrayList<>();
        this.bounds = new Rectangle(bounds);
        this.nodes = new QuadTree[4];
    }
    
    /**
     * Clears the quadtree
     */
    public void clear() {
        objects.clear();
        
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] != null) {
                nodes[i].clear();
                nodes[i] = null;
            }
        }
    }
    
    /**
     * Splits the node into 4 subnodes
     */
    private void split() {
        int subWidth = bounds.width / 2;
        int subHeight = bounds.height / 2;
        int x = bounds.x;
        int y = bounds.y;
        
        nodes[0] = new QuadTree(level + 1, new Rectangle(x + subWidth, y, subWidth, subHeight));
        nodes[1] = new QuadTree(level + 1, new Rectangle(x, y, subWidth, subHeight));
        nodes[2] = new QuadTree(level + 1, new Rectangle(x, y + subHeight, subWidth, subHeight));
        nodes[3] = new QuadTree(level + 1, new Rectangle(x + subWidth, y + subHeight, subWidth, subHeight));
    }
    
    /**
     * Determine which node the object belongs to
     * @param objectBounds The bounding rectangle of the object
     * @return Index of the node (0-3), or -1 if object doesn't fit completely in any node
     */
    private int getIndex(Rectangle objectBounds) {
        int index = -1;
        double verticalMidpoint = bounds.x + (bounds.width / 2.0);
        double horizontalMidpoint = bounds.y + (bounds.height / 2.0);
        
        // Object can completely fit within the top quadrants
        boolean topQuadrant = (objectBounds.y < horizontalMidpoint && 
                              objectBounds.y + objectBounds.height < horizontalMidpoint);
        
        // Object can completely fit within the bottom quadrants
        boolean bottomQuadrant = (objectBounds.y > horizontalMidpoint);
        
        // Object can completely fit within the left quadrants
        if (objectBounds.x < verticalMidpoint && 
            objectBounds.x + objectBounds.width < verticalMidpoint) {
            if (topQuadrant) {
                index = 1;
            } else if (bottomQuadrant) {
                index = 2;
            }
        }
        // Object can completely fit within the right quadrants
        else if (objectBounds.x > verticalMidpoint) {
            if (topQuadrant) {
                index = 0;
            } else if (bottomQuadrant) {
                index = 3;
            }
        }
        
        return index;
    }
    
    /**
     * Insert an entity into the quadtree
     * @param entity The entity to insert
     * @param entityBounds The bounding rectangle of the entity
     */
    public void insert(Entity entity, Rectangle entityBounds) {
        if (nodes[0] != null) {
            int index = getIndex(entityBounds);
            
            if (index != -1) {
                nodes[index].insert(entity, entityBounds);
                return;
            }
        }
        
        objects.add(new QuadTreeObject(entity, entityBounds));
        
        if (objects.size() > MAX_OBJECTS && level < MAX_LEVELS) {
            if (nodes[0] == null) {
                split();
            }
            
            int i = 0;
            while (i < objects.size()) {
                QuadTreeObject obj = objects.get(i);
                int index = getIndex(obj.bounds);
                if (index != -1) {
                    nodes[index].insert(obj.entity, obj.bounds);
                    objects.remove(i);
                } else {
                    i++;
                }
            }
        }
    }
    
    /**
     * Return all objects that could collide with the given object
     * @param returnObjects List to store the result
     * @param objectBounds Bounding rectangle to check against
     */
    public void retrieve(List<Entity> returnObjects, Rectangle objectBounds) {
        int index = getIndex(objectBounds);
        if (index != -1 && nodes[0] != null) {
            nodes[index].retrieve(returnObjects, objectBounds);
        }
        
        for (QuadTreeObject obj : objects) {
            returnObjects.add(obj.entity);
        }
    }
    
    /**
     * Get all entities in this quadtree
     * @return Set of all entities
     */
    public Set<Entity> getAllEntities() {
        Set<Entity> allEntities = new HashSet<>();
        
        // Add entities from this node
        for (QuadTreeObject obj : objects) {
            allEntities.add(obj.entity);
        }
        
        // Add entities from child nodes
        if (nodes[0] != null) {
            for (QuadTree node : nodes) {
                if (node != null) {
                    allEntities.addAll(node.getAllEntities());
                }
            }
        }
        
        return allEntities;
    }
    
    /**
     * Remove an entity from the quadtree
     * @param entity The entity to remove
     * @param entityBounds The bounding rectangle of the entity
     * @return true if the entity was found and removed
     */
    public boolean remove(Entity entity, Rectangle entityBounds) {
        // Try to remove from appropriate child node first
        if (nodes[0] != null) {
            int index = getIndex(entityBounds);
            if (index != -1) {
                return nodes[index].remove(entity, entityBounds);
            }
        }
        
        // Remove from this node
        return objects.removeIf(obj -> obj.entity.equals(entity));
    }
    
    /**
     * Get the bounds of this quadtree node
     * @return Rectangle representing the bounds
     */
    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }
    
    /**
     * Get the current level of this node
     * @return The level (0 is root)
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * Get the number of objects in this node (not including child nodes)
     * @return Number of objects
     */
    public int getObjectCount() {
        return objects.size();
    }
    
    /**
     * Get total number of objects in this node and all child nodes
     * @return Total number of objects
     */
    public int getTotalObjectCount() {
        int count = objects.size();
        
        if (nodes[0] != null) {
            for (QuadTree node : nodes) {
                if (node != null) {
                    count += node.getTotalObjectCount();
                }
            }
        }
        
        return count;
    }
    
    /**
     * Check if this node has been subdivided
     * @return true if this node has child nodes
     */
    public boolean isSubdivided() {
        return nodes[0] != null;
    }
    
    /**
     * Helper class to store entity and its bounds together
     */
    private static class QuadTreeObject {
        final Entity entity;
        final Rectangle bounds;
        
        QuadTreeObject(Entity entity, Rectangle bounds) {
            this.entity = entity;
            this.bounds = new Rectangle(bounds);
        }
    }
}