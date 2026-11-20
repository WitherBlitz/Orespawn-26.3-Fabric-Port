package orespawn.entity.oddities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import orespawn.entity.WanderGoal;
import orespawn.registry.ModItems;

import java.util.List;

/**
 * OreSpawn's Criminal (its class is "BandP", a bank robber): a burly thief who loiters around high-up villages by day
 * (y 100 or more, a villager within 36 blocks, one per 32 blocks). Every so often he goes for the nearest player in
 * survival or villager within 20 blocks, and when he lands a hit on a player he pockets one of their things: their
 * helmet, chestplate, leggings or boots first, then the last filled slot of their inventory. 100 health, 18 armour,
 * 1 damage. He keeps (and saves) what he stole, and won't despawn while he has it. Killed, he drops 10-14 emeralds,
 * half of them also 2-4 each of uranium and titanium nuggets, and everything he stole.
 * <p>
 * Not ported: 1.7.10's EntityAIMoveIndoors has no 26.3 equivalent; Girlfriend and Boyfriend (also his targets) are
 * left out of the pack.
 */
public class Criminal extends Monster {
	private NonNullList<ItemStack> stolen = NonNullList.withSize(100, ItemStack.EMPTY);
	private int gotStuff;
	/** BandP's "whatami": 0 or 1, rolled anew whenever he's created or loaded (as OreSpawn did); 0 also drops nuggets. */
	private int what = -1;

	public Criminal(EntityType<? extends Criminal> type, Level level) {
		super(type, level);
		this.xpReward = 1000;
	}

	/** get_mobstats("BandP", 100, 1, 18); speed 0.32. */
	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 100.0).add(Attributes.MOVEMENT_SPEED, 0.32)
			.add(Attributes.ATTACK_DAMAGE, 1.0).add(Attributes.ARMOR, 18.0);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new MoveThroughVillageGoal(this, 0.5, false, 4, () -> true));
		goalSelector.addGoal(1, WanderGoal.wanderALot(this, 16, 0.5));
		goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 10.0F));
		goalSelector.addGoal(3, new RandomLookAroundGoal(this));
		goalSelector.addGoal(4, new OpenDoorGoal(this, true));
	}

	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return !isPersistenceRequired() && gotStuff == 0;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.VILLAGER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.VILLAGER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.VILLAGER_DEATH;
	}

	@Override
	protected float getSoundVolume() {
		return 1.5F;
	}

	@Override
	public float getVoicePitch() {
		return 1.0F;
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		return InteractionResult.PASS;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (what < 0) what = random.nextInt(2);
		if (random.nextInt(12) != 1) return;
		LivingEntity e = Oddity.nearest(this, 20.0, 6.0, this::isSuitableTarget);
		if (e == null) return;
		lookAt(e, 10.0F, 10.0F);
		if (distanceToSqr(e) < 9.0) {
			doHurtTarget(level, e);
			if (e instanceof Player p) steal(p);
		} else {
			getNavigation().moveTo(e, 1.25);
		}
	}

	/** BandP: armour first (helmet down to boots), else the last filled inventory slot. */
	private void steal(Player p) {
		int k = -1;
		for (int i = 0; i < stolen.size() && k < 0; i++) {
			if (stolen.get(i).isEmpty()) k = i;
		}
		if (k < 0) return;
		for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
			ItemStack worn = p.getItemBySlot(slot);
			if (!worn.isEmpty()) {
				stolen.set(k, worn.copy());
				p.setItemSlot(slot, ItemStack.EMPTY);
				gotStuff++;
				return;
			}
		}
		Inventory inv = p.getInventory();
		for (int i = Inventory.INVENTORY_SIZE - 1; i >= 0; i--) {
			ItemStack s = inv.getItem(i);
			if (!s.isEmpty()) {
				stolen.set(k, s.copy());
				inv.setItem(i, ItemStack.EMPTY);
				gotStuff++;
				return;
			}
		}
	}

	private boolean isSuitableTarget(LivingEntity e) {
		if (e == this || !e.isAlive() || !getSensing().hasLineOfSight(e)) return false;
		if (e instanceof Player p) return !Oddity.creative(p);
		return e instanceof Villager;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		for (int i = 10 + random.nextInt(5); i > 0; i--) Oddity.dropRand(level, this, Items.EMERALD, 2, 1.0);
		if (what == 0) {
			for (int i = 2 + random.nextInt(3); i > 0; i--) {
				Oddity.dropRand(level, this, ModItems.URANIUM_NUGGET, 2, 1.0);
				Oddity.dropRand(level, this, ModItems.TITANIUM_NUGGET, 2, 1.0);
			}
		}
		for (int i = 0; i < stolen.size(); i++) {
			if (!stolen.get(i).isEmpty()) Oddity.dropRand(level, this, stolen.get(i), 2, 1.0);
			stolen.set(i, ItemStack.EMPTY);
		}
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		if (gotStuff != 0) ContainerHelper.saveAllItems(output.child("Inventory"), stolen);
		output.putInt("GotStuff", gotStuff);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		gotStuff = input.getIntOr("GotStuff", 0);
		stolen = NonNullList.withSize(100, ItemStack.EMPTY);
		if (gotStuff != 0) input.child("Inventory").ifPresent(i -> ContainerHelper.loadAllItems(i, stolen));
	}

	/**
	 * BandP.getCanSpawnHere: by day, at y 100 or higher, no other Criminal within 32 blocks, and a villager within 36.
	 * Spawners always may.
	 */
	public static boolean checkSpawn(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos,
			RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) return true;
		if (!level.getLevel().isBrightOutside() || pos.getY() < 100) return false;
		if (!level.getEntitiesOfClass(Criminal.class, new AABB(pos).inflate(32.0, 12.0, 32.0)).isEmpty()) return false;
		return !level.getEntitiesOfClass(Villager.class, new AABB(pos).inflate(36.0, 12.0, 36.0)).isEmpty();
	}
}
