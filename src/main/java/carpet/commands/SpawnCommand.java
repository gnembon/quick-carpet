package carpet.commands;

import carpet.CarpetSettings;
import carpet.settings.SettingsManager;
import carpet.utils.Messenger;
import carpet.utils.SpawnReporter;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.command.CommandSource.suggestMatching;


public class SpawnCommand
{
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        LiteralArgumentBuilder<ServerCommandSource> literalargumentbuilder = literal("spawn").
                requires((player) -> SettingsManager.canUseCommand(player, CarpetSettings.commandSpawn));

        literalargumentbuilder.
                then(literal("list").
                        then(argument("pos", BlockPosArgumentType.blockPos()).
                                executes( (c) -> listSpawns(c.getSource(), BlockPosArgumentType.getBlockPos(c, "pos"))))).
                then(literal("mobcaps").
                        executes( (c) -> generalMobcaps(c.getSource())).
                        then(literal("set").
                                then(argument("cap (hostile)", integer(1,1400)).
                                        executes( (c) -> setMobcaps(c.getSource(), getInteger(c, "cap (hostile)"))))).
                        then(argument("dimension", DimensionArgumentType.dimension()).
                                executes( (c)-> mobcapsForDimension(c.getSource(), DimensionArgumentType.getDimensionArgument(c, "dimension"))))).
                then(literal("entities").
                        executes( (c) -> generalMobcaps(c.getSource()) ).
                        then(argument("type", string()).
                                suggests( (c, b)->suggestMatching(Arrays.stream(SpawnGroup.values()).map(SpawnGroup::getName), b)).
                                executes( (c) -> listEntitiesOfType(c.getSource(), getString(c, "type")))));

        dispatcher.register(literalargumentbuilder);
    }

    private static SpawnGroup getCategory(String string) throws CommandSyntaxException
    {
        if (!Arrays.stream(SpawnGroup.values()).map(SpawnGroup::getName).collect(Collectors.toSet()).contains(string))
        {
            throw new SimpleCommandExceptionType(Messenger.c("r Wrong mob type: "+string+" should be "+ Arrays.stream(SpawnGroup.values()).map(SpawnGroup::getName).collect(Collectors.joining(", ")))).create();
        }
        return SpawnGroup.valueOf(string.toUpperCase());
    }


    private static int listSpawns(ServerCommandSource source, BlockPos pos)
    {
        Messenger.send(source, SpawnReporter.report(pos, source.getWorld()));
        return 1;
    }



    private static int generalMobcaps(ServerCommandSource source)
    {
        Messenger.send(source, SpawnReporter.printMobcapsForDimension(source.getWorld(), true));
        return 1;
    }


    private static int setMobcaps(ServerCommandSource source, int hostile_cap)
    {
        double desired_ratio = (double)hostile_cap/ SpawnGroup.MONSTER.getCapacity();
        SpawnReporter.mobcap_exponent = 4.0*Math.log(desired_ratio)/Math.log(2.0);
        Messenger.m(source, String.format("gi Mobcaps for hostile mobs changed to %d, other groups will follow", hostile_cap));
        return 1;
    }

    private static int mobcapsForDimension(ServerCommandSource source, ServerWorld dim)
    {
        Messenger.send(source, SpawnReporter.printMobcapsForDimension(dim, true));
        return 1;
    }

    private static int listEntitiesOfType(ServerCommandSource source, String mobtype) throws CommandSyntaxException
    {
        SpawnGroup cat = getCategory(mobtype);
        Messenger.send(source, SpawnReporter.printEntitiesByType(cat, source.getWorld()));
        return 1;
    }
}
