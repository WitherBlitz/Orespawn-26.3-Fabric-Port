package orespawn.entity.bugs;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.PathType;
import orespawn.content.Bugs;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Molenoid: a giant star-nosed mole of the plains and savannas that comes out at night. It burrows along,
 * digging out the dirt, grass, gravel, sand and leaves in front of it (with mobGriefing on) and flinging Molenoid Dirt
 * behind it as it goes. It goes for monsters, villagers and players within 12 blocks that it can see through soil:
 * close up it claws them (18 damage and a shove), otherwise it buries them in heaps of Molenoid Dirt. It doesn't
 * suffocate in walls. 200 health, 12 armor. Drops a Molenoid Nose, an item frame, gold nuggets and beef.
 * <p>
 * Not ported yet: OreSpawn's other non-monster targets (its dragons, Girlfriend and so on), its Utopia dimension spawns.
 */
public class Molenoid extends BugMonster {
	private static final double MOVE_SPEED = 0.35;

	public Molenoid(EntityType<? extends Molenoid> type, Level level) {
		super(type, level);
		this.xpReward = 40;
		setPathfindingMalus(PathType.WATER, -1.0F);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 200.0).add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
			.add(Attributes.ATTACK_DAMAGE, 18.0).add(Attributes.ARMOR, 12.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
		goalSelector.addGoal(2, WanderGoal.wanderALot(this, 16, 1.0));
		goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(4, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return random.nextInt(3) == 0 ? ModSounds.get("molenoid_living") : null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("molenoid_hit");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("molenoid_death");
	}

	@Override
	protected float getSoundVolume() {
		return 1.1F;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		return !source.is(DamageTypes.IN_WALL) && super.hurtServer(level, source, amount);
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (!super.doHurtTarget(level, target)) return false;
		if (target instanceof LivingEntity) knock(target, 0.8, 0.1);
		return true;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		LivingEntity e = null;
		if (random.nextInt(4) == 0) {
			e = findTarget(12.0, 6.0, 12.0, this::isSuitableTarget);
			if (e != null) {
				lookAt(e, 10.0F, 10.0F);
				double reach = 6.0F + e.getBbWidth() / 2.0F;
				double d = distanceToSqr(e);
				if (d < reach * reach) {
					setAttacking(true);
					if (!(d < 16.0) || random.nextInt(4) != 0 && random.nextInt(5) != 1) {
						// Heap Molenoid Dirt around it, 1-4 lumps, each on the first ground from 4 above to 2 below its feet.
						for (int k = 1 + random.nextInt(4); k > 0; k--) {
							double dx = e.getX() + (random.nextFloat() - random.nextFloat()) * 2.0;
							double dz = e.getZ() + (random.nextFloat() - random.nextFloat()) * 2.0;
							throwDirt(level, Mth.floor(dx), Mth.floor(e.getY()), Mth.floor(dz), 4, -3);
						}
					} else {
						doHurtTarget(level, e);
					}
				} else {
					getNavigation().moveTo(e, 1.25);
				}
			} else {
				setAttacking(false);
			}
		}
		if (random.nextInt(2) == 0) {
			// The faster it goes, the likelier a lump of dirt lands behind it.
			double spd = Math.min(getDeltaMovement().horizontalDistance(), MOVE_SPEED);
			int odds = (int) (100.0 * spd / MOVE_SPEED);
			if (odds > 0 && random.nextInt(100) < odds) {
				double dx = getX() + 6.0 * Math.sin(Math.toRadians(getYHeadRot())) + (random.nextFloat() - random.nextFloat()) * 3.0;
				double dz = getZ() - 6.0 * Math.cos(Math.toRadians(getYHeadRot())) + (random.nextFloat() - random.nextFloat()) * 3.0;
				throwDirt(level, Mth.floor(dx), getBlockY(), Mth.floor(dz), 4, -4);
			}
		}
		// Dig out the soft blocks 3 ahead: up a level if its prey is higher, down a level if lower.
		double dx = getX() - 3.0 * Math.sin(Math.toRadians(getYHeadRot())) + (random.nextFloat() - random.nextFloat()) * 3.0;
		double dz = getZ() + 3.0 * Math.cos(Math.toRadians(getYHeadRot())) + (random.nextFloat() - random.nextFloat()) * 3.0;
		int dir = 1;
		if (e != null) {
			if (e.getBlockY() > getBlockY()) dir = 2;
			if (e.getBlockY() < getBlockY()) dir = 0;
		}
		boolean griefing = level.getGameRules().get(GameRules.MOB_GRIEFING);
		for (int i = dir; i < dir + 3; i++) {
			BlockPos p = new BlockPos(Mth.floor(dx), getBlockY() + i, Mth.floor(dz));
			BlockState state = level.getBlockState(p);
			if (griefing && isDiggable(state) || state.is(Bugs.MOLENOID_DIRT)) level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
		}
	}

	/** Puts Molenoid Dirt on the first spot from {@code top} down to above {@code bottom} that's air over something solid. */
	private static void throwDirt(ServerLevel level, int x, int y, int z, int top, int bottom) {
		for (int i = top; i > bottom; i--) {
			if (level.getBlockState(new BlockPos(x, y + i + 1, z)).isAir() && !level.getBlockState(new BlockPos(x, y + i, z)).isAir()) {
				level.setBlock(new BlockPos(x, y + i + 1, z), Bugs.MOLENOID_DIRT.defaultBlockState(), 3);
				return;
			}
		}
	}

	/** Dirt, grass, gravel, sand and leaves (1.7.10's dirt and sand blocks covered coarse dirt, podzol and red sand too). */
	private static boolean isDiggable(BlockState s) {
		return s.is(Blocks.DIRT) || s.is(Blocks.COARSE_DIRT) || s.is(Blocks.PODZOL) || s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.GRAVEL)
			|| s.is(Blocks.SAND) || s.is(Blocks.RED_SAND) || s.is(BlockTags.LEAVES);
	}

