package orespawn.entity.robots;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import orespawn.content.Robots;

/**
 * OreSpawn's Island, the Light Floating Island: the hidden heart of a round floating island, which an Island Block
 * grows (1 time in 25). On its first tick it builds a disc of mycelium (with the odd mushroom, and lava 1 block in 5000)
 * over shrinking layers of end stone flecked with diamond ore, 3-6 blocks across and 2-4 deep (1 in 40: 6-10 and 3-6,
 * and slower). Then it drifts: every 73 ticks it moves its heading on a little, and each time it crosses into the next
 * block it rebuilds its rim around its new centre, blasting through stone in its way; it changes heading now and then.
 * Every so often it seeds a Triffid on top if none is near. If it ever meets bedrock it stops for good. OreSpawn's
 * "IslandSpeedFactor" and "IslandSizeFactor" are at their defaults (2). Drops 0-2 Island Blocks if killed (more with
 * Looting).
 * <p>
 * Not ported yet: the Triffid (it's seeded once its group registers orespawn:triffid).
 */
public class Island extends Mob {
	static final int SPEED_FACTOR = 2;
	private float dir;
	private float speed = 0.1F;
	private int radius = 5;
	private int depth = 3;
	private final int timer = 73;
	private boolean justSpawned = true;
	private int ticker;
	private boolean once = true;
	private double myX, myZ;
	private int dirchange;

	public Island(EntityType<? extends Island> type, Level level) {
		super(type, level);
		ticker = random.nextInt(50);
		dirchange = random.nextInt(2500);
		setPersistenceRequired();
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes();
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return false;
	}

