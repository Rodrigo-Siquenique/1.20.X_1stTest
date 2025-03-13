package com.github.sculkhorde.common.pools;

import com.github.sculkhorde.core.ModConfig;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * This class is used to randomly select a block from a given pool. <br>
 * NOTE: Weights need to be added from small to big.
 */
public class PoolBlocks {

    int totalWeight; //The sum of all weights
    ArrayList<PoolEntry> entries; //the array list of all entries

    /**
     * Default Constructor
     */
    public PoolBlocks()
    {
        totalWeight = 0;
        entries = new ArrayList<PoolEntry>();
    }

    /**
     * Adds an entrry
     * @param blockIn the block
     * @param weightIn the weight of the entry
     */
    public void addEntry(Block blockIn, int weightIn)
    {
        totalWeight += weightIn;
        PoolEntry poolEntry = new PoolEntry(blockIn, weightIn);
        entries.add(poolEntry);
        Collections.sort(entries);
    }

    public void addExperimentalEntry(Block blockIn, int weightIn)
    {
        totalWeight += weightIn;
        PoolEntry poolEntry = new PoolEntry(blockIn, weightIn);
        poolEntry.requireExperimentalMode();
        entries.add(poolEntry);
        Collections.sort(entries);
    }

    /**
     * Returns a random entry.
     * @return a random block.
     * <br> Learned from: https://softwareengineering.stackexchange.com/questions/150616/get-weighted-random-item
     */
    public Block getRandomEntry()
    {
        int randomValue = new Random().nextInt(totalWeight);
        int cumulativeSum = 0;
        for(PoolEntry entry : entries)
        {
            if(entry.doesRequireExperimentalMode() && !ModConfig.isExperimentalFeaturesEnabled())
            {
                continue;
            }

            cumulativeSum += entry.weight;
            if(cumulativeSum >= randomValue)
                return entry.block;
        }
        return entries.get(entries.size()-1).block; //If all else fails, just return most rare item
    }

}

class PoolEntry implements Comparable<PoolEntry>{

    protected Block block;
    protected int weight;

    protected boolean requiresExperimentalMode = false;

    /**
     * Default Constructor
     */
    public PoolEntry(Block blockIn, int weightIn)
    {
        block = blockIn;
        weight = weightIn;
    }

    public void requireExperimentalMode()
    {
        requiresExperimentalMode = true;
    }

    public boolean doesRequireExperimentalMode()
    {
        return requiresExperimentalMode;
    }

    @Override
    public int compareTo(PoolEntry other) {
        // Compare based on weight in ascending order
        return Integer.compare(this.weight, other.weight);
    }
}
