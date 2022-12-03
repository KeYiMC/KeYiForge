package gg.m2ke4u.keyicore.core.entities;

import gg.m2ke4u.keyicore.core.TickTask;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

public final class SingleEntityTickTask implements TickTask<Entity> {

    @Override
    public void call(Entity input) {
        final World world = input.world;
        if (!world.entityLimiter.shouldContinue()) {
            return;
        }
        Entity entity3 = input.getRidingEntity();
        if (entity3 != null) {
            if (!entity3.isDead && entity3.isPassenger(input)) {
                return;
            }
            input.dismountRidingEntity();
        }
        if (!input.isDead && !(input instanceof EntityPlayerMP)) {
            world.updateEntity(input);
        }
        if (input.isDead) {
            int l1 = input.chunkCoordX;
            int i2 = input.chunkCoordZ;
            if (input.addedToChunk && world.isChunkLoaded(l1, i2, true)) {
                world.getChunkFromChunkCoords(l1, i2).removeEntity(input);
            }
            world.loadedEntityList.remove(input); // CraftBukkit - Use field for loop variable
            world.onEntityRemoved(input);
        }
    }

    @Deprecated
    @Override
    public boolean finished() {
        return false;
    }

    @Deprecated
    @Override
    public boolean terminate() {
        return false;
    }

    @Deprecated
    @Override
    public void forceTerminate() {
    }
}
