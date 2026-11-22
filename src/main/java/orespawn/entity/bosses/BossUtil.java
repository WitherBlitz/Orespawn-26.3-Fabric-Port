package orespawn.entity.bosses;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import orespawn.OreSpawn;
import orespawn.content.Bosses;
import orespawn.registry.ModItems;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.function.Consumer;

/**
 * What OreSpawn's bosses share: MyUtils.isRoyalty, the "large unknown" check they all make (anything big that isn't one
 * of the giants they know), dropItemRand (a drop scattered around the body), 1.7.10's addEnchantment, and the random
 * enchanted-gear table Mobzilla and The Kraken drop from; and health over 26.3's 1024 cap.
 */
public final class BossUtil {
	/** The max health a mob was saved with (see {@link #saveMaxHealth}). */
	private static final String SAVED_MAX_HEALTH = "OreSpawnMaxHealth";

	private BossUtil() {
	}

	// ---- Health over the cap ----

	/** max_health's cap in this game: 1024 in 26.3, more with a mod that raises it (AttributeFix's default is 1,000,000). */
	public static double healthCap() {
		return Attributes.MAX_HEALTH.value() instanceof RangedAttribute ranged ? ranged.getMaxValue() : Double.MAX_VALUE;
	}

	/**
	 * What the hits and heals of a mob with this OreSpawn health are scaled by, so it takes as many hits as in OreSpawn:
	 * 1 when the cap allows its full health, else cap / its health (it then has the cap as its health). Read each time,
	 * since the cap can be raised after the mod starts.
	 */
	public static float healthScale(float oreSpawnHealth) {
		return (float) Math.min(1.0, healthCap() / oreSpawnHealth);
	}

	/** Saves the mob's max health, so {@link #keepHealthFraction} can tell when the cap has changed since. */
	public static void saveMaxHealth(LivingEntity mob, ValueOutput output) {
		output.putFloat(SAVED_MAX_HEALTH, mob.getMaxHealth());
	}

	/**
	 * After loading: if the mob's max health isn't what it was saved with (the cap was raised, say by adding AttributeFix,
	 * or lowered by taking it out), its health keeps the same fraction of the max. Saves from before this had OreSpawn's
	 * health held to 26.3's 1024.
	 */
	public static void keepHealthFraction(LivingEntity mob, ValueInput input, float oreSpawnHealth) {
		float savedMax = input.getFloatOr(SAVED_MAX_HEALTH, Math.min(1024.0F, oreSpawnHealth));
		float savedHealth = input.getFloatOr(LivingEntity.TAG_HEALTH, mob.getHealth());
		float max = mob.getMaxHealth();
		if (savedMax <= 0.0F || savedHealth <= 0.0F || Math.abs(savedMax - max) < 0.001F) return;
		mob.setHealth(Math.min(max, savedHealth / savedMax * max));
	}

	/** MyUtils.isRoyalty: the royal family, their heads and the purple power bolts. */
	public static boolean isRoyalty(Entity e) {
		return e instanceof ThePrince || e instanceof ThePrinceTeen || e instanceof ThePrinceAdult || e instanceof ThePrincess
			|| e instanceof TheKing || e instanceof TheQueen || e instanceof PurplePower
			|| e instanceof BossHead h && (h.getType() == Bosses.KING_HEAD || h.getType() == Bosses.QUEEN_HEAD);
	}

	/**
	 * OreSpawn's spawnCreature(world, "PurplePower", x, y, z) followed by setting its motion and kind: a purple power bolt
	 * at (x, y, z) facing a random way, moving (mx, current, mz), of the given kind (see {@link PurplePower#setPurpleType}).
	 */
	public static @Nullable PurplePower purplePower(ServerLevel level, double x, double y, double z, double mx, double mz, int kind) {
		PurplePower p = PurplePower.spawn(level, x, y, z);
		if (p == null) return null;
		p.setDeltaMovement(mx, p.getDeltaMovement().y, mz);
		p.setPurpleType(kind);
		return p;
	}

