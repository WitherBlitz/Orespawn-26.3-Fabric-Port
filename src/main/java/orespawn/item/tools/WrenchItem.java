package orespawn.item.tools;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import orespawn.OreSpawn;
import orespawn.registry.ModItems;

/**
 * OreSpawn's Wrench ({@code ItemWrench}): hitting a Robot Spider nobody is riding takes it apart into a Spider Robot
 * Kit; hitting a Red Ant Robot nobody is riding does the same with a Red Ant Robot Kit, but a wild one only once it's
 * down to half health or less (taking it apart makes it yours). The kit keeps the robot's damage (its missing health,
 * as the kit's damage value). Each one wears the wrench by 2 (100 uses). Hitting anything else is an ordinary blow.
 * Called from Tools' attack hook.
 * <p>
 * The robots and kits belong to another group; this works once they exist with the ids {@code robot_spider},
 * {@code robot_red_ant}, {@code spider_robot_kit} and {@code red_ant_robot_kit}, and the Red Ant Robot implements
 * {@link Owned} (OreSpawn's {@code AntRobot.getOwned/setOwned}).
 */
public class WrenchItem extends Item {
	/** OreSpawn's AntRobot ownership, for the robots group to implement. */
	public interface Owned {
		boolean isOwned();

		void setOwned();
	}

	public WrenchItem(Properties properties) {
		super(properties);
	}

	public InteractionResult leftClickEntity(Player player, Level level, InteractionHand hand, Entity entity) {
		if (!(entity instanceof LivingEntity robot) || entity.isVehicle()) return InteractionResult.PASS;
		String kit;
		if (is(entity, "robot_spider")) {
			kit = "spider_robot_kit";
		} else if (is(entity, "robot_red_ant")) {
			if (!(entity instanceof Owned owned) || !owned.isOwned()) {
				if (robot.getHealth() / robot.getMaxHealth() > 0.5F) return InteractionResult.PASS;
				if (entity instanceof Owned wild) wild.setOwned();
			}
			kit = "red_ant_robot_kit";
		} else {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			int missing = (int) (robot.getMaxHealth() - robot.getHealth());
			entity.discard();
			ItemStack stack = new ItemStack(ModItems.lazy(kit));
			if (!stack.isEmpty()) {
				stack.setDamageValue(missing);
				level.addFreshEntity(new ItemEntity(level, entity.getX(), entity.getY() + 1.0, entity.getZ(), stack));
			}
			player.getItemInHand(hand).hurtAndBreak(2, player, hand);
		}
		ZooKeeperItem.burst(player, level, entity);
		return InteractionResult.SUCCESS;
	}

	private static boolean is(Entity entity, String id) {
		return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).equals(OreSpawn.id(id));
	}
}
