package orespawn.entity.dinosaurs;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
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
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.PathType;
import orespawn.content.Dinosaurs;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Camarasaurus: a small long-necked pet. Feed it an apple (within 4 blocks): half the time a wild one is tamed
 * and healed; your own is healed. Click your own with anything else to make it sit or stand. It follows its owner, keeps
 * away from monsters, follows apples and panics when hurt. When it's hurt (1 in 20 a tick) or now and then (1 in 250) it
 * looks within 10 blocks for leaves (oak, spruce, birch, jungle), vines, grass, ferns, cactus or tall plants, walks there
 * and eats one (removed when mob griefing is on, with neighbour updates so a tall plant's other half goes too), healing
 * a little and burping. Falls hurt it at most 2. 20 health. A tamed one drops 2-6 poppies, a wild one nothing. Tamed ones
 * and babies don't despawn. Its tail sways slower and less the more hurt it is, and stops while it sits.
 * Breeds with OreSpawn's Crystal Apple. Not ported: 1.7.10's EntityAIMoveIndoors (villages'
 * doors), which 26.3 doesn't have; and its fire resistance (100 ticks in fire before it catches light).
 */
public class Camarasaurus extends TamableAnimal {
	public Camarasaurus(EntityType<? extends Camarasaurus> type, Level level) {
		super(type, level);
		this.xpReward = 5;
		setPathfindingMalus(PathType.WATER, -1.0F);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return TamableAnimal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.2)
			.add(Attributes.ATTACK_DAMAGE, 1.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
		goalSelector.addGoal(1, new BreedGoal(this, 1.0));
		goalSelector.addGoal(2, new FollowOwnerGoal(this, 2.0, 10.0F, 2.0F));
		goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Monster.class, 8.0F, 1.0, 1.4F));
		goalSelector.addGoal(4, new TemptGoal(this, 1.2F, s -> s.is(Items.APPLE), false));
		goalSelector.addGoal(5, new PanicGoal(this, 1.5));
		goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
		goalSelector.addGoal(7, WanderGoal.wander(this, 1.0));
		goalSelector.addGoal(8, new RandomLookAroundGoal(this));
	}

	/** What Camarasaurus.scan_it looks for. */
	private static boolean food(BlockState s) {
		return s.is(Blocks.OAK_LEAVES) || s.is(Blocks.SPRUCE_LEAVES) || s.is(Blocks.BIRCH_LEAVES) || s.is(Blocks.JUNGLE_LEAVES)
			|| s.is(Blocks.VINE) || s.is(Blocks.SHORT_GRASS) || s.is(Blocks.FERN) || s.is(Blocks.CACTUS) || s.is(Blocks.SUNFLOWER)
			|| s.is(Blocks.LILAC) || s.is(Blocks.TALL_GRASS) || s.is(Blocks.LARGE_FERN) || s.is(Blocks.ROSE_BUSH) || s.is(Blocks.PEONY);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		if (random.nextInt(200) == 1) setLastHurtByMob(null);
		super.customServerAiStep(level);
		if (isOrderedToSit()) return;
		if (!(random.nextInt(20) == 0 && (int) getHealth() < 20 || random.nextInt(250) == 0)) return;
		Grazing.Food plant = Grazing.find(level, blockPosition().above(), 11, 6, Camarasaurus::food);
		if (plant == null) return;
		BlockPos p = plant.pos();
		getNavigation().moveTo(p.getX(), p.getY(), p.getZ(), 1.0);
		if (plant.distSq() < 12) {
			if (level.getGameRules().get(GameRules.MOB_GRIEFING)) level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			heal(1.0F);
			playSound(SoundEvents.PLAYER_BURP, 1.0F, random.nextFloat() * 0.2F + 0.9F);
		}
	}

	@Override
	protected int calculateFallDamage(double fallDistance, float multiplier) {
		return Math.min(Mth.ceil(fallDistance - 3.0F), 2);
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		InteractionResult result = super.mobInteract(player, hand);
		if (result.consumesAction()) return result;
		if (player.distanceToSqr(this) >= 16.0) return result;
		if (stack.is(Items.APPLE)) {
			if (!level().isClientSide()) {
				if (!isTame()) {
					if (random.nextInt(2) == 0) {
						tame(player);
						heal(getMaxHealth() - getHealth());
						level().broadcastEntityEvent(this, (byte) 7);
					} else {
						level().broadcastEntityEvent(this, (byte) 6);
					}
				} else if (isOwnedBy(player)) {
					heal(getMaxHealth() - getHealth());
					level().broadcastEntityEvent(this, (byte) 7);
				}
				stack.consume(1, player);
			}
			return InteractionResult.SUCCESS;
		}
		if (isTame() && isOwnedBy(player) && !stack.is(Items.NAME_TAG)) {
			if (!level().isClientSide()) setOrderedToSit(!isOrderedToSit());
			return InteractionResult.SUCCESS;
		}
		return result;
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

	/** Camarasaurus.dropFewItems: a tamed one drops 2-6 poppies, a wild one nothing. */
	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		if (!isTame()) return;
		int n = 2 + random.nextInt(5);
		for (int i = 0; i < n; i++) spawnAtLocation(level, Items.POPPY);
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		if (isBaby()) {
			setPersistenceRequired();
			return false;
		}
		return !isTame();
	}

	/** Bred with OreSpawn's Crystal Apple, which isn't ported yet. */
	@Override
	public boolean isFood(ItemStack stack) {
		return orespawn.entity.Breeding.crystalApple(stack);
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return Dinosaurs.CAMARASAURUS.create(level, EntitySpawnReason.BREEDING);
	}

	/** Camarasaurus.getCanSpawnHere: in daylight at y 50 or higher. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return pos.getY() >= 50 && level.getLevel().isBrightOutside();
	}
}
