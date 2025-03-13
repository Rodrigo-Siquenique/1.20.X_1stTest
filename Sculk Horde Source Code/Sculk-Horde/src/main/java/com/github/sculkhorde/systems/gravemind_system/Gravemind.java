package com.github.sculkhorde.systems.gravemind_system;


import com.github.sculkhorde.common.blockentity.SculkNodeBlockEntity;
import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.gravemind_system.entity_factory.EntityFactory;
import com.github.sculkhorde.systems.gravemind_system.entity_factory.ReinforcementRequest;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;

import java.util.*;

import static com.github.sculkhorde.systems.gravemind_system.entity_factory.EntityFactoryEntry.StrategicValues.Combat;
import static com.github.sculkhorde.systems.gravemind_system.entity_factory.EntityFactoryEntry.StrategicValues.Infector;

/**
 * This class represents the logistics for the Gravemind and is SEPARATE from the physical version.
 * The gravemind is a state machine that is used to coordinate the sculk hoard.
 * Right now only controls the reinforcement system.
 *
 * Future Plans:
 * -Controls Sculk Raids
 * -Coordinate Defense
 * -Make Coordination of Reinforcements more advanced
 */
public class Gravemind
{
    public static enum evolution_states {Undeveloped, Immature, Mature}

    private evolution_states evolution_state;

    //This controls the reinforcement system.
    public static EntityFactory entityFactory;
    //This is a list of all known positions of sculkNodes.
    //We do not want to put them too close to each other.
    public static final int MINIMUM_DISTANCE_BETWEEN_NODES = 300;
    public int sculk_node_limit = 1;

    public static int TICKS_BETWEEN_NODE_SPAWNS = TickUnits.convertMinutesToTicks(ModConfig.SERVER.sculk_node_spawn_cooldown_minutes.get());

    /**
     * Default Constructor <br>
     * Called in ForgeEventSubscriber.java in world load event. <br>
     * WARNING: DO NOT CALL THIS FUNCTION UNLESS THE WORLD IS LOADED
     */
    public Gravemind()
    {
        evolution_state = evolution_states.Undeveloped;
        entityFactory = SculkHorde.entityFactory;
        calulateCurrentState();
    }

    public evolution_states getEvolutionState()
    {
        return evolution_state;
    }



    // Accessors

    /**
     * Used to figure out what state the gravemind is in. Called periodically. <br>
     * Useful for when world is loaded in because we dont store the state.
     */
    public void calulateCurrentState()
    {

        //This is how much mass is needed to go from undeveloped to immature
        int MASS_GOAL_FOR_IMMATURE = ModConfig.SERVER.gravemind_mass_goal_for_immature_stage.get();
        //This is how much mass is needed to go from immature to mature
        int MASS_GOAL_FOR_MATURE = ModConfig.SERVER.gravemind_mass_goal_for_mature_stage.get();

        if(SculkHorde.savedData.getSculkAccumulatedMass() >= MASS_GOAL_FOR_MATURE)
        {
            evolution_state = evolution_states.Mature;
            sculk_node_limit = 8;
        }
        else if(SculkHorde.savedData.getSculkAccumulatedMass() >= MASS_GOAL_FOR_IMMATURE)
        {
            evolution_state = evolution_states.Immature;
            sculk_node_limit = 4;
            if(SculkHorde.savedData.isHordeUnactivated())
            {
                SculkHorde.savedData.setHordeState(ModSavedData.HordeState.ACTIVE);
            }
        }

    }

    public void advanceState()
    {
        if(evolution_state == evolution_states.Undeveloped)
        {
            SculkHorde.savedData.setSculkAccumulatedMass(ModConfig.SERVER.gravemind_mass_goal_for_immature_stage.get());
            calulateCurrentState();
        }
        else if(evolution_state == evolution_states.Immature)
        {
            SculkHorde.savedData.setSculkAccumulatedMass(ModConfig.SERVER.gravemind_mass_goal_for_mature_stage.get());
            calulateCurrentState();
        }
    }

