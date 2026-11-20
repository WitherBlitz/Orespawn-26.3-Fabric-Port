package orespawn.world.utopia;

import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import orespawn.registry.ModBlocks;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * The trees OreSpawn grew from its items, which Utopia also generates: {@code ItemMagicApple}'s huge square, circular
 * and round trees (with their spiral staircases, chests, iron golems and, on the gold-and-emerald and
 * obsidian-and-ruby trees, The King or The Queen on top), and {@code ItemAppleSeed}'s apple, cherry and peach trees.
 * Ported line for line on a {@link GenWorld}; every random draw (OreSpawn mixed its own OreSpawnRand and world.rand) is
 * the world's random.
 */
public final class HugeTrees {
	private HugeTrees() {
	}

	/** A block chosen by OreSpawn's metadata (tree_type 0-3: oak, spruce, birch, jungle; -1 for meta 0). */
	public record Mat(BlockState... byMeta) {
		BlockState get(int treeType) {
			return this.byMeta[treeType < 0 ? 0 : treeType % this.byMeta.length];
		}

		Block block() {
			return this.byMeta[0].getBlock();
		}
	}

	public static final Mat LOGS = new Mat(Blocks.OAK_LOG.defaultBlockState(), Blocks.SPRUCE_LOG.defaultBlockState(),
		Blocks.BIRCH_LOG.defaultBlockState(), Blocks.JUNGLE_LOG.defaultBlockState());
	public static final Mat LEAVES = new Mat(Blocks.OAK_LEAVES.defaultBlockState(), Blocks.SPRUCE_LEAVES.defaultBlockState(),
		Blocks.BIRCH_LEAVES.defaultBlockState(), Blocks.JUNGLE_LEAVES.defaultBlockState());

	/** Who guards the top of a square tree: The King (diamond steps) or The Queen (amethyst steps). */
	public enum Guard { NONE, KING, QUEEN }

	/** ItemAppleSeed's three trees. */
	public enum Fruit { APPLE, CHERRY, PEACH }

	// ---- ItemAppleSeed ----------------------------------------------------------------------------------------------

	/** ItemAppleSeed.makeTree: an oak trunk with two crosses of branches and a stepped crown of fruit leaves. */
	public static void fruitTree(GenWorld w, int x, int y, int z, Fruit fruit) {
		Block b = w.block(x, y, z);
		if (b != Blocks.GRASS_BLOCK && b != Blocks.DIRT && b != Blocks.FARMLAND) {
			return;
		}
		BlockState leaves = switch (fruit) {
			case APPLE -> UtopiaBlocks.appleLeaves();
			case CHERRY -> UtopiaBlocks.cherryLeaves();
			case PEACH -> UtopiaBlocks.peachLeaves();
		};
		int h1 = 12;
		int h2 = 6;
		int h3 = 9;
		int h4 = 6;
		int h5 = 14;
		int w1 = 5;
		int w2 = 3;
		if (fruit == Fruit.PEACH) {
			h1 = 10;
			h2 = 5;
			h3 = 7;
			h4 = 5;
			h5 = 12;
			w1 = 4;
			w2 = 2;
		}
		if (fruit == Fruit.CHERRY) {
			h1 = 8;
			h2 = 3;
			h3 = 5;
			h4 = 3;
			h5 = 10;
			w1 = 3;
			w2 = 1;
		}
		BlockState log = Blocks.OAK_LOG.defaultBlockState();
		for (int j = 1; j < h1; j++) {
			w.set(x, y + j, z, log);
		}
		for (int i = 1; i < w1; i++) {
			w.set(x + i, y + h2, z, log);
			w.set(x - i, y + h2, z, log);
			w.set(x, y + h2, z + i, log);
			w.set(x, y + h2, z - i, log);
		}
		for (int i = 1; i < w2; i++) {
			w.set(x + i, y + h3, z, log);
			w.set(x - i, y + h3, z, log);
			w.set(x, y + h3, z + i, log);
			w.set(x, y + h3, z - i, log);
		}
		for (int i = h4; i < h5; i++) {
			int width = 6;
			if (i > 8) {
				width = 5;
			}
			if (i > 10) {
				width = 4;
			}
			if (fruit != Fruit.APPLE) {
				width--;
			}
			for (int a = -width; a <= width; a++) {
				for (int k = -width; k <= width; k++) {
					if (w.isAir(x + k, y + i, z + a)) {
						w.set(x + k, y + i, z + a, leaves);
					}
				}
			}
		}
	}

