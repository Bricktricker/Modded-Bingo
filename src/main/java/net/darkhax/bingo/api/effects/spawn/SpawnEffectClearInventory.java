package net.darkhax.bingo.api.effects.spawn;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

/**
 * This effect will clear the inventory of the player before the game starts.
 */
public class SpawnEffectClearInventory extends SpawnEffect {

    @Override
    public void onPlayerSpawn (ServerPlayerEntity player, BlockPos pos) {

        for (int slot = 0; slot < player.inventory.getContainerSize(); slot++) {
            
            player.inventory.setItem(slot, ItemStack.EMPTY);
        }
    }
}