	@Override
	public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource source) {
		return false;
	}

	@Override
	public void aiStep() {
		if (level().isClientSide()) super.aiStep();
	}

	@Override
	public void tick() {
		super.tick();
		setDeltaMovement(Vec3.ZERO);
		if (!(level() instanceof ServerLevel level)) return;
		if (once) {
			myX = getX();
			myZ = getZ();
			once = false;
		}
		if (justSpawned) {
			// Centred in its block, so the island around it doesn't smother it (OreSpawn left it on the block corner).
			setPos(Mth.floor(getX()) + 0.5, getY(), Mth.floor(getZ()) + 0.5);
			myX = getX();
			myZ = getZ();
			dir = randomHeading();
			if (random.nextInt(40) != 1) {
				radius = 3 + random.nextInt(4);
				depth = 2 + random.nextInt(3);
				speed = random.nextFloat() / 50.0F * SPEED_FACTOR;
			} else {
				radius = 6 + random.nextInt(5);
				depth = 3 + random.nextInt(4);
				speed = random.nextFloat() / 200.0F * SPEED_FACTOR;
			}
			if (!build(level, true)) return;
			setBlock(level, center(0), Blocks.AIR);
			ticker = random.nextInt(50);
			dirchange = random.nextInt(10000);
		}
		if (++ticker >= timer) {
			move(level);
			ticker = 0;
		}
		if (--dirchange <= 0) {
			dirchange = random.nextInt(5000);
			dir = randomHeading();
		}
		justSpawned = false;
	}

	private float randomHeading() {
		float d = random.nextFloat() * (float) Math.PI;
		return random.nextInt(2) == 1 ? -d : d;
	}

	private BlockPos center(int dy) {
		return new BlockPos(Mth.floor(getX()), Mth.floor(getY()) + dy, Mth.floor(getZ()));
	}

	static void setBlock(ServerLevel level, BlockPos pos, Block block) {
		level.setBlock(pos, block.defaultBlockState(), Block.UPDATE_ALL);
	}

	/** The top layer: mycelium with the odd mushroom on it (lava 1 in 5000). False if it hit bedrock and stopped. */
	private boolean top(ServerLevel level, BlockPos pos) {
		BlockState s = level.getBlockState(pos);
		if (s.isAir()) {
			if (random.nextInt(5000) == 1) {
				setBlock(level, pos, Blocks.LAVA);
			} else {
				setBlock(level, pos, Blocks.MYCELIUM);
				if (random.nextInt(20) == 1 && level.getBlockState(pos.above()).isAir()) {
					setBlock(level, pos.above(), random.nextInt(2) == 1 ? Blocks.BROWN_MUSHROOM : Blocks.RED_MUSHROOM);
				}
			}
		} else if (s.is(Blocks.BEDROCK)) {
			discard();
			return false;
		}
		return true;
	}

	/**
	 * create_island ({@code wholeDisc}: every layer, rim to centre) and update_island's rebuild (just each layer's outer
	 * band, blasting stone that's in the way below the top). False if it hit bedrock and stopped.
	 */
	private boolean build(ServerLevel level, boolean wholeDisc) {
		for (int i = 0; i < depth; i++) {
			int ixlast = 0, izlast = 0;
			for (double curdir = -3.1415926; curdir < 3.1415926; curdir += 0.10471975333333333) {
				double tradius = (double) radius / (i + 1);
				double h = 0.75;
				if (!wholeDisc) {
					while (h < tradius) h += 0.35F;
					h -= 0.35F * 3.0;
					if (h < 0.75) h = 0.75;
				}
				for (; h < tradius; h += 0.35F) {
					int ix = Mth.floor(getX() + Math.cos(curdir + dir) * h);
					int iz = Mth.floor(getZ() + Math.sin(curdir + dir) * h);
					if (ix == ixlast && iz == izlast) continue;
					ixlast = ix;
					izlast = iz;
					BlockPos pos = new BlockPos(ix, Mth.floor(getY()) - i + 1, iz);
					if (i == 0) {
						if (!top(level, pos)) return false;
					} else if (!wholeDisc && level.getBlockState(pos).is(Blocks.STONE)) {
						level.explode(this, ix, getY() - i + 1.0, iz, 5.0F, Level.ExplosionInteraction.BLOCK);
					} else {
						setBlock(level, pos, random.nextInt(10) == 1 ? Blocks.DIAMOND_ORE : Blocks.END_STONE);
					}
				}
			}
		}
		return true;
	}

	/** update_island: drift; on crossing into a new block, clear the old rim and build the new one around the new centre. */
	private void move(ServerLevel level) {
		myX += speed * Math.cos(dir);
		myZ += speed * Math.sin(dir);
		if ((int) myX != (int) getX() || (int) myZ != (int) getZ()) {
			for (int i = 0; i < depth; i++) {
				int ixlast = 0, izlast = 0;
				for (double curdir = -3.3; curdir < 3.3; curdir += 0.10471975333333333 / 2.0) {
					double tradius = (double) radius / (i + 1);
					double h = 0.75;
					while (h < tradius) h += 0.35F;
					h -= 0.35F;
					if (h < 0.75) h = 0.75;
					for (; h < tradius + 0.35F; h += 0.35F / 2.0) {
						int ix = Mth.floor(getX() + Math.cos(curdir + dir) * h);
						int iz = Mth.floor(getZ() + Math.sin(curdir + dir) * h);
						if (ix == ixlast && iz == izlast) continue;
						ixlast = ix;
						izlast = iz;
						if (i == 0) {
							BlockPos above = new BlockPos(ix, Mth.floor(getY()) + 2, iz);
							BlockState s = level.getBlockState(above);
							if (s.is(Blocks.RED_MUSHROOM) || s.is(Blocks.BROWN_MUSHROOM)) setBlock(level, above, Blocks.AIR);
						}
						setBlock(level, new BlockPos(ix, Mth.floor(getY()) - i + 1, iz), Blocks.AIR);
					}
				}
			}
			setBlock(level, center(0), Blocks.END_STONE);
			setPos((int) myX + (myX < 0.0 ? -0.5 : 0.5), getY(), (int) myZ + (myZ < 0.0 ? -0.5 : 0.5));
			if (!build(level, false)) return;
			setBlock(level, center(0), Blocks.AIR);
		}
		if (random.nextInt(2 + 2000 / timer) == 1) seedTriffid(level, this);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		dropIslandBlocks(level, this, source);
	}

	/** EntityLiving.dropFewItems with getDropItem the Island Block: 0-2, and up to Looting more. */
	static void dropIslandBlocks(ServerLevel level, Mob self, DamageSource source) {
		int n = level.getRandom().nextInt(3);
		if (source.getEntity() instanceof LivingEntity killer) {
			int looting = EnchantmentHelper.getEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING),
				killer);
			if (looting > 0) n += level.getRandom().nextInt(looting + 1);
		}
		for (int i = 0; i < n; i++) self.spawnAtLocation(level, Robots.ISLAND_BLOCK);
	}

	static void seedTriffid(ServerLevel level, Entity island) {
		EntityType<?> triffid = RobotUtil.lazyType("triffid");
		if (triffid == null) return;
		AABB box = new AABB(island.getX() - 10.0, island.getY() - 5.0, island.getZ() - 10.0, island.getX() + 10.0, island.getY() + 5.0,
			island.getZ() + 10.0);
		if (!level.getEntities(triffid, box, e -> true).isEmpty()) return;
		Entity e = triffid.create(level, EntitySpawnReason.MOB_SUMMONED);
		if (e == null) return;
		e.snapTo(island.getX(), island.getY() + 2.01, island.getZ(), level.getRandom().nextFloat() * 360.0F, 0.0F);
		if (e instanceof Mob m) m.finalizeSpawn(level, level.getCurrentDifficultyAt(e.blockPosition()), EntitySpawnReason.MOB_SUMMONED, null);
		level.addFreshEntity(e);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("JustSpawned", justSpawned ? 1 : 0);
		output.putInt("Idepth", depth);
		output.putInt("Iradius", radius);
		output.putFloat("Ispeed", speed);
		output.putFloat("Idir", dir);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		justSpawned = input.getIntOr("JustSpawned", 0) != 0;
		depth = input.getIntOr("Idepth", 3);
		radius = input.getIntOr("Iradius", 5);
		speed = input.getFloatOr("Ispeed", 0.1F);
		dir = input.getFloatOr("Idir", 0.0F);
	}
}
