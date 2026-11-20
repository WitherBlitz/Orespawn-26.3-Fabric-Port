"""Writes OreSpawnFabric's resources from OreSpawn 1.7.10-20.3: its textures (copied from the unzipped jar), and the
models, loot tables, recipes, tags, worldgen and English names that 26.3 needs (from OreSpawnMain / OreSpawnWorld).

    python -I tools/gen_assets.py [path to the unzipped OreSpawn jar]   (default: D:/Destinymods/_src_orespawn/zip)

OreSpawn has no license; the user allowed using its textures and code for this private pack (2026-11-19). The lists
below must match orespawn.registry.ModBlocks / ModItems (the game test checks every block and item has a model,
texture and name).
"""
import json
import os
import shutil
import sys

from PIL import Image

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources")
NS = "orespawn"
ASSETS = os.path.join(ROOT, "assets", NS)
DATA = os.path.join(ROOT, "data", NS)
SRC = os.path.join(sys.argv[1] if len(sys.argv) > 1 else "D:/Destinymods/_src_orespawn/zip", "assets", "orespawn")

# ---------------------------------------------------------------------------------------------------------------------
# Content (keep in step with ModBlocks / ModItems)
# ---------------------------------------------------------------------------------------------------------------------
# (modern mob id, OreSpawn texture, OreSpawn's name for the mob)
VANILLA_EGGS = [
    ("spider", "orespider", "Spider"), ("bat", "orebat", "Bat"), ("cow", "orecow", "Cow"), ("pig", "orepig", "Pig"),
    ("squid", "oresquid", "Squid"), ("chicken", "orechicken", "Chicken"), ("creeper", "orecreeper", "Creeper"),
    ("skeleton", "oreskeleton", "Skeleton"), ("zombie", "orezombie", "Zombie"), ("slime", "oreslime", "Slime"),
    ("ghast", "oreghast", "Ghast"), ("zombified_piglin", "orezombiepigman", "Zombie Pigman"), ("enderman", "oreenderman", "Enderman"),
    ("cave_spider", "orecavespider", "Cave Spider"), ("silverfish", "oresilverfish", "Silverfish"),
    ("magma_cube", "oremagmacube", "Magma Cube"), ("witch", "orewitch", "Witch"), ("sheep", "oresheep", "Sheep"),
    ("wolf", "orewolf", "Wolf"), ("mooshroom", "oremooshroom", "Mooshroom"), ("ocelot", "oreocelot", "Ocelot"),
    ("blaze", "oreblaze", "Blaze"), ("wither_skeleton", "orewitherskeleton", "Wither Skeleton"),
    ("ender_dragon", "oreenderdragon", "Ender Dragon"), ("snow_golem", "oresnowgolem", "Snow Golem"),
    ("iron_golem", "oreirongolem", "Iron Golem"), ("wither", "orewitherboss", "Wither Boss"), ("villager", "orevillager", "Villager"),
    ("horse", "orehorse", "Horse"),
]
# block id -> (OreSpawn texture, name)
BLOCKS = {
    "ruby_ore": ("oreruby", "Ruby Ore"), "amethyst_ore": ("oreamethyst", "Amethyst Ore"),
    "uranium_ore": ("oreuranium", "Uranium Ore"), "titanium_ore": ("oretitanium", "Titanium Ore"), "salt_ore": ("oresalt", "Salt Ore"),
    "ruby_block": ("blockruby", "Ruby Block"), "amethyst_block": ("blockamethyst", "Amethyst Block"),
    "uranium_block": ("blockuranium", "Uranium Block"), "titanium_block": ("blocktitanium", "Titanium Block"),
}
# item id -> (OreSpawn texture, name)
MATERIALS = {
    "ruby": ("ruby", "Ruby"), "amethyst": ("amethyst", "Amethyst"), "salt": ("salt", "Salt"),
    "uranium_nugget": ("uranium_nugget", "Uranium Nugget"), "titanium_nugget": ("titanium_nugget", "Titanium Nugget"),
    "uranium_ingot": ("ingoturanium", "Uranium Ingot"), "titanium_ingot": ("ingottitanium", "Titanium Ingot"),
    "peacock_feather": ("peacockfeather", "Peacock Feather"), "raw_peacock": ("rawpeacock", "Raw Peacock"),
    "cooked_peacock": ("cookedpeacock", "Cooked Peacock"), "dead_stink_bug": ("deadstinkbug", "Dead Stink Bug"),
    "raw_crab_meat": ("crabmeat", "Raw Crab Meat"), "crab_meat": ("cookedcrabmeat", "Crab Meat!"),
}
TOOL_SETS = {"emerald": "Emerald", "ruby": "Ruby", "amethyst": "Amethyst", "ultimate": "The Ultimate"}
ARMOR_SETS = {"emerald": "Emerald", "ruby": "Ruby", "amethyst": "Amethyst", "lapis": "Lapis Lazuli", "ultimate": "The Ultimate"}
TOOLS = ["sword", "pickaxe", "shovel", "axe", "hoe"]
ARMOR = {"helmet": "helmet", "chestplate": "chest", "leggings": "leggings", "boots": "boots"}  # our name -> OreSpawn's

