package carpet.mixins;

import carpet.CarpetSettings;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldChunk.class)
public class WorldChunk_fillUpdatesMixin
{
    @Redirect(method = "setBlockState", at = @At(
            value = "INVOKE",
            target ="Lnet/minecraft/block/BlockState;method_26197(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V"
    ))
    private void onAdded(BlockState blockState, World world_1, BlockPos blockPos_1, BlockState blockState_1, boolean boolean_1)
    {
        if (!CarpetSettings.impendingFillSkipUpdates)
            blockState.method_26197(world_1, blockPos_1, blockState_1, boolean_1); // onBLockAdded
    }
}
