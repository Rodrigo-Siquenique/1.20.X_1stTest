package com.github.sculkhorde.common.block;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.sculkhorde.systems.infestation_systems.block_infestation_system.infestation_entries.ITagInfestedBlock;
import com.github.sculkhorde.systems.infestation_systems.block_infestation_system.infestation_entries.ITagInfestedBlockEntity;
import com.github.sculkhorde.common.blockentity.InfestedTagBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlock;

public class InfestedStairBlock extends StairBlock implements EntityBlock, IForgeBlock, ITagInfestedBlock {
	
	public InfestedStairBlock(Properties properties) {
		this(() -> InfestedStairBlock.stateById(0), properties);
	}

	public InfestedStairBlock(Supplier<BlockState> state, Properties properties) {
		super(state, properties);
	}
	
	/* Properties from BaseEntityBlock */
	
	// Copy default event triggering (not entirely sure if this is actually used)
    public boolean triggerEvent(BlockState p_49226_, Level p_49227_, BlockPos p_49228_, int p_49229_, int p_49230_) {
    	super.triggerEvent(p_49226_, p_49227_, p_49228_, p_49229_, p_49230_);
    	BlockEntity blockentity = p_49227_.getBlockEntity(p_49228_);
    	return blockentity == null ? false : blockentity.triggerEvent(p_49229_, p_49230_);
    }
    
    // Copy entity ticking
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> p_152133_, BlockEntityType<E> p_152134_, BlockEntityTicker<? super E> p_152135_) {
       return p_152134_ == p_152133_ ? (BlockEntityTicker<A>)p_152135_ : null;
    }
	
	/* Properties from InfestedTagBlock */
	@Override
    public boolean isRandomlyTicking(BlockState blockState) {
        return false;
    }

	@Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState state) {
        return new InfestedTagBlockEntity(blockPos, state);
    }
	
	@Override
    public ITagInfestedBlockEntity getTagInfestedBlockEntity(Level level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if(blockEntity instanceof ITagInfestedBlockEntity)
        {
            return (ITagInfestedBlockEntity) blockEntity;
        }
        return null;
    }
}
