package orespawn.item.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import orespawn.content.Tools;
import orespawn.entity.tools.Elevator;

/** OreSpawn's Hoverboard item ({@code ItemElevator}): used on a block, it puts a Hoverboard ({@link Elevator}) on top. */
public class HoverboardItem extends Item {
	public HoverboardItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		BlockPos pos = context.getClickedPos();
		Elevator board = Tools.HOVERBOARD_ENTITY.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
		if (board != null) {
			board.snapTo(pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, level.getRandom().nextFloat() * 360.0F, 0.0F);
			level.addFreshEntity(board);
		}
		context.getItemInHand().consume(1, context.getPlayer());
		return InteractionResult.SUCCESS;
	}
}
