package orespawn.entity.tools;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import orespawn.content.Tools;
import org.jspecify.annotations.Nullable;

/**
 * A rock lying on the ground (OreSpawn's {@code RockBase}, entity "Rock"): put down by using a rock on a block, or
 * scattered over grass and sand when the world is made ({@link RockFeature}). It never moves or turns, can't burn, takes
 * no fall damage and doesn't flash red when hit; a blow from a creature makes a "pop". Its health is 1 + type / 4, and
 * when it breaks it vanishes at once and drops its rock item. A rock placed by the world picks its kind on its first
 * tick: a Small Rock usually, a Big Rock 1 time in 10, and so on down to an Explosive Crystal 1 time in 1000 (in the
 * Crystal dimension, only the four crystals). The crystals give off flame, green sparkles, smoke or firework sparks.
 */
public class RockBase extends Mob {
	private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(RockBase.class, EntityDataSerializers.INT);
	private int rockType;

	public RockBase(EntityType<? extends RockBase> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes();
	}

	public int rockType() {
		return entityData.get(TYPE);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(TYPE, 0);
	}

	/** RockBase.placeRock: sets the kind and its health. */
	public void placeRock(int type) {
		rockType = type;
		entityData.set(TYPE, type);
		getAttribute(Attributes.MAX_HEALTH).setBaseValue(1 + type / 4);
		setHealth(1 + type / 4);
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return false;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (source.is(DamageTypes.IN_WALL)) return false;
		if (source.getEntity() instanceof LivingEntity) playSound(SoundEvents.ITEM_PICKUP, 0.75F, 2.0F);
		return super.hurtServer(level, source, damage);
	}

	@Override
	public void animateHurt(float yaw) {
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	protected @Nullable SoundEvent getHurtSound(DamageSource source) {
		return null;
	}

	@Override
	protected @Nullable SoundEvent getDeathSound() {
		return null;
	}

	@Override
	protected float getSoundVolume() {
		return 0.65F;
	}

	@Override
	public void tick() {
		super.tick();
		setXRot(0.0F);
		setYRot(0.0F);
		yHeadRot = 0.0F;
		yBodyRot = 0.0F;
		if (level().isClientSide()) {
			rockType = rockType();
			crystalParticles();
			return;
		}
		if (rockType == 0) {
			if (!Tools.inCrystalDimension(level())) {
				rockType = 1;
				if (random.nextInt(10) == 0) rockType = 2;
				if (random.nextInt(20) == 0) rockType = 3;
				if (random.nextInt(30) == 0) rockType = 4;
				if (random.nextInt(40) == 0) rockType = 5;
				if (random.nextInt(50) == 0) rockType = 6;
				if (random.nextInt(100) == 0) rockType = 7;
				if (random.nextInt(200) == 0) rockType = 8;
				if (random.nextInt(500) == 0) rockType = 9;
				if (random.nextInt(500) == 0) rockType = 10;
				if (random.nextInt(500) == 0) rockType = 11;
				if (random.nextInt(1000) == 0) rockType = 12;
			} else {
				rockType = 9;
				if (random.nextInt(3) == 0) rockType = 10;
				if (random.nextInt(5) == 0) rockType = 11;
				if (random.nextInt(10) == 0) rockType = 12;
			}
			placeRock(rockType);
		}
		entityData.set(TYPE, rockType);
	}

	private void crystalParticles() {
		if (random.nextInt(20) != 0) return;
		double dx = (random.nextFloat() - random.nextFloat()) / 60.0F, dz = (random.nextFloat() - random.nextFloat()) / 60.0F;
		switch (rockType) {
			case 9 -> level().addParticle(ParticleTypes.FLAME, getX(), getY(), getZ(), dx, random.nextFloat() / 10.0F, dz);
			case 10 -> level().addParticle(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 0.25, getZ(), dx, random.nextFloat() / 2.0F, dz);
			case 11 -> level().addParticle(ParticleTypes.SMOKE, getX(), getY(), getZ(), dx, random.nextFloat() / 10.0F, dz);
			case 12 -> level().addParticle(ParticleTypes.FIREWORK, getX(), getY() + 0.25, getZ(), dx, random.nextFloat() / 5.0F, dz);
			default -> {
			}
		}
	}

	/** RockBase.onDeath: gone at once (no death animation, loot or experience), leaving its rock. */
	@Override
	public void die(DamageSource source) {
		if (!(level() instanceof ServerLevel level) || isRemoved()) return;
		if (rockType >= 1 && rockType <= Tools.ROCKS.size()) {
			ItemEntity drop = new ItemEntity(level, getX() + (random.nextFloat() - random.nextFloat()) / 3.0F, getY() + 0.25,
				getZ() + (random.nextFloat() - random.nextFloat()) / 3.0F, new ItemStack(Tools.ROCKS.get(rockType - 1)));
			level.addFreshEntity(drop);
		}
		discard();
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("ButterflyType", rockType);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		rockType = input.getIntOr("ButterflyType", 0);
	}
}
