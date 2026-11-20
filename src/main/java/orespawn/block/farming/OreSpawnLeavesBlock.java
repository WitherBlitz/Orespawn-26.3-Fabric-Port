package orespawn.block.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownExperienceBottle;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import orespawn.content.Farming;

/**
 * OreSpawn's own leaves: BlockAppleLeaves, BlockScaryLeaves (also its Cherry and Peach Leaves) and
 * BlockExperienceLeaves. They don't use vanilla's leaf distance: on every random tick they look for a log within 3
 * blocks (counted along the axes, at their level or up to 2 below) and, with none, fall apart; that's also when the
 * fruit leaves can let their fruit fall.
 * <ul>
 * <li>Apple Leaves: when they break or fall apart, a 1 in 25 chance of an apple, 1 in 500 of a golden apple, 1 in 1000
 * of an enchanted golden apple and 1 in 10,000 of OreSpawn's Magic Apple. Near a log with air below, 1 in 20 random
 * ticks drops that roll below them.</li>
 * <li>Cherry and Peach Leaves: the same with a 1 in 25 chance of cherries or a peach.</li>
 * <li>Scary Leaves: drop nothing, and turn into Apple Leaves in the day (before 12000) while near a log.</li>
 * <li>Experience Leaves: drop nothing; at night (14000-22000) they now and then drop a Bottle o' Enchanting two
 * blocks above (1 in 65) and throw one down (1 in 75), and sparkle.</li>
 * </ul>
 * Shears also give the leaves themselves (loot tables). They're tinted with the foliage colour, as 1.7.10's leaves were.
 * Not ported yet: Apple Leaves turning into Scary Leaves at night in OreSpawn's Islands dimension.
 */
public class OreSpawnLeavesBlock extends Block {
	public enum Kind { APPLE, CHERRY, PEACH, SCARY, EXPERIENCE }

	private final Kind kind;

	public OreSpawnLeavesBlock(Kind kind, BlockBehaviour.Properties properties) {
		super(properties);
		this.kind = kind;
	}

	@Override
	protected int getLightDampening(BlockState state) {
		return 1;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!nearLog(level, pos)) {
			dropResources(state, level, pos);
			level.removeBlock(pos, false);
			return;
		}
		long time = level.getOverworldClockTime() % 24000L;
		switch (kind) {
			case APPLE, CHERRY, PEACH -> {
				if (level.getBlockState(pos.below()).isAir() && random.nextInt(20) == 3) dropFruit(level, pos.below());
			}
			case SCARY -> {
				if (time < 12000L) level.setBlock(pos, Farming.APPLE_LEAVES.defaultBlockState(), Block.UPDATE_ALL);
			}
			case EXPERIENCE -> {
				if (time < 14000L || time > 22000L) return;
				if (random.nextInt(65) == 1 && level.getBlockState(pos.above()).isAir()) {
					popResource(level, pos.above(2), new ItemStack(Items.EXPERIENCE_BOTTLE));
				}
				if (random.nextInt(75) == 1 && level.getBlockState(pos.below()).isAir()) {
					ThrownExperienceBottle bottle = new ThrownExperienceBottle(level, pos.getX(), pos.getY() - 1, pos.getZ(),
						new ItemStack(Items.EXPERIENCE_BOTTLE));
					bottle.shoot((random.nextFloat() - random.nextFloat()) / 2.0F, -0.1F, (random.nextFloat() - random.nextFloat()) / 2.0F, 0.4F, 5.0F);
					level.addFreshEntity(bottle);
				}
			}
		}
	}

	/** A log within 3 steps along the axes, at this level or up to 2 below (OreSpawn's canSustainLeaves search). */
	private static boolean nearLog(Level level, BlockPos pos) {
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		for (int dx = -2; dx <= 2; dx++) {
			for (int dy = -2; dy <= 0; dy++) {
				for (int dz = -2; dz <= 2; dz++) {
					if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > 3) continue;
					if (level.getBlockState(p.setWithOffset(pos, dx, dy, dz)).is(BlockTags.LOGS)) return true;
				}
			}
		}
		return false;
	}

	/** OreSpawn's dropBlockAsItemWithChance: the fruit, whenever the leaves break or fall apart. */
	@Override
	protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, boolean dropExperience) {
		super.spawnAfterBreak(state, level, pos, tool, dropExperience);
		dropFruit(level, pos);
	}

	private void dropFruit(ServerLevel level, BlockPos pos) {
		RandomSource random = level.getRandom();
		switch (kind) {
			case APPLE -> {
				if (random.nextInt(25) == 1) popResource(level, pos, new ItemStack(Items.APPLE));
				if (random.nextInt(500) == 2) popResource(level, pos, new ItemStack(Items.GOLDEN_APPLE));
				if (random.nextInt(1000) == 3) popResource(level, pos, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE));
				if (random.nextInt(10000) == 4) popResource(level, pos, new ItemStack(Farming.MAGIC_APPLE));
			}
			case CHERRY -> {
				if (random.nextInt(25) == 1) popResource(level, pos, new ItemStack(Farming.CHERRIES));
			}
			case PEACH -> {
				if (random.nextInt(25) == 1) popResource(level, pos, new ItemStack(Farming.PEACH));
			}
			default -> {
			}
		}
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		switch (kind) {
			case APPLE, CHERRY, PEACH -> {
				// BlockLeaves' drip in the rain.
				if (level.isRainingAt(pos.above()) && random.nextInt(15) == 1) {
					BlockPos below = pos.below();
					if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
						ParticleUtils.spawnParticleBelow(level, pos, random, ParticleTypes.DRIPPING_WATER);
					}
				}
			}
			case EXPERIENCE -> sparkle(level, pos, random);
			default -> {
			}
		}
	}

	/** BlockExperienceLeaves.randomDisplayTick: firework sparks at night, fading in and out at its edges. */
	private static void sparkle(Level level, BlockPos pos, RandomSource random) {
		long t = level.getOverworldClockTime() % 24000L;
		if (t < 13000L || t > 23000L) return;
		int rate = 0;
		if (t < 14000L) rate = (14000 - (int) t) / 2;
		if (t > 22000L) rate = (int) (t - 22000L) / 2;
		if (random.nextInt(200 + rate) == 1 && level.getBlockState(pos.above()).isAir()) {
			for (int i = 0; i < 10; i++) {
				level.addParticle(ParticleTypes.FIREWORK, pos.getX(), pos.getY() + 1.25, pos.getZ(), random.nextGaussian(),
					Math.abs(random.nextGaussian()), random.nextGaussian());
			}
		}
		if (random.nextInt(40 + rate) == 1 && level.getBlockState(pos.below()).isAir()) {
			for (int i = 0; i < 4; i++) {
				level.addParticle(ParticleTypes.FIREWORK, pos.getX(), pos.getY() - 1.25, pos.getZ(), random.nextFloat() - random.nextFloat(),
					-Math.abs(random.nextFloat()), random.nextFloat() - random.nextFloat());
			}
		}
	}
}
