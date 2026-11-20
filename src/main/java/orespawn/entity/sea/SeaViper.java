package orespawn.entity.sea;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import orespawn.content.Sea;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Sea Viper (see {@link SeaSerpent} for what it shares with the Sea Monster): a long, fanged sea snake of
 * oceans and stony shores that comes out by day, flicking its forked tongue as it hunts. 160 health, 22 damage a bite
 * (within 4.5 blocks plus half the target's width, 1 time in 2 or 4 when its chance comes up, 1 in 5 a tick), 12
 * armour; 0.35 speed on land, 0.75 in water. Hunts within 18 blocks and chases at 1.5 speed. Its bite throws the target
 * back and half the time poisons it for 6 seconds (8 on Easy: OreSpawn's difficulty check only ever reached Easy).
 * After a hit it ignores hits for 5 ticks. Out of water with no water in reach it loses 1 health 1 time in 150. Heals 1
 * in water 1 time in 100. 120 xp. Drops a Sea Viper Tongue, and a raw chicken with each of its 9-14 raw cod.
 */
public class SeaViper extends SeaSerpent {
	private static final Kind KIND = new Kind(0.35, 0.75, 5, 150, 4.5F, 1.5, 100, 18.0, 0.8, 0.14);

	public SeaViper(EntityType<? extends SeaViper> type, Level level) {
		super(type, level, KIND);
		this.xpReward = 120;
	}

	public static AttributeSupplier.Builder createAttributes() {
		// get_mobstats "SeaViper": 160 health, 22 attack, 12 defense.
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 160.0).add(Attributes.MOVEMENT_SPEED, 0.35)
			.add(Attributes.ATTACK_DAMAGE, 22.0).add(Attributes.ARMOR, 12.0);
	}

	@Override
	protected boolean bites() {
		return random.nextInt(2) == 0 || random.nextInt(4) == 1;
	}

	@Override
	protected void afterBite(ServerLevel level, LivingEntity target) {
		int seconds = level.getDifficulty() == Difficulty.EASY ? 8 : 6;
		if (random.nextInt(2) == 1) target.addEffect(new MobEffectInstance(MobEffects.POISON, seconds * 20, 0), this);
	}

	@Override
	protected ItemLike trophy() {
		return Sea.SEA_VIPER_TONGUE;
	}

	@Override
	protected void dropFish(ServerLevel level) {
		super.dropFish(level);
		SeaLoot.drop(this, level, Items.CHICKEN);
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return random.nextInt(2) == 0 ? ModSounds.get("seaviper_living") : null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("seaviper_hit");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("seaviper_death");
	}

	/** SeaViper.getCanSpawnHere: at y 50 or higher, by day, and no other Sea Viper within 16 blocks. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return pos.getY() >= 50 && level.getLevel().isBrightOutside()
			&& level.getEntitiesOfClass(SeaViper.class, new AABB(pos).inflate(16.0, 5.0, 16.0)).isEmpty();
	}
}
