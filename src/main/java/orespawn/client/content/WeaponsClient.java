package orespawn.client.content;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.level.Level;
import orespawn.client.OreSpawnClient;
import orespawn.client.model.RatModel;
import orespawn.content.Weapons;
import orespawn.entity.weapons.PetRat;
import orespawn.item.weapons.ChainsawItem;
import orespawn.item.weapons.GlideBootsItem;
import orespawn.item.weapons.WeaponSwingPayload;
import org.jspecify.annotations.Nullable;

/**
 * The Weapons group's client side: its projectiles' renderers (OreSpawn drew its balls as spinning item icons, the
 * Bertha hit not at all, its arrows as arrows and its hook as a fishing hook; the Thunder Staff's bolt shows only its
 * sparks), the Rat Sword's rats (OreSpawn's rat model), the held 3D weapons ({@link HeldWeaponRenderer}), and per tick:
 * telling the server when the player swings a Bertha-class sword or the Chainsaw, the Chainsaw's smoke and flames while
 * it runs, and the glide of Royal, Peacock and Queen boots.
 */
@Environment(EnvType.CLIENT)
public final class WeaponsClient {
	private static final Identifier ARROW = Identifier.withDefaultNamespace("textures/entity/projectiles/arrow.png");
	private static LivingEntity.@Nullable SwingDescription lastSwing;
	private static int chainsawTicks;

	private WeaponsClient() {
	}

	public static void init() {
		EntityRendererRegistry.register(Weapons.BERTHA_HIT, NoopRenderer::new);
		EntityRendererRegistry.register(Weapons.THUNDER_BOLT, NoopRenderer::new);
		EntityRendererRegistry.register(Weapons.LASER_BALL, ThrownItemRenderer::new);
		EntityRendererRegistry.register(Weapons.ICE_BALL_ENTITY, ThrownItemRenderer::new);
		EntityRendererRegistry.register(Weapons.ACID_ENTITY, ThrownItemRenderer::new);
		EntityRendererRegistry.register(Weapons.ULTIMATE_ARROW, PlainArrowRenderer::new);
		EntityRendererRegistry.register(Weapons.IRUKANDJI_ARROW_ENTITY, PlainArrowRenderer::new);
		hookRenderer();
		OreSpawnClient.<PetRat>mob(Weapons.PET_RAT, OreSpawnClient.layer("pet_rat", RatModel::createBodyLayer), root -> new RatModel(root, 1.0F), 0.1F,
			0.75F, (e, s) -> {
				s.texture = OreSpawnClient.tex("rat.png");
				s.attacking = e.attacking();
			}, null);

		HeldWeaponRenderer.init();
		ModelLoadingPlugin.register(context -> context.modifyItemModelOnLoad().register((item, c) -> HeldWeaponRenderer.wrap(item, c.itemId())));

		ClientTickEvents.END_CLIENT_TICK.register(WeaponsClient::tick);
	}

	/** The Ultimate Fishing Rod's hook is a FishingHook, drawn (with its line) as vanilla draws one. */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void hookRenderer() {
		EntityRendererRegistry.register(Weapons.ULTIMATE_FISH_HOOK, (EntityRendererProvider) FishingHookRenderer::new);
	}

	private static void tick(Minecraft mc) {
		LocalPlayer player = mc.player;
		if (player == null) {
			lastSwing = null;
			chainsawTicks = 0;
			return;
		}
		LivingEntity.SwingDescription swing = player.getCurrentSwing();
		if (swing != null && swing != lastSwing && swing.hand() == InteractionHand.MAIN_HAND && WeaponSwingPayload.wanted(player.getMainHandItem())) {
			if (ClientPlayNetworking.canSend(WeaponSwingPayload.TYPE)) ClientPlayNetworking.send(WeaponSwingPayload.INSTANCE);
			if (player.getMainHandItem().getItem() instanceof ChainsawItem && chainsawTicks <= 0) chainsawTicks = ChainsawItem.RUN_TICKS;
		}
		lastSwing = swing;
		if (chainsawTicks > 0) {
			chainsawTicks--;
			chainsawSmoke(player.level(), player);
		}
		GlideBootsItem.glide(player);
	}

	/** UltimateSword.onUpdate for the running Chainsaw: flames, smoke and sparks by the player's right side. */
	private static void chainsawSmoke(Level level, LocalPlayer p) {
		var r = level.getRandom();
		double a = Math.toRadians(p.getYRot() + 90.0F + 45.0F);
		double x = p.getX() + Math.cos(a);
		double y = p.getEyeY();
		double z = p.getZ() + Math.sin(a);
		if (r.nextInt(8) == 0) {
			level.addParticle(ParticleTypes.FLAME, x, y, z, (r.nextFloat() - r.nextFloat()) / 20.0F, r.nextFloat() / 10.0F,
				(r.nextFloat() - r.nextFloat()) / 20.0F);
		}
		if (r.nextInt(2) == 0) {
			level.addParticle(ParticleTypes.SMOKE, x, y, z, (r.nextFloat() - r.nextFloat()) / 20.0F, r.nextFloat() / 10.0F,
				(r.nextFloat() - r.nextFloat()) / 20.0F);
		}
		if (r.nextInt(10) == 0) {
			level.addParticle(ParticleTypes.FIREWORK, x, y, z, (r.nextFloat() - r.nextFloat()) / 20.0F, r.nextFloat() / 5.0F,
				(r.nextFloat() - r.nextFloat()) / 20.0F);
		}
	}

	/** OreSpawn's arrows were vanilla arrows to look at (RenderArrow). */
	private static final class PlainArrowRenderer<T extends AbstractArrow> extends ArrowRenderer<T, ArrowRenderState> {
		PlainArrowRenderer(EntityRendererProvider.Context context) {
			super(context);
		}

		@Override
		protected Identifier getTextureLocation(ArrowRenderState state) {
			return ARROW;
		}

		@Override
		public ArrowRenderState createRenderState() {
			return new ArrowRenderState();
		}
	}
}
