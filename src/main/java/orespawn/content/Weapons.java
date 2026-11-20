package orespawn.content;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.resources.ResourceKey;
import orespawn.OreSpawn;
import orespawn.entity.ModEntities;
import orespawn.entity.Rat;
import orespawn.entity.weapons.Acid;
import orespawn.entity.weapons.BerthaHit;
import orespawn.entity.weapons.IceBall;
import orespawn.entity.weapons.IrukandjiArrow;
import orespawn.entity.weapons.LaserBall;
import orespawn.entity.weapons.PetRat;
import orespawn.entity.weapons.ThunderBolt;
import orespawn.entity.weapons.UltimateArrow;
import orespawn.entity.weapons.UltimateFishHook;
import orespawn.entity.weapons.WeaponHooks;
import orespawn.item.AutoEnchantItem;
import orespawn.item.AutoEnchantItem.Ench;
import orespawn.item.weapons.BerthaItem;
import orespawn.item.weapons.ChainsawItem;
import orespawn.item.weapons.ExperienceSwordItem;
import orespawn.item.weapons.GlideBootsItem;
import orespawn.item.weapons.GunItem;
import orespawn.item.weapons.HitEffectSwordItem;
import orespawn.item.weapons.HitEffectSwordItem.Effect;
import orespawn.item.weapons.IrukandjiArrowItem;
import orespawn.item.weapons.OreSpawnBowItem;
import orespawn.item.weapons.ThrownWeaponItem;
import orespawn.item.weapons.UltimateFishingRodItem;
import orespawn.item.weapons.UltimateSwordItem;
import orespawn.item.weapons.WeaponSwingPayload;
import orespawn.registry.ModItems;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * OreSpawn's weapons and the armor sets that aren't plain materials: its giant swords (Big Bertha, Slice, the Royal
 * Guardian Sword, the Attitude Adjuster), axes and Chainsaw, the special swords (Nightmare, Experience, Poison, Rat,
 * Fairy, Mantis Claw, Big Hammer), the guns and thrown ammo, the two bows, the Ultimate Fishing Rod, and the Experience,
 * Lava Eel, Moth Scale, Peacock Feather, Mobzilla Scale, Royal Guardian, Queen Scale, Pink Tourmaline and Tiger's Eye
 * armor, with their projectiles.
 * <p>
 * Stats are OreSpawnMain's defaults ({@code get_weaponstats}, {@code get_armorstats}), mapped as ModItems maps them: a
 * sword hits for 4 + the material's damage (+1 for the player's own), with 26.3's sword speed. Durability is what each
 * item set for itself (OreSpawn's setMaxDamage overrode the material's). Each item enchants itself as OreSpawn's did.
 * <p>
 * Not here: the crystal tools (another group), and the Water Ball / WaterDragon Charge, Dead Irukandji and Ink Sack,
 * which the Sea group owns (the Irukandji Arrow's recipe asks for its Dead Irukandji by id). OreSpawn's BetterFireball
 * is the Bosses group's.
 */
public final class Weapons {
	// ---- Projectiles and summons ----
	public static final EntityType<BerthaHit> BERTHA_HIT = ModEntities.misc("bertha_hit", BerthaHit::new, 0.33F, 0.33F, 4, 10);
	public static final EntityType<LaserBall> LASER_BALL = ModEntities.misc("laser_ball", LaserBall::new, 0.25F, 0.25F, 4, 10);
	public static final EntityType<IceBall> ICE_BALL_ENTITY = ModEntities.misc("ice_ball", IceBall::new, 0.25F, 0.25F, 4, 10);
	public static final EntityType<Acid> ACID_ENTITY = ModEntities.misc("acid", Acid::new, 0.25F, 0.25F, 4, 10);
	public static final EntityType<ThunderBolt> THUNDER_BOLT = ModEntities.misc("thunder_bolt", ThunderBolt::new, 0.25F, 0.25F, 4, 10);
	public static final EntityType<UltimateArrow> ULTIMATE_ARROW = ModEntities.misc("ultimate_arrow", UltimateArrow::new, 0.5F, 0.5F, 4, 20);
	public static final EntityType<IrukandjiArrow> IRUKANDJI_ARROW_ENTITY = ModEntities.misc("irukandji_arrow", IrukandjiArrow::new, 0.5F, 0.5F,
		4, 20);
	public static final EntityType<UltimateFishHook> ULTIMATE_FISH_HOOK = ModEntities.misc("ultimate_fish_hook", UltimateFishHook::new, 0.25F,
		0.25F, 4, 5);
	/** The Rat Sword's rats (OreSpawn's Rat with an owner); same size as the rat. */
	public static final EntityType<PetRat> PET_RAT = ModEntities.misc("pet_rat", PetRat::new, 0.25F, 0.5F, 8, 3);

	/** No OreSpawn weapon could be repaired with a material; the tag is left empty. */
	private static final TagKey<Item> NO_REPAIR = ModItems.repairTag("weapons");

	// ---- Enchantments (OreSpawn's onCreated / onUpdate, at its defaults; UltimateSwordEnchantmentLevel 5) ----
	private static final List<Ench> BERTHA_ENCH = List.of(new Ench(Enchantments.KNOCKBACK, 5), new Ench(Enchantments.BANE_OF_ARTHROPODS, 1),
		new Ench(Enchantments.FIRE_ASPECT, 1));
	private static final List<Ench> ULTIMATE_SWORD_ENCH = List.of(new Ench(Enchantments.LOOTING, 3), new Ench(Enchantments.SHARPNESS, 5),
		new Ench(Enchantments.SMITE, 5), new Ench(Enchantments.BANE_OF_ARTHROPODS, 5), new Ench(Enchantments.KNOCKBACK, 3),
		new Ench(Enchantments.UNBREAKING, 3), new Ench(Enchantments.FIRE_ASPECT, 2));

	// ---- Swords (get_weaponstats: uses, efficiency, damage, enchantability; uses here are the item's own) ----
	public static final Item BIG_BERTHA = ModItems.register("big_bertha",
		new BerthaItem(sword("big_bertha", 9000, 15, 496, 100), BERTHA_ENCH, BerthaHit.BERTHA));
	public static final Item SLICE = ModItems.register("slice", new BerthaItem(sword("slice", 9000, 15, 496, 100), BERTHA_ENCH, BerthaHit.BERTHA));
	public static final Item ROYAL_GUARDIAN_SWORD = ModItems.register("royal_guardian_sword", new BerthaItem(sword("royal_guardian_sword", 9000,
		15, 746, 150), List.of(new Ench(Enchantments.UNBREAKING, 5)), BerthaHit.ROYAL));
	public static final Item ATTITUDE_ADJUSTER = ModItems.register("attitude_adjuster",
		new BerthaItem(sword("attitude_adjuster", 9000, 15, 82, 100), List.of(), BerthaHit.HAMMY));
	public static final Item BATTLE_AXE = ModItems.register("battle_axe", new UltimateSwordItem(sword("battle_axe", 3000, 15, 46, 75),
		List.of(new Ench(Enchantments.LOOTING, 3), new Ench(Enchantments.UNBREAKING, 3))));
	public static final Item CHAINSAW = ModItems.register("chainsaw", new ChainsawItem(chainsaw(sword("chainsaw", 3000, 10, 56, 75))));
	public static final Item QUEEN_SCALE_BATTLE_AXE = ModItems.register("queen_scale_battle_axe",
		new UltimateSwordItem(sword("queen_scale_battle_axe", 3000, 15, 662, 100), ULTIMATE_SWORD_ENCH));
	public static final Item NIGHTMARE_SWORD = ModItems.register("nightmare_sword", new AutoEnchantItem(sword("nightmare_sword", 1200, 12, 26, 60),
		List.of(new Ench(Enchantments.KNOCKBACK, 3), new Ench(Enchantments.SHARPNESS, 1), new Ench(Enchantments.FIRE_ASPECT, 1))));
	public static final Item EXPERIENCE_SWORD = ModItems.register("experience_sword", new ExperienceSwordItem(sword("experience_sword", 1400, 10,
		6, 75), List.of(new Ench(Enchantments.SHARPNESS, 2), new Ench(Enchantments.UNBREAKING, 3))));
	public static final Item POISON_SWORD = ModItems.register("poison_sword",
		new HitEffectSwordItem(sword("poison_sword", 1300, 10, 6, 75), List.of(new Ench(Enchantments.SHARPNESS, 1)), Effect.POISON));
	public static final Item RAT_SWORD = ModItems.register("rat_sword", new HitEffectSwordItem(sword("rat_sword", 1300, 10, 6, 75), List.of(),
		Effect.RATS));
	public static final Item FAIRY_SWORD = ModItems.register("fairy_sword", new HitEffectSwordItem(sword("fairy_sword", 1300, 10, 6, 75),
		List.of(), Effect.FAIRIES));
	public static final Item MANTIS_CLAW = ModItems.register("mantis_claw", new HitEffectSwordItem(sword("mantis_claw", 1000, 10, 6, 75),
		List.of(), Effect.MANTIS));
	public static final Item BIG_HAMMER = ModItems.register("big_hammer", new HitEffectSwordItem(sword("big_hammer", 9000, 11, 11, 70), List.of(),
		Effect.HAMMER));
	// Big Bertha's three parts (crafted from boss drops, then crafted together into Big Bertha).
	public static final Item BIG_BERTHA_HANDLE = ModItems.item("big_bertha_handle", new Item.Properties());
	public static final Item BIG_BERTHA_GUARD = ModItems.item("big_bertha_guard", new Item.Properties());
	public static final Item BIG_BERTHA_BLADE = ModItems.item("big_bertha_blade", new Item.Properties());

	// ---- Guns, ammo, bows, rod ----
	public static final Item A_FREAKIN_RAY_GUN = ModItems.register("a_freakin_ray_gun",
		new GunItem(ModItems.props("a_freakin_ray_gun").durability(50), GunItem.Kind.RAY_GUN));
	public static final Item SQUIDZOOKA = ModItems.register("squidzooka", new GunItem(ModItems.props("squidzooka").durability(100),
		GunItem.Kind.SQUIDZOOKA));
	public static final Item THUNDER_STAFF = ModItems.register("thunder_staff", new GunItem(ModItems.props("thunder_staff").durability(50),
		GunItem.Kind.THUNDER_STAFF));
	/** OreSpawn's Creeper Launcher: hitting a creeper with it launches the creeper (see {@link #onAttack}). */
	public static final Item CREEPER_LAUNCHER = ModItems.item("creeper_launcher", new Item.Properties());
	public static final Item ROBOT_LASER_CHARGE = ModItems.register("robot_laser_charge",
		new ThrownWeaponItem(ModItems.props("robot_laser_charge"), () -> Weapons.LASER_BALL, SoundEvents.FIREWORK_ROCKET_LAUNCH));
	public static final Item ICE_BALL = ModItems.register("ice_ball",
		new ThrownWeaponItem(ModItems.props("ice_ball"), () -> Weapons.ICE_BALL_ENTITY, SoundEvents.ARROW_SHOOT));
	public static final Item ACID = ModItems.register("acid",
		new ThrownWeaponItem(ModItems.props("acid"), () -> Weapons.ACID_ENTITY, SoundEvents.ARROW_SHOOT));
	public static final Item IRUKANDJI_ARROW = ModItems.register("irukandji_arrow", new IrukandjiArrowItem(ModItems.props("irukandji_arrow")));
	public static final Item SKATE_STRING_BOW = ModItems.register("skate_string_bow",
		new OreSpawnBowItem(ModItems.props("skate_string_bow").durability(300).enchantable(50), List.of(), false));
	public static final Item THE_ULTIMATE_BOW = ModItems.register("the_ultimate_bow", new OreSpawnBowItem(
		ModItems.props("the_ultimate_bow").durability(1000).enchantable(50), List.of(new Ench(Enchantments.INFINITY, 1),
			new Ench(Enchantments.POWER, 5), new Ench(Enchantments.FLAME, 3), new Ench(Enchantments.PUNCH, 2)), true));
	public static final Item THE_ULTIMATE_FISHING_ROD = ModItems.register("the_ultimate_fishing_rod",
		new UltimateFishingRodItem(ModItems.props("the_ultimate_fishing_rod").durability(3000)));

	// ---- Armor (get_armorstats: durability, head, chest, legs, boots, enchantability, then the enchantment levels) ----
	// Each set's equipment asset is named after OreSpawn's worn textures (<asset>_1.png / <asset>_2.png).
	public static final Item[] EXPERIENCE = armorSet("experience", "experience", 70, 5, 9, 7, 4, 50, 0, 0, 2, 0, 1, 0, 0, 1, 0.0);
	public static final Item EXPERIENCE_HELMET = EXPERIENCE[0];
	public static final Item EXPERIENCE_CHESTPLATE = EXPERIENCE[1];
	public static final Item EXPERIENCE_LEGGINGS = EXPERIENCE[2];
	public static final Item EXPERIENCE_BOOTS = EXPERIENCE[3];
	public static final Item[] LAVA_EEL = armorSet("lava_eel", "lavaeel", 40, 2, 7, 5, 2, 35, 1, 2, 3, 2, 10, 0, 0, 2, 0.0);
	public static final Item[] MOTH_SCALE = armorSet("moth_scale", "mothscale", 50, 2, 7, 5, 2, 50, 0, 0, 3, 3, 3, 0, 0, 5, 0.0);
	public static final Item[] PEACOCK_FEATHER = armorSet("peacock_feather", "peacock", 40, 2, 5, 4, 2, 30, 0, 0, 0, 0, 0, 0, 0, 10, 0.1);
	public static final Item[] MOBZILLA_SCALE = armorSet("mobzilla_scale", "mobzilla", 1000, 7, 13, 11, 7, 150, 0, 0, 10, 10, 10, 10, 5, 10, 0.0);
	public static final Item[] ROYAL_GUARDIAN = armorSet("royal_guardian", "royal", 2000, 8, 14, 12, 8, 200, 1, 2, 10, 10, 10, 10, 5, 10, 0.1);
	public static final Item[] QUEEN_SCALE = armorSet("queen_scale", "queen", 1500, 9, 16, 14, 9, 150, 0, 0, 0, 0, 0, 0, 0, 0, 0.25);
	// Made from crystal-dimension ingots (orespawn:pink_tourmaline_ingot, orespawn:tigers_eye_ingot), which come later.
	public static final Item[] PINK_TOURMAILINE = armorSet("pink_tourmailine", "pink", 50, 3, 7, 5, 2, 40, 0, 0, 0, 0, 0, 0, 0, 0, 0.0);
	public static final Item[] TIGERS_EYE = armorSet("tigers_eye", "tigerseye", 80, 4, 8, 7, 4, 55, 0, 0, 0, 0, 0, 0, 0, 0, 0.0);

	private Weapons() {
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(PET_RAT, Rat.createAttributes());
		for (Item ammo : List.of(ROBOT_LASER_CHARGE, ICE_BALL, ACID, IRUKANDJI_ARROW)) DispenserBlock.registerProjectileBehavior(ammo);
		PayloadTypeRegistry.serverboundPlay().register(WeaponSwingPayload.TYPE, WeaponSwingPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(WeaponSwingPayload.TYPE, (payload, context) -> WeaponSwingPayload.handle(context.player()));
		AttackEntityCallback.EVENT.register(Weapons::onAttack);
	}

	/**
	 * OreSpawn's onLeftClickEntity hooks: the Creeper Launcher on a creeper, the giant weapons' "no PvP" rule, and the
	 * Chainsaw's sweep.
	 */
	private static InteractionResult onAttack(Player player, Level level, InteractionHand hand, Entity entity, EntityHitResult hit) {
		ItemStack stack = player.getItemInHand(hand);
		Item item = stack.getItem();
		if (item == CREEPER_LAUNCHER && entity instanceof Creeper creeper) {
			if (level.isClientSide()) {
				launchSmoke(level, creeper);
			} else {
				level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 2.0F, 1.2F);
				creeper.push(0.0, 4.5, 0.0);
				if (!player.hasInfiniteMaterials()) stack.shrink(1);
			}
			return InteractionResult.SUCCESS;
		}
		if ((item instanceof BerthaItem || item instanceof UltimateSwordItem) && WeaponHooks.pvpProtected(entity)) return InteractionResult.FAIL;
		if (item instanceof ChainsawItem && level instanceof ServerLevel server) ChainsawItem.sweep(server, player);
		return InteractionResult.PASS;
	}

	/** ItemCreeperLauncher's puffs: smoke, poofs and red dust rising up to 6 blocks over the creeper. */
	private static void launchSmoke(Level level, Entity e) {
		var r = level.getRandom();
		for (int i = 0; i < 6; i++) {
			for (int k = 0; k < 3; k++) {
				float f1 = r.nextFloat() - r.nextFloat();
				float f2 = 0.25F + r.nextFloat() * 6.0F;
				float f3 = r.nextFloat() - r.nextFloat();
				var particle = k == 0 ? ParticleTypes.SMOKE : k == 1 ? ParticleTypes.POOF : new DustParticleOptions(0xFF0000, 1.0F);
				level.addParticle(particle, e.getX() + f1, e.getY() + f2, e.getZ() + f3, 0.0, f2 / 4.0F, 0.0);
			}
		}
	}

	private static Item.Properties sword(String id, int uses, float efficiency, int damage, int enchantability) {
		ToolMaterial m = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, uses, efficiency, damage + 1, enchantability, NO_REPAIR);
		return ModItems.props(id).sword(m, 3.0F, -2.4F);
	}

	/**
	 * The Chainsaw cuts wood, plants and vines, leaves and cobwebs at its efficiency, 10 (UltimateSword.getStrVsBlock),
	 * and everything else at 2, two uses a block.
	 */
	private static Item.Properties chainsaw(Item.Properties p) {
		var blocks = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
		return p.component(DataComponents.TOOL, new Tool(List.of(Tool.Rule.minesAndDrops(blocks.getOrThrow(BlockTags.MINEABLE_WITH_AXE), 10.0F),
			Tool.Rule.minesAndDrops(blocks.getOrThrow(BlockTags.LEAVES), 10.0F),
			Tool.Rule.minesAndDrops(HolderSet.direct(Blocks.COBWEB.builtInRegistryHolder()), 10.0F),
			Tool.Rule.overrideSpeed(blocks.getOrThrow(BlockTags.SWORD_EFFICIENT), 10.0F)), 2.0F, 2, false));
	}

	/** One of OreSpawn's ItemOreSpawnArmor sets; {@code glide} is the boots' fall-speed limit (0 for none). */
	private static Item[] armorSet(String id, String asset, int durability, int head, int chest, int legs, int boots, int enchantability,
			int respiration, int aqua, int protection, int fire, int blast, int projectile, int unbreaking, int feather, double glide) {
		Map<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
		defense.put(ArmorType.HELMET, head);
		defense.put(ArmorType.CHESTPLATE, chest);
		defense.put(ArmorType.LEGGINGS, legs);
		defense.put(ArmorType.BOOTS, boots);
		defense.put(ArmorType.BODY, chest);
		ArmorMaterial material = new ArmorMaterial(durability, defense, enchantability, SoundEvents.ARMOR_EQUIP_DIAMOND, 0.0F, 0.0F,
			ModItems.repairTag(asset), ResourceKey.create(EquipmentAssets.ROOT_ID, OreSpawn.id(asset)));
		List<ArmorType> types = List.of(ArmorType.HELMET, ArmorType.CHESTPLATE, ArmorType.LEGGINGS, ArmorType.BOOTS);
		Item[] items = new Item[4];
		for (int i = 0; i < 4; i++) {
			ArmorType type = types.get(i);
			// ItemOreSpawnArmor's order: protection, fire, blast, projectile, unbreaking, then feather falling (boots) and
			// respiration and aqua affinity (helmet).
			List<Ench> e = new ArrayList<>();
			if (protection > 0) e.add(new Ench(Enchantments.PROTECTION, protection));
			if (fire > 0) e.add(new Ench(Enchantments.FIRE_PROTECTION, fire));
			if (blast > 0) e.add(new Ench(Enchantments.BLAST_PROTECTION, blast));
			if (projectile > 0) e.add(new Ench(Enchantments.PROJECTILE_PROTECTION, projectile));
			if (unbreaking > 0) e.add(new Ench(Enchantments.UNBREAKING, unbreaking));
			if (type == ArmorType.BOOTS && feather > 0) e.add(new Ench(Enchantments.FEATHER_FALLING, feather));
			if (type == ArmorType.HELMET && respiration > 0) e.add(new Ench(Enchantments.RESPIRATION, respiration));
			if (type == ArmorType.HELMET && aqua > 0) e.add(new Ench(Enchantments.AQUA_AFFINITY, aqua));
			String itemId = id + "_" + type.getName();
			Item.Properties props = ModItems.props(itemId).humanoidArmor(material, type);
			Item item = type == ArmorType.BOOTS && glide > 0.0 ? new GlideBootsItem(props, List.copyOf(e), glide)
				: new AutoEnchantItem(props, List.copyOf(e));
			items[i] = ModItems.register(itemId, item);
		}
		return items;
	}
}
