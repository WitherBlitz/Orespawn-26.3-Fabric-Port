package orespawn.item.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import orespawn.content.Tools;
import orespawn.entity.tools.ThrownCage;

/**
 * OreSpawn's Critter Cage ({@code CritterCage}). The empty cage is thrown like a snowball and catches the creature it
 * hits (see {@link ThrownCage}); a caged creature is let out by using its cage on a block: it appears on top of that
 * block, facing a random way, with a puff of smoke and a bang, and the empty cage drops beside it. A cage renamed in an
 * anvil names the creature.
 * <p>
 * Let-out creatures get the usual spawn setup ({@code finalizeSpawn}), which 26.3 needs for their variants; OreSpawn
 * only did that for horses and villagers. As in OreSpawn, a caged horse comes out as a donkey one time in ten.
 * OreSpawn's caged Easter Bunny named its creature wrongly and let nothing out; here it lets out the Easter Bunny.
 */
public class CritterCageItem extends Item {
	/** The cage's creature, or null for the empty cage. */
	private final CritterCages.Cage cage;

	public CritterCageItem(Properties properties, CritterCages.Cage cage) {
		super(properties);
		this.cage = cage;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (cage != null) return InteractionResult.PASS;
		ItemStack stack = player.getItemInHand(hand);
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 0.5F,
			0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
		if (level instanceof ServerLevel serverLevel) {
			Projectile.spawnProjectileFromRotation((l, owner, s) -> new ThrownCage(l, owner, s), serverLevel, stack, player, 0.0F, 1.5F, 1.0F);
		}
		stack.consume(1, player);
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (cage == null) return InteractionResult.PASS;
		EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(cage.releases()).orElse(null);
		if (type == null) return InteractionResult.PASS;
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();
		if (level.isClientSide()) {
			for (int i = 0; i < 6; i++) {
				level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 1.25, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
				level.addParticle(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 1.25, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
				level.addParticle(DustParticleOptions.REDSTONE, pos.getX() + 0.5, pos.getY() + 1.25, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
			}
			return InteractionResult.SUCCESS;
		}
		ServerLevel serverLevel = (ServerLevel) level;
		if (player != null) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.5F);
		}
		if (type == EntityTypes.HORSE && level.getRandom().nextInt(10) == 0) type = EntityTypes.DONKEY;
		Entity entity = type.create(serverLevel, EntitySpawnReason.SPAWN_ITEM_USE);
		ItemStack stack = context.getItemInHand();
		if (entity != null) {
			entity.snapTo(pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, level.getRandom().nextFloat() * 360.0F, 0.0F);
			if (entity instanceof Mob mob) {
				mob.yHeadRot = mob.getYRot();
				mob.yBodyRot = mob.getYRot();
				mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(mob.blockPosition()), EntitySpawnReason.SPAWN_ITEM_USE, null);
			}
			Component name = stack.get(DataComponents.CUSTOM_NAME);
			if (name != null) entity.setCustomName(name);
			serverLevel.addFreshEntity(entity);
			if (entity instanceof Mob mob) mob.playAmbientSound();
			entity.spawnAtLocation(serverLevel, new ItemStack(Tools.EMPTY_CRITTER_CAGE));
		}
		stack.consume(1, player);
		return InteractionResult.SUCCESS;
	}
}
