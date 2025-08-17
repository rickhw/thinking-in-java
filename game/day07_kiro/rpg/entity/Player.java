package rpg.entity;

import rpg.Config;
import rpg.GamePanel;
import rpg.KeyHandler;
import rpg.components.*;
import rpg.engine.Entity;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Component-based Player entity that uses the new ECS architecture.
 * Maintains compatibility with the old system while using modern component design.
 */
public class Player {
    // Legacy compatibility fields
    private final GamePanel gp;
    private final KeyHandler keyHandler;
    public final int screenX;
    public final int screenY;
    
    // New ECS entity
    private final Entity entity;
    
    // Component references for easy access
    private TransformComponent transform;
    private RenderComponent render;
    private MovementComponent movement;
    private CollisionComponent collision;
    private PlayerInputComponent input;
    private AnimationComponent animation;
    
    // Legacy compatibility fields
    public Rectangle solidArea;
    public boolean collisionOn = false;
    
    public Player(GamePanel gp, KeyHandler keyHandler) {
        this.gp = gp;
        this.keyHandler = keyHandler;
        
        screenX = Config.SCREEN_WIDTH / 2 - (Config.TILE_SIZE / 2);
        screenY = Config.SCREEN_HEIGHT / 2 - (Config.TILE_SIZE / 2);
        
        // Create new ECS entity
        this.entity = new Entity();
        
        // Initialize components
        initializeComponents();
        
        // Set default values
        setDefaultValues();
        
        // Load player images and setup animations
        setupAnimations();
    }
    
    private void initializeComponents() {
        // Transform component for position
        transform = new TransformComponent();
        entity.addComponent(transform);
        
        // Render component for sprite rendering
        render = new RenderComponent();
        render.setLayer(1); // Player should be above background
        entity.addComponent(render);
        
        // Movement component for physics
        movement = new MovementComponent(5.0f); // Base speed of 5
        entity.addComponent(movement);
        
        // Collision component for collision detection
        collision = new CollisionComponent(8, 16, 32, 32); // Match original solidArea
        collision.setSolid(true);
        collision.setCollisionLayer(1); // Player collision layer
        entity.addComponent(collision);
        
        // Player input component for input handling
        input = new PlayerInputComponent();
        entity.addComponent(input);
        
        // Animation component for sprite animations
        animation = new AnimationComponent();
        entity.addComponent(animation);
        
        // Set up legacy compatibility
        solidArea = new Rectangle(8, 16, 32, 32);
    }
    
    private void setDefaultValues() {
        // Set initial position (matching original)
        transform.setPosition(Config.TILE_SIZE * 23, Config.TILE_SIZE * 21);
        
        // Set initial direction
        input.setDirection(Direction.DOWN);
    }
    
