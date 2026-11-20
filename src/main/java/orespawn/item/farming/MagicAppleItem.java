package orespawn.item.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import orespawn.block.farming.MagicTree;
import orespawn.block.farming.Soil;

/**
 * OreSpawn's ItemMagicApple ("OMG! No! Don't do it!!!"): not a food. Used on grass, farmland or dirt, it turns that
 * block to gold and, with a bang, grows one of OreSpawn's giant trees from it ({@link MagicTree}): a square tree (60%),
 * a round one (20%), a circular one (19%), or a gold-and-emerald or obsidian-and-ruby one (1%). Doesn't stack.
 * OreSpawn gave it Fortune II just for the glint; here it simply glints.
 */
public class MagicAppleItem extends Item {
	public MagicAppleItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		BlockPos pos = context.getClickedPos();
		if (!Soil.ground(context.getLevel().getBlockState(pos))) return InteractionResult.PASS;
		if (context.getLevel() instanceof ServerLevel level) {
			level.setBlock(pos, Blocks.GOLD_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
			double x = pos.getX() + 0.5, y = pos.getY() + 1.25, z = pos.getZ() + 0.5;
			level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 6, 0.0, 0.0, 0.0, 0.0);
			level.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 6, 0.0, 0.0, 0.0, 0.0);
			level.sendParticles(DustParticleOptions.REDSTONE, x, y, z, 6, 0.0, 0.0, 0.0, 0.0);
			Player player = context.getPlayer();
			level.playSound(null, player != null ? player.blockPosition() : pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 2.8F, 1.5F);
			new MagicTree(level, level.getRandom()).growRandom(pos);
			if (player == null || !player.hasInfiniteMaterials()) context.getItemInHand().shrink(1);
		}
		return InteractionResult.SUCCESS;
	}
}