	// ---- ItemMagicApple ---------------------------------------------------------------------------------------------

	/** isBoringBlock: what a huge tree may grow through. */
	private static boolean boring(GenWorld w, int x, int y, int z) {
		BlockState s = w.get(x, y, z);
		if (s.isAir()) {
			return true;
		}
		Block b = s.getBlock();
		return b == Blocks.SHORT_GRASS || b == Blocks.FERN || b == Blocks.CACTUS || b instanceof FlowerBlock || b == Blocks.OAK_LEAVES
			|| b == Blocks.SPRUCE_LEAVES || b == Blocks.BIRCH_LEAVES || b == Blocks.JUNGLE_LEAVES || b == Blocks.SNOW_BLOCK
			|| b == Farm.APPLE_LEAVES || b == Farm.STRAWBERRY;
	}

	/** The Farming group's blocks the trees grow through, looked up once (worldgen only runs after every group registered). */
	private static final class Farm {
		static final Block APPLE_LEAVES = UtopiaBlocks.appleLeaves().getBlock();
		static final @Nullable Block STRAWBERRY = Lookup.block(UtopiaFeature.STRAWBERRY_PLANT);
	}

	/** isBoringBaseBlock: everything but stone and bedrock. */
	private static boolean boringBase(GenWorld w, int x, int y, int z) {
		BlockState s = w.get(x, y, z);
		return s.isAir() || !s.is(Blocks.STONE) && !s.is(Blocks.BEDROCK);
	}

	private static void growVines(GenWorld w, int x, int y, int z, BooleanProperty face, int len) {
		if (!w.isAir(x, y, z)) {
			return;
		}
		BlockState vine = Blocks.VINE.defaultBlockState().setValue(face, true);
		w.set(x, y, z, vine);
		while (len > 0) {
			if (!w.isAir(x, --y, z)) {
				return;
			}
			w.set(x, y, z, vine);
			len--;
		}
	}

	private static void makeBranch(GenWorld w, int x, int y, int z, int thisWidth, int dirx, int dirz, Mat id, Mat leaf, int treeType,
			boolean badCritters) {
		RandomSource rand = w.random();
		int currentWidth = thisWidth;
		int lastBranch = 0;
		int branchSide = 1;
		int xaccum = dirx;
		int zaccum = dirz;
		if (rand.nextInt(2) == 0) {
			branchSide = -1;
		}
		while (currentWidth >= 0) {
			int length = thisWidth * 3 + rand.nextInt(thisWidth + 3);
			for (int i = 0; i < length; i++) {
				for (int j = -currentWidth; j <= currentWidth; j++) {
					int realx = x + j * dirz + xaccum;
					int realz = z + j * dirx + zaccum;
					if (boring(w, realx, y, realz)) {
						w.set(realx, y, realz, id.get(treeType));
					}
					if (i > 0 && j == 0 && currentWidth >= 3) {
						if (treeType >= 0 && rand.nextInt(75) == 0 || treeType < 0 && rand.nextInt(50) == 0) {
							if (!badCritters && w.isAir(realx, y + 1, realz)) {
								w.chest(realx, y + 1, realz, Direction.SOUTH, ChestLoot.fill(rand, ChestLoot.MAGIC_APPLE, 1 + rand.nextInt(8)));
							}
						} else if (rand.nextInt(50) == 0 && !badCritters && w.isAir(realx, y + 1, realz) && w.isAir(realx, y + 2, realz)
							&& w.isAir(realx, y + 3, realz)) {
							// spawnCreature(world, 99, ...): entity id 99 is the iron golem
							w.spawn(EntityTypes.IRON_GOLEM, realx + 0.5, y + 1.01, realz + 0.5, rand.nextFloat() * 360.0F, null);
						}
					}
				}
				if (currentWidth < 3 || thisWidth <= 1) {
					int leafDepth = 2 + rand.nextInt(2);
					int leafWidth = 2 + rand.nextInt(3);
					for (int n = 0; n < leafDepth; n++) {
						int lw = currentWidth + leafWidth - n;
						if (currentWidth == 0 && length - i <= 2 && lw >= length - i) {
							lw = length - i - 1;
						}
						if (lw < 0) {
							lw = 0;
						}
						for (int m = -lw; m <= lw; m++) {
							int realx = x + m * Math.abs(dirz) + xaccum + dirx;
							int realz = z + m * Math.abs(dirx) + zaccum + dirz;
							if (!boring(w, realx, y + n, realz)) {
								continue;
							}
							if (treeType >= 0) {
								w.set(realx, y + n, realz, leaf.get(treeType));
								if (n == 0 && treeType == 3 && lw != 0 && (m == lw || m == -lw) && rand.nextInt(5) == 0) {
									if (dirx == 0) {
										if (m == lw) {
											growVines(w, realx + 1, y, realz, VineBlock.WEST, rand.nextInt(10));
										} else {
											growVines(w, realx - 1, y, realz, VineBlock.EAST, rand.nextInt(10));
										}
									} else if (m == lw) {
										growVines(w, realx, y, realz + 1, VineBlock.NORTH, rand.nextInt(10));
									} else {
										growVines(w, realx, y, realz - 1, VineBlock.SOUTH, rand.nextInt(10));
									}
								}
							} else {
								BlockState local = leaf.get(0);
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
								w.set(realx, y + n, realz, local);
							}
						}
					}
				}
				if (currentWidth > 0 && lastBranch > currentWidth && currentWidth != thisWidth && rand.nextInt(currentWidth + 1) == 0) {
					int subdirx = branchSide;
					int subdirz = 0;
					if (dirx != 0) {
						subdirx = 0;
						subdirz = branchSide;
					}
					makeBranch(w, x + xaccum + currentWidth * subdirx, y, z + zaccum + currentWidth * subdirz, currentWidth - 1, subdirx, subdirz,
						id, leaf, treeType, badCritters);
					lastBranch = 0;
					branchSide = branchSide < 0 ? 1 : -1;
				}
				xaccum += dirx;
				zaccum += dirz;
				lastBranch++;
			}
			currentWidth--;
		}
	}

