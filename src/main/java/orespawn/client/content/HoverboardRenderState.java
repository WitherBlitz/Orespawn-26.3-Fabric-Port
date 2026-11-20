package orespawn.client.content;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;

/** What the Hoverboard's renderer reads: its facing, the wobble after a hit, and its colour's texture. */
@Environment(EnvType.CLIENT)
public class HoverboardRenderState extends EntityRenderState {
	public float yRot;
	public float hurtTime;
	public float damageTime;
	public int hurtDir;
	public Identifier texture;
}
