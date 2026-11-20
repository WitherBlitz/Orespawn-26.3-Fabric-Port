package orespawn.entity.oddities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import orespawn.content.Oddities;
import orespawn.entity.Ignored;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModBlocks;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Jumpy Bug (its class is TrooperBug): a huge armoured swamp beetle that comes out at night (and now and
 * then by day). It goes for the nearest creature within 12 blocks (not endermen, creepers or its own kind, nor
 * creative players), leaping at it and swatting it hard away (get_mobstats("TrooperBug", 200, 20, 15)); while it
 * fights it now and then calls up a Spit Bug. Falls and cactus don't hurt it, and a hit makes it immune for a second.
 * Drops a Jumpy Bug Scale, an item frame, 2-6 amethysts and 1-5 rolls for amethyst gear (a block, tools or armour,
 * often enchanted, sometimes beyond the usual levels).
 */
public class TrooperBug extends Monster {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(TrooperBug.class, EntityDataSerializers.BOOLEAN);
	private int hurtTimer;

	public TrooperBug(EntityType<? extends TrooperBug> type, Level level) {
		super(type, level);
		this.xpReward = 150;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 200.0).add(Attributes.MOVEMENT_SPEED, 0.4)
			.add(Attributes.ATTACK_DAMAGE, 20.0).add(Attributes.ARMOR, 15.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new MoveThroughVillageGoal(this, 0.9, false, 4, () -> false));
		goalSelector.addGoal(2, WanderGoal.wanderALot(this, 14, 1.0));
		goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0F));
		goalSelector.addGoal(4, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ATTACKING, false);
	}

	public boolean attacking() {
		return entityData.get(ATTACKING);
	}

	@Override
	public void tick() {
		super.tick();
		if (!onGround()) getNavigation().stop();
	}

	/** TrooperBug.jump: a huge hop forward. */
	@Override
	public void jumpFromGround() {
		float f = 0.2F + Math.abs(random.nextFloat() * 0.45F);
		setDeltaMovement(getDeltaMovement().add(-f * Math.sin(Math.toRadians(getYHeadRot())), 1.15F, f * Math.cos(Math.toRadians(getYHeadRot()))));
		setPos(getX(), getY() + 1.5, getZ());
		needsSync = true;
	}

	private void jumpAt(LivingEntity e) {
		float f = 0.3F + Math.abs(random.nextFloat() * 0.25F);
		double d = Math.atan2(e.getX() - getX(), e.getZ() - getZ());
		setDeltaMovement(getDeltaMovement().add(f * Math.sin(d), 1.25, f * Math.cos(d)));
		setPos(getX(), getY() + 1.25, getZ());
		needsSync = true;
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return random.nextInt(4) == 0 ? ModSounds.get("clatter") : null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("crunch");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("emperorscorpion_death");
	}

	@Override
	protected float getSoundVolume() {
		return 1.5F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return !isPersistenceRequired();
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (!super.doHurtTarget(level, target)) return false;
		if (target instanceof LivingEntity) {
			double inair = target.isRemoved() || target instanceof Player ? 0.4 : 0.2;
			float a = (float) Math.atan2(target.getZ() - getZ(), target.getX() - getX());
			target.push(Math.cos(a) * 1.8, inair, Math.sin(a) * 1.8);
		}
		return true;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (hurtTimer > 0) return false;
		if (source.is(DamageTypes.CACTUS) || source.is(DamageTypes.FALL)) return false;
		boolean ret = super.hurtServer(level, source, damage);
		hurtTimer = 20;
		if (source.getEntity() instanceof Mob e) {
			setTarget(e);
			getNavigation().moveTo(e, 1.2);
			ret = true;
		}
		return ret;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (hurtTimer > 0) hurtTimer--;
		if (random.nextInt(5) == 0) {
			LivingEntity e = getTarget();
			if (e != null && !e.isAlive()) {
				setTarget(null);
				e = null;
			}
			if (e == null) e = Oddity.nearest(this, 12.0, 7.0, this::isSuitableTarget);
			if (e != null) {
				lookAt(e, 10.0F, 10.0F);
				double reach = 5.0F + e.getBbWidth() / 2.0F;
				if (random.nextInt(10) == 1 && onGround()) {
					jumpAt(e);
				} else if (distanceToSqr(e) < reach * reach) {
					entityData.set(ATTACKING, true);
					if (random.nextInt(6) == 0 || random.nextInt(7) == 1) {
						doHurtTarget(level, e);
						if (random.nextInt(3) == 1) e.playSound(ModSounds.get("scorpion_attack"), 1.4F, 1.0F);
						else e.playSound(ModSounds.get("clatter"), 1.0F, 1.0F);
					}
				} else if (onGround()) {
					getNavigation().moveTo(e, 1.2);
				}
				if (random.nextInt(30) == 1) {
					Oddity.spawn(level, Oddities.SPIT_BUG, (getX() + e.getX()) / 2.0 + random.nextInt(5) - random.nextInt(5),
						(getY() + e.getY()) / 2.0 + 1.01, (getZ() + e.getZ()) / 2.0 + random.nextInt(5) - random.nextInt(5));
				}
			} else {
				entityData.set(ATTACKING, false);
			}
		}
		if (random.nextInt(150) == 1 && getHealth() < getMaxHealth()) heal(1.0F);
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (e == this || !e.isAlive() || Ignored.byHunters(e) || !getSensing().hasLineOfSight(e) || Oddity.creative(e)) return false;
		if (e instanceof Enderman || e instanceof Creeper || e instanceof SpitBug || e instanceof TrooperBug) return false;
		return !Oddity.is(e, "ender_reaper", "ender_knight", "hydrolisc");
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		drop(level, new ItemStack(Oddities.JUMPY_BUG_SCALE));
		drop(level, new ItemStack(Items.ITEM_FRAME));
		for (int i = 2 + random.nextInt(5); i > 0; i--) drop(level, new ItemStack(ModItems.AMETHYST));
		for (int i = 1 + random.nextInt(5); i > 0; i--) {
			switch (random.nextInt(14)) {
				case 2 -> drop(level, new ItemStack(ModBlocks.AMETHYST_BLOCK));
				case 3 -> drop(level, gear(level, "amethyst_sword", new Ench(Enchantments.SHARPNESS, 6, 1, 5), new Ench(Enchantments.BANE_OF_ARTHROPODS, 6, 1, 5),
					new Ench(Enchantments.KNOCKBACK, 6, 1, 5), new Ench(Enchantments.LOOTING, 6, 1, 5), new Ench(Enchantments.UNBREAKING, 2, 2, 4),
					new Ench(Enchantments.FIRE_ASPECT, 6, 1, 5), new Ench(Enchantments.SHARPNESS, 6, 1, 5)));
				case 4 -> drop(level, gear(level, "amethyst_shovel", UNBREAKING, EFFICIENCY));
				case 5 -> drop(level, gear(level, "amethyst_pickaxe", UNBREAKING, EFFICIENCY, new Ench(Enchantments.FORTUNE, 6, 1, 5)));
				case 6 -> drop(level, gear(level, "amethyst_axe", UNBREAKING, EFFICIENCY));
				case 7 -> drop(level, gear(level, "amethyst_hoe", UNBREAKING, EFFICIENCY));
				case 8 -> drop(level, gear(level, "amethyst_helmet", PROTECTION, BLAST, FIRE, PROJECTILE, UNBREAKING,
					new Ench(Enchantments.RESPIRATION, 6, 1, 2), new Ench(Enchantments.AQUA_AFFINITY, 6, 1, 5)));
				case 9 -> drop(level, gear(level, "amethyst_chestplate", PROTECTION, BLAST, FIRE, PROJECTILE, UNBREAKING));
				case 10 -> drop(level, gear(level, "amethyst_leggings", PROTECTION, BLAST, FIRE, PROJECTILE, UNBREAKING));
				case 11 -> drop(level, gear(level, "amethyst_boots", new Ench(Enchantments.FEATHER_FALLING, 6, 5, 5), UNBREAKING));
				default -> {
				}
			}
		}
	}

	/** An enchantment OreSpawn adds one time in {@code chance} at level {@code min} to {@code min + spread - 1}. */
	private record Ench(ResourceKey<Enchantment> key, int chance, int min, int spread) {}

	private static final Ench UNBREAKING = new Ench(Enchantments.UNBREAKING, 2, 2, 4);
	private static final Ench EFFICIENCY = new Ench(Enchantments.EFFICIENCY, 6, 1, 5);
	private static final Ench PROTECTION = new Ench(Enchantments.PROTECTION, 6, 1, 5);
	private static final Ench BLAST = new Ench(Enchantments.BLAST_PROTECTION, 6, 1, 5);
	private static final Ench FIRE = new Ench(Enchantments.FIRE_PROTECTION, 6, 1, 5);
	private static final Ench PROJECTILE = new Ench(Enchantments.PROJECTILE_PROTECTION, 6, 1, 5);

	private ItemStack gear(ServerLevel level, String id, Ench... enchants) {
		Item item = ModItems.byName(id);
		ItemStack stack = new ItemStack(item);
		var lookup = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		for (Ench e : enchants) {
			if (random.nextInt(e.chance()) == 1) stack.enchant(lookup.getOrThrow(e.key()), e.min() + random.nextInt(e.spread()));
		}
		return stack;
	}

	/** TrooperBug.dropItemRand: within 4 blocks. */
	private void drop(ServerLevel level, ItemStack stack) {
		Oddity.dropRand(level, this, stack, 5, 1.0);
	}

	/**
	 * TrooperBug.getCanSpawnHere: dark enough for a monster, at night (or one time in ten by day), with 4 x 4 x 4 blocks
	 * of air above. Spawners always may.
	 */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (!Monster.isDarkEnoughToSpawn(level, pos, random)) return false;
		if (level.getLevel().isBrightOutside() && random.nextInt(20) > 1) return false;
		for (int k = -2; k < 2; k++) {
			for (int j = -2; j < 2; j++) {
				for (int i = 1; i < 5; i++) {
					if (!level.isEmptyBlock(pos.offset(j, i, k))) return false;
				}
			}
		}
		return true;
	}
}
