package orespawn.client.content;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.client.renderer.item.properties.select.DisplayContext;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemDisplayContext;
import orespawn.OreSpawn;
import orespawn.client.OreSpawnClient;
import orespawn.client.model.BattleAxeModel;
import orespawn.client.model.BerthaModel;
import orespawn.client.model.ChainsawModel;
import orespawn.client.model.HammyModel;
import orespawn.client.model.QueenBattleAxeModel;
import orespawn.client.model.SliceModel;
import orespawn.client.model.SquidZookaModel;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * OreSpawn's 3D weapons in the hand (its IItemRenderers RenderBertha, RenderSlice, RenderRoyal, RenderHammy,
 * RenderBattleAxe, RenderQueenBattleAxe, RenderChainsaw and RenderSquidZooka). OreSpawn drew these models only in the
 * hand, first and third person; elsewhere (inventory, ground, frames) the flat icon is used, as OreSpawn did.
 * <p>
 * Each item's model definition (gen_assets' flat one) is wrapped on load in a select on the display context, with this
 * special renderer for the four hand contexts. The renderer first undoes 26.3's hand transform and then applies
 * 1.7.10's: in third person RenderPlayer's arm offset, its "3D item" pose and Forge's -0.5 render-helper shift; in
 * first person ItemRenderer's 45-degree turn and 0.4 scale; then OreSpawn's own rotations, scale and offset. The
 * models were drawn at one block per model unit (render(1.0F)), so they're scaled up 16 times; the left hand gets a
 * mirror image.
 */
@Environment(EnvType.CLIENT)
public final class HeldWeaponRenderer implements NoDataSpecialModelRenderer {
	@FunctionalInterface
	private interface Transform {
		void apply(PoseStack p);
	}

	private enum View {
		FP_RIGHT(true, false), FP_LEFT(true, true), TP_RIGHT(false, false), TP_LEFT(false, true);

		final boolean firstPerson;
		final boolean left;

		View(boolean firstPerson, boolean left) {
			this.firstPerson = firstPerson;
			this.left = left;
		}
	}

	/**
	 * The held weapons, with OreSpawn's first-person (renderSword) and third-person (renderSwordF5) transforms. Their
	 * textures are OreSpawn's (Berthatexture.png, Slicetexture.png, Royaltexture.png, AttitudeAdjustertexture.png,
	 * BattleAxetexture.png, QueenBattleAxetexture.png, Chainsawtexture.png, SquidZookatexture.png) as
	 * textures/entity/weapons/<item id>.png.
	 */
	enum Held {
		BERTHA("big_bertha", "held_bertha", BerthaModel::new, BerthaModel::createBodyLayer,
			p -> sword(p, 190, 0, 25, 0.25F, 6, 3, -5), p -> f5(p, 0.25F, -4, 2, -3)),
		SLICE("slice", "held_slice", SliceModel::new, SliceModel::createBodyLayer,
			p -> sword(p, 190, 0, 25, 0.3F, 6, 3, -5), p -> f5(p, 0.3F, -4, 2, -3)),
		ROYAL("royal_guardian_sword", "held_slice", SliceModel::new, SliceModel::createBodyLayer,
			p -> sword(p, 190, 0, 25, 0.35F, 6, 3, -5), p -> f5(p, 0.35F, -4, 2, -3)),
		HAMMY("attitude_adjuster", "held_hammy", HammyModel::new, HammyModel::createBodyLayer, p -> {
			p.rotateDegrees(Axis.YP, 70.0F);
			sword(p, 190, 0, 25, 0.15F, -10, -13, -5);
		}, p -> {
			p.rotateDegrees(Axis.XP, 180.0F);
			scaleMove(p, 0.15F, 6, -20, -4);
		}),
		BATTLE_AXE("battle_axe", "held_battle_axe", BattleAxeModel::new, BattleAxeModel::createBodyLayer,
			HeldWeaponRenderer::axeFirstPerson, HeldWeaponRenderer::axeThirdPerson),
		QUEEN_BATTLE_AXE("queen_scale_battle_axe", "held_queen_battle_axe", QueenBattleAxeModel::new,
			QueenBattleAxeModel::createBodyLayer, HeldWeaponRenderer::axeFirstPerson, HeldWeaponRenderer::axeThirdPerson),
		CHAINSAW("chainsaw", "held_chainsaw", ChainsawModel::new, ChainsawModel::createBodyLayer, p -> {
			p.rotateDegrees(Axis.YP, 150.0F);
			p.rotateDegrees(Axis.XP, 100.0F);
			scaleMove(p, 0.25F, -10, 1, -4);
		}, p -> {
			p.rotateDegrees(Axis.ZP, 180.0F);
			p.rotateDegrees(Axis.YP, -20.0F);
			p.rotateDegrees(Axis.XP, -20.0F);
			scaleMove(p, 0.25F, -3, -3, -2);
		}),
		// ModelSquidZooka turned itself upside down before drawing (glRotatef(180, 0, 0, 1) in its render()).
		SQUIDZOOKA("squidzooka", "held_squidzooka", SquidZookaModel::new, SquidZookaModel::createBodyLayer, p -> {
			p.rotateDegrees(Axis.YP, -30.0F);
			scaleMove(p, 0.35F, 4, 2, 2);
		}, p -> {
			p.rotateDegrees(Axis.YP, 30.0F);
			scaleMove(p, 0.35F, 2, 8, 2);
		});

