package orespawn.item.robots;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import orespawn.entity.robots.AntRobot;

import java.util.function.Supplier;

/**
 * OreSpawn's ItemSpiderRobotKit: the Spider Robot Kit and Red Ant Robot Kit. Used on a block, it builds its robot on
 * top with a bang, with as much health as the kit has durability left (and the kit's name, if it has one); a robot
 * ant built from a kit is "owned", so it can be ridden. The kit is used up (not in creative).
 */
public class RobotKitItem extends Item {
	private final Supplier<EntityType<? extends Mob>> type;

	public RobotKitItem(Item.Properties properties, Supplier<EntityType<? extends Mob>> type) {
		super(properties);
		this.type = type;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
		ItemStack stack = context.getItemInHand();
		Player player = context.getPlayer();
		var pos = context.getClickedPos();
		Mob mob = type.get().create(level, EntitySpawnReason.SPAWN_ITEM_USE);
		if (mob != null) {
			mob.snapTo(pos.getX() + 0.5, pos.getY() + 1.01, pos.getZ() + 0.5, level.getRandom().nextFloat() * 360.0F, 0.0F);
			mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), EntitySpawnReason.SPAWN_ITEM_USE, null);
			level.addFreshEntity(mob);
			mob.setHealth(stack.getMaxDamage() - stack.getDamageValue());
			Component name = stack.get(DataComponents.CUSTOM_NAME);
			if (name != null) mob.setCustomName(name);
			level.playSound(null, player != null ? player.getX() : mob.getX(), player != null ? player.getY() : mob.getY(),
				player != null ? player.getZ() : mob.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F,
				level.getRandom().nextFloat() * 0.2F + 0.9F);
			if (mob instanceof AntRobot ant) ant.setOwned();
		}
		stack.consume(1, player);
		return InteractionResult.SUCCESS;
	}
}
