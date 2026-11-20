package orespawn.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.CameraType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import orespawn.entity.ModEntities;
import orespawn.registry.ModBlocks;
import orespawn.registry.ModItems;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Milestone 1 (OreSpawn's egg ores for vanilla mobs, its ores, and its emerald, ruby, amethyst, lapis and Ultimate gear):
 * <ul>
 * <li>every block and item has a model, texture and English name;</li>
 * <li>egg ores drop themselves (by hand too), and a water bucket plus an egg ore crafts that mob's spawn egg, which hatches
 * it; the other ores drop what OreSpawn's did;</li>
 * <li>OreSpawn's damage, auto-enchantments, the Ultimate Hoe's 3x3 tilling and the Ultimate Pickaxe's bonus ingots;</li>
 * <li>the ores generate in a real overworld and Nether (with -PallMods, on the pack's own terrain);</li>
 * <li>screenshots of every block, every item, and each armor set on armor stands, zombies and baby zombies.</li>
 * </ul>
 * Screenshots are copied to the project's screenshots folder (plain/ or allmods/).
 */
public class OreSpawnTest implements FabricClientGameTest {
	private final List<String> problems = new ArrayList<>();
	private ClientGameTestContext context;
	private final boolean allMods = System.getenv("ORESPAWN_ALLMODS") != null;

	private static void log(String line) {
		System.out.println("OS-TEST " + line);
	}

	private void check(boolean ok, String what) {
		log((ok ? "ok   " : "FAIL ") + what);
		if (!ok) problems.add(what);
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		this.context = context;
		log("mods=" + FabricLoader.getInstance().getAllMods().size() + " allMods=" + allMods + " fixedworld="
			+ FabricLoader.getInstance().isModLoaded("fixedworld") + " sodium=" + FabricLoader.getInstance().isModLoaded("sodium"));
		checkAssets();
		try (TestSingleplayerContext sp = context.worldBuilder().create()) {
			TestServerContext server = sp.getServer();
			for (String cmd : List.of("gamerule spawn_mobs false", "time set noon", "weather clear", "difficulty easy",
				"gamerule advance_time false", "forceload add -16 -80 48 16")) {
				server.runCommand(cmd);
			}
			int ground = server.computeOnServer(s -> s.overworld().getHeight(Heightmap.Types.MOTION_BLOCKING, 0, 0));
			log("ground y=" + ground);
			checkDropsAndRecipes(server, ground);
			checkGear(server);
			checkMining(server, ground);
			checkUltimateTools(server, ground);
			context.runOnClient(mc -> {
				if (!mc.gui.hud.isHidden()) mc.gui.hud.toggle();
			});
			blockWall(server, sp, ground);
			itemWall(server, sp, ground);
			armorShots(server, sp, ground);
			heldShots(server, sp, ground);
		}
		try (TestSingleplayerContext sp = context.worldBuilder().setUseConsistentSettings(false).create()) {
			checkWorldgen(sp.getServer());
		}
		if (!problems.isEmpty()) throw new AssertionError("OreSpawn test failed:\n - " + String.join("\n - ", problems));
		log("ALL PASSED");
	}

	// ---- Assets ----

	private static com.google.gson.JsonElement readJson(net.minecraft.server.packs.resources.ResourceManager rm, String namespace, String path) {
		var res = rm.getResource(Identifier.fromNamespaceAndPath(namespace, path));
		if (res.isEmpty()) return null;
		try (var reader = res.get().openAsReader()) {
			return com.google.gson.JsonParser.parseReader(reader);
		} catch (Exception e) {
			return null;
		}
	}

	/** The first model a blockstate or item definition names ("namespace:path"), or null. */
	private static String firstModel(net.minecraft.server.packs.resources.ResourceManager rm, String path) {
		return findModel(readJson(rm, "orespawn", path));
	}

	private static String findModel(com.google.gson.JsonElement e) {
		if (e == null) return null;
		if (e.isJsonObject()) {
			var o = e.getAsJsonObject();
			if (o.has("model") && o.get("model").isJsonPrimitive()) return o.get("model").getAsString();
			for (var entry : o.entrySet()) {
				String m = findModel(entry.getValue());
				if (m != null) return m;
			}
		} else if (e.isJsonArray()) {
			for (var x : e.getAsJsonArray()) {
				String m = findModel(x);
				if (m != null) return m;
			}
		}
		return null;
	}

	/** Whether a model's textures (or its parent's, for orespawn parents) all exist; vanilla parents count as present. */
	private static boolean texturesExist(net.minecraft.server.packs.resources.ResourceManager rm, String model, int depth) {
		if (model == null || depth > 8) return false;
		Identifier id = Identifier.parse(model);
		if (!id.getNamespace().equals("orespawn")) return true;
		var json = readJson(rm, "orespawn", "models/" + id.getPath() + ".json");
		if (json == null || !json.isJsonObject()) return false;
		var o = json.getAsJsonObject();
		boolean any = false;
		if (o.has("textures")) {
			for (var t : o.getAsJsonObject("textures").entrySet()) {
				String v = t.getValue().getAsString();
				if (v.startsWith("#")) continue;
				Identifier tex = Identifier.parse(v);
				if (rm.getResource(Identifier.fromNamespaceAndPath(tex.getNamespace(), "textures/" + tex.getPath() + ".png")).isEmpty()) return false;
				any = true;
			}
		}
		if (o.has("parent")) {
			Identifier parent = Identifier.parse(o.get("parent").getAsString());
			if (parent.getNamespace().equals("orespawn")) return texturesExist(rm, parent.toString(), depth + 1) || any;
			return true;
		}
		// No parent and no textures: a group's own entity-drawn item (special model) is fine as long as the model exists.
		return any || o.has("elements");
	}

	private void checkAssets() {
		List<String> missing = context.computeOnClient(mc -> {
			List<String> out = new ArrayList<>();
			Language lang = Language.getInstance();
			var rm = mc.getResourceManager();
			// Each block's and item's textures are followed from its blockstate or item definition through its models.
			for (String name : ModBlocks.BLOCKS.keySet()) {
				if (rm.getResource(Identifier.fromNamespaceAndPath("orespawn", "items/" + name + ".json")).isEmpty()) out.add("items/" + name);
				if (rm.getResource(Identifier.fromNamespaceAndPath("orespawn", "blockstates/" + name + ".json")).isEmpty()) out.add("blockstates/" + name);
				else if (!texturesExist(rm, firstModel(rm, "blockstates/" + name + ".json"), 0)) out.add("texture " + name);
				if (!lang.has(BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("orespawn", name)).getDescriptionId())) out.add("name of " + name);
			}
			for (String name : ModItems.ITEMS.keySet()) {
				if (rm.getResource(Identifier.fromNamespaceAndPath("orespawn", "items/" + name + ".json")).isEmpty()) out.add("items/" + name);
				else if (!texturesExist(rm, firstModel(rm, "items/" + name + ".json"), 0)) out.add("texture " + name);
				if (!lang.has(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("orespawn", name)).getDescriptionId())) out.add("name of " + name);
			}
			// The Weapons group's armor sets (tools/content/weapons.json "armor_sets") as well as milestone 1's.
			for (String set : List.of("emerald", "ruby", "amethyst", "lapis", "ultimate", "experience", "lavaeel", "mobzilla", "mothscale", "peacock",
				"pink", "queen", "royal", "tigerseye")) {
				for (String layer : List.of("humanoid", "humanoid_leggings", "humanoid_baby")) {
					String path = "textures/entity/equipment/" + layer + "/" + set + ".png";
					if (rm.getResource(Identifier.fromNamespaceAndPath("orespawn", path)).isEmpty()) out.add(path);
				}
			}
			return out;
		});
		check(missing.isEmpty(), "every block and item has a model, texture and name, and every armor set its worn textures ("
			+ ModBlocks.BLOCKS.size() + " blocks, " + ModItems.ITEMS.size() + " items); missing: " + missing);
		String names = context.computeOnClient(mc -> Language.getInstance().getOrDefault("block.orespawn.zombie_egg_ore") + " / "
			+ Language.getInstance().getOrDefault("item.orespawn.ultimate_sword"));
		check(names.equals("Ancient Dried Zombie Spawn Egg / The Ultimate Sword"), "OreSpawn's names: " + names);
	}

	// ---- Drops and recipes ----

	private void checkDropsAndRecipes(TestServerContext server, int ground) {
		List<String> bad = server.computeOnServer(s -> {
			ServerLevel level = s.overworld();
			BlockPos pos = new BlockPos(0, ground + 20, 40);
			ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
			List<String> out = new ArrayList<>();
			for (ModBlocks.EggOre ore : ModBlocks.EGG_ORES) {
				BlockState state = ore.block().defaultBlockState();
				List<ItemStack> drops = Block.getDrops(state, level, pos, null, null, ItemStack.EMPTY);
				if (drops.size() != 1 || !drops.getFirst().is(ore.block().asItem())) out.add(ore.block() + " -> " + drops);
				Item egg = BuiltInRegistries.ITEM.getValue(ore.spawnEgg());
				CraftingInput input = CraftingInput.of(2, 1, List.of(new ItemStack(Items.WATER_BUCKET), new ItemStack(ore.block())));
				ItemStack crafted = s.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level)
					.map(r -> r.value().assemble(input)).orElse(ItemStack.EMPTY);
				if (!crafted.is(egg) || egg == Items.AIR) out.add("water bucket + " + ore.mob() + " egg ore -> " + crafted);
			}
			for (int i = 0; i < 20; i++) {
				expect(out, level, pos, pick, "ruby_ore", ModItems.RUBY, 1, 2);
				expect(out, level, pos, pick, "amethyst_ore", ModItems.AMETHYST, 1, 2);
			}
			expect(out, level, pos, pick, "uranium_ore", ModBlocks.URANIUM_ORE.asItem(), 1, 1);
			expect(out, level, pos, pick, "titanium_ore", ModBlocks.TITANIUM_ORE.asItem(), 1, 1);
			expect(out, level, pos, pick, "salt_ore", ModBlocks.SALT_ORE.asItem(), 1, 1);
			expect(out, level, pos, pick, "ruby_block", ModBlocks.RUBY_BLOCK.asItem(), 1, 1);
			return out;
		});
		check(bad.isEmpty(), "egg ores drop themselves and craft with a water bucket into their mob's spawn egg (" + ModBlocks.EGG_ORES.size() + " mobs); ruby and "
			+ "amethyst ore drop 1-2 gems; uranium, titanium and salt ore drop themselves: " + bad);
	}

	private static void expect(List<String> out, ServerLevel level, BlockPos pos, ItemStack tool, String block, Item item, int min, int max) {
		List<ItemStack> drops = Block.getDrops(ModBlocks.byName(block).defaultBlockState(), level, pos, null, null, tool);
		boolean ok = drops.size() == 1 && drops.getFirst().is(item) && drops.getFirst().getCount() >= min && drops.getFirst().getCount() <= max;
		if (!ok) out.add(block + " -> " + drops);
	}

	// ---- Gear stats and auto-enchantments ----

	private void checkGear(TestServerContext server) {
		server.runCommand("gamemode survival @p");
		server.runCommand("clear @p");
		server.runOnServer(s -> {
			ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
			for (String name : List.of("ruby_sword", "ultimate_sword", "ultimate_pickaxe", "emerald_pickaxe", "ultimate_helmet", "ultimate_boots",
				"lapis_helmet")) {
				p.getInventory().add(new ItemStack(ModItems.byName(name)));
			}
		});
		context.waitTicks(3);
		Map<String, String> found = server.computeOnServer(s -> {
			Map<String, String> out = new TreeMap<>();
			ServerPlayer p = s.getPlayerList().getPlayers().getFirst();
			var lookup = s.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
			for (ItemStack stack : p.getInventory().getNonEquipmentItems()) {
				if (stack.isEmpty()) continue;
				StringBuilder b = new StringBuilder();
				for (var key : List.of(Enchantments.SHARPNESS, Enchantments.LOOTING, Enchantments.FIRE_ASPECT, Enchantments.EFFICIENCY,
					Enchantments.FORTUNE, Enchantments.SILK_TOUCH, Enchantments.PROTECTION, Enchantments.FEATHER_FALLING,
					Enchantments.RESPIRATION, Enchantments.PROJECTILE_PROTECTION)) {
					Holder<Enchantment> h = lookup.getOrThrow(key);
					int lvl = EnchantmentHelper.getItemEnchantmentLevel(h, stack);
					if (lvl > 0) b.append(key.identifier().getPath()).append(lvl).append(' ');
				}
				out.put(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath(), b.toString().trim());
			}
			return out;
		});
		log("auto-enchantments: " + found);
		check("sharpness5 looting3 fire_aspect2".equals(found.get("ultimate_sword")), "the Ultimate Sword enchants itself: "
			+ found.get("ultimate_sword"));
		check("efficiency5 fortune5".equals(found.get("ultimate_pickaxe")), "the Ultimate Pickaxe has Efficiency V and Fortune V: "
			+ found.get("ultimate_pickaxe"));
		check("silk_touch1".equals(found.get("emerald_pickaxe")), "the Emerald Pickaxe has Silk Touch: " + found.get("emerald_pickaxe"));
		check("protection5 respiration2 projectile_protection5".equals(found.get("ultimate_helmet")),
			"the Ultimate Helmet enchants itself: " + found.get("ultimate_helmet"));
		check("protection5 feather_falling3 projectile_protection5".equals(found.get("ultimate_boots")),
			"the Ultimate Boots enchant themselves: " + found.get("ultimate_boots"));
		check("protection1 respiration1 projectile_protection1".equals(found.get("lapis_helmet")),
			"the Lapis Lazuli Helmet enchants itself: " + found.get("lapis_helmet"));
		check("".equals(found.get("ruby_sword")), "the Ruby Sword has no enchantments of its own: " + found.get("ruby_sword"));

		StringBuilder b = new StringBuilder();
		for (String name : List.of("emerald_sword", "amethyst_sword", "ruby_sword", "ultimate_sword", "ultimate_pickaxe", "ruby_axe")) {
			b.append(name).append('=').append(1 + sum(name, Attributes.ATTACK_DAMAGE)).append(' ');
		}
		double armor = 0;
		for (String piece : List.of("helmet", "chestplate", "leggings", "boots")) armor += sum("ultimate_" + piece, Attributes.ARMOR);
		b.append("ultimate_armor=").append(armor);
		String damage = b.toString();
		log("attack damage (base 1 included) and armor: " + damage);
		check(damage.equals("emerald_sword=11.0 amethyst_sword=16.0 ruby_sword=21.0 ultimate_sword=41.0 ultimate_pickaxe=39.0 ruby_axe=20.0 "
			+ "ultimate_armor=34.0"), "OreSpawn's damage (sword 4 + material, pickaxe 2 +, axe 3 +, plus the base 1) and armor (Ultimate "
			+ "6+12+10+6; 26.3 caps the total at 30): " + damage);
		server.runCommand("clear @p");
	}

	/** The item's own modifiers for an attribute, summed (what it adds when held or worn). */
	private static double sum(String item, Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute) {
		ItemStack stack = new ItemStack(ModItems.byName(item));
		double total = 0;
		for (var e : stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers()) {
			if (e.attribute().equals(attribute)) total += e.modifier().amount();
		}
		return total;
	}

	// ---- Digging an egg ore, hatching its egg ----

	private void checkMining(TestServerContext server, int ground) {
		BlockPos ore = new BlockPos(3, ground + 1, 3);
		server.runCommand("setblock " + ore.getX() + " " + ore.getY() + " " + ore.getZ() + " orespawn:zombie_egg_ore");
		context.waitTicks(2);
		String result = server.computeOnServer(s -> {
			ServerPlayer player = s.getPlayerList().getPlayers().getFirst();
			player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
			player.teleportTo(ore.getX() + 0.5, ore.getY() + 1, ore.getZ() + 2.5);
			boolean broke = player.gameMode.destroyBlock(ore);
			return broke + ":" + s.overworld().getBlockState(ore).isAir();
		});
		context.waitTicks(5);
		String drops = server.computeOnServer(s -> {
			StringBuilder b = new StringBuilder();
			for (ItemEntity e : s.overworld().getEntitiesOfClass(ItemEntity.class, new AABB(ore).inflate(3))) {
				b.append(BuiltInRegistries.ITEM.getKey(e.getItem().getItem())).append(' ');
			}
			return b.toString().trim();
		});
		check(result.equals("true:true") && drops.equals("orespawn:zombie_egg_ore"),
			"a player digging an Ancient Dried Zombie Spawn Egg by hand gets the block (broke:air=" + result + ", drops=" + drops + ")");
		server.runCommand("kill @e[type=item]");
		server.runCommand("item replace entity @p weapon.mainhand with minecraft:zombie_spawn_egg");
		context.waitTicks(2);
		boolean hatched = server.computeOnServer(s -> {
			ServerPlayer player = s.getPlayerList().getPlayers().getFirst();
			BlockPos floor = ore.below();
			player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(floor).add(0, 0.5, 0), Direction.UP, floor, false)));
			return !s.overworld().getEntitiesOfClass(Zombie.class, new AABB(ore).inflate(3)).isEmpty();
		});
		check(hatched, "the crafted zombie spawn egg hatches a zombie");
		server.runCommand("kill @e[type=zombie]");
		server.runCommand("clear @p");
	}

	// ---- Ultimate Hoe and Pickaxe ----

	private void checkUltimateTools(TestServerContext server, int ground) {
		int y = ground - 1;
		server.runCommand("fill 10 " + y + " 10 12 " + y + " 12 minecraft:grass_block");
		server.runCommand("fill 10 " + (y + 1) + " 10 12 " + (y + 3) + " 12 minecraft:air");
		context.waitTicks(2);
		int farmland = server.computeOnServer(s -> {
			ServerPlayer player = s.getPlayerList().getPlayers().getFirst();
			player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.byName("ultimate_hoe")));
			BlockPos centre = new BlockPos(11, y, 11);
			player.teleportTo(11.5, y + 1, 13.5);
			player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(centre).add(0, 0.5, 0), Direction.UP, centre, false)));
			int n = 0;
			for (BlockPos p : BlockPos.betweenClosed(10, y, 10, 12, y, 12)) if (s.overworld().getBlockState(p).is(Blocks.FARMLAND)) n++;
			return n;
		});
		check(farmland == 9, "one click with the Ultimate Hoe tills the 3x3 around it: " + farmland + " farmland");

		int ingots = server.computeOnServer(s -> {
			ServerPlayer player = s.getPlayerList().getPlayers().getFirst();
			player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.byName("ultimate_pickaxe")));
			BlockPos p = new BlockPos(14, y + 1, 14);
			player.teleportTo(14.5, y + 1, 16.5);
			for (int i = 0; i < 24; i++) {
				s.overworld().setBlockAndUpdate(p, Blocks.IRON_ORE.defaultBlockState());
				player.gameMode.destroyBlock(p);
			}
			int n = 0;
			for (ItemEntity e : s.overworld().getEntitiesOfClass(ItemEntity.class, new AABB(p).inflate(3))) {
				if (e.getItem().is(Items.IRON_INGOT)) n += e.getItem().getCount();
			}
			return n;
		});
		check(ingots > 0, "the Ultimate Pickaxe drops bonus iron ingots from iron ore: " + ingots + " from 24 ores");
		server.runCommand("kill @e[type=item]");
		server.runCommand("clear @p");
	}

	// ---- Worldgen ----

	private void checkWorldgen(TestServerContext server) {
		Map<String, Integer> overworld = server.computeOnServer(s -> scan(s, s.overworld(), 5));
		log("overworld ores near spawn (11x11 chunks): " + overworld);
		int eggs = overworld.entrySet().stream().filter(e -> e.getKey().endsWith("_egg_ore")).mapToInt(Map.Entry::getValue).sum();
		long kinds = overworld.keySet().stream().filter(k -> k.endsWith("_egg_ore")).count();
		check(eggs >= 20, "egg ores generate in the overworld's stone: " + eggs + " blocks of " + kinds + " mobs");
		for (String ore : List.of("amethyst_ore", "uranium_ore", "titanium_ore", "salt_ore")) {
			check(overworld.getOrDefault(ore, 0) > 0, ore + " generates in the overworld: " + overworld.getOrDefault(ore, 0));
		}
		check(!overworld.containsKey("ruby_ore"), "no ruby ore in the overworld (OreSpawn only put it in the Nether and its own dimension)");
		Map<String, Integer> nether = server.computeOnServer(s -> scan(s, s.getLevel(Level.NETHER), 3));
		log("nether ores (7x7 chunks): " + nether);
		// OreSpawn's Nether worldgen: ruby ore, Lava Foam veins, mosquito plants and the nether ant nests; nothing else of ours.
		java.util.Set<String> netherBlocks = java.util.Set.of("ruby_ore", "lava_foam", "mosquito_plant", "red_ant_nest", "ant_nest", "rainbow_ant_nest",
			"unstable_ant_nest", "termite_nest", "red_ant_troll_block", "termite_troll_block");
		check(nether.getOrDefault("ruby_ore", 0) > 0 && nether.getOrDefault("lava_foam", 0) > 0 && netherBlocks.containsAll(nether.keySet()),
			"ruby ore and Lava Foam generate in the Nether, and nothing but OreSpawn's Nether blocks: " + nether);
	}

	private static Map<String, Integer> scan(MinecraftServer s, ServerLevel level, int radius) {
		Map<String, Integer> counts = new TreeMap<>();
		BlockPos spawn = level.dimension() == Level.OVERWORLD ? s.getPlayerList().getPlayers().getFirst().blockPosition() : BlockPos.ZERO;
		int cx0 = spawn.getX() >> 4, cz0 = spawn.getZ() >> 4;
		for (int cx = cx0 - radius; cx <= cx0 + radius; cx++) {
			for (int cz = cz0 - radius; cz <= cz0 + radius; cz++) {
				LevelChunk chunk = level.getChunk(cx, cz);
				for (LevelChunkSection section : chunk.getSections()) {
					if (section.hasOnlyAir()) continue;
					if (!section.maybeHas(st -> BuiltInRegistries.BLOCK.getKey(st.getBlock()).getNamespace().equals("orespawn"))) continue;
					for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
						Identifier id = BuiltInRegistries.BLOCK.getKey(section.getBlockState(x, y, z).getBlock());
						if (id.getNamespace().equals("orespawn")) counts.merge(id.getPath(), 1, Integer::sum);
					}
				}
			}
		}
		return counts;
	}

	// ---- Screenshots ----

	private void cmd(TestServerContext server, String command) {
		server.runCommand(command);
	}

	private void look(TestServerContext server, Vec3 from, Vec3 at) {
		Vec3 d = at.subtract(from);
		float yaw = (float) (Math.toDegrees(Math.atan2(d.z, d.x)) - 90.0);
		float pitch = (float) -Math.toDegrees(Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)));
		cmd(server, String.format(Locale.ROOT, "tp @p %.2f %.2f %.2f %.1f %.1f", from.x, from.y, from.z, yaw, pitch));
		context.waitTicks(2);
	}

	private void shot(TestSingleplayerContext sp, String name) {
		context.waitTicks(10);
		sp.getConnection().waitForChunksRender(true, 2400);
		context.waitTicks(10);
		Path path = context.takeScreenshot(name);
		log("screenshot " + path);
		try {
			Path root = Path.of(System.getProperty("user.dir")).toAbsolutePath();
			while (root != null && !Files.exists(root.resolve("build.gradle"))) root = root.getParent();
			Path dir = root.resolve("screenshots").resolve(allMods ? "allmods" : "plain");
			Files.createDirectories(dir);
			Files.copy(path, dir.resolve(path.getFileName()), StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			log("couldn't copy screenshot: " + e);
		}
	}

	/** Every block, set in a wall, 50 to a page, plus a close look at some egg ores. */
	private void blockWall(TestServerContext server, TestSingleplayerContext sp, int g) {
		cmd(server, "gamemode spectator @p");
		List<String> names = new ArrayList<>(ModBlocks.BLOCKS.keySet());
		int width = 10, rows = 5, perPage = width * rows;
		for (int page = 0; page * perPage < names.size(); page++) {
			cmd(server, "fill -1 " + (g + 1) + " -10 " + width + " " + (g + rows + 1) + " -10 minecraft:air");
			cmd(server, "fill -1 " + (g + 1) + " -11 " + width + " " + (g + rows + 1) + " -11 minecraft:smooth_stone");
			for (int i = 0; i < perPage && page * perPage + i < names.size(); i++) {
				int x = i % width, y = g + rows - i / width;
				cmd(server, "setblock " + x + " " + y + " -10 orespawn:" + names.get(page * perPage + i));
			}
			look(server, new Vec3(4.5, g + 3.0, -1.5), new Vec3(4.5, g + 3.0, -10));
			shot(sp, String.format(Locale.ROOT, "os_1_blocks_%02d", page + 1));
			if (page == 0) {
				look(server, new Vec3(2.0, g + 4.0, -6.5), new Vec3(2.0, g + 3.5, -10));
				shot(sp, "os_2_egg_ores_close");
			}
		}
	}

	/** Every item, shown as it is in the inventory, 54 to a page. */
	private void itemWall(TestServerContext server, TestSingleplayerContext sp, int g) {
		List<String> names = new ArrayList<>(ModItems.ITEMS.keySet());
		ModEntities.SPAWN_EGGS.keySet().forEach(k -> names.add(k + "_spawn_egg"));
		int width = 9, rows = 6, perPage = width * rows;
		for (int page = 0; page * perPage < names.size(); page++) {
			cmd(server, "kill @e[type=minecraft:item_display]");
			cmd(server, "fill -1 " + g + " -31 " + width + " " + (g + rows + 1) + " -31 minecraft:smooth_stone");
			for (int i = 0; i < perPage && page * perPage + i < names.size(); i++) {
				double x = i % width + 0.5, y = g + rows - i / width + 0.5;
				cmd(server, String.format(Locale.ROOT, "summon minecraft:item_display %.1f %.1f -29.45 {item_display:\"gui\",item:{id:\"orespawn:%s\","
					+ "count:1},transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[0.9f,0.9f,0.9f]}}",
					x, y, names.get(page * perPage + i)));
			}
			look(server, new Vec3(4.5, g + 3.5, -22.5), new Vec3(4.5, g + 3.5, -31));
			shot(sp, String.format(Locale.ROOT, "os_3_items_%02d", page + 1));
		}
		cmd(server, "kill @e[type=minecraft:item_display]");
	}

	/** Each armor set on an armor stand, a zombie and a baby zombie. */
	private void armorShots(TestServerContext server, TestSingleplayerContext sp, int g) {
		List<String> sets = List.of("emerald", "ruby", "amethyst", "lapis", "ultimate");
		for (int i = 0; i < sets.size(); i++) {
			String set = sets.get(i);
			String tag = "os_" + set;
			String sword = set.equals("lapis") ? "minecraft:air" : "orespawn:" + set + "_sword";
			String axe = set.equals("lapis") ? "minecraft:air" : "orespawn:" + set + "_axe";
			cmd(server, "summon minecraft:armor_stand " + (i * 2) + " " + (g + 1) + " -50 {ShowArms:1b,Tags:[\"" + tag + "\"]}");
			armor(server, "@e[tag=" + tag + "]", set, sword);
			cmd(server, "summon minecraft:zombie " + (i * 2) + " " + (g + 1) + " -65 {NoAI:1b,PersistenceRequired:1b,Tags:[\"" + tag + "_z\"]}");
			armor(server, "@e[tag=" + tag + "_z]", set, axe);
			cmd(server, "summon minecraft:zombie " + (i * 2) + " " + (g + 1) + " -62.5 {NoAI:1b,IsBaby:1b,PersistenceRequired:1b,Tags:[\"" + tag + "_b\"]}");
			armor(server, "@e[tag=" + tag + "_b]", set, "minecraft:air");
		}
		context.waitTicks(5);
		look(server, new Vec3(4.0, g + 2.2, -45.5), new Vec3(4.0, g + 1.2, -50));
		shot(sp, "os_4_armor_stands_front");
		look(server, new Vec3(4.0, g + 2.2, -71.0), new Vec3(4.0, g + 1.0, -65));
		shot(sp, "os_5_zombies_back");
		look(server, new Vec3(4.0, g + 2.0, -56.5), new Vec3(4.0, g + 0.9, -63));
		shot(sp, "os_6_zombies_front");
	}

	private void armor(TestServerContext server, String target, String set, String hand) {
		cmd(server, "item replace entity " + target + " armor.head with orespawn:" + set + "_helmet");
		cmd(server, "item replace entity " + target + " armor.chest with orespawn:" + set + "_chestplate");
		cmd(server, "item replace entity " + target + " armor.legs with orespawn:" + set + "_leggings");
		cmd(server, "item replace entity " + target + " armor.feet with orespawn:" + set + "_boots");
		cmd(server, "item replace entity " + target + " weapon.mainhand with " + hand);
	}

	/** The Ultimate Sword in first person, and the player in third person holding a Ruby Chestplate. */
	private void heldShots(TestServerContext server, TestSingleplayerContext sp, int g) {
		cmd(server, "gamemode creative @p");
		cmd(server, "item replace entity @p weapon.mainhand with orespawn:ultimate_sword");
		look(server, new Vec3(20.5, g + 1, 0.5), new Vec3(20.5, g + 1.5, -10));
		context.runOnClient(mc -> {
			if (mc.gui.hud.isHidden()) mc.gui.hud.toggle(); // hiding the HUD also hides the hand
			mc.options.setCameraType(CameraType.FIRST_PERSON);
		});
		shot(sp, "os_7_first_person_ultimate_sword");
		cmd(server, "item replace entity @p weapon.mainhand with orespawn:ruby_chestplate");
		context.runOnClient(mc -> mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
		context.waitTicks(30);
		shot(sp, "os_8_third_person_held_ruby_chestplate");
		context.runOnClient(mc -> mc.options.setCameraType(CameraType.FIRST_PERSON));
	}
}
