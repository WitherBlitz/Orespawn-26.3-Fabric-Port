package orespawn.item.weapons;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import orespawn.content.Weapons;
import orespawn.entity.weapons.IrukandjiArrow;

/** OreSpawn's Irukandji Arrow: the Skate String Bow's ammo; dispensers fire it (MyDispenserBehaviorArrow) and it can be picked up. */
public class IrukandjiArrowItem extends Item implements ProjectileItem {
	public IrukandjiArrowItem(Properties properties) {
		super(properties);
	}

	@Override
	public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
		IrukandjiArrow arrow = new IrukandjiArrow(Weapons.IRUKANDJI_ARROW_ENTITY, level);
		arrow.setPos(pos.x(), pos.y(), pos.z());
		arrow.pickup = AbstractArrow.Pickup.ALLOWED;
		return arrow;
	}
}
