package orespawn.entity.bosses;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import orespawn.entity.ModEntities;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's ThePrincess, "The Princess": The Queen's 0.75 x 1.25 baby (see {@link RoyalBaby} for all it does), who
 * doesn't grow up. With her fire lit she gathers power (1 a tick, 5 while fighting): at 500 she either looses three
 * coloured purple bolts (fire, poison or weakness) while fighting, or else brightens the ground near her when mob
 * griefing is on (flowers on grass, grass on dirt, dirt on stone, cactus or dirt on sand, lava to water) and calls two
 * butterflies or birds; sparks fly from her while her power is over 400. 400 health, 9 damage, 14 armor, 0.32 speed, 50
 * experience; her voice is high (1.5). She hunts monsters, Mothras, dragonflies and mosquitoes, but not butterflies or
 * birds. Her texture changes while she's attacking.
 */
public class ThePrincess extends RoyalBaby {
	private static final EntityDataAccessor<Integer> POWER = SynchedEntityData.defineId(ThePrincess.class, EntityDataSerializers.INT);
	private static final String[][] FLOWERS = {{"minecraft:poppy"}, {"minecraft:dandelion"},
		{"orespawn:blue_flower", "orespawn:flower_blue"}, {"orespawn:pink_flower", "orespawn:flower_pink"}, {"orespawn:red_crystal_flower"},
		{"orespawn:green_crystal_flower"}, {"orespawn:blue_crystal_flower"}, {"orespawn:yellow_crystal_flower"}};
	private int attackLevel = 1;
	private int ticker;

	public ThePrincess(EntityType<? extends ThePrincess> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return TamableAnimal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 400.0).add(Attributes.MOVEMENT_SPEED, 0.32)
			.add(Attributes.ATTACK_DAMAGE, 10.0).add(Attributes.ARMOR, 14.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(POWER, 1);
	}

	public int power() {
		return entityData.get(POWER);
	}

	@Override
	protected float biteDamage() {
		return 9.0F;
	}

	@Override
	protected String title() {
		return "Princess";
	}

	@Override
	protected boolean huntsCritters() {
		return false;
	}

	@Override
	public float getVoicePitch() {
		return (random.nextFloat() - random.nextFloat()) * 0.2F + 1.5F;
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide() && power() > 400 && random.nextInt(6) == 1) {
			double yaw = Math.toRadians(getYRot());
			Vec3 m = getDeltaMovement();
			for (int i = 0; i < 2; i++) {
				level().addParticle(ParticleTypes.FIREWORK, getX() - 0.25 * Math.sin(yaw), getY() + 0.4, getZ() + 0.25 * Math.cos(yaw),
					(random.nextGaussian() - random.nextGaussian()) / 7.0 + m.x * 3.0, (random.nextGaussian() - random.nextGaussian()) / 7.0,
					(random.nextGaussian() - random.nextGaussian()) / 7.0 + m.z * 3.0);
			}
		}
	}

	@Override
	protected void ownAi(ServerLevel level) {
		if (++ticker % 10 == 0) entityData.set(POWER, attackLevel);
		attackLevel++;
		if (getAttacking() != 0) attackLevel += 4;
		if (getFire() == 0) attackLevel = 0;
		if (attackLevel <= 500) return;
		double yaw = Math.toRadians(getYRot());
		if (getAttacking() != 0) {
			Vec3 m = getDeltaMovement();
			for (int i = 0; i < 3; i++) {
				BossUtil.purplePower(level, getX() - 1.5 * Math.sin(yaw), getY() + 1.0, getZ() + 1.5 * Math.cos(yaw), m.x * 3.0, m.z * 3.0,
					1 + random.nextInt(3));
			}
		} else {
			if (level.getGameRules().get(GameRules.MOB_GRIEFING)) brighten(level);
			for (int m = 0; m < 2; m++) {
				int i = random.nextInt(4) - random.nextInt(4);
				int k = random.nextInt(4) - random.nextInt(4);
				int j = 1 + random.nextInt(4);
				if (!level.getBlockState(BlockPos.containing(getX() + i, getY() + j, getZ() + k)).isAir()) continue;
				Mob critter = (random.nextInt(2) == 0 ? ModEntities.BUTTERFLY : ModEntities.BIRD).create(level, EntitySpawnReason.MOB_SUMMONED);
				if (critter == null) continue;
				critter.snapTo(getX() + i, getY() + j, getZ() + k, random.nextFloat() * 360.0F, 0.0F);
				level.addFreshEntity(critter);
			}
		}
		attackLevel = 1;
	}

	/** Five columns within 4 blocks, each changed at the first thing found from 5 below up. */
	private void brighten(ServerLevel level) {
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos up = new BlockPos.MutableBlockPos();
		int x0 = (int) getX();
		int y0 = (int) getY();
		int z0 = (int) getZ();
		for (int m = 0; m < 5; m++) {
			int i = random.nextInt(5) - random.nextInt(5);
			int k = random.nextInt(5) - random.nextInt(5);
			for (int j = -5; j < 5; j++) {
				BlockState bid = level.getBlockState(p.set(x0 + i, y0 + j, z0 + k));
				boolean airAbove = level.getBlockState(up.set(x0 + i, y0 + j + 1, z0 + k)).isAir();
				if (bid.is(Blocks.GRASS_BLOCK)) {
					if (airAbove) {
						Block flower = flower(random.nextInt(8));
						if (flower != null) level.setBlock(up, flower.defaultBlockState(), Block.UPDATE_ALL);
					}
					break;
				}
				if (bid.is(Blocks.DIRT) && airAbove) {
					level.setBlock(p, Blocks.GRASS_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
					break;
				}
				if (bid.is(Blocks.STONE) && airAbove) {
					level.setBlock(up, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
					break;
				}
				if (bid.is(Blocks.SAND) && airAbove) {
					if (random.nextInt(2) == 0) level.setBlock(up, Blocks.CACTUS.defaultBlockState(), Block.UPDATE_ALL);
					else level.setBlock(p, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
					break;
				}
				if (bid.is(Blocks.LAVA) && airAbove) {
					level.setBlock(p, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, bid.getValue(LiquidBlock.LEVEL)), Block.UPDATE_ALL);
					break;
				}
				if (bid.isAir() && j > 0) break;
			}
		}
	}

	private static @Nullable Block flower(int which) {
		for (String id : FLOWERS[which]) {
			Block b = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(id)).orElse(null);
			if (b != null) return b;
		}
		return null;
	}

	/** getCanSpawnHere: always (she only comes from The Queen's death and eggs). */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		return true;
	}
}
