package net.darkhax.bingo.api.effects.collection;

import net.darkhax.bingo.api.team.Team;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * This effect is used to announce when a player/team obtains an item that would complete a
 * goal on the board.
 */
public class CollectionEffectAnnouncement extends CollectionEffect {

    @Override
    public void onItemCollected (ServerPlayerEntity player, ItemStack item, Team team) {

        final ITextComponent playerName = player.getDisplayName();
        playerName.getStyle().withColor(Color.fromLegacyFormat(team.getTeamColorText()));

        final ITextComponent itemName = item.getDisplayName();
        itemName.getStyle().withColor(Color.fromLegacyFormat(TextFormatting.GRAY));

        player.server.getPlayerList().broadcastMessage(new TranslationTextComponent("bingo.player.obtained", playerName, itemName), ChatType.SYSTEM, player.getUUID());
    }
}