	/**
	 * One of the weapons group's projectiles (OreSpawn's ThunderBolt {@code orespawn:thunder_bolt}, IceBall
	 * {@code orespawn:ice_ball}), by id, at (x, y, z) and owned by {@code owner}; not yet in the world. Null until that
	 * group is in. {@code iceMaker}: OreSpawn's IceBall.setIceMaker(1) (it leaves ice where it lands), if it has one.
	 */
	public static net.minecraft.world.entity.projectile.@Nullable Projectile weapon(ServerLevel level, String id, @Nullable Entity owner, double x,
			double y, double z, boolean iceMaker) {
		var type = BuiltInRegistries.ENTITY_TYPE.getOptional(OreSpawn.id(id)).orElse(null);
		if (type == null || !(type.create(level, net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED)
			instanceof net.minecraft.world.entity.projectile.Projectile p)) {
			return null;
		}
		p.snapTo(x, y, z, 0.0F, 0.0F);
		if (owner != null) p.setOwner(owner);
		if (iceMaker) {
			try {
				p.getClass().getMethod("setIceMaker", int.class).invoke(p, 1);
			} catch (ReflectiveOperationException e) {
				// That group's ice ball has no ice-making switch.
			}
		}
		return p;
	}

	/**
	 * OreSpawn's lob at a target: aimed at (tx, ty + 0.25, tz) with a lift of a fifth of the distance, thrown at 1.4 with a
	 * spread of 4, then three times as fast; added to the world.
	 */
	public static void lob(ServerLevel level, net.minecraft.world.entity.projectile.@Nullable Projectile p, double tx, double ty, double tz) {
		if (p == null) return;
		double dx = tx - p.getX();
		double dy = ty + 0.25 - p.getY();
		double dz = tz - p.getZ();
		float lift = (float) Math.sqrt(dx * dx + dz * dz) * 0.2F;
		p.shoot(dx, dy + lift, dz, 1.4F, 4.0F);
		p.setDeltaMovement(p.getDeltaMovement().scale(3.0));
		level.addFreshEntity(p);
	}

