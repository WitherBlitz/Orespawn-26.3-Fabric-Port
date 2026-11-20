package orespawn.entity.bugs;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import orespawn.content.Bugs;
import orespawn.entity.ModEntities;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's CaterKiller: a towering spiked caterpillar of the daytime forests. It wanders, munching the leaves, logs
 * and vines around it (with mobGriefing on) to heal, and goes for monsters, villagers and players within 20 blocks: close
 * up it bites (32 damage and a long shove), farther off it chases them and drops cobwebs at their feet. It tears out
 * webs it gets caught in. If it stays hurt for two minutes it bursts into a Brutalfly and a swarm of butterflies.
 * 450 health, 19 armor. Drops CaterKiller Jaws, an item frame, leather, beef, some ruby gear, maybe The Ultimate Sword or
 * Bow, and 25 butterflies.
 * <p>
 * Not ported yet: OreSpawn's own leaves and the Duplicator Tree log among what it eats (vanilla's leaves and logs tags
 * stand in), its other non-monster targets (dragons, Girlfriend and so on), its Utopia dimension spawns.
 */
public class CaterKiller extends BugMonster {
	private static final Item[] RUBY_GEAR = {ModItems.byName("ruby_sword"), ModItems.byName("ruby_shovel"), ModItems.byName("ruby_pickaxe"),
		ModItems.byName("ruby_axe"), ModItems.byName("ruby_hoe"), ModItems.byName("ruby_helmet"), ModItems.byName("ruby_chestplate"),
		ModItems.byName("ruby_leggings"), ModItems.byName("ruby_boots")};
	private int foundMob;
	private int ticker;
	private boolean inWeb;
	private int closest = 99999;
	private int tx;
	private int ty;
	private int tz;