    public void deadvanceState()
    {
        if(evolution_state == evolution_states.Immature)
        {
            SculkHorde.savedData.setSculkAccumulatedMass(ModConfig.SERVER.gravemind_mass_goal_for_immature_stage.get()/2);
        }
        else if(evolution_state == evolution_states.Mature)
        {
            SculkHorde.savedData.setSculkAccumulatedMass(ModConfig.SERVER.gravemind_mass_goal_for_mature_stage.get()/2);

        }
        calulateCurrentState();
    }

    public void resetGravemindState()
    {
        SculkHorde.savedData.setSculkAccumulatedMass(0);
        calulateCurrentState();
    }


    public void processReinforcementRequest(ReinforcementRequest context)
    {
        context.isRequestViewed = true;


        boolean isSenderDeveloper = context.sender == ReinforcementRequest.senderType.Developer;
        boolean isSenderSculkMassBlock = context.sender == ReinforcementRequest.senderType.SculkMass;
        boolean isThereNoMass = SculkHorde.savedData.getSculkAccumulatedMass() <= 0;
        boolean isHordeDefeated = SculkHorde.savedData.isHordeDefeated();

        //Auto approve is this reinforcement is requested by a developer or sculk mass
        if(isSenderDeveloper || isSenderSculkMassBlock)
        {
            context.isRequestApproved = true;
        }

        if(isHordeDefeated || isThereNoMass)
        {
            return;
        }

        boolean isSenderTypeSummoner = context.sender == ReinforcementRequest.senderType.Summoner;
        boolean isSenderTypeMassBlock = context.sender == ReinforcementRequest.senderType.SculkMass;
        boolean isThereAtLeastOneSpawnPoint = context.positions.length > 0;
        boolean isThereSculkNodesInExistence = !SculkHorde.savedData.getNodeEntries().isEmpty();

        if( (isSenderTypeSummoner || isSenderTypeMassBlock) && isThereAtLeastOneSpawnPoint && isThereSculkNodesInExistence)
        {
            BlockPos nodeBlockPos = SculkHorde.savedData.getClosestNodeEntry(context.dimension, context.positions[0]).getPosition();
            Optional<SculkNodeBlockEntity> nodeBlockEntity = SculkHorde.savedData.level.getBlockEntity(nodeBlockPos, ModBlockEntities.SCULK_NODE_BLOCK_ENTITY.get());
            if(nodeBlockEntity.isPresent())
            {
                if(SculkHorde.populationHandler.isPopulationAtMax())
                {
                    context.isRequestApproved = false;
                    return;
                }
            }
        }

        //Spawn Combat Mobs to deal with player
        if(context.is_aggressor_nearby)
        {
            context.approvedStrategicValues.add(Combat);
            context.isRequestApproved = true;
        }

        //Spawn infector mobs to infect
        //NOTE: I turned this into an else if because is both aggressors and passives are present,
        //it will choose from both combat and infector units. I think its better we prioritize
        //spawning aggressors if both are present
        else if(context.is_non_sculk_mob_nearby)
        {
            context.approvedStrategicValues.add(Infector);
            context.isRequestApproved = true;
        }
    }

    /**
     * Determines if a given evolution state is equal to or below the current evolution state.
     * @param stateIn The given state to check
     * @return True if the given state is equal to or less than current evolution state.
     */
    public boolean isEvolutionStateEqualOrLessThanCurrent(evolution_states stateIn)
    {
        if(evolution_state == evolution_states.Undeveloped)
        {
            return (stateIn == evolution_states.Undeveloped);
        }
        else if(evolution_state == evolution_states.Immature)
        {
            return (stateIn == evolution_states.Immature || stateIn == evolution_states.Undeveloped);
        }
        else if(evolution_state == evolution_states.Mature)
        {
            return(stateIn == evolution_states.Undeveloped
                || stateIn == evolution_states.Immature
                || stateIn == evolution_states.Mature);
        }
        return false;
    }

    public boolean isEvolutionInMatureState()
    {
        return evolution_state == evolution_states.Mature;
    }
}