	private static void root(GenWorld w, int x, int y, int z, BlockState wood) {
		if (boringBase(w, x, y, z)) {
			for (int j = 0; j < 20; j++) {
				if (y - j > 0) {
					if (!boringBase(w, x, y - j, z)) {
						break;
					}
					w.set(x, y - j, z, wood);
				}
			}
		}
	}

	private static void stepAt(GenWorld w, int x, int y, int z, BlockState step) {
		if (boring(w, x, y, z)) {
			w.set(x, y, z, step);
		}
	}

	/**
	 * MakeBigSquareTree: a hollow square trunk with a spiral staircase round it, storey floors with chests, and branches
	 * out of every storey, topped with two emerald blocks (The King or The Queen waits above the gem trees).
	 */
	public static void bigSquareTree(GenWorld w, int x, int y, int z, Mat id, Mat leaf, BlockState step, int treeType, int radius,
			boolean badCritters, Guard guard) {
		RandomSource rand = w.random();
		int thisHeight = radius + rand.nextInt(radius);
		int thisWidth = radius;
		int baseHeight = radius * 3;
		int last = -1;
		int lastLast = -1;
		BlockState wood = id.get(treeType);
		for (int i = -radius; i <= radius; i++) {
			root(w, x + i, y, z - radius, wood);
			root(w, x + i, y, z + radius, wood);
			root(w, x - radius, y, z + i, wood);
			root(w, x + radius, y, z + i, wood);
		}
		int currentY = y;
		int spiral = -thisWidth;
		while (thisWidth >= 0) {
			if (thisWidth != radius) {
				baseHeight = 0;
			}
			for (int j = 0; j < thisHeight + baseHeight; j++) {
				boolean floor = false;
				for (int i = -thisWidth; i <= thisWidth; i++) {
					if (boringBase(w, x + i, currentY, z - thisWidth)) {
						w.set(x + i, currentY, z - thisWidth, wood);
					}
					if (boringBase(w, x + i, currentY, z + thisWidth)) {
						w.set(x + i, currentY, z + thisWidth, wood);
					}
					if (boringBase(w, x - thisWidth, currentY, z + i)) {
						w.set(x - thisWidth, currentY, z + i, wood);
					}
					if (boringBase(w, x + thisWidth, currentY, z + i)) {
						w.set(x + thisWidth, currentY, z + i, wood);
					}
				}
				if (thisWidth != 0 || j < thisHeight / 2) {
					int platformLooper = 1;
					if (spiral == 0 && thisWidth >= 2 || spiral == thisWidth || spiral == thisWidth - 1 && j == thisHeight + baseHeight - 1) {
						platformLooper++;
						if (spiral != 0 && thisWidth >= 3) {
							platformLooper++;
						}
						if (spiral == 0) {
							floor = true;
						}
					}
					for (int k = 0; k < platformLooper; k++) {
						stepAt(w, x - spiral, currentY, z - thisWidth - 1, step);
						stepAt(w, x + spiral, currentY, z + thisWidth + 1, step);
						stepAt(w, x - thisWidth - 1, currentY, z + spiral, step);
						stepAt(w, x + thisWidth + 1, currentY, z - spiral, step);
						if (thisWidth >= 3) {
							stepAt(w, x - spiral, currentY, z - thisWidth - 2, step);
							stepAt(w, x + spiral, currentY, z + thisWidth + 2, step);
							stepAt(w, x - thisWidth - 2, currentY, z + spiral, step);
							stepAt(w, x + thisWidth + 2, currentY, z - spiral, step);
						}
						if (platformLooper != 1) {
							spiral++;
						}
					}
					if (floor) {
						for (int m = -thisWidth; m <= thisWidth; m++) {
							for (int n = -thisWidth; n <= thisWidth; n++) {
								if (!boring(w, x + m, currentY, z + n)) {
									continue;
								}
								w.set(x + m, currentY, z + n, wood);
								if (m == 0 && n == 0 && rand.nextInt(2) == 0 && !badCritters && w.isAir(x, currentY + 1, z)) {
									w.chest(x, currentY + 1, z, Direction.SOUTH,
										ChestLoot.fill(rand, ChestLoot.MAGIC_APPLE, radius - thisWidth + rand.nextInt(10)));
								}
							}
						}
					}
				}
				if (thisWidth != radius) {
					int next = rand.nextInt(4 + thisWidth);
					while (next == last || next == lastLast) {
						next = rand.nextInt(4 + thisWidth);
					}
					if (next < 4) {
						lastLast = last;
						last = next;
					}
					switch (next) {
						case 0 -> makeBranch(w, x + thisWidth, currentY, z, thisWidth, 1, 0, id, leaf, treeType, badCritters);
						case 1 -> makeBranch(w, x - thisWidth, currentY, z, thisWidth, -1, 0, id, leaf, treeType, badCritters);
						case 2 -> makeBranch(w, x, currentY, z + thisWidth, thisWidth, 0, 1, id, leaf, treeType, badCritters);
						case 3 -> makeBranch(w, x, currentY, z - thisWidth, thisWidth, 0, -1, id, leaf, treeType, badCritters);
						default -> {
						}
					}
				}
				currentY++;
				if (!floor) {
					spiral++;
				}
				if (spiral > thisWidth) {
					spiral = -thisWidth;
				}
			}
			if (Math.abs(spiral) > --thisWidth) {
				spiral = -thisWidth;
			}
			thisHeight += rand.nextInt(radius);
		}
		if (boringBase(w, x, currentY, z)) {
			w.set(x, currentY, z, Blocks.EMERALD_BLOCK.defaultBlockState());
			w.set(x, currentY + 1, z, Blocks.EMERALD_BLOCK.defaultBlockState());
			if (guard == Guard.KING) {
				EntityType<?> king = Lookup.entity("the_king");
				if (king != null) {
					w.spawn(king, x, currentY + 4, z, rand.nextFloat() * 360.0F, call("setGuardMode"));
				}
			} else if (guard == Guard.QUEEN) {
				EntityType<?> queen = Lookup.entity("the_queen");
				if (queen != null) {
					w.spawn(queen, x, currentY + 4, z, rand.nextFloat() * 360.0F, call("setGuardMode", "setBadMood"));
				}
			}
		}
	}

