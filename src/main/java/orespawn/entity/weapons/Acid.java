package orespawn.entity.weapons;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import orespawn.content.Weapons;

/**
 * OreSpawn's Acid: a LaserBall with no trail and no burst. It does 16 thrown damage and sets the target on fire for
 * 1 s; OreSpawn's Jumpy Bugs and Spit Bugs (which spit it) aren't hurt by it.
 */
public class Acid extends LaserBall {
	public Acid(EntityType<? extends Acid> type, Level level) {
		super(type, level);
	}

	@Override
	protected boolean acid() {
		return true;
	}

	@Override
	protected Item getDefaultItem() {
		return Weapons.ACID;
	}
}
