package orespawn.entity.weapons;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import orespawn.content.Weapons;

/**
 * OreSpawn's UltimateArrow, shot by The Ultimate Bow: it hits for its speed times OreSpawn's UltimateBowDamage (10,
 * the default), so a full-speed shot does 45 (more on a critical), and Punch knocks back as the bow's level says. Power
 * doesn't add to it (OreSpawn ignored it). It flies past OreSpawn's Hoverboards and ridden Cephadromes, Dragons and
 * horses. Players, tamed pets and the Girlfriend/Boyfriend aren't hurt (UltimateSwordPvp, off by default): the arrow
 * heals them 1 and is gone. It stays stuck for 500 ticks and can only be picked up in creative.
 */
public class UltimateArrow extends AbstractArrow {
	/** OreSpawnMain.UltimateBowDamage's default. */
	public static final double ULTIMATE_BOW_DAMAGE = 10.0;
	private int punch;
	private int groundTicks;

	public UltimateArrow(EntityType<? extends UltimateArrow> type, Level level) {
		super(type, level);
		setBaseDamage(ULTIMATE_BOW_DAMAGE);
	}

	/** EntityArrow(world, player, 3.0F): at 4.5 blocks a tick, straight (UltimateArrow ignored the spread). */
	public static UltimateArrow shoot(Level level, LivingEntity shooter) {
		UltimateArrow arrow = new UltimateArrow(Weapons.ULTIMATE_ARROW, level);
		arrow.setOwner(shooter);
		arrow.setPos(shooter.getX(), shooter.getEyeY() - 0.1F, shooter.getZ());
		Vec3 d = Vec3.directionFromRotation(shooter.getXRot(), shooter.getYRot());
		arrow.shoot(d.x, d.y, d.z, 4.5F, 0.0F);
		arrow.pickup = Pickup.CREATIVE_ONLY;
		return arrow;
	}

	public void setPunch(int punch) {
		this.punch = punch;
	}

	@Override
	protected ItemStack getDefaultPickupItem() {
		return new ItemStack(Items.ARROW);
	}

	@Override
	protected boolean canHitEntity(Entity e) {
		if (WeaponHooks.is(e, "hoverboard")) return false;
		if ((WeaponHooks.is(e, "cephadrome") || WeaponHooks.is(e, "dragon") || e instanceof AbstractHorse) && e.isVehicle()) return false;
		return super.canHitEntity(e);
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		Entity e = result.getEntity();
		if (WeaponHooks.pvpProtected(e)) {
			playSound(SoundEvents.ARROW_HIT, 1.0F, 1.2F / (random.nextFloat() * 0.2F + 0.9F));
			if (e instanceof LivingEntity living && !level().isClientSide()) living.heal(1.0F);
			discard();
			return;
		}
		super.onHitEntity(result);
	}

	@Override
	protected void doKnockback(LivingEntity mob, DamageSource source) {
		Vec3 v = getDeltaMovement();
		double horizontal = Math.sqrt(v.x * v.x + v.z * v.z);
		if (punch > 0 && horizontal > 0.0) mob.push(v.x * punch * 0.6 / horizontal, 0.1, v.z * punch * 0.6 / horizontal);
	}

	@Override
	protected void tickDespawn() {
		if (++groundTicks >= 500) discard();
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
}
