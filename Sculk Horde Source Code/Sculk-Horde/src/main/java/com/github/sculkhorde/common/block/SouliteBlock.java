package com.github.sculkhorde.common.block;

import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.extensions.IForgeBlock;

public class SouliteBlock extends HalfTransparentBlock implements IForgeBlock {

    /**
     * HARDNESS determines how difficult a block is to break<br>
     * 0.6f = dirt<br>
     * 1.5f = stone<br>
     * 2f = log<br>
     * 3f = iron ore<br>
     * 50f = obsidian
     */
    public static float HARDNESS = 2f;

    /**
     * BLAST_RESISTANCE determines how difficult a block is to blow up<br>
     * 0.5f = dirt<br>
     * 2f = wood<br>
     * 6f = cobblestone<br>
     * 1,200f = obsidian
     */
    public static float BLAST_RESISTANCE = 2f;


    /**
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public SouliteBlock(Properties prop) {
        super(prop);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public SouliteBlock() {
        this(getProperties());
    }

    /**
     * Determines the properties of a block.<br>
     * I made this in order to be able to establish a block's properties from within the block class and not in the BlockRegistry.java
     * @return The Properties of the block
     */
    public static Properties getProperties()
    {
        Properties prop = Properties.copy(Blocks.STONE)
                .mapColor(MapColor.COLOR_CYAN)
                .strength(HARDNESS, BLAST_RESISTANCE)//Hardness & Resistance
                .sound(SoundType.HONEY_BLOCK)
                .destroyTime(5f)
                .sound(SoundType.AMETHYST)
                .noOcclusion()
                .lightLevel((value) -> 15);
        return prop;
    }

    public boolean propagatesSkylightDown(BlockState p_48740_, BlockGetter p_48741_, BlockPos p_48742_) {
        return true;
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHitResult) {
        if(player.getItemInHand(hand).isEmpty())
        {
            return InteractionResult.FAIL;
        }

        if(player.getItemInHand(hand).is(ModItems.PURE_SOULS.get()))
        {
            level.setBlockAndUpdate(pos, ModBlocks.DEPLETED_SOULITE_BLOCK.get().defaultBlockState());
            BuddingSouliteBlock.spawnSouliteClusters(level, pos);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
