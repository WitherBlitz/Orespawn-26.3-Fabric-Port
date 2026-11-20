package orespawn.block.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * OreSpawn's BlockButterflyPlant, BlockMothPlant, BlockFireflyPlant and BlockMosquitoPlant: crops that hatch their
 * creature onto the air above them. On every random tick, after the crop's own tick, the chance is 1 in
 * ({@code rateBase} - age), certain from age 6 (5 for the firefly plant) on: so a grown plant hatches one on nearly
 * every random tick. The butterfly plant hatches by day, the moth and firefly plants by night, and those three not in
 * the rain; the mosquito plant any time. Fireflies and mosquitoes come 2-6 at a time.
 * <p>
 * The creatures are looked up by id when they hatch (orespawn:butterfly, moth, firefly, mosquito), so nothing hatches
 * from a plant whose creature isn't registered.
 */
public class CritterPlantBlock extends OreSpawnCropBlock {
	public enum When { DAY, NIGHT, ANY }

	private final Identifier creature;
	private final When when;
	private final boolean dryOnly;
	private final int rateBase;
	private final int minCount;
	private final int maxCount;

	public CritterPlantBlock(Supplier<? extends ItemLike> seed, Identifier creature, When when, boolean dryOnly, int rateBase, int minCount,
			int maxCount, BlockBehaviour.Properties properties) {
		super(seed, properties);
		this.creature = creature;
		this.when = when;
		this.dryOnly = dryOnly;
		this.rateBase = rateBase;
		this.minCount = minCount;
		this.maxCount = maxCount;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		super.randomTick(state, level, pos, random);
		if (dryOnly && level.isRaining()) return;
		// OreSpawn read the metadata after the crop's tick: 0 if the plant had just popped off.
		BlockState now = level.getBlockState(pos);
		int age = now.is(this) ? getAge(now) : 0;
		int rate = rateBase - age;
		if (rate > 1 && random.nextInt(rate) != 0) return;
		if (!level.getBlockState(pos.above()).isAir()) return;
		if (when == When.DAY && !level.isBrightOutside() || when == When.NIGHT && level.isBrightOutside()) return;
		int count = minCount + random.nextInt(maxCount - minCount + 1);
		for (int i = 0; i < count; i++) spawn(level, creature, pos.getX() + 0.5, pos.getY() + 1.01, pos.getZ() + 0.5);
	}

	/** OreSpawn's spawnCreature: makes the creature (if it's registered), faces it a random way and plays its sound. */
	public static Entity spawn(ServerLevel level, Identifier id, double x, double y, double z) {
		Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(id);
		if (type.isEmpty()) return null;
		Entity entity = type.get().create(level, EntitySpawnReason.TRIGGERED);
		if (entity == null) return null;
		entity.snapTo(x, y, z, level.getRandom().nextFloat() * 360.0F, 0.0F);
		if (entity instanceof Mob mob) {
			mob.finalizeSpawn(level, level.getCurrentDifficultyAt(entity.blockPosition()), EntitySpawnReason.TRIGGERED, null);
		}
		level.addFreshEntity(entity);
		if (entity instanceof Mob mob) mob.playAmbientSound();
		return entity;
	}
}
