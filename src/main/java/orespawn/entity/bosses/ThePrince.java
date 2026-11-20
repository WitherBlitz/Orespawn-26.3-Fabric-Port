package orespawn.entity.bosses;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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

/**
 * OreSpawn's ThePrince, "The Prince": The King's 0.75 x 1.25 baby (see {@link RoyalBaby} for all it does). It grows into
 * The Young Prince once it has killed 26 things, been fed 11 times and seen 11 dawns; a diamond block makes it ready at
 * once, and once it may grow its owner can also make it grow with a diamond. 500 health, 10 damage, 16 armor, 0.32
 * speed, 50 experience; its voice is high (1.3).
 */
public class ThePrince extends RoyalBaby {
	private boolean okToGrow;

	public ThePrince(EntityType<? extends ThePrince> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return TamableAnimal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 500.0).add(Attributes.MOVEMENT_SPEED, 0.32)
			.add(Attributes.ATTACK_DAMAGE, 10.0).add(Attributes.ARMOR, 16.0);
	}

	/** set_ok_to_grow: shrunk back from The Young Prince, it may grow again (from scratch). */
	public void setOkToGrow() {
		okToGrow = true;
		killCount = 0;
		fedCount = 0;
		dayCount = 0;
	}

	@Override
	protected float biteDamage() {
		return 10.0F;
	}

	@Override
	protected String title() {
		return "Prince";
	}

	@Override
	protected boolean huntsCritters() {
		return true;
	}

	@Override
	public float getVoicePitch() {
		return (random.nextFloat() - random.nextFloat()) * 0.2F + 1.3F;
	}

	@Override
	protected void onDiamondBlock() {
		okToGrow = true;
		killCount = 1000;
		fedCount = 1000;
		dayCount = 1000;
	}

	@Override
	protected void onFed() {
		fedCount++;
	}

	@Override
	protected boolean diamond(ServerLevel level, Player player) {
		if (!okToGrow) return false;
		growUp(level);
		return true;
	}

	@Override
	protected void ownAi(ServerLevel level) {
		if (killCount > 25 && fedCount > 10 && dayCount > 10) growUp(level);
	}

	private void growUp(ServerLevel level) {
		ThePrinceTeen teen = Bosses.THE_YOUNG_PRINCE.create(level, EntitySpawnReason.CONVERSION);
		if (teen == null) return;
		teen.snapTo(getX(), getY(), getZ(), random.nextFloat() * 360.0F, 0.0F);
		if (isTame() && getOwner() instanceof Player p) teen.tame(p);
		if (hasCustomName()) teen.setCustomName(getCustomName());
		level.addFreshEntity(teen);
		teen.playAmbientSound();
		discard();
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putBoolean("SpyroGrow", okToGrow);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		okToGrow = input.getBooleanOr("SpyroGrow", false);
	}

	/** getCanSpawnHere: always (it only comes from The King's death, eggs and growing back down). */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		return true;
	}
}
