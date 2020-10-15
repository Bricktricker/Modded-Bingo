package net.darkhax.bingo.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.darkhax.bingo.ModdedBingo;
import net.darkhax.bingo.api.BingoAPI;
import net.darkhax.bingo.network.PacketSyncGameState;
import net.darkhax.bookshelf.util.CommandUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TranslationTextComponent;

public class CommandBingoStop {

	public static LiteralArgumentBuilder<CommandSource> register() {
		return CommandUtils.createCommand("stop", 2, ctx -> {
			if(!BingoAPI.GAME_STATE.isActive()) {
				throw new SimpleCommandExceptionType(new TranslationTextComponent("command.bingo.stop.nogame")).create();
			}
			BingoAPI.GAME_STATE.end();
			CommandSource source = ctx.getSource();
			
			source.getServer().getPlayerList().func_232641_a_(new TranslationTextComponent("command.bingo.stop.stopped", source.getDisplayName()), ChatType.SYSTEM, null);
			ModdedBingo.NETWORK.sendToAllPlayers(new PacketSyncGameState());
			return 1;
		});
	}
    
}
