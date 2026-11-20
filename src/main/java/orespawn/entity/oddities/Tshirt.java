package orespawn.entity.oddities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's T-Shirt!: a giant floating shirt that slowly turns. It doesn't move, makes no sound, has 1 health and
 * drops emeralds (its loot table). OreSpawn spawns it only in its Utopia dimension (not ported yet), by day at y 50 or
 * higher, one per 20 blocks.
 */
public class Tshirt extends Animal {
	public Tshirt(EntityType<? extends Tshirt> type, Level level) {
		super(type, level);
		this.xpReward = 40;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.MOVEMENT_SPEED, 0.0)
			.add(Attributes.ATTACK_DAMAGE, 0.0);
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return !isPersistenceRequired();
	}

	@Override
	protected @Nullable SoundEvent getHurtSound(DamageSource source) {
		return null;
	}

	@Override
	protected @Nullable SoundEvent getDeathSound() {
		return null;
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		return InteractionResult.PASS;
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return false;
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return null;
	}

	/** Tshirt.getCanSpawnHere: by day, at y 50 or higher, no other T-Shirt within 20 blocks. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return level.getLevel().isBrightOutside() && pos.getY() >= 50
			&& level.getEntitiesOfClass(Tshirt.class, new AABB(pos).inflate(20.0, 8.0, 20.0)).isEmpty();
	}
}