	public CaterKiller(EntityType<? extends CaterKiller> type, Level level) {
		super(type, level);
		this.xpReward = 200;
		setPathfindingMalus(PathType.WATER, -1.0F);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 450.0).add(Attributes.MOVEMENT_SPEED, 0.35)
			.add(Attributes.ATTACK_DAMAGE, 32.0).add(Attributes.ARMOR, 19.0);
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
		return random.nextInt(3) == 0 ? ModSounds.get("caterkiller_living") : null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("caterkiller_hit");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("caterkiller_death");
	}

	@Override
	protected float getSoundVolume() {
		return 1.5F;
	}

	/** 1.7.10's setInWeb: remembered, so it can tear the webs out. */
	@Override
	public void makeStuckInBlock(BlockState state, Vec3 speedMultiplier) {
		super.makeStuckInBlock(state, speedMultiplier);
		if (state.is(Blocks.COBWEB)) inWeb = true;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		boolean ret = super.hurtServer(level, source, amount);
		if (source.getEntity() instanceof Mob attacker) setTarget(attacker);
		return ret;
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (!super.doHurtTarget(level, target)) return false;
		if (target instanceof LivingEntity) knock(target, 1.2, 0.1);
		return true;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (getHealth() + 1.0F < getMaxHealth() && ++ticker > 2400) {
			// Hurt too long: it becomes a Brutalfly.
			summon(level, Bugs.BRUTALFLY, getX(), getY() + 4.0, getZ());
			playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.0F, random.nextFloat() * 0.2F + 0.9F);
			for (int i = 0; i < 10; i++) summon(level, ModEntities.BUTTERFLY, getX(), getY() + 1.0 + random.nextInt(4), getZ());
			discard();
			return;
		}
		if (inWeb) {
			for (int i = -2; i <= 2; i++) {
				for (int j = -1; j < 5; j++) {
					for (int k = -2; k <= 2; k++) {
						BlockPos p = new BlockPos(getBlockX() + i, getBlockY() + j, getBlockZ() + k);
						if (level.getBlockState(p).is(Blocks.COBWEB)) level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
					}
				}
			}
			inWeb = false;
		}
		if (random.nextInt(4) == 0) {
			LivingEntity e = getTarget();
			if (e != null && !e.isAlive()) {
				setTarget(null);
				e = null;
			}
			if (random.nextInt(200) == 0) setTarget(null);
			if (e == null) e = findTarget(20.0, 8.0, 20.0, this::isSuitableTarget);
			if (e != null) {
				foundMob = 1;
				lookAt(e, 10.0F, 10.0F);
				double reach = 5.0F + e.getBbWidth() / 2.0F;
				if (distanceToSqr(e) < reach * reach) {
					setAttacking(true);
					if (random.nextInt(3) == 0 || random.nextInt(4) == 1) doHurtTarget(level, e);
				} else {
					setAttacking(false);
					getNavigation().moveTo(e, 1.25);
					if (random.nextInt(4) == 0) {
						// A cobweb on the first ground from 2 above to 1 below its prey's feet.
						int x = Mth.floor(e.getX() + (random.nextFloat() - random.nextFloat()) * 2.0);
						int z = Mth.floor(e.getZ() + (random.nextFloat() - random.nextFloat()) * 2.0);
						int y = e.getBlockY();
						for (int i = 2; i > -2; i--) {
							if (level.getBlockState(new BlockPos(x, y + i + 1, z)).isAir() && !level.getBlockState(new BlockPos(x, y + i, z)).isAir()) {
								level.setBlock(new BlockPos(x, y + i + 1, z), Blocks.COBWEB.defaultBlockState(), 3);
								break;
							}
						}
					}
				}
			} else {
				setAttacking(false);
				foundMob = 0;
			}
		}
		if (random.nextInt(8) == 0 && getHealth() < getMaxHealth() || random.nextInt(30) == 0) {
			// Look for the nearest leaves or wood in growing shells around it, walk over (unless hunting) and eat it.
			closest = 99999;
			tx = ty = tz = 0;
			for (int i = 1; i < 13; i++) {
				int j = Math.min(i, 9);
				if (scan(level, getBlockX(), getBlockY() + 1, getBlockZ(), i, j, i)) break;
				if (i >= 9) i++;
			}
			if (closest < 99999) {
				if (foundMob == 0) getNavigation().moveTo(tx, ty, tz, 1.0);
				if (closest < 81) {
					if (level.getGameRules().get(GameRules.MOB_GRIEFING)) level.setBlock(new BlockPos(tx, ty, tz), Blocks.AIR.defaultBlockState(), 2);
					heal(2.0F);
					if (random.nextInt(20) == 1) playSound(SoundEvents.PLAYER_BURP, 1.0F, random.nextFloat() * 0.2F + 0.9F);
				}
			}
		}
	}

	private static boolean isFood(BlockState s) {
		return s.is(BlockTags.LEAVES) || s.is(BlockTags.LOGS) || s.is(Blocks.VINE);
	}

	/** scan_it: the faces of a box dx by dy by dz around x, y, z; keeps the closest food found (as tx, ty, tz). */
	private boolean scan(ServerLevel level, int x, int y, int z, int dx, int dy, int dz) {
		int found = 0;
		for (int i = -dy; i <= dy; i++) {
			for (int j = -dz; j <= dz; j++) {
				found += consider(level, x + dx, y + i, z + j, dx * dx + j * j + i * i);
				found += consider(level, x - dx, y + i, z + j, dx * dx + j * j + i * i);
			}
		}
		for (int i = -dx; i <= dx; i++) {
			for (int j = -dz; j <= dz; j++) {
				found += consider(level, x + i, y + dy, z + j, dy * dy + j * j + i * i);
				found += consider(level, x + i, y - dy, z + j, dy * dy + j * j + i * i);
			}
		}
		for (int i = -dx; i <= dx; i++) {
			for (int j = -dy; j <= dy; j++) {
				found += consider(level, x + i, y + j, z + dz, dz * dz + j * j + i * i);
				found += consider(level, x + i, y + j, z - dz, dz * dz + j * j + i * i);
			}
		}
		return found != 0;
	}

	private int consider(ServerLevel level, int x, int y, int z, int d) {
		if (!isFood(level.getBlockState(new BlockPos(x, y, z))) || d >= closest) return 0;
		closest = d;
		tx = x;
		ty = y;
		tz = z;
		return 1;
	}

	/** CaterKiller.isSuitableTarget: what it can see past leaves and webs, players (not in creative), monsters and villagers. */
	private boolean isSuitableTarget(LivingEntity e) {
		if (e == this || !e.isAlive() || !canSeeThrough(e, 2.5, 3.0, CaterKiller::seesThrough)) return false;
		if (e instanceof Player) return !isCreativeOrSpectator(e);
		if (e instanceof CaterKiller) return false;
		return e instanceof Monster || e instanceof Villager;
	}

	/** MyCanSee: air, cobwebs, tall grass and (1.7.10's first) leaves don't block its view. */
	private static boolean seesThrough(BlockState s) {
		return s.is(Blocks.COBWEB) || s.is(Blocks.SHORT_GRASS) || s.is(Blocks.FERN) || s.is(Blocks.OAK_LEAVES) || s.is(Blocks.SPRUCE_LEAVES)
			|| s.is(Blocks.BIRCH_LEAVES) || s.is(Blocks.JUNGLE_LEAVES);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		dropRand(level, Bugs.CATERKILLER_JAWS, 5);
		dropRand(level, Items.ITEM_FRAME, 5);
		for (int i = 0; i < 10; i++) dropRand(level, Items.LEATHER, 5);
		for (int i = 0; i < 6; i++) dropRand(level, Items.BEEF, 5);
		dropGear(level, ModItems.byName("ultimate_sword"), ModItems.RUBY, RUBY_GEAR, ModItems.lazy("the_ultimate_bow"));
		for (int i = 0; i < 25; i++) summon(level, ModEntities.BUTTERFLY, getX(), getY() + 1.0, getZ());
	}

	/**
	 * CaterKiller.getCanSpawnHere: y 50 or higher, then only 1 try in 10, by day, nothing but air, leaves and logs 3x4x3
	 * above, and no other CaterKiller within 48 blocks.
	 */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (pos.getY() < 50 || random.nextInt(10) != 0 || !level.getLevel().isBrightOutside()) return false;
		for (int z = -1; z < 2; z++) {
			for (int x = -1; x < 2; x++) {
				for (int y = 1; y < 5; y++) {
					BlockState s = level.getBlockState(pos.offset(x, y, z));
					if (!s.isAir() && !s.is(BlockTags.LEAVES) && !s.is(BlockTags.LOGS)) return false;
				}
			}
		}
		return noneNear(level, type, CaterKiller.class, pos, 48.0, 16.0, 48.0);
	}
}
