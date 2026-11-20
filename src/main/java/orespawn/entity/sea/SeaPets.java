package orespawn.entity.sea;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import orespawn.registry.ModItems;

/**
 * The taming OreSpawn's Hydrolisc and Water Dragon share: raw fish (any of the four; 1.7.10 had one raw fish item for
 * them all) tames a wild one now and then and heals a tame one fully; the owner frees it with a dead bush and makes it
 * sit or stand with anything else. OreSpawn's "sitting" only stops it following, wandering and heading for water (it had
 * no sit goal), so it stays put without sitting down. Their breeding item is OreSpawn's Crystal Apple.
 */
final class SeaPets {
	private SeaPets() {
	}

	static boolean rawFish(ItemStack s) {
		return s.is(Items.COD) || s.is(Items.SALMON) || s.is(Items.TROPICAL_FISH) || s.is(Items.PUFFERFISH);
	}

	/** OreSpawn's Crystal Apple, the Crystal group's item. */
	static boolean crystalApple(ItemStack s) {
		return !s.isEmpty() && s.is(ModItems.lazy("crystal_apple"));
	}

	/**
	 * The fish, dead bush and sit/stand part of their interact, after the animal's own (breeding). {@code reachSq} is how
	 * close the player must be (squared), {@code tameChance} the 1-in-n chance a fish tames it.
	 */
	static InteractionResult interact(TamableAnimal pet, Player player, InteractionHand hand, double reachSq, int tameChance) {
		ItemStack stack = player.getItemInHand(hand);
		boolean near = player.distanceToSqr(pet) < reachSq;
		boolean server = !pet.level().isClientSide();
		if (rawFish(stack) && near) {
			if (!pet.isTame()) {
				if (server) {
					if (pet.getRandom().nextInt(tameChance) == 0) {
						pet.tame(player);
						pet.heal(pet.getMaxHealth() - pet.getHealth());
						pet.level().broadcastEntityEvent(pet, (byte) 7);
					} else {
						pet.level().broadcastEntityEvent(pet, (byte) 6);
					}
				}
			} else if (pet.isOwnedBy(player) && server) {
				pet.level().broadcastEntityEvent(pet, (byte) 7);
				pet.heal(pet.getMaxHealth() - pet.getHealth());
			}
			if (server) stack.consume(1, player);
			return InteractionResult.SUCCESS;
		}
		if (!pet.isTame() || !pet.isOwnedBy(player) || !near) return InteractionResult.PASS;
		if (stack.is(Items.DEAD_BUSH)) {
			if (server) {
				pet.setTame(false, true);
				pet.setOwner(null);
				sit(pet, false);
				pet.level().broadcastEntityEvent(pet, (byte) 6);
				stack.consume(1, player);
			}
			return InteractionResult.SUCCESS;
		}
		// A name tag is left to vanilla, which names any mob (OreSpawn's own handling named it after the tag item).
		if (stack.is(Items.NAME_TAG)) return InteractionResult.PASS;
		if (server) sit(pet, !pet.isOrderedToSit());
		return InteractionResult.SUCCESS;
	}

	static void sit(TamableAnimal pet, boolean sitting) {
		pet.setOrderedToSit(sitting);
		pet.setInSittingPose(sitting);
	}
}
