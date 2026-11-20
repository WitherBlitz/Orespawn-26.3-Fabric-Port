package orespawn.entity.bosses;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import orespawn.OreSpawn;
import orespawn.content.Bosses;
import orespawn.entity.Ignored;
import orespawn.entity.ModEntities;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * OreSpawn's The Queen: The King's 22 x 24 winged mate, who flies through blocks (no clip) round her home (120 blocks;
 * spawner blocks and Magic Apples make her guard it) and has a mood. Happy (her bright texture) she ignores everything,
 * flies near The King when one is within 64 blocks, and every 100 ticks or so beautifies the land around her when mob
 * griefing is on (flowers on grass, grass on dirt, dirt on stone, cactus or dirt on sand, lava to water) and fills the
 * air with butterflies and birds. Any hit makes her angry for good (until she's back to full health, then 1 in 500 a
 * tick); she then grows her head hitbox ({@link BossHead}) and, every few ticks, hunts what she can see within 80 blocks
 * (players, horses, monsters, the Ender Dragon, villagers and OreSpawn's attackable creatures, never royalty, never
 * more than 144 blocks from home). Up close (30 blocks) she bites for 225 (x20 below 3/4 health, x100 below 1/2, x500
 * below 1/3, x1000 below 1/4 unless 10 player hits have landed), knocks it away, keeps it from healing while she's
 * near, slams the ground and sweeps her tail; anything over 30 (width x height) first loses a quarter of its health
 * and takes a second bite. From further she fires seven fireballs (one really big) or three thunder bolts when facing
 * it, and when her power peaks she looses 15 purple bolts (45 while fewer than 10 player hits have landed). Explosions
 * heal her by half their damage; small monsters that hit her vanish; purple bolts can't hurt her. She heals 5 (55
 * while under 10 player hits) now and then and never drops below 2000 health while fewer than 10 player hits have
 * landed. Hits are capped at 750, one every 20 ticks. 6000 health, 225 damage, 21 armor (more as she weakens),
 * 25000 experience; she never despawns.
 * <p>
 * Death drops: the Royal Guardian Sword, The Prince's spawn egg, 56 each of The Queen Scales, raw beef, bones and
 * rotten flesh, and The Princess, spawned 10 blocks up.
 * <p>
 * Health: 26.3 caps max health at 1024, so The Queen has 1024 and every hit and heal is scaled by 1024/6000.
 * <p>
 * Not ported yet: the thunder bolts are the weapons group's ({@code orespawn:thunder_bolt}, looked up by id, nothing until
 * it's in); OreSpawn's blue and pink flowers and the Crystal
 * dimension's flowers are looked up by id and skipped until their groups exist. PlayNicely isn't ported.
 */
