package orespawn.entity.bosses;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import orespawn.OreSpawn;
import orespawn.content.Bosses;
import orespawn.entity.Ignored;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

/**
 * OreSpawn's The King: a three-headed winged dragon, 22 wide and 24 high, that flies through blocks (no clip) over its
 * home. It wanders up to 120 blocks around home (picking spots at its own height, nudged up or down by the ground it
 * finds), and every few ticks hunts the nearest thing it can see within 80 blocks (players, horses, monsters, the Ender
 * Dragon, villagers and OreSpawn's other attackable creatures; never royalty, never more than 144 blocks from home). Up
 * close (within 30 blocks) it bites for 350 (more as it weakens: x2 below 2/3 health, x4 below 1/2, x8 below 1/4, x16
 * below 1/8, unless 10 player hits have landed) with a big knockback, slams the ground under it and sweeps its tail
 * behind it. Further away it fires one of three volleys from in front of its heads when facing its target: seven
 * fireballs (one really big, six big or small), three thunder bolts or five ice balls. It sometimes backs off and
 * circles. It heals 5 now and then (200 more once it has met a large stranger), and while fewer than 10 player hits
 * have landed it never drops below 2000 health. Hits are capped at 750 and it takes at most one every 20 ticks; large
 * strangers do a tenth, and small monsters that hit it simply vanish. It ignores suffocation, fall damage and
 * lightning, and never despawns. Spawned by its spawner block or a Magic Apple it guards: it stays within 120 blocks of
 * where it appeared and forgets targets 128 blocks from there.
 * <p>
 * The end game ({@link #setFree}, used when a tamed Young Adult Prince grows up into a King): it freezes, holds the
 * nearest player at 1 health while it explains itself in chat for 25 seconds, then counts down from 10 and attacks
 * for real: players, Girlfriends, Boyfriends and villagers anywhere in sight, every volley refilled each tick, purple
 * exploding bolts trailing behind it, no healing floor, no player-hit softening.
 * <p>
 * Death drops: The Prince (spawned 10 blocks up), a full set of Royal Guardian armor and the Royal Guardian Sword, then
 * 150 random items and 150 random blocks, all scattered around. 25000 experience.
 * <p>
 * Health: OreSpawn's 7000 when max_health's cap allows it (with AttributeFix). Under 26.3's own cap of 1024, The King
 * has 1024 and every hit (and heal) is scaled by 1024/7000; it takes exactly as many hits as OreSpawn's 7000-health
 * King either way, and its boss bar shows the same fraction.
 * <p>
 * Not ported yet: the thunder bolts and ice balls are the weapons group's projectiles (OreSpawn's ThunderBolt and
 * IceBall); they are looked up by id ({@code orespawn:thunder_bolt} / {@code thunderbolt}, {@code orespawn:ice_ball}
 * / {@code iceball}) and skipped until that group exists. OreSpawn's PlayNicely option (off by default) isn't ported.
 */
public class TheKing extends Monster {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(TheKing.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> IS_END = SynchedEntityData.defineId(TheKing.class, EntityDataSerializers.INT);
	/** OreSpawn's get_mobstats("TheKing", 7000, 350, 21). */
	public static final int HEALTH = 7000;
	public static final int ATTACK = 350;
	public static final int DEFENSE = 21;
	/** What hits and heals are scaled by: 1 when max_health's cap allows the full HEALTH, else cap / HEALTH. */
	private static float scale() {
		return BossUtil.healthScale(HEALTH);
	}

	private final BossBar bar = new BossBar(this, BossEvent.BossBarColor.YELLOW);
	private @Nullable BossHead head;
	private boolean hasTarget;
	private int tx;
	private int ty;
	private int tz;
	private @Nullable LivingEntity rt;
	private double attdam = ATTACK;
	private int hurtTimer;
	private int homex;
	private int homez;
	private int streamCount;
	private int streamCountL;
	private int streamCountI;
	private int ticker;
	private int playerHitCount;
	private int backoffTimer;
	private int guardMode;
	private int wingSound;
	private int largeUnknownDetected;
	private int isEnd;
	private int endCounter;

	public TheKing(EntityType<? extends TheKing> type, Level level) {
		super(type, level);
		this.xpReward = 25000;
		this.noPhysics = true;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, HEALTH).add(Attributes.MOVEMENT_SPEED, 0.62)
			.add(Attributes.ATTACK_DAMAGE, ATTACK).add(Attributes.ARMOR, DEFENSE).add(Attributes.STEP_HEIGHT, 12.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ATTACKING, false);
		builder.define(IS_END, 0);
	}

