package orespawn.entity.dinosaurs;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import orespawn.content.Dinosaurs;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Baryonyx: a big, peaceful grazer of the daytime plains. It keeps away from monsters and panics when hurt.
 * Every few seconds it looks for grass within 10 blocks, walks to it and, once there, crops it down to dirt (when mob
 * griefing is on), heals a little and burps. 40 health. Drops 2-6 raw beef. Babies stay put rather than despawning.
 * Breeds with OreSpawn's Crystal Apple. Not ported: its fire resistance (it took 100 ticks in fire to catch light),
 * which 26.3 has no setting for.
 */
public class Baryonyx extends Animal {
	public Baryonyx(EntityType<? extends Baryonyx> type, Level level) {
		super(type, level);
		this.xpReward = 5;
		setPathfindingMalus(PathType.WATER, -1.0F);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 40.0).add(Attributes.MOVEMENT_SPEED, 0.25)
			.add(Attributes.ATTACK_DAMAGE, 8.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new BreedGoal(this, 1.0));
		goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Monster.class, 8.0F, 1.0, 1.4F));
		goalSelector.addGoal(4, new PanicGoal(this, 1.5));
		goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 12.0F));
		goalSelector.addGoal(6, WanderGoal.wander(this, 1.0));
		goalSelector.addGoal(7, new RandomLookAroundGoal(this));
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (random.nextInt(200) == 1) setLastHurtByMob(null);
		if (random.nextInt(60) == 0) {
			Grazing.Food grass = Grazing.find(level, blockPosition().above(), 11, 6, s -> s.is(Blocks.GRASS_BLOCK));
			if (grass != null) {
				BlockPos p = grass.pos();
				getNavigation().moveTo(p.getX(), p.getY(), p.getZ(), 1.0);
				if (grass.distSq() < 12) {
					if (level.getGameRules().get(GameRules.MOB_GRIEFING)) level.setBlock(p, Blocks.DIRT.defaultBlockState(), 2);
					heal(1.0F);
					playSound(SoundEvents.PLAYER_BURP, 1.0F, random.nextFloat() * 0.2F + 0.9F);
				}
			}
		}
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("duck_hurt");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("duck_hurt");
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F;
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		if (isBaby()) {
			setPersistenceRequired();
			return false;
		}
		return true;
	}

	/** Bred with OreSpawn's Crystal Apple, which isn't ported yet. */
	@Override
	public boolean isFood(ItemStack stack) {
		return orespawn.entity.Breeding.crystalApple(stack);
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return Dinosaurs.BARYONYX.create(level, EntitySpawnReason.BREEDING);
	}

	/** Baryonyx.getCanSpawnHere: in daylight at y 50 or higher, with no more than 8 others within 20 blocks. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (pos.getY() < 50 || !level.getLevel().isBrightOutside()) return false;
		return level.getEntitiesOfClass(Baryonyx.class, new AABB(pos).inflate(20.0, 10.0, 20.0)).size() <= 8;
	}
}
