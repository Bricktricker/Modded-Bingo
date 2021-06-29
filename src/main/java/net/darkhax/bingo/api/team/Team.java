package net.darkhax.bingo.api.team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

/**
 * Represents a team in the game.
 */
public class Team {

    /**
     * A mapping of all teams by their name. This is used to (de)serialize them later.
     */
    private static final Map<String, Team> TEAMS_BY_NAME = new HashMap<>();

    /**
     * A list of all the team names. This is used to automate tab completion.
     */
    private static final List<String> TEAM_NAMES = new ArrayList<>();

    /**
     * The text format for the team.
     */
    private final TextFormatting teamColorText;

    /**
     * The corner of the board that this team is drawn to.
     */
    private final int teamCorner;

    /**
     * The dye color associated with the team.
     */
    private final DyeColor dyeColor;

    /**
     * A firework item that has the team color.
     */
    private ItemStack fireworStack;

    /**
     * A text component of the team name.
     */
    private final ITextComponent teamName;

    /**
     * A packed RGB int containing the color code of the team.
     */
    private final int color;

    public Team (TextFormatting teamColorText, int teamCorner, DyeColor dyeColor) {
        this.teamColorText = teamColorText;
        this.teamCorner = teamCorner;
        this.dyeColor = dyeColor;
        TEAMS_BY_NAME.put(dyeColor.getName(), this);
        TEAM_NAMES.add(dyeColor.getName()); 
        this.teamName = new StringTextComponent(dyeColor.getName());
        this.teamName.getStyle().withColor(Color.fromLegacyFormat(this.getTeamColorText()));
        this.color = this.dyeColor.getColorValue();
    }

    /**
     * Gets the text formatting for this team.
     *
     * @return The text formatting for this team.
     */
    public TextFormatting getTeamColorText () {

        return this.teamColorText;
    }

    /**
     * Gets the corner the team is drawn to.
     *
     * @return The corner the team is drawn to.
     */
    public int getTeamCorner () {

        return this.teamCorner;
    }

    /**
     * Gets the dye color for the team.
     *
     * @return The dye color linked to the team.
     */
    public DyeColor getDyeColor () {

        return this.dyeColor;
    }

    /**
     * A text component containing the team's name.
     *
     * @return The team's name as a text component.
     */
    public ITextComponent getTeamName () {

        return this.teamName;
    }

    /**
     * Gets the raw team name. Used for (de)serialization.
     *
     * @return The team's raw name.
     */
    public String getTeamKey () {

        return this.getDyeColor().getName();
    }

    /**
     * Generates a firework itemstack with the nbt matching the team.
     *
     * @return A firework matching the team's info.
     */
    public ItemStack getFireworStack () {

        if (this.fireworStack == null || this.fireworStack.isEmpty()) {
            this.fireworStack = new ItemStack(Items.FIREWORK_STAR);
            final CompoundNBT baseTag = this.fireworStack.getOrCreateTag();
            final CompoundNBT fireworks = new CompoundNBT();
            fireworks.putByte("Flight", (byte) 0);
            baseTag.put("Fireworks", fireworks);
            final ListNBT explosions = new ListNBT();
            fireworks.put("Explosions", explosions);
            final CompoundNBT explosion = new CompoundNBT();
            explosion.putByte("Type", (byte) 0);
            explosion.putIntArray("Colors", new int[] { this.color });
        }

        return this.fireworStack;
    }

    /**
     * Spawns the team's signature firework on a player.
     *
     * @param player The player to spawn the firework on.
     */
    public void spawnFirework (ServerPlayerEntity player) {
        final FireworkRocketEntity rocket = new FireworkRocketEntity(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), this.getFireworStack());
        ObfuscationReflectionHelper.setPrivateValue(FireworkRocketEntity.class, rocket, 0, "lifetime"); //lifetime
        player.getCommandSenderWorld().addFreshEntity(rocket);
        player.level.broadcastEntityEvent(rocket, (byte) 17);
    }

    /**
     * Gets a team by their name.
     *
     * @param name The name to search for.
     * @return The team that was found.
     */
    @Nullable
    public static Team getTeamByName (String name) {

        return TEAMS_BY_NAME.get(name);
    }

    /**
     * Gets a list of all valid team names.
     *
     * @return The list of valid team names.
     */
    public static List<String> getTeamNames () {

        return TEAM_NAMES;
    }
}
