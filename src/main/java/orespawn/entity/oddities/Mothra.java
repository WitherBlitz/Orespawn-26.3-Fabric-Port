package orespawn.entity.oddities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import orespawn.content.Oddities;
import orespawn.entity.Butterfly;
import orespawn.entity.Ignored;
import orespawn.entity.ModEntities;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Mothra: a moth the size of a house (OreSpawn's butterfly model at ten times size, with the Luna Moth's
 * glow). It roams the night sky over mountains, 150 health, 8 armour, fire-proof, healing slowly. Now and then it
 * swoops over the nearest player in survival within 25 blocks (or, failing that, another creature within 15) and
 * shoots: on Easy small fireballs, on Normal small or big ones, on Hard big ones, healing a little each shot. Hit it and
 * it turns on you. It dies in an explosion, dropping an item frame, 53 gold nuggets, 25 Moth Scales, 3 blaze rods and
 * a nether star, and 20 moths fly out. As in OreSpawn it still runs the butterfly's own flight under its own.
 * <p>
 * The big fireballs are OreSpawn's BetterFireball (passing through players, so they burst on the ground by them) from
 * the group that ports it, or a vanilla ghast fireball of the same blast until that group is in. Not ported:
 * OreSpawn's "MothraPeaceful" config (off by default). Its wing sound ("MothraWings") plays once tools/gen_assets.py
 * copies it as {@code mothrawings}.
 */
public class Mothra extends Butterfly implements Enemy {
	private BlockPos mothraTarget;
	private int lastX;
	private int lastY;
	private int lastZ;
	private int stuckCount;
	private int wingSound;
	private int healthTicker = 100;

	public Mothra(EntityType<? extends Mothra> type, Level level) {
		super(type, level);
		this.xpReward = 100;
	}

