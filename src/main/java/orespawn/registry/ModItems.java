package orespawn.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.block.Block;
import orespawn.OreSpawn;
import orespawn.item.AutoEnchantItem;
import orespawn.item.AutoEnchantItem.Ench;
import orespawn.item.UltimateHoeItem;
import orespawn.item.UltimatePickaxeItem;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Materials, tools and armor, with OreSpawn 1.7.10-20.3's default stats ({@code OreSpawnMain.get_weaponstats},
 * {@code get_armorstats}).
 * <p>
 * Damage: 1.7.10 gave a sword 4 + the material's damage, a pickaxe 2 +, an axe 3 + and a shovel 1 +, hoes nothing; the
 * same totals are kept here (so a Ruby Sword hits for 21 and the Ultimate Sword for 41, before enchantments). Attack
 * speeds are 26.3's usual ones for each tool, since 1.7.10 had no attack cooldown.
 */
public final class ModItems {
	public static final Map<String, Item> ITEMS = new LinkedHashMap<>();

	public static final Item RUBY = item("ruby", new Item.Properties());
	public static final Item AMETHYST = item("amethyst", new Item.Properties());
	public static final Item SALT = item("salt", new Item.Properties());
	public static final Item URANIUM_NUGGET = item("uranium_nugget", new Item.Properties());
	public static final Item TITANIUM_NUGGET = item("titanium_nugget", new Item.Properties());
	public static final Item URANIUM_INGOT = item("uranium_ingot", new Item.Properties());
	public static final Item TITANIUM_INGOT = item("titanium_ingot", new Item.Properties());
	// Creature drops (OreSpawnMain: ItemPopcorn / ItemSunFish foods are plain hunger and saturation).
	public static final Item PEACOCK_FEATHER = item("peacock_feather", new Item.Properties());
	public static final Item RAW_PEACOCK = item("raw_peacock", new Item.Properties().food(food(6, 0.7F)));
	public static final Item COOKED_PEACOCK = item("cooked_peacock", new Item.Properties().food(food(12, 1.4F)));
	public static final Item DEAD_STINK_BUG = item("dead_stink_bug", new Item.Properties());
	public static final Item RAW_CRAB_MEAT = item("raw_crab_meat", new Item.Properties().food(food(4, 0.25F)));
	public static final Item CRAB_MEAT = item("crab_meat", new Item.Properties().food(food(6, 0.75F)));

	// get_weaponstats(name, harvest level, uses, efficiency, damage, enchantability)
	public static final ToolMaterial EMERALD_TOOL = tool("emerald", BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1300, 10, 6, 75);
	public static final ToolMaterial RUBY_TOOL = tool("ruby", BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 1500, 11, 16, 85);
	public static final ToolMaterial AMETHYST_TOOL = tool("amethyst", BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2000, 11, 11, 70);
	public static final ToolMaterial ULTIMATE_TOOL = tool("ultimate", BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 3000, 15, 36, 100);

	// get_armorstats(name, durability, head, chest, legs, boots, enchantability)
	public static final ArmorMaterial EMERALD_ARMOR = armor("emerald", 60, 3, 8, 6, 3, 40);
	public static final ArmorMaterial RUBY_ARMOR = armor("ruby", 90, 4, 9, 8, 4, 40);
	public static final ArmorMaterial AMETHYST_ARMOR = armor("amethyst", 100, 4, 8, 7, 3, 40);
	public static final ArmorMaterial LAPIS_ARMOR = armor("lapis", 60, 2, 7, 5, 2, 60);
	public static final ArmorMaterial ULTIMATE_ARMOR = armor("ultimate", 200, 6, 12, 10, 6, 100);

	/** The Ultimate Sword's enchantments at OreSpawn's default level 5 ("UltimateSwordEnchantmentLevel"). */
	private static final List<Ench> ULTIMATE_SWORD = List.of(new Ench(Enchantments.LOOTING, 3), new Ench(Enchantments.SHARPNESS, 5),
		new Ench(Enchantments.SMITE, 5), new Ench(Enchantments.BANE_OF_ARTHROPODS, 5), new Ench(Enchantments.KNOCKBACK, 3),
		new Ench(Enchantments.UNBREAKING, 3), new Ench(Enchantments.FIRE_ASPECT, 2));

	static {
		toolSet("emerald", EMERALD_TOOL, Rarity.COMMON, List.of(), List.of(new Ench(Enchantments.SILK_TOUCH, 1)), List.of(), List.of(), List.of());
		toolSet("ruby", RUBY_TOOL, Rarity.COMMON, List.of(), List.of(), List.of(), List.of(), List.of());
		toolSet("amethyst", AMETHYST_TOOL, Rarity.COMMON, List.of(), List.of(), List.of(), List.of(), List.of());
		toolSet("ultimate", ULTIMATE_TOOL, Rarity.RARE, ULTIMATE_SWORD,
			List.of(new Ench(Enchantments.EFFICIENCY, 5), new Ench(Enchantments.FORTUNE, 5)),
			List.of(new Ench(Enchantments.EFFICIENCY, 5)), List.of(new Ench(Enchantments.EFFICIENCY, 5)),
			List.of(new Ench(Enchantments.EFFICIENCY, 2)));

		armorSet("emerald", EMERALD_ARMOR, Rarity.COMMON, 0, 0, 0, 0, 0, 0, 0);
		armorSet("ruby", RUBY_ARMOR, Rarity.COMMON, 0, 0, 0, 0, 0, 0, 0);
		armorSet("amethyst", AMETHYST_ARMOR, Rarity.COMMON, 0, 0, 0, 0, 0, 0, 0);
		// e_respiration, e_aquaaffinity, e_protection, e_fireprotection, e_blastprotection, e_projectileprotection, e_featherfalling
		armorSet("lapis", LAPIS_ARMOR, Rarity.COMMON, 1, 1, 1, 0, 0, 1, 0);
		armorSet("ultimate", ULTIMATE_ARMOR, Rarity.RARE, 2, 3, 5, 5, 5, 5, 3);
	}

