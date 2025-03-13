package com.github.sculkhorde.systems.event_system.events;


import com.github.sculkhorde.common.entity.SculkPhantomEntity;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.systems.event_system.Event;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;

import java.util.Random;

public class SpawnPhantomsEvent extends Event {


    public SpawnPhantomsEvent(ResourceKey<net.minecraft.world.level.Level> dimension) {
        super(dimension);
    }

    public static Event createEvent(ResourceKey<net.minecraft.world.level.Level> dimension)
    {
        return new SpawnPhantomsEvent(dimension);
    }

    private void spawnScoutPhantomsAtTopOfWorld(int amount)
    {
        int spawnRange = 100;
        int minimumSpawnRange = 50;
        Random rng = new Random();
        for(int i = 0; i < amount; i++)
        {
            int x = minimumSpawnRange + rng.nextInt(spawnRange) - (spawnRange/2);
            int z = minimumSpawnRange + rng.nextInt(spawnRange) - (spawnRange/2);
            int y = getDimension().getMaxBuildHeight();
            BlockPos spawnPosition = new BlockPos(getEventLocation().getX() + x, y, getEventLocation().getZ() + z);

            SculkPhantomEntity.spawnPhantom(getDimension(), spawnPosition, true);
        }
    }

    @Override
    public void start()
    {
        super.start();
        if (ModConfig.SERVER.should_sculk_nodes_and_raids_spawn_phantoms.get()) {
        	spawnScoutPhantomsAtTopOfWorld(10);
        }
    }
}
