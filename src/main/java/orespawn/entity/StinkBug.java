package orespawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

/**
 * OreSpawn's stink bug: a slow, silent beetle that wanders, keeps a few blocks from players, and near a village at
 * night or in the rain heads for the indoor side of a door. When it dies it farts: every living thing from 8 blocks
 * around it (5 below to 10 above) gets 15 s of nausea. Drops 0-2 dead stink bugs. It bred with OreSpawn's Crystal
 * Apple, which comes with the Crystal dimension, so it can't be bred yet.
 */
public class StinkBug extends Animal {
	public StinkBug(EntityType<? extends StinkBug> type, Level level) {
		super(type, level);
		this.xpReward = 2;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 5.0).add(Attributes.MOVEMENT_SPEED, 0.15)
			.add(Attributes.ATTACK_DAMAGE, 0.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new BreedGoal(this, 1.0));
		goalSelector.addGoal(4, new PanicGoal(this, 1.5));
		goalSelector.addGoal(5, new AvoidEntityGoal<>(this, Player.class, 4.0F, 1.0, 1.4F));
		goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
		goalSelector.addGoal(8, WanderGoal.wanderALot(this, 10, 1.0));
		goalSelector.addGoal(9, new RandomLookAroundGoal(this));
		goalSelector.addGoal(10, new MoveIndoorsGoal(this));
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		if (random.nextInt(200) == 1) setLastHurtByMob(null);
		super.customServerAiStep(level);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		if (isRemoved()) return false;
		boolean hurt = super.hurtServer(level, source, damage);
		if (getHealth() <= 0.0F || isRemoved()) {
			// StinkBug.attackEntityFrom: the dying stink (again for each hit while it's dying, as in OreSpawn).
			AABB area = new AABB(getX() - 8.0, getY() - 5.0, getZ() - 8.0, getX() + 8.0, getY() + 10.0, getZ() + 8.0);
			for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, area)) {
				e.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 300, 0));
			}
		}
		return hurt;
	}

	@Override
	protected @Nullable SoundEvent getHurtSound(DamageSource source) {
		return null;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("fart");
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		if (isBaby()) {
			setPersistenceRequired();
			return false;
		}
		return !isPersistenceRequired();
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return orespawn.entity.Breeding.crystalApple(stack);
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return ModEntities.STINK_BUG.create(level, EntitySpawnReason.BREEDING);
	}

	/** StinkBug.getCanSpawnHere: at y 50 or higher. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		return EntitySpawnReason.isSpawner(reason) || pos.getY() >= 50;
	}

	/**
	 * 1.7.10's EntityAIMoveIndoors: at night or in the rain (where there's a sky), near a village, a 1 in 50 chance each
	 * check of heading for the inside of the nearest wooden door within 16 blocks (4 up or down). As 1.7.10's villages
	 * worked it out, the inside is 2 blocks from the door on the side with less open sky within 5 blocks; a door with
	 * the same sky on both sides doesn't count. Further than 16 blocks, it only heads that way. It doesn't pick the same
	 * spot again while it's within 2 blocks of it.
	 */
	private static final class MoveIndoorsGoal extends Goal {
		private final PathfinderMob mob;
		private @Nullable BlockPos inside;
		private @Nullable BlockPos lastInside;

		MoveIndoorsGoal(PathfinderMob mob) {
			this.mob = mob;
			setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			Level level = mob.level();
			if ((level.isBrightOutside() && !level.isRaining()) || !level.dimensionType().hasSkyLight()) return false;
			if (mob.getRandom().nextInt(50) != 0) return false;
			if (lastInside != null && mob.distanceToSqr(lastInside.getX(), mob.getY(), lastInside.getZ()) < 4.0) return false;
			if (!(level instanceof ServerLevel server) || !server.isVillage(mob.blockPosition())) return false;
			inside = findInside(server);
			return inside != null;
		}

		@Override
		public boolean canContinueToUse() {
			return !mob.getNavigation().isDone();
		}

		@Override
		public void start() {
			lastInside = null;
			Vec3 target = Vec3.atBottomCenterOf(inside);
			if (mob.distanceToSqr(inside.getX(), inside.getY(), inside.getZ()) > 256.0) {
				Vec3 towards = DefaultRandomPos.getPosTowards(mob, 14, 3, target, Math.PI / 2);
				if (towards != null) mob.getNavigation().moveTo(towards.x, towards.y, towards.z, 1.0);
			} else {
				mob.getNavigation().moveTo(target.x, target.y, target.z, 1.0);
			}
		}

		@Override
		public void stop() {
			lastInside = inside;
			inside = null;
		}

		private @Nullable BlockPos findInside(ServerLevel level) {
			BlockPos me = mob.blockPosition();
			BlockPos best = null;
			double bestDist = Double.MAX_VALUE;
			for (BlockPos p : BlockPos.betweenClosed(me.offset(-16, -4, -16), me.offset(16, 4, 16))) {
				BlockState state = level.getBlockState(p);
				if (!state.is(BlockTags.WOODEN_DOORS) || state.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) continue;
				double d = p.distSqr(me);
				if (d >= bestDist) continue;
				Direction facing = state.getValue(DoorBlock.FACING);
				int sky = 0;
				for (int i = 1; i <= 5; i++) {
					if (level.canSeeSky(p.relative(facing, i))) sky++;
					if (level.canSeeSky(p.relative(facing, -i))) sky--;
				}
				if (sky == 0) continue;
				best = p.relative(facing, sky > 0 ? -2 : 2);
				bestDist = d;
			}
			return best;
		}
	}
}
