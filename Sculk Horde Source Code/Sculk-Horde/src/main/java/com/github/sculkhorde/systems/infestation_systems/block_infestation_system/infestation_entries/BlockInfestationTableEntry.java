package com.github.sculkhorde.systems.infestation_systems.block_infestation_system.infestation_entries;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockInfestationTableEntry implements IBlockInfestationEntry
{
    protected Block normalVariant;
    protected BlockState infectedVariant;

    protected float priority = 0;

    @Override
    public float getPriority() {
        return priority;
    }

    // This is needed to tell java that the properties are of the same type
    private static <T extends Comparable<T>> BlockState copyBlockProperty(BlockState from, BlockState to, Property<T> property) {
    	// copy only if from and to have the same Property (this solution is a little bit hacky but I don't no a better way)
    	try {
    		return to.setValue(property, from.getValue(property));
    	} catch(IllegalArgumentException e) {
    		return to;
    	}
    }
    
    // Default constructor
    public BlockInfestationTableEntry(float priority, Block normalVariantIn, BlockState infectedVariantIn)
    {
        normalVariant = normalVariantIn;
        infectedVariant = infectedVariantIn;
        this.priority = priority;
    }

    public boolean isNormalVariant(BlockState blockState)
    {
        return blockState.is(normalVariant);
    }

    public boolean isInfectedVariant(BlockState blockState)
    {
        return infectedVariant.is(blockState.getBlock());
    }

    public BlockState getNormalVariant(Level level, BlockPos blockPos)
    {
        return getNormalVariant(level.getBlockState(blockPos));
    }

    public BlockState getInfectedVariant(Level level, BlockPos blockPos)
    {
        return getInfectedVariant(level.getBlockState(blockPos));
    }

    public BlockState getNormalVariant(BlockState blockState)
    {
    	// In this case we need to copy all the properties again
    	BlockState normalState = normalVariant.defaultBlockState();
    	
    	for(Property<?> prop : blockState.getProperties()) {
    		normalState = copyBlockProperty(blockState, normalState, prop);
    	}
    	
        return normalState;
    }

    public BlockState getInfectedVariant(BlockState blockState)
    {
    	// copy block properties of normal block to infected block
    	for(Property<?> prop : blockState.getProperties()) {
    		this.infectedVariant = copyBlockProperty(blockState, this.infectedVariant, prop);
    	}
    	
        return this.infectedVariant;
    }
}