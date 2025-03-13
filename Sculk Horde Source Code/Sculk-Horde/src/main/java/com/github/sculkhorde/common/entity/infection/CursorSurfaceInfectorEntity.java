package com.github.sculkhorde.common.entity.infection;

import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.systems.infestation_systems.block_infestation_system.BlockInfestationSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import java.util.Random;

import static com.github.sculkhorde.util.BlockAlgorithms.isExposedToInfestationWardBlock;

public class CursorSurfaceInfectorEntity extends CursorEntity{
    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     *
     * @param worldIn The world to initialize this mob in
     */
    public CursorSurfaceInfectorEntity(Level worldIn) {
        this(ModEntities.CURSOR_SURFACE_INFECTOR.get(), worldIn);
    }

    public CursorSurfaceInfectorEntity(EntityType<?> pType, Level pLevel) {
        super(pType, pLevel);
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_20052_) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag p_20139_) {

    }

    /**
     * Returns true if the block is considered a target.
     * @param pos the block position
     * @return true if the block is considered a target
     */
    @Override
    protected boolean isTarget(BlockPos pos)
    {
        return BlockInfestationSystem.isInfectable((ServerLevel) level(), pos);
    }

    /**
     * Transforms the block at the given position.
     * @param pos the position of the block
     */
    @Override
    protected void transformBlock(BlockPos pos)
    {
        BlockInfestationSystem.tryToInfestBlock((ServerLevel) level(), pos);
    }

    /**
     * Returns true if the block is considered obstructed.
     * @param state the block state
     * @param pos the block position
     * @return true if the block is considered obstructed
     */
    @Override
    protected boolean isObstructed(BlockState state, BlockPos pos)
    {
        if(!ModConfig.SERVER.block_infestation_enabled.get())
        {
            return true;
        }
        else if(SculkHorde.savedData.isHordeDefeated())
        {
            return true;
        }
        else if(state.isAir())
        {
            return true;
        }
        // If we detect fluid
        else if(!state.getFluidState().isEmpty())
        {
            // If its water, its only obstructed if its the water source block or flowing water block
            if(state.getFluidState().is(Fluids.WATER) && state.is(Blocks.WATER))
            {
                return true;
            }

            if(!state.getFluidState().is(Fluids.WATER))
            {
                return true;
            }
        }
        else if(isExposedToInfestationWardBlock((ServerLevel) this.level(), pos))
        {
            return true;
        }
        else if(BlockAlgorithms.getBlockDistance(origin, pos) > MAX_RANGE)
        {
            return true;
        }

        // Check if block is not beyond world border
        if(!level().isInWorldBounds(pos))
        {
            return true;
        }

        // This is to prevent the entity from getting stuck in a loop
        if(visitedPositons.containsKey(pos.asLong()))
        {
            return true;
        }

        boolean isBlockNotExposedToAir = !BlockAlgorithms.isExposedToAir((ServerLevel) this.level(), pos);
        boolean isBlockNotSculkArachnoid = !state.is(ModBlocks.SCULK_ARACHNOID.get());
        boolean isBlockNotSculkDuraMatter = !state.is(ModBlocks.SCULK_DURA_MATTER.get());

        if(isBlockNotExposedToAir && isBlockNotSculkArachnoid && isBlockNotSculkDuraMatter)
        {
            return true;
        }

        return false;
    }

    @Override
    protected void spawnParticleEffects()
    {
        Random random = new Random();
        float maxOffset = 2;
        float randomXOffset = random.nextFloat(maxOffset * 2) - maxOffset;
        float randomYOffset = random.nextFloat(maxOffset * 2) - maxOffset;
        float randomZOffset = random.nextFloat(maxOffset * 2) - maxOffset;
        this.level().addParticle(ParticleTypes.SCULK_SOUL, getX() + randomXOffset, getY() + randomYOffset, getZ() + randomZOffset, randomXOffset * 0.1, randomYOffset * 0.1, randomZOffset * 0.1);
    }
}