	/** An OreSpawn entity another content group owns, by its id (false if that group isn't ported yet). */
	public static boolean isType(Entity e, String id) {
		return BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).equals(OreSpawn.id(id));
	}

	/**
	 * The check every boss makes before its "large unknown" behaviour (halving the other's health, taking a tenth of its
	 * damage...): a living thing whose width times height is over 30 that isn't royalty, Mobzilla or its head, a Pitch
	 * Black (OreSpawn's "Nightmare") or a Kraken.
	 */
	public static boolean isLargeUnknown(Entity e) {
		return e instanceof LivingEntity && e.getBbWidth() * e.getBbHeight() > 30.0F && !isRoyalty(e) && !(e instanceof Godzilla)
			&& !(e instanceof Kraken) && !isType(e, "nightmare");
	}

	/**
	 * OreSpawn's GenericTargetSorter: nearest first, where a creeper counts as twice as close and anything bigger than 1
	 * (width x height) as that many times closer.
	 */
	public static Comparator<Entity> targetOrder(Entity self) {
		return Comparator.comparingDouble(e -> {
			double d = self.distanceToSqr(e);
			if (e instanceof Creeper) d /= 2.0;
			double weight = e.getBbHeight() * e.getBbWidth();
			if (weight > 1.0) d /= weight;
			return d;
		});
	}

	/**
	 * Damage OreSpawn's caps, timers and immunities must not stop: /kill and the void (26.3's bypasses_invulnerability
	 * damage), which 1.7.10 dealt without going through the bosses' attackEntityFrom.
	 */
	public static boolean unstoppable(DamageSource source) {
		return source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
	}

	/** The first of these OreSpawn item ids that exists (another group's item), or air. */
	public static Item item(String... ids) {
		for (String id : ids) {
			Item it = ModItems.lazy(id);
			if (it != Items.AIR) return it;
		}
		return Items.AIR;
	}

	/**
	 * OreSpawn's dropItemRand: the stack drops at x and z plus nextInt(spread) - nextInt(spread), and y plus yBase plus
	 * nextInt(yRand) (yRand 0: no random height). Empty stacks (another group's item not ported yet) are skipped.
	 */
	public static void dropAround(Mob mob, ItemStack stack, int spread, double yBase, int yRand) {
		if (stack.isEmpty() || !(mob.level() instanceof ServerLevel level)) return;
		RandomSource r = mob.getRandom();
		double x = mob.getX() + r.nextInt(spread) - r.nextInt(spread);
		double y = mob.getY() + yBase + (yRand > 0 ? r.nextInt(yRand) : 0);
		double z = mob.getZ() + r.nextInt(spread) - r.nextInt(spread);
		level.addFreshEntity(new ItemEntity(level, x, y, z, stack));
	}

	public static void dropAround(Mob mob, ItemLike item, int spread, double yBase, int yRand) {
		dropAround(mob, new ItemStack(item), spread, yBase, yRand);
	}

	/** 1.7.10's ItemStack.addEnchantment (levels past the vanilla maximum are kept, as OreSpawn's were). */
	public static void enchant(Level level, ItemStack stack, ResourceKey<Enchantment> key, int lvl) {
		if (stack.isEmpty()) return;
		stack.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key), lvl);
	}

	// ---- The random gear table (Godzilla.dropFewItems; Kraken.dropFewItems uses its first 53 entries) ----

	/**
	 * One roll of the gear table: Mobzilla rolls nextInt(80) (entries 72-79 give nothing), The Kraken nextInt(53). The
	 * item is built and enchanted as OreSpawn did, then handed to {@code drop} (the boss's own dropItemRand). Entry 41,
	 * the enchanted golden apple, OreSpawn dropped close by instead ({@code nearDrop}: x/z plus or minus 2, y + 1).
	 */
	public static void randomGear(Mob mob, int roll, Consumer<ItemStack> drop, Consumer<ItemStack> nearDrop) {
		Level level = mob.level();
		RandomSource r = mob.getRandom();
		ItemStack is;
		switch (roll) {
			case 0 -> is = stack(item("ultimate_sword"));
			case 1 -> is = stack(Items.DIAMOND);
			case 2 -> is = stack(Items.DIAMOND_BLOCK);
			case 3 -> is = sword(level, r, Items.DIAMOND_SWORD);
			case 4 -> is = tool(level, r, Items.DIAMOND_SHOVEL, false);
			case 5 -> is = tool(level, r, Items.DIAMOND_PICKAXE, true);
			case 6 -> is = tool(level, r, Items.DIAMOND_AXE, false);
			case 7 -> is = tool(level, r, Items.DIAMOND_HOE, false);
			case 8 -> is = helmet(level, r, Items.DIAMOND_HELMET, 2);
			case 9 -> is = body(level, r, Items.DIAMOND_CHESTPLATE);
			case 10 -> is = body(level, r, Items.DIAMOND_LEGGINGS);
			case 11 -> is = boots(level, r, Items.DIAMOND_BOOTS);
			case 12 -> is = stack(item("ultimate_bow", "the_ultimate_bow"));
			case 13 -> is = stack(item("ultimate_axe"));
			case 14 -> is = stack(Items.IRON_INGOT);
			case 15 -> is = stack(item("ultimate_pickaxe"));
			case 16 -> is = sword(level, r, Items.IRON_SWORD);
			case 17 -> is = tool(level, r, Items.IRON_SHOVEL, false);
			case 18 -> is = tool(level, r, Items.IRON_PICKAXE, true);
			case 19 -> is = tool(level, r, Items.IRON_AXE, false);
			case 20 -> is = tool(level, r, Items.IRON_HOE, false);
			case 21 -> is = helmet(level, r, Items.IRON_HELMET, 5);
			case 22 -> is = body(level, r, Items.IRON_CHESTPLATE);
			case 23 -> is = body(level, r, Items.IRON_LEGGINGS);
			case 24 -> is = boots(level, r, Items.IRON_BOOTS);
			case 25 -> is = stack(item("ultimate_shovel"));
			case 26 -> is = stack(Items.IRON_BLOCK);
			case 27 -> is = stack(Items.GOLD_NUGGET);
			case 28 -> is = stack(Items.GOLD_INGOT);
			case 29 -> is = stack(Items.GOLDEN_CARROT);
			case 30 -> is = sword(level, r, Items.GOLDEN_SWORD);
			case 31 -> is = tool(level, r, Items.GOLDEN_SHOVEL, false);
			case 32 -> is = tool(level, r, Items.GOLDEN_PICKAXE, true);
			case 33 -> is = tool(level, r, Items.GOLDEN_AXE, false);
			case 34 -> is = tool(level, r, Items.GOLDEN_HOE, false);
			case 35 -> is = helmet(level, r, Items.GOLDEN_HELMET, 5);
			case 36 -> is = body(level, r, Items.GOLDEN_CHESTPLATE);
			case 37 -> is = body(level, r, Items.GOLDEN_LEGGINGS);
			case 38 -> is = boots(level, r, Items.GOLDEN_BOOTS);
			case 39 -> is = stack(Items.GOLDEN_APPLE);
			case 40 -> is = stack(Items.GOLD_BLOCK);
			case 41 -> {
				nearDrop.accept(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
				return;
			}
			case 42 -> is = sword(level, r, item("experience_sword"));
			case 43 -> is = helmet(level, r, item("experience_helmet"), 5);
			case 44 -> is = body(level, r, item("experience_chestplate"));
			case 45 -> is = body(level, r, item("experience_leggings"));
			case 46 -> is = boots(level, r, item("experience_boots"));
			case 47 -> is = sword(level, r, item("amethyst_sword"));
			case 48 -> is = tool(level, r, item("amethyst_shovel"), false);
			case 49 -> is = tool(level, r, item("amethyst_pickaxe"), true);
			case 50 -> is = tool(level, r, item("amethyst_axe"), false);
			case 51 -> is = tool(level, r, item("amethyst_hoe"), false);
			case 52 -> is = stack(item("amethyst_block"));
			case 53 -> is = helmet(level, r, item("amethyst_helmet"), 5);
			case 54 -> is = body(level, r, item("amethyst_chestplate"));
			case 55 -> is = body(level, r, item("amethyst_leggings"));
			case 56 -> is = boots(level, r, item("amethyst_boots"));
			case 57 -> is = helmet(level, r, item("ruby_helmet"), 5);
			case 58 -> is = body(level, r, item("ruby_chestplate"));
			case 59 -> is = body(level, r, item("ruby_leggings"));
			case 60 -> is = boots(level, r, item("ruby_boots"));
			case 61 -> is = sword(level, r, item("ruby_sword"));
			case 62 -> is = tool(level, r, item("ruby_shovel"), false);
			case 63 -> is = tool(level, r, item("ruby_pickaxe"), true);
			case 64 -> is = tool(level, r, item("ruby_axe"), false);
			case 65 -> is = tool(level, r, item("ruby_hoe"), false);
			case 66 -> is = stack(item("ruby_block"));
			case 67 -> is = helmet(level, r, item("ultimate_helmet"), 5);
			case 68 -> is = body(level, r, item("ultimate_chestplate"));
			case 69 -> is = body(level, r, item("ultimate_leggings"));
			case 70 -> is = boots(level, r, item("ultimate_boots"));
			case 71 -> is = tool(level, r, item("ultimate_shovel"), false);
			case 73 -> is = tool(level, r, item("ultimate_pickaxe"), true);
			case 74 -> is = tool(level, r, item("ultimate_axe"), false);
			case 75 -> is = tool(level, r, item("ultimate_hoe"), false);
			default -> {
				return;
			}
		}
		drop.accept(is);
	}

	private static ItemStack stack(ItemLike item) {
		return new ItemStack(item);
	}

	private static void maybe(Level level, RandomSource r, ItemStack s, int chance, ResourceKey<Enchantment> e, int base, int spread) {
		if (r.nextInt(chance) == 1) enchant(level, s, e, base + r.nextInt(spread));
	}

	/** Swords: sharpness, bane of arthropods, knockback, looting 1-5 (1 in 6 each), unbreaking 2-5 (1 in 2), fire aspect 1-5, sharpness again. */
	private static ItemStack sword(Level level, RandomSource r, ItemLike item) {
		ItemStack s = stack(item);
		maybe(level, r, s, 6, Enchantments.SHARPNESS, 1, 5);
		maybe(level, r, s, 6, Enchantments.BANE_OF_ARTHROPODS, 1, 5);
		maybe(level, r, s, 6, Enchantments.KNOCKBACK, 1, 5);
		maybe(level, r, s, 6, Enchantments.LOOTING, 1, 5);
		maybe(level, r, s, 2, Enchantments.UNBREAKING, 2, 4);
		maybe(level, r, s, 6, Enchantments.FIRE_ASPECT, 1, 5);
		maybe(level, r, s, 6, Enchantments.SHARPNESS, 1, 5);
		return s;
	}

	/** Shovels, axes, hoes: unbreaking 2-5 (1 in 2), efficiency 1-5 (1 in 6); pickaxes also fortune 1-5 (1 in 6). */
	private static ItemStack tool(Level level, RandomSource r, ItemLike item, boolean pickaxe) {
		ItemStack s = stack(item);
		maybe(level, r, s, 2, Enchantments.UNBREAKING, 2, 4);
		maybe(level, r, s, 6, Enchantments.EFFICIENCY, 1, 5);
		if (pickaxe) maybe(level, r, s, 6, Enchantments.FORTUNE, 1, 5);
		return s;
	}

	/** Chestplates and leggings: protection, blast, fire, projectile protection 1-5 (1 in 6 each), unbreaking 2-5 (1 in 2). */
	private static ItemStack body(Level level, RandomSource r, ItemLike item) {
		ItemStack s = stack(item);
		maybe(level, r, s, 6, Enchantments.PROTECTION, 1, 5);
		maybe(level, r, s, 6, Enchantments.BLAST_PROTECTION, 1, 5);
		maybe(level, r, s, 6, Enchantments.FIRE_PROTECTION, 1, 5);
		maybe(level, r, s, 6, Enchantments.PROJECTILE_PROTECTION, 1, 5);
		maybe(level, r, s, 2, Enchantments.UNBREAKING, 2, 4);
		return s;
	}

	/** Helmets: as chestplates, then respiration (1 to respirationMax) and aqua affinity 1-5, 1 in 6 each. */
	private static ItemStack helmet(Level level, RandomSource r, ItemLike item, int respirationMax) {
		ItemStack s = body(level, r, item);
		maybe(level, r, s, 6, Enchantments.RESPIRATION, 1, respirationMax);
		maybe(level, r, s, 6, Enchantments.AQUA_AFFINITY, 1, 5);
		return s;
	}

	/** Boots: feather falling 5-9 (1 in 6), unbreaking 2-5 (1 in 2). */
	private static ItemStack boots(Level level, RandomSource r, ItemLike item) {
		ItemStack s = stack(item);
		maybe(level, r, s, 6, Enchantments.FEATHER_FALLING, 5, 5);
		maybe(level, r, s, 2, Enchantments.UNBREAKING, 2, 4);
		return s;
	}
}