# OreSpawn's creatures so far: id -> (OreSpawn's name, its spawn egg texture).
CREATURES = {
    "apple_cow": ("Apple Cow", "eggredcow"), "golden_apple_cow": ("Golden Apple Cow", "egggoldcow"),
    "enchanted_golden_apple_cow": ("Enchanted Golden Apple Cow", "eggenchantedcow"), "butterfly": ("Butterfly", "eggbutterfly"),
    "moth": ("Moth", "eggmoth"), "firefly": ("Firefly", "eggfirefly"), "bird": ("Bird", "eggcockateil"),
    "chipmunk": ("Chipmunk", "eggchipmunk"), "cricket": ("Cricket", "eggcricket"), "frog": ("Frog", "eggfrog"),
    "beaver": ("Beaver", "eggbeaver"), "cassowary": ("Cassowary", "eggcassowary"), "rat": ("Rat", "eggrat"),
}
# Entity textures: OreSpawn's file -> ours (textures/entity/).
ENTITY_TEXTURES = {
    "red_cow.png": "red_cow.png", "gold_cow.png": "gold_cow.png", "butterfly.png": "butterfly.png", "butterfly2.png": "butterfly2.png",
    "butterfly3.png": "butterfly3.png", "butterfly4.png": "butterfly4.png", "vbutterfly1.png": "vbutterfly1.png", "lunamoth.png": "lunamoth.png", "eyemoth.png": "eyemoth.png",
    "darkmoth.png": "darkmoth.png", "firemoth.png": "firemoth.png", "Fireflytexture.png": "firefly.png", "Bird1.png": "bird1.png",
    "Bird2.png": "bird2.png", "Bird3.png": "bird3.png", "Bird4.png": "bird4.png", "Bird5.png": "bird5.png", "Bird6.png": "bird6.png",
    "chipmunktexture.png": "chipmunk.png", "Crickettexture.png": "cricket.png", "Frogtexture.png": "frog.png",
    "Beavertexture.png": "beaver.png", "Cassowary.png": "cassowary.png", "Rattexture.png": "rat.png",
}
# OreSpawn's own mobs with an egg ore (keep in step with ModBlocks.CREATURE_EGGS): id -> (texture, name in its egg's name).
CREATURE_EGG_ORES = {
    "apple_cow": ("oreredcow", "Apple Cow"), "golden_apple_cow": ("oregoldcow", "Golden Apple Cow"),
    "enchanted_golden_apple_cow": ("oreenchantedcow", "Enchanted Golden Apple Cow"), "bird": ("orecockateil", "Bird"),
    "chipmunk": ("orechipmunk", "Chipmunk"), "cricket": ("orecricket", "Cricket"), "frog": ("orefrog", "Frog"),
    "beaver": ("orebeaver", "Beaver"), "cassowary": ("orecassowary", "Cassowary"), "rat": ("orerat", "Rat"),
    # batch 2
    "peacock": ("orepeacock", "Peacock"), "stink_bug": ("orestink", "Stink Bug"), "dragonfly": ("oredragonfly", "Dragonfly"),
    "gazelle": ("oregazelle", "Gazelle"), "ostrich": ("oreostrich", "Ostrich"), "lizard": ("orelizard", "Lizard"),
    "crab": ("orecrab", "Crab"), "scorpion": ("orescorpion", "Scorpion"), "small_worm": ("oresmallworm", "Small Worm"),
    "medium_worm": ("oremediumworm", "Medium Worm"), "large_worm": ("orelargeworm", "Large Worm"),
    "gold_fish": ("oregoldfish", "Gold Fish"), "flounder": ("oreflounder", "Flounder"), "skate": ("oreskate", "Skate"),
    "whale": ("orewhale", "Whale"),
}
# Creatures that leave a Wither's Combat ragdoll (keep in step with CreatureTest.RAGDOLLS).
RAGDOLLS = ["apple_cow", "golden_apple_cow", "enchanted_golden_apple_cow", "bird", "chipmunk", "frog", "beaver", "cassowary", "rat"]
# OreSpawn sound events used so far (keep in step with ModSounds.NAMES).
SOUNDS = ["birds", "duck_hurt", "cricket", "scorpion_hit", "big_splat", "ratlive", "rathit", "ratdead", "cryo_death", "chainsaw"]

NAMES = {}
# Egg ores OreSpawn never generated (The King's and The Queen's full ores), and other blocks it put in the egg-ore
# lottery (their egg parts); from the specs' "worldgen": false and "egg_ore_worldgen": true.
EGG_ORES_NOT_GENERATED = []
EXTRA_GENERATED_EGG_ORES = []

