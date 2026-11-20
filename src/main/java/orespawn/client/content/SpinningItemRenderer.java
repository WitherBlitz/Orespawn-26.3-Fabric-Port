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
 * OreSpawn's RenderSpinner and RenderThrownRock: a thrown thing drawn as its item's picture facing the camera, half a
 * block across, spinning in the picture's plane (OreSpawn turned it 20 degrees a tick for cages and shoes, 30 for rocks).
 * OreSpawn's spinner sheet holds the same pictures as the items.
 */
@Environment(EnvType.CLIENT)
public class SpinningItemRenderer<T extends Entity & ItemSupplier> extends EntityRenderer<T, ThrownItemRenderState> {
	private final ItemModelResolver itemModelResolver;
	private final float degreesPerTick;

	public SpinningItemRenderer(EntityRendererProvider.Context context, float degreesPerTick) {
		super(context);
		this.itemModelResolver = context.getItemModelResolver();
		this.degreesPerTick = degreesPerTick;
	}

	@Override
	public void submit(ThrownItemRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.rotate(camera.orientation);
		poseStack.rotateDegrees(Axis.ZP, state.ageInTicks * degreesPerTick % 360.0F);
		state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
		poseStack.popPose();
		super.submit(state, poseStack, collector, camera);
	}

	@Override
	public ThrownItemRenderState createRenderState() {
		return new ThrownItemRenderState();
	}

	@Override
	public void extractRenderState(T entity, ThrownItemRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		itemModelResolver.updateForNonLiving(state.item, entity.getItem(), ItemDisplayContext.GROUND, entity);
	}
}
