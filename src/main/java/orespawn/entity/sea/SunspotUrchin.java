package orespawn.entity.sea;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import orespawn.content.Sea;

/**
 * OreSpawn's thrown Sunspot Urchin: a burning, smoking urchin. It hits anything but a player for 3 (6 on a creeper) and
 * sets it alight for 5 seconds; on a block it lights a fire on the face it hit, if that spot is empty. It bursts in
 * smoke and red dust. Drawn as a spinning Sunspot Urchin, on fire.
 */
public class SunspotUrchin extends ThrowableItemProjectile {
	public SunspotUrchin(EntityType<? extends SunspotUrchin> type, Level level) {
		super(type, level);
	}

	public SunspotUrchin(Level level, double x, double y, double z, ItemStack stack) {
		super(Sea.SUNSPOT_URCHIN_ENTITY, x, y, z, level, stack);
	}

	public SunspotUrchin(Level level, LivingEntity owner, ItemStack stack) {
		super(Sea.SUNSPOT_URCHIN_ENTITY, owner, level, stack);
	}

	@Override
	protected Item getDefaultItem() {
		return Sea.SUNSPOT_URCHIN;
	}

	@Override
	public void tick() {
		super.tick();
		if (!level().isClientSide()) igniteForSeconds(1.0F);
		else level().addParticle(ParticleTypes.SMOKE, getX(), getY(), getZ(), 0.0, 0.0, 0.0);
	}

	@Override
	protected void onHitEntity(EntityHitResult hit) {
		super.onHitEntity(hit);
		if (!(level() instanceof ServerLevel level)) return;
		Entity e = hit.getEntity();
		if (e instanceof Player) return;
		e.hurtServer(level, damageSources().thrown(this, getOwner()), e instanceof Creeper ? 6.0F : 3.0F);
		if (!e.fireImmune()) e.igniteForSeconds(5.0F);
	}

	@Override
	protected void onHitBlock(BlockHitResult hit) {
		super.onHitBlock(hit);
		if (level().isClientSide()) return;
		BlockPos p = hit.getBlockPos().relative(hit.getDirection());
		if (level().isEmptyBlock(p)) level().setBlockAndUpdate(p, BaseFireBlock.getState(level(), p));
	}

	@Override
	protected void onHit(HitResult hit) {
		super.onHit(hit);
		if (!(level() instanceof ServerLevel level)) return;
		for (int i = 0; i < 5; i++) {
			level.sendParticles(ParticleTypes.SMOKE, getX(), getY(), getZ(), 0, random.nextFloat(), random.nextFloat(), random.nextFloat(), 1.0);
			level.sendParticles(DustParticleOptions.REDSTONE, getX(), getY(), getZ(), 0, random.nextFloat(), random.nextFloat(), random.nextFloat(), 1.0);
		}
		discard();
	}
}
