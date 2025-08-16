package rpg.systems;

import rpg.components.RenderComponent;
import rpg.components.TransformComponent;
import rpg.engine.Entity;

/**
 * Helper class to hold renderable entity data for the rendering pipeline.
 * Contains references to the entity and its transform and render components.
 */
public class RenderableEntity {
    public final Entity entity;
    public final TransformComponent transformComponent;
    public final RenderComponent renderComponent;
    
    public RenderableEntity(Entity entity, TransformComponent transform, RenderComponent render) {
        this.entity = entity;
        this.transformComponent = transform;
        this.renderComponent = render;
    }
}