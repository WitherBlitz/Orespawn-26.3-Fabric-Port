package orespawn.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import orespawn.OreSpawn;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * OreSpawn's own sounds, under its own event names ({@code orespawn:<name>}). Their .ogg files and sounds.json come from
 * the original OreSpawn 1.7.10 file at runtime (see orespawn.client.original.OriginalPack).
 */
public final class ModSounds {
	private static final Map<String, SoundEvent> EVENTS = new LinkedHashMap<>();

	/**
	 * Registers every sound event the port uses (orespawn/original/sound_events.txt), so a content group looks its sounds
	 * up with {@link #get}.
	 */
	static {
		try (var in = ModSounds.class.getResourceAsStream("/orespawn/original/sound_events.txt")) {
			for (String name : new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().filter(l -> !l.isBlank()).toList()) {
				EVENTS.put(name, Registry.register(BuiltInRegistries.SOUND_EVENT, OreSpawn.id(name), SoundEvent.createVariableRangeEvent(OreSpawn.id(name))));
			}
		} catch (Exception e) {
			throw new IllegalStateException("OreSpawn's sound event list (orespawn/original/sound_events.txt) is missing or broken", e);
		}
	}

	public static final SoundEvent BIRDS = get("birds");
	public static final SoundEvent DUCK_HURT = get("duck_hurt");
	public static final SoundEvent CRICKET = get("cricket");
	public static final SoundEvent SCORPION_HIT = get("scorpion_hit");
	public static final SoundEvent BIG_SPLAT = get("big_splat");
	public static final SoundEvent RAT_LIVE = get("ratlive");
	public static final SoundEvent RAT_HIT = get("rathit");
	public static final SoundEvent RAT_DEAD = get("ratdead");
	public static final SoundEvent CRYO_DEATH = get("cryo_death");
	public static final SoundEvent CHAINSAW = get("chainsaw");

	private ModSounds() {
	}

	public static SoundEvent get(String name) {
		SoundEvent e = EVENTS.get(name);
		if (e == null) throw new IllegalArgumentException("No OreSpawn sound " + name);
		return e;
	}

	public static void init() {
		// Static initialisers do the work.
	}
}
