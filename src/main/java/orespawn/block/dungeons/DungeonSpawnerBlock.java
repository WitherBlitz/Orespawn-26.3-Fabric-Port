package orespawn.block.dungeons;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import orespawn.world.dungeons.OreSpawnDungeons;

/**
 * OreSpawn's DungeonSpawnerBlock ("Random Dungeon Spawner"), which the Random Dungeon item plants: a sugar-cane-like
 * plant (no collision, breaks instantly, light 13) that must stand on something solid and gives off firework sparks.
 * Twenty seconds after it's placed, or on a random tick before that, it removes itself and the block above it and
 * builds one of OreSpawn's dungeons there, picked at random (1 in 50 each). Broken or left without support, it drops
 * the Random Dungeon item.
 * Rolls 0 and 1 are OreSpawn's fairy tree and fairy castle tree (OreSpawnTrees), from the Crystal group's port.
 */
public class DungeonSpawnerBlock extends Block {
	/** BlockReed's bounds as OreSpawn set them: 2-14 across, full height. */
	private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

	public DungeonSpawnerBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	/** canBlockStay: the block below is a solid material. */
	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return level.getBlockState(pos.below()).isSolid();
	}

	/** BlockReed.onNeighborBlockChange: dropped (as a Random Dungeon) when it can't stay. */
	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
			BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
		return state.canSurvive(level, pos) ? state : Blocks.AIR.defaultBlockState();
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		for (int i = 0; i < 5; i++) {
			level.addParticle(ParticleTypes.FIREWORK, pos.getX() + random.nextFloat(), pos.getY() + random.nextFloat(), pos.getZ() + random.nextFloat(),
				(random.nextFloat() - random.nextFloat()) / 4.0, random.nextFloat() / 2.0, (random.nextFloat() - random.nextFloat()) / 4.0);
		}
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		if (!level.isClientSide()) {
			level.scheduleTick(pos, this, 400);
		}
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		this.build(level, pos);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		this.build(level, pos);
	}

	private void build(ServerLevel level, BlockPos pos) {
		if (!level.getBlockState(pos).is(this)) {
			return;
		}
		level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		RandomSource random = level.getRandom();
		BlockPos up = pos.above();
		switch (random.nextInt(50)) {
			case 0 -> orespawn.world.crystal.CrystalStructures.fairyTree(level, pos, random);
			case 1 -> orespawn.world.crystal.CrystalStructures.fairyCastleTree(level, pos, random);
			case 2 -> OreSpawnDungeons.enormousCastle(level, pos, random);
			case 3 -> OreSpawnDungeons.rotatorStation(level, pos, random);
			case 4 -> OreSpawnDungeons.beeHive(level, pos, random);
			case 5 -> OreSpawnDungeons.hauntedHouse(level, pos, random);
			case 6 -> OreSpawnDungeons.mantisHive(level, pos, random);
			case 7 -> OreSpawnDungeons.kyuubiDungeon(level, pos, random);
			case 8 -> OreSpawnDungeons.smallBeeHive(level, pos, random);
			case 9 -> OreSpawnDungeons.shadowDungeon(level, pos, random);
			case 10 -> OreSpawnDungeons.alienWTFDungeon(level, pos, random);
			case 11 -> OreSpawnDungeons.enderKnightDungeon(level, pos, random);
			case 12 -> OreSpawnDungeons.playPool(level, pos, random);
			case 13 -> OreSpawnDungeons.waterDragonLair(level, pos, random);
			case 14 -> OreSpawnDungeons.cloudSharkDungeon(level, pos, random);
			case 15 -> OreSpawnDungeons.leafMonsterDungeon(level, pos, random);
			case 16 -> OreSpawnDungeons.miniDungeon(level, pos, random);
			case 17 -> OreSpawnDungeons.goldFishBowl(level, pos, random);
			case 18 -> OreSpawnDungeons.enderReaperGraveyard(level, pos, random);
			case 19 -> OreSpawnDungeons.spitBugLair(level, pos, random);
			case 20 -> OreSpawnDungeons.igloo(level, pos, random);
			case 21 -> OreSpawnDungeons.genericDungeon(level, pos, random);
			case 22 -> OreSpawnDungeons.rubyBirdDungeon(level, pos, random);
			case 23 -> OreSpawnDungeons.basiliskMaze(level, pos, random);
			case 24 -> OreSpawnDungeons.enderDragonHospital(level, pos, random);
			case 25 -> OreSpawnDungeons.crystalHauntedHouse(level, pos, random);
			case 26 -> OreSpawnDungeons.bouncyCastle(level, pos, random);
			case 27 -> OreSpawnDungeons.enderCastle(level, pos, random);
			case 28 -> OreSpawnDungeons.damselInDistress(level, pos, random);
			case 29 -> OreSpawnDungeons.incaPyramid(level, pos, random);
			case 30 -> OreSpawnDungeons.robotLab(level, pos, random);
			case 31 -> OreSpawnDungeons.kingAltar(level, pos, random);
			case 32 -> OreSpawnDungeons.leonNest(level, pos, random);
			case 33 -> OreSpawnDungeons.crystalBattleTower(level, pos, random);
			case 34 -> OreSpawnDungeons.cephadromeAltar(level, pos, random);
			case 35 -> OreSpawnDungeons.girlfriendIsland(level, pos, random);
			case 36 -> OreSpawnDungeons.greenhouseDungeon(level, pos, random);
			case 37 -> OreSpawnDungeons.monsterIsland(level, pos, random);
			case 38 -> OreSpawnDungeons.nightmareRookery(level, pos, random);
			case 39 -> OreSpawnDungeons.stinkyHouse(level, pos, random);
			case 40 -> OreSpawnDungeons.rubberDuckyPond(level, pos, random);
			case 41 -> OreSpawnDungeons.whiteHouse(level, pos, random);
			case 42 -> OreSpawnDungeons.queenAltar(level, pos, random);
			case 43 -> OreSpawnDungeons.frogPond(level, up, random);
			case 44 -> OreSpawnDungeons.pumpkin(level, up, random);
			case 45 -> OreSpawnDungeons.roundRotator(level, up, random);
			case 46 -> OreSpawnDungeons.rainbow(level, pos, random);
			case 47 -> OreSpawnDungeons.enormousCastleQ(level, pos, random);
			case 48 -> OreSpawnDungeons.spiderHangout(level, pos, random);
			case 49 -> OreSpawnDungeons.redAntHangout(level, pos, random);
			default -> {
				// 0 and 1: the fairy trees (see above).
			}
		}
	}
}
