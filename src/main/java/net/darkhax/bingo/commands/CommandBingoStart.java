package net.darkhax.bingo.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.darkhax.bingo.ModdedBingo;
import net.darkhax.bingo.PregenWorker;
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
import net.minecraft.world.World;
import net.minecraftforge.common.WorldWorkerManager;

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
		
		if(BingoAPI.GAME_STATE.shouldGroupTeams()) {
			final Map<Team, List<ServerPlayerEntity>> teamMap = new HashMap<>();
			for (final ServerPlayerEntity player : source.getServer().getPlayerList().getPlayers()) {
				Team t = BingoPersistantData.getTeam(player);
				teamMap.computeIfAbsent(t, k -> new ArrayList<>()).add(player);
			}
			
			for(final Map.Entry<Team, List<ServerPlayerEntity>> entry : teamMap.entrySet()) {
				BlockPos startPos = getRandomPosition(entry.getValue().get(0), 0);
				entry.getKey().setStartPosition(startPos);
				PregenWorker genWorker = new PregenWorker(startPos, source.getServer().getLevel(World.OVERWORLD), () -> {
            		for (final ServerPlayerEntity player : entry.getValue()) {
            			applySpawnEffects(player, startPos);
                    }
            	});
            	WorldWorkerManager.addWorker(genWorker);
			}
			
			// Generate starting positions for empty teams, makes things easier
			for(Team team : BingoAPI.TEAMS) {
				if(team.getStartPosition() == null) {
					team.setStartPosition(getRandomPosition(source, 0));
				}
			}
			
		}else {
			for (final ServerPlayerEntity player : source.getServer().getPlayerList().getPlayers()) {
				BlockPos startPos = getRandomPosition(player, 0);
				PregenWorker genWorker = new PregenWorker(startPos, source.getServer().getLevel(World.OVERWORLD), () -> {
					applySpawnEffects(player, startPos);
            	});
            	WorldWorkerManager.addWorker(genWorker);
			}
		}
	}
	
	/**
	 * Applies the spawnEffects to the given player
	 * @param player The player to apply the spawn effects
	 * @param spawnPos If present, pass in the known spawn Position, or null
	 */
	public static void applySpawnEffects(ServerPlayerEntity player, @Nullable BlockPos spawnPos) {
		if(spawnPos == null) {
			spawnPos = getSpawnPositionFor(player);
		}
        for (final SpawnEffect effect : BingoAPI.GAME_STATE.getMode().getSpawnEffect()) {
            effect.onPlayerSpawn(player, spawnPos);
        }
	}
	
	private static BlockPos getSpawnPositionFor(ServerPlayerEntity player) {
		if (BingoAPI.GAME_STATE.shouldGroupTeams()) {
			Team team = BingoPersistantData.getTeam(player);
			BlockPos teamPos = team.getStartPosition();
			if(teamPos == null) {
				teamPos = getRandomPosition(player, 0);
				team.setStartPosition(teamPos);
			}
			return teamPos;
		}else {
			return getRandomPosition(player, 0);
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
