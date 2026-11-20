package orespawn.entity;

import net.minecraft.world.entity.LivingEntity;

/** OreSpawn's MyUtils.isIgnoreable: the small things its hunting mobs don't bother with. Grows as more mobs are ported. */
public final class Ignored {
	private Ignored() {
	}

	public static boolean byHunters(LivingEntity e) {
		return e instanceof Butterfly || e instanceof Firefly || e instanceof Cricket || e instanceof Bird || e instanceof Mosquito
			|| e instanceof Dragonfly || e instanceof orespawn.entity.spooky.Ghost || e instanceof orespawn.entity.ants.Ant;
	}
}
