package carpet.mixins;

import carpet.CarpetSettings;
import carpet.utils.TickSpeed;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderTickCounter.class)
public class RenderTickCounter_tickSpeedMixin {
    @Shadow @Final private float tickTime;

    @Redirect(method = "beginRenderTick", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/render/RenderTickCounter;tickTime:F"
    ))
    private float adjustTickSpeed(RenderTickCounter counter) {

        if ( CarpetSettings.smoothClientAnimations)
            return Math.max(tickTime, TickSpeed.mspt);
        return tickTime;
    }
}