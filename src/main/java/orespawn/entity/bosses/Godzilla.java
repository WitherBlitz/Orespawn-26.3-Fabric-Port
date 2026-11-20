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
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import orespawn.OreSpawn;
import orespawn.content.Bosses;
import orespawn.entity.Ignored;
import orespawn.registry.ModBlocks;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

/**
 * OreSpawn's Godzilla, "Mobzilla": a 9.9 x 25 walking lizard with a head hitbox ({@link BossHead}, 16 up and 17 ahead)
 * that wanders (15 blocks) until something it can see comes within 64 blocks (40 up and down): villagers first, then
 * anything else but its own kind, creepers, zombies, spiders, skeletons, ghosts and creative players. It then turns to
 * it and either calls down lightning (1 in 65, if over about 17 blocks away: 100 damage, 5 s of fire, an explosion of 3
 * and two lightning bolts), leaps at it (1 in 20, 30 ticks apart), or closes in and bites for 175 (big knockback); from
 * over 25 blocks away it breathes fire when facing it: one big BetterFireball and five more (half of them small), eight
 * volleys every 100 ticks. Its feet and tail crush a 25-block square (33 while attacking) a slice at a time, a block
 * high each tick (with mob griefing; grass and farmland become dirt; ground, ores' blocks and fluids stay), sometimes
 * dropping what it crushed; the tail stamps for 87 every 12 ticks. Landing from a fall stamps everything within 10, 15
 * and 25 blocks. Things larger than 30 (width x height) that aren't the giants OreSpawn knew lose half their health to
 * a bite and take ten times its damage, and do only a tenth to it (then 25 armor and faster attacks). Hits are capped
 * at 750 and it takes at most one every 20 ticks; it heals 5 now and then, never despawns, ignores fire, falls and
 * lightning. 4000 health, 175 damage, 21 armor, 10000 experience.
 * <p>
 * Spawning: OreSpawn's rule (dark, at night, y 50 or higher, 1 in 40, a 17 x 17 x 10 space of air above, no other
 * Mobzilla within 64, only one per game session). OreSpawn spawned it only in its Utopian Plains biome (weight 2, one at
 * a time), which the Village Mania group adds by id; otherwise it comes from eggs and egg ores.
 * <p>
 * Death drops: an item frame, 50-79 Mobzilla Scales, 100-259 raw beef, 50-109 bones and 25-39 rolls of OreSpawn's gear
 * table (see {@link BossUtil#randomGear}), scattered around the body.
 * <p>
 * Health: 26.3 caps max health at 1024, so Mobzilla has 1024 and every hit and heal is scaled by 1024/4000; it takes
 * as many hits as OreSpawn's. Movement: 26.3's path finding sizes its search to the whole 10-block-wide body and
 * rarely finds a way, so Mobzilla walks straight at where it's going (its wander spot or its prey) with its move
 * control, as OreSpawn's crushing clears the way; its step height is OreSpawn's 12.
 * <p>
 * Not ported yet: OreSpawn's PlayNicely option (off by default).
 */
public class Godzilla extends Monster {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(Godzilla.class, EntityDataSerializers.BOOLEAN);
	/** OreSpawn's get_mobstats("Mobzilla", 4000, 175, 21). */
	public static final int HEALTH = 4000;
	public static final int ATTACK = 175;
	public static final int DEFENSE = 21;
	/** 26.3's max health is 1024: OreSpawn health and damage are scaled by this. */
	private static final float SCALE = 1024.0F / HEALTH;
	/** OreSpawnMain.godzilla_has_spawned: once one has lived this game session, no more spawn naturally. */
	private static volatile boolean hasSpawned;

	private final BossBar bar = new BossBar(this, BossEvent.BossBarColor.GREEN);
	/** Made in registerGoals, which Mob's constructor calls before this class's fields are set. */
	private GiantWander wander;
	private @Nullable BossHead head;
	private @Nullable LivingEntity chase;
	private double chaseSpeed = 1.0;
	private int hurtTimer;
	private int jumped;
	private int jumpTimer;
	private int ticker;
	private int streamCount = 8;
	private int largeUnknownDetected;