	/**
	 * OreSpawn set The King's / The Queen's guard mode (and The Queen's bad mood) to 1 so they stay on their tree. Their
	 * classes belong to another group, so the same-named methods are called if that group kept them.
	 */
	private static @Nullable Consumer<Entity> call(String... methods) {
		return entity -> {
			for (String name : methods) {
				try {
					entity.getClass().getMethod(name, int.class).invoke(entity, 1);
				} catch (ReflectiveOperationException | RuntimeException ignored) {
					// not ported (yet) under that name
				}
			}
		};
	}

	private static void circularBranch(GenWorld w, int iangle, int branchlen, int width, int startx, int starty, int startz, int twist, Mat id,
			Mat leaf, int treeType) {
		int curangle = iangle;
		double curx = startx;
		double curz = startz;
		for (double len = 0.0; len < branchlen; len += 0.5) {
			curx += 0.5 * Math.sin(Math.toRadians(curangle));
			curz += 0.5 * Math.cos(Math.toRadians(curangle));
			double tw = width - width * len / branchlen;
			for (double wd = 0.0; wd <= tw; wd += 0.5) {
				boolean isWood = wd < tw / 2.0 && !(tw < 0.9);
				BlockState block = isWood ? id.get(treeType) : leaf.get(treeType);
				for (int side = 0; side < 2; side++) {
					int ta = side == 0 ? curangle + 90 : curangle - 90;
					if (ta > 360) {
						ta -= 360;
					}
					if (ta < 0) {
						ta += 360;
					}
					double wx = curx + wd * Math.sin(Math.toRadians(ta));
					double wz = curz + wd * Math.cos(Math.toRadians(ta));
					if (boring(w, (int) wx, starty, (int) wz)) {
						w.set((int) wx, starty, (int) wz, block);
					}
					if (isWood && boring(w, (int) wx, starty + 1, (int) wz)) {
						w.set((int) wx, starty + 1, (int) wz, leaf.get(treeType));
					}
				}
			}
			curangle += twist;
			if (curangle < 0) {
				curangle += 360;
			}
			if (curangle >= 360) {
				curangle -= 360;
			}
		}
	}

