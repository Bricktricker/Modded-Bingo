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

        player.teleportTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        if (this.setSpawn) {
        	player.setRespawnPosition(player.getCommandSenderWorld().dimension(), pos, 0.0F, true, false);
        }

        if (this.fallbackBlock != null && player.level.isEmptyBlock(pos.below())) {

            player.level.setBlockAndUpdate(pos.below(), this.fallbackBlock);
        }
    }
}