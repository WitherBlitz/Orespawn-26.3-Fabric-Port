package orespawn.entity.oddities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import orespawn.content.Oddities;
import orespawn.entity.ModEntities;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

/**
 * OreSpawn's Easter Bunny: a shy rabbit (10 health, quick) that runs from monsters and players and now and then lays
 * 1-3 of a random spawn egg (about every 30 seconds; one time in nineteen nothing). OreSpawn picked from its own eggs;
 * here it's every OreSpawn spawn egg ported so far plus vanilla's. It only appears on April 20 (OreSpawn's "Easter"),
 * by day at y 50 or higher, one per 32 blocks. Drops 2-4 raw chicken (its loot table).
 * <p>
 * Breeds with OreSpawn's Crystal Apple.
 */
public class EasterBunny extends Animal {
	public EasterBunny(EntityType<? extends EasterBunny> type, Level level) {
		super(type, level);
		this.xpReward = 5;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.45)
			.add(Attributes.ATTACK_DAMAGE, 8.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new BreedGoal(this, 1.0));
		goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Monster.class, 8.0F, 1.0, 1.4F));
		goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 8.0F, 1.0, 1.4F));
		goalSelector.addGoal(4, new PanicGoal(this, 1.5));
		goalSelector.addGoal(5, new LookAtPlayerGoal(this, LivingEntity.class, 8.0F));
		goalSelector.addGoal(6, WanderGoal.wanderALot(this, 16, 1.0));
		goalSelector.addGoal(7, new RandomLookAroundGoal(this));
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.DUCK_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.DUCK_HURT;
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		if (random.nextInt(200) == 1) setLastHurtByMob(null);
		super.customServerAiStep(level);
		if (random.nextInt(600) == 1) layAnEgg(level, 1 + random.nextInt(3));
	}

	/** LayAnEgg: OreSpawn rolled 115 ways, 109 of them an egg. */
	private void layAnEgg(ServerLevel level, int count) {
		if (random.nextInt(115) < 6) return;
		List<Item> eggs = new ArrayList<>(ModEntities.SPAWN_EGGS.values());
		for (Item item : BuiltInRegistries.ITEM) {
			if (item instanceof SpawnEggItem && BuiltInRegistries.ITEM.getKey(item).getNamespace().equals("minecraft")) eggs.add(item);
		}
		if (eggs.isEmpty()) return;
		Oddity.dropRand(level, this, new ItemStack(eggs.get(random.nextInt(eggs.size())), count), 2, 1.0);
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		if (isBaby()) {
			setPersistenceRequired();
			return false;
		}
		return !isPersistenceRequired();
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return orespawn.entity.Breeding.crystalApple(stack);
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return Oddities.EASTER_BUNNY.create(level, EntitySpawnReason.BREEDING);
	}

	/** OreSpawn added its spawns only on April 20; EasterBunny.getCanSpawnHere: y 50 or higher, by day, one per 32 blocks. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		LocalDate today = LocalDate.now();
		if (today.getMonth() != Month.APRIL || today.getDayOfMonth() != 20) return false;
		return pos.getY() >= 50 && level.getLevel().isBrightOutside()
			&& level.getEntitiesOfClass(EasterBunny.class, new AABB(pos).inflate(32.0, 8.0, 32.0)).isEmpty();
	}
}
