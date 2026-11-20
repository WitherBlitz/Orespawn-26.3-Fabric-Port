package orespawn.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/** One render state for OreSpawn's creatures: the few things their models and renderers read from the entity. */
@Environment(EnvType.CLIENT)
public class OreSpawnRenderState extends LivingEntityRenderState {
	public Identifier texture;
	public int variant;
	public boolean sitting;
	public boolean singing;
	public boolean attacking;
	public boolean blink;
	/** Vertical speed in blocks per tick (the frog spreads its legs in the air). */
	public float verticalSpeed;
	/** Drawn again with the enchantment glint (the Enchanted Golden Apple Cow). */
	public boolean glint;
	/** Drawn again with the charged-creeper swirl (the Luna Moth). */
	public boolean swirl;
	/** This mob's own size on top of the renderer's scale (a crab's size, say); 1 for most. */
	public float scale = 1.0F;
	/** Someone is riding it. */
	public boolean ridden;
	public boolean inWater;
	/** A mob-specific yes/no its model or renderer reads (each creature's renderer says what it means). */
	public boolean flag;
	/** A mob-specific number its model or renderer reads (each creature's renderer says what it means). */
	public float value;
}
