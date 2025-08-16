package rpg.systems.commands;

import rpg.engine.Entity;
import rpg.systems.EventBus;
import rpg.systems.GameStateEvent;

/**
 * Command for handling menu-related actions.
 * Can open/close menus, navigate menu options, and trigger menu actions.
 */
public class MenuCommand extends InputCommand {
    private final MenuAction action;
    private final EventBus eventBus;
    private GameStateEvent previousStateEvent;
    
    /**
     * Enumeration of possible menu actions.
     */
    public enum MenuAction {
        OPEN_MAIN_MENU,
        CLOSE_MENU,
        TOGGLE_PAUSE,
        NAVIGATE_UP,
        NAVIGATE_DOWN,
        NAVIGATE_LEFT,
        NAVIGATE_RIGHT,
        SELECT,
        BACK
    }
    
    /**
     * Create a menu command.
     * @param entity the entity triggering the menu action
     * @param action the menu action to perform
     * @param eventBus the event bus for publishing state changes
     */
    public MenuCommand(Entity entity, MenuAction action, EventBus eventBus) {
        super(entity);
        this.action = action;
        this.eventBus = eventBus;
    }
    
    /**
     * Create a menu command without event bus.
     * @param entity the entity triggering the menu action
     * @param action the menu action to perform
     */
    public MenuCommand(Entity entity, MenuAction action) {
        this(entity, action, null);
    }
    
    @Override
    protected boolean shouldExecute() {
        // Most menu actions should only execute on key press, not hold
        boolean requiresPress = action == MenuAction.OPEN_MAIN_MENU || 
                               action == MenuAction.CLOSE_MENU ||
                               action == MenuAction.TOGGLE_PAUSE ||
                               action == MenuAction.SELECT ||
                               action == MenuAction.BACK;
        
        if (requiresPress && !justPressed) {
            return false;
        }
        
        return super.shouldExecute();
    }
    
    @Override
    protected void executeInput() {
        switch (action) {
            case OPEN_MAIN_MENU:
                openMainMenu();
                break;
            case CLOSE_MENU:
                closeMenu();
                break;
            case TOGGLE_PAUSE:
                togglePause();
                break;
            case NAVIGATE_UP:
                navigateMenu(0, -1);
                break;
            case NAVIGATE_DOWN:
                navigateMenu(0, 1);
                break;
            case NAVIGATE_LEFT:
                navigateMenu(-1, 0);
                break;
            case NAVIGATE_RIGHT:
                navigateMenu(1, 0);
                break;
            case SELECT:
                selectMenuItem();
                break;
            case BACK:
                goBack();
                break;
        }
    }
    
    /**
     * Open the main menu.
     */
    private void openMainMenu() {
        System.out.println("Opening main menu");
        
        if (eventBus != null) {
            // Store previous state for undo
            previousStateEvent = new GameStateEvent("MENU");
            eventBus.publish(previousStateEvent);
        }
    }
    
    /**
     * Close the current menu.
     */
    private void closeMenu() {
        System.out.println("Closing menu");
        
        if (eventBus != null) {
            GameStateEvent closeEvent = new GameStateEvent("PLAYING");
            eventBus.publish(closeEvent);
        }
    }
    
    /**
     * Toggle the pause state.
     */
    private void togglePause() {
        System.out.println("Toggling pause");
        
        if (eventBus != null) {
            // This would need to check current state and toggle appropriately
            GameStateEvent pauseEvent = new GameStateEvent("PAUSED");
            eventBus.publish(pauseEvent);
        }
    }
    
    /**
     * Navigate within a menu.
     * @param deltaX horizontal navigation direction
     * @param deltaY vertical navigation direction
     */
    private void navigateMenu(int deltaX, int deltaY) {
        System.out.println("Navigating menu: deltaX=" + deltaX + ", deltaY=" + deltaY);
        
        // TODO: Implement menu navigation logic
        // This would typically involve:
        // 1. Getting the current menu state
        // 2. Moving the selection cursor
        // 3. Updating the UI
        // 4. Playing navigation sounds
    }
    
    /**
     * Select the current menu item.
     */
    private void selectMenuItem() {
        System.out.println("Selecting menu item");
        
        // TODO: Implement menu selection logic
        // This would typically involve:
        // 1. Getting the currently selected menu item
        // 2. Executing the associated action
        // 3. Playing selection sounds
        // 4. Updating game state if necessary
    }
    
    /**
     * Go back in the menu hierarchy.
     */
    private void goBack() {
        System.out.println("Going back in menu");
        
        // TODO: Implement menu back navigation
        // This would typically involve:
        // 1. Checking if there's a parent menu
        // 2. Returning to the previous menu level
        // 3. Restoring previous selection state
    }
    
    @Override
    public boolean canUndo() {
        // Some menu actions can be undone (like opening a menu)
        return action == MenuAction.OPEN_MAIN_MENU || action == MenuAction.TOGGLE_PAUSE;
    }
    
    @Override
    public void undo() {
        if (!canUndo()) return;
        
        switch (action) {
            case OPEN_MAIN_MENU:
                closeMenu();
                break;
            case TOGGLE_PAUSE:
                togglePause(); // Toggle back
                break;
        }
    }
    
    @Override
    public String getDescription() {
        return String.format("MenuCommand(entity=%d, action=%s, justPressed=%b)", 
            entity != null ? entity.getId() : -1, action, justPressed);
    }
}