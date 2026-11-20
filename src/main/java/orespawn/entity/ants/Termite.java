package orespawn.entity.ants;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import orespawn.OreSpawn;
import orespawn.entity.WanderGoal;
import orespawn.world.OreSpawnDimensions;
import orespawn.world.OreSpawnTeleporter;

/**
 * OreSpawn's termite: 5 health, bites players like the red ant (one bite in 15 lands, for 1), and eats wood. One tick in
 * 200 it looks for the nearest wooden block within 7 (planks, slabs, stairs, fences and gates, doors, pressure plates,
 * standing signs, beds, crafting tables, bookshelves, Crystal Planks; not logs), walks to it, and when it's within reach
 * eats it: two times in three the block turns to dirt and a new termite appears on this one, otherwise the block is
 * gone and the new termite appears where it was (only with mob griefing on; the new termite comes either way, while
 * fewer than 10 termites are within 3 blocks). Eating heals it 1.
 * <p>
 * Right-clicking one with an empty hand takes you to the Crystal world, but only with an empty inventory and no armor
 * ("Empty your hand!", "Empty your inventory!", "Take off your armor!"); from the Crystal world it takes you back to the
 * overworld as you are. The off-hand counts as inventory, since 1.7.10 had none. OreSpawn's 1.7.10 stairs list missed
 * acacia and dark oak; here every wooden stair counts, like its planks and slabs did.
 */
public class Termite extends Ant {
	private static final Identifier CRYSTAL_PLANKS = OreSpawn.id("crystal_planks");
	private int attackDelay = 20;
	private int closest = 99999;
	private int tx;
	private int ty;
	private int tz;

