package net.darkhax.bingo.api.effects.ending;

import net.darkhax.bingo.api.team.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * This effect announces when a player or team has completed the goals required to win the
 * game.
 */
public class EndingEffectAnnounce extends GameWinEffect {

    @Override
    public void onGameCompleted (MinecraftServer server, Team winningTeam) {
        server.getPlayerList().func_232641_a_(new TranslationTextComponent("bingo.winner", winningTeam.getTeamName()), ChatType.SYSTEM, null);
    }
}