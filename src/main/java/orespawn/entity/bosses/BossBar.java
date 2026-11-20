package orespawn.entity.bosses;

import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Mob;

/**
 * A boss health bar (26.3's ServerBossEvent, as the Wither's). OreSpawn 1.7.10 had none; the pack keeps one for each
 * boss. The boss creates it as a field and calls {@link #update} from its server AI step, {@link #add} / {@link #remove}
 * from startSeenByPlayer / stopSeenByPlayer, and {@link #rename} from setCustomName and readAdditionalSaveData.
 */
public final class BossBar {
	private final Mob mob;
	private final ServerBossEvent event;

	public BossBar(Mob mob, BossEvent.BossBarColor color) {
		this.mob = mob;
		this.event = new ServerBossEvent(Mth.createInsecureUUID(mob.getRandom()), mob.getDisplayName(), color, BossEvent.BossBarOverlay.PROGRESS);
	}

	public void update() {
		event.setProgress(Mth.clamp(mob.getHealth() / mob.getMaxHealth(), 0.0F, 1.0F));
	}

	public void add(ServerPlayer player) {
		event.addPlayer(player);
	}

	public void remove(ServerPlayer player) {
		event.removePlayer(player);
	}

	public void rename() {
		event.setName(mob.getDisplayName());
	}
}