		final Identifier item;
		final String layerName;
		final Identifier texture;
		final Function<ModelPart, Model<Float>> model;
		final Supplier<LayerDefinition> layer;
		final Transform firstPerson;
		final Transform thirdPerson;
		/** The turn about the grip for Wither's Animations' first-person hand (see {@link #firstPersonTurn}), or none. */
		Quaternionfc firstPersonTurn;

		Held(String item, String layerName, Function<ModelPart, Model<Float>> model, Supplier<LayerDefinition> layer, Transform firstPerson,
				Transform thirdPerson) {
			this.item = OreSpawn.id(item);
			this.layerName = layerName;
			this.texture = OreSpawnClient.tex("weapons/" + item + ".png");
			this.model = model;
			this.layer = layer;
			this.firstPerson = firstPerson;
			this.thirdPerson = thirdPerson;
		}
	}

	private static final Map<Held, ModelLayerLocation> LAYERS = new EnumMap<>(Held.class);
	private static final Identifier BASE = Identifier.withDefaultNamespace("block/air");

	/*
	 * Wither's Animations draws first-person items in its own 3D hand, in the third-person display context, so these
	 * weapons get their 1.7.10 third-person grip there. That suits the swords and the Chainsaw, which point out of the
	 * fist, but the axes and the Attitude Adjuster were held across the body and the SquidZooka sideways. In that hand
	 * (its HeldItemModelsApi.firstPersonHandWeight, 1 there, 0 everywhere else, easing between the two as the hands hand
	 * off to the body when looking down) they're turned about the grip: the axes and the Attitude Adjuster to lie like Big
	 * Bertha's blade, the SquidZooka to point along the forearm. Without Wither's Animations nothing changes.
	 */
	/** Big Bertha's blade, from the grip, in the 1.7.10 third-person item frame (its model's -Y after renderSwordF5). */
	private static final Vector3fc SWORD_TIP = new Vector3f(-1.0F, 0.0F, 0.0F);
	/** Along the forearm towards the hand, in that frame (RenderPlayer's 20 and 45 degree turns and mirror, undone). */
	private static final Vector3fc FOREARM = new Matrix3f().rotateX((float) Math.toRadians(20.0)).rotateY((float) Math.toRadians(45.0))
		.scale(-1.0F, -1.0F, 1.0F).invert().transform(new Vector3f(0.0F, 1.0F, 0.0F)).normalize();
	private static final MethodHandle HAND_WEIGHT = handWeight();

