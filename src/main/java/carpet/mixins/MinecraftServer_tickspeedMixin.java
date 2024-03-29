package carpet.mixins;

import carpet.utils.TickSpeed;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TickDurationMonitor;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServer_tickspeedMixin extends ReentrantThreadExecutor<ServerTask>
{
    @Shadow private volatile boolean running;

    @Shadow private long timeReference;

    //@Shadow private boolean profilerStartQueued;

    @Shadow @Final private Profiler profiler;

    public MinecraftServer_tickspeedMixin(String string)
    {
        super(string);
    }

    @Shadow protected abstract void tick(BooleanSupplier booleanSupplier_1);

    @Shadow protected abstract boolean shouldKeepTicking();

    //@Shadow private long field_19248;

    //@Shadow protected abstract void method_16208();

    @Shadow private volatile boolean loading;

    //@Shadow protected abstract void startMonitor(TickDurationMonitor monitor);

    @Shadow private long lastTimeReference;
    @Shadow private boolean waitingForNextTick;

    @Shadow public abstract Iterable<ServerWorld> getWorlds();

    @Shadow private boolean needsDebugSetup;
    @Shadow private boolean needsRecorderSetup;
    @Shadow private int ticks;

    @Shadow protected abstract void startTickMetrics();

    @Shadow protected abstract void runTasksTillTickEnd();

    @Shadow private long nextTickTimestamp;
    @Shadow @Final private static Logger LOGGER;
    private float carpetMsptAccum = 0.0f;

    /**
     * To ensure compatibility with other mods we should allow milliseconds
     */

    // Cancel a while statement
    @Redirect(method = "runServer", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;running:Z"))
    private boolean cancelRunLoop(MinecraftServer server)
    {
        return false;
    }

    // Replaced the above cancelled while statement with this one
    // could possibly just inject that mspt selection at the beginning of the loop, but then adding all mspt's to
    // replace 50L will be a hassle
    @Inject(method = "runServer", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/server/MinecraftServer;setFavicon(Lnet/minecraft/server/ServerMetadata;)V"))
    private void modifiedRunLoop(CallbackInfo ci)
    {
        while (this.running)
        {
            //long long_1 = Util.getMeasuringTimeMs() - this.timeReference;
            //CM deciding on tick speed
            long msThisTick = 0L;
            long long_1 = 0L;
            if (TickSpeed.time_warp_start_time != 0 && TickSpeed.continueWarp((MinecraftServer) (Object)this))
            {
                //making sure server won't flop after the warp or if the warp is interrupted
                this.timeReference = this.lastTimeReference = Util.getMeasuringTimeMs();
                carpetMsptAccum = TickSpeed.mspt;
            }
            else
            {
                if (Math.abs(carpetMsptAccum - TickSpeed.mspt) > 1.0f)
                {
                	// Tickrate changed. Ensure that we use the correct value.
                	carpetMsptAccum = TickSpeed.mspt;
                }

                msThisTick = (long)carpetMsptAccum; // regular tick
                carpetMsptAccum += TickSpeed.mspt - msThisTick;

                long_1 = Util.getMeasuringTimeMs() - this.timeReference;
            }
            //end tick deciding
            //smoothed out delay to include mcpt component. With 50L gives defaults.
            if (long_1 > /*2000L*/1000L+20*TickSpeed.mspt && this.timeReference - this.lastTimeReference >= /*15000L*/10000L+100*TickSpeed.mspt)
            {
                long long_2 = (long)(long_1 / TickSpeed.mspt);//50L;
                LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", long_1, long_2);
                this.timeReference += (long)(long_2 * TickSpeed.mspt);//50L;
                this.lastTimeReference = this.timeReference;
            }

            if (needsDebugSetup) {
                this.needsDebugSetup = false;
                this.profilerTimings = Pair.of(Util.getMeasuringTimeNano(), ticks);
            }

            this.timeReference += msThisTick;//50L;
            startTickMetrics();
            this.profiler.push("tick");
            this.tick(TickSpeed.time_warp_start_time != 0 ? ()->true : this::shouldKeepTicking);
            this.profiler.swap("nextTickWait");
            if (TickSpeed.time_warp_start_time != 0) // clearing all hanging tasks no matter what when warping
            {
                while(this.runEveryTask()) {Thread.yield();}
            }
            this.waitingForNextTick = true;
            this.nextTickTimestamp = Math.max(Util.getMeasuringTimeMs() + /*50L*/ msThisTick, this.timeReference);
            this.runTasksTillTickEnd();
            this.profiler.pop();
            this.profiler.endTick();
            this.loading = true;
        }
    }

    // just because profilerTimings class is public
    Pair<Long,Integer> profilerTimings = null;

    private boolean runEveryTask() {
        if (super.runTask()) {
            return true;
        } else {
            if (true) { // unconditionally this time
                for(ServerWorld serverlevel : getWorlds()) {
                    if (serverlevel.getChunkManager().executeQueuedTasks()) {
                        return true;
                    }
                }
            }

            return false;
        }
    }
}
