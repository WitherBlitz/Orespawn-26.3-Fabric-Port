package orespawn.entity.oddities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import orespawn.entity.ModEntities;
import orespawn.registry.ModItems;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Coin!: a huge spinning coin that hangs in the air, silent, with 1 health. Breaking it drops one prize: an
 * Emerald Sword (most likely), a diamond, a uranium or titanium nugget, an emerald, an emerald axe, shovel, pickaxe or
 * hoe, or a Coin spawn egg. Spawns by day at y 50 or higher, one per 20 blocks.
 */
public class Coin extends Animal {
	public Coin(EntityType<? extends Coin> type, Level level) {
		super(type, level);
		this.xpReward = 10;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.MOVEMENT_SPEED, 0.0)
			.add(Attributes.ATTACK_DAMAGE, 0.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new RandomLookAroundGoal(this));
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
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		Item prize = switch (random.nextInt(10)) {
			case 0 -> Items.DIAMOND;
			case 1 -> ModItems.URANIUM_NUGGET;
			case 2 -> ModItems.TITANIUM_NUGGET;
			case 3 -> Items.EMERALD;
			case 4 -> ModItems.byName("emerald_axe");
			case 5 -> ModItems.byName("emerald_shovel");
			case 6 -> ModItems.byName("emerald_pickaxe");
			case 7 -> ModItems.byName("emerald_hoe");
			case 8 -> ModEntities.SPAWN_EGGS.get("coin");
			default -> ModItems.byName("emerald_sword");
		};
		Oddity.dropRand(level, this, new ItemStack(prize), 2, 1.0);
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

	/** Coin.getCanSpawnHere: by day, at y 50 or higher, no other Coin within 20 blocks. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return level.getLevel().isBrightOutside() && pos.getY() >= 50
			&& level.getEntitiesOfClass(Coin.class, new AABB(pos).inflate(20.0, 8.0, 20.0)).isEmpty();
	}
}