	static {
		// Where each model's head or muzzle points after its own third-person turn (renderSwordF5 / renderItem).
		Vector3f axeHead = new Quaternionf().rotationAxis((float) Math.PI, new Vector3f(1.0F, 0.25F, 0.0F).normalize())
			.transform(new Vector3f(0.0F, -1.0F, 0.0F));
		Held.BATTLE_AXE.firstPersonTurn = new Quaternionf().rotationTo(axeHead, SWORD_TIP);
		Held.QUEEN_BATTLE_AXE.firstPersonTurn = Held.BATTLE_AXE.firstPersonTurn;
		Held.HAMMY.firstPersonTurn = new Quaternionf().rotationTo(new Vector3f(0.0F, 1.0F, 0.0F), SWORD_TIP);
		Vector3f muzzle = new Quaternionf().rotationY((float) Math.toRadians(30.0)).transform(new Vector3f(0.0F, 0.0F, -1.0F));
		Held.SQUIDZOOKA.firstPersonTurn = new Quaternionf().rotationTo(muzzle, FOREARM);
	}

	private static MethodHandle handWeight() {
		try {
			Class<?> api = Class.forName("com.trainguy9512.locomotion.api.HeldItemModelsApi");
			return MethodHandles.publicLookup().findStatic(api, "firstPersonHandWeight", MethodType.methodType(float.class));
		} catch (ReflectiveOperationException | LinkageError e) {
			return null;
		}
	}

	/** Wither's Animations' first-person hand weight for the draw in progress (0 without it). */
	private static float firstPersonHandWeight() {
		if (HAND_WEIGHT == null) return 0.0F;
		try {
			return Math.clamp((float) HAND_WEIGHT.invokeExact(), 0.0F, 1.0F);
		} catch (Throwable t) {
			return 0.0F;
		}
	}

	private final Held held;
	private final View view;
	private final Model<Float> model;

	private HeldWeaponRenderer(Held held, View view, Model<Float> model) {
		this.held = held;
		this.view = view;
		this.model = model;
	}

	/** Registers the model layers (shared models once) and the special model type. */
	static void init() {
		Map<String, ModelLayerLocation> byName = new HashMap<>();
		for (Held h : Held.values()) {
			LAYERS.put(h, byName.computeIfAbsent(h.layerName, name -> OreSpawnClient.layer(name, h.layer)));
		}
		SpecialModelRenderers.ID_MAPPER.put(OreSpawn.id("held_weapon"), Unbaked.MAP_CODEC);
	}

	/** The item's loaded model, with the 3D model in the four hand contexts if it's one of OreSpawn's held weapons. */
	static ClientItem wrap(ClientItem item, Identifier id) {
		for (Held h : Held.values()) {
			if (!h.item.equals(id)) continue;
			List<SelectItemModel.SwitchCase<ItemDisplayContext>> cases = List.of(
				hand(h, View.FP_RIGHT, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND), hand(h, View.FP_LEFT, ItemDisplayContext.FIRST_PERSON_LEFT_HAND),
				hand(h, View.TP_RIGHT, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND), hand(h, View.TP_LEFT, ItemDisplayContext.THIRD_PERSON_LEFT_HAND));
			ItemModel.Unbaked model = new SelectItemModel.Unbaked(Optional.empty(), new SelectItemModel.UnbakedSwitch<>(new DisplayContext(), cases),
				Optional.of(item.model()));
			return new ClientItem(model, item.properties(), item.registrySwapper());
		}
		return item;
	}

	private static SelectItemModel.SwitchCase<ItemDisplayContext> hand(Held h, View view, ItemDisplayContext context) {
		return new SelectItemModel.SwitchCase<>(List.of(context), new SpecialModelWrapper.Unbaked(BASE, Optional.empty(), new Unbaked(h.name(),
			view.name())));
	}

	// ---- OreSpawn's transforms ----

	/** renderSword for the swords: two or three rotations, then scale and offset. */
	private static void sword(PoseStack p, float x, float y, float z, float scale, float dx, float dy, float dz) {
		p.rotateDegrees(Axis.XP, x);
		if (y != 0.0F) p.rotateDegrees(Axis.YP, y);
		p.rotateDegrees(Axis.ZP, z);
		scaleMove(p, scale, dx, dy, dz);
	}