# Every file this script writes is listed in MANIFEST, and the next run deletes only those before writing again. Groups
# may keep hand-written files in the same folders (dimension JSON, special models), and those are never touched.
MANIFEST = os.path.join(os.path.dirname(os.path.abspath(__file__)), ".generated_files.txt")
WRITTEN = set()


def track(path):
    WRITTEN.add(os.path.relpath(os.path.abspath(path), os.path.abspath(ROOT)).replace(os.sep, "/"))

# Content groups after batch 1 describe their assets in tools/content/*.json (see tools/PORTING.md).
SPEC_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "content")
SPECS = []
if os.path.isdir(SPEC_DIR):
    for f in sorted(os.listdir(SPEC_DIR)):
        if f.endswith(".json"):
            with open(os.path.join(SPEC_DIR, f), encoding="utf-8") as fh:
                SPECS.append((f, json.load(fh)))
for _f, _spec in SPECS:
    ENTITY_TEXTURES.update(_spec.get("entity_textures", {}))
    for _id, _c in _spec.get("creatures", {}).items():
        CREATURES[_id] = (_c["name"], _c["egg"])
        ENTITY_TEXTURES.update(_c.get("textures", {}))
        if _c.get("egg_ore"):
            CREATURE_EGG_ORES[_id] = (_c["egg_ore"]["texture"], _c["egg_ore"]["name"])
            if _c["egg_ore"].get("worldgen") is False:
                EGG_ORES_NOT_GENERATED.append(f"orespawn:{_id}_egg_ore")
    for _id, _b in _spec.get("blocks", {}).items():
        if _b.get("egg_ore_worldgen"):
            EXTRA_GENERATED_EGG_ORES.append(f"orespawn:{_id}")
        if _c.get("ragdoll") == "from_model":
            RAGDOLLS.append(_id)
    for _s in _spec.get("sounds", []):
        if _s.lower() not in SOUNDS:
            SOUNDS.append(_s.lower())


# ---------------------------------------------------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------------------------------------------------
def write_json(path, obj):
    track(path)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def copy_texture(src_rel, dest_rel):
    src = os.path.join(SRC, src_rel)
    if not os.path.exists(src):
        raise SystemExit(f"missing OreSpawn texture {src}")
    dest = os.path.join(ASSETS, "textures", dest_rel)
    os.makedirs(os.path.dirname(dest), exist_ok=True)
    shutil.copyfile(src, dest)
    track(dest)


def cube_block(name, texture):
    copy_texture(f"textures/blocks/{texture}.png", f"block/{name}.png")
    write_json(f"{ASSETS}/blockstates/{name}.json", {"variants": {"": {"model": f"{NS}:block/{name}"}}})
    write_json(f"{ASSETS}/models/block/{name}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": f"{NS}:block/{name}"}})
    write_json(f"{ASSETS}/items/{name}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/{name}"}})


def spec_block(name, b):
    """A block from a content spec: "model" is "cube" (default; "texture" on all faces, or "textures": {"top", "bottom",
    "side"} for a column-like block), "cross" (a plant, drawn as an X; its item uses the same texture) or "none" (the
    group ships its own blockstate and models). "drops" is "self" (default), "none", or an item id."""
    model = b.get("model", "cube")
    if model == "cube" and "textures" in b:
        faces = b["textures"]
        for face, tex in faces.items():
            copy_texture(f"textures/blocks/{tex}.png", f"block/{name}_{face}.png")
        write_json(f"{ASSETS}/models/block/{name}.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
            "top": f"{NS}:block/{name}_top", "bottom": f"{NS}:block/{name}_bottom", "side": f"{NS}:block/{name}_side"}})
        write_json(f"{ASSETS}/blockstates/{name}.json", {"variants": {"": {"model": f"{NS}:block/{name}"}}})
        write_json(f"{ASSETS}/items/{name}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:block/{name}"}})
    elif model == "cube":
        cube_block(name, b["texture"])
    elif model == "cross":
        copy_texture(f"textures/blocks/{b['texture']}.png", f"block/{name}.png")
        write_json(f"{ASSETS}/models/block/{name}.json", {"parent": "minecraft:block/cross",
                                                          "textures": {"cross": f"{NS}:block/{name}"}})
        write_json(f"{ASSETS}/blockstates/{name}.json", {"variants": {"": {"model": f"{NS}:block/{name}"}}})
        write_json(f"{ASSETS}/models/item/{name}.json", {"parent": "minecraft:item/generated",
                                                         "textures": {"layer0": f"{NS}:block/{name}"}})
        write_json(f"{ASSETS}/items/{name}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/{name}"}})
    drops = b.get("drops", "self")
    if drops == "self":
        loot_self(name)
    elif drops != "none":
        loot(name, {"rolls": 1, "condition": {"type": "minecraft:survives_explosion"},
                    "entries": [{"type": "minecraft:item", "name": drops}]})
    NAMES[f"block.{NS}.{name}"] = b["name"]


