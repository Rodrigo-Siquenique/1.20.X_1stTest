package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.systems.infestation_systems.node_infestation.DevNodeBranchingInfestationSystem;
import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.core.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.TimeUnit;

public class DevMassInfectinator3000BlockEntity extends BlockEntity
{

    private long tickedAt = System.nanoTime();

    public static final int tickIntervalSeconds = 1;

    private DevNodeBranchingInfestationSystem infectionHandler1;
    private DevNodeBranchingInfestationSystem infectionHandler2;
    private DevNodeBranchingInfestationSystem infectionHandler3;
    private DevNodeBranchingInfestationSystem infectionHandler4;
    private DevNodeBranchingInfestationSystem infectionHandler5;
    private DevNodeBranchingInfestationSystem infectionHandler6;
    private DevNodeBranchingInfestationSystem infectionHandler7;
    private DevNodeBranchingInfestationSystem infectionHandler8;

    public DevMassInfectinator3000BlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.DEV_MASS_INFECTINATOR_3000_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    /** Accessors **/

    /** Modifiers **/

    /** Events **/

    private void initializeInfectionHandler()
    {
        if(infectionHandler1 == null)
        {
            infectionHandler1 = new DevNodeBranchingInfestationSystem(this, getBlockPos());
        }
        if(infectionHandler2 == null)
        {
            infectionHandler2 = new DevNodeBranchingInfestationSystem(this, getBlockPos());
        }
        if(infectionHandler3 == null)
        {
            infectionHandler3 = new DevNodeBranchingInfestationSystem(this, getBlockPos());
        }
        if(infectionHandler4 == null)
        {
            infectionHandler4 = new DevNodeBranchingInfestationSystem(this, getBlockPos());
        }
        if(infectionHandler5 == null)
        {
            infectionHandler5 = new DevNodeBranchingInfestationSystem(this, getBlockPos());
        }
        if(infectionHandler6 == null)
        {
            infectionHandler6 = new DevNodeBranchingInfestationSystem(this, getBlockPos());
        }
        if(infectionHandler7 == null)
        {
            infectionHandler7 = new DevNodeBranchingInfestationSystem(this, getBlockPos());
        }
        if(infectionHandler8 == null)
        {
            infectionHandler8 = new DevNodeBranchingInfestationSystem(this, getBlockPos());
        }
    }
    public static void tick(Level level, BlockPos blockPos, BlockState blockState, DevMassInfectinator3000BlockEntity blockEntity)
    {
        if(level.isClientSide || !ModConfig.SERVER.block_infestation_enabled.get())
        {
            return;
        }

        // Initialize the infection handler
        if(blockEntity.infectionHandler1 == null || blockEntity.infectionHandler2 == null || blockEntity.infectionHandler3 == null || blockEntity.infectionHandler4 == null || blockEntity.infectionHandler5 == null || blockEntity.infectionHandler6 == null || blockEntity.infectionHandler7 == null || blockEntity.infectionHandler8 == null)
        {
            blockEntity.initializeInfectionHandler();
        }

        long timeElapsed = TimeUnit.SECONDS.convert(System.nanoTime() - blockEntity.tickedAt, TimeUnit.NANOSECONDS);

        // If the time elapsed is less than the tick interval, return
        if(timeElapsed < tickIntervalSeconds) { return; }

        blockEntity.infectionHandler1.tick();
        blockEntity.infectionHandler2.tick();
        blockEntity.infectionHandler3.tick();
        blockEntity.infectionHandler4.tick();
        blockEntity.infectionHandler5.tick();
        blockEntity.infectionHandler6.tick();
        blockEntity.infectionHandler7.tick();
        blockEntity.infectionHandler8.tick();

        // Update the tickedAt time
        blockEntity.tickedAt = System.nanoTime();

    }
}
