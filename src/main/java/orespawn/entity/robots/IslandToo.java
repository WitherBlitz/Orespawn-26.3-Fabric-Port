package orespawn.entity.robots;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import orespawn.registry.ModBlocks;

/**
 * OreSpawn's IslandToo, the Dark Floating Island: the hidden heart of a square floating island, which an Island Block
 * grows (24 times in 25). On its first tick it builds an upside-down stepped pyramid of stone above itself, 1-10 blocks
 * across and 1-4 deep (1 in 40: 5-20 and 3-8, and slower), seamed with one kind of ore (coal, iron, emerald, titanium,
 * uranium, ruby, amethyst or gold) and the rare block of something precious, topped with grass, flowers and little
 * trees (water 1 block in 5000). It drifts north, south, east or west, a block at a time (checked every 42 ticks),
 * moving its trailing edge to its leading edge and blasting through end stone in its way, and changes direction now
 * and then. If it ever meets bedrock it stops for good. Drops 0-2 Island Blocks if killed (more with Looting).
 * <p>
 * Not ported yet: the pink and blue flowers, the Sky Tree logs and Apple Leaves of its little trees, and its Ender
 * Pearl and Eye of Ender blocks; each appears once its group registers it (orespawn:flower_pink, flower_blue,
 * sky_tree_log, apple_leaves, ender_pearl_block, eye_of_ender_block), and is left out until then.
 */
public class IslandToo extends Mob {
	static final int SIZE_FACTOR = 2;
	private int dir;
	private float speed = 0.1F;
	private int width = 5;
	private int depth = 3;
	private int length = 10;
	private final int timer = 42;
	private boolean justSpawned = true;
	private int ticker;
	private boolean once = true;
	private double myX, myZ;
	private int dirchange;
	private int blocktype;

