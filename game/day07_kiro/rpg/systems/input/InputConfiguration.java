package rpg.systems.input;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.*;

/**
 * Manages input configuration including key bindings and input device settings.
 * Supports loading and saving configurations from/to files.
 */
public class InputConfiguration {
    private final Map<String, Integer> keyBindings;
    private final Map<String, Integer> mouseBindings;
    private final Map<String, Object> inputSettings;
    
    // Default key bindings
    private static final Map<String, Integer> DEFAULT_KEY_BINDINGS = new HashMap<>();
    private static final Map<String, Integer> DEFAULT_MOUSE_BINDINGS = new HashMap<>();
    
    static {
        // Initialize default key bindings
        DEFAULT_KEY_BINDINGS.put("move_up", KeyEvent.VK_W);
        DEFAULT_KEY_BINDINGS.put("move_down", KeyEvent.VK_S);
        DEFAULT_KEY_BINDINGS.put("move_left", KeyEvent.VK_A);
        DEFAULT_KEY_BINDINGS.put("move_right", KeyEvent.VK_D);
        
        // Alternative arrow key bindings
        DEFAULT_KEY_BINDINGS.put("move_up_alt", KeyEvent.VK_UP);
        DEFAULT_KEY_BINDINGS.put("move_down_alt", KeyEvent.VK_DOWN);
        DEFAULT_KEY_BINDINGS.put("move_left_alt", KeyEvent.VK_LEFT);
        DEFAULT_KEY_BINDINGS.put("move_right_alt", KeyEvent.VK_RIGHT);
        
        // Action bindings
        DEFAULT_KEY_BINDINGS.put("interact", KeyEvent.VK_E);
        DEFAULT_KEY_BINDINGS.put("attack", KeyEvent.VK_SPACE);
        DEFAULT_KEY_BINDINGS.put("run", KeyEvent.VK_SHIFT);
        DEFAULT_KEY_BINDINGS.put("menu", KeyEvent.VK_ESCAPE);
        DEFAULT_KEY_BINDINGS.put("pause", KeyEvent.VK_P);
        DEFAULT_KEY_BINDINGS.put("inventory", KeyEvent.VK_I);
        DEFAULT_KEY_BINDINGS.put("map", KeyEvent.VK_M);
        
        // Initialize default mouse bindings
        DEFAULT_MOUSE_BINDINGS.put("attack_alt", MouseEvent.BUTTON1);
        DEFAULT_MOUSE_BINDINGS.put("interact_alt", MouseEvent.BUTTON3);
    }
    
    /**
     * Create a new input configuration with default bindings.
     */
    public InputConfiguration() {
        this.keyBindings = new HashMap<>();
        this.mouseBindings = new HashMap<>();
        this.inputSettings = new HashMap<>();
        
        resetToDefaults();
    }
    
    /**
     * Reset all bindings to default values.
     */
    public void resetToDefaults() {
        keyBindings.clear();
        mouseBindings.clear();
        inputSettings.clear();
        
        keyBindings.putAll(DEFAULT_KEY_BINDINGS);
        mouseBindings.putAll(DEFAULT_MOUSE_BINDINGS);
        
        // Set default input settings
        inputSettings.put("mouse_sensitivity", 1.0f);
        inputSettings.put("key_repeat_delay", 250);
        inputSettings.put("key_repeat_rate", 50);
        inputSettings.put("double_click_time", 300);
    }
    
    /**
     * Bind a key to an action.
     * @param action the action name
     * @param keyCode the key code
     */
    public void bindKey(String action, int keyCode) {
        // Remove any existing binding for this action
        keyBindings.entrySet().removeIf(entry -> entry.getValue().equals(keyCode));
        
        // Add the new binding
        keyBindings.put(action, keyCode);
    }
    
    /**
     * Bind a mouse button to an action.
     * @param action the action name
     * @param mouseButton the mouse button code
     */
    public void bindMouseButton(String action, int mouseButton) {
        // Remove any existing binding for this action
        mouseBindings.entrySet().removeIf(entry -> entry.getValue().equals(mouseButton));
        
        // Add the new binding
        mouseBindings.put(action, mouseButton);
    }
    
    /**
     * Unbind an action from all inputs.
     * @param action the action name
     */
    public void unbindAction(String action) {
        keyBindings.remove(action);
        mouseBindings.remove(action);
    }
    
    /**
     * Get the key binding for an action.
     * @param action the action name
     * @return the key code, or null if not bound
     */
    public Integer getKeyBinding(String action) {
        return keyBindings.get(action);
    }
    
    /**
     * Get the mouse binding for an action.
     * @param action the action name
     * @return the mouse button code, or null if not bound
     */
    public Integer getMouseBinding(String action) {
        return mouseBindings.get(action);
    }
    
    /**
     * Get all key bindings.
     * @return a copy of the key bindings map
     */
    public Map<String, Integer> getAllKeyBindings() {
        return new HashMap<>(keyBindings);
    }
    
    /**
     * Get all mouse bindings.
     * @return a copy of the mouse bindings map
     */
    public Map<String, Integer> getAllMouseBindings() {
        return new HashMap<>(mouseBindings);
    }
    
