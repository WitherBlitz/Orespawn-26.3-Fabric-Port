package orespawn.entity.spooky;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import orespawn.entity.Flight;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * OreSpawn's Fairy: a tiny winged sprite in one of nine colours that flits about and hunts monsters, darting at any
 * hostile mob within 8 blocks and stinging it for 2 (never on Peaceful). Its body flashes on and off, and at night it
 * sheds sparks while lit. 40 health, 4 armor, heals slowly. Fairies called up by the Fairy Sword (the weapons group's,
 * through {@link #setOwner}) follow their player: they fly back when more than 8 blocks away, jump to the player when
 * more than 16 away, and never despawn. Drops 0-2 Crystal Torches (more with Looting). Spawns in air at y 50 or higher.
 * <p>
 * Changed: OreSpawn saved the colour as "FairyType" but read "fairyType", so a fairy came back from a save as the first
 * colour; here it keeps its colour. OreSpawn's hurt sound ("orespawn:rat_hit") didn't exist, so fairies are silent when
 * hit, as they were.
 */
public class Fairy extends AmbientCreature {
	private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(Fairy.class, EntityDataSerializers.INT);
	private final int myBlink;
	private int blinker;
	private @Nullable BlockPos flightTarget;
	private @Nullable UUID owner;

	public Fairy(EntityType<? extends Fairy> type, Level level) {
		super(type, level);
		this.myBlink = 20 + random.nextInt(20);
		if (!level.isClientSide()) entityData.set(TYPE, random.nextInt(9));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 40.0).add(Attributes.MOVEMENT_SPEED, 0.1).add(Attributes.ATTACK_DAMAGE, 3.0)
			.add(Attributes.ARMOR, 4.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new LookAtPlayerGoal(this, Mob.class, 8.0F));
		goalSelector.addGoal(1, new RandomLookAroundGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(TYPE, 0);
	}

	/** Its colour, 0-8 (fairytexture.png, fairytexture2.png ... fairytexture9.png). */
	public int variant() {
		return entityData.get(TYPE);
	}

	/** Fairy.getBlink: the body is lit for the first half of each blink cycle (20-39 ticks, its own). */
	public boolean blinking() {
		return blinker < myBlink / 2;
	}

	/** The Fairy Sword's fairies: they follow this player. */
	public void setOwner(@Nullable LivingEntity e) {
		if (e instanceof Player p) owner = p.getUUID();
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("FairyType", variant());
		if (owner != null) output.store("MyOwner", UUIDUtil.CODEC, owner);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		entityData.set(TYPE, input.getIntOr("FairyType", 0));
		owner = input.read("MyOwner", UUIDUtil.CODEC).orElse(null);
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
		return target.hurtServer(level, damageSources().mobAttack(this), 2.0F);
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected @Nullable SoundEvent getHurtSound(DamageSource source) {
		return null;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.BIG_SPLAT;
	}

	@Override
	protected float getSoundVolume() {
		return 0.25F;
	}

	@Override
	public float getVoicePitch() {
		return 1.7F;
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	@Override
	protected void doPush(Entity entity) {
	}

	@Override
	protected void pushEntities() {
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return !isPersistenceRequired() && owner == null;
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
	}

	@Override
	public boolean isIgnoringBlockTriggers() {
		return true;
	}

	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.NONE;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		// getDropItem: the Crystal Torch (another group's block), with 1.7.10's default count.
		Item torch = ModItems.lazy("crystal_torch");
		int n = SpookyHelpers.fewItems(random, SpookyHelpers.looting(level, source));
		for (int i = 0; i < n; i++) spawnAtLocation(level, torch);
	}

	@Override
	public void tick() {
		super.tick();
		setDeltaMovement(getDeltaMovement().multiply(1.0, 0.6, 1.0));
		if (++blinker > myBlink) blinker = 0;
		Level level = level();
		if (level.isClientSide() && level.getDefaultClockTime() % 24000L >= 12000L && level.getRandom().nextInt(5) == 0 && blinking()) {
			RandomSource r = level.getRandom();
			level.addParticle(ParticleTypes.FIREWORK, getX(), getY() - 0.15F, getZ(), (r.nextFloat() - r.nextFloat()) / 8.0F, -r.nextFloat() / 8.0F,
				(r.nextFloat() - r.nextFloat()) / 8.0F);
		}
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		BlockPos here = blockPosition();
		if (flightTarget == null) flightTarget = here;
		if (random.nextInt(200) != 0 && !(Flight.distSq(this, flightTarget) < 2.5)) {
			if (random.nextInt(12) == 0 && level.getDifficulty() != Difficulty.PEACEFUL) {
				LivingEntity prey = SpookyHelpers.closest(this, getBoundingBox().inflate(8.0), this::isPrey);
				if (prey != null) {
					flightTarget = new BlockPos((int) prey.getX(), (int) (prey.getY() + 1.0), (int) prey.getZ());
					if (distanceToSqr(prey) < 6.0) doHurtTarget(level, prey);
				}
			} else if (owner != null) {
				Player p = level.getPlayerByUUID(owner);
				if (p != null) {
					if (distanceToSqr(p) > 64.0) {
						flightTarget = new BlockPos((int) p.getX() + random.nextInt(3) - random.nextInt(3), (int) (p.getY() + 1.0),
							(int) p.getZ() + random.nextInt(3) - random.nextInt(3));
					}
					if (distanceToSqr(p) > 256.0) {
						setPos(p.getX() + random.nextFloat() - random.nextFloat(), p.getY(), p.getZ() + random.nextFloat() - random.nextFloat());
					}
				}
			}
		} else {
			// A new spot up to 7 blocks away, 2 up or down: open air it can see.
			for (int tries = 25; tries > 0; tries--) {
				int zdir = random.nextInt(8);
				int xdir = random.nextInt(8);
				if (random.nextInt(2) == 0) zdir = -zdir;
				if (random.nextInt(2) == 0) xdir = -xdir;
				flightTarget = new BlockPos(here.getX() + xdir, here.getY() + random.nextInt(5) - 2, here.getZ() + zdir);
				if (level.isEmptyBlock(flightTarget) && canSee(flightTarget)) break;
			}
		}
		if (random.nextInt(250) == 1) heal(1.0F);
		Flight.steer(this, flightTarget, 0.2, 0.1, 0.1, 0.5, 0.2F, 0.25F);
	}

	/** Fairy.canSeeTarget: nothing solid between it and the target's corner. */
	private boolean canSee(BlockPos target) {
		Vec3 from = new Vec3(getX(), getY() + 0.25, getZ());
		Vec3 to = new Vec3(target.getX(), target.getY(), target.getZ());
		return level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
	}

	/** Fairy.isSuitableTarget: any monster it can see. */
	private boolean isPrey(LivingEntity e) {
		return e != this && e.isAlive() && e instanceof Monster && getSensing().hasLineOfSight(e);
	}

	/** Fairy.getCanSpawnHere: at least 6 of the 9 blocks around it at its height are air, and it's at y 50 or higher. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		int air = 0;
		for (int k = -1; k <= 1; k++) {
			for (int j = -1; j <= 1; j++) {
				if (level.isEmptyBlock(pos.offset(j, 0, k))) air++;
			}
		}
		return air >= 6 && pos.getY() >= 50;
	}
}
