/*
 * Checks data files (worldgen features and placed features, loot tables, recipes) against 26.3's own codecs, outside the
 * game. orespawn: ids are swapped for vanilla ones first, since OreSpawn isn't registered here; a placed feature's
 * orespawn: feature is read from worldgen/feature. Written for the Farming group (tools/farming_assets.py).
 *
 *   1. Get the compile classpath: ./run-gradle.sh -I <init script with a task printing sourceSets.main.compileClasspath>
 *   2. javac -proc:none -cp <classpath> -d <out> tools/FarmingDataCheck.java
 *   3. java -cp "<out>;<classpath>" FarmingDataCheck src/main/resources/data/orespawn <json files...>
 * Prints "ok"/"BAD <reason>" per file (through the game's logger) and "ALL OK" at the end.
 */
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.storage.loot.LootTable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** Decodes data files with 26.3's codecs (orespawn ids swapped for vanilla ones, since those aren't registered here). */
public class FarmingDataCheck {
	static Path data;

	public static void main(String[] args) throws Exception {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
		HolderLookup.Provider world = VanillaRegistries.createWorldLookup();
		HolderLookup.Provider all = VanillaRegistries.createReloadableLookup(world);
		RegistryOps<JsonElement> ops = all.createSerializationContext(JsonOps.INSTANCE);
		data = Path.of(args[0]);
		int bad = 0;
		for (int i = 1; i < args.length; i++) {
			Path p = Path.of(args[i]);
			String s = p.toString().replace('\\', '/');
			JsonElement json = JsonParser.parseString(Files.readString(p));
			Codec<?> codec;
			if (s.contains("/worldgen/placed_feature/")) {
				codec = PlacedFeature.DIRECT_CODEC;
				JsonObject o = json.getAsJsonObject();
				String ref = o.get("feature").getAsString();
				o.add("feature", JsonParser.parseString(Files.readString(data.resolve("worldgen/feature/" + ref.substring(ref.indexOf(':') + 1) + ".json"))));
			} else if (s.contains("/worldgen/feature/")) {
				codec = Feature.DIRECT_CODEC;
			} else if (s.contains("/loot_table/")) {
				codec = LootTable.DIRECT_CODEC;
			} else if (s.contains("/recipe/")) {
				codec = Recipe.DIRECT_CODEC;
				json.getAsJsonObject().remove("fabric:load_conditions");
			} else {
				continue;
			}
			json = sanitize(json, s.contains("/worldgen/"));
			DataResult<?> r = decode(codec, ops, json);
			if (r.error().isPresent()) {
				bad++;
				System.out.println("BAD " + s + ": " + r.error().get().message());
			} else {
				System.out.println("ok  " + s);
			}
		}
		System.out.println(bad == 0 ? "ALL OK" : bad + " BAD");
		System.exit(bad == 0 ? 0 : 1);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	static DataResult<?> decode(Codec codec, RegistryOps<JsonElement> ops, JsonElement json) {
		return codec.parse(ops, json);
	}

	static JsonElement sanitize(JsonElement e, boolean worldgen) {
		if (e.isJsonObject()) {
			JsonObject o = e.getAsJsonObject();
			if (o.has("id") && o.get("id").isJsonPrimitive() && o.get("id").getAsString().startsWith("orespawn:")) {
				JsonObject r = new JsonObject();
				r.addProperty("id", "minecraft:wheat");
				return r;
			}
			JsonObject r = new JsonObject();
			for (Map.Entry<String, JsonElement> en : o.entrySet()) r.add(en.getKey(), sanitize(en.getValue(), worldgen));
			return r;
		}
		if (e.isJsonArray()) {
			JsonArray a = new JsonArray();
			for (JsonElement x : e.getAsJsonArray()) a.add(sanitize(x, worldgen));
			return a;
		}
		if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isString()) {
			String v = e.getAsString();
			if (v.startsWith("orespawn:") && !v.startsWith("orespawn:blocks/")) return new JsonPrimitive("minecraft:stone");
		}
		return e;
	}
}
