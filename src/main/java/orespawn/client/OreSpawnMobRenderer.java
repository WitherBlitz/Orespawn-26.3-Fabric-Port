package orespawn.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

import java.util.function.BiConsumer;
import java.util.function.ToIntFunction;

/**
 * The renderer every OreSpawn creature shares, as OreSpawn's RenderX classes did: its model, shadow, a uniform scale
 * (halved for babies, as OreSpawn's renderers did), the texture chosen from the entity, and two optional extra passes:
 * the enchantment glint and the charged-creeper swirl.
 */
@Environment(EnvType.CLIENT)
public class OreSpawnMobRenderer<E extends Mob> extends MobRenderer<E, OreSpawnRenderState, EntityModel<OreSpawnRenderState>> {
	private static final Identifier SWIRL = Identifier.withDefaultNamespace("textures/entity/creeper/creeper_armor.png");
	private final float scale;
	private final BiConsumer<E, OreSpawnRenderState> extractor;
	private final ToIntFunction<E> blockLight;

	public OreSpawnMobRenderer(EntityRendererProvider.Context context, EntityModel<OreSpawnRenderState> model, float shadow, float scale,
			BiConsumer<E, OreSpawnRenderState> extractor, ToIntFunction<E> blockLight) {
		super(context, model, shadow);
		this.scale = scale;
		this.extractor = extractor;
		this.blockLight = blockLight;
		addLayer(new RenderLayer<>(this) {
			@Override
			public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, OreSpawnRenderState state, float yRot, float xRot) {
				if (state.glint) {
					collector.order(1).submitModel(getParentModel(), state, poseStack, RenderTypes.entitySolidGlint(state.texture), light,
						OverlayTexture.NO_OVERLAY, state.outlineColor);
				}
				if (state.swirl) {
					float t = state.ageInTicks * 0.01F;
					collector.order(1).submitModel(getParentModel(), state, poseStack, RenderTypes.energySwirl(SWIRL, t % 1.0F, t % 1.0F), light,
						OverlayTexture.NO_OVERLAY, -8355712, null, state.outlineColor);
				}
			}
		});
	}

	@Override
	public OreSpawnRenderState createRenderState() {
		return new OreSpawnRenderState();
	}

	@Override
	public void extractRenderState(E entity, OreSpawnRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.verticalSpeed = (float) entity.getDeltaMovement().y;
		state.ridden = entity.isVehicle();
		state.inWater = entity.isInWater();
		extractor.accept(entity, state);
	}

	@Override
	public Identifier getTextureLocation(OreSpawnRenderState state) {
		return state.texture;
	}

	@Override
	protected void scale(OreSpawnRenderState state, PoseStack poseStack) {
		float s = (state.isBaby ? scale / 2.0F : scale) * state.scale;
		poseStack.scale(s, s, s);
	}

	@Override
	protected int getBlockLightLevel(E entity, BlockPos pos) {
		int own = blockLight == null ? 0 : blockLight.applyAsInt(entity);
		return Math.max(own, super.getBlockLightLevel(entity, pos));
	}
}
