package net.darkhax.bingo.commands;

import java.util.Random;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.darkhax.bingo.ModdedBingo;
import net.darkhax.bingo.api.BingoAPI;
import net.darkhax.bingo.api.effects.spawn.SpawnEffect;
import net.darkhax.bingo.api.team.Team;
import net.darkhax.bingo.data.BingoPersistantData;
import net.darkhax.bingo.network.PacketSyncGameState;
import net.darkhax.bookshelf.util.CommandUtils;
import net.darkhax.bookshelf.util.MathsUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TranslationTextComponent;

public class CommandBingoStart {

	public static LiteralArgumentBuilder<CommandSource> register() {
		return CommandUtils.createCommand("start", 2, ctx -> {
			execute(ctx.getSource().getPlayerOrException());
			return 1;
		});
	}
	
	private static void execute(ServerPlayerEntity source) throws CommandSyntaxException {
		if (!BingoAPI.GAME_STATE.isActive()) {
			throw new SimpleCommandExceptionType(new TranslationTextComponent("command.bingo.info.notactive")).create();
        }
		
		if (BingoAPI.GAME_STATE.hasStarted() || BingoAPI.GAME_STATE.getWinner() != null) {
			throw new SimpleCommandExceptionType(new TranslationTextComponent("command.bingo.info.alreadystarted")).create();
        }
		
		BingoAPI.GAME_STATE.start(source.getServer(), source.level.getGameTime());
		source.getServer().getPlayerList().broadcastMessage(new TranslationTextComponent("command.bingo.start.started", source.getDisplayName()), ChatType.CHAT, source.getUUID());
		ModdedBingo.NETWORK.sendToAllPlayers(new PacketSyncGameState());
		
		if (BingoAPI.GAME_STATE.shouldGroupTeams()) {
            for (Team team : BingoAPI.TEAMS) {
            	team.setStartPosition(getRandomPosition(source, 0));
            }
        }
		
		for (final ServerPlayerEntity player : source.getServer().getPlayerList().getPlayers()) {
			applySpawnEffects(player);
        }
		
	}
	
	public static void applySpawnEffects(ServerPlayerEntity player) {
		BlockPos spawnPos = BingoAPI.GAME_STATE.shouldGroupTeams() ? BingoPersistantData.getTeam(player).getStartPosition() : getRandomPosition(player, 0);
        for (final SpawnEffect effect : BingoAPI.GAME_STATE.getMode().getSpawnEffect()) {
            effect.onPlayerSpawn(player, spawnPos);
        }
	}
	
	private static BlockPos getRandomPosition(ServerPlayerEntity source, int depth) {
		Random rand = BingoAPI.GAME_STATE.getRandom();
		if(rand == null) { // is null when loaded from disc
			rand = new Random();
		}
		
        final int x = MathsUtils.nextIntInclusive(rand, -3_000_000, 3_000_000);
        final int z = MathsUtils.nextIntInclusive(rand, -3_000_000, 3_000_000);
        
        final BlockPos.Mutable pos = new BlockPos.Mutable(x, 255, z);

        while (source.level.isEmptyBlock(pos)) {
            pos.move(Direction.DOWN);
            if (pos.getY() <= 10) {
                return depth == 100 ? pos : getRandomPosition(source, depth + 1);
            }
        }

        return !source.level.loadedAndEntityCanStandOn(pos, source) && depth < 100 ? getRandomPosition(source, depth + 1) : pos.move(Direction.UP);
    }
}
