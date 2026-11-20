package orespawn.item.tools;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import orespawn.entity.tools.Shoes;

/**
 * OreSpawn's {@code ItemShoes}: Red Heels, Black Heels, Slippers, Boots and the Game Controller, which are only good
 * for throwing at things ({@link Shoes}).
 */
public class ShoeItem extends Item {
	public ShoeItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 0.5F,
			0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
		if (level instanceof ServerLevel serverLevel) {
			Projectile.spawnProjectileFromRotation((l, owner, s) -> new Shoes(l, owner, s), serverLevel, stack, player, 0.0F, 1.5F, 1.0F);
		}
		stack.consume(1, player);
		return InteractionResult.SUCCESS;
	}
}