	public Godzilla(EntityType<? extends Godzilla> type, Level level) {
		super(type, level);
		this.xpReward = 10000;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, HEALTH).add(Attributes.MOVEMENT_SPEED, 0.75)
			.add(Attributes.ATTACK_DAMAGE, ATTACK).add(Attributes.ARMOR, DEFENSE).add(Attributes.STEP_HEIGHT, 12.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		wander = new GiantWander();
		goalSelector.addGoal(2, wander);
		goalSelector.addGoal(3, new LookAtPlayerGoal(this, LivingEntity.class, 50.0F));
		goalSelector.addGoal(4, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ATTACKING, false);
	}

	public final int getAttacking() {
		return entityData.get(ATTACKING) ? 1 : 0;
	}

	public final void setAttacking(int attacking) {
		entityData.set(ATTACKING, attacking != 0);
	}

	// ---- Sounds ----

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return random.nextInt(5) == 0 ? ModSounds.get("godzilla_living") : null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("alo_hurt");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("godzilla_death");
	}

	@Override
	protected float getSoundVolume() {
		return 1.65F;
	}

	@Override
	public float getVoicePitch() {
		return 1.1F;
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
		return largeUnknownDetected != 0 ? 25 : DEFENSE;
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

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		bar.rename();
	}

	// ---- Movement ----

	/** OreSpawn's jump: half a block up, then a hop forward along the head's heading. */
	@Override
	public void jumpFromGround() {
		Vec3 m = getDeltaMovement();
		setPos(getX(), getY() + 0.5, getZ());
		float f = 0.2F + Math.abs(random.nextFloat() * 0.45F);
		double dir = Math.toRadians(getYHeadRot() + 90.0F);
		setDeltaMovement(m.x + f * Math.cos(dir), m.y + 0.45, m.z + f * Math.sin(dir));
		needsSync = true;
		chase = null;
	}

	private void chase(LivingEntity e, double speed) {
		chase = e;
		chaseSpeed = speed;
	}

	/** jumpAtEntity: a big leap at the prey, farther the farther away it is. */
	private void jumpAtEntity(LivingEntity e) {
		Vec3 m = getDeltaMovement();
		setPos(getX(), getY() + 1.55F, getZ());
		double d1 = e.getX() - getX();
		double d2 = e.getZ() - getZ();
		float d = (float) Math.atan2(d2, d1);
		setYRot((float) (d * 180.0 / Math.PI) - 90.0F);
		d1 = Math.sqrt(d1 * d1 + d2 * d2);
		setDeltaMovement(m.x + d1 * 0.05 * Math.cos(d), m.y + 1.25, m.z + d1 * 0.05 * Math.sin(d));
		needsSync = true;
		chase = null;
	}

	// ---- Ticking ----

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (isRemoved() || isDeadOrDying()) return;
		bar.update();
		if (++ticker > 30000) ticker = 0;
		if (ticker % 100 == 0) streamCount = 8;
		if (hurtTimer > 0) hurtTimer--;
		if (jumpTimer > 0) jumpTimer--;
		hasSpawned = true;
		if (random.nextInt(200) == 0) setTarget(null);

		// Landing from a fall stamps the ground around it.
		double my = getDeltaMovement().y;
		if (my < -0.95) jumped = 1;
		if (my < -1.5) jumped = 2;
		if (jumped != 0 && my > -0.1) {
			double df = jumped == 2 ? 1.5 : 1.0;
			doJumpDamage(level, getX(), getY(), getZ(), 10.0, ATTACK * df, false);
			doJumpDamage(level, getX(), getY(), getZ(), 15.0, ATTACK / 2 * df, false);
			doJumpDamage(level, getX(), getY(), getZ(), 25.0, ATTACK / 4 * df, false);
			jumped = 0;
		}

		// Crushing: a slice under and around the feet, one block a tick from 3 below up to 26 above, and another round
		// the tail (16 blocks behind) over 12 ticks.
		int range = getAttacking() != 0 ? 16 : 12;
		crush(level, getX(), getZ(), -3 + ticker % 30, range);
		double headYaw = Math.toRadians(getYHeadRot());
		double tx = getX() + 16.0 * Math.sin(headYaw);
		double tz = getZ() - 16.0 * Math.cos(headYaw);
		int k = -3 + ticker % 12;
		crush(level, tx, tz, k, range);
		if (k == 0) doJumpDamage(level, tx, getY(), tz, 15.0, ATTACK / 2, true);

		if (random.nextInt(5 - largeUnknownDetected) == 1) hunt(level);
		if (chase != null && (!chase.isAlive() || chase.isRemoved())) chase = null;
		if (chase != null) getMoveControl().setWantedPosition(chase.getX(), chase.getY(), chase.getZ(), chaseSpeed);

		if (random.nextInt(35) == 1 && getHealth() < getMaxHealth()) heal(5.0F * SCALE);
	}

	/** The attack branch of OreSpawn's updateAITasks. */
	private void hunt(ServerLevel level) {
		LivingEntity e = getTarget();
		if (e != null && (!e.isAlive() || e instanceof Godzilla)) {
			setTarget(null);
			e = null;
		}
		if (e == null) {
			e = findSomethingToAttack(level);
			if (head == null || head.isRemoved()) head = BossHead.spawn(level, Bosses.MOBZILLA_HEAD, this);
		}
		if (e == null) {
			setAttacking(0);
			wander.busy = false;
			chase = null;
			streamCount = 8;
			return;
		}
		wander.busy = true;
		lookAt(e, 10.0F, 10.0F);
		double halfWidth = e.getBbWidth() / 2.0F;
		if (random.nextInt(65) == 1 && distSq(e) > 300.0) {
			doLightningAttack(level, e);
		} else if (random.nextInt(20 - largeUnknownDetected * 5) == 1 && jumpTimer == 0) {
			jumpAtEntity(e);
			jumpTimer = 30;
		} else if (distSq(e) < 300.0F + halfWidth * halfWidth) {
			setAttacking(1);
			chase(e, 1.0);
			if (random.nextInt(4 - largeUnknownDetected) == 0 || random.nextInt(3 - largeUnknownDetected) == 1) doHurtTarget(level, e);
		} else {
			chase(e, 1.0);
			if (horizontalDistSq(e) > 625.0 && streamCount > 0) {
				setAttacking(1);
				double rr = Math.atan2(e.getZ() - getZ(), e.getX() - getX());
				double rhdir = Math.toRadians((getYHeadRot() + 90.0F) % 360.0F);
				double pi = 3.1415926545;
				double rdd = Math.abs(rr - rhdir) % (pi * 2.0);
				if (rdd > pi) rdd -= pi * 2.0;
				if (Math.abs(rdd) < 0.5) fireCannon(level, e);
			} else {
				setAttacking(0);
			}
		}
	}

	/** MygetDistanceSqToEntity: height differences up to 20 above don't count, and beyond that 10 less. */
	private double distSq(Entity e) {
		double d0 = getX() - e.getX();
		double d1 = e.getY() - getY();
		double d2 = getZ() - e.getZ();
		if (d1 > 0.0 && d1 < 20.0) d1 = 0.0;
		if (d1 > 20.0) d1 -= 10.0;
		return d0 * d0 + d1 * d1 + d2 * d2;
	}

	private double horizontalDistSq(Entity e) {
		double d1 = e.getZ() - getZ();
		double d2 = e.getX() - getX();
		return d1 * d1 + d2 * d2;
	}

	// ---- Crushing ----

	private void crush(ServerLevel level, double cx, double cz, int k, int range) {
		if (!level.getGameRules().get(GameRules.MOB_GRIEFING)) return;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int x0 = (int) cx;
		int y = (int) getY() + k;
		int z0 = (int) cz;
		for (int i = -range; i <= range; i++) {
			for (int j = -range; j <= range; j++) {
				pos.set(x0 + i, y, z0 + j);
				BlockState state = level.getBlockState(pos);
				if (state.isAir()) continue;
				if (isCrushable(state)) {
					level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
					if (random.nextInt(15) == 1) dropNear(new ItemStack(state.getBlock().asItem()), cx, cz);
				} else if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.FARMLAND)) {
					level.setBlock(pos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
				}
			}
		}
	}

	/** dropItemRand / dropItemRandAt: around (x, z), 4-9 blocks up (4-13 at the feet). */
	private void dropNear(ItemStack stack, double x, double z) {
		if (stack.isEmpty() || !(level() instanceof ServerLevel level)) return;
		boolean feet = x == getX() && z == getZ();
		double px = x + random.nextInt(10) - random.nextInt(10);
		double py = getY() + 4.0 + random.nextInt(feet ? 10 : 6);
		double pz = z + random.nextInt(10) - random.nextInt(10);
		level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(level, px, py, pz, stack));
	}

	/**
	 * isCrushable: everything but the ground (grass, dirt, stone, farmland, sand, gravel), bedrock, obsidian, netherrack,
	 * end stone, fluids, the precious blocks (iron, gold, diamond, emerald, amethyst, ruby, uranium, titanium) and
	 * OreSpawn's crystal stone (kyanite) and grass. 26.3's stone and dirt variants count as stone and dirt.
	 */
	private static boolean isCrushable(BlockState s) {
		if (s.is(Blocks.WATER) || s.is(Blocks.LAVA) || s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT) || s.is(Blocks.COARSE_DIRT) || s.is(Blocks.PODZOL) || s.is(Blocks.ROOTED_DIRT)
			|| s.is(BlockTags.BASE_STONE_OVERWORLD) || s.is(Blocks.FARMLAND) || s.is(Blocks.BEDROCK) || s.is(Blocks.OBSIDIAN)
			|| s.is(Blocks.SAND) || s.is(Blocks.RED_SAND) || s.is(Blocks.GRAVEL) || s.is(Blocks.IRON_BLOCK) || s.is(Blocks.DIAMOND_BLOCK)
			|| s.is(Blocks.EMERALD_BLOCK) || s.is(Blocks.GOLD_BLOCK) || s.is(Blocks.NETHERRACK) || s.is(Blocks.END_STONE)
			|| s.is(ModBlocks.AMETHYST_BLOCK) || s.is(ModBlocks.RUBY_BLOCK) || s.is(ModBlocks.URANIUM_BLOCK) || s.is(ModBlocks.TITANIUM_BLOCK)) {
			return false;
		}
		var id = BuiltInRegistries.BLOCK.getKey(s.getBlock());
		return !id.equals(OreSpawn.id("kyanite")) && !id.equals(OreSpawn.id("crystal_grass"));
	}

	// ---- Attacks ----

	/** doJumpDamage: half blast, half fall damage to everything (but its kind and ghosts) in the box; knock flings them away. */
	private void doJumpDamage(ServerLevel level, double x, double y, double z, double dist, double damage, boolean knock) {
		AABB box = new AABB(x - dist, y - 10.0, z - dist, x + dist, y + 10.0, z + dist);
		List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, box);
		list.sort(BossUtil.targetOrder(this));
		for (LivingEntity e : list) {
			if (e == this || !e.isAlive() || e instanceof Godzilla || BossUtil.isType(e, "ghost") || BossUtil.isType(e, "ghost_pumpkin_skelly")) {
				continue;
			}
			e.hurtServer(level, damageSources().explosion(null, null), (float) damage / 2.0F);
			e.hurtServer(level, damageSources().fall(), (float) damage / 2.0F);
			level.playSound(null, e, SoundEvents.GENERIC_EXPLODE.value(), getSoundSource(), 0.85F,
				1.0F + (random.nextFloat() - random.nextFloat()) * 0.5F);
			if (knock) {
				float f3 = (float) Math.atan2(e.getZ() - getZ(), e.getX() - getX());
				e.push(Math.cos(f3) * 3.5, 0.75, Math.sin(f3) * 3.5);
				if (e instanceof Player) e.syncVelocity = true;
			}
		}
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (target instanceof LivingEntity e && BossUtil.isLargeUnknown(e)) {
			e.setHealth(e.getHealth() / 2.0F);
			e.hurtServer(level, damageSources().mobAttack(this), ATTACK * 10.0F);
			largeUnknownDetected = 1;
		}
		if (target instanceof EnderDragon dragon) {
			DamageSource blast = damageSources().explosion(null, null);
			dragon.hurt(level, random.nextInt(6) == 1 ? dragon.head : dragon.getSubEntities()[2], blast, ATTACK / 2.0F);
		}
		if (!super.doHurtTarget(level, target)) return false;
		if (target instanceof LivingEntity) {
			double inair = 0.3;
			float f3 = (float) Math.atan2(target.getZ() - getZ(), target.getX() - getX());
			if (!target.isAlive() || target instanceof Player) inair *= 2.0;
			target.push(Math.cos(f3) * 3.2, inair, Math.sin(f3) * 3.2);
			if (target instanceof Player) target.syncVelocity = true;
		}
		return true;
	}

	/** firecanon: from 22 blocks ahead and 19 up, a big fireball at the prey and five more around it, half of them small. */
	private void fireCannon(ServerLevel level, LivingEntity e) {
		if (streamCount <= 0) return;
		double yoff = 19.0;
		double yaw = Math.toRadians(getYRot());
		double cx = getX() - 22.0 * Math.sin(yaw);
		double cz = getZ() + 22.0 * Math.cos(yaw);
		double cy = getY() + yoff;
		double ty = e.getY() + e.getBbHeight() / 2.0F - cy;
		BetterFireball bf = new BetterFireball(level, this, e.getX() - cx, ty, e.getZ() - cz);
		bf.snapTo(cx, cy, cz, getYRot(), 0.0F);
		bf.setBig();
		playSound(SoundEvents.TNT_PRIMED, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
		level.addFreshEntity(bf);
		for (int i = 0; i < 5; i++) {
			float r1 = 5.0F * (random.nextFloat() - random.nextFloat());
			float r2 = 3.0F * (random.nextFloat() - random.nextFloat());
			float r3 = 5.0F * (random.nextFloat() - random.nextFloat());
			bf = new BetterFireball(level, this, e.getX() - cx + r1, ty + r2, e.getZ() - cz + r3);
			bf.snapTo(cx, cy, cz, getYRot(), 0.0F);
			if (random.nextInt(2) == 1) bf.setSmall();
			playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
			level.addFreshEntity(bf);
		}
		streamCount--;
	}

	/** doLightningAttack: 100 damage, 5 s of fire, smoke and sparks, an explosion of 3, and lightning on both of them. */
	private void doLightningAttack(ServerLevel level, LivingEntity e) {
		e.hurtServer(level, damageSources().mobAttack(this), 100.0F);
		e.igniteForSeconds(5.0F);
		RandomSource r = random;
		for (int i = 0; i < 20; i++) {
			level.sendParticles(ParticleTypes.SMOKE, e.getX() + r.nextFloat() - r.nextFloat(), e.getY() + r.nextFloat() - r.nextFloat(),
				e.getZ() + r.nextFloat(), 1, 0.0, 0.0, 0.0, 0.0);
			level.sendParticles(ParticleTypes.LARGE_SMOKE, e.getX() + r.nextFloat() - r.nextFloat(), e.getY() + r.nextFloat() - r.nextFloat(),
				e.getZ() + r.nextFloat() - r.nextFloat(), 1, 0.0, 0.0, 0.0, 0.0);
			level.sendParticles(ParticleTypes.FIREWORK, e.getX(), e.getY(), e.getZ(), 0, r.nextGaussian(), r.nextGaussian(), r.nextGaussian(), 1.0);
		}
		level.playSound(null, e, SoundEvents.GENERIC_EXPLODE.value(), getSoundSource(), 0.5F, 1.0F + (r.nextFloat() - r.nextFloat()) * 0.5F);
		level.explode(this, e.getX(), e.getY(), e.getZ(), 3.0F, Level.ExplosionInteraction.MOB);
		lightning(level, e.getX(), e.getY() + 1.0, e.getZ());
		lightning(level, getX(), getY() + 15.0, getZ());
	}

	private static void lightning(ServerLevel level, double x, double y, double z) {
		LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
		if (bolt == null) return;
		bolt.snapTo(x, y, z);
		level.addFreshEntity(bolt);
	}

	// ---- Being hurt ----

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (BossUtil.unstoppable(source)) return super.hurtServer(level, source, damage);
		if (hurtTimer > 0) return false;
		float dm = Math.min(damage, 750.0F);
		Entity e = source.getEntity();
		if (e instanceof LivingEntity enl && BossUtil.isLargeUnknown(enl)) {
			dm /= 10.0F;
			hurtTimer = 50;
			largeUnknownDetected = 1;
		}
		boolean ret = false;
		if (!source.is(DamageTypes.CACTUS)) {
			ret = super.hurtServer(level, source, dm * SCALE);
			hurtTimer = 20;
			if (e instanceof LivingEntity enl && !(e instanceof Godzilla)) {
				setTarget(enl);
				chase(enl, 1.2);
			}
		}
		return ret;
	}

	// ---- Targets ----

	private boolean isSuitableTarget(LivingEntity e) {
		if (e == this || !e.isAlive() || Ignored.byHunters(e) || !getSensing().hasLineOfSight(e)) return false;
		if (e instanceof Godzilla || e instanceof Creeper || e instanceof Zombie || e instanceof Spider || e instanceof AbstractSkeleton) return false;
		if (BossUtil.isType(e, "ghost") || BossUtil.isType(e, "ghost_pumpkin_skelly")) return false;
		return !(e instanceof Player p) || !(p.isCreative() || p.isSpectator());
	}

	private boolean isVillagerTarget(LivingEntity e) {
		return e != this && e.isAlive() && getSensing().hasLineOfSight(e) && e instanceof Villager;
	}

	/** findSomethingToAttack: within 64 (40 up and down), the first villager in GenericTargetSorter order, else the first suitable. */
	private @Nullable LivingEntity findSomethingToAttack(ServerLevel level) {
		List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(64.0, 40.0, 64.0));
		list.sort(BossUtil.targetOrder(this));
		LivingEntity ret = null;
		for (LivingEntity e : list) {
			if (isVillagerTarget(e)) return e;
			if (ret == null && isSuitableTarget(e)) ret = e;
		}
		return ret;
	}

	// ---- Death ----

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		BossUtil.dropAround(this, Items.ITEM_FRAME, 10, 4.0, 10);
		int n = 50 + random.nextInt(30);
		for (int i = 0; i < n; i++) BossUtil.dropAround(this, Bosses.MOBZILLA_SCALE, 10, 4.0, 10);
		n = 100 + random.nextInt(160);
		for (int i = 0; i < n; i++) BossUtil.dropAround(this, Items.BEEF, 10, 4.0, 10);
		n = 50 + random.nextInt(60);
		for (int i = 0; i < n; i++) BossUtil.dropAround(this, Items.BONE, 10, 4.0, 10);
		n = 25 + random.nextInt(15);
		for (int i = 0; i < n; i++) {
			BossUtil.randomGear(this, random.nextInt(80), s -> BossUtil.dropAround(this, s, 10, 4.0, 10), s -> BossUtil.dropAround(this, s, 3, 1.0, 0));
		}
	}

	// ---- Spawning ----

	/** getCanSpawnHere: see the class comment. Spawners and eggs always may. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason) || reason == EntitySpawnReason.SPAWN_ITEM_USE) return true;
		if (!Monster.isDarkEnoughToSpawn(level, pos, random) || level.getLevel().isBrightOutside()) return false;
		if (pos.getY() < 50 || hasSpawned || random.nextInt(40) != 1) return false;
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		for (int k = -8; k <= 8; k++) {
			for (int j = -8; j <= 8; j++) {
				for (int i = 5; i < 15; i++) {
					if (!level.getBlockState(p.set(pos.getX() + j, pos.getY() + i, pos.getZ() + k)).isAir()) return false;
				}
			}
		}
		if (!level.getEntitiesOfClass(Godzilla.class, new AABB(pos).inflate(64.0, 16.0, 64.0)).isEmpty()) return false;
		hasSpawned = true;
		return true;
	}

	/** MyEntityAIWanderALot(15, 1.0), walking straight to the spot with the move control (see the class comment). */
	private final class GiantWander extends Goal {
		private boolean busy;
		private @Nullable Vec3 dest;
		private int time;

		GiantWander() {
			setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			if (busy || getRandom().nextInt(30) != 0) return false;
			dest = DefaultRandomPos.getPos(Godzilla.this, 15, 7);
			return dest != null;
		}

		@Override
		public void start() {
			time = 0;
		}

		@Override
		public boolean canContinueToUse() {
			return !busy && dest != null && time < 200 && distanceToSqr(dest.x, getY(), dest.z) > 4.0;
		}

		@Override
		public void tick() {
			time++;
			if (dest != null && chase == null) getMoveControl().setWantedPosition(dest.x, dest.y, dest.z, 1.0);
		}
	}
}
