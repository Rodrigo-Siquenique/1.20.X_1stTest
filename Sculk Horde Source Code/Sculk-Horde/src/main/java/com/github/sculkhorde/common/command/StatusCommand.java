package com.github.sculkhorde.common.command;

import com.github.sculkhorde.core.SculkHorde;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class StatusCommand implements Command<CommandSourceStack> {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {

        return Commands.literal("status")
                .executes(new StatusCommand());

    }

    @Override
    public int run(CommandContext<CommandSourceStack> context)
    {
        context.getSource().sendSuccess(()->Component.literal(
                "Horde State: " + SculkHorde.savedData.getHordeState().toString()
                        + "\n"
                        + "Gravemind State: " + SculkHorde.gravemind.getEvolutionState().toString()
                        + "\n"
                        + "Sculk Mass Accumulated: " + SculkHorde.savedData.getSculkAccumulatedMass()
                        + "\n"
                        + "Sculk Nodes Present: " + SculkHorde.savedData.getNodeEntries().size()
                        + "\n"
                        + "Performance Mode: " + SculkHorde.autoPerformanceSystem.getPerformanceMode().toString()
                ), false);
        return 0;
    }

}