def flat_item(name, texture, handheld=False):
    copy_texture(f"textures/items/{texture}.png", f"item/{name}.png")
    write_json(f"{ASSETS}/models/item/{name}.json", {
        "parent": "minecraft:item/handheld" if handheld else "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{name}"}})
    write_json(f"{ASSETS}/items/{name}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/{name}"}})


def loot(name, pool):
    write_json(f"{DATA}/loot_table/blocks/{name}.json", {"type": "minecraft:block", "pools": [pool], "random_sequence": f"{NS}:blocks/{name}"})


def loot_self(name):
    loot(name, {"rolls": 1, "condition": {"type": "minecraft:survives_explosion"},
                "entries": [{"type": "minecraft:item", "name": f"{NS}:{name}"}]})


def loot_gem(name, gem):
    """OreRuby / OreAmethyst: 1-2 gems (Fortune doesn't change it); silk touch keeps the ore."""
    loot(name, {"rolls": 1, "entries": [{"type": "minecraft:alternatives", "children": [
        {"type": "minecraft:item", "condition": "minecraft:tool/can_silk_touch", "name": f"{NS}:{name}"},
        {"type": "minecraft:item", "name": gem, "modifier": [
            {"type": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 2}},
            {"type": "minecraft:explosion_decay"}]}]}]})


def result(item, count):
    r = {"id": item}
    if count != 1:
        r["count"] = count
    return r


def shaped(name, pattern, key, item, count=1, category="equipment"):
    used = {k: v for k, v in key.items() if any(k in row for row in pattern)}
    write_json(f"{DATA}/recipe/{name}.json", {"type": "minecraft:crafting_shaped", "category": category, "key": used,
                                               "pattern": pattern, "result": result(item, count)})


def shapeless(name, ingredients, item, count=1):
    write_json(f"{DATA}/recipe/{name}.json", {"type": "minecraft:crafting_shapeless", "category": "misc",
                                               "ingredients": ingredients, "result": result(item, count)})


def smelt(name, ingredient, item, xp, count=1):
    write_json(f"{DATA}/recipe/{name}.json", {"type": "minecraft:smelting", "cookingtime": 200, "experience": xp,
                                               "ingredient": ingredient, "result": result(item, count)})


def tag(registry, namespace, name, values):
    write_json(os.path.join(ROOT, "data", namespace, "tags", registry, f"{name}.json"), {"replace": False, "values": values})


def ore_config(size, block, target):
    return {"type": "minecraft:ore", "discard_chance_on_air_exposure": 0.0, "size": size,
            "targets": [{"state": block, "target": {"predicate_type": "minecraft:block_match", "block": target}}]}


def placed(name, feature, count, low, high):
    c = count if isinstance(count, int) else {"type": "minecraft:uniform", "min_inclusive": count[0], "max_inclusive": count[1]}
    write_json(f"{DATA}/worldgen/placed_feature/{name}.json", {"feature": feature, "placement": [
        {"type": "minecraft:count", "count": c}, {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform", "min_inclusive": {"absolute": low},
                                                      "max_inclusive": {"absolute": high}}},
        {"type": "minecraft:biome"}]})


def ore(name, size, block, target, count, low, high, scattered=False):
    """WorldGenMinable(block, size) in `target`. OreSpawn rolled y in 0-127 and skipped tries outside the ore's band, so
    the counts here are its tries per chunk times the band's share of 128. Veins of 2 use scattered_ore: 26.3's ore
    feature almost never places a block at that size."""
    config = ore_config(size, block, target)
    if scattered:
        config["type"] = "minecraft:scattered_ore"
    write_json(f"{DATA}/worldgen/feature/{name}.json", config)
    placed(name, f"{NS}:{name}", count, low, high)


# ---------------------------------------------------------------------------------------------------------------------
# Worn armor: OreSpawn's 64x32 layer 1 (helmet, chestplate, boots) and layer 2 (leggings) are 26.3's humanoid and
# humanoid_leggings layouts unchanged. 26.3's baby humanoid armor has its own 64x64 layout, so it's made by scaling
# each face of the adult boxes onto the matching baby box.
# ---------------------------------------------------------------------------------------------------------------------
def box_faces(u, v, w, h, d):
    return [(u + d, v, w, d), (u + d + w, v, w, d), (u, v + d, d, h), (u + d, v + d, w, h), (u + d + w, v + d, d, h),
            (u + d + w + d, v + d, w, h)]


def remap(src, dst, src_box, dst_box):
    for (sx, sy, sw, sh), (dx, dy, dw, dh) in zip(box_faces(*src_box), box_faces(*dst_box)):
        face = src.crop((sx, sy, sx + sw, sy + sh)).resize((dw, dh), Image.NEAREST)
        dst.alpha_composite(face, (dx, dy))