	/** get_mobstats("Mothra", 150, 12, 8); speed 0.35. */
	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 150.0).add(Attributes.MOVEMENT_SPEED, 0.35)
			.add(Attributes.ATTACK_DAMAGE, 12.0).add(Attributes.ARMOR, 8.0);
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	protected float getSoundVolume() {
		return 1.5F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	@Override
	protected @Nullable SoundEvent getHurtSound(DamageSource source) {
		return null;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.GENERIC_EXPLODE.value();
	}

	@Override
	public boolean isIgnoringBlockTriggers() {
		return true;
	}

	@Override
	public void tick() {
		super.tick();
		setDeltaMovement(getDeltaMovement().multiply(1.0, 0.6, 1.0));
		if (++wingSound > 30) {
			if (!level().isClientSide()) Oddity.play(this, Oddity.optionalSound("mothrawings"), 1.0F, 1.0F);
			wingSound = 0;
		}
		if (--healthTicker <= 0) {
			if (getHealth() < getMaxHealth()) heal(1.0F);
			healthTicker = 200;
		}
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (lastX == getBlockX() && lastY == getBlockY() && lastZ == getBlockZ()) {
			stuckCount++;
		} else {
			stuckCount = 0;
			lastX = getBlockX();
			lastY = getBlockY();
			lastZ = getBlockZ();
		}
		int shoot = level.getDifficulty() == Difficulty.HARD ? 2 : 3;
		if (mothraTarget == null) mothraTarget = blockPosition();
		if (stuckCount > 50 || random.nextInt(300) == 0 || blockPosition().distSqr(mothraTarget) < 9.0) {
			// Keep within 10 blocks of the ground: find the nearest ground below on a 3x3 grid 5 apart.
			int dist = 20;
			for (int i = -5; i <= 5; i += 5) {
				for (int j = -5; j <= 5; j += 5) {
					for (int k = 1; k < 20; k++) {
						if (!level.isEmptyBlock(new BlockPos(getBlockX() + j, getBlockY() - k, getBlockZ() + i))) {
							if (k < dist) dist = k;
							break;
						}
					}
				}
			}
			int down = dist > 10 ? dist - 10 + 1 : 0;
			for (int tries = 50; tries > 0; tries--) {
				int sx = random.nextInt(2) == 0 ? -1 : 1, sz = random.nextInt(2) == 0 ? -1 : 1;
				int nz = (random.nextInt(20) + 8) * sz, nx = (random.nextInt(20) + 8) * sx;
				mothraTarget = new BlockPos(getBlockX() + nx, getBlockY() + random.nextInt(7) - 1 - down, getBlockZ() + nz);
				if (level.isEmptyBlock(mothraTarget) && Oddity.canSee(this, mothraTarget)) break;
			}
			stuckCount = 0;
		} else if (random.nextInt(10) == 0 && level.getDifficulty() != Difficulty.PEACEFUL) {
			Player player = level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(25.0, 20.0, 25.0)).stream()
				.min(java.util.Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
			LivingEntity target = null;
			if (player != null && !Oddity.creative(player)) {
				target = player;
				if (getSensing().hasLineOfSight(player)) {
					mothraTarget = BlockPos.containing(player.getX(), player.getY() + 4, player.getZ());
					if (random.nextInt(shoot) == 0) attackWithSomething(level, player);
				}
			}
			if (target == null && random.nextInt(3) == 0) {
				LivingEntity e = Oddity.nearest(this, 15.0, 20.0, this::isSuitableTarget);
				if (e != null) {
					mothraTarget = BlockPos.containing(e.getX(), e.getY() + 5, e.getZ());
					if (random.nextInt(shoot) == 0) attackWithSomething(level, e);
				}
			}
		}
		Oddity.steer(this, mothraTarget.getX() + 0.5, mothraTarget.getY() + 0.1, mothraTarget.getZ() + 0.5, 0.5, 0.7, 0.30001, 0.20001, 4.0F);
	}

	private void attackWithSomething(ServerLevel level, LivingEntity target) {
		double xz = 2.25;
		double cx = getX() - xz * Math.sin(Math.toRadians(getYRot()));
		double cz = getZ() + xz * Math.cos(Math.toRadians(getYRot()));
		double tx = target.getX(), ty = target.getY() + 0.55, tz = target.getZ();
		boolean big = switch (level.getDifficulty()) {
			case EASY -> false;
			case NORMAL -> random.nextInt(2) != 0;
			default -> true;
		};
		if (big) {
			Oddity.bigFireball(this, cx, getY(), cz, tx, ty, tz, true);
		} else {
			Oddity.smallFireball(this, cx, getY(), cz, tx, ty, tz);
			Oddity.bowSound(this);
		}
		if (getHealth() < getMaxHealth()) heal(1.0F);
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (e == this || !e.isAlive() || Ignored.byHunters(e) || !getSensing().hasLineOfSight(e)) return false;
		if (e instanceof Mothra || e instanceof Vortex || e instanceof Rotator) return false;
		if (Oddity.is(e, "brutalfly", "velocity_raptor", "cryolophosaurus", "terrible_terror", "lurking_terror", "cloud_shark", "bee", "mantis")) {
			return false;
		}
		return !Oddity.creative(e);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		Entity e = source.getEntity();
		if (e instanceof Mothra) return false;
		boolean ret = super.hurtServer(level, source, damage);
		if (e != null && mothraTarget != null) mothraTarget = BlockPos.containing(e.getX(), e.getY() + 2, e.getZ());
		return ret;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		Oddity.dropRand(level, this, Items.ITEM_FRAME, 8, 1.0);
		for (int i = 0; i < 53; i++) Oddity.dropRand(level, this, Items.GOLD_NUGGET, 8, 1.0);
		for (int i = 0; i < 25; i++) Oddity.dropRand(level, this, Oddities.MOTH_SCALE, 8, 1.0);
		for (int i = 0; i < 3; i++) Oddity.dropRand(level, this, Items.BLAZE_ROD, 8, 1.0);
		Oddity.dropRand(level, this, new ItemStack(Items.NETHER_STAR), 8, 1.0);
		for (int i = 0; i < 20; i++) Oddity.spawn(level, ModEntities.MOTH, getX() + 0.5, getY() + 1.0, getZ() + 0.5);
	}

	/**
	 * Mothra.getCanSpawnHere: at night, at y 70 or higher, with open sky (8 x 9 x 6 blocks of air above) and no other
	 * Mothra within 64 blocks.
	 */
	public static boolean checkMothraSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (pos.getY() < 70 || level.getLevel().isBrightOutside()) return false;
		for (int k = -4; k < 4; k++) {
			for (int j = -3; j < 3; j++) {
				for (int i = 1; i < 10; i++) {
					if (!level.isEmptyBlock(pos.offset(j, i, k))) return false;
				}
			}
		}
		return level.getEntitiesOfClass(Mothra.class, new AABB(pos).inflate(64.0, 32.0, 64.0)).isEmpty();
	}
}
