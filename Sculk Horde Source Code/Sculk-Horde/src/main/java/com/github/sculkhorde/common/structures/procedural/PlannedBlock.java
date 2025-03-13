package com.github.sculkhorde.common.structures.procedural;

import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.util.BlockAlgorithms;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.function.Predicate;

public class PlannedBlock
{
    protected BlockState plannedBlock = null;
    protected BlockPos targetPos;
    protected ServerLevel world;

    /**
     * Constructor
     * @param worldIn
     * @param plannedBlockIn
     * @param targetPosIn
     */
    public PlannedBlock(ServerLevel worldIn, BlockState plannedBlockIn, BlockPos targetPosIn)
    {
        plannedBlock = plannedBlockIn;
        targetPos = targetPosIn;
        world = worldIn;
    }

    /**
     * Represents a predicate (boolean-valued function) of one argument. <br>
     * Determines if a block can be replaced in the building process
     */
    protected final Predicate<BlockState> VALID_BLOCKS_TO_REPLACE = (validBlocksPredicate) ->
    {
        // Explicit Deny List
        if(validBlocksPredicate.is(BlockTags.NEEDS_DIAMOND_TOOL)
        || validBlocksPredicate.is(BlockTags.WITHER_IMMUNE)
        || validBlocksPredicate.is(BlockTags.DRAGON_IMMUNE))
        {
            return false;
        }
        // Explicit Allow
        if(validBlocksPredicate.is(ModBlocks.BlockTags.INFESTED_BLOCK)
                || validBlocksPredicate.is(BlockTags.REPLACEABLE)
                || validBlocksPredicate.is(BlockTags.NEEDS_IRON_TOOL)
                || validBlocksPredicate.is(BlockTags.NEEDS_STONE_TOOL)
                || !validBlocksPredicate.requiresCorrectToolForDrops()
                || validBlocksPredicate.getDestroySpeed(world, targetPos) <= 3.0F
                || validBlocksPredicate.getBlock().equals(Blocks.AIR)
                || validBlocksPredicate.getBlock().equals(Blocks.CAVE_AIR))

        {
            return true;
        }
        return false;
    };

    /**
     * Outputs if the block we are trying to place, is able to be placed at a location
     * @return True if able to place, false otherwise.
     */
    public boolean canBePlaced()
    {
        return BlockAlgorithms.isDestroyableByStructures(world, targetPos);
    }

    /**
     * Outputs if the block has been placed
     * @return True if placed, false otherwise.
     */
    public boolean isPlaced()
    {
        return world.getBlockState(targetPos).is(plannedBlock.getBlock());
    }

    /**
     * If able, will place the block in the world.
     */
    public void build()
    {
        //If we 1n replace the block at the location
        if(canBePlaced())
        {
            world.setBlockAndUpdate(targetPos, plannedBlock);
        }
        else
        {
            //Very Bad
        }
    }

    public BlockPos getPosition()
    {
        return targetPos;
    }

}
