package orespawn.item.weapons;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import orespawn.entity.weapons.Shots;

import java.util.function.Supplier;

/**
 * OreSpawn's throwable ammo (ItemLaserBall, ItemIceBall, ItemAcid): right click throws one as a snowball is
 * thrown (1.5 blocks a tick), using it up unless in creative; dispensers fire them too. The Robot Laser Charge sounds
 * like a firework launch, the others like a bow. (The Dead Irukandji and WaterDragon Charge are the Sea group's.)
 */
public class ThrownWeaponItem extends Item implements ProjectileItem {
	private final Supplier<? extends EntityType<? extends ThrowableItemProjectile>> type;
	private final SoundEvent sound;

	public ThrownWeaponItem(Properties properties, Supplier<? extends EntityType<? extends ThrowableItemProjectile>> type, SoundEvent sound) {
		super(properties);
		this.type = type;
		this.sound = sound;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		level.playSound(player, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 3.0F, 1.0F);
		if (level instanceof ServerLevel server) {
			ThrowableItemProjectile p = Shots.aimed(type.get(), level, player, 1.5F, 1.0F);
			p.setItem(stack);
			server.addFreshEntity(p);
		}
		stack.consume(1, player);
		return InteractionResult.SUCCESS;
	}

	@Override
	public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
		ThrowableItemProjectile p = type.get().create(level, EntitySpawnReason.DISPENSER);
		if (p == null) throw new IllegalStateException("Can't create " + type.get());
		p.setPos(pos.x(), pos.y(), pos.z());
		p.setItem(stack);
		return p;
	}
}
