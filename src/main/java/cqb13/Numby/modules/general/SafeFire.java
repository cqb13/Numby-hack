package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.block.Material;
import net.minecraft.util.shape.VoxelShapes;

/**
 * from Tanuki made by walaryne
 */
public class SafeFire extends Module {
    public SafeFire() {
        super(NumbyHack.CATEGORY, "safe-fire", "Prevents you from walking into fire.");
    }

    @EventHandler
    public void onCollisionShape(CollisionShapeEvent e) {
        if (e.state.getMaterial() == Material.FIRE) {
            e.shape = VoxelShapes.fullCube();
        }
    }
}

