package orespawn.entity.weapons;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import orespawn.content.Weapons;

/**
 * OreSpawn's IceBall: a LaserBall that doesn't set things on fire, with a thinner spark trail and no red dust. It still
 * bursts and explodes (power 3) where it lands, as OreSpawn's did. Royalty (The King, The Queen and their family) is
 * never hurt. Creatures that throw them can make them ice makers ({@link #setIceMaker}): then 5 random blocks up to 3
 * away from the impact turn to ice (unbreakable blocks such as bedrock are left alone here).
 */
public class IceBall extends LaserBall {
	private int iceMaker;

	public IceBall(EntityType<? extends IceBall> type, Level level) {
		super(type, level);
	}

	public void setIceMaker(int iceMaker) {
		this.iceMaker = iceMaker;
	}

	@Override
	protected boolean iceBall() {
		return true;
	}

	@Override
	protected Item getDefaultItem() {
		return Weapons.ICE_BALL;
	}

	@Override
	protected boolean spares(Entity e) {
		return WeaponHooks.isRoyalty(e) || super.spares(e);
	}

	@Override
	protected void afterImpact(ServerLevel level, HitResult result) {
		if (iceMaker == 0) return;
		Vec3 at = result.getLocation();
		for (int i = 0; i < 5; i++) {
			int x = random.nextInt(4);
			if (random.nextInt(2) == 1) x = -x;
			int y = random.nextInt(4);
			if (random.nextInt(2) == 1) y = -y;
			int z = random.nextInt(4);
			if (random.nextInt(2) == 1) z = -z;
			BlockPos pos = new BlockPos((int) (x + at.x), (int) (y + at.y), (int) (z + at.z));
			if (level.getBlockState(pos).getDestroySpeed(level, pos) >= 0.0F) level.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
		}
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("IceMaker", iceMaker);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		iceMaker = input.getIntOr("IceMaker", 0);
	}
}
