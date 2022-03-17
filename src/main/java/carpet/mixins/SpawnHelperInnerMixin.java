package carpet.mixins;

import carpet.utils.SpawnReporter;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.GravityField;
import net.minecraft.world.SpawnHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// temporary mixin until naming for spawnhelper gets fixed.

@Mixin(SpawnHelper.Info.class)
public class SpawnHelperInnerMixin
{
    @Shadow @Final private int spawningChunkCount;

    @Shadow @Final private GravityField densityField;

    @Shadow @Final private Object2IntOpenHashMap<SpawnGroup> groupToCount;

    @Inject(method = "isBelowCap", at = @At("HEAD"), cancellable = true)
    private void changeMobCaps(SpawnGroup group, ChunkPos chunkPos, CallbackInfoReturnable<Boolean> cir)
    {
        int newCap = (int) ((double)group.getCapacity()*(Math.pow(2.0,(SpawnReporter.mobcap_exponent/4))));
        int i = newCap * spawningChunkCount / SpawnReporter.MAGIC_NUMBER;
        cir.setReturnValue(groupToCount.getInt(group) < i);
    }

}