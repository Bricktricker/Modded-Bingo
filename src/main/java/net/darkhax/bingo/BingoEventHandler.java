package net.darkhax.bingo;

import net.darkhax.bingo.api.BingoAPI;
import net.darkhax.bingo.network.PacketSyncGameState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
public class BingoEventHandler {

    @SubscribeEvent
    public static void onPlayerPickupItem (ItemPickupEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayerEntity && BingoAPI.GAME_STATE.hasStarted()) {
            BingoAPI.GAME_STATE.onPlayerPickupItem((ServerPlayerEntity) event.getEntityLiving(), event.getOriginalEntity().getItem());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn (PlayerLoggedInEvent event) {
        // When a player connects to the server, sync their client data with the server's data.
        if (event.getEntityLiving() instanceof ServerPlayerEntity) {
           ModdedBingo.NETWORK.sendToPlayer((ServerPlayerEntity) event.getEntityLiving(), new PacketSyncGameState());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick (TickEvent.PlayerTickEvent event) {

        // Only check once a second
        if (event.player.tickCount % 20 == 0 && event.player instanceof ServerPlayerEntity) {

            for (final ItemStack stack : event.player.inventory.items) {

                if (!stack.isEmpty()) {

                    BingoAPI.GAME_STATE.onPlayerPickupItem((ServerPlayerEntity) event.player, stack);
                }
            }
        }
    }
}