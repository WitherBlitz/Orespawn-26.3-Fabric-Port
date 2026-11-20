package orespawn.block.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import orespawn.content.Farming;
import orespawn.registry.ModBlocks;
import orespawn.registry.ModItems;

import java.util.List;

/**
 * OreSpawn's giant trees (ItemMagicApple's MakeBigSquareTree, MakeBigCircularTree and MakeBigRoundTree), as the Magic
 * Apple grows them. Kept public so OreSpawn's Utopia dimension can use the same trees when it's ported.
 * <ul>
 * <li>Square trees: a hollow square trunk narrowing as it rises, with a stair of steps winding around it, floors with
 * a chest now and then, and long leafy branches (with chests and iron golems on the big ones).</li>
 * <li>Circular trees: a round trunk with spiralling steps, floors every 6 blocks, curling branches, a diamond block on
 * top.</li>
 * <li>Round trees: a round trunk with flat round branches, a diamond block on top.</li>
 * </ul>
 * Wood type ({@code treeType} 0-3: oak, spruce, birch, jungle; jungle trees grow vines) is the log and leaves'
 * variant; -1 means the given blocks are used as they are (the gold/emerald and obsidian/ruby trees). The trunk
 * replaces anything but stone and bedrock; the rest only fills air, grass, flowers, snow, leaves and the like.
 * Vanilla leaves are set persistent: 1.7.10's leaves only checked for decay after a nearby block changed, so the tree
 * stayed whole until cut. Not ported yet: The King or The Queen guarding the top of the gold and obsidian trees.
 */
public final class MagicTree {
	private record Loot(Item item, int min, int max, int weight) {}

	private final ServerLevel level;
	private final RandomSource rand;
	private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

	public MagicTree(ServerLevel level, RandomSource random) {
		this.level = level;
		this.rand = random;
	}

	/** Log of a 1.7.10 wood type (0 oak, 1 spruce, 2 birch, 3 jungle). */
	public static BlockState log(int type) {
		return switch (type) {
			case 1 -> Blocks.SPRUCE_LOG.defaultBlockState();
			case 2 -> Blocks.BIRCH_LOG.defaultBlockState();
			case 3 -> Blocks.JUNGLE_LOG.defaultBlockState();
			default -> Blocks.OAK_LOG.defaultBlockState();
		};
	}

	/** Persistent leaves of a 1.7.10 wood type. */
	public static BlockState leaves(int type) {
		Block block = switch (type) {
			case 1 -> Blocks.SPRUCE_LEAVES;
			case 2 -> Blocks.BIRCH_LEAVES;
			case 3 -> Blocks.JUNGLE_LEAVES;
			default -> Blocks.OAK_LEAVES;
		};
		return block.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
	}

