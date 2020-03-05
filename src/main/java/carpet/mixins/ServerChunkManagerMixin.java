package carpet.mixins;

import carpet.utils.SpawnReporter;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.entity.EntityCategory;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin
{
    @Shadow @Final private ServerWorld world;

    @Inject(
            method = "tickChunks",
            locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;entryIterator()Ljava/lang/Iterable;",
                    shift = At.Shift.AFTER,
                    ordinal = 0

    ))
    //this runs once per world spawning cycle. Allows to grab mob counts and count spawn ticks
    private void grabMobcaps(CallbackInfo ci,
                             long long_1,
                             long long_2,
                             LevelProperties levelProperties_1,
                             boolean boolean_1,
                             boolean boolean_2,
                             int int_1,
                             boolean boolean_3,
                             int int_2,
                             EntityCategory[] entityCategorys_1,
                             Object2IntMap object2IntMap_1)
    {
        DimensionType dim = this.world.dimension.getType();
        //((WorldInterface)world).getPrecookedMobs().clear(); not needed because mobs are compared with predefined BBs
        SpawnReporter.mobCounts.put(dim, (Object2IntMap<EntityCategory>)object2IntMap_1);
        SpawnReporter.chunkCounts.put(dim, int_2);

    }


    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(method = "method_20801", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/EntityCategory;getSpawnCap()I"
    ))
    // allows to change mobcaps and captures each category try per dimension before it fails due to full mobcaps.
    private int getNewMobcaps(EntityCategory entityCategory)
    {
        int newCap = (int) ((double)entityCategory.getSpawnCap()*(Math.pow(2.0,(SpawnReporter.mobcap_exponent/4))));
        return newCap;
    }
}
