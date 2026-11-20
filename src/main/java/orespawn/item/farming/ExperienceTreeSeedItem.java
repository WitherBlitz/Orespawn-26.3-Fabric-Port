package orespawn.item.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import orespawn.block.farming.Soil;
import orespawn.content.Farming;

/**
 * OreSpawn's ItemExperienceTreeSeed: used on grass, dirt or farmland, it sets an Experience Tree Sapling on top (over
 * whatever was there, as OreSpawn did) with a puff of happy-villager sparkles. Doesn't stack.
 */
public class ExperienceTreeSeedItem extends Item {
	public ExperienceTreeSeedItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		if (!Soil.ground(level.getBlockState(pos))) return InteractionResult.PASS;
		if (level.isClientSide()) {
			for (int i = 0; i < 10; i++) {
				level.addParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX() + level.getRandom().nextFloat(), pos.getY() + 1.0 + level.getRandom().nextFloat(),
					pos.getZ() + level.getRandom().nextFloat(), 0.0, 0.0, 0.0);
			}
		} else {
			level.setBlock(pos.above(), Farming.EXPERIENCE_TREE_SAPLING.defaultBlockState(), Block.UPDATE_CLIENTS);
			Player player = context.getPlayer();
			if (player == null || !player.hasInfiniteMaterials()) context.getItemInHand().shrink(1);
		}
		return InteractionResult.SUCCESS;
	}
}