    private void setupAnimations() {
        try {
            // Load player sprites
            BufferedImage up1 = ImageIO.read(getClass().getResourceAsStream("/rpg/assets/player/boy_up_1.png"));
            BufferedImage up2 = ImageIO.read(getClass().getResourceAsStream("/rpg/assets/player/boy_up_2.png"));
            BufferedImage down1 = ImageIO.read(getClass().getResourceAsStream("/rpg/assets/player/boy_down_1.png"));
            BufferedImage down2 = ImageIO.read(getClass().getResourceAsStream("/rpg/assets/player/boy_down_2.png"));
            BufferedImage left1 = ImageIO.read(getClass().getResourceAsStream("/rpg/assets/player/boy_left_1.png"));
            BufferedImage left2 = ImageIO.read(getClass().getResourceAsStream("/rpg/assets/player/boy_left_2.png"));
            BufferedImage right1 = ImageIO.read(getClass().getResourceAsStream("/rpg/assets/player/boy_right_1.png"));
            BufferedImage right2 = ImageIO.read(getClass().getResourceAsStream("/rpg/assets/player/boy_right_2.png"));
            
            // Create walking animation (0.1 seconds per frame, looping)
            AnimationComponent.DirectionalAnimation walkAnimation = 
                new AnimationComponent.DirectionalAnimation(0.1f, true);
            
            walkAnimation.setFrames(Direction.UP, new BufferedImage[]{up1, up2});
            walkAnimation.setFrames(Direction.DOWN, new BufferedImage[]{down1, down2});
            walkAnimation.setFrames(Direction.LEFT, new BufferedImage[]{left1, left2});
            walkAnimation.setFrames(Direction.RIGHT, new BufferedImage[]{right1, right2});
            
            animation.addAnimation("walk", walkAnimation);
            
            // Create idle animation (just first frame of each direction)
            AnimationComponent.DirectionalAnimation idleAnimation = 
                new AnimationComponent.DirectionalAnimation(1.0f, true);
            
            idleAnimation.setFrames(Direction.UP, new BufferedImage[]{up1});
            idleAnimation.setFrames(Direction.DOWN, new BufferedImage[]{down1});
            idleAnimation.setFrames(Direction.LEFT, new BufferedImage[]{left1});
            idleAnimation.setFrames(Direction.RIGHT, new BufferedImage[]{right1});
            
            animation.addAnimation("idle", idleAnimation);
            
            // Start with idle animation facing down
            animation.playAnimation("idle", Direction.DOWN);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Legacy compatibility method
    public void update() {
        // Process input from KeyHandler (legacy compatibility)
        processLegacyInput();
        
        // Update components
        float deltaTime = 1.0f / 60.0f; // Assume 60 FPS for now
        
        input.update(deltaTime);
        movement.update(deltaTime);
        animation.update(deltaTime);
        collision.update(deltaTime);
        render.update(deltaTime);
        
        // Handle movement and collision (legacy style)
        handleMovementAndCollision();
        
        // Update legacy fields for compatibility
        updateLegacyFields();
    }
    
    private void processLegacyInput() {
        // Convert KeyHandler input to component input
        if (keyHandler.upPressed) {
            input.processKeyPressed(input.getKeyBinding("move_up"));
        } else {
            input.processKeyReleased(input.getKeyBinding("move_up"));
        }
        
        if (keyHandler.downPressed) {
            input.processKeyPressed(input.getKeyBinding("move_down"));
        } else {
            input.processKeyReleased(input.getKeyBinding("move_down"));
        }
        
        if (keyHandler.leftPressed) {
            input.processKeyPressed(input.getKeyBinding("move_left"));
        } else {
            input.processKeyReleased(input.getKeyBinding("move_left"));
        }
        
        if (keyHandler.rightPressed) {
            input.processKeyPressed(input.getKeyBinding("move_right"));
        } else {
            input.processKeyReleased(input.getKeyBinding("move_right"));
        }
    }
    
    private void handleMovementAndCollision() {
        if (input.isMoving()) {
            // Get movement vector
            float[] moveVector = input.getMovementVector();
            float speed = 5.0f; // Base speed
            
            // Calculate new position
            float newX = transform.x + moveVector[0] * speed;
            float newY = transform.y + moveVector[1] * speed;
            
            // Store old position for collision checking
            float oldX = transform.x;
            float oldY = transform.y;
            
            // Set new position temporarily
            transform.setPosition(newX, newY);
            
            // Check tile collision (legacy compatibility)
            collisionOn = false;
            if (gp != null && gp.collisionChecker != null) {
                // Create a temporary legacy entity for collision checking
                LegacyEntityAdapter adapter = new LegacyEntityAdapter(this);
                gp.collisionChecker.checkTile(adapter);
                collisionOn = adapter.collisionOn;
            }
            
            // If collision detected, revert to old position
            if (collisionOn) {
                transform.setPosition(oldX, oldY);
            }
            
            // Update animation to walking
            animation.playAnimation("walk", input.getDirection());
        } else {
            // Update animation to idle
            animation.playAnimation("idle", input.getLastDirection());
        }
    }
    
    private void updateLegacyFields() {
        // Update legacy solidArea position
        solidArea.x = (int)transform.x + 8;
        solidArea.y = (int)transform.y + 16;
    }
    
    // Legacy compatibility method
    public void draw(Graphics2D g2) {
        BufferedImage image = animation.getCurrentFrameImage();
        if (image != null) {
            g2.drawImage(image, screenX, screenY, Config.TILE_SIZE, Config.TILE_SIZE, null);
        }
    }
    
    // Getters for ECS components (for systems that need direct access)
    public Entity getEntity() {
        return entity;
    }
    
    public TransformComponent getTransform() {
        return transform;
    }
    
    public RenderComponent getRender() {
        return render;
    }
    
    public MovementComponent getMovement() {
        return movement;
    }
    
    public CollisionComponent getCollision() {
        return collision;
    }
    
    public PlayerInputComponent getInput() {
        return input;
    }
    
    public AnimationComponent getAnimation() {
        return animation;
    }
    
    // Legacy compatibility getters/setters
    public int getWorldX() {
        return (int)transform.x;
    }
    
    public int getWorldY() {
        return (int)transform.y;
    }
    
    public void setWorldX(int worldX) {
        transform.x = worldX;
    }
    
    public void setWorldY(int worldY) {
        transform.y = worldY;
    }
    
    public Direction getDirection() {
        return input.getDirection();
    }
    
    public void setDirection(Direction direction) {
        input.setDirection(direction);
    }
    
    public int getSpeed() {
        return (int)movement.maxSpeed;
    }
    
    public void setSpeed(int speed) {
        movement.maxSpeed = speed;
    }
    
    /**
     * Legacy adapter class to maintain compatibility with old collision system
     */
    private static class LegacyEntityAdapter extends rpg.entity.Entity {
        private final Player player;
        
        public LegacyEntityAdapter(Player player) {
            this.player = player;
            this.worldX = (int)player.transform.x;
            this.worldY = (int)player.transform.y;
            this.solidArea = new Rectangle(player.solidArea);
            this.direction = player.input.getDirection();
        }
        
        @Override
        public void update() {
            // Not used
        }
        
        @Override
        public void draw(Graphics2D g2) {
            // Not used
        }
    }
}