	/** renderSwordF5 for the swords. */
	private static void f5(PoseStack p, float scale, float dx, float dy, float dz) {
		p.rotateDegrees(Axis.XP, 90.0F);
		p.rotateDegrees(Axis.ZP, -90.0F);
		scaleMove(p, scale, dx, dy, dz);
	}

	private static void axeFirstPerson(PoseStack p) {
		p.rotateDegrees(Axis.YP, 50.0F);
		p.rotateDegrees(Axis.XP, 190.0F);
		p.rotateDegrees(Axis.ZP, 15.0F);
		scaleMove(p, 0.35F, -2, -4, -6);
	}

	/** glRotatef(180, 1, 0.25, 0): half a turn about a slightly tilted axis. */
	private static void axeThirdPerson(PoseStack p) {
		p.rotate(Axis.of(new Vector3f(1.0F, 0.25F, 0.0F).normalize()).rotationDegrees(180.0F));
		scaleMove(p, 0.35F, 3, -8, -2);
	}

	private static void scaleMove(PoseStack p, float scale, float dx, float dy, float dz) {
		p.scale(scale, scale, scale);
		p.translate(dx, dy, dz);
	}

	@Override
	public void submit(PoseStack p, SubmitNodeCollector collector, int light, int overlay, boolean foil, int outline) {
		p.pushPose();
		if (view.left) p.scale(-1.0F, 1.0F, 1.0F);
		if (view.firstPerson) {
			// 26.3 ends its first-person arm transform with a -45 degree turn that 1.7.10 didn't have.
			p.rotateDegrees(Axis.YP, 45.0F);
			p.scale(0.4F, 0.4F, 0.4F);
			p.translate(-0.5F, -0.5F, -0.5F);
			held.firstPerson.apply(p);
		} else {
			// Undo ItemInHandLayer's turn and offset, back to the arm...
			p.translate(-1.0F / 16.0F, -2.0F / 16.0F, 10.0F / 16.0F);
			p.rotateDegrees(Axis.YP, 180.0F);
			p.rotateDegrees(Axis.XP, 90.0F);
			// ...then RenderPlayer.renderEquippedItems for a "3D" custom item, and Forge's render-helper shift.
			p.translate(-1.0F / 16.0F, 7.0F / 16.0F, 1.0F / 16.0F);
			p.translate(0.0F, 0.1875F, -0.3125F);
			p.rotateDegrees(Axis.XP, 20.0F);
			p.rotateDegrees(Axis.YP, 45.0F);
			p.scale(-0.375F, -0.375F, 0.375F);
			p.translate(-0.5F, -0.5F, -0.5F);
			float weight = held.firstPersonTurn != null ? firstPersonHandWeight() : 0.0F;
			if (weight > 0.0F) p.rotate(new Quaternionf().slerp(held.firstPersonTurn, weight));
			held.thirdPerson.apply(p);
		}
		p.scale(16.0F, 16.0F, 16.0F);
		if (held == Held.SQUIDZOOKA) p.rotateDegrees(Axis.ZP, 180.0F);
		if (view.left) p.scale(-1.0F, 1.0F, 1.0F);
		float clock = (Util.getMillis() % 7000L) / 1000.0F;
		collector.submitModel(model, clock, p, RenderTypes.entityCutout(held.texture), light, overlay, outline);
		p.popPose();
	}

	@Override
	public void getExtents(Consumer<Vector3fc> output) {
		model.root().getExtentsForGui(new PoseStack(), output);
	}

	/** {@code orespawn:held_weapon}: which weapon, and which hand view. */
	record Unbaked(String weapon, String view) implements NoDataSpecialModelRenderer.Unbaked {
		static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.fieldOf("weapon").forGetter(Unbaked::weapon), Codec.STRING.fieldOf("view").forGetter(Unbaked::view)).apply(i, Unbaked::new));

		@Override
		public MapCodec<Unbaked> type() {
			return MAP_CODEC;
		}

		@Override
		public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
			Held h = Held.valueOf(weapon);
			return new HeldWeaponRenderer(h, View.valueOf(view), h.model.apply(context.entityModelSet().bakeLayer(LAYERS.get(h))));
		}
	}
}