	public final int getAttacking() {
		return entityData.get(ATTACKING) ? 1 : 0;
	}

	public final void setAttacking(int attacking) {
		entityData.set(ATTACKING, attacking != 0);
	}

	/** Spawner blocks and Magic Apples: stay near the spot it appeared. */
	public void setGuardMode(int mode) {
		guardMode = mode;
	}

	/** The end game: see the class comment. */
	public void setFree() {
		isEnd = 1;
	}

	// ---- Sounds ----

	/** OreSpawn's sound events (its "MothraWings" is registered lower-case: 26.3 ids can't have capitals). */
	private static SoundEvent sound(String name) {
		try {
			return ModSounds.get(name);
		} catch (IllegalArgumentException e) {
			return ModSounds.get(name.toLowerCase(java.util.Locale.ROOT));
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return sound("king_living");
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return sound("king_hit");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return sound("trex_death");
	}

	@Override
	protected float getSoundVolume() {
		return 1.35F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	// ---- What OreSpawn turned off ----

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return false;
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected void doPush(Entity entity) {
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
		return false;
	}

	@Override
	protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
	}

	@Override
	public void thunderHit(ServerLevel level, LightningBolt lightningBolt) {
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double distance) {
		return true;
	}

	@Override
	public int getArmorValue() {
		float h = getHealth();
		float max = getMaxHealth();
		if (largeUnknownDetected != 0) return 25;
		if (playerHitCount < 10 && h < max * 2 / 3) return DEFENSE + 1;
		if (playerHitCount < 10 && h < max / 2) return DEFENSE + 2;
		if (playerHitCount < 10 && h < max / 4) return DEFENSE + 3;
		return DEFENSE;
	}

	// ---- Boss bar ----

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		bar.add(player);
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		bar.remove(player);
	}

	@Override
	public void setCustomName(@Nullable Component name) {
		super.setCustomName(name);
		bar.rename();
	}

	// ---- Ticking ----

	@Override
	public void tick() {
		super.tick();
		if (++wingSound > 30) {
			if (!level().isClientSide()) playSound(sound("mothrawings"), 1.75F, 0.75F);
			wingSound = 0;
		}
		noPhysics = true;
		Vec3 m = getDeltaMovement();
		setDeltaMovement(m.x, m.y * 0.6, m.z);
		float h = getHealth();
		float max = getMaxHealth();
		if (playerHitCount < 10 && h < max * 2 / 3) attdam = ATTACK * 2;
		if (playerHitCount < 10 && h < max / 2) attdam = ATTACK * 4;
		if (playerHitCount < 10 && h < max / 4) attdam = ATTACK * 8;
		if (playerHitCount < 10 && h < max / 8) attdam = ATTACK * 16;
		if (level().isClientSide()) {
			isEnd = entityData.get(IS_END);
			RandomSource r = level().getRandom();
			if (isEnd != 0 && r.nextInt(3) == 1) {
				double yaw = Math.toRadians(getYRot());
				Vec3 dm = getDeltaMovement();
				for (int i = 0; i < 10; i++) {
					level().addParticle(ParticleTypes.FIREWORK, getX() - 7.0 * Math.sin(yaw), getY() + 14.0, getZ() + 7.0 * Math.cos(yaw),
						(r.nextGaussian() - r.nextGaussian()) / 4.0 + dm.x * 6.0, (r.nextGaussian() - r.nextGaussian()) / 4.0,
						(r.nextGaussian() - r.nextGaussian()) / 4.0 + dm.z * 6.0);
				}
			}
		}
	}

