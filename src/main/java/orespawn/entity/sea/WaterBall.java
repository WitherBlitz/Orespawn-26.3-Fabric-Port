package orespawn.entity.sea;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import orespawn.content.Sea;

/**
 * OreSpawn's WaterBall ("WaterDragon Charge"): spat by Water Dragons and Attack Squids, and thrown from the item (by
 * hand or from a dispenser). It hits for 2 (5 on a creeper), puts out a burning target and 1 time in 10 knocks a
 * WaterDragon Charge loose from it, then bursts in bubbles and splashes. It flies through Water Dragons, Attack Squids
 * and riding players. It trails splashes and is drawn as a spinning WaterDragon Charge.
 * Not ported yet: it also flew through OreSpawn's Dragons that weren't of the plain kind (that comes with the Dragon).
 */
public class WaterBall extends ThrowableItemProjectile {
	public WaterBall(EntityType<? extends WaterBall> type, Level level) {
		super(type, level);
	}

	/** Spat by a mob: OreSpawn gave it no thrower. */
	public WaterBall(Level level, double x, double y, double z) {
		this(level, x, y, z, new ItemStack(Sea.WATERDRAGON_CHARGE));
	}

	public WaterBall(Level level, double x, double y, double z, ItemStack stack) {
		super(Sea.WATER_BALL, x, y, z, level, stack);
	}

	public WaterBall(Level level, LivingEntity owner, ItemStack stack) {
		super(Sea.WATER_BALL, owner, level, stack);
	}

	@Override
	protected Item getDefaultItem() {
		return Sea.WATERDRAGON_CHARGE;
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) level().addParticle(ParticleTypes.SPLASH, getX(), getY(), getZ(), 0.0, 0.0, 0.0);
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		if (entity instanceof WaterDragon || entity instanceof AttackSquid) return false;
		if (entity instanceof Player p && p.isPassenger()) return false;
		return super.canHitEntity(entity);
	}

	@Override
	protected void onHitEntity(EntityHitResult hit) {
		super.onHitEntity(hit);
		if (!(level() instanceof ServerLevel level)) return;
		Entity e = hit.getEntity();
		e.hurtServer(level, damageSources().thrown(this, getOwner()), e instanceof Creeper ? 5.0F : 2.0F);
		if (random.nextInt(10) == 1) e.spawnAtLocation(level, Sea.WATERDRAGON_CHARGE);
		e.extinguishFire();
	}

	@Override
	protected void onHit(HitResult hit) {
		super.onHit(hit);
		if (!(level() instanceof ServerLevel level)) return;
		for (int i = 0; i < 8; i++) {
			level.sendParticles(ParticleTypes.BUBBLE, getX() + random.nextFloat() - random.nextFloat(), getY() + random.nextFloat() - random.nextFloat(),
				getZ() + random.nextFloat(), 1, 0.0, 0.0, 0.0, 0.0);
			level.sendParticles(ParticleTypes.SPLASH, getX() + random.nextFloat() - random.nextFloat(), getY() + random.nextFloat() - random.nextFloat(),
				getZ() + random.nextFloat() - random.nextFloat(), 1, 0.0, 0.0, 0.0, 0.0);
		}
		playSound(SoundEvents.GENERIC_SPLASH, 0.5F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.5F);
		discard();
	}
}
