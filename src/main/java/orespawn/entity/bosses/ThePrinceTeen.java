package orespawn.entity.bosses;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import orespawn.content.Bosses;
import orespawn.entity.ModEntities;
import orespawn.registry.ModSounds;

/**
 * OreSpawn's ThePrinceTeen, "The Young Prince": The Prince grown up, a 3.25 x 4.25 rideable three-headed dragon (see
 * {@link RoyalFlyer} for all it does). It grows into The Young Adult Prince once it has killed 26 things with its bites
 * and seen 11 dawns (a diamond block tames it and lets it grow at once); a diamond turns it back into The Prince
 * (tamed, ready to grow again). 1500 health, 50 attack (its bite does 45, twice that to a Kraken), 18 armor, 0.32
 * speed, 300 experience; it drops The Prince's spawn egg.
 * <p>
 * Health: OreSpawn's 1500 when max_health's cap allows it (with AttributeFix). Under 26.3's own cap of 1024, it has
 * 1024 and hits and heals are scaled by 1024/1500.
 */
public class ThePrinceTeen extends RoyalFlyer {
	public static final int HEALTH = 1500;
	private int killCount;
	private int dayCount;
	private int isDay;

	public ThePrinceTeen(EntityType<? extends ThePrinceTeen> type, Level level) {
		super(type, level);
		this.xpReward = 300;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return TamableAnimal.createAnimalAttributes().add(Attributes.MAX_HEALTH, HEALTH).add(Attributes.MOVEMENT_SPEED, 0.32)
			.add(Attributes.ATTACK_DAMAGE, 50.0).add(Attributes.ARMOR, 18.0);
	}

	@Override
	protected float oreSpawnHealth() {
		return HEALTH;
	}

	@Override
	protected float lookRange() {
		return 9.0F;
	}

	@Override
	protected float biteReach() {
		return 8.0F;
	}

	@Override
	protected float biteDamage() {
		return 45.0F;
	}

	@Override
	protected double knock() {
		return 1.75;
	}

	@Override
	protected double knockLift() {
		return 0.1;
	}

	@Override
	protected int huntChance() {
		return 7;
	}

	@Override
	protected double shootRangeSq() {
		return 400.0;
	}

	@Override
	protected double riddenShootMinSq() {
		return 100.0;
	}

	@Override
	protected int[] wanderRanges() {
		return new int[] {5, 14, 6, 16, 10};
	}

	@Override
	protected double huntRange() {
		return 25.0;
	}

	@Override
	protected int wingInterval() {
		return 20;
	}

	@Override
	protected double ownerFarSq() {
		return 400.0;
	}

	@Override
	protected float idleHeal() {
		return 2.0F;
	}

	@Override
	protected double maxSpeed() {
		return 0.95;
	}

	@Override
	protected double accel() {
		return 0.025;
	}

	@Override
	protected double climb() {
		return 0.035;
	}

	@Override
	protected double climbPerSpeed() {
		return 0.046;
	}

	@Override
	protected double shotHeight() {
		return 1.5;
	}

	@Override
	protected double shotAhead() {
		return 7.5;
	}

	@Override
	protected double shotPerExt() {
		return 0.04;
	}

	@Override
	protected boolean bigRiderFireball() {
		return false;
	}

	@Override
	protected int shotTicks() {
		return 10;
	}

	@Override
	protected double riderAhead() {
		return 0.65;
	}

	@Override
	protected double riderHeight() {
		return 2.75;
	}

	@Override
	protected double shoveX() {
		return 3.25;
	}

	@Override
	protected double shoveY() {
		return 4.0;
	}

	@Override
	protected double jumpBoost() {
		return 0.25;
	}

	@Override
	protected double interactRangeSq() {
		return 25.0;
	}

	@Override
	protected double sitRangeSq() {
		return 16.0;
	}

	@Override
	protected SoundEvent roar() {
		return ModSounds.get("roar");
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("alo_hurt");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("alo_death");
	}

	@Override
	protected float getSoundVolume() {
		return 0.6F;
	}

	@Override
	public float getVoicePitch() {
		return 0.75F;
	}

	@Override
	protected void diamondBlock(Player player) {
		tame(player);
		level().broadcastEntityEvent(this, (byte) 7);
		killCount = 1000;
		dayCount = 1000;
	}

	@Override
	protected void shrink(ServerLevel level, Player player) {
		ThePrince prince = becomeStage(level, Bosses.THE_PRINCE, player);
		if (prince != null && prince.isTame()) prince.setOkToGrow();
	}

	@Override
	protected void onBite(Entity target) {
		if (target instanceof LivingEntity e && e.getHealth() <= 0.0F) killCount++;
	}

	@Override
	protected void growTick(ServerLevel level) {
		if (killCount > 25 && dayCount > 10) {
			becomeStage(level, Bosses.THE_YOUNG_ADULT_PRINCE, getOwner());
			return;
		}
		boolean day = level.isBrightOutside();
		if (isDay == -1 && day) dayCount++;
		isDay = day ? 1 : -1;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		BossUtil.dropAround(this, ModEntities.SPAWN_EGGS.get("the_prince"), 2, 1.0, 0);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("SpyroKill", killCount);
		output.putInt("SpyroDay", dayCount);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		killCount = input.getIntOr("SpyroKill", 0);
		dayCount = input.getIntOr("SpyroDay", 0);
	}

	/** getCanSpawnHere: never naturally (it only comes from growing up and eggs). */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		return EntitySpawnReason.isSpawner(reason);
	}
}