	private int distToTargetSq() {
		int dx = tx - Mth.floor(getX());
		int dy = ty - Mth.floor(getY());
		int dz = tz - Mth.floor(getZ());
		return dx * dx + dy * dy + dz * dz;
	}

	private void setTarget(int x, int y, int z) {
		hasTarget = true;
		tx = x;
		ty = y;
		tz = z;
	}

	private boolean tooFarFromHome() {
		double d1 = getX() - homex;
		double d2 = getZ() - homez;
		return Math.sqrt(d1 * d1 + d2 * d2) > 120.0;
	}

	/** How far up or down to fly near (cx, cz): +1 for each solid block above our height, -1 for each air block below. */
	private int heightNudge(int cx, int cz) {
		int y = Mth.floor(getY());
		int dist = 0;
		for (int i = -5; i <= 5; i += 5) {
			for (int j = -5; j <= 5; j += 5) {
				if (!level().getBlockState(new BlockPos(cx + j, y, cz + i)).isAir()) {
					for (int k = 1; k < 20; k++) {
						dist++;
						if (level().getBlockState(new BlockPos(cx + j, y + k, cz + i)).isAir()) break;
					}
				} else {
					for (int k = 1; k < 20; k++) {
						dist--;
						if (!level().getBlockState(new BlockPos(cx + j, y - k, cz + i)).isAir()) break;
					}
				}
			}
		}
		dist = dist / 9 + 2;
		if (y + dist > 230) dist = 230 - y;
		return dist;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (isRemoved() || isDeadOrDying()) return;
		bar.update();
		entityData.set(IS_END, isEnd);
		if (isEnd == 1) {
			endGame(level);
			return;
		}
		int attrand = 5;
		if (isEnd == 2) {
			hurtTimer = 10;
			playerHitCount = 0;
			streamCount = 10;
			streamCountL = 10;
			streamCountI = 10;
			attrand = 3;
			guardMode = 0;
			largeUnknownDetected = 1;
			if (backoffTimer > 0) backoffTimer--;
		}
		if (hurtTimer > 0) hurtTimer--;
		if (homex == 0 && homez == 0 || guardMode == 0) {
			homex = Mth.floor(getX());
			homez = Mth.floor(getZ());
		}
		if (++ticker > 30000) ticker = 0;
		if (ticker % 80 == 0) streamCount = 10;
		if (ticker % 90 == 0) streamCountL = 5;
		if (ticker % 70 == 0) streamCountI = 8;
		if (backoffTimer > 0) backoffTimer--;
		if (playerHitCount < 10 && getHealth() < getMaxHealth() / 2) attrand = 3;
		noPhysics = true;
		if (!hasTarget) setTarget(Mth.floor(getX()), Mth.floor(getY()), Mth.floor(getZ()));

		if (tooFarFromHome() || random.nextInt(200) == 0 || distToTargetSq() < 9.1F) {
			int zdir = random.nextInt(120);
			int xdir = random.nextInt(120);
			if (random.nextInt(2) == 0) zdir = -zdir;
			if (random.nextInt(2) == 0) xdir = -xdir;
			int dist = heightNudge(homex, homez);
			setTarget(homex + xdir, Mth.floor(getY() + dist), homez + zdir);
		} else if (random.nextInt(attrand) == 0) {
			hunt(level);
		}

		if (getAttacking() != 0 && isEnd == 2) {
			double yaw = Math.toRadians(getYRot());
			Vec3 m = getDeltaMovement();
			BossUtil.purplePower(level, getX() - 10.0 * Math.sin(yaw), getY() + 14.0, getZ() + 10.0 * Math.cos(yaw), m.x * 3.0, m.z * 3.0, 10);
		}

		// Fly at the target: OreSpawn's steering, 0.7 blocks a tick at most.
		double var1 = tx + 0.5 - getX();
		double var3 = ty + 0.1 - getY();
		double var5 = tz + 0.5 - getZ();
		Vec3 m = getDeltaMovement();
		double mx = m.x + (Math.signum(var1) * 0.7 - m.x) * 0.35;
		double my = m.y + (Math.signum(var3) * 0.69999 - m.y) * 0.3;
		double mz = m.z + (Math.signum(var5) * 0.7 - m.z) * 0.35;
		setDeltaMovement(mx, my, mz);
		float var7 = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0F;
		float var8 = Mth.wrapDegrees(var7 - getYRot());
		setYRot(getYRot() + var8 / 8.0F);

		if (random.nextInt(30) == 1 && getHealth() < getMaxHealth()) {
			heal(5.0F * scale());
			if (largeUnknownDetected != 0) heal(200.0F * scale());
		}
		if (playerHitCount < 10 && getHealth() < 2000.0F * scale()) heal(2000.0F * scale() - getHealth());
	}

