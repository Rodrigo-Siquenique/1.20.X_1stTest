package com.github.sculkhorde.systems;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.TickUnits;
import com.google.common.base.Predicates;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import java.util.ArrayList;

public class DebugSlimeSystem {
    protected PlayerTeam redDebugTeam;
    public static String redDebugTeamID = "debug_red";
    protected PlayerTeam yellowDebugTeam;
    public static String yellowDebugTeamID = "debug_yellow";
    protected PlayerTeam greenDebugTeam;
    public static String greenDebugTeamID = "debug_green";
    protected PlayerTeam blueDebugTeam;
    public static String blueDebugTeamID = "debug_blue";

    protected ArrayList<Slime> debugSlimes = new ArrayList<>();
    protected long timeOfLastSlimeDeletion = 0;
    protected final long SLIME_DELETION_INTERVAL = TickUnits.convertSecondsToTicks(30);

    public DebugSlimeSystem()
    {
        createTeams();
    }

    protected void createTeams()
    {
        redDebugTeam = createTeamIfAbsent(redDebugTeamID, Component.literal(redDebugTeamID));
        redDebugTeam.setColor(ChatFormatting.RED);
        yellowDebugTeam = createTeamIfAbsent(yellowDebugTeamID, Component.literal(yellowDebugTeamID));
        yellowDebugTeam.setColor(ChatFormatting.YELLOW);
        greenDebugTeam = createTeamIfAbsent(greenDebugTeamID, Component.literal(greenDebugTeamID));
        greenDebugTeam.setColor(ChatFormatting.GREEN);
        blueDebugTeam = createTeamIfAbsent(blueDebugTeamID, Component.literal(blueDebugTeamID));
        blueDebugTeam.setColor(ChatFormatting.BLUE);
    }

    protected PlayerTeam createTeamIfAbsent(String teamID, Component teamDisplayName) {
        Scoreboard scoreboard = SculkHorde.savedData.level.getServer().getScoreboard();
        if (scoreboard.getPlayerTeam(teamID) != null)
        {
            return scoreboard.getPlayerTeam(teamID);
        }

        PlayerTeam playerteam = scoreboard.addPlayerTeam(teamID);
        playerteam.setDisplayName(teamDisplayName);
        return playerteam;
    }

    public Slime createDebugSlime(Level level, BlockPos pos)
    {
        Slime slime = new Slime(EntityType.SLIME, level);
        slime.setPos(pos.getCenter());
        slime.setInvulnerable(true);
        slime.goalSelector.removeAllGoals(Predicates.alwaysTrue());
        slime.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, Integer.MAX_VALUE));
        slime.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, Integer.MAX_VALUE, Integer.MAX_VALUE));
        slime.setSilent(true);
        level.addFreshEntity(slime);
        debugSlimes.add(slime);
        return slime;
    }

    public void serverTick()
    {
       if(Math.abs(SculkHorde.savedData.level.getGameTime() - timeOfLastSlimeDeletion) < SLIME_DELETION_INTERVAL)
       {
           return;
       }

        for(Slime slime : debugSlimes)
        {
            slime.setXRot(0);
            slime.setYRot(0);
        }

       timeOfLastSlimeDeletion = SculkHorde.savedData.level.getGameTime();

       for(Slime slime : debugSlimes)
       {
           slime.discard();
       }
       debugSlimes.clear();
    }

    public void glowRed(LivingEntity entity)
    {
        joinTeam(redDebugTeam, entity);
    }

    public void glowYellow(LivingEntity entity)
    {
        joinTeam(yellowDebugTeam, entity);
    }

    public void glowGreen(LivingEntity entity)
    {
        joinTeam(greenDebugTeam, entity);
    }

    public void glowBlue(LivingEntity entity)
    {
        joinTeam(blueDebugTeam, entity);
    }

    public static void renameSlime(Slime slime, String text)
    {
        slime.setCustomName(Component.literal(text));
    }

    private void joinTeam(PlayerTeam team, LivingEntity entity) {
        Scoreboard scoreboard = SculkHorde.savedData.level.getServer().getScoreboard();

        scoreboard.addPlayerToTeam(entity.getStringUUID(), team);
    }
}
