package com.github.sculkhorde.util;

import com.github.sculkhorde.common.advancement.*;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.gravemind_system.Gravemind;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;

public final class AdvancementUtil {

    public static Advancement getAdvancement(ResourceLocation id) {
        return ServerLifecycleHooks.getCurrentServer().getAdvancements().getAdvancement(id);
    }

    public static boolean completeAdvancement(ServerPlayer player, ResourceLocation id, String criterion) {
        Advancement adv = getAdvancement(id);

        if (adv != null)
            return player.getAdvancements().award(adv, criterion);

        return false;
    }

    public static boolean isAdvancementCompleted(ServerPlayer player, ResourceLocation id) {
        Advancement adv = ServerLifecycleHooks.getCurrentServer().getAdvancements().getAdvancement(id);

        if (adv != null)
        {
            return player.getAdvancements().getOrStartProgress(adv).isDone();
        }

        return false;
    }

    public static void giveAdvancementToAllPlayers(ServerLevel level, CustomCriterionTrigger trigger) {
        for (ServerPlayer player : level.players()) {
            trigger.trigger(player);
        }
    }

    public static void giveAdvancementToPlayer(ServerPlayer player, CustomCriterionTrigger trigger) {
        trigger.trigger(player);
    }


    public static void advancementHandlingTick(ServerLevel level)
    {

        // If Immature, give all players advancement
        if(SculkHorde.gravemind.getEvolutionState().ordinal() >= Gravemind.evolution_states.Immature.ordinal())
        {
            AdvancementUtil.giveAdvancementToAllPlayers(SculkHorde.savedData.level, GravemindEvolveImmatureTrigger.INSTANCE);
        }

        // If Immature, give all players advancement
        if(SculkHorde.gravemind.getEvolutionState().ordinal() >= Gravemind.evolution_states.Mature.ordinal())
        {
            AdvancementUtil.giveAdvancementToAllPlayers(SculkHorde.savedData.level, GravemindEvolveMatureTrigger.INSTANCE);
        }

        // If Immature, give all players advancement
        if(SculkHorde.savedData.isHordeDefeated())
        {
            AdvancementUtil.giveAdvancementToAllPlayers(SculkHorde.savedData.level, SculkHordeDefeatTrigger.INSTANCE);
        }

        if(SculkHorde.savedData.getNodeEntries().size() > 0)
        {
            AdvancementUtil.giveAdvancementToAllPlayers(SculkHorde.savedData.level, SculkNodeSpawnTrigger.INSTANCE);
        }

    }

}
