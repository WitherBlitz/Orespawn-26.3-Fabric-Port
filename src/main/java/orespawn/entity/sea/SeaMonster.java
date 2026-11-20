package orespawn.entity.sea;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import orespawn.content.Sea;
import orespawn.registry.ModSounds;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's Sea Monster (see {@link SeaSerpent} for what it shares with the Sea Viper): a long-necked sea serpent of
 * oceans and swamps that comes out at night. 110 health, 14 damage a bite (within 4 blocks plus half the target's
 * width, 1 time in 4 or 5 when its chance comes up, 1 in 5 a tick), 8 armour; 0.25 speed on land, 0.55 in water.
 * Hunts within 16 blocks. Its bite throws the target back a little (twice as high for players). After a hit it ignores
 * hits for 8 ticks. Out of water with no water in reach it loses 1 health 1 time in 40. Heals 1 in water 1 time in 120.
 * 150 xp. Drops a Sea Monster Scale.
 */
public class SeaMonster extends SeaSerpent {
	private static final Kind KIND = new Kind(0.25, 0.55, 8, 40, 4.0F, 1.0, 120, 16.0, 0.6, 0.1);

	public SeaMonster(EntityType<? extends SeaMonster> type, Level level) {
		super(type, level, KIND);
		this.xpReward = 150;
	}

	public static AttributeSupplier.Builder createAttributes() {
		// get_mobstats "SeaMonster": 110 health, 14 attack, 8 defense.
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 110.0).add(Attributes.MOVEMENT_SPEED, 0.25)
			.add(Attributes.ATTACK_DAMAGE, 14.0).add(Attributes.ARMOR, 8.0);
	}

	@Override
	protected boolean bites() {
		return random.nextInt(4) == 0 || random.nextInt(5) == 1;
	}

	@Override
	protected ItemLike trophy() {
		return Sea.SEA_MONSTER_SCALE;
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return random.nextInt(3) == 0 ? ModSounds.get("seamonster_living") : null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("seamonster_hit");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("seamonster_death");
	}

	/** SeaMonster.getCanSpawnHere: at y 50 or higher, at night, dark enough for a monster, and no other Sea Monster within 16 blocks. */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		return pos.getY() >= 50 && !level.getLevel().isBrightOutside() && Monster.isDarkEnoughToSpawn(level, pos, random)
			&& level.getEntitiesOfClass(SeaMonster.class, new AABB(pos).inflate(16.0, 5.0, 16.0)).isEmpty();
	}
}
