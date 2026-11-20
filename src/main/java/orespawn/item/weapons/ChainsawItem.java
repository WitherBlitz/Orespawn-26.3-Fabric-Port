package orespawn.item.weapons;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import orespawn.entity.weapons.WeaponHooks;
import orespawn.registry.ModSounds;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * OreSpawn's Chainsaw (an UltimateSword): 56 + 4 damage, 3000 uses, no enchantments. Swinging it starts the engine
 * (OreSpawn's "chainsawshort" sound, at most every 50 ticks), and while it runs it smokes, sparks and spits flames (the
 * client draws those). Hitting a creature also saws everything else alive within 5 blocks that the player can see, for
 * 56 (never players, tamed pets or the Girlfriend/Boyfriend, OreSpawn's UltimateSwordPvp). It cuts wood, plants,
 * leaves and cobwebs at speed 10. Breaking any block with it fells what's around it: every log, plank, leaf block,
 * sapling, grass, fern, cobweb and cactus from 5 below to 10 above and 5 to each side goes, each dropping itself
 * nearby; if the block broken was leaves (or a sapling, grass or cobweb), only those go.
 * <p>
 * OreSpawn's own leaves, logs and planks are cut when their groups add them to the vanilla tags (logs, leaves, planks,
 * saplings).
 */
public class ChainsawItem extends UltimateSwordItem {
	/** OreSpawn's swingtimer: how long the engine runs after a swing. */
	public static final int RUN_TICKS = 50;
	private static final Map<ServerPlayer, Long> LAST_START = new WeakHashMap<>();

	public ChainsawItem(Properties properties) {
		super(properties, List.of());
	}

	/** UltimateSword.onEntitySwing for the Chainsaw: the engine sound, once per run. */
	public void swing(ServerPlayer player) {
		ServerLevel level = player.level();
		long now = level.getGameTime();
		Long last = LAST_START.get(player);
		if (last != null && now - last < RUN_TICKS && now >= last) return;
		LAST_START.put(player, now);
		level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.get("chainsawshort"), SoundSource.PLAYERS, 1.0F,
			level.getRandom().nextFloat() * 0.2F + 0.9F);
	}

	/** UltimateSword.findSomethingToHit: the Chainsaw's sweep when it hits a creature. */
	public static void sweep(ServerLevel level, Player player) {
		for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5.0))) {
			if (e == player || !e.isAlive() || WeaponHooks.pvpProtected(e) || !canSee(level, e, player)) continue;
			e.hurtServer(level, level.damageSources().playerAttack(player), 56.0F);
		}
	}

	/** UltimateSword.MyCanSee: nothing (no block, no liquid) between the player's chest and the target's middle. */
	private static boolean canSee(Level level, LivingEntity e, Player player) {
		Vec3 from = new Vec3(player.getX(), player.getY() + 1.4, player.getZ());
		Vec3 to = new Vec3(e.getX(), e.getY() + e.getBbHeight() / 2.0, e.getZ());
		BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player));
		return hit.getType() == HitResult.Type.MISS;
	}

	private static boolean isLeaves(BlockState s) {
		return s.is(Blocks.COBWEB) || s.is(BlockTags.LEAVES) || s.is(BlockTags.SAPLINGS) || s.is(Blocks.SHORT_GRASS) || s.is(Blocks.FERN)
			|| s.is(Blocks.DEAD_BUSH);
	}

	private static boolean canCrush(BlockState s) {
		return isLeaves(s) || s.is(BlockTags.LOGS) || s.is(BlockTags.PLANKS) || s.is(Blocks.CACTUS);
	}

	@Override
	public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity owner) {
		if (level instanceof ServerLevel server) {
			boolean leaf = isLeaves(state);
			var random = level.getRandom();
			for (BlockPos p : BlockPos.betweenClosed(pos.offset(-5, -5, -5), pos.offset(5, 10, 5))) {
				BlockState s = level.getBlockState(p);
				if (leaf ? !isLeaves(s) : !canCrush(s)) continue;
				ItemStack drop = new ItemStack(s.getBlock().asItem());
				if (!drop.isEmpty()) {
					double x = p.getX() + random.nextInt(5) - random.nextInt(5);
					double y = p.getY() + 1.0 + random.nextInt(5);
					double z = p.getZ() + random.nextInt(5) - random.nextInt(5);
					server.addFreshEntity(new ItemEntity(server, x, y, z, drop));
				}
				level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			}
		}
		return super.mineBlock(stack, level, state, pos, owner);
	}
}
