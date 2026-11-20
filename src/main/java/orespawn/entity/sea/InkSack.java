package orespawn.entity.sea;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import orespawn.content.Sea;

/**
 * OreSpawn's InkSack: the ink sac an Attack Squid spits now and then instead of a water ball. It hits for 1 (4 on a
 * creeper) and half the time blinds what it hits for 5-22 seconds, then bursts in a puff of smoke with a splash. It
 * flies through Attack Squids and Water Dragons. Drawn as a spinning ink sac.
 */
public class InkSack extends ThrowableItemProjectile {
	public InkSack(EntityType<? extends InkSack> type, Level level) {
		super(type, level);
	}

	/** Fired by an Attack Squid: OreSpawn gave it no thrower. */
	public InkSack(Level level, double x, double y, double z) {
		super(Sea.INK_SACK, x, y, z, level, new ItemStack(Items.INK_SAC));
	}

	@Override
	protected Item getDefaultItem() {
		return Items.INK_SAC;
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		return !(entity instanceof AttackSquid) && !(entity instanceof WaterDragon) && super.canHitEntity(entity);
	}

	@Override
	protected void onHitEntity(EntityHitResult hit) {
		super.onHitEntity(hit);
		if (!(level() instanceof ServerLevel level)) return;
		Entity e = hit.getEntity();
		e.hurtServer(level, damageSources().thrown(this, getOwner()), e instanceof Creeper ? 4.0F : 1.0F);
		if (e instanceof LivingEntity living && random.nextInt(2) == 0) {
			living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100 + 50 * random.nextInt(8), 0), this);
		}
	}

	@Override
	protected void onHit(HitResult hit) {
		super.onHit(hit);
		if (!(level() instanceof ServerLevel level)) return;
		for (int i = 0; i < 4; i++) {
			level.sendParticles(ParticleTypes.SMOKE, getX() + random.nextFloat() - random.nextFloat(), getY() + random.nextFloat() - random.nextFloat(),
				getZ() + random.nextFloat(), 1, 0.0, 0.0, 0.0, 0.0);
		}
		playSound(SoundEvents.GENERIC_SPLASH, 0.5F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.5F);
		discard();
	}
}
