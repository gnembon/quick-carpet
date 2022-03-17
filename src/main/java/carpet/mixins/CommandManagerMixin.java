package carpet.mixins;

import carpet.CarpetServer;
import carpet.CarpetSettings;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.class_7157;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(CommandManager.class)
public abstract class CommandManagerMixin
{

    @Shadow
    @Final
    private CommandDispatcher<ServerCommandSource> dispatcher;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onRegister(CommandManager.RegistrationEnvironment environment, class_7157 arg, CallbackInfo ci) {
        CarpetServer.registerCarpetCommands(this.dispatcher);
    }

    @Inject(method = "execute", at = @At("HEAD"))
    private void onExecuteBegin(ServerCommandSource serverCommandSource_1, String string_1, CallbackInfoReturnable<Integer> cir)
    {
        if (!CarpetSettings.fillUpdates)
            CarpetSettings.impendingFillSkipUpdates = true;
    }

    @Inject(method = "execute", at = @At("RETURN"))
    private void onExecuteEnd(ServerCommandSource serverCommandSource_1, String string_1, CallbackInfoReturnable<Integer> cir)
    {
        CarpetSettings.impendingFillSkipUpdates = false;
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(method = "execute", at = @At(
            value = "INVOKE",
            target = "Lorg/slf4j/Logger;isDebugEnabled()Z"
    ))
    private boolean doesOutputCommandStackTrace(Logger instance)
    {
        if (CarpetSettings.superSecretSetting)
            return true;
        return instance.isDebugEnabled();
    }
}