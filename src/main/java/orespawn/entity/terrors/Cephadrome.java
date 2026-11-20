package orespawn.entity.terrors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * OreSpawn's Cephadrome: a great hammer-headed flying beast of the snowy lands, out by day. It wanders on foot and
 * hunts monsters (and the Ender Dragon, untamed WTF?s and some of OreSpawn's other beasts) within 16 blocks, hitting
 * for 70 and flinging them far; players it leaves alone unless one has hurt it badly or it came from a spawner. After
 * each hit it shrugs off damage for over a second, cactus can't hurt it, it heals over time and falls don't hurt it.
 * 300 health, 16 armor.
 * <p>
 * Riding: click it with an empty hand (within 5 blocks) to climb on, and it takes to the air. It steers where you look;
 * walk forward to speed up (to 1.15 blocks a tick), back to slow down, and hold jump to climb (OreSpawn's own "UP/FAST"
 * key; 26.3 sends the jump key for riders). It rises over the ground and anything ahead of it and sinks slowly
 * otherwise. Feed it raw beef, chicken or porkchop to heal it fully.
 * <p>
 * Drops 4-9 uranium and 4-9 titanium nuggets and 1-5 rolls of OreSpawn's treasure table (rubies, diamonds, the Thunder
 * Staff, and ruby tools and armor that are often enchanted).
 * <p>
 * Not ported: OreSpawn's "PlayNicely" option (off by default; with it on, an unfed Cephadrome turned on whoever tried to
 * mount it), and its fire resistance setting (no 26.3 equivalent). The Kraken bonus applies once that creature exists.
 * The Cephadrome altar and the Extreme Torch that summons one belong to other groups.
 */
public class Cephadrome extends PathfinderMob {
	private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(Cephadrome.class, EntityDataSerializers.BOOLEAN);
	/** OreSpawn's "activity": false on foot, true flying (with a rider). */
	private static final EntityDataAccessor<Boolean> FLYING = SynchedEntityData.defineId(Cephadrome.class, EntityDataSerializers.BOOLEAN);
	/** Untamed ones of these it hunts (Leonopteryx, Water Dragon). */
	private static final Set<String> TAMEABLE_PREY = Set.of("leonopteryx", "water_dragon");
	private static final Set<String> MOTHRA = Set.of("mothra");
	private static final Set<String> KRAKEN = Set.of("the_kraken");
	private int updateIt = 1;
	private int hurtTimer;
	private int wingSound;
	private boolean hitByPlayer;
	private boolean badMood;
	// OreSpawn's RenderInfo (client side): the turn its neck leans into while flying.
	private float neckTurn;

	public Cephadrome(EntityType<? extends Cephadrome> type, Level level) {
		super(type, level);
		this.xpReward = 200;
	}

