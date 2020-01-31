package carpet.commands;

import carpet.CarpetServer;
import carpet.utils.Messenger;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TestCommand
{
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        dispatcher.register(literal("test").
                then(literal("dump").
                        executes((c) -> CarpetServer.settingsManager.printAllRulesToLog(null)).
                        then(argument("category", word()).
                                executes( (c) -> CarpetServer.settingsManager.printAllRulesToLog(getString(c, "category"))))));
    }
}