	/** MakeBigCircularTree: a round trunk tapering as it rises, with a 3x3 stair winding round it and curling branches. */
	public static void bigCircularTree(GenWorld w, int x, int y, int z, Mat id, Mat leaf, BlockState step, int treeType, int radius,
			boolean badCritters) {
		RandomSource rand = w.random();
		double rad = radius;
		int stepindex = rand.nextInt(360);
		int ibranch = 0;
		for (int i = 0; i < 360; i++) {
			int curx = (int) (rad * Math.sin(Math.toRadians(i)) + 0.5);
			int curz = (int) (rad * Math.cos(Math.toRadians(i)) + 0.5);
			root(w, x + curx, y, z + curz, id.get(treeType));
		}
		int cury = 1;
		while (rad > 0.0) {
			for (int i = 0; i < 360; i++) {
				int curx = (int) (rad * Math.sin(Math.toRadians(i)) + 0.5);
				int curz = (int) (rad * Math.cos(Math.toRadians(i)) + 0.5);
				if (boringBase(w, x + curx, y + cury, z + curz)) {
					w.set(x + curx, y + cury, z + curz, id.get(treeType));
				}
				if (i >= stepindex - 1 && i <= stepindex + 1 && rad > 1.0) {
					curx = (int) ((rad + 1.9) * Math.sin(Math.toRadians(i)) + 0.5);
					curz = (int) ((rad + 1.9) * Math.cos(Math.toRadians(i)) + 0.5);
					for (int m = -1; m <= 1; m++) {
						for (int n = -1; n <= 1; n++) {
							if (boringBase(w, x + curx + m, y + cury, z + curz + n)) {
								w.set(x + curx + m, y + cury, z + curz + n, step);
							}
						}
					}
				}
			}
			if (cury > (int) rad) {
				ibranch += 80 + rand.nextInt(80);
				if (ibranch > 360) {
					ibranch -= 360;
				}
				int ibranchlen = (int) (rad * 5.0) + rand.nextInt((int) rad + 2);
				int curx = (int) (rad * Math.sin(Math.toRadians(ibranch)) + 0.5);
				int curz = (int) (rad * Math.cos(Math.toRadians(ibranch)) + 0.5);
				int twist = rand.nextInt(2) * (rand.nextInt(2) == 0 ? -1 : 1);
				circularBranch(w, ibranch, ibranchlen, (int) rad + 1, x + curx, y + cury, z + curz, twist, id, leaf, treeType);
			}
			if (cury % 6 == 0 && rad > 3.0) {
				for (double dr = rad - 0.25; dr > 0.0; dr -= 0.25) {
					for (int i = 0; i < 360; i++) {
						int curx = (int) (dr * Math.sin(Math.toRadians(i)) + 0.5);
						int curz = (int) (dr * Math.cos(Math.toRadians(i)) + 0.5);
						if (boringBase(w, x + curx, y + cury, z + curz)) {
							w.set(x + curx, y + cury, z + curz, id.get(treeType));
						}
					}
				}
				if (rand.nextInt(2) == 0 && !badCritters && w.isAir(x, y + cury + 1, z)) {
					w.chest(x, y + cury + 1, z, Direction.SOUTH, ChestLoot.fill(rand, ChestLoot.MAGIC_APPLE, radius - (int) rad + rand.nextInt(10)));
				}
			}
			stepindex += 15 + (int) ((radius - rad) * 3.0);
			if (stepindex > 360) {
				stepindex -= 360;
			}
			cury++;
			rad -= 0.01 * rand.nextInt(15);
			if (rad <= 0.0 && boringBase(w, x, y + cury, z)) {
				w.set(x, y + cury, z, Blocks.DIAMOND_BLOCK.defaultBlockState());
			}
		}
	}

