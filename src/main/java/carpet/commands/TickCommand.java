package carpet.commands;

import carpet.CarpetSettings;
import carpet.utils.TickSpeed;
import carpet.settings.SettingsManager;
import carpet.utils.Messenger;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.BaseText;
import net.minecraft.server.command.ServerCommandSource;

import static com.mojang.brigadier.arguments.FloatArgumentType.floatArg;
import static com.mojang.brigadier.arguments.FloatArgumentType.getFloat;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandSource.suggestMatching;

public class TickCommand
{
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        LiteralArgumentBuilder<ServerCommandSource> literalargumentbuilder = literal("tick").
                requires((player) -> SettingsManager.canUseCommand(player, CarpetSettings.commandTick)).
                then(literal("rate").
                        executes((c) -> queryTps(c.getSource())).
                        then(argument("rate", floatArg(0.1F, 500.0F)).
                                suggests( (c, b) -> suggestMatching(new String[]{"20.0"},b)).
                                executes((c) -> setTps(c.getSource(), getFloat(c, "rate"))))).
                then(literal("warp").
                        executes( (c)-> setWarp(c.getSource(), 0, null)).
                        then(argument("ticks", integer(0,4000000)).
                                suggests( (c, b) -> suggestMatching(new String[]{"3600","72000"},b)).
                                executes((c) -> setWarp(c.getSource(), getInteger(c,"ticks"), null)).
                                then(argument("tail command", greedyString()).
                                        executes( (c) -> setWarp(
                                                c.getSource(),
                                                getInteger(c,"ticks"),
                                                getString(c, "tail command")))))).
                then(literal("health").executes( (c) -> healthReport(c.getSource())));


        dispatcher.register(literalargumentbuilder);
    }


    private static int setTps(ServerCommandSource source, float tps)
    {
        TickSpeed.tickrate(tps);
        queryTps(source);
        return (int)tps;
    }

    private static int queryTps(ServerCommandSource source)
    {
        Messenger.m(source, "w Current tps is: ",String.format("wb %.1f", TickSpeed.tickrate));
        return (int)TickSpeed.tickrate;
    }

    private static int setWarp(ServerCommandSource source, int advance, String tail_command)
    {
        PlayerEntity player = null;
        try
        {
            player = source.getPlayer();
        }
        catch (CommandSyntaxException ignored)
        {
        }
        BaseText message = TickSpeed.tickrate_advance(source.getMinecraftServer(), player, advance, tail_command, source);
        if (message != null)
        {
            source.sendFeedback(message, false);
        }
        return 1;
    }


    private static double average(long[] ls) {
        long l = 0L;
        long[] var4 = ls;
        int var5 = ls.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            long m = var4[var6];
            l += m;
        }

        return (double)l / (double)ls.length;
    }

    private static int healthReport(ServerCommandSource source)
    {
        Messenger.m(source, String.format(
                "w Avg tick time: %.2f ms",
                average(source.getMinecraftServer().lastTickLengths) * 1.0E-6D)
        );
        return 1;
    }
}

