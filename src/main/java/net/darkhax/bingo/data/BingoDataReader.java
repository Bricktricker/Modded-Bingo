package net.darkhax.bingo.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import net.darkhax.bingo.ModdedBingo;
import net.darkhax.bingo.api.BingoAPI;
import net.darkhax.bingo.api.game.GameMode;
import net.darkhax.bingo.api.goal.GoalTable;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

public class BingoDataReader extends JsonReloadListener {

	public BingoDataReader() {
		super(BingoAPI.GSON, "bingo");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {
		BingoAPI.resetTables();
		List<GameMode> gamemodes = new ArrayList<>();
		objectIn.forEach((rl, jsonElem) -> {
			try {
				if(!jsonElem.isJsonObject()) {
					throw new JsonSyntaxException("Bingo tables need to be a JSON Object");
				}
				
				JsonObject jsonObject = jsonElem.getAsJsonObject();
				String type = JSONUtils.getString(jsonObject, "type");
				if(type.equalsIgnoreCase("gamemode")) {
					GameMode gamemode = BingoAPI.GSON.fromJson(jsonObject, GameMode.class);
					gamemodes.add(gamemode);
				}else if(type.equalsIgnoreCase("goaltable")) {
					GoalTable goaltable = BingoAPI.GSON.fromJson(jsonObject, GoalTable.class);
					BingoAPI.addGoalTable(goaltable);
				}else {
					ModdedBingo.LOG.error("Parsing error loading bingo data {} - unknown type", rl, type);
				}
			}catch(JsonParseException jsonparseexception) {
				ModdedBingo.LOG.error("Parsing error loading bingo data {}", rl, jsonparseexception);
			}
		});

		for(GameMode gamemode : gamemodes) {
			boolean valid = true;
			for(ResourceLocation tableId : gamemode.getGoalTables()) {
				if(BingoAPI.getGoalTable(tableId) == null) {
					ModdedBingo.LOG.error("The game mode {} references unknown table {}. It will not be registered.", gamemode.getModeId().toString(), tableId.toString());
					valid = false;
					break;
				}
			}
			if(valid) {
				ModdedBingo.LOG.info("Successfully loaded Game Mode: " + gamemode.getModeId().toString());
				BingoAPI.addGameMode(gamemode);
			}
		}
	}
	
}