	/** Molenoid.isSuitableTarget: what it can see through soil, players (not in creative), monsters and villagers. */
	private boolean isSuitableTarget(LivingEntity e) {
		if (e == this || !e.isAlive() || !canSeeThrough(e, 2.0, 1.0, Molenoid::seesThrough)) return false;
		if (e instanceof Player) return !isCreativeOrSpectator(e);
		if (e instanceof Molenoid) return false;
		return e instanceof Monster || e instanceof Villager;
	}

	private static boolean seesThrough(BlockState s) {
		return s.is(Bugs.MOLENOID_DIRT) || s.is(Blocks.DIRT) || s.is(Blocks.COARSE_DIRT) || s.is(Blocks.PODZOL) || s.is(Blocks.GRASS_BLOCK)
			|| s.is(Blocks.SHORT_GRASS) || s.is(Blocks.FERN) || s.is(Blocks.SAND) || s.is(Blocks.RED_SAND) || s.is(Blocks.GRAVEL);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		dropRand(level, Bugs.MOLENOID_NOSE, 4);
		dropRand(level, Items.ITEM_FRAME, 4);
		for (int i = 0; i < 10; i++) dropRand(level, Items.GOLD_NUGGET, 4);
		for (int i = 0; i < 6; i++) dropRand(level, Items.BEEF, 4);
	}

	/** Molenoid.getCanSpawnHere: dark, y 50 or higher, at night, open air 2x3x2 above, and no other Molenoid within 16 blocks. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (!isDarkEnoughToSpawn(level, pos, random) || pos.getY() < 50 || level.getLevel().isBrightOutside()) return false;
		if (!allAir(level, pos, -1, 1, 1, 4, -1, 1)) return false;
		return noneNear(level, type, Molenoid.class, pos, 16.0, 8.0, 16.0);
	}
}