    /**
     * Get the action bound to a specific key.
     * @param keyCode the key code
     * @return the action name, or null if no action is bound
     */
    public String getActionForKey(int keyCode) {
        for (Map.Entry<String, Integer> entry : keyBindings.entrySet()) {
            if (entry.getValue().equals(keyCode)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * Get the action bound to a specific mouse button.
     * @param mouseButton the mouse button code
     * @return the action name, or null if no action is bound
     */
    public String getActionForMouseButton(int mouseButton) {
        for (Map.Entry<String, Integer> entry : mouseBindings.entrySet()) {
            if (entry.getValue().equals(mouseButton)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * Check if a key is bound to any action.
     * @param keyCode the key code
     * @return true if the key is bound
     */
    public boolean isKeyBound(int keyCode) {
        return keyBindings.containsValue(keyCode);
    }
    
    /**
     * Check if a mouse button is bound to any action.
     * @param mouseButton the mouse button code
     * @return true if the mouse button is bound
     */
    public boolean isMouseButtonBound(int mouseButton) {
        return mouseBindings.containsValue(mouseButton);
    }
    
    /**
     * Set an input setting value.
     * @param setting the setting name
     * @param value the setting value
     */
    public void setSetting(String setting, Object value) {
        inputSettings.put(setting, value);
    }
    
    /**
     * Get an input setting value.
     * @param setting the setting name
     * @param defaultValue the default value if setting doesn't exist
     * @return the setting value
     */
    @SuppressWarnings("unchecked")
    public <T> T getSetting(String setting, T defaultValue) {
        Object value = inputSettings.get(setting);
        if (value != null && defaultValue.getClass().isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return defaultValue;
    }
    
    /**
     * Load configuration from a properties file.
     * @param filename the file path
     * @return true if loaded successfully
     */
    public boolean loadFromFile(String filename) {
        try (InputStream input = new FileInputStream(filename)) {
            Properties props = new Properties();
            props.load(input);
            
            // Clear existing bindings
            keyBindings.clear();
            mouseBindings.clear();
            inputSettings.clear();
            
            // Load key bindings
            for (String key : props.stringPropertyNames()) {
                String value = props.getProperty(key);
                
                if (key.startsWith("key.")) {
                    String action = key.substring(4);
                    try {
                        int keyCode = Integer.parseInt(value);
                        keyBindings.put(action, keyCode);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid key code for action " + action + ": " + value);
                    }
                } else if (key.startsWith("mouse.")) {
                    String action = key.substring(6);
                    try {
                        int mouseButton = Integer.parseInt(value);
                        mouseBindings.put(action, mouseButton);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid mouse button for action " + action + ": " + value);
                    }
                } else if (key.startsWith("setting.")) {
                    String setting = key.substring(8);
                    inputSettings.put(setting, value);
                }
            }
            
            return true;
        } catch (IOException e) {
            System.err.println("Failed to load input configuration from " + filename + ": " + e.getMessage());
            resetToDefaults();
            return false;
        }
    }
    
    /**
     * Save configuration to a properties file.
     * @param filename the file path
     * @return true if saved successfully
     */
    public boolean saveToFile(String filename) {
        try (OutputStream output = new FileOutputStream(filename)) {
            Properties props = new Properties();
            
            // Save key bindings
            for (Map.Entry<String, Integer> entry : keyBindings.entrySet()) {
                props.setProperty("key." + entry.getKey(), entry.getValue().toString());
            }
            
            // Save mouse bindings
            for (Map.Entry<String, Integer> entry : mouseBindings.entrySet()) {
                props.setProperty("mouse." + entry.getKey(), entry.getValue().toString());
            }
            
            // Save settings
            for (Map.Entry<String, Object> entry : inputSettings.entrySet()) {
                props.setProperty("setting." + entry.getKey(), entry.getValue().toString());
            }
            
            props.store(output, "Input Configuration");
            return true;
        } catch (IOException e) {
            System.err.println("Failed to save input configuration to " + filename + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get a human-readable name for a key code.
     * @param keyCode the key code
     * @return the key name
     */
    public static String getKeyName(int keyCode) {
        return KeyEvent.getKeyText(keyCode);
    }
    
    /**
     * Get a human-readable name for a mouse button.
     * @param mouseButton the mouse button code
     * @return the mouse button name
     */
    public static String getMouseButtonName(int mouseButton) {
        switch (mouseButton) {
            case MouseEvent.BUTTON1: return "Left Mouse Button";
            case MouseEvent.BUTTON2: return "Middle Mouse Button";
            case MouseEvent.BUTTON3: return "Right Mouse Button";
            default: return "Mouse Button " + mouseButton;
        }
    }
    
    /**
     * Validate the current configuration.
     * @return a list of validation errors, empty if valid
     */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        
        // Check for duplicate key bindings
        Set<Integer> usedKeys = new HashSet<>();
        for (Map.Entry<String, Integer> entry : keyBindings.entrySet()) {
            if (usedKeys.contains(entry.getValue())) {
                errors.add("Key " + getKeyName(entry.getValue()) + " is bound to multiple actions");
            }
            usedKeys.add(entry.getValue());
        }
        
        // Check for duplicate mouse bindings
        Set<Integer> usedButtons = new HashSet<>();
        for (Map.Entry<String, Integer> entry : mouseBindings.entrySet()) {
            if (usedButtons.contains(entry.getValue())) {
                errors.add("Mouse button " + getMouseButtonName(entry.getValue()) + " is bound to multiple actions");
            }
            usedButtons.add(entry.getValue());
        }
        
        return errors;
    }
    
    /**
     * Get a summary of the current configuration.
     * @return a string describing the configuration
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Input Configuration Summary:\n");
        sb.append("Key Bindings: ").append(keyBindings.size()).append("\n");
        sb.append("Mouse Bindings: ").append(mouseBindings.size()).append("\n");
        sb.append("Settings: ").append(inputSettings.size()).append("\n");
        
        List<String> errors = validate();
        if (!errors.isEmpty()) {
            sb.append("Validation Errors: ").append(errors.size()).append("\n");
        }
        
        return sb.toString();
    }
}