public class TheQueen extends Monster {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(TheQueen.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> MOOD = SynchedEntityData.defineId(TheQueen.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> POWER = SynchedEntityData.defineId(TheQueen.class, EntityDataSerializers.INT);
	/** OreSpawn's get_mobstats("TheQueen", 6000, 225, 21). */
	public static final int HEALTH = 6000;
	public static final int ATTACK = 225;
	public static final int DEFENSE = 21;
	/** 26.3's max health is 1024: OreSpawn health and damage are scaled by this. */
	private static final float SCALE = 1024.0F / HEALTH;
	/** The flowers her good mood plants (OreSpawn's red and yellow, its own blue and pink, and the Crystal flowers). */
	private static final String[][] FLOWERS = {{"minecraft:poppy"}, {"minecraft:dandelion"},
		{"orespawn:blue_flower", "orespawn:flower_blue"}, {"orespawn:pink_flower", "orespawn:flower_pink"}, {"orespawn:red_crystal_flower"},
		{"orespawn:green_crystal_flower"}, {"orespawn:blue_crystal_flower"}, {"orespawn:yellow_crystal_flower"}};

	private final BossBar bar = new BossBar(this, BossEvent.BossBarColor.PURPLE);
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
	private int ticker;
	private int playerHitCount;
	private int backoffTimer;
	private int guardMode;
	private int wingSound;
	private int attackLevel = 1;
	private @Nullable LivingEntity ev;
	private float evh;
	private int mood;
	private int alwaysMad;

	public TheQueen(EntityType<? extends TheQueen> type, Level level) {
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
		builder.define(MOOD, 0);
		builder.define(POWER, 0);
	}

	public final int getAttacking() {
		return entityData.get(ATTACKING) ? 1 : 0;
	}

	public final void setAttacking(int attacking) {
		entityData.set(ATTACKING, attacking != 0);
	}

	/** Happy: mood 0 (synced; her renderer draws the bright texture then). */
	public boolean isHappy() {
		return entityData.get(MOOD) == 0;
	}

	/** OreSpawn's getPower: her attack level, synced every 10 ticks (sparks over 800; the model's power cubes). */
	public int power() {
		return entityData.get(POWER);
	}

	/** Spawner blocks and Magic Apples: stay near the spot she appeared. */
	public void setGuardMode(int mode) {
		guardMode = mode;
	}

	/** Magic Apples: always angry. */
	public void setBadMood(int mad) {
		alwaysMad = mad;
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

	/** getTotalArmorValue, with OreSpawn's order of checks. */
	@Override
	public int getArmorValue() {
		float h = getHealth();
		float max = getMaxHealth();
		if (playerHitCount < 10 && h < max * 2 / 3) return DEFENSE + 2;
		if (playerHitCount < 10 && h < max / 2) return DEFENSE + 3;
		if (playerHitCount < 10 && h < max / 3) return DEFENSE + 5;
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
		if (playerHitCount < 10 && h < max * 3 / 4) attdam = ATTACK * 20;
		if (playerHitCount < 10 && h < max / 2) attdam = ATTACK * 100;
		if (playerHitCount < 10 && h < max / 3) attdam = ATTACK * 500;
		if (playerHitCount < 10 && h < max / 4) attdam = ATTACK * 1000;
		if (level().isClientSide() && power() > 800) {
			RandomSource r = level().getRandom();
			if (r.nextInt(4) == 1) {
				double yaw = Math.toRadians(getYRot());
				Vec3 dm = getDeltaMovement();
				for (int i = 0; i < 10; i++) {
					level().addParticle(ParticleTypes.FIREWORK, getX() - 7.0 * Math.sin(yaw), getY() + 14.0, getZ() + 7.0 * Math.cos(yaw),
						(r.nextGaussian() - r.nextGaussian()) / 5.0 + dm.x * 3.0, (r.nextGaussian() - r.nextGaussian()) / 5.0,
						(r.nextGaussian() - r.nextGaussian()) / 5.0 + dm.z * 3.0);
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
		keepVictimDown();
		if (attackLevel > 1000) {
			if (mood == 1) {
				int j = playerHitCount < 10 ? 45 : 15;
				double yaw = Math.toRadians(getYRot());
				Vec3 m = getDeltaMovement();
				for (int i = 0; i < j; i++) {
					BossUtil.purplePower(level, getX() - 8.0 * Math.sin(yaw), getY() + 14.0, getZ() + 8.0 * Math.cos(yaw), m.x * 3.0, m.z * 3.0, 0);
				}
			} else {
				if (level.getGameRules().get(GameRules.MOB_GRIEFING)) beautify(level);
				spawnCritters(level);
			}
			attackLevel = 1;
		}
		if (attackLevel > 1) attackLevel--;
		if (hurtTimer > 0) hurtTimer--;
		if (homex == 0 && homez == 0 || guardMode == 0) {
			homex = Mth.floor(getX());
			homez = Mth.floor(getZ());
		}
		if (getHealth() > getMaxHealth() - 2.0F * SCALE && random.nextInt(500) == 1) mood = 0;
		if (alwaysMad != 0) mood = 1;
		if (mood == 0) attackLevel += 10;
		if (++ticker > 30000) ticker = 0;
		if (ticker % 60 == 0) streamCount = 10;
		if (ticker % 70 == 0) streamCountL = 6;
		if (ticker % 10 == 0) {
			entityData.set(MOOD, mood);
			entityData.set(POWER, attackLevel);
		}
		if (backoffTimer > 0) backoffTimer--;
		int attrand = playerHitCount < 10 && getHealth() < getMaxHealth() / 2 ? 3 : 5;
		noPhysics = true;
		if (!hasTarget) setTarget(Mth.floor(getX()), Mth.floor(getY()), Mth.floor(getZ()));

		if (tooFarFromHome() || random.nextInt(200) == 0 || distToTargetSq() < 9.1F) {
			int zdir = random.nextInt(120);
			int xdir = random.nextInt(120);
			if (random.nextInt(2) == 0) zdir = -zdir;
			if (random.nextInt(2) == 0) xdir = -xdir;
			int dist = heightNudge(homex, homez);
			setTarget(homex + xdir, Mth.floor(getY() + dist), homez + zdir);
			if (mood == 0) {
				List<TheKing> kings = level.getEntitiesOfClass(TheKing.class, getBoundingBox().inflate(64.0, 32.0, 64.0));
				kings.sort(BossUtil.targetOrder(this));
				if (!kings.isEmpty()) {
					TheKing king = kings.getFirst();
					guardMode = 0;
					zdir = random.nextInt(16);
					xdir = random.nextInt(16);
					if (random.nextInt(2) == 0) zdir = -zdir;
					if (random.nextInt(2) == 0) xdir = -xdir;
					setTarget(Mth.floor(king.getX()) + xdir, (int) (king.getY() + (random.nextInt(8) - random.nextInt(8))), Mth.floor(king.getZ()) + zdir);
				}
			}
		} else if (random.nextInt(attrand) == 0) {
			hunt(level);
		}

		// Fly at the target: OreSpawn's steering, 0.65 blocks a tick at most.
		double var1 = tx + 0.5 - getX();
		double var3 = ty + 0.1 - getY();
		double var5 = tz + 0.5 - getZ();
		Vec3 m = getDeltaMovement();
		double mx = m.x + (Math.signum(var1) * 0.65 - m.x) * 0.35;
		double my = m.y + (Math.signum(var3) * 0.69999 - m.y) * 0.3;
		double mz = m.z + (Math.signum(var5) * 0.65 - m.z) * 0.35;
		setDeltaMovement(mx, my, mz);
		float var7 = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0F;
		setYRot(getYRot() + Mth.wrapDegrees(var7 - getYRot()) / 8.0F);

		if (random.nextInt(32) == 1 && getHealth() < getMaxHealth()) {
			heal(5.0F * SCALE);
			if (playerHitCount < 10) heal(50.0F * SCALE);
		}
		if (playerHitCount < 10 && getHealth() < 2000.0F * SCALE) heal(2000.0F * SCALE - getHealth());
	}

	/** OreSpawn's ev/evh: the thing she last bit can't heal while she's near it. */
	private void keepVictimDown() {
		if (ev == null) return;
		if (distanceToSqr(ev) < 2000.0 && ev.isAlive()) {
			if (evh < ev.getHealth()) ev.setHealth(evh);
			else evh = ev.getHealth();
			if (evh <= 0.0F && !(ev instanceof Player)) ev.discard();
		} else {
			ev = null;
			evh = 0.0F;
		}
	}

	/** Her good mood: 25 columns within 24 blocks, each changed at the first thing found from 20 below up. */
	private void beautify(ServerLevel level) {
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos up = new BlockPos.MutableBlockPos();
		int x0 = (int) getX();
		int y0 = (int) getY();
		int z0 = (int) getZ();
		for (int m = 0; m < 25; m++) {
			int i = random.nextInt(25) - random.nextInt(25);
			int k = random.nextInt(25) - random.nextInt(25);
			for (int j = -20; j < 20; j++) {
				BlockState bid = level.getBlockState(p.set(x0 + i, y0 + j, z0 + k));
				boolean airAbove = level.getBlockState(up.set(x0 + i, y0 + j + 1, z0 + k)).isAir();
				if (bid.is(Blocks.GRASS_BLOCK)) {
					if (airAbove) {
						Block flower = flower(random.nextInt(8));
						if (flower != null) level.setBlock(up, flower.defaultBlockState(), Block.UPDATE_ALL);
					}
					break;
				}
				if (bid.is(Blocks.DIRT) && airAbove) {
					level.setBlock(p, Blocks.GRASS_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
					break;
				}
				if (bid.is(Blocks.STONE) && airAbove) {
					level.setBlock(up, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
					break;
				}
				if (bid.is(Blocks.SAND) && airAbove) {
					if (random.nextInt(2) == 0) level.setBlock(up, Blocks.CACTUS.defaultBlockState(), Block.UPDATE_ALL);
					else level.setBlock(p, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
					break;
				}
				if (bid.is(Blocks.LAVA) && airAbove) {
					// Still lava becomes still water, flowing lava flowing water (the same level).
					level.setBlock(p, Blocks.WATER.defaultBlockState().setValue(net.minecraft.world.level.block.LiquidBlock.LEVEL,
						bid.getValue(net.minecraft.world.level.block.LiquidBlock.LEVEL)), Block.UPDATE_ALL);
					break;
				}
				if (bid.isAir() && j > 0) break;
			}
		}
	}

	private static @Nullable Block flower(int which) {
		for (String id : FLOWERS[which]) {
			Block b = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(id)).orElse(null);
			if (b != null) return b;
		}
		return null;
	}

	/** Ten tries at a butterfly or bird in the air around her (15 blocks across, up to 19 up). */
	private void spawnCritters(ServerLevel level) {
		for (int m = 0; m < 10; m++) {
			int i = random.nextInt(15) - random.nextInt(15);
			int k = random.nextInt(15) - random.nextInt(15);
			int j = random.nextInt(20);
			BlockPos p = BlockPos.containing(getX() + i, getY() + j, getZ() + k);
			if (!level.getBlockState(p).isAir()) continue;
			Mob critter = (random.nextInt(2) == 0 ? ModEntities.BUTTERFLY : ModEntities.BIRD).create(level, EntitySpawnReason.MOB_SUMMONED);
			if (critter == null) continue;
			critter.snapTo(getX() + i, getY() + j, getZ() + k, random.nextFloat() * 360.0F, 0.0F);
			level.addFreshEntity(critter);
		}
	}

	/** The attack branch of OreSpawn's updateAITasks. */
	private void hunt(ServerLevel level) {
		LivingEntity e = rt;
		if (isHappy()) e = null;
		if (e instanceof TheQueen) {
			rt = null;
			e = null;
		}
		if (e != null) {
			double d1 = e.getX() - homex;
			double d2 = e.getZ() - homez;
			double d = Math.sqrt(d1 * d1 + d2 * d2);
			if (!e.isAlive() || random.nextInt(450) == 1 || d > 128.0 && guardMode == 1) {
				e = null;
				rt = null;
			}
			if (e != null && !myCanSee(e)) e = null;
		}
		LivingEntity f = findSomethingToAttack(level);
		if (mood == 1 && (head == null || head.isRemoved())) head = BossHead.spawn(level, Bosses.QUEEN_HEAD, this);
		if (e == null) e = f;
		if (e == null) {
			setAttacking(0);
			streamCount = 10;
			streamCountL = 6;
			return;
		}
		float size = e.getBbWidth() * e.getBbHeight();
		if (attackLevel < 1000) {
			attackLevel += 15;
			if (getHealth() < getMaxHealth() / 2) attackLevel += 15;
			if (size > 50.0F) attackLevel += 15;
			if (size > 100.0F) attackLevel += 15;
			if (size > 200.0F) attackLevel += 25;
		}
		setAttacking(1);
		if (backoffTimer == 0) {
			int dist = (int) (e.getY() + e.getBbHeight() / 2.0F + 1.0);
			if (dist > 230) dist = 230;
			setTarget(Mth.floor(e.getX()), dist, Mth.floor(e.getZ()));
			if (random.nextInt(50) == 1) backoffTimer = 90 + random.nextInt(90);
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
		double yaw = Math.toRadians(getYRot());
		double dx = getX() + 20.0 * Math.sin(yaw);
		double dz = getZ() - 20.0 * Math.cos(yaw);
		if (random.nextInt(3) == 1) doJumpDamage(level, dx, getY() + 10.0, dz, 15.0, ATTACK / 2, true);

		if (horizontalDistanceSq(e) > 900.0) {
			boolean fire = random.nextInt(2) == 0;
			if (fire ? streamCount > 0 : streamCountL > 0) {
				setAttacking(1);
				double rr = Math.atan2(e.getZ() - getZ(), e.getX() - getX());
				double rhdir = Math.toRadians((getYRot() + 90.0F) % 360.0F);
				double pi = 3.1415926545;
				double rdd = Math.abs(rr - rhdir) % (pi * 2.0);
				if (rdd > pi) rdd -= pi * 2.0;
				if (Math.abs(rdd) < 0.5) {
					if (fire) fireCannon(level, e);
					else fireThunder(level, e);
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
		if (target instanceof LivingEntity e) {
			if (e.isAlive()) {
				if (ev == e) {
					if (evh < e.getHealth()) e.setHealth(evh);
				} else {
					ev = e;
				}
				if (e.getBbWidth() * e.getBbHeight() > 30.0F) {
					e.setHealth(e.getHealth() * 3.0F / 4.0F);
					e.hurtServer(level, damageSources().mobAttack(this), (float) attdam);
				}
				evh = e.getHealth();
				if (evh <= 0.0F && !(e instanceof Player)) e.discard();
			} else {
				ev = null;
				evh = 0.0F;
			}
		}
		if (target instanceof EnderDragon dragon) {
			DamageSource blast = damageSources().explosion(null, null);
			dragon.hurt(level, random.nextInt(6) == 1 ? dragon.head : dragon.getSubEntities()[2], blast, (float) attdam);
		}
		boolean hit = target.hurtServer(level, damageSources().mobAttack(this), (float) attdam);
		if (hit) {
			double inair = 0.2 + random.nextFloat() * 0.25F;
			float f3 = (float) Math.atan2(target.getZ() - getZ(), target.getX() - getX());
			if (!target.isAlive() || target instanceof Player) inair *= 1.5;
			target.push(Math.cos(f3) * 2.75, inair, Math.sin(f3) * 2.75);
			if (target instanceof Player) target.syncVelocity = true;
		}
		return hit;
	}

	/** doJumpDamage: half blast, half fall damage to everything (but royalty and ghosts) in the box. */
	private void doJumpDamage(ServerLevel level, double x, double y, double z, double dist, double damage, boolean knock) {
		AABB box = new AABB(x - dist, y - 10.0, z - dist, x + dist, y + 10.0, z + dist);
		List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, box);
		list.sort(BossUtil.targetOrder(this));
		for (LivingEntity e : list) {
			if (e == this || !e.isAlive() || BossUtil.isRoyalty(e) || BossUtil.isType(e, "ghost") || BossUtil.isType(e, "ghost_pumpkin_skelly")) {
				continue;
			}
			e.hurtServer(level, damageSources().explosion(null, null), (float) damage / 2.0F);
			e.hurtServer(level, damageSources().fall(), (float) damage / 2.0F);
			level.playSound(null, e, SoundEvents.GENERIC_EXPLODE.value(), getSoundSource(), 0.65F,
				1.0F + (random.nextFloat() - random.nextFloat()) * 0.5F);
			if (knock) {
				float f3 = (float) Math.atan2(e.getZ() - getZ(), e.getX() - getX());
				e.push(Math.cos(f3) * 2.75, 0.65, Math.sin(f3) * 2.75);
				if (e instanceof Player) e.syncVelocity = true;
			}
		}
	}

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
		double cx = cannonX();
		double cz = cannonZ();
		double cy = getY() + 14.0;
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

	/** firecanonl: three thunder bolts (OreSpawn's ThunderBolt, the weapons group's {@code orespawn:thunder_bolt}). */
	private void fireThunder(ServerLevel level, LivingEntity e) {
		if (streamCountL <= 0) return;
		playSound(SoundEvents.ARROW_SHOOT, 1.0F, bowPitch());
		EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(OreSpawn.id("thunder_bolt")).orElse(null);
		double cx = cannonX();
		double cz = cannonZ();
		double cy = getY() + 14.0;
		for (int i = 0; i < 3 && type != null; i++) {
			if (!(type.create(level, EntitySpawnReason.MOB_SUMMONED) instanceof Projectile p)) break;
			p.snapTo(cx, cy, cz, 0.0F, 0.0F);
			p.setOwner(this);
			double dx = e.getX() - p.getX();
			double dy = e.getY() + 0.25 - p.getY();
			double dz = e.getZ() - p.getZ();
			float lift = (float) Math.sqrt(dx * dx + dz * dz) * 0.2F;
			p.shoot(dx, dy + lift, dz, 1.4F, 4.0F);
			p.setDeltaMovement(p.getDeltaMovement().scale(3.0));
			level.addFreshEntity(p);
		}
		streamCountL--;
	}

	// ---- Being hurt ----

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (BossUtil.unstoppable(source)) return super.hurtServer(level, source, damage);
		if (hurtTimer > 0) return false;
		float dm = Math.min(damage, 750.0F);
		if (source.is(DamageTypes.IN_WALL)) return false;
		mood = 1;
		if (source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) {
			setHealth(Math.min(getHealth() + damage / 2.0F * SCALE, getMaxHealth()));
			return false;
		}
		Entity e = source.getEntity();
		if (e instanceof LivingEntity enl) {
			if (e instanceof PurplePower) return false;
			if (e instanceof Monster && enl.getBbHeight() * enl.getBbWidth() < 3.0F) {
				e.discard();
				return false;
			}
		}
		boolean ret = false;
		if (!source.is(DamageTypes.CACTUS)) {
			hurtTimer = 20;
			ret = super.hurtServer(level, source, dm * SCALE);
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

	/** MyCanSee: steps from 10 blocks in front, 14 up, to the target; only air lets her see through. */
	private boolean myCanSee(LivingEntity e) {
		double yaw = Math.toRadians(getYRot());
		int nblks = 20;
		float startx = (float) (getX() - 10.0 * Math.sin(yaw));
		float starty = (float) (getY() + 14.0);
		float startz = (float) (getZ() + 10.0 * Math.cos(yaw));
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
			if (!level().getBlockState(pos.set((int) startx, (int) starty, (int) startz)).isAir()) return false;
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
		if (Ignored.byHunters(e) || !getSensing().hasLineOfSight(e)) return false;
		if (e instanceof Player p) return !p.isCreative() && !p.isSpectator();
		if (e instanceof AbstractHorse || e instanceof Monster || e instanceof EnderDragon) return true;
		return isAttackableNonMob(e);
	}

	private @Nullable LivingEntity findSomethingToAttack(ServerLevel level) {
		if (isHappy()) return null;
		List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(80.0, 60.0, 80.0));
		list.sort(BossUtil.targetOrder(this));
		for (LivingEntity e : list) {
			if (isSuitableTarget(e)) return e;
		}
		return null;
	}

	// ---- Death ----

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		BossUtil.dropAround(this, BossUtil.item("royal_guardian_sword"), 20, 12.0, 0);
		BossUtil.dropAround(this, ModEntities.SPAWN_EGGS.get("the_prince"), 20, 12.0, 0);
		ThePrincess princess = Bosses.THE_PRINCESS.create(level, EntitySpawnReason.MOB_SUMMONED);
		if (princess != null) {
			princess.snapTo(getX(), getY() + 10.0, getZ(), random.nextFloat() * 360.0F, 0.0F);
			level.addFreshEntity(princess);
		}
		for (int i = 0; i < 56; i++) {
			BossUtil.dropAround(this, Bosses.THE_QUEEN_SCALE, 20, 12.0, 0);
			BossUtil.dropAround(this, Items.BEEF, 20, 12.0, 0);
			BossUtil.dropAround(this, Items.BONE, 20, 12.0, 0);
			BossUtil.dropAround(this, Items.ROTTEN_FLESH, 20, 12.0, 0);
		}
	}

	// ---- Saving ----

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("QueenHomeX", homex);
		output.putInt("QueenHomeZ", homez);
		output.putInt("GuardMode", guardMode);
		output.putInt("PlayerHits", playerHitCount);
		output.putInt("Mood", mood);
		output.putInt("AlwaysMad", alwaysMad);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		homex = input.getIntOr("QueenHomeX", 0);
		homez = input.getIntOr("QueenHomeZ", 0);
		guardMode = input.getIntOr("GuardMode", 0);
		playerHitCount = input.getIntOr("PlayerHits", 0);
		mood = input.getIntOr("Mood", 0);
		alwaysMad = input.getIntOr("AlwaysMad", 0);
		entityData.set(MOOD, mood);
		bar.rename();
	}

	/** OreSpawn's getCanSpawnHere: always (The Queen only comes from spawner blocks, eggs and Magic Apples). */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		return true;
	}
}
