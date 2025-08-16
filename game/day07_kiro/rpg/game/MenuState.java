package rpg.game;

import rpg.systems.EventBus;
import rpg.utils.GameLogger;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Main menu state for game navigation and options.
 */
public class MenuState extends AbstractGameState {
    
    private List<MenuItem> menuItems;
    private int selectedIndex;
    private Font titleFont;
    private Font menuFont;
    private Color backgroundColor;
    private Color textColor;
    private Color selectedColor;
    private String title;
    
    public MenuState(EventBus eventBus) {
        super("MENU", eventBus);
    }
    
    @Override
    protected void initialize() {
        GameLogger.info("Initializing menu state");
        
        // Initialize menu visuals
        this.titleFont = new Font("Arial", Font.BOLD, 36);
        this.menuFont = new Font("Arial", Font.PLAIN, 24);
        this.backgroundColor = new Color(20, 20, 40);
        this.textColor = Color.WHITE;
        this.selectedColor = Color.YELLOW;
        this.title = "RPG Game";
        this.selectedIndex = 0;
        
        // Initialize menu items
        this.menuItems = new ArrayList<>();
        menuItems.add(new MenuItem("Start Game", this::startGame));
        menuItems.add(new MenuItem("Load Game", this::loadGame));
        menuItems.add(new MenuItem("Options", this::showOptions));
        menuItems.add(new MenuItem("Exit", this::exitGame));
    }
    
    @Override
    protected void onEnter() {
        GameLogger.info("Entered main menu");
        selectedIndex = 0; // Reset selection
    }
    
    @Override
    protected void onExit() {
        GameLogger.info("Exited main menu");
    }
    
    @Override
    protected void onUpdate(float deltaTime) {
        // Menu doesn't need complex updates
        // Could add animations or effects here
    }
    
    @Override
    protected void onRender(Graphics2D g2) {
        // Get screen dimensions
        Rectangle bounds = g2.getClipBounds();
        if (bounds == null) {
            bounds = new Rectangle(0, 0, 800, 600); // Default size
        }
        
        // Draw background
        g2.setColor(backgroundColor);
        g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        
        // Draw title
        g2.setColor(textColor);
        g2.setFont(titleFont);
        
        FontMetrics titleFm = g2.getFontMetrics();
        int titleWidth = titleFm.stringWidth(title);
        int titleX = bounds.x + (bounds.width - titleWidth) / 2;
        int titleY = bounds.y + 100;
        
        g2.drawString(title, titleX, titleY);
        
        // Draw menu items
        g2.setFont(menuFont);
        FontMetrics menuFm = g2.getFontMetrics();
        
        int startY = titleY + 100;
        int itemSpacing = 50;
        
        for (int i = 0; i < menuItems.size(); i++) {
            MenuItem item = menuItems.get(i);
            
            // Set color based on selection
            if (i == selectedIndex) {
                g2.setColor(selectedColor);
            } else {
                g2.setColor(textColor);
            }
            
            // Calculate position
            int itemWidth = menuFm.stringWidth(item.getText());
            int itemX = bounds.x + (bounds.width - itemWidth) / 2;
            int itemY = startY + (i * itemSpacing);
            
            // Draw selection indicator
            if (i == selectedIndex) {
                g2.drawString("> " + item.getText() + " <", itemX - 30, itemY);
            } else {
                g2.drawString(item.getText(), itemX, itemY);
            }
        }
    }
    
    @Override
    protected void onHandleInput(InputEvent inputEvent) {
        if (inputEvent.getType() == InputEvent.Type.KEY_PRESSED) {
            int keyCode = inputEvent.getKeyCode();
            
            switch (keyCode) {
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                    navigateUp();
                    break;
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_S:
                    navigateDown();
                    break;
                case KeyEvent.VK_ENTER:
                case KeyEvent.VK_SPACE:
                    selectCurrentItem();
                    break;
                case KeyEvent.VK_ESCAPE:
                    // Could go back to previous menu or exit
                    GameLogger.info("Menu escape pressed");
                    break;
            }
        }
    }
    
    private void navigateUp() {
        selectedIndex = (selectedIndex - 1 + menuItems.size()) % menuItems.size();
        GameLogger.debug("Menu selection: " + selectedIndex);
    }
    
    private void navigateDown() {
        selectedIndex = (selectedIndex + 1) % menuItems.size();
        GameLogger.debug("Menu selection: " + selectedIndex);
    }
    
    private void selectCurrentItem() {
        if (selectedIndex >= 0 && selectedIndex < menuItems.size()) {
            MenuItem item = menuItems.get(selectedIndex);
            GameLogger.info("Selected menu item: " + item.getText());
            item.execute();
        }
    }
    
    // Menu action methods
    private void startGame() {
        GameLogger.info("Starting new game");
        // This would typically trigger a state change to PlayingState
    }
    
    private void loadGame() {
        GameLogger.info("Loading game");
        // This would show a load game dialog or state
    }
    
    private void showOptions() {
        GameLogger.info("Showing options");
        // This would show an options menu state
    }
    
    private void exitGame() {
        GameLogger.info("Exiting game");
        // This would trigger application shutdown
        System.exit(0);
    }
    
    @Override
    public boolean pausesUnderlyingStates() {
        return true; // Menu pauses any underlying game state
    }
    
    @Override
    public boolean rendersOverUnderlyingStates() {
        return false; // Menu renders its own full screen
    }
    
    /**
     * Represents a menu item with text and action.
     */
    private static class MenuItem {
        private final String text;
        private final Runnable action;
        
        public MenuItem(String text, Runnable action) {
            this.text = text;
            this.action = action;
        }
        
        public String getText() {
            return text;
        }
        
        public void execute() {
            if (action != null) {
                action.run();
            }
        }
    }
}