	public IslandToo(EntityType<? extends IslandToo> type, Level level) {
		super(type, level);
		ticker = random.nextInt(50);
		dirchange = random.nextInt(5000);
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

	/** OreSpawn snapped it back to its block's centre when hit. */
	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		setPos((int) getX() + (getX() < 0.0 ? -0.5 : 0.5), getY(), (int) getZ() + (getZ() < 0.0 ? -0.5 : 0.5));
		super.hurtServer(level, source, amount);
		return false;
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
			dir = random.nextInt(4);
			if (random.nextInt(40) != 1) {
				width = 1 + random.nextInt(5 * SIZE_FACTOR);
				length = width;
				depth = 1 + random.nextInt(4);
				speed = random.nextFloat() / 40.0F * Island.SPEED_FACTOR;
				if (length * width * depth <= 64) speed *= 2.0F;
				if (length * width * depth <= 32) speed *= 2.0F;
			} else {
				width = 5 + random.nextInt(8 * SIZE_FACTOR);
				length = width;
				depth = 3 + random.nextInt(6);
				speed = random.nextFloat() / 150.0F * Island.SPEED_FACTOR;
			}
			if (!create(level)) return;
			ticker = random.nextInt(50);
			dirchange = random.nextInt(10000);
		}
		if (++ticker >= timer) {
			move(level);
			ticker = 0;
		}
		if (--dirchange <= 0) {
			dirchange = random.nextInt(5000);
			dir = random.nextInt(4);
		}
		justSpawned = false;
	}

	private int cx() {
		return Mth.floor(getX());
	}

	private int cz() {
		return Mth.floor(getZ());
	}

	private int cy() {
		return Mth.floor(getY());
	}

	private int layerHalf(int k) {
		return Math.max(1, length / (depth - k + 1));
	}

	private boolean create(ServerLevel level) {
		for (int k = 0; k <= depth; k++) {
			int il = layerHalf(k);
			for (int i = -il; i <= il; i++) {
				for (int j = -il; j <= il; j++) {
					BlockPos pos = new BlockPos(cx() + j, cy() + k, cz() + i);
					if (k == depth) {
						if (!top(level, pos)) return false;
					} else {
						setOre(level, pos);
					}
				}
			}
		}
		Island.setBlock(level, new BlockPos(cx(), cy(), cz()), Blocks.AIR);
		return true;
	}

	/** The top: grass with the odd flower or little tree (water 1 in 5000). False if it hit bedrock and stopped. */
	private boolean top(ServerLevel level, BlockPos pos) {
		BlockState s = level.getBlockState(pos);
		if (s.isAir()) {
			if (random.nextInt(5000) == 1) {
				Island.setBlock(level, pos, Blocks.WATER);
			} else {
				Island.setBlock(level, pos, Blocks.GRASS_BLOCK);
				if (random.nextInt(30) == 1) {
					if (level.getBlockState(pos.above()).isAir()) {
						Block flower = RobotUtil.lazyBlock(random.nextInt(2) == 1 ? "pink_flower" : "blue_flower");
						if (flower != null) Island.setBlock(level, pos.above(), flower);
					}
				} else if (random.nextInt(100) == 1 && level.getBlockState(pos.above()).isAir()) {
					smallTree(level, pos.above());
				}
			}
		} else if (s.is(Blocks.BEDROCK)) {
			discard();
			return false;
		}
		return true;
	}

	/** Trees.SmallTree: a 1-3 block Sky Tree trunk with a little crown of Apple Leaves. */
	private void smallTree(ServerLevel level, BlockPos pos) {
		Block log = RobotUtil.lazyBlock("sky_tree_wood");
		Block leaves = RobotUtil.lazyBlock("apple_leaves");
		if (log == null || leaves == null) return;
		BlockState below = level.getBlockState(pos.below());
		if (!below.is(Blocks.GRASS_BLOCK) && !below.is(Blocks.DIRT) && !below.is(Blocks.FARMLAND)) return;
		int x = pos.getX(), z = pos.getZ();
		int realy = pos.getY() - 1;
		if (level.getBlockState(new BlockPos(x, realy + 1, z)).isAir()) Island.setBlock(level, new BlockPos(x, realy + 1, z), log);
		if (random.nextInt(2) == 1) {
			if (level.getBlockState(new BlockPos(x, realy + 2, z)).isAir()) Island.setBlock(level, new BlockPos(x, realy + 2, z), log);
			if (random.nextInt(2) == 1) {
				if (level.getBlockState(new BlockPos(x, realy + 3, z)).isAir()) Island.setBlock(level, new BlockPos(x, realy + 3, z), log);
			} else {
				realy--;
			}
		} else {
			realy -= 2;
		}
		if (level.getBlockState(new BlockPos(x, realy + 4, z)).isAir()) Island.setBlock(level, new BlockPos(x, realy + 4, z), leaves);
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				BlockPos p = new BlockPos(x + i, realy + 3, z + j);
				if (level.getBlockState(p).isAir()) Island.setBlock(level, p, leaves);
			}
		}
	}

	/** mySetBlock: stone, with this island's one kind of ore mixed in, and 1 in 3000 a precious block. */
	private void setOre(ServerLevel level, BlockPos pos) {
		Block b = Blocks.STONE;
		if (blocktype == 0) blocktype = 1 + random.nextInt(8);
		if (blocktype == 1 && random.nextInt(5) == 1) b = Blocks.COAL_ORE;
		if (blocktype == 2 && random.nextInt(10) == 1) b = Blocks.IRON_ORE;
		if (blocktype == 3 && random.nextInt(20) == 1) b = Blocks.EMERALD_ORE;
		if (blocktype == 4 && random.nextInt(30) == 1) b = ModBlocks.TITANIUM_ORE;
		if (blocktype == 5 && random.nextInt(30) == 1) b = ModBlocks.URANIUM_ORE;
		if (blocktype == 6 && random.nextInt(30) == 1) b = ModBlocks.RUBY_ORE;
		if (blocktype == 7 && random.nextInt(30) == 1) b = ModBlocks.AMETHYST_ORE;
		if (blocktype == 8 && random.nextInt(20) == 1) b = Blocks.GOLD_ORE;
		if (b == Blocks.STONE) {
			Block[] rare = {RobotUtil.lazyBlock("ender_pearl_block"), RobotUtil.lazyBlock("eye_of_ender_block"), ModBlocks.AMETHYST_BLOCK,
				ModBlocks.RUBY_BLOCK, ModBlocks.URANIUM_BLOCK, ModBlocks.TITANIUM_BLOCK, Blocks.GOLD_BLOCK, Blocks.DIAMOND_BLOCK};
			for (int i = 0; i < rare.length; i++) {
				if (random.nextInt(3000) == i + 1 && rare[i] != null) b = rare[i];
			}
		}
		Island.setBlock(level, pos, b);
	}

	private boolean isFlower(BlockState s) {
		for (String id : new String[] {"pink_flower", "blue_flower", "black_flower", "dead_flower"}) {
			Block f = RobotUtil.lazyBlock(id);
			if (f != null && s.is(f)) return true;
		}
		return false;
	}

	/** update_island: drift; on crossing into a new block, clear the trailing edge and build the leading one. */
	private void move(ServerLevel level) {
		switch (dir) {
			case 0 -> myZ -= speed;
			case 1 -> myZ += speed;
			case 2 -> myX += speed;
			default -> myX -= speed;
		}
		int mx = (int) myX, mz = (int) myZ;
		if (mx == (int) getX() && mz == (int) getZ()) return;
		// The trailing edge: j runs along z, k along x.
		int[] t = switch (dir) {
			case 0 -> new int[] {1, 1, -1, 1};
			case 1 -> new int[] {-1, -1, -1, 1};
			case 2 -> new int[] {-1, 1, -1, -1};
			default -> new int[] {-1, 1, 1, 1};
		};
		Block log = RobotUtil.lazyBlock("sky_tree_wood");
		for (int i = 0; i <= depth; i++) {
			int il = layerHalf(i);
			for (int j = t[0] * il; j <= t[1] * il; j++) {
				for (int k = t[2] * il; k <= t[3] * il; k++) {
					int ix = cx() + k, iz = cz() + j;
					if (i == depth) {
						BlockPos above = new BlockPos(ix, cy() + i + 1, iz);
						BlockState s = level.getBlockState(above);
						if (isFlower(s)) Island.setBlock(level, above, Blocks.AIR);
						if (s.is(Blocks.WATER)) Island.setBlock(level, new BlockPos(ix, cy() + i, iz), Blocks.AIR);
						if (log != null && s.is(log)) {
							for (int up = 1; up <= 3 && level.getBlockState(new BlockPos(ix, cy() + i + up, iz)).is(log); up++) {
								Island.setBlock(level, new BlockPos(ix, cy() + i + up, iz), Blocks.AIR);
							}
						}
						if (level.getBlockState(new BlockPos(ix, cy() + i, iz)).is(Blocks.WATER)) {
							Island.setBlock(level, new BlockPos(ix, cy() + i, iz), Blocks.AIR);
						}
					}
					Island.setBlock(level, new BlockPos(ix, cy() + i, iz), Blocks.AIR);
				}
			}
		}
		setOre(level, new BlockPos(cx(), cy(), cz()));
		setPos(mx + (myX < 0.0 ? -0.5 : 0.5), getY(), mz + (myZ < 0.0 ? -0.5 : 0.5));
		// The leading edge.
		int[] l = switch (dir) {
			case 0 -> new int[] {-1, -1, -1, 1};
			case 1 -> new int[] {1, 1, -1, 1};
			case 2 -> new int[] {-1, 1, 1, 1};
			default -> new int[] {-1, 1, -1, -1};
		};
		Island.setBlock(level, new BlockPos(cx(), cy(), cz()), Blocks.AIR);
		for (int i = 0; i <= depth; i++) {
			int il = layerHalf(i);
			for (int j = l[0] * il; j <= l[1] * il; j++) {
				for (int k = l[2] * il; k <= l[3] * il; k++) {
					BlockPos pos = new BlockPos(cx() + k, cy() + i, cz() + j);
					if (i == depth) {
						if (!top(level, pos)) return;
					} else if (level.getBlockState(pos).is(Blocks.END_STONE)) {
						level.explode(this, pos.getX(), getY() + i, pos.getZ(), 5.0F, Level.ExplosionInteraction.BLOCK);
					} else {
						setOre(level, pos);
					}
				}
			}
		}
		Island.setBlock(level, new BlockPos(cx(), cy(), cz()), Blocks.AIR);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		Island.dropIslandBlocks(level, this, source);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("JustSpawned", justSpawned ? 1 : 0);
		output.putInt("Iwidth", width);
		output.putInt("Idepth", depth);
		output.putInt("Ilength", length);
		output.putFloat("Ispeed", speed);
		output.putInt("Idir", dir);
		output.putInt("Iblocktype", blocktype);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		justSpawned = input.getIntOr("JustSpawned", 0) != 0;
		width = input.getIntOr("Iwidth", 5);
		depth = input.getIntOr("Idepth", 3);
		length = input.getIntOr("Ilength", 10);
		speed = input.getFloatOr("Ispeed", 0.1F);
		dir = input.getIntOr("Idir", 0);
		blocktype = input.getIntOr("Iblocktype", 0);
	}
}
