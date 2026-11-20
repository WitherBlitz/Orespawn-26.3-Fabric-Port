package orespawn.entity.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import orespawn.content.Tools;
import orespawn.item.tools.RockItem;
import orespawn.registry.ModSounds;

/**
 * A thrown rock (OreSpawn's {@code EntityThrownRock}); which rock it is comes from its item (1 Small Rock ... 12
 * Explosive Crystal, {@link RockItem}). Hitting a creature hurts it as its thrower's own blow and knocks it away from the
 * thrower (more upward if it died), with each rock's extras: fire, poison, slowness, weakness, or an explosion that
 * sets fires and breaks blocks if mob griefing is on. The rock is spent. Hitting a block shatters any glass and glass
 * panes next to it (one "glass dead" sound) and drops the rock to be picked up again; so does a dispensed rock (no
 * thrower) that hits a creature. A rock skipping fast and low over water bounces off it, and one still flying after 1000
 * ticks is gone.
 */
public class ThrownRock extends ThrowableItemProjectile {
	private int age;

	public ThrownRock(EntityType<? extends ThrownRock> type, Level level) {
		super(type, level);
	}

	public ThrownRock(Level level, LivingEntity owner, ItemStack stack) {
		super(Tools.ENTITY_THROWN_ROCK, owner, level, stack);
	}

	public ThrownRock(Level level, double x, double y, double z, ItemStack stack) {
		super(Tools.ENTITY_THROWN_ROCK, x, y, z, level, stack);
	}

	@Override
	protected Item getDefaultItem() {
		return Tools.ROCKS.getFirst();
	}

	public int rockType() {
		return getItem().getItem() instanceof RockItem rock ? rock.type() : 0;
	}

	@Override
	public void tick() {
		BlockPos at = blockPosition();
		super.tick();
		if (++age > 1000) discard();
		Vec3 m = getDeltaMovement();
		if (level().getBlockState(at).is(Blocks.WATER) && m.y < -0.15F && m.y > -0.55F && (float) (m.x * m.x + m.z * m.z) > 0.5F) {
			setDeltaMovement(m.x * 3.0 / 4.0, -(m.y * 3.0 / 4.0), m.z * 3.0 / 4.0);
		}
	}

	@Override
	protected void onHit(HitResult hit) {
		super.onHit(hit);
		if (!(level() instanceof ServerLevel level) || isRemoved()) return;
		int type = rockType();
		Entity owner = getOwner();
		if (hit instanceof EntityHitResult entityHit && owner != null) {
			Entity e = entityHit.getEntity();
			if (e != owner) strike(level, e, owner, type);
		} else if (type != 0) {
			if (hit instanceof BlockHitResult blockHit) shatterGlass(level, blockHit.getBlockPos());
			spawnAtLocation(level, new ItemStack(getItem().getItem()));
		}
		discard();
	}

	/** Damage, knockback ({@code ks} sideways, {@code inair} up) and each rock's extras. */
	private void strike(ServerLevel level, Entity e, Entity owner, int type) {
		DamageSource source = owner instanceof Player player ? damageSources().playerAttack(player) : damageSources().thrown(this, owner);
		float damage = switch (type) {
			case 1 -> 2.0F;
			case 2, 3, 4 -> 5.0F;
			case 5 -> 10.0F;
			case 6 -> 20.0F;
			case 7, 8 -> 40.0F;
			case 9, 10, 11 -> 150.0F;
			case 12 -> 250.0F;
			default -> 0.0F;
		};
		if (damage == 0.0F) return;
		e.hurtServer(level, source, damage);
		double ks = type == 1 || type == 5 ? 0.1 : type == 8 ? 0.5 : 0.2;
		double inair = type == 8 ? 0.055 : 0.025;
		float angle = (float) Math.atan2(e.getZ() - owner.getZ(), e.getX() - owner.getX());
		if (!e.isAlive()) inair *= 2.0;
		e.push(Math.cos(angle) * ks, inair, Math.sin(angle) * ks);
		switch (type) {
			case 3 -> e.igniteForSeconds(20.0F);
			case 4 -> effect(e, MobEffects.POISON, 100);
			case 5 -> effect(e, MobEffects.SLOWNESS, 100);
			case 6 -> effect(e, MobEffects.WEAKNESS, 100);
			case 8 -> level.explode(null, e.getX(), e.getY() + 0.25, e.getZ(), 2.1F, true, Level.ExplosionInteraction.MOB);
			case 9 -> {
				e.igniteForSeconds(50.0F);
				effect(e, MobEffects.WEAKNESS, 100);
			}
			case 10 -> {
				effect(e, MobEffects.POISON, 200);
				effect(e, MobEffects.WEAKNESS, 100);
			}
			case 11 -> {
				effect(e, MobEffects.SLOWNESS, 200);
				effect(e, MobEffects.WEAKNESS, 100);
			}
			case 12 -> {
				effect(e, MobEffects.WEAKNESS, 100);
				level.explode(null, e.getX(), e.getY() + 0.25, e.getZ(), 5.1F, true, Level.ExplosionInteraction.MOB);
			}
			default -> {
			}
		}
	}

	private static void effect(Entity e, Holder<MobEffect> effect, int ticks) {
		if (e instanceof LivingEntity living) living.addEffect(new MobEffectInstance(effect, ticks, 0));
	}

	/** Glass and glass panes in the 3x3x3 around the hit block break (OreSpawn's plain glass only). */
	private static void shatterGlass(ServerLevel level, BlockPos pos) {
		boolean played = false;
		for (BlockPos p : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
			BlockState state = level.getBlockState(p);
			if (state.is(Blocks.GLASS) || state.is(Blocks.GLASS_PANE)) {
				level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
				if (!played) {
					level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), ModSounds.get("glassdead"), SoundSource.BLOCKS, 1.0F, 1.0F);
					played = true;
				}
			}
		}
	}
}