	private void endGame(ServerLevel level) {
		endCounter++;
		noPhysics = true;
		setDeltaMovement(Vec3.ZERO);
		hurtTimer = 10;
		Player p = findNearestPlayer(level);
		if (p != null) {
			lookAt(p, 10.0F, 10.0F);
			// As OreSpawn: the server holds the player still and turned to face it (the client keeps its own movement,
			// as it did in 1.7.10), at 1 health.
			p.setDeltaMovement(Vec3.ZERO);
			p.setYRot((float) (Math.atan2(getZ() - p.getZ(), getX() - p.getX()) * 180.0 / Math.PI) - 90.0F);
			p.setHealth(1.0F);
		}
		switch (endCounter) {
			case 10 -> msgToPlayers(level, "The King: Enough of this charade. I am done. You have shown me what I wanted to know.");
			case 80 -> msgToPlayers(level, "The King: That's right my little pet. It has all been a game. You never killed me. You can't.");
			case 160 -> msgToPlayers(level, "The King: I am the one. The only. The many. I exist within both space and time. Everywhere and always.");
			case 240 -> msgToPlayers(level, "The King: I used you to learn your ways, and I have reached my conclusion on your species.");
			case 300 -> msgToPlayers(level, "The King: You have 10 seconds to run...");
			case 320 -> msgToPlayers(level, "9.");
			case 340 -> msgToPlayers(level, "8.");
			case 360 -> msgToPlayers(level, "7.");
			case 380 -> msgToPlayers(level, "6.");
			case 400 -> msgToPlayers(level, "5.");
			case 420 -> msgToPlayers(level, "4.");
			case 440 -> msgToPlayers(level, "3.");
			case 460 -> msgToPlayers(level, "2.");
			case 480 -> msgToPlayers(level, "1.");
			case 500 -> {
				msgToPlayers(level, "The King: Prepare to die!");
				isEnd = 2;
			}
			default -> {
			}
		}
	}

