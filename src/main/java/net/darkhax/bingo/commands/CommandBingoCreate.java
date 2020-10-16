package net.darkhax.bingo.commands;

import java.util.Random;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.darkhax.bingo.ModdedBingo;
import net.darkhax.bingo.api.BingoAPI;
import net.darkhax.bingo.api.game.GameMode;
import net.darkhax.bingo.network.PacketSyncGameState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class CommandBingoCreate {

	public static LiteralArgumentBuilder<CommandSource> register() {
		LiteralArgumentBuilder<CommandSource> builder = Commands.literal("create").requires(sender ->sender.hasPermissionLevel(2));
		
				builder.then(Commands.argument("gamemode", ResourceLocationArgument.resourceLocation())
						.executes(ctx -> {
							execute(ctx.getSource(), ResourceLocationArgument.getResourceLocation(ctx, "gamemode"), true, false, -1);
							return 1;
						})
						.then(Commands.argument("groupTeam", BoolArgumentType.bool())
								.executes(ctx -> {
									execute(ctx.getSource(), ResourceLocationArgument.getResourceLocation(ctx, "gamemode"), BoolArgumentType.getBool(ctx, "groupTeam"), false, -1);
									return 1;
								})
								.then(Commands.argument("blackout", BoolArgumentType.bool())
										.executes(ctx -> {
											execute(ctx.getSource(), ResourceLocationArgument.getResourceLocation(ctx, "gamemode"), BoolArgumentType.getBool(ctx, "groupTeam"), BoolArgumentType.getBool(ctx, "blackout"), -1);
											return 1;
										})
										.then(Commands.argument("seed", IntegerArgumentType.integer(0))
												.executes(ctx -> {
													execute(ctx.getSource(), ResourceLocationArgument.getResourceLocation(ctx, "gamemode"), BoolArgumentType.getBool(ctx, "groupTeam"), BoolArgumentType.getBool(ctx, "blackout"), ctx.getArgument("seed", Integer.class));
													return 1;
												})
										)
								)
						)
					);
		
		builder.executes(ctx -> {
			execute(ctx.getSource(), new ResourceLocation("bingo:default"), true, false, -1);
			return 1;
		});
		return builder;
	}
	
	private static void execute(CommandSource source, ResourceLocation gamemodeRL, boolean groupTeam, boolean blackout, int seed) throws CommandSyntaxException {		
		final GameMode gameMode = BingoAPI.getGameMode(gamemodeRL);
		if(gameMode == null) {
			throw new DynamicCommandExceptionType(rl -> new TranslationTextComponent("command.bingo.create.unknown", rl)).create(gamemodeRL);
		}
		
		Random random = seed == -1 ? new Random() : new Random(seed);
		
		if(BingoAPI.GAME_STATE.hasStarted() || BingoAPI.GAME_STATE.isActive()) {
			BingoAPI.GAME_STATE.end();
			sendToAll(source, new TranslationTextComponent("command.bingo.stop.stopped", source.getDisplayName()));
			ModdedBingo.NETWORK.sendToAllPlayers(new PacketSyncGameState());
		}
		
		BingoAPI.GAME_STATE.create(random, gameMode, groupTeam, blackout);
		sendToAll(source, new TranslationTextComponent("command.bingo.create.announce"  + (blackout ? ".blackout" : ""), source.getDisplayName()));
		ModdedBingo.NETWORK.sendToAllPlayers(new PacketSyncGameState());
	}
	
	private static void sendToAll(CommandSource source, ITextComponent component) {
		Entity entity = source.getEntity();
		if(entity == null) {
			source.getServer().getPlayerList().func_232641_a_(new TranslationTextComponent("command.bingo.stop.stopped", source.getDisplayName()), ChatType.SYSTEM, Util.DUMMY_UUID);
		}else {
			source.getServer().getPlayerList().func_232641_a_(new TranslationTextComponent("command.bingo.stop.stopped", source.getDisplayName()), ChatType.CHAT, entity.getUniqueID());
		}
	}
   
}