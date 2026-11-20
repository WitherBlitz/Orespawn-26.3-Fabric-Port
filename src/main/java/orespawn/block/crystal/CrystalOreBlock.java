package orespawn.block.crystal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * OreSpawn's crystal ores: Crystal Energy (OreCrystal), Pink Tourmaline and Tiger's Eye (OreCrystalCrystal). They glow,
 * sparkle, give 5-18 experience when broken below y 40, and some blow up when a player breaks them: Crystal Energy one
 * time in 3 (power 1.5), Pink Tourmaline one time in 10 (power 1.0), both setting fires. Drops are loot tables (Tiger's
 * Eye drops itself only half the time).
 */
public class CrystalOreBlock extends CrystalBlock {
	/** Which sparkle the block shows (OreCrystal's flame/smoke/red dust burst, or one firework spark or flame). */
	public enum Sparkle { ENERGY, PINK, TIGER }

	private static final DustParticleOptions RED_DUST = new DustParticleOptions(0xFF0000, 1.0F);
	private final Sparkle sparkle;
	private final int explodeOneIn;
	private final float power;

	public CrystalOreBlock(Sparkle sparkle, int explodeOneIn, float power, Properties properties) {
		super(properties);
		this.sparkle = sparkle;
		this.explodeOneIn = explodeOneIn;
		this.power = power;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		double x = pos.getX() + 0.5;
		double y = pos.getY() + 0.5;
		double z = pos.getZ() + 0.5;
		if (this.sparkle == Sparkle.ENERGY) {
			if (random.nextInt(5) != 0) return;
			for (int i = 0; i < 5; i++) {
				ParticleOptions p = switch (random.nextInt(3)) {
					case 0 -> ParticleTypes.FLAME;
					case 1 -> ParticleTypes.SMOKE;
					default -> RED_DUST;
				};
				level.addParticle(p, x, y, z, spread(random), spread(random), spread(random));
			}
		} else {
			if (random.nextInt(20) != 0) return;
			level.addParticle(this.sparkle == Sparkle.TIGER ? ParticleTypes.FLAME : ParticleTypes.FIREWORK, x, y, z, spread(random), spread(random),
				spread(random));
		}
	}

	private static double spread(RandomSource random) {
		return (random.nextFloat() - random.nextFloat()) / 4.0F;
	}

	/** onBlockDestroyedByPlayer: the chance of an explosion (with fire; blocks break only if mobGriefing is on). */
	@Override
	public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
		super.destroy(level, pos, state);
		if (this.explodeOneIn > 0 && level instanceof ServerLevel server && server.getRandom().nextInt(this.explodeOneIn) == 1) {
			server.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, this.power, true, Level.ExplosionInteraction.MOB);
		}
	}

	@Override
	protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, boolean dropExperience) {
		super.spawnAfterBreak(state, level, pos, tool, dropExperience);
		if (dropExperience && pos.getY() < 40) {
			RandomSource r = level.getRandom();
			this.popExperience(level, pos, 5 + r.nextInt(5) + r.nextInt(10));
		}
	}
}
