package orespawn.entity.bosses;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
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
 * OreSpawn's ThePrinceAdult, "The Young Adult Prince": The Young Prince grown up, a 6.25 x 10.25 rideable three-headed
 * dragon (see {@link RoyalFlyer} for all it does). A diamond turns it back into The Young Prince; a diamond block heals
 * it. With OreSpawn's FullPowerKingEnable option on (off by default, and off here), a tamed one that stays on the ground
 * for 288000 ticks (four game days) becomes The King in his end game ({@link TheKing#setFree}); a diamond block then
 * sets that off at once. 3000 health, 100 attack (its bite does 100, twice that to a Kraken), 20 armor, 0.36 speed, 3000
 * experience; it drops The Prince's spawn egg.
 * <p>
 * Health: 26.3 caps max health at 1024, so it has 1024 and hits and heals are scaled by 1024/3000.
 */
public class ThePrinceAdult extends RoyalFlyer {
	public static final int HEALTH = 3000;
	/** OreSpawnMain.FullPowerKingEnable (a config option, off by default). */
	public static final boolean FULL_POWER_KING = false;
	private int growCounter;

	public ThePrinceAdult(EntityType<? extends ThePrinceAdult> type, Level level) {
		super(type, level);
		this.xpReward = 3000;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return TamableAnimal.createAnimalAttributes().add(Attributes.MAX_HEALTH, HEALTH).add(Attributes.MOVEMENT_SPEED, 0.36)
			.add(Attributes.ATTACK_DAMAGE, 100.0).add(Attributes.ARMOR, 20.0);
	}

	@Override
	protected float oreSpawnHealth() {
		return HEALTH;
	}

	@Override
	protected float lookRange() {
		return 20.0F;
	}

	@Override
	protected float biteReach() {
		return 10.0F;
	}

	@Override
	protected float biteDamage() {
		return 100.0F;
	}

	@Override
	protected double knock() {
		return 2.0;
	}

	@Override
	protected double knockLift() {
		return 0.2;
	}

	@Override
	protected int huntChance() {
		return 6;
	}

	@Override
	protected double shootRangeSq() {
		return 600.0;
	}

	@Override
	protected double riddenShootMinSq() {
		return 0.0;
	}

	@Override
	protected int[] wanderRanges() {
		return new int[] {8, 16, 12, 20, 15};
	}

	@Override
	protected double huntRange() {
		return 32.0;
	}

	@Override
	protected int wingInterval() {
		return 30;
	}

	@Override
	protected double ownerFarSq() {
		return 900.0;
	}

	@Override
	protected float idleHeal() {
		return 5.0F;
	}

	@Override
	protected double maxSpeed() {
		return 1.05;
	}

	@Override
	protected double accel() {
		return 0.105;
	}

	@Override
	protected double climb() {
		return 0.045;
	}

	@Override
	protected double climbPerSpeed() {
		return 0.066;
	}

	@Override
	protected double shotHeight() {
		return 9.5;
	}

	@Override
	protected double shotAhead() {
		return 14.5;
	}

	@Override
	protected double shotPerExt() {
		return -0.08;
	}

	@Override
	protected boolean bigRiderFireball() {
		return true;
	}

	@Override
	protected int shotTicks() {
		return 8;
	}

	@Override
	protected double riderAhead() {
		return 4.65;
	}

	@Override
	protected double riderHeight() {
		return 9.25;
	}

	@Override
	protected double shoveX() {
		return 6.25;
	}

	@Override
	protected double shoveY() {
		return 10.0;
	}

	@Override
	protected double jumpBoost() {
		return 0.35;
	}

	@Override
	protected double interactRangeSq() {
		return 36.0;
	}

	@Override
	protected double sitRangeSq() {
		return 36.0;
	}

	@Override
	protected SoundEvent roar() {
		return ModSounds.get("king_living");
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.get("king_hit");
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.get("trex_death");
	}

	@Override
	protected float getSoundVolume() {
		return 0.85F;
	}

	@Override
	public float getVoicePitch() {
		return 1.1F;
	}

	@Override
	protected void diamondBlock(Player player) {
		growCounter = 288000;
	}

	@Override
	protected void shrink(ServerLevel level, Player player) {
		becomeStage(level, Bosses.THE_YOUNG_PRINCE, player);
	}

	@Override
	protected void growTick(ServerLevel level) {
		if (!FULL_POWER_KING || isVehicle() || !isTame() || level.getDifficulty() == Difficulty.PEACEFUL) return;
		if (++growCounter > 288000) {
			TheKing king = Bosses.THE_KING.create(level, EntitySpawnReason.CONVERSION);
			if (king == null) return;
			king.snapTo(getX(), getY(), getZ(), random.nextFloat() * 360.0F, 0.0F);
			level.addFreshEntity(king);
			king.setFree();
			ejectPassengers();
			discard();
		}
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		BossUtil.dropAround(this, ModEntities.SPAWN_EGGS.get("the_prince"), 2, 1.0, 0);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("XGrow", growCounter);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		growCounter = input.getIntOr("XGrow", 0);
	}

	/** getCanSpawnHere: never naturally (it only comes from growing up and eggs). */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		return EntitySpawnReason.isSpawner(reason);
	}
}
