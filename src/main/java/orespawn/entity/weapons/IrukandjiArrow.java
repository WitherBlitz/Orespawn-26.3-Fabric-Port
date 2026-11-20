package orespawn.entity.weapons;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import orespawn.content.Weapons;

/**
 * OreSpawn's IrukandjiArrow, shot by the Skate String Bow: always 100 damage, plus a critical's bonus. Like the
 * Ultimate Arrow it flies past Hoverboards and ridden Cephadromes, Dragons and horses, and it spares players, tamed pets
 * and the Girlfriend/Boyfriend (it's just gone). After 50 ticks stuck in a block it drops as an Irukandji Arrow.
 * <p>
 * Picked up before that, it gives an Irukandji Arrow; OreSpawn's gave a plain arrow there, only because it inherited
 * vanilla's pickup.
 */
public class IrukandjiArrow extends AbstractArrow {
	private int groundTicks;
	private int punch;

	public IrukandjiArrow(EntityType<? extends IrukandjiArrow> type, Level level) {
		super(type, level);
	}

	/** EntityArrow(world, player, power): power x 1.5 blocks a tick, with vanilla's spread. */
	public static IrukandjiArrow shoot(Level level, LivingEntity shooter, float power) {
		IrukandjiArrow arrow = new IrukandjiArrow(Weapons.IRUKANDJI_ARROW_ENTITY, level);
		arrow.setOwner(shooter);
		arrow.setPos(shooter.getX(), shooter.getEyeY() - 0.1F, shooter.getZ());
		Vec3 d = Vec3.directionFromRotation(shooter.getXRot(), shooter.getYRot());
		arrow.shoot(d.x, d.y, d.z, power * 1.5F, 1.0F);
		arrow.pickup = Pickup.ALLOWED;
		return arrow;
	}

	/** The bow's Punch level (OreSpawn's setKnockbackStrength). */
	public void setPunch(int punch) {
		this.punch = punch;
	}

	@Override
	protected void doKnockback(LivingEntity mob, DamageSource source) {
		Vec3 v = getDeltaMovement();
		double horizontal = Math.sqrt(v.x * v.x + v.z * v.z);
		if (punch > 0 && horizontal > 0.0) mob.push(v.x * punch * 0.6 / horizontal, 0.1, v.z * punch * 0.6 / horizontal);
	}

	@Override
	protected ItemStack getDefaultPickupItem() {
		return new ItemStack(Weapons.IRUKANDJI_ARROW);
	}

	@Override
	protected boolean canHitEntity(Entity e) {
		if (WeaponHooks.is(e, "hoverboard")) return false;
		if ((WeaponHooks.is(e, "cephadrome") || WeaponHooks.is(e, "dragon") || e instanceof AbstractHorse) && e.isVehicle()) return false;
		return super.canHitEntity(e);
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		if (WeaponHooks.pvpProtected(result.getEntity())) {
			playSound(SoundEvents.ARROW_HIT, 1.0F, 1.2F / (random.nextFloat() * 0.2F + 0.9F));
			discard();
			return;
		}
		// A flat 100: base damage is set so that speed x base comes to just under 100, which vanilla rounds up.
		float speed = (float) getDeltaMovement().length();
		if (speed > 0.0F) setBaseDamage((100.0 - 1.0E-4) / speed);
		super.onHitEntity(result);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("Punch", punch);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		punch = input.getIntOr("Punch", 0);
	}

	@Override
	protected void tickDespawn() {
		if (++groundTicks == 50 && level() instanceof ServerLevel level) {
			spawnAtLocation(level, new ItemStack(Weapons.IRUKANDJI_ARROW));
			discard();
		}
	}
}
