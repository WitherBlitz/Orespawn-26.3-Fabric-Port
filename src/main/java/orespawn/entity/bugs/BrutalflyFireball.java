package orespawn.entity.bugs;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * The fireball a Brutalfly shoots on Normal and Hard: OreSpawn's BetterFireball as the Brutalfly fired it ("setNotMe",
 * explosion power 1). It flies through players (it's meant to blow up next to them), hits anything else for 10 and sets
 * it alight for 5 s, halves the health of very large mobs (width x height over 30), lights the block face it hits, and
 * explodes (power 1, with fire; blocks break only with mobGriefing on). It burns out after 30 s.
 * <p>
 * OreSpawn shared BetterFireball between many mobs; it isn't ported as a whole yet. Not ported yet: its exceptions for
 * Mothra, the Dragon, Godzilla and OreSpawn's royalty.
 */
public class BrutalflyFireball extends Fireball {
	public BrutalflyFireball(EntityType<? extends BrutalflyFireball> type, Level level) {
		super(type, level);
	}

	@Override
	public void tick() {
		super.tick();
		if (!level().isClientSide() && tickCount > 600) discard();
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		return super.canHitEntity(entity) && entity != getOwner() && !(entity instanceof BrutalflyFireball) && !(entity instanceof Player);
	}

	@Override
	protected void onHitEntity(EntityHitResult hit) {
		super.onHitEntity(hit);
		if (level() instanceof ServerLevel level) {
			Entity e = hit.getEntity();
			if (e instanceof Mob mob && mob.getBbWidth() * mob.getBbHeight() > 30.0F) mob.setHealth(mob.getHealth() / 2.0F);
			e.hurtServer(level, damageSources().fireball(this, getOwner()), 10.0F);
			e.igniteForSeconds(5.0F);
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult hit) {
		super.onHitBlock(hit);
		if (level() instanceof ServerLevel level) {
			BlockPos pos = hit.getBlockPos().relative(hit.getDirection());
			if (level.isEmptyBlock(pos)) level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
		}
	}

	@Override
	protected void onHit(HitResult hit) {
		super.onHit(hit);
		if (level() instanceof ServerLevel level) {
			level.explode(null, getX(), getY(), getZ(), 1.0F, true, Level.ExplosionInteraction.MOB);
			discard();
		}
	}
}