	/** MakeBigRoundTree: a round trunk tapering as it rises, with flat round branches every few blocks. */
	public static void bigRoundTree(GenWorld w, int inx, int y, int inz, Mat id, Mat leaf, int treeType, int radius) {
		RandomSource rand = w.random();
		double rad = radius;
		int ibranch = 0;
		float fx = inx + 0.5F;
		float fz = inz + 0.5F;
		for (int i = 0; i < 360; i++) {
			float fcurx = (float) (rad * Math.sin(Math.toRadians(i)));
			float fcurz = (float) (rad * Math.cos(Math.toRadians(i)));
			root(w, (int) (fx + fcurx), y, (int) (fz + fcurz), id.get(treeType));
		}
		int cury = 1;
		while (rad > 0.0) {
			for (int i = 0; i < 360; i++) {
				float fcurx = (float) (rad * Math.sin(Math.toRadians(i)));
				float fcurz = (float) (rad * Math.cos(Math.toRadians(i)));
				if (boringBase(w, (int) (fx + fcurx), y + cury, (int) (fz + fcurz))) {
					w.set((int) (fx + fcurx), y + cury, (int) (fz + fcurz), id.get(treeType));
				}
			}
			if (cury > (int) rad) {
				ibranch += 80 + rand.nextInt(80);
				if (ibranch > 360) {
					ibranch -= 360;
				}
				int ibranchlen = (int) (rad * 5.0) + rand.nextInt((int) rad + 2);
				float fcurx = (float) (rad * Math.sin(Math.toRadians(ibranch)));
				float fcurz = (float) (rad * Math.cos(Math.toRadians(ibranch)));
				roundBranch(w, ibranch, ibranchlen, fx + fcurx, y + cury, fz + fcurz, id, leaf, treeType);
			}
			if (cury % 6 == 0 && rad > 3.0) {
				for (double dr = rad - 0.25; dr > 0.0; dr -= 0.25) {
					for (int i = 0; i < 360; i++) {
						float fcurx = (float) (dr * Math.sin(Math.toRadians(i)));
						float fcurz = (float) (dr * Math.cos(Math.toRadians(i)));
						if (boringBase(w, (int) (fx + fcurx), y + cury, (int) (fz + fcurz))) {
							w.set((int) (fx + fcurx), y + cury, (int) (fz + fcurz), id.get(treeType));
						}
					}
				}
			}
			cury++;
			rad -= 0.01 * rand.nextInt(15);
			if (rad <= 0.0 && boringBase(w, (int) fx, y + cury, (int) fz)) {
				w.set((int) fx, y + cury, (int) fz, Blocks.DIAMOND_BLOCK.defaultBlockState());
			}
		}
	}

	private static void roundBranch(GenWorld w, int iangle, int branchlen, float startx, int starty, float startz, Mat id, Mat leaf, int treeType) {
		double deltadir = 0.06283185200000001;
		double deltamag = 0.35F;
		int ixlast = 0;
		int izlast = 0;
		int radius = branchlen / 2;
		float centerx = (float) (startx + radius * Math.sin(Math.toRadians(iangle)));
		float centerz = (float) (startz + radius * Math.cos(Math.toRadians(iangle)));
		for (double curdir = -3.1415926; curdir < 3.1415926; curdir += deltadir) {
			for (double h = 0.75; h < radius; h += deltamag) {
				int ix = (int) (centerx + Math.cos(curdir) * h);
				int iz = (int) (centerz + Math.sin(curdir) * h);
				if (ix != ixlast || iz != izlast) {
					ixlast = ix;
					izlast = iz;
					BlockState block = radius - h < 2.0 ? leaf.get(treeType) : id.get(treeType);
					if (boring(w, ix, starty, iz)) {
						w.set(ix, starty, iz, block);
					}
				}
			}
		}
	}
}
