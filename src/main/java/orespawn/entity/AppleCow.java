package orespawn.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Apple Cow, Golden Apple Cow and Enchanted Golden Apple Cow (RedCow, GoldCow, EnchantedCow): cows that
 * never despawn and drop apples as well as leather and beef; the golden one adds a golden apple, the enchanted one two
 * golden apples and an enchanted golden apple (their loot tables). One class for the three; each breeds its own kind.
 */
public class AppleCow extends AbstractCow {
	public AppleCow(EntityType<? extends AppleCow> type, Level level) {
		super(type, level);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		if (random.nextInt(200) == 1) setLastHurtByMob(null);
		super.customServerAiStep(level);
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return ((EntityType<AppleCow>) getType()).create(level, EntitySpawnReason.BREEDING);
	}
}
