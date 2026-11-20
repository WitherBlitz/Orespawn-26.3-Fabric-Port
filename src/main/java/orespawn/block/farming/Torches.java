package orespawn.block.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import orespawn.OreSpawn;

import java.util.Set;

/**
 * What OreSpawn's torches do: the Extreme Torch (BlockExtremeTorch) and the Creeper and Kraken Repellents (both
 * BlockTorch). Other groups' blocks and creatures are matched by id.
 */
public final class Torches {
	/** Which repellent a torch is, if any. */
	public enum Repel { CREEPERS, KRAKENS }

	public static final ResourceKey<net.minecraft.world.level.block.Block> EYE_OF_ENDER_BLOCK =
		ResourceKey.create(Registries.BLOCK, OreSpawn.id("eye_of_ender_block"));
	private static final Identifier CEPHADROME = OreSpawn.id("cephadrome");
	private static final Identifier KRAKEN = OreSpawn.id("the_kraken");
	/** OreSpawn's EntityAnt and its subclasses. */
	private static final Set<Identifier> ANTS = Set.of(OreSpawn.id("ant"), OreSpawn.id("red_ant"), OreSpawn.id("rainbow_ant"),
		OreSpawn.id("unstable_ant"), OreSpawn.id("termite"));
	/** CreeperRepellent / KrakenRepellent.tickRate. */
	public static final int REPEL_TICKS = 10;

	private Torches() {
	}

	/** OreSpawn's torch particles (smoke, flame and red dust), at {@code height} above y + 0.7, toward the wall it's on. */
	public static void particles(Level level, BlockPos pos, double height, int stepX, int stepZ) {
		double x = pos.getX() + 0.5 + 0.271 * stepX;
		double y = pos.getY() + 0.7 + height;
		double z = pos.getZ() + 0.5 + 0.271 * stepZ;
		level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
		level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
		level.addParticle(DustParticleOptions.REDSTONE, x, y, z, 0.0, 0.0, 0.0);
	}

	/**
	 * BlockExtremeTorch.onBlockPlacedBy: an Extreme Torch set on an Eye-of-Ender Block looks (100 tries) for a free
	 * spot about 4 blocks away and, if it finds one, summons a Cephadrome there and goes out with a bang.
	 */
	public static void summonCephadrome(Level level, BlockPos pos) {
		if (!(level instanceof ServerLevel server) || !level.getBlockState(pos.below()).is(EYE_OF_ENDER_BLOCK)) return;
		RandomSource random = level.getRandom();
		BlockPos found = null;
		for (int tries = 0; tries < 100 && found == null; tries++) {
			int x = pos.getX() + (random.nextInt(2) == 0 ? 4 : -4) + random.nextInt(3) - random.nextInt(3);
			int z = pos.getZ() + (random.nextInt(2) == 0 ? 4 : -4) + random.nextInt(3) - random.nextInt(3);
			for (int y = pos.getY() - 2; y <= pos.getY() + 2; y++) {
				BlockPos p = new BlockPos(x, y, z);
				if (level.getBlockState(p.below()).isSolid() && level.getBlockState(p).isAir() && level.getBlockState(p.above()).isAir()) {
					found = p;
					break;
				}
			}
		}
		if (found == null) return;
		CritterPlantBlock.spawn(server, CEPHADROME, found.getX() + 0.5, found.getY() + 0.01, found.getZ() + 0.5);
		server.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 16, 0.5, 0.5, 0.5, 0.0);
		server.sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 16, 0.5, 0.5, 0.5, 0.0);
		server.sendParticles(DustParticleOptions.REDSTONE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 16, 0.5, 0.5, 0.5, 0.0);
		level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.0F, random.nextFloat() * 0.2F + 0.9F);
		level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
	}

	/**
	 * CreeperRepellent / KrakenRepellent.findSomethingToRepell, every 10 ticks: creepers (or the Kraken) and OreSpawn's
	 * ants within 20 blocks are pushed straight away from it, harder the closer they are (0.4 x (20 - distance)). The
	 * Kraken's distance is taken from 15 blocks below its middle, and the Kraken Repellent reaches 40 blocks up.
	 */
	public static void repel(ServerLevel level, BlockPos pos, Repel kind) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		AABB box = new AABB(x - 20.0, y - 10.0, z - 20.0, x + 20.0, y + (kind == Repel.KRAKENS ? 40.0 : 10.0), z + 20.0);
		for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
			Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
			if (kind == Repel.CREEPERS && e instanceof Creeper || ANTS.contains(id)) {
				push(e, x, y, z, 0.0);
			} else if (kind == Repel.KRAKENS && KRAKEN.equals(id)) {
				push(e, x, y, z, 15.0);
			}
		}
	}

	private static void push(LivingEntity e, int x, int y, int z, double below) {
		double dx = e.getX() - x, dy = e.getY() - below - y, dz = e.getZ() - z;
		double f = Math.max(0.0, Math.min(20.0, 20.0 - Math.sqrt(dx * dx + dy * dy + dz * dz))) * 0.4;
		double angle = (float) Math.atan2(e.getX() - x, e.getZ() - z);
		Vec3 motion = e.getDeltaMovement();
		e.setDeltaMovement(motion.x + f * Math.sin(angle), motion.y, motion.z + f * Math.cos(angle));
		e.needsSync = true;
	}
}
