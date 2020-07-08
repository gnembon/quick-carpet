package carpet;

import carpet.settings.ParsedRule;
import carpet.settings.Rule;
import carpet.settings.Validator;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static carpet.settings.RuleCategory.COMMAND;
import static carpet.settings.RuleCategory.CREATIVE;

@SuppressWarnings("CanBeFinal")
public class CarpetSettings
{
    public static final String carpetVersion = "1.0.2";
    public static final Logger LOG = LogManager.getLogger();
    public static boolean impendingFillSkipUpdates = false;

    @Rule(desc = "Gbhs sgnf sadsgras fhskdpri!", category = CREATIVE)
    public static boolean superSecretSetting = false;

    @Rule( desc = "Explosions won't destroy blocks", category = {CREATIVE} )
    public static boolean explosionNoBlockDamage = false;

    @Rule(
            desc = "hoppers pointing to wool will count items passing through them",
            extra = {
                    "Enables /counter command, and actions while placing red and green carpets on wool blocks",
                    "Use /counter <color?> reset to reset the counter, and /counter <color?> to query",
                    "Counters are global and shared between players, 16 channels available",
                    "Items counted are destroyed, count up to one stack per tick per hopper"
            },
            category = {COMMAND, CREATIVE}
    )
    public static boolean hopperCounters = false;


    @Rule(desc = "Enables /spawn command for mobcaps information", category = COMMAND)
    public static String commandSpawn = "true";

    @Rule(desc = "Enables /tick command to control game clocks", extra = "Available functions: warp, rate and health", category = COMMAND)
    public static String commandTick = "true";

    @Rule(desc = "Enables /player command to spawn players", category = COMMAND)
    public static String commandPlayer = "true";

    @Rule(desc = "fill/clone/setblock and structure blocks cause block updates", category = CREATIVE)
    public static boolean fillUpdates = true;

    @Rule(desc = "placing blocks cause block updates", category = CREATIVE)
    public static boolean interactionUpdates = true;

    @Rule(desc = "smooth client animations with low tps settings", extra = "works only in SP, and will slow down players", category = CREATIVE)
    public static boolean smoothClientAnimations;

    private static class FillLimitLimits extends Validator<Integer> {
        @Override public Integer validate(ServerCommandSource source, ParsedRule<Integer> currentRule, Integer newValue, String string) {
            return (newValue>0 && newValue < 20000000) ? newValue : null;
        }
        @Override
        public String description() { return "You must choose a value from 1 to 20M";}
    }
    @Rule(
            desc = "Customizable fill/clone volume limit",
            options = {"32768", "250000", "1000000"},
            category = CREATIVE,
            strict = false,
            validate = FillLimitLimits.class
    )
    public static int fillLimit = 32768;
}
