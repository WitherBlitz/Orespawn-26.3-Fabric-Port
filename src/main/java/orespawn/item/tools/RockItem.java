package orespawn.item.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import orespawn.content.Tools;
import orespawn.entity.tools.RockBase;
import orespawn.entity.tools.ThrownRock;

/**
 * OreSpawn's rocks ({@code ItemRock}): 1 Small Rock, 2 Big Rock, 3 Flame Rock, 4 Poison Rock, 5 Slowness Rock, 6
 * Weakness Rock, 7 Painful Rock, 8 Explosive Rock, 9 Flame Crystal, 10 Poison Crystal, 11 Slowness Crystal, 12 Explosive
 * Crystal. Using one in the air throws it ({@link ThrownRock}), and so does a dispenser; using one on a block puts it
 * down there as a {@link RockBase}.
 */
public class RockItem extends Item implements ProjectileItem {
	private final int type;

	public RockItem(Properties properties, int type) {
		super(properties);
		this.type = type;
	}

	public int type() {
		return type;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 0.5F,
			0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
		if (level instanceof ServerLevel serverLevel) {
			Projectile.spawnProjectileFromRotation((l, owner, s) -> new ThrownRock(l, owner, s), serverLevel, stack, player, 0.0F, 1.5F, 1.0F);
		}
		stack.consume(1, player);
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (context.getLevel() instanceof ServerLevel level) {
			BlockPos pos = context.getClickedPos();
			RockBase rock = Tools.ROCK.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
			if (rock != null) {
				rock.snapTo(pos.getX() + 0.5, pos.getY() + 1.02, pos.getZ() + 0.5, level.getRandom().nextFloat() * 360.0F, 0.0F);
				level.addFreshEntity(rock);
				rock.placeRock(type);
			}
		}
		context.getItemInHand().consume(1, context.getPlayer());
		return InteractionResult.SUCCESS;
	}

	@Override
	public Projectile asProjectile(Level level, Position position, ItemStack stack, Direction direction) {
		return new ThrownRock(level, position.x(), position.y(), position.z(), stack);
	}
}
