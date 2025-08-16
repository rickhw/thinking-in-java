import rpg.systems.*;
import rpg.components.*;
import rpg.engine.Entity;

import java.awt.Rectangle;
import java.util.List;
import java.util.ArrayList;

/**
 * Test class for the enhanced collision detection system
 */
public class TestCollisionSystem {
    
    public static void main(String[] args) {
        System.out.println("Testing Enhanced Collision Detection System...");
        
        // Test QuadTree
        testQuadTree();
        
        // Test Collision Detection algorithms
        testCollisionDetection();
        
        // Test Collision Manager
        testCollisionManager();
        
        System.out.println("All collision system tests completed!");
    }
    
    private static void testQuadTree() {
        System.out.println("\n=== Testing QuadTree ===");
        
        Rectangle worldBounds = new Rectangle(0, 0, 800, 600);
        QuadTree quadTree = new QuadTree(0, worldBounds);
        
        // Create test entities
        Entity entity1 = createTestEntity(1, 100, 100, 32, 32);
        Entity entity2 = createTestEntity(2, 150, 150, 32, 32);
        Entity entity3 = createTestEntity(3, 500, 400, 32, 32);
        
        // Insert entities
        quadTree.insert(entity1, new Rectangle(100, 100, 32, 32));
        quadTree.insert(entity2, new Rectangle(150, 150, 32, 32));
        quadTree.insert(entity3, new Rectangle(500, 400, 32, 32));
        
        System.out.println("Inserted 3 entities into QuadTree");
        System.out.println("Total entities in QuadTree: " + quadTree.getTotalObjectCount());
        
        // Test retrieval
        List<Entity> nearbyEntities = new ArrayList<>();
        quadTree.retrieve(nearbyEntities, new Rectangle(90, 90, 100, 100));
        
        System.out.println("Entities near (90,90,100,100): " + nearbyEntities.size());
        
        // Test clear
        quadTree.clear();
        System.out.println("After clear, total entities: " + quadTree.getTotalObjectCount());
        
        System.out.println("QuadTree test completed ✓");
    }
    
    private static void testCollisionDetection() {
        System.out.println("\n=== Testing Collision Detection Algorithms ===");
        
        // Test AABB collision
        Rectangle rect1 = new Rectangle(10, 10, 50, 50);
        Rectangle rect2 = new Rectangle(40, 40, 50, 50);
        Rectangle rect3 = new Rectangle(100, 100, 50, 50);
        
        boolean collision1 = CollisionDetection.aabbCollision(rect1, rect2);
        boolean collision2 = CollisionDetection.aabbCollision(rect1, rect3);
        
        System.out.println("AABB collision test 1 (should be true): " + collision1);
        System.out.println("AABB collision test 2 (should be false): " + collision2);
        
        // Test circle collision
        boolean circleCollision1 = CollisionDetection.circleCollision(0, 0, 10, 5, 5, 10);
        boolean circleCollision2 = CollisionDetection.circleCollision(0, 0, 10, 50, 50, 10);
        
        System.out.println("Circle collision test 1 (should be true): " + circleCollision1);
        System.out.println("Circle collision test 2 (should be false): " + circleCollision2);
        
        // Test point in rectangle
        boolean pointInRect1 = CollisionDetection.pointInRectangle(25, 25, rect1);
        boolean pointInRect2 = CollisionDetection.pointInRectangle(200, 200, rect1);
        
        System.out.println("Point in rectangle test 1 (should be true): " + pointInRect1);
        System.out.println("Point in rectangle test 2 (should be false): " + pointInRect2);
        
        System.out.println("Collision Detection algorithms test completed ✓");
    }
    
    private static void testCollisionManager() {
        System.out.println("\n=== Testing Collision Manager ===");
        
        Rectangle worldBounds = new Rectangle(0, 0, 800, 600);
        CollisionManager collisionManager = new CollisionManager(worldBounds);
        
        // Create test entities with collision components
        List<Entity> entities = new ArrayList<>();
        
        Entity player = createTestEntity(1, 100, 100, 32, 32);
        CollisionComponent playerCollision = new CollisionComponent(32, 32);
        playerCollision.setCollisionLayer(CollisionLayer.PLAYER);
        player.addComponent(playerCollision);
        entities.add(player);
        
        Entity enemy = createTestEntity(2, 120, 120, 32, 32);
        CollisionComponent enemyCollision = new CollisionComponent(32, 32);
        enemyCollision.setCollisionLayer(CollisionLayer.ENEMY);
        enemy.addComponent(enemyCollision);
        entities.add(enemy);
        
        Entity wall = createTestEntity(3, 200, 200, 64, 64);
        CollisionComponent wallCollision = new CollisionComponent(64, 64);
        wallCollision.setCollisionLayer(CollisionLayer.WALL);
        wallCollision.setStatic(true);
        wall.addComponent(wallCollision);
        entities.add(wall);
        
        // Update collision manager
        collisionManager.update(entities, 0.016f);
        
        CollisionManager.CollisionStats stats = collisionManager.getStats();
        System.out.println("Collision Manager Stats: " + stats);
        
        // Test layer collision rules
        boolean playerEnemyCollision = CollisionLayer.shouldCollide(CollisionLayer.PLAYER, CollisionLayer.ENEMY);
        boolean playerWallCollision = CollisionLayer.shouldCollide(CollisionLayer.PLAYER, CollisionLayer.WALL);
        
        System.out.println("Player-Enemy collision rule (should be true): " + playerEnemyCollision);
        System.out.println("Player-Wall collision rule (should be true): " + playerWallCollision);
        
        System.out.println("Collision Manager test completed ✓");
    }
    
    private static Entity createTestEntity(int id, float x, float y, int width, int height) {
        Entity entity = new Entity();
        
        TransformComponent transform = new TransformComponent();
        transform.x = x;
        transform.y = y;
        entity.addComponent(transform);
        
        return entity;
    }
}