package rpg.exceptions;

/**
 * Exception thrown when asset loading fails.
 * This includes images, sounds, maps, and other game resources.
 */
public class AssetLoadException extends GameException {
    private final String assetPath;
    private final String assetType;
    
    public AssetLoadException(String assetPath, String assetType) {
        super("Failed to load " + assetType + " asset: " + assetPath, "ASSET_LOAD_FAILED");
        this.assetPath = assetPath;
        this.assetType = assetType;
    }
    
    public AssetLoadException(String assetPath, String assetType, Throwable cause) {
        super("Failed to load " + assetType + " asset: " + assetPath, cause, "ASSET_LOAD_FAILED", true);
        this.assetPath = assetPath;
        this.assetType = assetType;
    }
    
    public AssetLoadException(String assetPath, String assetType, String message) {
        super("Failed to load " + assetType + " asset: " + assetPath + " - " + message, "ASSET_LOAD_FAILED");
        this.assetPath = assetPath;
        this.assetType = assetType;
    }
    
    /**
     * Get the path of the asset that failed to load.
     */
    public String getAssetPath() {
        return assetPath;
    }
    
    /**
     * Get the type of asset that failed to load.
     */
    public String getAssetType() {
        return assetType;
    }
}