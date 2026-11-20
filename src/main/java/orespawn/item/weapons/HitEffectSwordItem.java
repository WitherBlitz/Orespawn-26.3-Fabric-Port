package orespawn.item.weapons;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import orespawn.OreSpawn;
import orespawn.content.Weapons;
import orespawn.entity.weapons.PetRat;
import orespawn.entity.spooky.Fairy;
import orespawn.item.AutoEnchantItem;

import java.util.List;

/**
 * OreSpawn's swords that do something extra when they hit a creature (ItemSword.hitEntity):
 * <ul>
 * <li>Poison Sword (Sharpness I): poison, wither and weakness, each for 10-19 s;</li>
 * <li>Rat Sword: 1-6 rats appear at the target, pets of the player ({@link PetRat});</li>
 * <li>Fairy Sword: 1-3 of OreSpawn's Fairies appear at the target, the player's;</li>
 * <li>Mantis Claw: takes 1 health from the target and gives it to the player;</li>
 * <li>Big Hammer: knocks the target up to 2/3 of a block a tick upwards.</li>
 * </ul>
 */
public class HitEffectSwordItem extends AutoEnchantItem {
	public enum Effect {
		POISON, RATS, FAIRIES, MANTIS, HAMMER
	}

	private final Effect effect;

	public HitEffectSwordItem(Properties properties, List<Ench> enchantments, Effect effect) {
		super(properties, enchantments);
		this.effect = effect;
	}

	@Override
	public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.hurtEnemy(stack, target, attacker);
		if (!(target.level() instanceof ServerLevel level)) return;
		var random = level.getRandom();
		switch (effect) {
			case POISON -> {
				target.addEffect(new MobEffectInstance(MobEffects.POISON, (10 + random.nextInt(10)) * 20, 0));
				target.addEffect(new MobEffectInstance(MobEffects.WITHER, (10 + random.nextInt(10)) * 20, 0));
				target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, (10 + random.nextInt(10)) * 20, 0));
			}
			case RATS -> {
				int n = 1 + random.nextInt(6);
				for (int i = 0; i < n; i++) {
					PetRat rat = summon(level, Weapons.PET_RAT, target);
					if (rat != null) rat.setOwner(attacker);
				}
			}
			case FAIRIES -> {
				EntityType<?> fairy = BuiltInRegistries.ENTITY_TYPE.getOptional(OreSpawn.id("fairy")).orElse(null);
				if (fairy == null) return;
				int n = 1 + random.nextInt(3);
				for (int i = 0; i < n; i++) {
					Entity e = summon(level, fairy, target);
					if (e instanceof Fairy f) f.setOwner(attacker);
				}
			}
			case MANTIS -> {
				target.heal(-1.0F);
				attacker.heal(1.0F);
			}
			case HAMMER -> target.push(0.0, Math.abs(random.nextFloat() * 2.0F / 3.0F), 0.0);
		}
	}

	/** RatSword.spawnCreature: next to the target, facing a random way. */
	private static <T extends Entity> T summon(ServerLevel level, EntityType<T> type, LivingEntity at) {
		T e = type.create(level, EntitySpawnReason.MOB_SUMMONED);
		if (e == null) return null;
		var r = level.getRandom();
		e.snapTo(at.getX() + (r.nextFloat() - r.nextFloat()) * 0.5, at.getY() + r.nextFloat() + 0.01, at.getZ() + (r.nextFloat() - r.nextFloat()) * 0.5,
			r.nextFloat() * 360.0F, 0.0F);
		level.addFreshEntity(e);
		return e;
	}
}
