package orespawn.item.sea;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;

/**
 * OreSpawn's ItemIrukandji, ItemSunspotUrchin and ItemWaterBall: right click throws one (at a snowball's speed, as
 * 1.7.10's EntityThrowable did) with a bow sound, and a dispenser fires them too (OreSpawn's MyDispenserBehavior...).
 */
public class SeaThrowableItem extends Item implements ProjectileItem {
	/** Makes the projectile at a position (dispensers). */
	public interface AtPosition {
		Projectile create(Level level, double x, double y, double z, ItemStack stack);
	}

	private final Projectile.ProjectileFactory<? extends Projectile> thrown;
	private final AtPosition dispensed;
	/** OreSpawn's bow-sound volume; the pitch is fixed at {@code pitch} when positive, else random as for a snowball. */
	private final float volume;
	private final float pitch;

	public SeaThrowableItem(Item.Properties properties, Projectile.ProjectileFactory<? extends Projectile> thrown, AtPosition dispensed, float volume,
			float pitch) {
		super(properties);
		this.thrown = thrown;
		this.dispensed = dispensed;
		this.volume = volume;
		this.pitch = pitch;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		float p = pitch > 0.0F ? pitch : 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F);
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, volume, p);
		if (level instanceof ServerLevel serverLevel) {
			Projectile.spawnProjectileFromRotation(thrown, serverLevel, stack, player, 0.0F, 1.5F, 1.0F);
		}
		player.awardStat(Stats.ITEM_USED.get(this));
		stack.consume(1, player);
		return InteractionResult.SUCCESS;
	}

	@Override
	public Projectile asProjectile(Level level, Position position, ItemStack stack, Direction direction) {
		return dispensed.create(level, position.x(), position.y(), position.z(), stack);
	}
}
