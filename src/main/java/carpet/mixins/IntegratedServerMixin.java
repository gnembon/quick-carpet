package carpet.mixins;

import carpet.CarpetServer;
import com.google.gson.JsonElement;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.level.LevelGeneratorOptions;
import net.minecraft.world.level.LevelGeneratorType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IntegratedServer.class)
public class IntegratedServerMixin
{
    @Inject(method = "loadWorld", at = @At("HEAD"))
    private void onSetupServerIntegrated(String name, String serverName, long seed, LevelGeneratorOptions levelGeneratorOptions, CallbackInfo ci) {
        CarpetServer.onServerLoaded((IntegratedServer) (Object) this);
    }
}