def baby_armor(layer1, layer2):
    baby = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    remap(layer1, baby, (0, 0, 8, 8, 8), (0, 0, 9, 8, 8))          # head
    remap(layer1, baby, (16, 16, 8, 12, 4), (0, 17, 6, 5, 3))      # body
    remap(layer1, baby, (40, 16, 4, 12, 4), (30, 25, 2, 5, 3))     # right arm
    remap(layer1, baby, (40, 16, 4, 12, 4), (30, 17, 2, 5, 3))     # left arm
    remap(layer2, baby, (16, 16, 8, 12, 4), (0, 36, 6, 2, 3))      # waist (leggings)
    remap(layer2, baby, (0, 16, 4, 12, 4), (18, 24, 3, 4, 3))      # legs (leggings)
    remap(layer2, baby, (0, 16, 4, 12, 4), (18, 17, 3, 4, 3))
    remap(layer1, baby, (0, 16, 4, 12, 4), (0, 25, 3, 1, 3))       # feet (boots, on layer 1's legs)
    remap(layer1, baby, (0, 16, 4, 12, 4), (0, 29, 3, 1, 3))
    return baby


def worn_armor(asset, layer1, layer2):
    """An armor material's worn look: OreSpawn's two layers, plus the baby layout made from them, and its equipment json."""
    copy_texture(layer1, f"entity/equipment/humanoid/{asset}.png")
    copy_texture(layer2, f"entity/equipment/humanoid_leggings/{asset}.png")
    baby = baby_armor(Image.open(os.path.join(SRC, layer1)).convert("RGBA"), Image.open(os.path.join(SRC, layer2)).convert("RGBA"))
    os.makedirs(f"{ASSETS}/textures/entity/equipment/humanoid_baby", exist_ok=True)
    baby.save(f"{ASSETS}/textures/entity/equipment/humanoid_baby/{asset}.png")
    track(f"{ASSETS}/textures/entity/equipment/humanoid_baby/{asset}.png")
    write_json(f"{ASSETS}/equipment/{asset}.json", {"layers": {
        "humanoid": [{"texture": f"{NS}:{asset}"}], "humanoid_leggings": [{"texture": f"{NS}:{asset}"}],
        "humanoid_baby": [{"texture": f"{NS}:{asset}"}]}})


