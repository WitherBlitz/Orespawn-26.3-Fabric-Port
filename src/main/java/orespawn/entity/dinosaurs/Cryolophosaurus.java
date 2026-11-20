package orespawn.entity.dinosaurs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import orespawn.OreSpawn;
import orespawn.entity.Butterfly;
import orespawn.entity.Firefly;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.Set;

/**
 * OreSpawn's Cryolophosaurus: a small crested dinosaur that hunts at night (and any time underground, below
 * y 50). Every 5 ticks or so it chases the nearest living thing within 9 blocks and nips it for 3 now and then; it
 * leaves its big cousins (Alosaurus, T. Rex), its own kind, butterflies, moths, fireflies and a few OreSpawn oddities
 * alone, but not crickets or birds. It panics when hurt. 10 health, 1 armor. One time in ten it drops 0-2 raw chicken,
 * one in ten 0-2 uranium nuggets, one in ten 0-2 titanium nuggets (more with Looting).
 * Not ported: OreSpawn's fire resistance (10 ticks in fire before it catches light), which 26.3 has no setting for.
 */
public class Cryolophosaurus extends Monster {
	/**
	 * OreSpawn mobs it leaves alone that aren't ported yet (Ghost, Ghost Pumpkin Skelly, Cave Fisher, WTF? (Gamma Metroid),
	 * Mosquito, Rock), by their entity ids, so they're left alone once their groups register them.
	 */
	private static final Set<String> FRIENDS = Set.of("ghost", "ghost_pumpkin_skelly", "cave_fisher", "wtf", "mosquito", "rock");

	public Cryolophosaurus(EntityType<? extends Cryolophosaurus> type, Level level) {
		super(type, level);
		this.xpReward = 10;
		setPathfindingMalus(PathType.WATER, -1.0F);
	}

	/** get_mobstats("Cryolophosaurus", 10, 3, 1). */
	public static AttributeSupplier.Builder createAttributes() {
		return createMonsterAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.25)
			.add(Attributes.ATTACK_DAMAGE, 3.0).add(Attributes.ARMOR, 1.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new PanicGoal(this, 1.35F));
		goalSelector.addGoal(2, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
		goalSelector.addGoal(3, WanderGoal.wanderALot(this, 10, 1.0));
		goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(5, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (random.nextInt(200) == 1) setLastHurtByMob(null);
		if (random.nextInt(5) == 1) {
			LivingEntity prey = level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(9.0, 2.0, 9.0), this::isPrey).stream()
				.min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
			if (prey != null) {
				getNavigation().moveTo(prey, 1.25);
				if (distanceToSqr(prey) < 5.0 && (random.nextInt(12) == 0 || random.nextInt(14) == 1)) doHurtTarget(level, prey);
			}
		}
	}

	private boolean isPrey(LivingEntity e) {
		if (e == this || !e.isAlive() || !getSensing().hasLineOfSight(e)) return false;
		if (e instanceof Alosaurus || e instanceof TRex || e instanceof Cryolophosaurus || e instanceof Butterfly || e instanceof Firefly) {
			return false;
		}
		Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
		if (id.getNamespace().equals(OreSpawn.MOD_ID) && FRIENDS.contains(id.getPath())) return false;
		return !(e instanceof Player p) || !(p.isCreative() || p.isSpectator());
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return random.nextInt(6) == 0 ? ModSounds.get("cryo_living") : null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("cryo_hurt");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("cryo_death");
	}

	@Override
	protected float getSoundVolume() {
		return 0.75F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	/**
	 * Cryolophosaurus.getDropItem with 1.7.10's dropFewItems: one item picked (chicken, uranium or titanium nugget, each 1
	 * in 10, else nothing), then 0-2 of it, plus 0 to the killing player's Looting level.
	 */
	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		int pick = random.nextInt(10);
		Item item = pick == 0 ? Items.CHICKEN : pick == 1 ? ModItems.URANIUM_NUGGET : pick == 2 ? ModItems.TITANIUM_NUGGET : null;
		if (item == null) return;
		int n = random.nextInt(3);
		if (source.getEntity() instanceof Player player) {
			int looting = EnchantmentHelper.getEnchantmentLevel(
				level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING), player);
			if (looting > 0) n += random.nextInt(looting + 1);
		}
		for (int i = 0; i < n; i++) spawnAtLocation(level, item);
	}

	/** Cryolophosaurus.getCanSpawnHere: dark enough, and either night or at y 50 or below. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (level.getDifficulty() == Difficulty.PEACEFUL || !isDarkEnoughToSpawn(level, pos, random)) return false;
		return !level.getLevel().isBrightOutside() || pos.getY() <= 50;
	}
}
