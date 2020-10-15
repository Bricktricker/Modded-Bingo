package net.darkhax.bingo.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.darkhax.bingo.ModdedBingo;
import net.darkhax.bingo.api.team.Team;
import net.darkhax.bingo.data.BingoPersistantData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TranslationTextComponent;

public class CommandBingoTeam {

	public static LiteralArgumentBuilder<CommandSource> register() {
		LiteralArgumentBuilder<CommandSource> builder = Commands.literal("team").requires(sender ->sender.hasPermissionLevel(0));
		
		for(String team : Team.getTeamNames()) {
			builder.then(Commands.literal(team)
				.executes(ctx -> {
					execute(ctx.getSource().asPlayer(), team);
					return 1;
				}));
		}
		
		return builder;
	}
	
	private static void execute(ServerPlayerEntity player, String teamStr) throws CommandSyntaxException {
		final Team team = Team.getTeamByName(teamStr);
		if(team == null) {
			throw new DynamicCommandExceptionType(rl -> new TranslationTextComponent("command.bingo.team.unknown", rl)).create(teamStr);
		}
		
		player.getServer().getPlayerList().func_232641_a_(new TranslationTextComponent("command.bingo.team.change", player.getName(), team.getTeamName()), ChatType.SYSTEM, null);
		BingoPersistantData.setTeam(player, team);
	}
}
