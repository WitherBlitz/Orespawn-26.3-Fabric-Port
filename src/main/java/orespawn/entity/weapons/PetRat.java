package orespawn.entity.weapons;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import orespawn.entity.Flounder;
import orespawn.entity.Ignored;
import orespawn.entity.ModEntities;
import orespawn.entity.Rat;
import orespawn.entity.Skate;
import orespawn.entity.Whale;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

/**
 * A rat made by OreSpawn's Rat Sword: OreSpawn's Rat with an owner ({@code Rat.setOwner}). It's the same rat (model,
 * stats, sounds, rotten flesh) but, with OreSpawn's default RatPlayerFriendly and RatPetFriendly, it never goes for a
 * player, nor anyone's tamed pet; it hunts everything else within 9 blocks as a wild rat does (not other rats, the bugs
 * and birds hunters ignore, Irukandji, Skates, Whales, Flounders, Ghosts, Ghost Skeletons or Dungeon Beasts). With
 * nothing to hunt it runs back to its owner when more than 8 blocks away and jumps to them past 16. It never despawns.
 * <p>
 * OreSpawn looked the owner up by name with the UUID it stored, so its pet rats never actually found their owner;
 * following works here as it was meant to.
 */
public class PetRat extends Rat {
	private static final EntityDataAccessor<Boolean> HUNTING = SynchedEntityData.defineId(PetRat.class, EntityDataSerializers.BOOLEAN);
	private @Nullable UUID owner;

	public PetRat(EntityType<? extends PetRat> type, Level level) {
		super(type, level);
	}

	/** Rat.setOwner: only a player can own rats. */
	public void setOwner(@Nullable LivingEntity e) {
		if (e instanceof Player p) owner = p.getUUID();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(HUNTING, false);
	}

	@Override
	public boolean attacking() {
		return entityData.get(HUNTING);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		if (random.nextInt(200) == 1) setLastHurtByMob(null);
		if (random.nextInt(5) == 1) {
			LivingEntity prey = level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(9.0, 2.0, 9.0), this::isPrey).stream()
				.min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
			if (prey != null) {
				entityData.set(HUNTING, true);
				getNavigation().moveTo(prey, 1.25);
				if (distanceToSqr(prey) < 4.0 && (random.nextInt(8) == 0 || random.nextInt(7) == 1)) doHurtTarget(level, prey);
			} else {
				entityData.set(HUNTING, false);
				Player p = owner == null ? null : level.getPlayerByUUID(owner);
				if (p != null) {
					if (distanceToSqr(p) > 64.0) getNavigation().moveTo(p, 1.75);
					if (distanceToSqr(p) > 256.0) {
						setPos(p.getX() + random.nextFloat() - random.nextFloat(), p.getY(), p.getZ() + random.nextFloat() - random.nextFloat());
					}
				}
			}
		}
		if (random.nextInt(250) == 1) heal(1.0F);
	}

	private boolean isPrey(LivingEntity e) {
		if (e == this || !e.isAlive() || Ignored.byHunters(e) || e instanceof Rat) return false;
		if (e instanceof Skate || e instanceof Whale || e instanceof Flounder) return false;
		for (String id : new String[] {"irukandji", "ghost", "ghost_pumpkin_skelly", "dungeon_beast"}) if (WeaponHooks.is(e, id)) return false;
		if (!getSensing().hasLineOfSight(e)) return false;
		if (e instanceof Player p) {
			if (p.isCreative() || p.isSpectator()) return false;
			if (owner != null) return false;
		}
		if (owner != null && e instanceof TamableAnimal t) {
			if (t.isTame()) return false;
			if (t.getOwnerReference() != null && Objects.equals(t.getOwnerReference().getUUID(), owner)) return false;
		}
		return true;
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return owner == null && super.removeWhenFarAway(distSqr);
	}

	/** A pet rat drops what a wild one does. */
	@Override
	protected void dropFromLootTable(ServerLevel level, DamageSource source, boolean playerKilled) {
		ModEntities.RAT.getDefaultLootTable().ifPresent(table -> dropFromLootTable(level, source, playerKilled, table));
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		if (owner != null) output.putString("MyOwner", owner.toString());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		String s = input.getStringOr("MyOwner", "");
		try {
			owner = s.isEmpty() ? null : UUID.fromString(s);
		} catch (IllegalArgumentException e) {
			owner = null;
		}
	}
}
