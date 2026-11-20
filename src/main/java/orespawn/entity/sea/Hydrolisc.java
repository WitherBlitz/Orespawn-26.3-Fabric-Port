package orespawn.entity.sea;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import orespawn.content.Sea;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Hydrolisc: a small, many-legged, feathered swamp and jungle pet that loves water. Raw fish (within 4
 * blocks) tames a wild one half the time and heals a tame one fully; the owner frees it with a dead bush and makes it
 * stay or move with anything else (see {@link SeaPets}). It shies from monsters, panics when hurt, follows its owner and
 * players holding fish. When hurt (1 in 20 a tick) or now and then (1 in 100) it heads for the nearest water (out to
 * about 9 blocks) and heals 1 when it gets there; in water it bobs up. A tame one with more than 20 health gives its
 * hurt owner 1 health at a time from its own (1 in 10 a tick). Its crest of feathers sways with its health. 100 health,
 * 10 armour; no hit hurts it for more than 10, and falls for at most 2. Never despawns. A tame one drops 2-6 raw cod.
 * Not ported: OreSpawn's EntityAIMoveIndoors (26.3's villages have no door goal for other mobs).
 */
public class Hydrolisc extends TamableAnimal {
	public Hydrolisc(EntityType<? extends Hydrolisc> type, Level level) {
		super(type, level);
		this.xpReward = 5;
		setPathfindingMalus(PathType.WATER, 0.0F);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return TamableAnimal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 100.0).add(Attributes.MOVEMENT_SPEED, 0.25)
			.add(Attributes.ATTACK_DAMAGE, 1.0).add(Attributes.ARMOR, 10.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new BreedGoal(this, 1.0));
		goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Monster.class, 8.0F, 1.0, 1.4));
		goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.2, 10.0F, 2.0F));
		goalSelector.addGoal(4, new TemptGoal(this, 1.25, SeaPets::rawFish, false));
		goalSelector.addGoal(5, new PanicGoal(this, 1.5));
		goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
		goalSelector.addGoal(7, WanderGoal.wander(this, 1.0));
		goalSelector.addGoal(8, new RandomLookAroundGoal(this));
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	protected void travelInWater(Vec3 input, double baseGravity, boolean isFalling, double oldY) {
		super.travelInWater(SeaSwim.swimInput(input), baseGravity, isFalling, oldY);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (isInWater()) setDeltaMovement(getDeltaMovement().add(0.0, 0.04, 0.0));
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (random.nextInt(200) == 1) setLastHurtByMob(null);
		if (!isOrderedToSit() && (random.nextInt(20) == 0 && (int) getHealth() < getMaxHealth() || random.nextInt(100) == 0)) {
			BlockPos water = SeaSwim.nearestWater(this, level, 11, 4);
			if (water != null) {
				getNavigation().moveTo(water.getX(), water.getY() - 1, water.getZ(), 1.0);
				// OreSpawn also played "splash" here, which named no sound.
				if (isInWater()) heal(1.0F);
			}
		}
		LivingEntity owner = random.nextInt(10) == 0 && isTame() ? getOwner() : null;
		if (owner != null && owner.getHealth() < owner.getMaxHealth() && (int) getHealth() > 20) {
			owner.heal(1.0F);
			heal(-1.0F);
		}
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		InteractionResult result = super.mobInteract(player, hand);
		if (result.consumesAction()) return result;
		return SeaPets.interact(this, player, hand, 16.0, 2);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
		return super.hurtServer(level, source, Math.min(damage, 10.0F));
	}

	@Override
	protected int calculateFallDamage(double fallDistance, float multiplier) {
		return Math.min(Mth.ceil(fallDistance - 3.0F), 2);
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return false;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		if (!isTame()) return;
		int n = 2 + random.nextInt(5);
		for (int i = 0; i < n; i++) spawnAtLocation(level, Items.COD);
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("cryo_hurt");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("cryo_death");
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F;
	}

	@Override
	public float getVoicePitch() {
		return (random.nextFloat() - random.nextFloat()) * 0.1F + (isBaby() ? 1.5F : 1.0F);
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return SeaPets.crystalApple(stack);
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return Sea.HYDROLISC.create(level, EntitySpawnReason.BREEDING);
	}
}
