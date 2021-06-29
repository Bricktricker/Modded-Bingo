package net.darkhax.bingo.network;

import java.util.function.Supplier;

import net.darkhax.bingo.api.BingoAPI;
import net.darkhax.bingo.api.team.Team;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * This packet is used to sync the state of a specific goal.
 */

public class PacketSyncGoal {

    /**
     * The X pos on the bingo board for the goal that is updated.
     */
    public int x;

    /**
     * The Y pos on the bingo board for the goal that is updated.
     */
    public int y;

    /**
     * The name of the team that completed the goal.
     */
    public String teamName;

    public PacketSyncGoal (int x, int y, String team) {
        this.x = x;
        this.y = y;
        this.teamName = team;
    }
    
    public PacketSyncGoal (PacketBuffer buffer) {
    	this.x = buffer.readInt();
    	this.y = buffer.readInt();
    	this.teamName = buffer.readUtf(30);
    }
    
    public void encode(PacketBuffer buffer) {
    	buffer.writeInt(this.x);
    	buffer.writeInt(this.y);
    	buffer.writeUtf(this.teamName);
    }
    
    public void handle(Supplier<NetworkEvent.Context> ctx) {
    	final Team team = Team.getTeamByName(this.teamName);
        if (team != null) {
            BingoAPI.GAME_STATE.getCompletionStates()[this.x][this.y][team.getTeamCorner()] = team;
        }
    }
}
