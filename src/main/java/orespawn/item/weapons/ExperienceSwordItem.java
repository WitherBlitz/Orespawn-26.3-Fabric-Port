package orespawn.item.weapons;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import orespawn.content.Weapons;
import orespawn.item.AutoEnchantItem;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * OreSpawn's Experience Sword (Sharpness II, Unbreaking III, 1400 uses). Hitting a creature gives the player 10
 * experience, and then hits it again for half the player's level (a player hit, so it only adds what's more than the
 * first hit), in a puff of portal particles. While it's carried, each worn piece of Experience armor sometimes gives a
 * point of experience too (OreSpawn checked once in 60 ticks: then the helmet gave one with a 1 in 10 chance, the
 * chestplate 1 in 20, the leggings 1 in 30, the boots 1 in 40), with portal particles where the piece is worn.
 */
public class ExperienceSwordItem extends AutoEnchantItem {
	public ExperienceSwordItem(Properties properties, List<Ench> enchantments) {
		super(properties, enchantments);
	}

	@Override
	public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.hurtEnemy(stack, target, attacker);
		if (!(target.level() instanceof ServerLevel level) || !(attacker instanceof Player p)) return;
		if (target instanceof Mob) p.giveExperiencePoints(10);
		float extra = p.experienceLevel / 2;
		if (extra > 0.0F) target.hurtServer(level, level.damageSources().playerAttack(p), extra);
		level.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 1.0, target.getZ(), (int) (extra / 2.0F) + 1, 0.0, 0.0, 0.0, 1.0);
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
		super.inventoryTick(stack, level, owner, slot);
		if (level.getRandom().nextInt(60) != 1 || !(owner instanceof Player p)) return;
		pieceXp(level, p, EquipmentSlot.HEAD, Weapons.EXPERIENCE_HELMET, 10, 1.5);
		pieceXp(level, p, EquipmentSlot.CHEST, Weapons.EXPERIENCE_CHESTPLATE, 20, 1.25);
		pieceXp(level, p, EquipmentSlot.LEGS, Weapons.EXPERIENCE_LEGGINGS, 30, 0.75);
		pieceXp(level, p, EquipmentSlot.FEET, Weapons.EXPERIENCE_BOOTS, 40, 0.25);
	}

	private static void pieceXp(ServerLevel level, Player p, EquipmentSlot slot, Item piece, int chance, double height) {
		if (!p.getItemBySlot(slot).is(piece)) return;
		if (level.getRandom().nextInt(chance) == 1) p.giveExperiencePoints(1);
		level.sendParticles(ParticleTypes.PORTAL, p.getX(), p.getY() + height, p.getZ(), 1, 0.0, 0.0, 0.0, 1.0);
	}
}