	/** ItemMagicApple.onItemUse's choice of tree, grown on {@code at} (radius 6). */
	public void growRandom(BlockPos at) {
		int x = at.getX(), y = at.getY(), z = at.getZ();
		int treeType = rand.nextInt(4);
		BlockState leaf = leaves(treeType);
		boolean noCritters = rand.nextInt(2) != 1;
		int kind = rand.nextInt(100);
		if (kind >= 40) {
			if (treeType != 3 && rand.nextInt(10) == 1) leaf = Farming.APPLE_LEAVES.defaultBlockState();
			squareTree(x, y, z, log(treeType), leaf, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), treeType, 6, noCritters);
		} else if (kind >= 20) {
			roundTree(x, y, z, log(treeType), leaf, treeType, 6);
		} else if (kind == 1) {
			if (rand.nextInt(2) == 0) {
				squareTree(x, y, z, Blocks.GOLD_BLOCK.defaultBlockState(), Blocks.EMERALD_BLOCK.defaultBlockState(),
					Blocks.DIAMOND_BLOCK.defaultBlockState(), -1, 6, true);
			} else {
				squareTree(x, y, z, Blocks.OBSIDIAN.defaultBlockState(), ModBlocks.RUBY_BLOCK.defaultBlockState(),
					ModBlocks.AMETHYST_BLOCK.defaultBlockState(), -1, 6, true);
			}
		} else {
			circularTree(x, y, z, log(treeType), leaf, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), treeType, 6, noCritters);
		}
	}

	// ---- What may be built over ----

	/** isBoringBlock: air and small plants, snow, leaves. */
	private boolean boring(int x, int y, int z) {
		BlockState s = level.getBlockState(pos.set(x, y, z));
		return s.isAir() || s.is(Blocks.SHORT_GRASS) || s.is(Blocks.FERN) || s.is(Blocks.CACTUS) || s.is(BlockTags.SMALL_FLOWERS)
			|| s.is(Blocks.OAK_LEAVES) || s.is(Blocks.SPRUCE_LEAVES) || s.is(Blocks.BIRCH_LEAVES) || s.is(Blocks.JUNGLE_LEAVES)
			|| s.is(Blocks.SNOW) || s.is(Farming.STRAWBERRY_PLANT) || s.is(Farming.APPLE_LEAVES);
	}

	/** isBoringBaseBlock: anything but stone and bedrock. */
	private boolean boringBase(int x, int y, int z) {
		BlockState s = level.getBlockState(pos.set(x, y, z));
		return !s.is(Blocks.STONE) && !s.is(Blocks.BEDROCK);
	}

	private boolean air(int x, int y, int z) {
		return level.getBlockState(pos.set(x, y, z)).isAir();
	}

	private void set(int x, int y, int z, BlockState state) {
		if (level.isInsideBuildHeight(y)) level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
	}

	/** The foundation under the trunk: down from y while it may build, at most 20 blocks. */
	private void column(int x, int y, int z, BlockState id) {
		if (!boringBase(x, y, z)) return;
		for (int j = 0; j < 20; j++) {
			if (y - j > 0) {
				if (!boringBase(x, y - j, z)) break;
				set(x, y - j, z, id);
			}
		}
	}

	// ---- Square trees ----

	public void squareTree(int x, int y, int z, BlockState id, BlockState leaf, BlockState step, int treeType, int radius, boolean noCritters) {
		int height = radius + rand.nextInt(radius);
		int width = radius;
		int baseHeight = radius * 3;
		int last = -1, lastLast = -1;
		for (int i = -radius; i <= radius; i++) {
			column(x + i, y, z - radius, id);
			column(x + i, y, z + radius, id);
			column(x - radius, y, z + i, id);
			column(x + radius, y, z + i, id);
		}
		int cy = y;
		int spiral = -width;
		while (width >= 0) {
			if (width != radius) baseHeight = 0;
			for (int j = 0; j < height + baseHeight; j++) {
				boolean floor = false;
				for (int m = -width; m <= width; m++) {
					if (boringBase(x + m, cy, z - width)) set(x + m, cy, z - width, id);
					if (boringBase(x + m, cy, z + width)) set(x + m, cy, z + width, id);
					if (boringBase(x - width, cy, z + m)) set(x - width, cy, z + m, id);
					if (boringBase(x + width, cy, z + m)) set(x + width, cy, z + m, id);
				}
				if (width != 0 || j < height / 2) {
					int looper = 1;
					if (spiral == 0 && width >= 2 || spiral == width || spiral == width - 1 && j == height + baseHeight - 1) {
						looper++;
						if (spiral != 0 && width >= 3) looper++;
						if (spiral == 0) floor = true;
					}
					for (int k = 0; k < looper; k++) {
						steps(x, cy, z, spiral, width + 1, step);
						if (width >= 3) steps(x, cy, z, spiral, width + 2, step);
						if (looper != 1) spiral++;
					}
					if (floor) {
						for (int m = -width; m <= width; m++) {
							for (int n = -width; n <= width; n++) {
								if (!boring(x + m, cy, z + n)) continue;
								set(x + m, cy, z + n, id);
								if (m == 0 && n == 0 && rand.nextInt(2) == 0 && !noCritters && air(x, cy + 1, z)) {
									chest(x, cy + 1, z, radius - width + rand.nextInt(10));
								}
							}
						}
					}
				}
				if (width != radius) {
					int next = rand.nextInt(4 + width);
					while (next == last || next == lastLast) next = rand.nextInt(4 + width);
					if (next < 4) {
						lastLast = last;
						last = next;
					}
					switch (next) {
						case 0 -> branch(x + width, cy, z, width, 1, 0, id, leaf, treeType, noCritters);
						case 1 -> branch(x - width, cy, z, width, -1, 0, id, leaf, treeType, noCritters);
						case 2 -> branch(x, cy, z + width, width, 0, 1, id, leaf, treeType, noCritters);
						case 3 -> branch(x, cy, z - width, width, 0, -1, id, leaf, treeType, noCritters);
						default -> {
						}
					}
				}
				cy++;
				if (!floor) spiral++;
				if (spiral > width) spiral = -width;
			}
			if (Math.abs(spiral) > --width) spiral = -width;
			height += rand.nextInt(radius);
		}
		if (boringBase(x, cy, z)) {
			set(x, cy, z, Blocks.EMERALD_BLOCK.defaultBlockState());
			set(x, cy + 1, z, Blocks.EMERALD_BLOCK.defaultBlockState());
		}
	}

	private void steps(int x, int cy, int z, int spiral, int d, BlockState step) {
		if (boring(x - spiral, cy, z - d)) set(x - spiral, cy, z - d, step);
		if (boring(x + spiral, cy, z + d)) set(x + spiral, cy, z + d, step);
		if (boring(x - d, cy, z + spiral)) set(x - d, cy, z + spiral, step);
		if (boring(x + d, cy, z - spiral)) set(x + d, cy, z - spiral, step);
	}

	/** make_branch: a tapering branch out along (dirx, dirz) that forks to the sides, leafy toward its end. */
	private void branch(int x, int y, int z, int thisWidth, int dirx, int dirz, BlockState id, BlockState leaf, int treeType, boolean noCritters) {
		int width = thisWidth;
		int lastBranch = 0;
		int side = rand.nextInt(2) == 0 ? -1 : 1;
		int xacc = dirx, zacc = dirz;
		while (width >= 0) {
			int length = thisWidth * 3 + rand.nextInt(thisWidth + 3);
			for (int i = 0; i < length; i++) {
				for (int j = -width; j <= width; j++) {
					int rx = x + j * dirz + xacc;
					int rz = z + j * dirx + zacc;
					if (boring(rx, y, rz)) set(rx, y, rz, id);
					if (i > 0 && j == 0 && width >= 3) {
						if (treeType >= 0 && rand.nextInt(75) == 0 || treeType < 0 && rand.nextInt(50) == 0) {
							if (!noCritters && air(rx, y + 1, rz)) chest(rx, y + 1, rz, 1 + rand.nextInt(8));
						} else if (rand.nextInt(50) == 0 && !noCritters && air(rx, y + 1, rz) && air(rx, y + 2, rz) && air(rx, y + 3, rz)) {
							CritterPlantBlock.spawn(level, Identifier.withDefaultNamespace("iron_golem"), rx + 0.5, y + 1.01, rz + 0.5);
						}
					}
				}
				if (width < 3 || thisWidth <= 1) {
					int depth = 2 + rand.nextInt(2);
					int leafWidth = 2 + rand.nextInt(3);
					for (int n = 0; n < depth; n++) {
						int lw = width + leafWidth - n;
						if (width == 0 && length - i <= 2 && lw >= length - i) lw = length - i - 1;
						if (lw < 0) lw = 0;
						for (int k = -lw; k <= lw; k++) {
							int rx = x + k * Math.abs(dirz) + xacc + dirx;
							int rz = z + k * Math.abs(dirx) + zacc + dirz;
							if (!boring(rx, y + n, rz)) continue;
							if (treeType >= 0) {
								set(rx, y + n, rz, leaf);
								if (n == 0 && treeType == 3 && lw != 0 && (k == lw || k == -lw) && rand.nextInt(5) == 0) {
									if (dirx == 0) {
										if (k == lw) vines(rx + 1, y, rz, VineBlock.WEST, rand.nextInt(10));
										else vines(rx - 1, y, rz, VineBlock.EAST, rand.nextInt(10));
									} else if (k == lw) {
										vines(rx, y, rz + 1, VineBlock.NORTH, rand.nextInt(10));
									} else {
										vines(rx, y, rz - 1, VineBlock.SOUTH, rand.nextInt(10));
									}
								}
							} else {
								BlockState local = leaf;
								if (rand.nextInt(20) == 1) {
									if (rand.nextInt(3) != 0) {
										local = Blocks.REDSTONE_BLOCK.defaultBlockState();
									} else {
										local = switch (rand.nextInt(4)) {
											case 0 -> ModBlocks.URANIUM_BLOCK.defaultBlockState();
											case 1 -> ModBlocks.TITANIUM_BLOCK.defaultBlockState();
											case 2 -> ModBlocks.RUBY_BLOCK.defaultBlockState();
											default -> ModBlocks.AMETHYST_BLOCK.defaultBlockState();
										};
									}
								}
								set(rx, y + n, rz, local);
							}
						}
					}
				}
				if (width > 0 && lastBranch > width && width != thisWidth && rand.nextInt(width + 1) == 0) {
					int subx = side, subz = 0;
					if (dirx != 0) {
						subx = 0;
						subz = side;
					}
					branch(x + xacc + width * subx, y, z + zacc + width * subz, width - 1, subx, subz, id, leaf, treeType, noCritters);
					lastBranch = 0;
					side = side < 0 ? 1 : -1;
				}
				xacc += dirx;
				zacc += dirz;
				lastBranch++;
			}
			width--;
		}
	}

	/** growVines: a vine on the given face, hanging down through air up to {@code length} more blocks. */
	private void vines(int x, int y, int z, BooleanProperty face, int length) {
		if (!air(x, y, z)) return;
		BlockState vine = Blocks.VINE.defaultBlockState().setValue(face, true);
		set(x, y, z, vine);
		while (length > 0) {
			if (!air(x, --y, z)) return;
			set(x, y, z, vine);
			length--;
		}
	}

	// ---- Circular trees ----

	public void circularTree(int x, int y, int z, BlockState id, BlockState leaf, BlockState step, int treeType, int radius, boolean noCritters) {
		double rad = radius;
		int stepIndex = rand.nextInt(360);
		int ibranch = 0;
		for (int i = 0; i < 360; i++) {
			column(x + (int) (rad * Math.sin(Math.toRadians(i)) + 0.5), y, z + (int) (rad * Math.cos(Math.toRadians(i)) + 0.5), id);
		}
		int cy = 1;
		while (rad > 0.0) {
			for (int i = 0; i < 360; i++) {
				int cx = (int) (rad * Math.sin(Math.toRadians(i)) + 0.5);
				int cz = (int) (rad * Math.cos(Math.toRadians(i)) + 0.5);
				if (boringBase(x + cx, y + cy, z + cz)) set(x + cx, y + cy, z + cz, id);
				if (i >= stepIndex - 1 && i <= stepIndex + 1 && rad > 1.0) {
					cx = (int) ((rad + 1.9) * Math.sin(Math.toRadians(i)) + 0.5);
					cz = (int) ((rad + 1.9) * Math.cos(Math.toRadians(i)) + 0.5);
					for (int m = -1; m <= 1; m++) {
						for (int n = -1; n <= 1; n++) {
							if (boringBase(x + cx + m, y + cy, z + cz + n)) set(x + cx + m, y + cy, z + cz + n, step);
						}
					}
				}
			}
			if (cy > (int) rad) {
				ibranch += 80 + rand.nextInt(80);
				if (ibranch > 360) ibranch -= 360;
				int len = (int) (rad * 5.0) + rand.nextInt((int) rad + 2);
				int cx = (int) (rad * Math.sin(Math.toRadians(ibranch)) + 0.5);
				int cz = (int) (rad * Math.cos(Math.toRadians(ibranch)) + 0.5);
				int twist = rand.nextInt(2) * (rand.nextInt(2) == 0 ? -1 : 1);
				circularBranch(ibranch, len, (int) rad + 1, x + cx, y + cy, z + cz, twist, id, leaf);
			}
			if (cy % 6 == 0 && rad > 3.0) {
				disc(x + 0.0, y + cy, z + 0.0, rad, id, true);
				if (rand.nextInt(2) == 0 && !noCritters && air(x, y + cy + 1, z)) chest(x, y + cy + 1, z, radius - (int) rad + rand.nextInt(10));
			}
			stepIndex += 15 + (int) ((radius - rad) * 3.0);
			if (stepIndex > 360) stepIndex -= 360;
			cy++;
			rad -= 0.01 * rand.nextInt(15);
			if (rad <= 0.0 && boringBase(x, y + cy, z)) set(x, y + cy, z, Blocks.DIAMOND_BLOCK.defaultBlockState());
		}
	}

	/** A filled disc of trunk every 6 blocks (rounded with +0.5 for circular trees, from the block centre for round ones). */
	private void disc(double cx, int y, double cz, double rad, BlockState id, boolean circular) {
		for (double dr = rad - 0.25; dr > 0.0; dr -= 0.25) {
			for (int i = 0; i < 360; i++) {
				int px, pz;
				if (circular) {
					px = (int) cx + (int) (dr * Math.sin(Math.toRadians(i)) + 0.5);
					pz = (int) cz + (int) (dr * Math.cos(Math.toRadians(i)) + 0.5);
				} else {
					px = (int) ((float) cx + (float) (dr * Math.sin(Math.toRadians(i))));
					pz = (int) ((float) cz + (float) (dr * Math.cos(Math.toRadians(i))));
				}
				if (boringBase(px, y, pz)) set(px, y, pz, id);
			}
		}
	}

	/** MakeCirclularBranch: a curling branch, trunk in its middle and leaves along its sides and on top. */
	private void circularBranch(int angle, int length, int width, int sx, int sy, int sz, int twist, BlockState id, BlockState leaf) {
		int cur = angle;
		double cx = sx, cz = sz;
		for (double l = 0.0; l < length; l += 0.5) {
			cx += 0.5 * Math.sin(Math.toRadians(cur));
			cz += 0.5 * Math.cos(Math.toRadians(cur));
			double tw = width - width * l / length;
			for (double wd = 0.0; wd <= tw; wd += 0.5) {
				BlockState block = wd < tw / 2.0 && !(tw < 0.9) ? id : leaf;
				int ta = cur + 90;
				if (ta > 360) ta -= 360;
				branchSide(cx + wd * Math.sin(Math.toRadians(ta)), sy, cz + wd * Math.cos(Math.toRadians(ta)), block, id, leaf);
				ta = cur - 90;
				if (ta < 0) ta += 360;
				branchSide(cx + wd * Math.sin(Math.toRadians(ta)), sy, cz + wd * Math.cos(Math.toRadians(ta)), block, id, leaf);
			}
			cur += twist;
			if (cur < 0) cur += 360;
			if (cur >= 360) cur -= 360;
		}
	}

	private void branchSide(double wx, int y, double wz, BlockState block, BlockState id, BlockState leaf) {
		int x = (int) wx, z = (int) wz;
		if (boring(x, y, z)) set(x, y, z, block);
		if (block == id && boring(x, y + 1, z)) set(x, y + 1, z, leaf);
	}

	// ---- Round trees ----

	public void roundTree(int inx, int y, int inz, BlockState id, BlockState leaf, int treeType, int radius) {
		double rad = radius;
		int ibranch = 0;
		float fx = inx + 0.5F;
		float fz = inz + 0.5F;
		for (int i = 0; i < 360; i++) {
			column((int) (fx + (float) (rad * Math.sin(Math.toRadians(i)))), y, (int) (fz + (float) (rad * Math.cos(Math.toRadians(i)))), id);
		}
		int cy = 1;
		while (rad > 0.0) {
			for (int i = 0; i < 360; i++) {
				int px = (int) (fx + (float) (rad * Math.sin(Math.toRadians(i))));
				int pz = (int) (fz + (float) (rad * Math.cos(Math.toRadians(i))));
				if (boringBase(px, y + cy, pz)) set(px, y + cy, pz, id);
			}
			if (cy > (int) rad) {
				ibranch += 80 + rand.nextInt(80);
				if (ibranch > 360) ibranch -= 360;
				int len = (int) (rad * 5.0) + rand.nextInt((int) rad + 2);
				float cx = (float) (rad * Math.sin(Math.toRadians(ibranch)));
				float cz = (float) (rad * Math.cos(Math.toRadians(ibranch)));
				roundBranch(ibranch, len, fx + cx, y + cy, fz + cz, id, leaf);
			}
			if (cy % 6 == 0 && rad > 3.0) disc(fx, y + cy, fz, rad, id, false);
			cy++;
			rad -= 0.01 * rand.nextInt(15);
			if (rad <= 0.0 && boringBase((int) fx, y + cy, (int) fz)) set((int) fx, y + cy, (int) fz, Blocks.DIAMOND_BLOCK.defaultBlockState());
		}
	}

	/** MakeRoundBranch: a flat disc out from the trunk, wood with a 2-block rim of leaves. */
	private void roundBranch(int angle, int length, float sx, int sy, float sz, BlockState id, BlockState leaf) {
		double deltaDir = 0.06283185200000001;
		double deltaMag = 0.35F;
		int radius = length / 2;
		float centerX = (float) (sx + radius * Math.sin(Math.toRadians(angle)));
		float centerZ = (float) (sz + radius * Math.cos(Math.toRadians(angle)));
		int lastX = 0, lastZ = 0;
		for (double dir = -3.1415926; dir < 3.1415926; dir += deltaDir) {
			for (double h = 0.75; h < radius; h += deltaMag) {
				int ix = (int) (centerX + Math.cos(dir) * h);
				int iz = (int) (centerZ + Math.sin(dir) * h);
				if (ix == lastX && iz == lastZ) continue;
				lastX = ix;
				lastZ = iz;
				if (boring(ix, sy, iz)) set(ix, sy, iz, radius - h < 2.0 ? leaf : id);
			}
		}
	}

	// ---- Chests ----

	/** ItemMagicApple.chestContentsList (WeightedRandomChestContent: min-max of the item, by weight). */
	private static List<Loot> chestLoot() {
		return List.of(new Loot(Items.ENDER_PEARL, 1, 2, 3), new Loot(Items.DIAMOND, 1, 5, 15), new Loot(Items.GHAST_TEAR, 1, 3, 10),
			new Loot(ModItems.lazy("empty_critter_cage"), 1, 10, 7), new Loot(ModItems.lazy("caged_girlfriend"), 1, 2, 6),
			new Loot(Items.IRON_INGOT, 1, 10, 16), new Loot(Items.GOLD_INGOT, 1, 6, 16), new Loot(ModItems.URANIUM_NUGGET, 1, 6, 6),
			new Loot(ModItems.TITANIUM_NUGGET, 1, 4, 6), new Loot(Items.BREAD, 1, 8, 20), new Loot(Items.APPLE, 1, 8, 20),
			new Loot(Items.COOKIE, 1, 16, 20), new Loot(Items.COOKED_BEEF, 1, 8, 20), new Loot(Items.COOKED_CHICKEN, 1, 8, 20),
			new Loot(Items.COOKED_COD, 1, 8, 20), new Loot(Items.COOKED_PORKCHOP, 1, 8, 20), new Loot(Items.PUMPKIN_PIE, 1, 4, 20),
			new Loot(Items.CARROT, 1, 16, 20), new Loot(Items.POTATO, 1, 16, 20), new Loot(Farming.SUN_FISH, 1, 4, 6),
			new Loot(Farming.FIRE_FISH, 1, 8, 6), new Loot(Farming.BAG_OF_POPCORN, 1, 4, 16), new Loot(Items.IRON_PICKAXE, 1, 1, 20),
			new Loot(Items.IRON_SWORD, 1, 1, 20), new Loot(Items.DIAMOND_PICKAXE, 1, 1, 5), new Loot(Items.DIAMOND_SWORD, 1, 1, 5),
			new Loot(Items.BOW, 1, 1, 20), new Loot(Items.ARROW, 1, 64, 20), new Loot(ModItems.lazy("ultimate_pickaxe"), 1, 1, 2),
			new Loot(ModItems.lazy("ultimate_sword"), 1, 1, 1), new Loot(ModItems.lazy("the_ultimate_fishing_rod"), 1, 1, 5),
			new Loot(Items.IRON_CHESTPLATE, 1, 1, 20), new Loot(Items.IRON_HELMET, 1, 1, 20), new Loot(Items.IRON_LEGGINGS, 1, 1, 20),
			new Loot(Items.IRON_BOOTS, 1, 1, 20), new Loot(Items.DIAMOND_CHESTPLATE, 1, 1, 5), new Loot(Items.DIAMOND_HELMET, 1, 1, 5),
			new Loot(Items.DIAMOND_LEGGINGS, 1, 1, 5), new Loot(Items.DIAMOND_BOOTS, 1, 1, 5), new Loot(Items.GOLDEN_APPLE, 1, 1, 5));
	}

	/** A chest with {@code count} picks from the list, each into a random slot (items not ported yet are skipped). */
	private void chest(int x, int y, int z, int count) {
		BlockPos at = new BlockPos(x, y, z);
		if (!level.isInsideBuildHeight(y)) return;
		level.setBlock(at, Blocks.CHEST.defaultBlockState(), Block.UPDATE_CLIENTS);
		if (!(level.getBlockEntity(at) instanceof ChestBlockEntity chest)) return;
		List<Loot> loot = chestLoot();
		int total = loot.stream().mapToInt(Loot::weight).sum();
		for (int i = 0; i < count; i++) {
			int pick = rand.nextInt(total);
			Loot entry = loot.getLast();
			for (Loot l : loot) {
				pick -= l.weight();
				if (pick < 0) {
					entry = l;
					break;
				}
			}
			if (entry.item() == Items.AIR) continue;
			int n = entry.min() + rand.nextInt(entry.max() - entry.min() + 1);
			int stackMax = new ItemStack(entry.item()).getMaxStackSize();
			if (n > stackMax) {
				for (int k = 0; k < n; k++) chest.setItem(rand.nextInt(chest.getContainerSize()), new ItemStack(entry.item()));
			} else {
				chest.setItem(rand.nextInt(chest.getContainerSize()), new ItemStack(entry.item(), n));
			}
		}
	}
}