	public Termite(EntityType<? extends Termite> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return attributes(5.0, 0.2, 2.0);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		goalSelector.addGoal(0, new PanicGoal(this, 1.4F));
		goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
		goalSelector.addGoal(2, WanderGoal.wanderALot(this, 8, 1.0));
		targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 6, true, false, null));
	}

	@Override
	protected ResourceKey<Level> destination() {
		return OreSpawnDimensions.CRYSTAL;
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (hand != InteractionHand.MAIN_HAND) return super.mobInteract(player, hand);
		if (!player.getMainHandItem().isEmpty()) {
			tell(player, "Empty your hand!");
			return super.mobInteract(player, hand);
		}
		if (player.level().dimension() != OreSpawnDimensions.CRYSTAL) {
			if (!player.getOffhandItem().isEmpty() || player.getInventory().getNonEquipmentItems().stream().anyMatch(s -> !s.isEmpty())) {
				tell(player, "Empty your inventory!");
				return InteractionResult.PASS;
			}
			for (EquipmentSlot slot : EquipmentSlot.values()) {
				if (slot.isArmor() && !player.getItemBySlot(slot).isEmpty()) {
					tell(player, "Take off your armor!");
					return InteractionResult.PASS;
				}
			}
		}
		if (player instanceof ServerPlayer serverPlayer) OreSpawnTeleporter.travel(serverPlayer, destination());
		return InteractionResult.CONSUME;
	}

	private static void tell(Player player, String message) {
		if (player instanceof ServerPlayer) player.sendSystemMessage(Component.literal(message));
	}

	/** One bite in 15 lands, for 1 damage. */
	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		if (random.nextInt(15) != 0) return false;
		return level.getDifficulty() != Difficulty.PEACEFUL && target.hurtServer(level, damageSources().mobAttack(this), 1.0F);
	}

	@Override
	public void tick() {
		super.tick();
		if (level() instanceof ServerLevel level && isAlive() && --attackDelay <= 0) {
			attackDelay = 20;
			if (level.getDifficulty() != Difficulty.PEACEFUL) {
				Player player = level.getNearestPlayer(getX(), getY(), getZ(), 1.5, true);
				if (player != null) doHurtTarget(level, player);
			}
		}
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		if (random.nextInt(200) == 1) setLastHurtByMob(null);
		if (random.nextInt(200) == 1) eatWood(level);
		super.customServerAiStep(level);
	}

	private void eatWood(ServerLevel level) {
		closest = 99999;
		tx = ty = tz = 0;
		BlockPos at = blockPosition();
		// Shells at 1, 2, 3, 4, 5 and 7 blocks out (at most 4 up and down), stopping at the first with wood.
		for (int i = 1; i < 8; i++) {
			if (scan(level, at.getX(), at.getY() + 1, at.getZ(), i, Math.min(i, 4), i)) break;
			if (i >= 5) i++;
		}
		if (closest >= 99999) return;
		getNavigation().moveTo(tx, ty, tz, 1.0);
		if (closest >= 6) return;
		BlockPos wood = new BlockPos(tx, ty, tz);
		boolean griefing = level.getGameRules().get(GameRules.MOB_GRIEFING);
		if (random.nextInt(3) != 0) {
			if (griefing) level.setBlock(wood, Blocks.DIRT.defaultBlockState(), Block.UPDATE_CLIENTS);
			if (buddies() < 10) spawnTermite(level, getX() + 0.1, getY() + 0.1, getZ() + 0.1);
		} else {
			if (griefing) level.setBlock(wood, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			if (buddies() < 10) spawnTermite(level, tx + 0.1, ty + 0.1, tz + 0.1);
		}
		heal(1.0F);
	}

	/** Termite.scan_it: the six faces of the box (dx, dy, dz) around (x, y, z); keeps the nearest wood found. */
	private boolean scan(ServerLevel level, int x, int y, int z, int dx, int dy, int dz) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int found = 0;
		for (int i = -dy; i <= dy; i++) {
			for (int j = -dz; j <= dz; j++) {
				found += check(level, pos, x + dx, y + i, z + j, dx * dx + j * j + i * i);
				found += check(level, pos, x - dx, y + i, z + j, dx * dx + j * j + i * i);
			}
		}
		for (int i = -dx; i <= dx; i++) {
			for (int j = -dz; j <= dz; j++) {
				found += check(level, pos, x + i, y + dy, z + j, dy * dy + j * j + i * i);
				found += check(level, pos, x + i, y - dy, z + j, dy * dy + j * j + i * i);
			}
		}
		for (int i = -dx; i <= dx; i++) {
			for (int j = -dy; j <= dy; j++) {
				found += check(level, pos, x + i, y + j, z + dz, dz * dz + j * j + i * i);
				found += check(level, pos, x + i, y + j, z - dz, dz * dz + j * j + i * i);
			}
		}
		return found != 0;
	}

	private int check(ServerLevel level, BlockPos.MutableBlockPos pos, int x, int y, int z, int distance) {
		if (distance >= closest || !isWood(level.getBlockState(pos.set(x, y, z)))) return 0;
		closest = distance;
		tx = x;
		ty = y;
		tz = z;
		return 1;
	}

	/** Termite.isWood, with 26.3's tags for each 1.7.10 block that covered every wood type. */
	public static boolean isWood(BlockState state) {
		return state.is(BlockTags.WOODEN_FENCES) || state.is(BlockTags.FENCE_GATES) || state.is(BlockTags.PLANKS)
			|| state.is(BlockTags.WOODEN_SLABS) || state.is(BlockTags.BEDS) || state.is(Blocks.CRAFTING_TABLE)
			|| state.is(BlockTags.STANDING_SIGNS) || state.is(Blocks.BOOKSHELF) || state.is(BlockTags.WOODEN_DOORS)
			|| state.is(BlockTags.WOODEN_PRESSURE_PLATES) || state.is(BlockTags.WOODEN_STAIRS)
			|| CRYSTAL_PLANKS.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
	}

	private int buddies() {
		return level().getEntitiesOfClass(Termite.class, getBoundingBox().inflate(3.0)).size();
	}

	private void spawnTermite(ServerLevel level, double x, double y, double z) {
		Entity termite = getType().create(level, EntitySpawnReason.TRIGGERED);
		if (termite == null) return;
		termite.snapTo(x, y, z, level.getRandom().nextFloat() * 360.0F, 0.0F);
		level.addFreshEntity(termite);
	}
}
