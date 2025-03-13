package com.github.sculkhorde.systems.infestation_systems.block_infestation_system.infestation_entries;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BlockInfestationTable{

    private List<IBlockInfestationEntry> entries;
    protected boolean denyNonSolidBlocks = true;
    protected float priority = 0F;

    public BlockInfestationTable(float priority, boolean denyNonSolidBlocks)
    {
        entries = new ArrayList<>();
        setDenyNonSolidBlocks(denyNonSolidBlocks);
        this.priority = priority;
    }

    public float getPriority()
    {
        return priority;
    }


    public boolean isDenyNonSolidBlocks() {
    	return denyNonSolidBlocks;
    }

    public void setDenyNonSolidBlocks(boolean denyNonSolidBlocks) {
    	this.denyNonSolidBlocks = denyNonSolidBlocks;
    }

    /**
     * Adds a new entry to the table.
     * @param normalVariant The normal variant of the block.
     * @param infectedVariant The infected variant of the block.
     */
    public void addEntry(float priority, Block normalVariant, BlockState infectedVariant)
    {
        entries.add(new BlockInfestationTableEntry(priority, normalVariant, infectedVariant));
        entries.sort(Comparator.comparing(IBlockInfestationEntry::getPriority));
    }

    public void addEntry(float priority, TagKey<Block> normalTag, ITagInfestedBlock infectedVariant, Block defaultNormalVariant)
    {
        entries.add(new BlockTagInfestationTableEntry(priority, normalTag, infectedVariant, defaultNormalVariant));
        entries.sort(Comparator.comparing(IBlockInfestationEntry::getPriority));
    }

    public void addEntry(float priority, String normalBlockID, String infectedBlockID)
    {
        entries.add(new BlockIDOnlyCurableTableEntry(priority, normalBlockID, infectedBlockID));
        entries.sort(Comparator.comparing(IBlockInfestationEntry::getPriority));
    }

    public void addEntry(float priority, TagKey<Block> toolRequired, Tier tier, ITagInfestedBlock infectedVariant, Block defaultNormalVariant)
    {
        entries.add(new ToolTaglInfestationTableEntry(priority, toolRequired, tier, infectedVariant, defaultNormalVariant));
        entries.sort(Comparator.comparing(IBlockInfestationEntry::getPriority));
    }

    public void addEntry(float priority, TagKey<Block> tag1, TagKey<Block> tag2, Tier tier, ITagInfestedBlock infestedVariant, Block defaultNormalVariant)
    {
        entries.add(new MultiTagInfestationTableEntry(priority, tag1, tag2, tier, infestedVariant, defaultNormalVariant));
        entries.sort(Comparator.comparing(IBlockInfestationEntry::getPriority));
    }

    public void addEntry(ITagInfestedBlock infectedVariant)
    {
        entries.add(new ConfigInfestationTableEntry(infectedVariant));
        entries.sort(Comparator.comparing(IBlockInfestationEntry::getPriority));
    }

    public void addOnlyCurableEntry(float priority, Block normalVariant, Block infectedVariant)
    {
        entries.add(new BlockExplicitCurableTableEntry(priority, normalVariant, infectedVariant));
        entries.sort(Comparator.comparing(IBlockInfestationEntry::getPriority));
    }

    public BlockState getInfestedVariant(Level level, BlockPos blockPos)
    {
        BlockState blockState = level.getBlockState(blockPos);
        for(IBlockInfestationEntry entry : entries)
        {
            if(entry.isNormalVariant(blockState))
            {
                return entry.getInfectedVariant(level, blockPos);
            }
        }
        return null;
    }

    public BlockState getNormalVariant(Level level, BlockPos blockPos)
    {
        BlockState blockState = level.getBlockState(blockPos);
        for(IBlockInfestationEntry entry : entries)
        {
            if(entry.isInfectedVariant(blockState))
            {
                return entry.getNormalVariant(level, blockPos);
            }
        }
        return null;
    }


    /**
     * Checks if a block is a normal variant.
     * @return True if the block is a normal variant.
     */
    public boolean canBeInfectedByThisTable(ServerLevel level, BlockPos pos)
    {
        BlockState blockState = level.getBlockState(pos);

        // If we are denying non-solid blocks, then we need to check if the block is solid.
        boolean areWeDenyingNonSolidBlocks = isDenyNonSolidBlocks();
        boolean isBlockNotSolid = BlockAlgorithms.isNotSolid(level, pos);
        if(areWeDenyingNonSolidBlocks && isBlockNotSolid)
        {
            return false;
        }

        for(IBlockInfestationEntry entry : entries)
        {
            if(entry.isNormalVariant(blockState))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * This Method serves the purpose of converting a victim block into a dormant variant.
     * This is only a temporary method until I fully design the new infestation system.
     * @param world The world of the block.
     * @param targetPos The position of the block we are trying to convert.
     */
    public boolean infectBlock(ServerLevel world, BlockPos targetPos)
    {
        if(world == null || !canBeInfectedByThisTable(world, targetPos))
        {
            return false;
        }

        BlockState oldBlock = world.getBlockState(targetPos);
        BlockState newBlock = getInfestedVariant(world, targetPos);

        if(newBlock == null)
        {
            return false;
        }

        world.setBlockAndUpdate(targetPos, newBlock);
        world.playSound(null, targetPos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 2.0F, 0.6F + world.getRandom().nextFloat() * 0.4F);

        if(newBlock.getBlock() instanceof ITagInfestedBlock tagInfestedBlock)
        {
            ITagInfestedBlockEntity tagInfestedBlockEntity = tagInfestedBlock.getTagInfestedBlockEntity(world, targetPos);
            if(tagInfestedBlockEntity == null)
            {
                return false;
            }

            tagInfestedBlockEntity.setNormalBlockState(oldBlock);
        }

        SculkHorde.statisticsData.incrementTotalBlocksInfested();

        return true;
    }
}
