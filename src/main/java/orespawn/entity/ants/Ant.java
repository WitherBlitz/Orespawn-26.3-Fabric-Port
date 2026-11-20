package orespawn.entity.ants;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import orespawn.entity.WanderGoal;
import orespawn.world.OreSpawnDimensions;
import orespawn.world.OreSpawnTeleporter;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's black ant (EntityAnt), and the base of its other ants: tiny, 1 health, silent, no drops. It panics when
 * hurt, wanders a lot and keeps out of water. Right-clicking one with an empty hand takes you to Utopia, or back to the
 * overworld if you're already there (OreSpawnTeleporter). Ants don't spawn naturally; they come out of their nests
 * (AntNestBlock) and the troll blocks. Experience is Animal's 1-3: 1.7.10's EntityAnimal also gave 1-3 and ignored
 * the experienceValue OreSpawn set.
 */
public class Ant extends Animal {
	public Ant(EntityType<? extends Ant> type, Level level) {
		super(type, level);
		setPathfindingMalus(PathType.WATER, -1.0F);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return attributes(1.0, 0.15, 0.0);
	}

	protected static AttributeSupplier.Builder attributes(double health, double speed, double attack) {
		return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, health).add(Attributes.MOVEMENT_SPEED, speed)
			.add(Attributes.ATTACK_DAMAGE, attack);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new PanicGoal(this, 1.4));
		goalSelector.addGoal(1, WanderGoal.wanderALot(this, 9, 1.0));
	}

	/** The dimension this ant takes you to (and back to the overworld from). */
	protected ResourceKey<Level> destination() {
		return OreSpawnDimensions.UTOPIA;
	}

	/** EntityAnt.interact: only with nothing in the main hand. */
	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (hand != InteractionHand.MAIN_HAND || !player.getMainHandItem().isEmpty()) return super.mobInteract(player, hand);
		if (player instanceof ServerPlayer serverPlayer) OreSpawnTeleporter.travel(serverPlayer, destination());
		return InteractionResult.CONSUME;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		if (random.nextInt(200) == 1) setLastHurtByMob(null);
		super.customServerAiStep(level);
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return null;
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
	protected float getSoundVolume() {
		return 0.0F;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return !isPersistenceRequired();
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return false;
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return null;
	}

	/** EntityAnt.getCanSpawnHere (every ant): at y 50 or higher, no more than 4 ants within 20 blocks. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return pos.getY() >= 50 && level.getEntitiesOfClass(Ant.class, new AABB(pos).inflate(20.0, 10.0, 20.0)).size() <= 4;
	}
}
