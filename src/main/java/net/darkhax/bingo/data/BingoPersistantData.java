package net.darkhax.bingo.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import net.darkhax.bingo.api.BingoAPI;
import net.darkhax.bingo.api.team.Team;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

/**
 * This class is responsible for handling all save data that is persistent in the world.
 */
public class BingoPersistantData {

    /**
     * A map of player UUIDs to their teams.
     */
    private static final Map<UUID, Team> PLAYER_TEAMS = new HashMap<>();

    /**
     * Gets the team a player is on. Players without a team default to the red team.
     *
     * @param player The player to lookup.
     * @return The team the player is on.
     */
    public static Team getTeam (PlayerEntity player) {

        return PLAYER_TEAMS.getOrDefault(player.getUUID(), BingoAPI.TEAM_RED);
    }
    
    /**
     * Checks if the given player is in a team
     * @param player The player to lookup
     * @return true if the player is in a team
     */
    public static boolean isInTeam(PlayerEntity player) {
    	return PLAYER_TEAMS.containsKey(player.getUUID());
    }

    /**
     * Sets the team for a player.
     *
     * @param player The player to set the team for.
     * @param team The team they are now on.
     */
    public static void setTeam (PlayerEntity player, Team team) {

        PLAYER_TEAMS.put(player.getUUID(), team);
    }
    
    /**
     * removes a player from the teams list
     * @param player the player to remove
     */
    public static void removePlayer(PlayerEntity player) {
    	
    	PLAYER_TEAMS.remove(player.getUUID());
    }
    
    /**
     * Writes all of the data to the provided PacketBuffer
     * 
     * @param buffer The PacketBuffer to write the game state to.
     */
    public static void write(PacketBuffer buffer) {
    	buffer.writeVarInt(PLAYER_TEAMS.entrySet().size());
    	for (final Entry<UUID, Team> entry : PLAYER_TEAMS.entrySet()) {
    		buffer.writeUUID(entry.getKey());
    		buffer.writeUtf(entry.getValue().getDyeColor().getName());
        }
    	
    	if(BingoAPI.GAME_STATE.hasStarted() && BingoAPI.GAME_STATE.shouldGroupTeams()) {
    		buffer.writeBoolean(true);
        	for(Team team : BingoAPI.TEAMS) {
        		BlockPos startPos = team.getStartPosition();
        		buffer.writeBlockPos(startPos);
        	}	
    	}else {
    		buffer.writeBoolean(false);
    	}
    	
    	BingoAPI.GAME_STATE.write(buffer);
    }
    
    /**
     * Reads data from a PacketBuffer.
     * 
     * @param buffer The PacketBuffer to read from.
     */
    public static void read(PacketBuffer buffer) {
    	PLAYER_TEAMS.clear();
    	int numPlayer = buffer.readVarInt();
    	for(int i = 0; i < numPlayer; i++) {
    		final UUID uuid = buffer.readUUID();
    		final Team team = Team.getTeamByName(buffer.readUtf(30));
    		if(team != null) {
    			PLAYER_TEAMS.put(uuid, team);
    		}
    	}
    	
    	if(buffer.readBoolean()) {
    		for(Team team : BingoAPI.TEAMS) {
        		BlockPos startPos = buffer.readBlockPos();
        		team.setStartPosition(startPos);
        	}
    	}
    	
    	BingoAPI.GAME_STATE.read(buffer);
    }

}
