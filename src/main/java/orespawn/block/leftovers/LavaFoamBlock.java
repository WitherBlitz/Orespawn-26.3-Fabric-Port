package orespawn.block.leftovers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * OreSpawn's Lava Foam ({@code Lavafoam}): a rock block (hardness 5, resistance 5, needs a pickaxe) found in veins in the
 * Nether's netherrack, and what the Bouncy Castle is built from.
 * <ul>
 * <li>Slippery: friction 1.1, so things on it speed up rather than slow down (set in its properties).</li>
 * <li>Its collision box is 0.0125 smaller at each side, so a living thing that walks into a side touches it and is
 * bounced straight back out at 0.45 blocks a tick, its sideways speed along the face multiplied by 1.35; if that adds up
 * to more than 1 block a tick it takes that much fall damage.</li>
 * <li>Now and then gives off smoke and red dust at its faces.</li>
 * <li>Drops itself; mined in the Nether it also drops 5-13 experience (none with Silk Touch, as in 1.7.10).</li>
 * </ul>
 * It's a full, solid block for everything else (suffocation, redstone, support for torches and mob spawning), as it was.
 */
public class LavaFoamBlock extends Block {
	private static final double F = 0.0125 * 16.0;
	private static final VoxelShape COLLISION = Block.box(F, 0.0, F, 16.0 - F, 16.0, 16.0 - F);

	public LavaFoamBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return COLLISION;
	}

	@Override
	protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
		return Shapes.block();
	}

	/** Lavafoam.onEntityCollidedWithBlock: push living things away from the side they touched. */
	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
		if (!(entity instanceof LivingEntity)) return;
		double pi = 3.14159;
		double pi2 = pi / 2.0;
		double pi4 = pi / 4.0;
		Vec3 motion = entity.getDeltaMovement();
		double mx = motion.x;
		double mz = motion.z;
		double d = Math.atan2(entity.getX() - (pos.getX() + 0.5F), entity.getZ() - (pos.getZ() + 0.5F));
		if (d < 0.0) d += pi * 2.0;
		if (d > pi2 - pi4 && d < pi2 + pi4) {
			mx = 0.45F;
			mz *= 1.35F;
		} else if (d > pi - pi4 && d < pi + pi4) {
			mz = -0.45F;
			mx *= 1.35F;
		} else if (d > pi + pi2 - pi4 && d < pi + pi2 + pi4) {
			mx = -0.45F;
			mz *= 1.35F;
		} else {
			mz = 0.45F;
			mx *= 1.35F;
		}
		entity.setDeltaMovement(mx, motion.y, mz);
		d = Math.sqrt(mz * mz + mx * mx);
		if (d > 1.0 && level instanceof ServerLevel server) entity.hurtServer(server, level.damageSources().fall(), (float) d);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextInt(20) == 0) this.sparkle(level, pos, random);
	}

	/** Lavafoam.sparkle, kept as it was (its height check compares with 0, so no dust shows under the block). */
	private void sparkle(Level level, BlockPos pos, RandomSource random) {
		double off = 0.0625;
		int px = pos.getX();
		int py = pos.getY();
		int pz = pos.getZ();
		for (int i = 0; i < 6; i++) {
			double x = px + random.nextFloat();
			double y = py + random.nextFloat();
			double z = pz + random.nextFloat();
			if (i == 0 && !level.getBlockState(pos.above()).isSolidRender()) y = py + 1 + off;
			if (i == 1 && !level.getBlockState(pos.below()).isSolidRender()) y = py - off;
			if (i == 2 && !level.getBlockState(pos.south()).isSolidRender()) z = pz + 1 + off;
			if (i == 3 && !level.getBlockState(pos.north()).isSolidRender()) z = pz - off;
			if (i == 4 && !level.getBlockState(pos.east()).isSolidRender()) x = px + 1 + off;
			if (i == 5 && !level.getBlockState(pos.west()).isSolidRender()) x = px - off;
			if (x < px || x > px + 1 || y < 0.0 || y > py + 1 || z < pz || z > pz + 1) {
				int which = random.nextInt(10);
				if (which == 1) level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
				if (which == 2) level.addParticle(DustParticleOptions.REDSTONE, x, y, z, 0.0, 0.0, 0.0);
			}
		}
	}

	/** Lavafoam.dropBlockAsItemWithChance: 5-13 experience, only in the Nether. */
	@Override
	protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, boolean dropExperience) {
		super.spawnAfterBreak(state, level, pos, tool, dropExperience);
		if (dropExperience && level.dimension() == Level.NETHER) {
			RandomSource random = level.getRandom();
			this.tryDropExperience(level, pos, tool, ConstantInt.of(5 + random.nextInt(5) + random.nextInt(5)));
		}
	}
}
