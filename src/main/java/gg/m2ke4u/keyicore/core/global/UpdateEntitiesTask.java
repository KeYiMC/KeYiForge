package gg.m2ke4u.keyicore.core.global;

import gg.m2ke4u.keyicore.config.WorkerConfig;
import gg.m2ke4u.keyicore.utils.ConcurrentlyTraverse;
import gg.m2ke4u.lutils.threading.traversing.CollectionConcurrentUtils;
import gg.m2ke4u.keyicore.core.TickTask;
import gg.m2ke4u.keyicore.core.entities.SingleEntityTickTask;
import gg.m2ke4u.keyicore.core.profiler.SinglePartProfiler;
import gg.m2ke4u.keyicore.core.tiles.SingleSimpleTileEntityTickTask;
import gg.m2ke4u.keyicore.core.tiles.SingleTickableTileEntityTickTask;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

public final class UpdateEntitiesTask implements TickTask<World> {
    private volatile boolean finished = false;
    private final Executor executor;
    private volatile boolean terminated = false;
    private final SinglePartProfiler profiler = new SinglePartProfiler("ConcurrentEntities");

    private static final SingleEntityTickTask entityTickTask = new SingleEntityTickTask();
    private static final SingleTickableTileEntityTickTask tickableTileEntityTickTask = new SingleTickableTileEntityTickTask();
    private static final SingleSimpleTileEntityTickTask addedTileEntityTickTask = new SingleSimpleTileEntityTickTask();

    public UpdateEntitiesTask(Executor executor){
        this.executor = executor;
    }

    @Override
    public void call(World input) {
        try {
            this.profiler.postSection();;
            CollectionConcurrentUtils.runTraverseInOtherPool(input.weatherEffects, entity->{
                if (entity == null || this.terminated) {
                    return;
                }
                try
                {
                    if(entity.updateBlocked) return;
                    ++entity.ticksExisted;
                    entity.onUpdate();
                }
                catch (Throwable throwable2) {throwable2.printStackTrace();}
                if (entity.isDead) {input.weatherEffects.remove(entity);}
            }, ((ForkJoinPool) this.executor));

            input.loadedEntityList.removeAll(input.unloadedEntityList);
            CollectionConcurrentUtils.traverseConcurrent(input.unloadedEntityList,entity1->{
                int j = entity1.chunkCoordX;
                int k1 = entity1.chunkCoordZ;
                if (entity1.addedToChunk && input.isChunkLoaded(j, k1, true)) {
                    input.getChunkFromChunkCoords(j, k1).removeEntity(entity1);
                }
                input.onEntityRemoved(entity1);
            },this.executor);
            input.unloadedEntityList.clear();

            org.spigotmc.ActivationRange.activateEntities(input); // Spigot
            input.entityLimiter.initTick();

            switch (WorkerConfig.WORKER_MODE){
                case 1:

                    CollectionConcurrentUtils.traverseConcurrent(input.loadedEntityList,entity2->{
                    if (this.terminated){
                        return;
                    }
                    entityTickTask.call(entity2);
                }, ((ForkJoinPool) this.executor));
                    break;
                case 2:
                    CollectionConcurrentUtils.runTraverseInOtherPool(input.loadedEntityList,entity2->{
                        if (this.terminated){
                            return;
                        }
                        entityTickTask.call(entity2);
                    }, ((ForkJoinPool) this.executor));
            }

            input.processingLoadedTiles = true; //FML Move above remove to prevent CMEs

            //Remove tile entities
            if (!input.tileEntitiesToBeRemoved.isEmpty())
            {
                for (TileEntity tile : input.tileEntitiesToBeRemoved) {tile.onChunkUnload();}
                java.util.Set<TileEntity> remove = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
                remove.addAll(input.tileEntitiesToBeRemoved);
                input.tickableTileEntities.removeAll(remove);
                input.loadedTileEntityList.removeAll(remove);
                input.tileEntitiesToBeRemoved.clear();
            }

            input.tileLimiter.initTick();

            switch (WorkerConfig.WORKER_MODE){
                case 1:
                    CollectionConcurrentUtils.traverseConcurrent(input.tickableTileEntities,tileentity->{
                        if (this.terminated){
                            return;
                        }
                        tickableTileEntityTickTask.call(tileentity);
                    },((ForkJoinPool) this.executor));
                    break;
                case 2:
                    CollectionConcurrentUtils.runTraverseInOtherPool(input.tickableTileEntities,tileentity->{
                        if (this.terminated){
                            return;
                        }
                        tickableTileEntityTickTask.call(tileentity);
                    },((ForkJoinPool) this.executor));
                    break;
            }

            input.processingLoadedTiles = false;
            if (!input.addedTileEntityList.isEmpty())
            {
                switch (WorkerConfig.WORKER_MODE){
                    case 1:
                        CollectionConcurrentUtils.traverseConcurrent(input.addedTileEntityList,tileentity1->{
                            if (this.terminated){
                                return;
                            }
                            addedTileEntityTickTask.call(tileentity1);
                        },((ForkJoinPool) this.executor));
                        break;
                    case 2:
                        CollectionConcurrentUtils.runTraverseInOtherPool(input.addedTileEntityList,tileentity1->{
                            if (this.terminated){
                                return;
                            }
                            addedTileEntityTickTask.call(tileentity1);
                        },((ForkJoinPool) this.executor));
                        break;
                }
                input.addedTileEntityList.clear();
            }
        }finally {
            this.finished = true;
            this.profiler.finishedTask();
        }
    }

    public SinglePartProfiler getProfiler(){
        return this.profiler;
    }


    @Override
    public boolean finished() {
        return this.finished;
    }

    @Deprecated
    @Override
    public boolean terminate() {
        this.terminated = true;
        return this.terminated;
    }

    @Deprecated
    @Override
    public void forceTerminate() {
        this.terminated = true;
    }
}
