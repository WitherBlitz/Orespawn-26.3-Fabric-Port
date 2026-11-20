package orespawn.item.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import orespawn.content.Tools;

/**
 * OreSpawn's Experience Orb Catcher: used on the ground where an experience orb lies (within half a block of the spot
 * clicked, up to two blocks up), it catches one worth 3 or more four times in five, and comes apart into a Bottle o'
 * Enchanting, its string and its stick. If it catches nothing, it falls to the ground at that spot instead (even in
 * creative mode). Using it in the air just swings it.
 */
public class ExperienceCatcherItem extends Item {
	public ExperienceCatcherItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		BlockPos pos = context.getClickedPos();
		Vec3 hit = context.getClickLocation();
		ItemStack stack = context.getItemInHand();
		AABB box = new AABB(hit.x - 0.5, pos.getY(), hit.z - 0.5, hit.x + 0.5, pos.getY() + 2.0, hit.z + 0.5);
		for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class, box)) {
			if (orb.getValue() >= 3 && level.getRandom().nextInt(5) != 1) {
				orb.discard();
				drop(level, hit, pos, Items.EXPERIENCE_BOTTLE);
				drop(level, hit, pos, Items.STRING);
				drop(level, hit, pos, Items.STICK);
				stack.consume(1, context.getPlayer());
				return InteractionResult.SUCCESS;
			}
		}
		drop(level, hit, pos, Tools.EXPERIENCE_ORB_CATCHER);
		stack.shrink(1);
		return InteractionResult.SUCCESS;
	}

	private static void drop(Level level, Vec3 hit, BlockPos pos, Item item) {
		level.addFreshEntity(new ItemEntity(level, hit.x, pos.getY() + 1.0, hit.z, new ItemStack(item)));
	}
}
