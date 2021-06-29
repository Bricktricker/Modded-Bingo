package net.darkhax.bingo.client;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.darkhax.bingo.ModdedBingo;
import net.darkhax.bingo.api.BingoAPI;
import net.darkhax.bingo.api.team.Team;
import net.darkhax.bingo.data.GameState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = "bingo")
public class BingoRenderer {

	/**
	 * An array of UV coordinate offsets related to the team corners. This is used
	 * to render the highlight when a team has completed a goal.
	 */
	private static final int[][] teamUVs = new int[][] { new int[] { 0, 0, 11, 11 }, new int[] { 11, 0, 11, 11 }, new int[] { 0, 11, 11, 11 }, new int[] { 11, 11, 11, 11 } };

	private static final ResourceLocation TEXTURE_LOC = new ResourceLocation(ModdedBingo.MOD_ID, "hud/bingo_board.png");

	@SubscribeEvent
	public static void onTooltip(ItemTooltipEvent event) {

		if(BingoAPI.GAME_STATE != null) {

			for(int x = 0; x < 5; x++) {

				for(int y = 0; y < 5; y++) {

					ItemStack goal = BingoAPI.GAME_STATE.getGoal(x, y);
					if(goal != null && !goal.isEmpty() && ItemStack.isSame(goal, event.getItemStack())) {

						event.getToolTip().add(new StringTextComponent(TextFormatting.YELLOW + I18n.get("tooltip.bingo.goalitem")));
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void render(RenderGameOverlayEvent.Post event) {
		if(event.getType() != RenderGameOverlayEvent.ElementType.ALL)
			return;

		GameState bingo = BingoAPI.GAME_STATE;
		final Minecraft mc = Minecraft.getInstance();

		if(!bingo.isActive() || !Minecraft.renderNames() || mc.screen != null || mc.options.renderDebug) {
			return;
		}

		if(bingo.getStartTime() > 0 && mc.level != null) {
			long endTime = bingo.getEndTime() >= bingo.getStartTime() ? bingo.getEndTime() : mc.level.getGameTime();
			mc.font.draw(event.getMatrixStack(), "Time: " + StringUtils.formatTickDuration((int) (endTime - bingo.getStartTime())), 14, 2, 0xffffff);
		}

		mc.getTextureManager().bind(TEXTURE_LOC);
		renderGUI(event.getMatrixStack().last().pose(), 10, 142, 10, 142, 0, 0, 132f / 256f, 0, 132f / 256f);

		final ItemRenderer itemRender = mc.getItemRenderer();

		for(int x = 0; x < 5; x++) {
			for(int y = 0; y < 5; y++) {

				final ItemStack goal = BingoAPI.GAME_STATE.getGoal(x, y);

				if(goal != null) {
					itemRender.renderAndDecorateItem(null, goal, 16 + x * 24, 16 + y * 24);
				}
			}
		}

	}

	private static void renderGUI(Matrix4f matrix, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1) {
		BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.vertex(matrix, (float) x0, (float) y1, (float) z).uv(u0, v1).endVertex();
		bufferbuilder.vertex(matrix, (float) x1, (float) y1, (float) z).uv(u1, v1).endVertex();
		bufferbuilder.vertex(matrix, (float) x1, (float) y0, (float) z).uv(u1, v0).endVertex();
		bufferbuilder.vertex(matrix, (float) x0, (float) y0, (float) z).uv(u0, v0).endVertex();
		bufferbuilder.end();
		RenderSystem.enableAlphaTest();
		WorldVertexBufferUploader.end(bufferbuilder);

		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		final float texSize = 256f;

		for(int x = 0; x < 5; x++) {
			for(int y = 0; y < 5; y++) {

				final Team[] completedTeams = BingoAPI.GAME_STATE.getCompletionStats(x, y);
				for(final Team team : completedTeams) {

					if(team != null) {
						final int[] uvs = teamUVs[team.getTeamCorner()];
						final float[] color = team.getDyeColor().getTextureDiffuseColors();

						final int xOffset = 13 + x * 24 + uvs[0];
						final int yOffset = 13 + y * 24 + uvs[1];
						final float minU = (132 + uvs[0]) / texSize;
						final float maxU = (132 + uvs[0] + uvs[2]) / texSize;
						final float minV = uvs[1] / texSize;
						final float maxV = (uvs[1] + uvs[3]) / texSize;

						bufferbuilder.vertex(matrix, xOffset, yOffset + uvs[3], 0).uv(minU, maxV).color(color[0], color[1], color[2], 1f).endVertex();
						bufferbuilder.vertex(matrix, xOffset + uvs[2], yOffset + uvs[3], 0).uv(maxU, maxV).color(color[0], color[1], color[2], 1f).endVertex();
						bufferbuilder.vertex(matrix, xOffset + uvs[2], yOffset, 0).uv(maxU, minV).color(color[0], color[1], color[2], 1f).endVertex();
						bufferbuilder.vertex(matrix, xOffset, yOffset, 0).uv(minU, minV).color(color[0], color[1], color[2], 1f).endVertex();
					}
				}
			}
		}
		bufferbuilder.end();
		WorldVertexBufferUploader.end(bufferbuilder);
	}
}