	private ModItems() {
	}

	public static void init() {
		// Static initialisers do the work.
	}

	/** An item another content group registers, looked up by id when it's needed (air if that group isn't ported yet). */
	public static Item lazy(String id) {
		return BuiltInRegistries.ITEM.getValue(OreSpawn.id(id));
	}

	public static Item byName(String name) {
		Item item = ITEMS.get(name);
		if (item == null) throw new IllegalArgumentException("No OreSpawn item " + name);
		return item;
	}

	public static FoodProperties food(int nutrition, float saturation) {
		return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build();
	}

	public static TagKey<Item> repairTag(String material) {
		return TagKey.create(Registries.ITEM, OreSpawn.id(material + "_repair_materials"));
	}

	/** 1.7.10's material damage becomes 26.3's bonus + 1, since 26.3's tool baselines sit 1 lower. */
	private static ToolMaterial tool(String name, TagKey<Block> incorrect, int uses, float efficiency, int damage, int enchant) {
		return new ToolMaterial(incorrect, uses, efficiency, damage + 1, enchant, repairTag(name));
	}

	private static ArmorMaterial armor(String name, int durability, int head, int chest, int legs, int boots, int enchant) {
		Map<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
		defense.put(ArmorType.HELMET, head);
		defense.put(ArmorType.CHESTPLATE, chest);
		defense.put(ArmorType.LEGGINGS, legs);
		defense.put(ArmorType.BOOTS, boots);
		defense.put(ArmorType.BODY, chest);
		ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, OreSpawn.id(name));
		return new ArmorMaterial(durability, defense, enchant, SoundEvents.ARMOR_EQUIP_DIAMOND, 0.0F, 0.0F, repairTag(name), asset);
	}

	private static void toolSet(String name, ToolMaterial m, Rarity rarity, List<Ench> sword, List<Ench> pickaxe, List<Ench> shovel,
			List<Ench> axe, List<Ench> hoe) {
		float bonus = m.attackDamageBonus();
		boolean ultimate = name.equals("ultimate");
		register(name + "_sword", new AutoEnchantItem(props(name + "_sword").sword(m, 3.0F, -2.4F).rarity(rarity), sword));
		Item.Properties pick = props(name + "_pickaxe").pickaxe(m, 1.0F, -2.8F).rarity(rarity);
		register(name + "_pickaxe", ultimate ? new UltimatePickaxeItem(pick, pickaxe) : new AutoEnchantItem(pick, pickaxe));
		register(name + "_shovel", new AutoEnchantItem(props(name + "_shovel").shovel(m, 0.0F, -3.0F).rarity(rarity), shovel));
		register(name + "_axe", new AutoEnchantItem(props(name + "_axe").axe(m, 2.0F, -3.0F).rarity(rarity), axe));
		Item.Properties hoeProps = props(name + "_hoe").hoe(m, -bonus, 0.0F).rarity(rarity);
		register(name + "_hoe", ultimate ? new UltimateHoeItem(hoeProps, hoe) : new AutoEnchantItem(hoeProps, hoe));
	}

	private static void armorSet(String name, ArmorMaterial material, Rarity rarity, int respiration, int aqua, int protection, int fire,
			int blast, int projectile, int feather) {
		for (ArmorType type : List.of(ArmorType.HELMET, ArmorType.CHESTPLATE, ArmorType.LEGGINGS, ArmorType.BOOTS)) {
			List<Ench> e = new ArrayList<>();
			if (protection > 0) e.add(new Ench(Enchantments.PROTECTION, protection));
			if (fire > 0) e.add(new Ench(Enchantments.FIRE_PROTECTION, fire));
			if (blast > 0) e.add(new Ench(Enchantments.BLAST_PROTECTION, blast));
			if (projectile > 0) e.add(new Ench(Enchantments.PROJECTILE_PROTECTION, projectile));
			if (type == ArmorType.BOOTS && feather > 0) e.add(new Ench(Enchantments.FEATHER_FALLING, feather));
			if (type == ArmorType.HELMET && respiration > 0) e.add(new Ench(Enchantments.RESPIRATION, respiration));
			if (type == ArmorType.HELMET && aqua > 0) e.add(new Ench(Enchantments.AQUA_AFFINITY, aqua));
			String id = name + "_" + type.getName();
			register(id, new AutoEnchantItem(props(id).humanoidArmor(material, type).rarity(rarity), List.copyOf(e)));
		}
	}

	public static Item.Properties props(String name) {
		return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, OreSpawn.id(name)));
	}

	public static Item item(String name, Item.Properties p) {
		return register(name, new Item(p.setId(ResourceKey.create(Registries.ITEM, OreSpawn.id(name)))));
	}

	/** Registers an item (used by every content group); props must come from {@link #props}. */
	public static Item register(String name, Item item) {
		Registry.register(BuiltInRegistries.ITEM, OreSpawn.id(name), item);
		ITEMS.put(name, item);
		return item;
	}
}
