package orespawn.entity.tools;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
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
import orespawn.OreSpawn;
import orespawn.content.Tools;

import java.time.LocalDate;
import java.time.Month;

/**
 * A thrown shoe or Game Controller (OreSpawn's {@code Shoes}). It hits for 2 (a Game Controller for 6), 4 more on a
 * Creeper, only 1 on a Girlfriend or Boyfriend and nothing on a player; on Valentine's Day (14 February) everything
 * hits for 10. It's gone after it hits, with a puff of snow and red dust.
 */
public class Shoes extends ThrowableItemProjectile {
	public Shoes(EntityType<? extends Shoes> type, Level level) {
		super(type, level);
	}

	public Shoes(Level level, LivingEntity owner, ItemStack stack) {
		super(Tools.SHOES, owner, level, stack);
	}

	@Override
	protected Item getDefaultItem() {
		return Tools.RED_HEELS;
	}

	@Override
	protected void onHitEntity(EntityHitResult hit) {
		super.onHitEntity(hit);
		if (!(level() instanceof ServerLevel level)) return;
		Entity target = hit.getEntity();
		float damage = getItem().is(Tools.GAME_CONTROLLER) ? 6.0F : 2.0F;
		if (target instanceof Creeper) damage += 4.0F;
		if (target.getType() == entity("girlfriend") || target.getType() == entity("boyfriend")) damage = 1.0F;
		if (target instanceof Player) damage = 0.0F;
		LocalDate today = LocalDate.now();
		if (today.getMonth() == Month.FEBRUARY && today.getDayOfMonth() == 14) damage = 10.0F;
		target.hurtServer(level, damageSources().thrown(this, getOwner()), damage);
	}

	private static EntityType<?> entity(String id) {
		return BuiltInRegistries.ENTITY_TYPE.getOptional(OreSpawn.id(id)).orElse(null);
	}

	@Override
	protected void onHit(HitResult hit) {
		super.onHit(hit);
		if (!(level() instanceof ServerLevel level) || isRemoved()) return;
		for (int i = 0; i < 4; i++) {
			level.sendParticles(ParticleTypes.ITEM_SNOWBALL, getX(), getY(), getZ(), 1, 0.0, 0.0, 0.0, 0.0);
			level.sendParticles(DustParticleOptions.REDSTONE, getX(), getY(), getZ(), 1, 0.0, 0.0, 0.0, 0.0);
		}
		discard();
	}
}
