package orespawn.entity.spooky;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import orespawn.registry.ModSounds;

/**
 * OreSpawn's Ghost Pumpkin Skelly: a bigger ghost, a skeleton in a shirt with a pumpkin head and chains hanging from its
 * arms. It drifts and haunts players exactly like the Ghost, rattling its chains; 5 health. Its head now and then spins
 * round (a model animation).
 */
public class GhostSkelly extends Ghost {
	public GhostSkelly(EntityType<? extends GhostSkelly> type, Level level) {
		super(type, level);
		this.xpReward = 10;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return attributes(5.0);
	}

	@Override
	protected SoundEvent moan() {
		return ModSounds.get("chain_rattles");
	}

	@Override
	protected float getSoundVolume() {
		return 0.5F;
	}
}