	/** The attack branch of OreSpawn's updateAITasks. */
	private void hunt(ServerLevel level) {
		LivingEntity e = rt;
		if (e instanceof TheKing) {
			rt = null;
			e = null;
		}
		if (e != null) {
			double d1 = e.getX() - homex;
			double d2 = e.getZ() - homez;
			double d = Math.sqrt(d1 * d1 + d2 * d2);
			if (!e.isAlive() || random.nextInt(250) == 1 || d > 128.0 && guardMode == 1) {
				e = null;
				rt = null;
			}
			if (e != null && !myCanSee(e)) e = null;
		}
		LivingEntity f = findSomethingToAttack(level);
		if (head == null || head.isRemoved()) head = BossHead.spawn(level, Bosses.KING_HEAD, this);
		if (e == null) e = f;
		if (e == null) {
			setAttacking(0);
			streamCount = 10;
			streamCountL = 5;
			streamCountI = 8;
			return;
		}
		setAttacking(1);
		if (backoffTimer == 0) {
			int dist = (int) (e.getY() + e.getBbHeight() / 2.0F + 1.0);
			if (dist > 230) dist = 230;
			setTarget(Mth.floor(e.getX()), dist, Mth.floor(e.getZ()));
			if (random.nextInt(70) == 1) backoffTimer = 80 + random.nextInt(80);
		} else if (distToTargetSq() < 9.1F) {
			int zdir = random.nextInt(20) + 30;
			int xdir = random.nextInt(20) + 30;
			if (random.nextInt(2) == 0) zdir = -zdir;
			if (random.nextInt(2) == 0) xdir = -xdir;
			int dist = heightNudge(Mth.floor(e.getX()), Mth.floor(e.getZ()));
			setTarget(Mth.floor(e.getX()) + xdir, Mth.floor(getY() + dist), Mth.floor(e.getZ()) + zdir);
		}
		if (distanceToSqr(e) < 900.0) {
			if (random.nextInt(2) == 1) doJumpDamage(level, getX(), getY(), getZ(), 15.0, ATTACK / 4, false);
			doHurtTarget(level, e);
		}
		// The tail, 20 blocks behind.
		double headYaw = Math.toRadians(getYHeadRot());
		double dx = getX() + 20.0 * Math.sin(headYaw);
		double dz = getZ() - 20.0 * Math.cos(headYaw);
		if (random.nextInt(3) == 1) doJumpDamage(level, dx, getY() + 10.0, dz, 15.0, ATTACK / 2, true);

		if (horizontalDistanceSq(e) > 900.0) {
			int which = random.nextInt(3);
			if (which == 0 ? streamCount > 0 : which == 1 ? streamCountL > 0 : streamCountI > 0) {
				setAttacking(1);
				double rr = Math.atan2(e.getZ() - getZ(), e.getX() - getX());
				double rhdir = Math.toRadians((getYHeadRot() + 90.0F) % 360.0F);
				double pi = 3.1415926545;
				double rdd = Math.abs(rr - rhdir) % (pi * 2.0);
				if (rdd > pi) rdd -= pi * 2.0;
				if (Math.abs(rdd) < 0.5) {
					if (which == 0) fireCannon(level, e);
					else if (which == 1) fireThunder(level, e);
					else fireIce(level, e);
				}
			}
		}
	}

	private double horizontalDistanceSq(Entity e) {
		double d1 = e.getZ() - getZ();
		double d2 = e.getX() - getX();
		return d1 * d1 + d2 * d2;
	}

