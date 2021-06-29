package net.darkhax.bingo.api.effects.spawn;

import com.google.gson.annotations.Expose;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.BlockPos;

/**
 * This effect applies a potion effect to all players when the game has been started.
 */
public class SpawnEffectPotion extends SpawnEffect {

    /**
     * The potion effect to apply.
     */
    @Expose
    private Effect effect;

    /**
     * The duration in ticks.
     */
    @Expose
    private int duration;

    /**
     * An amplifier.
     */
    @Expose
    private int amplifier;

    @Override
    public void onPlayerSpawn (ServerPlayerEntity player, BlockPos pos) {
        player.addEffect(new EffectInstance(this.effect, this.duration, this.amplifier));
    }
}