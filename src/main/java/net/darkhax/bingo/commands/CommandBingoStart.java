package net.darkhax.bingo.commands;

import net.darkhax.bingo.BingoMod;
import net.darkhax.bookshelf.command.Command;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class CommandBingoStart extends Command {

	@Override
	public String getName() {

		return "start";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		
		return "command.bingo.start.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

		if (!BingoMod.GAME_STATE.isActive()) {
			
			throw new CommandException("command.bingo.info.notactive");
		}
		
		if (BingoMod.GAME_STATE.isHasStarted()) {
			
			throw new CommandException("command.bingo.info.alreadystarted");
		}
		
		BingoMod.GAME_STATE.start();
		server.getPlayerList().sendMessage(new TextComponentTranslation("command.bingo.start.started", sender.getDisplayName()));
	}
}