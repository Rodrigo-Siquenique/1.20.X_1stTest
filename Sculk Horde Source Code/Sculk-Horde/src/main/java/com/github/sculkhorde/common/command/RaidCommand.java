package com.github.sculkhorde.common.command;

import com.github.sculkhorde.systems.raid_system.RaidHandler;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class RaidCommand implements Command<CommandSourceStack> {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {

        return Commands.literal("raid")
                .then(Commands.literal("end").requires(command -> command.hasPermission(1))
                        .executes((context -> startOrEndRaid(context, "end"))
                                ));

    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        return 0;
    }


    private static int startOrEndRaid(CommandContext<CommandSourceStack> context, String operation) throws CommandSyntaxException
    {

        RaidHandler.raidData.setRaidState(RaidHandler.RaidState.FAILED);
        return 0;
    }


}
