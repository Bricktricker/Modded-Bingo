package net.darkhax.bingo.api.effects.spawn;

import com.google.gson.annotations.Expose;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

/**
 * This effect is used to randomize the spawn of all players when the game has started.
 */
public class SpawnEffectMovePlayer extends SpawnEffect {

    /**
     * Whether or not the spawn point should be changed.
     */
    @Expose
    private boolean setSpawn;

    /**
     * A block to place below the player if there is no solid blocks at this position.
     */
    @Expose
    private BlockState fallbackBlock;


    @Override
    public void onPlayerSpawn (ServerPlayerEntity player, BlockPos pos) {

        player.setPositionAndUpdate(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        if (this.setSpawn) {
        	player.func_242111_a(player.getEntityWorld().getDimensionKey(), pos, 0.0F, true, false);
        }

        if (this.fallbackBlock != null && player.world.isAirBlock(pos.down())) {

            player.world.setBlockState(pos.down(), this.fallbackBlock);
        }
    }
}