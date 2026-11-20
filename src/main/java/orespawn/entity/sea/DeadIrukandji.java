package orespawn.entity.sea;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import orespawn.content.Sea;

/**
 * OreSpawn's thrown Dead Irukandji (its LaserBall in "Irukandji" mode): whatever it hits takes 100 damage. If it hits
 * a block instead it drops back as a Dead Irukandji item. It falls apart after 10 seconds in the air. Drawn as a
 * spinning Dead Irukandji.
 */
public class DeadIrukandji extends ThrowableItemProjectile {
	private int ticksAlive;

	public DeadIrukandji(EntityType<? extends DeadIrukandji> type, Level level) {
		super(type, level);
	}

	public DeadIrukandji(Level level, double x, double y, double z, ItemStack stack) {
		super(Sea.DEAD_IRUKANDJI_ENTITY, x, y, z, level, stack);
	}

	public DeadIrukandji(Level level, LivingEntity owner, ItemStack stack) {
		super(Sea.DEAD_IRUKANDJI_ENTITY, owner, level, stack);
	}

	@Override
	protected Item getDefaultItem() {
		return Sea.DEAD_IRUKANDJI;
	}

	@Override
	public void tick() {
		if (++ticksAlive > 200) {
			discard();
			return;
		}
		super.tick();
	}

	@Override
	protected void onHitEntity(EntityHitResult hit) {
		super.onHitEntity(hit);
		if (level() instanceof ServerLevel level) hit.getEntity().hurtServer(level, damageSources().thrown(this, getOwner()), 100.0F);
	}

	@Override
	protected void onHitBlock(BlockHitResult hit) {
		super.onHitBlock(hit);
		if (level() instanceof ServerLevel level) spawnAtLocation(level, Sea.DEAD_IRUKANDJI);
	}

	@Override
	protected void onHit(HitResult hit) {
		super.onHit(hit);
		if (!level().isClientSide()) discard();
	}
}
