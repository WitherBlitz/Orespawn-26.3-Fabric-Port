package orespawn.client.content;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * OreSpawn's RenderSpinner (RenderItemUrchin), for the Sea group's projectiles: the item's picture, half a block across,
 * always facing the camera and spinning about the view axis (OreSpawn turned it 30 degrees a tick, 50 for the Dead
 * Irukandji). Like vanilla's ThrownItemRenderer, with the spin.
 */
@Environment(EnvType.CLIENT)
public class SeaSpinnerRenderer<T extends Entity & ItemSupplier> extends EntityRenderer<T, SeaSpinnerRenderer.State> {
	@Environment(EnvType.CLIENT)
	public static class State extends ThrownItemRenderState {
		public float spin;
	}

	private final ItemModelResolver itemModelResolver;
	private final float degreesPerTick;

	public SeaSpinnerRenderer(EntityRendererProvider.Context context, float degreesPerTick) {
		super(context);
		this.itemModelResolver = context.getItemModelResolver();
		this.degreesPerTick = degreesPerTick;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(T entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		itemModelResolver.updateForNonLiving(state.item, entity.getItem(), ItemDisplayContext.GROUND, entity);
		state.spin = (entity.tickCount + partialTicks) * degreesPerTick % 360.0F;
	}

	@Override
	public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.rotate(camera.orientation);
		poseStack.rotateDegrees(Axis.ZP, state.spin);
		state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
		poseStack.popPose();
		super.submit(state, poseStack, collector, camera);
	}
}
