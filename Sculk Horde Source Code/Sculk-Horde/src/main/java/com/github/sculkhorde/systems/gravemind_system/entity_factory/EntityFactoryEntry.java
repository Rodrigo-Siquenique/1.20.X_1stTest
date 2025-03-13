package com.github.sculkhorde.systems.gravemind_system.entity_factory;

import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.gravemind_system.Gravemind;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.Random;

/**
 * This class is only used in the EntityFactory class which stores a list
 * of these entries. It is simply to store an EntityType and how much
 * sculk mass is required to spawn it.
 */
public class EntityFactoryEntry {

    Random rng = new Random();

    public enum StrategicValues {Combat, Infector, Melee, Ranged, Boss, Support, Tank, EffectiveInSkies, Aquatic, EffectiveOnGround}

    private int orderCost;
    private EntityType<Mob> entity;
    private int squad_limit = Integer.MAX_VALUE;

    private float chanceToSpawn = 1.0F;

    private StrategicValues[] strategicValues = new StrategicValues[]{};

    private ReinforcementRequest.senderType explicitDeniedSenders[] = new ReinforcementRequest.senderType[]{};
    private Gravemind.evolution_states minEvolutionRequired = Gravemind.evolution_states.Undeveloped;

    boolean experimentalMode = false;
    private ForgeConfigSpec.ConfigValue<Boolean> requiredConfig = ModConfig.SERVER.experimental_features_enabled;

    public EntityFactoryEntry(EntityType entity)
    {
        this.entity = entity;
    }

    public EntityType<Mob> getEntity()
    {
        return entity;
    }

    // Getters and Setters
    public EntityFactoryEntry setCost(int cost)
    {
        orderCost = cost;
        return this;
    }

    public int getCost()
    {
        return orderCost;
    }

    public boolean getChanceToSpawn()
    {
        return rng.nextFloat() <= chanceToSpawn;
    }

    public EntityFactoryEntry setChanceToSpawn(float value)
    {
        chanceToSpawn = value;
        return this;
    }

    public EntityFactoryEntry setLimit(int limit)
    {
        this.squad_limit = limit;
        return this;
    }

    public EntityFactoryEntry enableExperimentalMode(ForgeConfigSpec.ConfigValue<Boolean> configOptionThatNeedsToBeTrue)
    {
        experimentalMode = true;
        requiredConfig = configOptionThatNeedsToBeTrue;
        return this;
    }

    public int getLimit()
    {
        return squad_limit;
    }

    public EntityFactoryEntry addStrategicValues(StrategicValues... values)
    {
        strategicValues = values;
        return this;
    }


    public EntityFactoryEntry setExplicitlyDeniedSenders(ReinforcementRequest.senderType... deniedSenders)
    {
        explicitDeniedSenders = deniedSenders;
        return this;
    }


    public EntityFactoryEntry setMinEvolutionRequired(Gravemind.evolution_states minEvolutionRequired)
    {
        this.minEvolutionRequired = minEvolutionRequired;
        return this;
    }

    public boolean isSenderExplicitlyDenied(ReinforcementRequest.senderType sender)
    {
        for(ReinforcementRequest.senderType deniedSender : explicitDeniedSenders)
        {
            if(deniedSender == sender)
            {
                return true;
            }
        }

        return false;
    }

    public boolean isEntryAppropriateMinimalCheck()
    {
        if(getCost() > SculkHorde.savedData.getSculkAccumulatedMass())
        {
            return false;
        }
        else if(!SculkHorde.gravemind.isEvolutionStateEqualOrLessThanCurrent(minEvolutionRequired))
        {
            return false;
        }
        else if(!getChanceToSpawn())
        {
            return false;
        }

        return true;
    }

    public boolean doesEntityContainNeededStrategicValue(StrategicValues requiredValue)
    {
        for(StrategicValues entityValue : strategicValues)
        {
            if(entityValue == requiredValue)
            {
                return true;
            }
        }

        return false;
    }

    public boolean doesEntityContainNeededStrategicValues(ArrayList<StrategicValues> requiredValues)
    {
        int amountOfValuesNeeded = requiredValues.size();
        int amountOfValuesUnitHasFromRequirement = 0;

        for(StrategicValues value : requiredValues)
        {
            for(StrategicValues entityValue : strategicValues)
            {
                if(entityValue == value)
                {
                    amountOfValuesUnitHasFromRequirement++;
                }
            }
        }

        return amountOfValuesNeeded == amountOfValuesUnitHasFromRequirement;
    }

    public boolean doesEntityContainAnyDeniedStrategicValues(ArrayList<StrategicValues>  deniedValues)
    {
        for(StrategicValues value : deniedValues)
        {
            for(StrategicValues entityValue : strategicValues)
            {
                if(entityValue == value)
                {
                    return true;
                }
            }
        }

        return false;
    }

     public boolean isEntryAppropriate(ReinforcementRequest context)
    {
        if(context == null)
        {
            return false;
        }

        boolean isOverBudget = getCost() > context.budget && context.budget != -1;
        boolean doesHordeNotHaveEnoughMass = getCost() >= SculkHorde.savedData.getSculkAccumulatedMass();
        boolean isSenderExplicitlyDenied = isSenderExplicitlyDenied(context.sender);
        boolean isEvolutionStateNotMet = !SculkHorde.gravemind.isEvolutionStateEqualOrLessThanCurrent(minEvolutionRequired);
        boolean doesEntityNotContainNeededStrategicValues = !doesEntityContainNeededStrategicValues(context.approvedStrategicValues);
        boolean doesEntityContainBannedStrategicValues = doesEntityContainAnyDeniedStrategicValues(context.deniedStrategicValues);
        boolean doesRequestSpecifyAnyApprovedMobTypes = !context.approvedStrategicValues.isEmpty();

        if(doesHordeNotHaveEnoughMass || isOverBudget)
        {
            return false;
        }
        else if(experimentalMode && (!ModConfig.SERVER.experimental_features_enabled.get() || !requiredConfig.get()))
        {
            return false;
        }
        else if(doesEntityNotContainNeededStrategicValues && doesRequestSpecifyAnyApprovedMobTypes)
        {
            return false;
        }
        else if(doesEntityContainBannedStrategicValues)
        {
            return false;
        }
        else if(isEvolutionStateNotMet)
        {
            return false;
        }
        else if(isSenderExplicitlyDenied)
        {
            return false;
        }
        else if(!getChanceToSpawn())
        {
            return false;
        }

        return true;
    }

    /**
     * Will spawn entity and subtract the cost of spawning it.
     * @param level The level to spawn the entity in
     * @param pos The position to spawn the entity at
     */
    public Mob spawnEntity(ServerLevel level, BlockPos pos)
    {
        SculkHorde.savedData.subtractSculkAccumulatedMass(getCost());
        SculkHorde.statisticsData.incrementTotalUnitsSpawned();
        return getEntity().spawn(level, pos, MobSpawnType.EVENT);
    }
}
