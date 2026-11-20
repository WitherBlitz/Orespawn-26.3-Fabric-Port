package orespawn.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes every registered orespawn: item, block, entity type, sound event and feature type to build/orespawn-ids.txt,
 * for tools/id_audit.py, which checks that the ids the groups look each other up by all exist.
 */
public class IdDumpTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		List<String> lines = new ArrayList<>();
		dump(lines, "item", BuiltInRegistries.ITEM);
		dump(lines, "block", BuiltInRegistries.BLOCK);
		dump(lines, "entity", BuiltInRegistries.ENTITY_TYPE);
		dump(lines, "sound", BuiltInRegistries.SOUND_EVENT);
		dump(lines, "feature", BuiltInRegistries.FEATURE_TYPE);
		try {
			Path root = Path.of(System.getProperty("user.dir")).toAbsolutePath();
			while (root != null && !Files.exists(root.resolve("build.gradle"))) root = root.getParent();
			Path out = root.resolve("build").resolve("orespawn-ids.txt");
			Files.createDirectories(out.getParent());
			Files.write(out, lines);
		} catch (IOException e) {
			throw new AssertionError("Couldn't write the id list", e);
		}
	}

	private static void dump(List<String> lines, String kind, Registry<?> registry) {
		for (var id : registry.keySet()) {
			if (id.getNamespace().equals("orespawn")) lines.add(kind + "\t" + id.getPath());
		}
	}
}