	// ---- Attacks ----

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (target instanceof LivingEntity e && BossUtil.isLargeUnknown(e)) {
			e.setHealth(e.getHealth() / 2.0F);
			e.hurtServer(level, damageSources().mobAttack(this), (float) attdam * 10.0F);
			largeUnknownDetected = 1;
		}
		if (target instanceof EnderDragon dragon) {
			DamageSource blast = damageSources().explosion(null, null);
			dragon.hurt(level, random.nextInt(6) == 1 ? dragon.head : dragon.getSubEntities()[2], blast, (float) attdam);
		}
		boolean hit = target.hurtServer(level, damageSources().mobAttack(this), (float) attdam);
		if (hit) {
			double ks = 3.3;
			double inair = 0.25 + random.nextFloat() * 0.25F;
			float f3 = (float) Math.atan2(target.getZ() - getZ(), target.getX() - getX());
			if (!target.isAlive() || target instanceof Player) inair *= 1.5;
			target.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
			if (target instanceof Player) target.syncVelocity = true;
		}
		return hit;
	}

	/** OreSpawn's doJumpDamage: half blast, half fall damage to everything (but royalty and ghosts) in the box. */
	private void doJumpDamage(ServerLevel level, double x, double y, double z, double dist, double damage, boolean knock) {
		AABB box = new AABB(x - dist, y - 10.0, z - dist, x + dist, y + 10.0, z + dist);
		for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
			if (e == this || !e.isAlive() || BossUtil.isRoyalty(e) || BossUtil.isType(e, "ghost") || BossUtil.isType(e, "ghost_pumpkin_skelly")) {
				continue;
			}
			e.hurtServer(level, damageSources().explosion(null, null), (float) damage / 2.0F);
			e.hurtServer(level, damageSources().fall(), (float) damage / 2.0F);
			level.playSound(null, e, SoundEvents.GENERIC_EXPLODE, getSoundSource(), 0.65F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.5F);
			if (knock) {
				float f3 = (float) Math.atan2(e.getZ() - getZ(), e.getX() - getX());
				e.push(Math.cos(f3) * 2.75, 0.65, Math.sin(f3) * 2.75);
				if (e instanceof Player) e.syncVelocity = true;
			}
		}
	}

	/** The volleys come from 32 blocks in front of the body, 14 up. */
	private double cannonX() {
		return getX() - 32.0 * Math.sin(Math.toRadians(getYRot()));
	}

	private double cannonZ() {
		return getZ() + 32.0 * Math.cos(Math.toRadians(getYRot()));
	}

	private float bowPitch() {
		return 1.0F / (getRandom().nextFloat() * 0.4F + 0.8F);
	}

	/** firecanon: one really big fireball at the target, six more around it (big, half of them small). */
	private void fireCannon(ServerLevel level, LivingEntity e) {
		if (streamCount <= 0) return;
		double yoff = 14.0;
		double cx = cannonX();
		double cz = cannonZ();
		double cy = getY() + yoff;
		BetterFireball bf = new BetterFireball(level, this, e.getX() - cx, e.getY() + e.getBbHeight() / 2.0F - cy, e.getZ() - cz);
		bf.snapTo(cx, cy, cz, getYRot(), 0.0F);
		bf.setReallyBig();
		playSound(SoundEvents.TNT_PRIMED, 1.0F, bowPitch());
		level.addFreshEntity(bf);
		for (int i = 0; i < 6; i++) {
			float r1 = 5.0F * (random.nextFloat() - random.nextFloat());
			float r2 = 3.0F * (random.nextFloat() - random.nextFloat());
			float r3 = 5.0F * (random.nextFloat() - random.nextFloat());
			bf = new BetterFireball(level, this, e.getX() - cx + r1, e.getY() + e.getBbHeight() / 2.0F - cy + r2, e.getZ() - cz + r3);
			bf.snapTo(cx, cy, cz, getYRot(), 0.0F);
			bf.setBig();
			if (random.nextInt(2) == 1) bf.setSmall();
			playSound(SoundEvents.ARROW_SHOOT, 1.0F, bowPitch());
			level.addFreshEntity(bf);
		}
		streamCount--;
	}

	/** firecanonl: three thunder bolts (OreSpawn's ThunderBolt, the weapons group's). */
	private void fireThunder(ServerLevel level, LivingEntity e) {
		if (streamCountL <= 0) return;
		playSound(SoundEvents.ARROW_SHOOT, 1.0F, bowPitch());
		throwVolley(level, e, 3, false, "thunder_bolt", "thunderbolt");
		streamCountL--;
	}

	/** firecanoni: five ice balls that leave ice where they land (OreSpawn's IceBall with setIceMaker(1)). */
	private void fireIce(ServerLevel level, LivingEntity e) {
		if (streamCountI <= 0) return;
		playSound(SoundEvents.ARROW_SHOOT, 1.0F, bowPitch());
		throwVolley(level, e, 5, true, "ice_ball", "iceball");
		streamCountI--;
	}

	private void throwVolley(ServerLevel level, LivingEntity e, int count, boolean iceMaker, String... ids) {
		EntityType<?> type = null;
		for (String id : ids) {
			type = BuiltInRegistries.ENTITY_TYPE.getOptional(OreSpawn.id(id)).orElse(null);
			if (type != null) break;
		}
		double cx = cannonX();
		double cz = cannonZ();
		double cy = getY() + 14.0;
		for (int i = 0; i < count; i++) {
			// OreSpawn rolled three offsets here and never used them.
			random.nextFloat();
			random.nextFloat();
			random.nextFloat();
			random.nextFloat();
			random.nextFloat();
			random.nextFloat();
			if (type == null || !(type.create(level, EntitySpawnReason.MOB_SUMMONED) instanceof Projectile p)) continue;
			p.snapTo(cx, cy, cz, 0.0F, 0.0F);
			double dx = e.getX() - p.getX();
			double dy = e.getY() + 0.25 - p.getY();
			double dz = e.getZ() - p.getZ();
			float lift = (float) Math.sqrt(dx * dx + dz * dz) * 0.2F;
			p.shoot(dx, dy + lift, dz, 1.4F, 4.0F);
			p.setDeltaMovement(p.getDeltaMovement().scale(3.0));
			if (iceMaker) {
				try {
					p.getClass().getMethod("setIceMaker", int.class).invoke(p, 1);
				} catch (ReflectiveOperationException ignored) {
					// That group's ice ball has no ice-making switch.
				}
			}
			level.addFreshEntity(p);
		}
	}

	// ---- Being hurt ----

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (BossUtil.unstoppable(source)) return super.hurtServer(level, source, damage);
		if (hurtTimer > 0) return false;
		float dm = Math.min(damage, 750.0F);
		if (source.is(DamageTypes.IN_WALL)) return false;
		Entity e = source.getEntity();
		if (e instanceof LivingEntity enl) {
			if (BossUtil.isLargeUnknown(enl)) {
				dm /= 10.0F;
				hurtTimer = 50;
				largeUnknownDetected = 1;
			}
			if (e instanceof Monster && enl.getBbHeight() * enl.getBbWidth() < 3.0F) {
				e.discard();
				return false;
			}
		}
		boolean ret = false;
		if (!source.is(DamageTypes.CACTUS)) {
			hurtTimer = 20;
			ret = super.hurtServer(level, source, dm * scale());
			if (e instanceof Player) playerHitCount++;
			if (e instanceof LivingEntity enl && hasTarget && !BossUtil.isRoyalty(e)) {
				rt = enl;
				int dist = Math.min((int) e.getY(), 230);
				setTarget(Mth.floor(e.getX()), dist, Mth.floor(e.getZ()));
			}
		}
		return ret;
	}

	// ---- Targets ----

	/** MyCanSee: steps from in front of the heads to the target; only air, water, leaves and vines let it see through. */
	private boolean myCanSee(LivingEntity e) {
		double yaw = Math.toRadians(getYRot());
		int nblks = 20;
		float startx = (float) (getX() - 22.0 * Math.sin(yaw));
		float starty = (float) (getY() + getBbHeight() * 7.0F / 8.0F);
		float startz = (float) (getZ() + 22.0 * Math.cos(yaw));
		float dx = (float) ((e.getX() - startx) / 20.0);
		float dy = (float) ((e.getY() + e.getBbHeight() / 2.0F - starty) / 20.0);
		float dz = (float) ((e.getZ() - startz) / 20.0);
		if (Math.abs(dx) > 1.0) {
			dy /= Math.abs(dx);
			dz /= Math.abs(dx);
			nblks = (int) (nblks * Math.abs(dx));
			dx = Mth.clamp(dx, -1.0F, 1.0F);
		}
		if (Math.abs(dy) > 1.0) {
			dx /= Math.abs(dy);
			dz /= Math.abs(dy);
			nblks = (int) (nblks * Math.abs(dy));
			dy = Mth.clamp(dy, -1.0F, 1.0F);
		}
		if (Math.abs(dz) > 1.0) {
			dy /= Math.abs(dz);
			dx /= Math.abs(dz);
			nblks = (int) (nblks * Math.abs(dz));
			dz = Mth.clamp(dz, -1.0F, 1.0F);
		}
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int i = 0; i < nblks; i++) {
			startx += dx;
			starty += dy;
			startz += dz;
			BlockState s = level().getBlockState(pos.set(Mth.floor(startx), Mth.floor(starty), Mth.floor(startz)));
			if (!s.isAir() && !s.is(Blocks.WATER) && !s.is(BlockTags.LEAVES) && !s.is(Blocks.VINE)) return false;
		}
		return true;
	}

	/** MyUtils.isAttackableNonMob: OreSpawn's own creatures it treats as fair game (by id until their groups exist). */
	private static boolean isAttackableNonMob(LivingEntity e) {
		if (e instanceof Monster || BossUtil.isRoyalty(e) || e instanceof AbstractVillager) return true;
		for (String id : List.of("mothra", "leonopteryx", "dragon", "baby_dragon", "wtf", "cephadrome", "water_dragon",
			"girlfriend", "boyfriend", "stinky")) {
			if (BossUtil.isType(e, id)) return true;
		}
		return false;
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (e == this || !e.isAlive() || BossUtil.isRoyalty(e)) return false;
		double d1 = e.getX() - homex;
		double d2 = e.getZ() - homez;
		if (Math.sqrt(d1 * d1 + d2 * d2) > 144.0) return false;
		if (Ignored.byHunters(e)) return false;
		if (isEnd == 2) {
			if (e instanceof Player p) return !p.isCreative() && !p.isSpectator();
			if (BossUtil.isType(e, "girlfriend") || BossUtil.isType(e, "boyfriend") || e instanceof AbstractVillager) return true;
		}
		if (!myCanSee(e)) return false;
		if (e instanceof Player p) return !p.isCreative() && !p.isSpectator();
		if (e instanceof AbstractHorse || e instanceof Monster || e instanceof EnderDragon) return true;
		return isAttackableNonMob(e);
	}

	private @Nullable LivingEntity findSomethingToAttack(ServerLevel level) {
		AABB box = getBoundingBox().inflate(80.0, 64.0, 80.0);
		Comparator<Entity> nearest = BossUtil.targetOrder(this);
		if (isEnd == 2) {
			List<Player> players = level.getEntitiesOfClass(Player.class, box);
			players.sort(nearest);
			for (Player p : players) {
				if (isSuitableTarget(p)) return p;
			}
		}
		List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, box);
		list.sort(nearest);
		for (LivingEntity e : list) {
			if (isSuitableTarget(e)) return e;
		}
		return null;
	}

	private @Nullable Player findNearestPlayer(ServerLevel level) {
		return level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(80.0, 64.0, 80.0)).stream()
			.min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
	}

	private void msgToPlayers(ServerLevel level, String s) {
		for (Player p : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(80.0, 64.0, 80.0))) {
			p.sendSystemMessage(Component.literal(s));
		}
	}

	// ---- Death ----

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		ThePrince prince = Bosses.THE_PRINCE.create(level, EntitySpawnReason.MOB_SUMMONED);
		if (prince != null) {
			prince.snapTo(getX(), getY() + 10.0, getZ(), random.nextFloat() * 360.0F, 0.0F);
			level.addFreshEntity(prince);
		}
		for (String id : List.of("royal_guardian_chestplate", "royal_guardian_helmet", "royal_guardian_leggings", "royal_guardian_boots",
			"royal_guardian_sword")) {
			BossUtil.dropAround(this, BossUtil.item(id), 20, 12.0, 0);
		}
		// 150 random items, then 150 random blocks, from everything registered (OreSpawn's Item / Block registries).
		var features = level.enabledFeatures();
		int items = BuiltInRegistries.ITEM.size();
		for (int j = 0; j < 150; j++) {
			Item it = BuiltInRegistries.ITEM.byId(random.nextInt(items));
			if (it != null && it != Items.AIR && it.isEnabled(features)) BossUtil.dropAround(this, new ItemStack(it), 20, 12.0, 0);
		}
		int blocks = BuiltInRegistries.BLOCK.size();
		for (int j = 0; j < 150; j++) {
			var bl = BuiltInRegistries.BLOCK.byId(random.nextInt(blocks));
			Item it = bl == null ? Items.AIR : bl.asItem();
			if (it != Items.AIR && it.isEnabled(features)) BossUtil.dropAround(this, new ItemStack(it), 20, 12.0, 0);
		}
	}

	// ---- Saving ----

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("KingHomeX", homex);
		output.putInt("KingHomeZ", homez);
		output.putInt("GuardMode", guardMode);
		output.putInt("PlayerHits", playerHitCount);
		output.putInt("IsEnd", isEnd);
		output.putInt("EndCounter", endCounter);
		BossUtil.saveMaxHealth(this, output);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		BossUtil.keepHealthFraction(this, input, HEALTH);
		homex = input.getIntOr("KingHomeX", 0);
		homez = input.getIntOr("KingHomeZ", 0);
		guardMode = input.getIntOr("GuardMode", 0);
		playerHitCount = input.getIntOr("PlayerHits", 0);
		isEnd = input.getIntOr("IsEnd", 0);
		endCounter = input.getIntOr("EndCounter", 0);
		bar.rename();
	}

	/** OreSpawn's getCanSpawnHere: always (The King only comes from spawner blocks, eggs and a grown-up Prince). */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		return true;
	}
}
