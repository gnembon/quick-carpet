package carpet.mixins;

import carpet.utils.SpawnReporter;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin
{
    @Shadow @Final private ServerWorld world;

    @Redirect(method = "tickChunks", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/world/ChunkTicketManager;getTickedChunkCount()I"
    ))
    //this runs once per world spawning cycle. Allows to grab mob counts and count spawn ticks
    private int setupTracking(ChunkTicketManager chunkTicketManager)
    {
        int j = chunkTicketManager.getTickedChunkCount();
        RegistryKey<World> dim = this.world.getRegistryKey(); // getDimensionType;
        //((WorldInterface)world).getPrecookedMobs().clear(); not needed because mobs are compared with predefined BBs
        SpawnReporter.chunkCounts.put(dim, j);
        return j;
    }
}
