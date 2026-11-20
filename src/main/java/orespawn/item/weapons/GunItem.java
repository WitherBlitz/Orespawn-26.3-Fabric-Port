package orespawn.item.weapons;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import orespawn.OreSpawn;
import orespawn.content.Weapons;
import orespawn.entity.weapons.LaserBall;
import orespawn.entity.weapons.Shots;
import orespawn.entity.weapons.ThunderBolt;
import orespawn.entity.sea.AttackSquid;
import org.jspecify.annotations.Nullable;

/**
 * OreSpawn's three guns, which fire on right click, kick the player back and use up one charge a shot; with one charge
 * left they stop firing (so they never break):
 * <ul>
 * <li>A Freakin' Ray Gun! (ItemRayGun, 50 charges): a Ray Gun shot ({@link LaserBall#setSpecial}) from beside the
 * player's head at 3 times a thrown ball's speed, with a loud firework launch; a strong kick (1.5 back, 0.3 up).
 * Redstone blocks recharge it in the crafting grid.</li>
 * <li>Thunder Staff (ItemThunderStaff, 50 charges): a {@link ThunderBolt} the same way; a lighter kick (0.5, 0.15).
 * It recharges by itself during thunderstorms, one charge every 50 ticks.</li>
 * <li>SquidZooka! (ItemSquidZooka, 100 charges): fires one of OreSpawn's Attack Squids at 3.6 blocks a tick from 2.5
 * blocks ahead with an explosion sound; kick 0.45, 0.1. Ink sacs recharge it.</li>
 * </ul>
 */
public class GunItem extends Item {
	public enum Kind {
		RAY_GUN, THUNDER_STAFF, SQUIDZOOKA
	}

	private final Kind kind;

	public GunItem(Properties properties, Kind kind) {
		super(properties);
		this.kind = kind;
	}

	private static @Nullable EntityType<?> attackSquid() {
		return BuiltInRegistries.ENTITY_TYPE.getOptional(OreSpawn.id("attack_squid")).orElse(null);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (stack.getMaxDamage() - stack.getDamageValue() <= 1) return InteractionResult.PASS;
		if (kind == Kind.SQUIDZOOKA && attackSquid() == null) return InteractionResult.PASS;
		double back = Math.toRadians(player.getYHeadRot() - 90.0F);
		switch (kind) {
			case RAY_GUN -> {
				level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 3.5F, 0.5F);
				if (level instanceof ServerLevel server) {
					LaserBall shot = Shots.aimed(Weapons.LASER_BALL, level, player, 1.5F, 1.0F);
					shot.setSpecial();
					Shots.fromGunBarrel(shot, player);
					server.addFreshEntity(shot);
				}
				if (level.isClientSide()) player.push(Math.cos(back) * 1.5, 0.3, Math.sin(back) * 1.5);
			}
			case THUNDER_STAFF -> {
				if (level instanceof ServerLevel server) {
					ThunderBolt bolt = Shots.aimed(Weapons.THUNDER_BOLT, level, player, 1.5F, 1.0F);
					Shots.fromGunBarrel(bolt, player);
					server.addFreshEntity(bolt);
				}
				if (level.isClientSide()) player.push(Math.cos(back) * 0.5, 0.15, Math.sin(back) * 0.5);
			}
			case SQUIDZOOKA -> {
				level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.5F,
					0.5F);
				if (level instanceof ServerLevel server) fireSquid(server, player);
				if (level.isClientSide()) player.push(Math.cos(back) * 0.45, 0.1, Math.sin(back) * 0.45);
			}
		}
		stack.hurtAndBreak(1, player, hand);
		return InteractionResult.SUCCESS;
	}

	private static void fireSquid(ServerLevel level, Player player) {
		EntityType<?> type = attackSquid();
		Entity squid = type == null ? null : type.create(level, EntitySpawnReason.TRIGGERED);
		if (squid == null) return;
		double yaw = Math.toRadians(player.getYHeadRot() + 15.0F);
		squid.snapTo(player.getX() - 2.5 * Math.sin(yaw), player.getY() + 1.65, player.getZ() + 2.5 * Math.cos(yaw),
			level.getRandom().nextFloat() * 360.0F, 0.0F);
		level.addFreshEntity(squid);
		if (squid instanceof AttackSquid shot) shot.setWasShot();
		float yRot = player.getYRot() * Mth.DEG_TO_RAD;
		float xRot = player.getXRot() * Mth.DEG_TO_RAD;
		var r = level.getRandom();
		squid.setDeltaMovement(new Vec3(-Mth.sin(yRot) * Mth.cos(xRot) * 3.6F, -Mth.sin(xRot) * 3.6F, Mth.cos(yRot) * Mth.cos(xRot) * 3.6F)
			.add((r.nextFloat() - r.nextFloat()) * 0.05, (r.nextFloat() - r.nextFloat()) * 0.05, (r.nextFloat() - r.nextFloat()) * 0.05));
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
		super.inventoryTick(stack, level, owner, slot);
		if (kind == Kind.THUNDER_STAFF && level.isRaining() && level.isThundering() && level.getGameTime() % 50 == 0 && stack.getDamageValue() > 0) {
			stack.setDamageValue(stack.getDamageValue() - 1);
		}
	}
}
