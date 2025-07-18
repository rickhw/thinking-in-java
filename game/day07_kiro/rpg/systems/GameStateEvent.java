package rpg.systems;

/**
 * Event fired when game state changes
 */
public class GameStateEvent extends GameEvent {
    private final String previousState;
    private final String newState;
    
    public GameStateEvent(String previousState, String newState) {
        super();
        this.previousState = previousState;
        this.newState = newState;
    }
    
    public String getPreviousState() { return previousState; }
    public String getNewState() { return newState; }
    
    @Override
    public String toString() {
        return String.format("GameStateEvent{previousState='%s', newState='%s'}", 
            previousState, newState);
    }
}