	/** Cephadrome: 300 health, 70 damage, 16 armor, speed 0.25 (it had no config entry). */
	public static AttributeSupplier.Builder createAttributes() {
		return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 300.0).add(Attributes.MOVEMENT_SPEED, 0.25)
			.add(Attributes.ATTACK_DAMAGE, 70.0).add(Attributes.ARMOR, 16.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, WanderGoal.wanderALot(this, 16, 1.0));
		goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 9.0F));
		goalSelector.addGoal(3, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ATTACKING, false);
		builder.define(FLYING, false);
	}

	public boolean attacking() {
		return entityData.get(ATTACKING);
	}

	public boolean flying() {
		return entityData.get(FLYING);
	}

	private void setFlying(boolean flying) {
		entityData.set(FLYING, flying);
		updateControlFlags();
	}

	/** In the air its walking goals stand still (OreSpawn skipped its AI tasks while flying). */
	@Override
	protected void updateControlFlags() {
		super.updateControlFlags();
		if (flying()) {
			goalSelector.setControlFlag(Goal.Flag.MOVE, false);
			goalSelector.setControlFlag(Goal.Flag.JUMP, false);
			goalSelector.setControlFlag(Goal.Flag.LOOK, false);
		}
	}

	/** The smoothed turn (yaw change a tick times 10, held to +-50) its neck leans into while flying; for the model. */
	public float neckTurn() {
		float turn = -(yRotO - getYRot()) * 10.0F;
		neckTurn = Mth.clamp(neckTurn + (turn - neckTurn) / 50.0F, -50.0F, 50.0F);
		return neckTurn;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("CephaAttacking", attacking() ? 1 : 0);
		output.putInt("CephaActivity", flying() ? 1 : 0);
		output.putInt("CephaHitByPlayer", hitByPlayer ? 1 : 0);
		output.putInt("CephaBadMood", badMood ? 1 : 0);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		hitByPlayer = input.getIntOr("CephaHitByPlayer", 0) != 0;
		badMood = input.getIntOr("CephaBadMood", 0) != 0;
		entityData.set(ATTACKING, input.getIntOr("CephaAttacking", 0) != 0);
		entityData.set(FLYING, input.getIntOr("CephaActivity", 0) != 0);
	}

	/** One from a spawner is in a bad mood: it attacks players too. */
	@Override
	public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
			@Nullable SpawnGroupData data) {
		if (EntitySpawnReason.isSpawner(reason)) badMood = true;
		return super.finalizeSpawn(level, difficulty, reason, data);
	}

	@Override
	public void tick() {
		super.tick();
		if (flying() && ++wingSound > 22) {
			if (!level().isClientSide()) TerrorAi.play(this, TerrorAi.optionalSound("mothrawings"), 0.5F, 1.0F);
			wingSound = 0;
		}
	}

	@Override
	public void jumpFromGround() {
		super.jumpFromGround();
		setDeltaMovement(getDeltaMovement().add(0.0, 0.1, 0.0));
	}

	// ---- Riding ----

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if ((stack.is(Items.BEEF) || stack.is(Items.CHICKEN) || stack.is(Items.PORKCHOP)) && player.distanceToSqr(this) < 25.0) {
			if (level().isClientSide()) {
				hearts();
			} else {
				heal(getMaxHealth() - getHealth());
				stack.consume(1, player);
			}
			return InteractionResult.SUCCESS;
		}
		if (getFirstPassenger() instanceof Player rider && rider != player) return InteractionResult.SUCCESS;
		if (stack.isEmpty() && player.distanceToSqr(this) < 25.0) {
			if (!level().isClientSide()) player.startRiding(this);
			return InteractionResult.SUCCESS;
		}
		return super.mobInteract(player, hand);
	}

	/** playTameEffect(true): hearts all round it. */
	private void hearts() {
		for (int i = 0; i < 20; i++) {
			level().addParticle(ParticleTypes.HEART, getX() + (random.nextFloat() - random.nextFloat()) * 2.5F,
				getY() + 0.5 + random.nextFloat() * 1.5, getZ() + (random.nextFloat() - random.nextFloat()) * 2.5F,
				random.nextGaussian() * 0.08, random.nextGaussian() * 0.08, random.nextGaussian() * 0.08);
		}
	}

	@Override
	public @Nullable LivingEntity getControllingPassenger() {
		return getFirstPassenger() instanceof Player player ? player : super.getControllingPassenger();
	}

	/** The rider sits 0.75 forward, its feet 2 blocks up (OreSpawn's mounted offset of 2.5, less a player's 0.5). */
	@Override
	protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
		return new Vec3(0.0, 2.6, 0.75).yRot(-getYRot() * Mth.DEG_TO_RAD);
	}

	@Override
	public boolean isFlyingVehicle() {
		return true;
	}

	@Override
	public void travel(Vec3 input) {
		if (getControllingPassenger() instanceof Player rider) {
			rideFlight(rider);
		} else if (flying()) {
			// Flying with nobody on it (until it notices, every 30 ticks): it just drifts.
			move(MoverType.SELF, getDeltaMovement());
			setDeltaMovement(getDeltaMovement().multiply(0.985, 0.94, 0.985));
		} else {
			super.travel(input);
		}
	}

	/** Cephadrome.onLivingUpdate with a rider: OreSpawn's own flight. */
	private void rideFlight(Player rider) {
		Vec3 m = getDeltaMovement();
		double mx = Mth.clamp(m.x, -2.0, 2.0);
		double my = m.y;
		double mz = Mth.clamp(m.z, -2.0, 2.0);
		double velocity = Math.sqrt(mx * mx + mz * mz);
		double y = getY();
		// Lifts off the ground under it, otherwise sinks slowly.
		if (!level().isEmptyBlock(BlockPos.containing(getX(), y - 1.55, getZ()))) {
			my += 0.07;
			y += 0.1;
		} else {
			my -= 0.018;
		}
		// Rises over anything ahead of it (further ahead the faster it goes).
		double obstruction = 0.0;
		int reach = 2 + (int) (velocity * 6.0);
		double ahead = Math.toRadians(getYRot() + 90.0F);
		for (int k = 1; k < reach; k++) {
			for (int i = 1; i < reach * 2; i++) {
				BlockPos p = new BlockPos(Mth.floor(getX() + i * Math.cos(ahead)), Mth.floor(y) - k, Mth.floor(getZ() + i * Math.sin(ahead)));
				if (!level().isEmptyBlock(p)) obstruction += 0.04;
			}
		}
		my += obstruction * 0.09;
		y += obstruction * 0.09;
		if (my > 2.0) my = 2.0;
		// Turns towards where the rider looks.
		double riderYaw = rider.getYRot();
		double relative = (mod360(riderYaw) - mod360(getYRot())) % 180.0;
		while (relative < 0.0) relative += 180.0;
		if (relative > 90.0) relative -= 180.0;
		float yaw;
		if (velocity > 0.1) {
			double f = Mth.clamp(Math.abs(1.5 - velocity), 0.01, 0.9);
			yaw = (float) (riderYaw + relative * f);
		} else {
			yaw = (float) riderYaw;
		}
		float pitch = my > 0.0 ? -2.0F * (float) velocity : 2.0F * (float) velocity;
		setYRot(yaw);
		setXRot(pitch);
		yBodyRot = yaw;
		yHeadRot = yaw;
		// Speeds up or slows down with the rider's forward key, along the way it faces.
		double speed = Math.sqrt(mx * mx + mz * mz);
		double heading = Math.atan2(mz, mx);
		double wanted = Math.toRadians((riderYaw + 90.0) % 360.0);
		if (rider.isJumping()) my += 0.04 + velocity * 0.05;
		double off = Math.abs(heading - wanted) % (Math.PI * 2.0);
		if (off > Math.PI) off -= Math.PI * 2.0;
		off = Math.abs(off);
		if (Math.abs(speed) < 0.01) off = 0.0;
		if (off > 1.5) speed = -speed;
		double forward = rider.zza;
		double max = 1.15;
		boolean input = Math.abs(forward) > 0.001F;
		if (input) {
			double dv;
			if (forward > 0.0) {
				dv = 0.03 + 0.05;
			} else {
				max = 0.35;
				dv = -0.03;
			}
			speed += dv;
		}
		double dir = Math.toRadians(yaw + 90.0F);
		if (speed >= 0.0) {
			if (input && speed > max) speed = max;
		} else {
			if (input && speed < -max) speed = -max;
			speed = -speed;
			dir = Math.toRadians(yaw + 270.0F);
		}
		mx = Math.cos(dir) * speed;
		mz = Math.sin(dir) * speed;
		if (y != getY()) setPos(getX(), y, getZ());
		setDeltaMovement(mx, my, mz);
		move(MoverType.SELF, getDeltaMovement());
		setDeltaMovement(getDeltaMovement().multiply(0.985, 0.94, 0.985));
	}

	private static double mod360(double a) {
		a %= 360.0;
		while (a < 0.0) a += 360.0;
		return a;
	}

	// ---- Fighting ----

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (updateIt > 0) updateIt--;
		if (hurtTimer > 0) hurtTimer--;
		if (updateIt <= 0) {
			updateIt = 30;
			setFlying(isVehicle());
		}
		if (random.nextInt(100) == 1 && getHealth() < getMaxHealth()) heal(2.0F);
		if (random.nextInt(7) == 1 && level.getDifficulty() != Difficulty.PEACEFUL) {
			double maxDist = 10.0;
			LivingEntity e = getTarget();
			if (e != null && !e.isAlive()) {
				setTarget(null);
				e = null;
			}
			if (e == null) e = TerrorAi.findTarget(this, 16.0, 20.0, this::isSuitableTarget);
			if (e != null) {
				if (!flying()) {
					getNavigation().moveTo(e, 1.7);
					maxDist = 6.0;
				}
				lookAt(e, 10.0F, 10.0F);
				entityData.set(ATTACKING, true);
				double reach = maxDist + e.getBbWidth() / 2.0F;
				reach *= reach;
				if (distanceToSqr(e) < reach) {
					doHurtTarget(level, e);
				} else if (TerrorAi.isOreSpawn(e, KRAKEN)) {
					double dx = getX() - e.getX(), dz = getZ() - e.getZ();
					if (dx * dx + dz * dz < reach) doHurtTarget(level, e);
				}
			} else if (attacking()) {
				entityData.set(ATTACKING, false);
			}
		}
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (!getSensing().hasLineOfSight(e) || e instanceof Cephadrome) return false;
		if (e instanceof Monster || e instanceof EnderDragon || TerrorAi.isOreSpawn(e, MOTHRA)) return true;
		if (e instanceof GammaMetroid || TerrorAi.isOreSpawn(e, TAMEABLE_PREY)) return !(e instanceof TamableAnimal t && t.isTame());
		if (e instanceof Player) return !TerrorAi.protectedPlayer(e) && (hitByPlayer || badMood);
		return false;
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (target instanceof EnderDragon dragon) {
			TerrorAi.hitDragon(level, dragon, 6, 70.0F);
			return true;
		}
		if (!(target instanceof LivingEntity)) return false;
		float damage = TerrorAi.isOreSpawn(target, KRAKEN) ? 1.5F * 70.0F : 70.0F;
		boolean hit = target.hurtServer(level, damageSources().mobAttack(this), damage);
		TerrorAi.fling(this, target, 2.5, 0.35);
		return hit;
	}

	/** After each hit it ignores damage for 25 ticks; cactus never hurts it; whoever hurts it becomes its target. */
	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (hurtTimer > 0 && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
		if (source.is(DamageTypes.CACTUS)) return false;
		boolean hurt = super.hurtServer(level, source, damage);
		hurtTimer = 25;
		Entity e = source.getEntity();
		if (e instanceof LivingEntity attacker) {
			setTarget(attacker);
			setLastHurtByMob(attacker);
			getNavigation().moveTo(attacker, 1.2);
			hurt = true;
		}
		if (e instanceof Player && getHealth() < getMaxHealth() * 9.0F / 10.0F) hitByPlayer = true;
		return hurt;
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return !flying() && random.nextInt(6) == 1 ? TerrorAi.optionalSound("mothrawings") : null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("alo_hurt");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("alo_death");
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
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		// Cephadrome.dropFewItems (randomly enchanted gear, so not a loot table).
		int n = 4 + random.nextInt(6);
		for (int i = 0; i < n; i++) drop(level, ModItems.URANIUM_NUGGET);
		n = 4 + random.nextInt(6);
		for (int i = 0; i < n; i++) drop(level, ModItems.TITANIUM_NUGGET);
		n = 1 + random.nextInt(5);
		for (int i = 0; i < n; i++) {
			int roll = random.nextInt(20);
			if (roll == 0) drop(level, ModItems.byName("ruby_sword"));
			else if (roll == 1) drop(level, Items.DIAMOND);
			else if (roll == 2) drop(level, ModItems.lazy("thunder_staff"));
			else if (roll >= 12 && roll <= 17) drop(level, ModItems.RUBY);
			else TerrorAi.dropAround(this, level, TerrorAi.treasureGear(level, random, "ruby", roll), 5, 1.0F);
		}
	}

	private void drop(ServerLevel level, Item item) {
		TerrorAi.dropAround(this, level, new ItemStack(item), 5, 1.0F);
	}

	/** canDespawn: not while someone rides it. */
	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return !isVehicle();
	}

	/**
	 * Cephadrome.getCanSpawnHere: by day, at y 50 or higher, with a 4 x 4 x 4 space of air above it and no other
	 * Cephadrome within 16 blocks.
	 */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (!level.getLevel().isBrightOutside() || pos.getY() < 50) return false;
		if (!TerrorAi.airAbove(level, pos, -2, 1, -2, 1, 1, 4)) return false;
		return level.getEntitiesOfClass(Cephadrome.class, new AABB(pos).inflate(16.0, 6.0, 16.0)).isEmpty();
	}
}