def main():
    if os.path.exists(MANIFEST):
        with open(MANIFEST, encoding="utf-8") as fh:
            for rel in fh.read().splitlines():
                path = os.path.join(ROOT, rel)
                if rel and os.path.isfile(path):
                    os.remove(path)

    pickaxe_blocks, shovel_blocks = [], []

    # ---- Egg ores ----
    egg_ids = []
    for mob, texture, mob_name in VANILLA_EGGS:
        name = f"{mob}_egg_ore"
        cube_block(name, texture)
        loot_self(name)
        NAMES[f"block.{NS}.{name}"] = f"Ancient Dried {mob_name} Spawn Egg"
        shovel_blocks.append(f"{NS}:{name}")
        egg_ids.append(f"{NS}:{name}")
        # Water bucket + egg ore = the mob's spawn egg (the bucket comes back empty).
        shapeless(f"{mob}_spawn_egg_from_egg_ore", ["minecraft:water_bucket", f"{NS}:{name}"], f"minecraft:{mob}_spawn_egg")
    for mob, (texture, mob_name) in CREATURE_EGG_ORES.items():
        name = f"{mob}_egg_ore"
        cube_block(name, texture)
        loot_self(name)
        NAMES[f"block.{NS}.{name}"] = f"Ancient Dried {mob_name} Spawn Egg"
        shovel_blocks.append(f"{NS}:{name}")
        egg_ids.append(f"{NS}:{name}")
        shapeless(f"{mob}_spawn_egg_from_egg_ore", ["minecraft:water_bucket", f"{NS}:{name}"], f"{NS}:{mob}_spawn_egg")

    # ---- Other blocks ----
    for name, (texture, label) in BLOCKS.items():
        cube_block(name, texture)
        NAMES[f"block.{NS}.{name}"] = label
        pickaxe_blocks.append(f"{NS}:{name}")
    loot_gem("ruby_ore", f"{NS}:ruby")
    loot_gem("amethyst_ore", f"{NS}:amethyst")
    for name in ("uranium_ore", "titanium_ore", "salt_ore", "ruby_block", "amethyst_block", "uranium_block", "titanium_block"):
        loot_self(name)

    # ---- Worldgen (OreSpawnWorld.generateOres / generateNether, default config) ----
    # Egg ores: 28-47 tries a chunk (+30 one chunk in 20) at y 50-127; 93% pick from 98 slots, 29 of them vanilla mobs.
    # That's about 6.6 vanilla-egg veins of 4 a chunk; OreSpawn's own mobs join this list when they're ported.
    generated = [e for e in egg_ids if e not in EGG_ORES_NOT_GENERATED] + EXTRA_GENERATED_EGG_ORES
    write_json(f"{DATA}/worldgen/feature/egg_ores.json", {
        "type": "minecraft:simple_random_selector",
        "features": [{"feature": ore_config(4, e, "minecraft:stone"), "placement": []} for e in generated]})
    # OreSpawn: about 24 egg-ore veins a chunk over its 98 slots, so the count grows with the egg ores ported so far.
    n = min(len(generated), 98)  # the whole port has more egg ores than OreSpawn's 98 slots; keep its ~24 veins a chunk
    placed("egg_ores", f"{NS}:egg_ores", (round(0.17 * n), round(0.31 * n)), 50, 127)
    ore("amethyst_ore", 6, f"{NS}:amethyst_ore", "minecraft:stone", (0, 3), 0, 25)        # 2-13 tries, y 0-25
    ore("uranium_ore", 4, f"{NS}:uranium_ore", "minecraft:stone", (0, 3), 0, 30)          # 3-11 tries, y 0-30
    ore("titanium_ore", 4, f"{NS}:titanium_ore", "minecraft:stone", (0, 2), 0, 20)        # 3-11 tries, y 0-20
    ore("salt_ore", 12, f"{NS}:salt_ore", "minecraft:stone", (3, 8), 50, 127)             # 5-13 tries, y 50-128
    ore("ruby_ore_nether", 2, f"{NS}:ruby_ore", "minecraft:netherrack", (5, 9), 10, 117, scattered=True)  # Nether: 5-9 veins of 2

    # ---- Materials ----
    for name, (texture, label) in MATERIALS.items():
        flat_item(name, texture)
        NAMES[f"item.{NS}.{name}"] = label
    smelt("uranium_nugget", f"{NS}:uranium_ore", f"{NS}:uranium_nugget", 0.3)
    smelt("titanium_nugget", f"{NS}:titanium_ore", f"{NS}:titanium_nugget", 0.3)
    smelt("salt", f"{NS}:salt_ore", f"{NS}:salt", 0.1, 8)
    smelt("cooked_peacock", f"{NS}:raw_peacock", f"{NS}:cooked_peacock", 0.4)
    smelt("crab_meat", f"{NS}:raw_crab_meat", f"{NS}:crab_meat", 0.2)
    for metal in ("uranium", "titanium"):
        shaped(f"{metal}_ingot", ["UUU", "UUU", "UUU"], {"U": f"{NS}:{metal}_nugget"}, f"{NS}:{metal}_ingot", category="misc")
        shapeless(f"{metal}_nugget_from_ingot", [f"{NS}:{metal}_ingot"], f"{NS}:{metal}_nugget", 9)
        shaped(f"{metal}_block", ["TTT", "TTT", "TTT"], {"T": f"{NS}:{metal}_ingot"}, f"{NS}:{metal}_block", category="building")
        shapeless(f"{metal}_ingot_from_block", [f"{NS}:{metal}_block"], f"{NS}:{metal}_ingot", 9)
    for gem in ("ruby", "amethyst"):
        shaped(f"{gem}_block", ["TTT", "TTT", "TTT"], {"T": f"{NS}:{gem}"}, f"{NS}:{gem}_block", category="building")
        shapeless(f"{gem}_from_block", [f"{NS}:{gem}_block"], f"{NS}:{gem}", 9)

    # ---- Tools ----
    tool_tags = {"sword": "swords", "pickaxe": "pickaxes", "shovel": "shovels", "axe": "axes", "hoe": "hoes"}
    armor_tags = {"helmet": "head_armor", "chestplate": "chest_armor", "leggings": "leg_armor", "boots": "foot_armor"}
    tagged = {t: [] for t in list(tool_tags.values()) + list(armor_tags.values())}
    for mat, label in TOOL_SETS.items():
        for tool in TOOLS:
            name = f"{mat}_{tool}"
            flat_item(name, f"{mat}{tool}", handheld=True)
            NAMES[f"item.{NS}.{name}"] = f"{label} {tool.capitalize()}"
            tagged[tool_tags[tool]].append(f"{NS}:{name}")
    gem = {"emerald": "minecraft:emerald", "ruby": f"{NS}:ruby", "amethyst": f"{NS}:amethyst"}
    for mat, item in gem.items():
        key = {"E": item, "I": "minecraft:stick"}
        shaped(f"{mat}_sword", ["E", "E", "I"], key, f"{NS}:{mat}_sword")
        shaped(f"{mat}_pickaxe", ["EEE", " I ", " I "], key, f"{NS}:{mat}_pickaxe")
        shaped(f"{mat}_shovel", ["E", "I", "I"], key, f"{NS}:{mat}_shovel")
        shaped(f"{mat}_hoe", ["EE", " I", " I"], key, f"{NS}:{mat}_hoe")
        shaped(f"{mat}_axe", ["EE", "EI", " I"], key, f"{NS}:{mat}_axe")
    u = {"T": f"{NS}:titanium_ingot", "U": f"{NS}:uranium_ingot", "I": "minecraft:iron_ingot"}
    shaped("ultimate_sword", ["T", "U", "I"], u, f"{NS}:ultimate_sword")
    shaped("ultimate_pickaxe", ["TUT", " U ", " I "], u, f"{NS}:ultimate_pickaxe")
    shaped("ultimate_shovel", ["U", "T", "I"], u, f"{NS}:ultimate_shovel")
    shaped("ultimate_hoe", ["TU", " I", " I"], u, f"{NS}:ultimate_hoe")
    shaped("ultimate_axe", ["TU", "TI", " I"], u, f"{NS}:ultimate_axe")

    # ---- Armor ----
    for mat, label in ARMOR_SETS.items():
        for piece, theirs in ARMOR.items():
            name = f"{mat}_{piece}"
            flat_item(name, f"{mat}_{theirs}")
            NAMES[f"item.{NS}.{name}"] = f"{label} {piece.capitalize()}"
            tagged[armor_tags[piece]].append(f"{NS}:{name}")
        worn_armor(mat, f"{mat}_1.png", f"{mat}_2.png")
    for _fname, _spec in SPECS:
        for asset, layers in _spec.get("armor_sets", {}).items():
            worn_armor(asset, layers["layer1"], layers["layer2"])
    armor_key = {"emerald": "minecraft:emerald", "ruby": f"{NS}:ruby", "amethyst": f"{NS}:amethyst", "lapis": "minecraft:lapis_block"}
    for mat, item in armor_key.items():
        k = {"X": item}
        shaped(f"{mat}_helmet", ["XXX", "X X"], k, f"{NS}:{mat}_helmet")
        shaped(f"{mat}_chestplate", ["X X", "XXX", "XXX"], k, f"{NS}:{mat}_chestplate")
        shaped(f"{mat}_leggings", ["XXX", "X X", "X X"], k, f"{NS}:{mat}_leggings")
        shaped(f"{mat}_boots", ["X X", "X X"], k, f"{NS}:{mat}_boots")
    shaped("ultimate_helmet", ["TIT", "U U"], u, f"{NS}:ultimate_helmet")
    shaped("ultimate_chestplate", ["I I", "TTT", "UUU"], u, f"{NS}:ultimate_chestplate")
    shaped("ultimate_leggings", ["III", "T T", "U U"], u, f"{NS}:ultimate_leggings")
    shaped("ultimate_boots", ["T T", "U U"], u, f"{NS}:ultimate_boots")

    # Repairing in an anvil: each set with what it's made of.
    repair = {"emerald": ["minecraft:emerald"], "ruby": [f"{NS}:ruby"], "amethyst": [f"{NS}:amethyst"],
              "lapis": ["minecraft:lapis_block"], "ultimate": [f"{NS}:titanium_ingot", f"{NS}:uranium_ingot"]}
    for mat, values in repair.items():
        tag("item", NS, f"{mat}_repair_materials", values)
    for t, values in tagged.items():
        tag("item", "minecraft", t, values)

    # ---- Block tags ----
    tag("block", "minecraft", "mineable/pickaxe", pickaxe_blocks)
    tag("block", "minecraft", "mineable/shovel", shovel_blocks)
    tag("block", NS, "egg_ores", egg_ids)
    tag("item", NS, "egg_ores", egg_ids)
    tag("item", "c", "gems/ruby", [f"{NS}:ruby"])
    tag("item", "c", "ingots/titanium", [f"{NS}:titanium_ingot"])
    tag("item", "c", "ingots/uranium", [f"{NS}:uranium_ingot"])
    tag("item", "c", "nuggets/titanium", [f"{NS}:titanium_nugget"])
    tag("item", "c", "nuggets/uranium", [f"{NS}:uranium_nugget"])

    # ---- Creatures ----
    for src, dest in ENTITY_TEXTURES.items():
        copy_texture(src, f"entity/{dest}")
    for mob, (label, egg) in CREATURES.items():
        NAMES[f"entity.{NS}.{mob}"] = label
        flat_item(f"{mob}_spawn_egg", egg)
        NAMES[f"item.{NS}.{mob}_spawn_egg"] = f"Spawn {label}"
    looting = {"type": "minecraft:enchanted_count_increase", "count": {"type": "minecraft:uniform", "min": 0.0, "max": 1.0},
               "enchantment": "minecraft:looting"}

    def drops(mob, pools):
        write_json(f"{DATA}/loot_table/entities/{mob}.json", {"type": "minecraft:entity", "pools": pools,
                                                              "random_sequence": f"{NS}:entities/{mob}"})

    def pool(item, lo, hi, loot=True):
        mods = [{"type": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}}] + ([looting] if loot else [])
        return {"rolls": 1, "entries": [{"type": "minecraft:item", "name": item, "modifier": mods}]}

    cow = [pool("minecraft:leather", 0, 2), pool("minecraft:beef", 1, 3)]
    drops("apple_cow", cow + [pool("minecraft:apple", 0, 2)])                                    # RedCow: rand(3) + rand(1 + looting)
    drops("golden_apple_cow", cow + [pool("minecraft:apple", 0, 2), pool("minecraft:golden_apple", 1, 1, False)])
    drops("enchanted_golden_apple_cow", cow + [pool("minecraft:apple", 0, 3), pool("minecraft:golden_apple", 2, 2, False),
                                               pool("minecraft:enchanted_golden_apple", 1, 1, False)])
    drops("bird", [pool("minecraft:feather", 0, 2)])
    drops("firefly", [pool(f"{NS}:extreme_torch", 0, 2)])
    drops("rat", [pool("minecraft:rotten_flesh", 0, 2)])
    drops("beaver", [pool("minecraft:porkchop", 0, 2)])
    drops("cassowary", [pool("minecraft:chicken", 2, 4, False)])

    # 26.3 ids are lowercase, so OreSpawn's two capitalised events (Beebuzz, MothraWings) and their files are lowercased.
    sounds_src = {k.lower(): v for k, v in json.load(open(os.path.join(SRC, "sounds.json"), encoding="utf-8")).items()}
    sounds = {}
    for event in SOUNDS:
        files = sounds_src[event]["sounds"]
        sounds[event] = {"sounds": [f"{NS}:{f.lower()}" for f in files]}
        for f in files:
            dest = os.path.join(ASSETS, "sounds", f.lower() + ".ogg")
            os.makedirs(os.path.dirname(dest), exist_ok=True)
            shutil.copyfile(os.path.join(SRC, "sounds", f + ".ogg"), dest)
            track(dest)
    write_json(f"{ASSETS}/sounds.json", sounds)

    # Wither's Combat ragdolls (its tag hook): bodies built from each mob's own model. The tiny insects (butterfly, moth,
    # firefly, cricket) are left out so swatted bugs don't leave bodies that stay forever.
    tag("entity_type", "witherscombatmod", "ragdoll_from_model", [f"{NS}:{m}" for m in RAGDOLLS])

    # ---- Content group specs (tools/content/*.json) ----
    scatter = []
    for fname, spec in SPECS:
        for item_id, it in spec.get("items", {}).items():
            flat_item(item_id, it["texture"], handheld=it.get("handheld", False))
            NAMES[f"item.{NS}.{item_id}"] = it["name"]
        for block_id, b in spec.get("blocks", {}).items():
            spec_block(block_id, b)
        for mob, c in spec.get("creatures", {}).items():
            if c.get("loot"):
                drops(mob, [pool(e["item"], e.get("min", 0), e.get("max", 1), e.get("looting", False)) for e in c["loot"]])
            if c.get("loot_table"):
                write_json(f"{DATA}/loot_table/entities/{mob}.json", c["loot_table"])
            if c.get("ragdoll") == "scatter":
                scatter.append(f"{NS}:{mob}")
        for name, recipe in spec.get("recipes", {}).items():
            write_json(f"{DATA}/recipe/{name}.json", recipe)
        for path, obj in spec.get("worldgen", {}).items():
            write_json(f"{DATA}/worldgen/{path}.json", obj)
        for path, obj in spec.get("data", {}).items():
            write_json(f"{DATA}/{path}", obj)
        for path, obj in spec.get("assets", {}).items():
            write_json(f"{ASSETS}/{path}", obj)
        for src, dest in spec.get("block_textures", {}).items():
            copy_texture(f"textures/blocks/{src}.png", f"block/{dest}.png")
        for key, values in spec.get("tags", {}).items():
            registry, namespace, tag_name = key.split("/", 2)
            path = os.path.join(ROOT, "data", namespace, "tags", registry, f"{tag_name}.json")
            existing = json.load(open(path, encoding="utf-8"))["values"] if os.path.exists(path) else []
            tag(registry, namespace, tag_name, existing + [v for v in values if v not in existing])
        NAMES.update(spec.get("lang", {}))
    if scatter:
        tag("entity_type", "witherscombatmod", "ragdoll_scatter", scatter)

    NAMES["itemGroup.orespawn"] = "OreSpawn"
    write_json(f"{ASSETS}/lang/en_us.json", dict(sorted(NAMES.items())))
    with open(MANIFEST, "w", encoding="utf-8") as fh:
        fh.writelines(rel + chr(10) for rel in sorted(WRITTEN))
    print(f"wrote {len(NAMES)} names; {len(egg_ids)} egg ores")

    # Groups whose assets the spec can't express keep their own script (tools/<group>_assets.py); run them after this one,
    # so a file both write ends up as the group's.
    import subprocess
    tools_dir = os.path.dirname(os.path.abspath(__file__))
    for f in sorted(os.listdir(tools_dir)):
        if f.endswith("_assets.py") and f != "gen_assets.py":
            subprocess.run([sys.executable, "-I", os.path.join(tools_dir, f)], check=True)


if __name__ == "__main__":
    sys.exit(main())
