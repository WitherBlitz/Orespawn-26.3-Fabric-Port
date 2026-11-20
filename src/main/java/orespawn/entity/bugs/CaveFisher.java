package orespawn.entity.bugs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import orespawn.entity.Ignored;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModItems;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Cave Fisher: a small long-legged cave crawler. It wanders, and goes for animals and players within 10
 * blocks (never monsters), biting from under 3 blocks: 4 damage. Cactus can't hurt it. 10 health, 4 armor. Half the
 * time it drops 0-2 (more with Looting) gold, uranium or titanium nuggets.
 * <p>
 * It spawns only in the dark at y 50 or lower, and OreSpawn spawned it only in its Utopia and Mining dimensions and its
 * dungeons, none of which are ported yet; so for now it comes only from its spawn egg and egg ore. Its claw animation is
 * picked on the client ({@link #renderClaws}), as OreSpawn's RenderInfo was. Not ported yet: the EnderKnight and
 * EnderReaper (which it leaves alone).
 */
public class CaveFisher extends BugMonster {
	/** Client only (OreSpawn's RenderInfo ri1, ri2): the claws snap when ri1 is 1 or 3; ri2 is only rolled, as OreSpawn did. */
	public int renderClaws;
	public int renderTail;

	public CaveFisher(EntityType<? extends CaveFisher> type, Level level) {
		super(type, level);
		this.xpReward = 10;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.2)
			.add(Attributes.ATTACK_DAMAGE, 4.0).add(Attributes.ARMOR, 4.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, WanderGoal.wanderALot(this, 14, 1.0));
		goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(3, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
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
		return ModSounds.CRYO_DEATH;
	}

	@Override
	protected float getSoundVolume() {
		return 1.5F;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (random.nextInt(8) == 0) {
			LivingEntity e = findTarget(10.0, 3.0, 10.0, this::isSuitableTarget);
			if (e != null) {
				if (distanceToSqr(e) < 8.0) {
					setAttacking(true);
					if (random.nextInt(7) == 0 || random.nextInt(8) == 1) doHurtTarget(level, e);
				} else {
					getNavigation().moveTo(e, 1.2);
				}
			} else {
				setAttacking(false);
			}
		}
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		return !source.is(DamageTypes.CACTUS) && super.hurtServer(level, source, amount);
	}

	/** CaveFisher.isSuitableTarget: anything alive it can see that isn't a monster, another Cave Fisher or a creative player. */
	private boolean isSuitableTarget(LivingEntity e) {
		if (e == this || !e.isAlive() || Ignored.byHunters(e) || !getSensing().hasLineOfSight(e)) return false;
		if (e instanceof CaveFisher || e instanceof Monster) return false;
		return !isCreativeOrSpectator(e);
	}

	/** getDropItem + 1.7.10's default dropFewItems: one roll picks the item (half the time none), then 0-2 of it, + 0-Looting. */
	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		Item item = switch (random.nextInt(6)) {
			case 0 -> Items.GOLD_NUGGET;
			case 1 -> ModItems.URANIUM_NUGGET;
			case 2 -> ModItems.TITANIUM_NUGGET;
			default -> null;
		};
		if (item == null) return;
		int looting = source.getEntity() instanceof LivingEntity killer ? EnchantmentHelper.getEnchantmentLevel(
			level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING), killer) : 0;
		int count = random.nextInt(3);
		if (looting > 0) count += random.nextInt(looting + 1);
		for (int i = 0; i < count; i++) spawnAtLocation(level, item);
	}

	/** CaveFisher.getCanSpawnHere: dark, and y 50 or lower. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return isDarkEnoughToSpawn(level, pos, random) && pos.getY() <= 50;
	}
}
