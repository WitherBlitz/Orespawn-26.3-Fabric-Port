"""Writes the Farming group's block resources: block textures copied from OreSpawn 1.7.10-20.3 (textures/blocks), and the
blockstates, block models, block item models, block loot tables and worldgen (the wild plants of OreSpawnWorld) that
tools/content/farming.json can't express. Items, recipes, tags and names are in that spec, for gen_assets.py.

    python -I tools/farming_assets.py [-v] [path to the unzipped OreSpawn jar]   (default: D:/Destinymods/_src_orespawn/zip)

gen_assets.py clears the folders these files go in, so run this after it. Everything written is named after one of
Farming's block ids (orespawn.content.Farming). OreSpawn has no license; the user allowed using its textures for this
private pack (2026-11-19).
"""
import json
import os
import shutil
import sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources")
NS = "orespawn"
ASSETS = os.path.join(ROOT, "assets", NS)
DATA = os.path.join(ROOT, "data", NS)
ARGS = [a for a in sys.argv[1:] if not a.startswith("-")]
SRC = os.path.join(ARGS[0] if ARGS else "D:/Destinymods/_src_orespawn/zip", "assets", "orespawn")

WRITTEN = []

# BlockCrops plants: block id -> OreSpawn's texture prefix (<prefix>_0 .. _3), the crop and seed it drops, and how many.
# Any age drops (seed_min-seed_max) of what getItemDropped gives (the seed while growing, the crop when grown; the creature
# plants' crop is nothing), and a grown one 3 more tries at 8 in 15 for a seed (1.7.10 BlockCrops.getDrops).
CROPS = {
    "radish_plant": ("radish", "orespawn:radish", "orespawn:radish", 2, 5),
    "rice_plant": ("rice", "orespawn:rice", "orespawn:rice", 2, 5),
    "strawberry_plant": ("strawberry", "orespawn:strawberry", "orespawn:strawberry_plant", 1, 5),
    "butterfly_plant": ("butterfly", None, "orespawn:butterfly_plant", 1, 5),
    "moth_plant": ("moth", None, "orespawn:moth_plant", 1, 5),
    "firefly_plant": ("firefly", None, "orespawn:firefly_plant", 1, 5),
    "mosquito_plant": ("mosquito", None, "orespawn:mosquito_plant", 1, 5),
}
# The texture each crop age shows (BlockCrops.getIcon: 0-1, 2-3, 4-6, 7).
AGE_STAGE = [0, 0, 1, 1, 2, 2, 2, 3]
# Reed-like crops (one block, stage 0-3 = OreSpawn's Plant1-4): texture prefix, crop item, how many a ripe block drops.
STALKS = {
    "corn_plant": ("corn", "orespawn:corn", 1, 2),
    "tomato_plant": ("tomato", "orespawn:tomato", 2, 5),
    "lettuce_plant": ("lettuce", "orespawn:lettuce", 2, 4),
    "quinoa_plant": ("quinoa", "orespawn:quinoa", 3, 5),
}
LEAVES = {"apple_leaves": "leaves_apple", "cherry_leaves": "leaves_cherry", "peach_leaves": "leaves_peach",
          "scary_leaves": "leaves_scary", "experience_leaves": "leaves_experience"}
TORCHES = {"extreme_torch": "extremetorch", "creeper_repellent": "creeperrepellent", "kraken_repellent": "krakenrepellent"}
SLICED = {"pizza": "pizza", "duct_tape": "ducttape"}
FOLIAGE_ITEM_TINT = -12012264  # 1.7.10 BlockLeaves.getRenderColor: ColorizerFoliage.getFoliageColorBasic()


def write_json(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")
    WRITTEN.append(os.path.relpath(path, ROOT).replace(os.sep, "/"))


def texture(theirs, ours):
    src = os.path.join(SRC, "textures", "blocks", theirs + ".png")
    if not os.path.exists(src):
        raise SystemExit(f"missing OreSpawn texture {src}")
    dest = os.path.join(ASSETS, "textures", "block", ours + ".png")
    os.makedirs(os.path.dirname(dest), exist_ok=True)
    shutil.copyfile(src, dest)
    WRITTEN.append(os.path.relpath(dest, ROOT).replace(os.sep, "/"))


def model(name, obj):
    write_json(f"{ASSETS}/models/block/{name}.json", obj)


def blockstate(name, obj):
    write_json(f"{ASSETS}/blockstates/{name}.json", obj)


def item_model(name, model_ref, tint=None):
    m = {"type": "minecraft:model", "model": model_ref}
    if tint is not None:
        m["tints"] = [{"type": "minecraft:constant", "value": tint}]
    write_json(f"{ASSETS}/items/{name}.json", {"model": m})


def flat_block_item(name, texture_ref):
    """An item drawn flat from the block's texture (1.7.10 drew torches and reeds this way)."""
    write_json(f"{ASSETS}/models/item/{name}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": texture_ref}})
    item_model(name, f"{NS}:item/{name}")


def loot(name, pools, decay=False):
    table = {"type": "minecraft:block", "pools": pools, "random_sequence": f"{NS}:blocks/{name}"}
    if decay:
        table["modifier"] = {"type": "minecraft:explosion_decay"}
    write_json(f"{DATA}/loot_table/blocks/{name}.json", table)


def count(lo, hi):
    return {"type": "minecraft:set_count", "count": lo if lo == hi else {"type": "minecraft:uniform", "min": lo, "max": hi}}


def state_is(prop, value):
    return {"type": "minecraft:match_block", "state": {prop: str(value)}}


def self_drop(name):
    loot(name, [{"rolls": 1, "condition": {"type": "minecraft:survives_explosion"},
                 "entries": [{"type": "minecraft:item", "name": f"{NS}:{name}"}]}])


# ---- Worldgen helpers (26.3: worldgen/feature holds the configured feature, worldgen/placed_feature its placement) ----
def state(block, **props):
    s = {"id": block}
    if props:
        s["properties"] = {k: str(v) for k, v in props.items()}
    return s


def column(*layers):
    """block_column straight up from the spot, no checks of its own (OreSpawn set its plants without any)."""
    return {"type": "minecraft:block_column", "direction": "up", "allowed_placement": {"type": "minecraft:true"},
            "prioritize_tip": False, "layers": [{"height": h, "provider": s} for s, h in layers]}


def inline(feature, placement=None):
    return {"feature": feature, "placement": placement or []}


def feature(name, obj):
    write_json(f"{DATA}/worldgen/feature/{name}.json", obj)


def placed(name, placement):
    write_json(f"{DATA}/worldgen/placed_feature/{name}.json", {"feature": f"{NS}:{name}", "placement": placement})


def on_grass(chance, tries, air_above=0):
    """OreSpawnWorld's pattern: one chunk in `chance`, `tries` random columns; going down from y 100 to 41 through air,
    plant on the first grass block (so not under leaves or on tall grass), with `air_above` more air blocks above."""
    predicates = [{"type": "minecraft:matching_blocks", "blocks": "minecraft:grass_block", "offset": [0, -1, 0]},
                  {"type": "minecraft:height_range", "min_inclusive": {"absolute": 41}, "max_inclusive": {"absolute": 100}}]
    predicates += [{"type": "minecraft:matching_blocks", "blocks": "minecraft:air", "offset": [0, i, 0]} for i in range(1, air_above + 1)]
    return [{"type": "minecraft:rarity_filter", "chance": chance}, {"type": "minecraft:count", "count": tries},
            {"type": "minecraft:in_square"}, {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"},
            {"type": "minecraft:biome"},
            {"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:all_of", "predicates": predicates}}]


def main():
    # ---- Crops ----
    for name, (prefix, crop, seed, lo, hi) in CROPS.items():
        for i in range(4):
            texture(f"{prefix}_{i}", f"{name}_stage{i}")
            model(f"{name}_stage{i}", {"parent": "minecraft:block/crop", "textures": {"crop": f"{NS}:block/{name}_stage{i}"}})
        blockstate(name, {"variants": {f"age={a}": {"model": f"{NS}:block/{name}_stage{AGE_STAGE[a]}"} for a in range(8)}})
        grown = state_is("age", 7)
        if crop:
            first = {"type": "minecraft:alternatives", "children": [
                {"type": "minecraft:item", "name": crop, "condition": grown, "modifier": count(lo, hi)},
                {"type": "minecraft:item", "name": seed, "modifier": count(lo, hi)}]}
        else:
            first = {"type": "minecraft:item", "name": seed, "condition": {"type": "minecraft:inverted", "term": grown},
                     "modifier": count(lo, hi)}
        loot(name, [{"rolls": 1, "entries": [first]},
                    {"rolls": 1, "condition": grown, "entries": [{"type": "minecraft:item", "name": seed, "modifier": {
                        "type": "minecraft:set_count", "count": {"type": "minecraft:binomial", "n": 3, "p": 8.0 / 15.0}}}]}],
             decay=True)

    # ---- Reed-like crops ----
    for name, (prefix, crop, lo, hi) in STALKS.items():
        for i in range(4):
            texture(f"{prefix}_{i}", f"{name}_stage{i}")
            model(f"{name}_stage{i}", {"parent": "minecraft:block/tinted_cross", "textures": {"cross": f"{NS}:block/{name}_stage{i}"}})
        blockstate(name, {"variants": {f"stage={i}": {"model": f"{NS}:block/{name}_stage{i}"} for i in range(4)}})
        loot(name, [{"rolls": 1, "condition": state_is("stage", 3),
                     "entries": [{"type": "minecraft:item", "name": crop, "modifier": count(lo, hi)}]}], decay=True)

    # ---- Experience Tree Sapling (a reed: tinted cross, flat item, drops itself) ----
    texture("experiencesapling", "experience_tree_sapling")
    model("experience_tree_sapling", {"parent": "minecraft:block/tinted_cross", "textures": {"cross": f"{NS}:block/experience_tree_sapling"}})
    blockstate("experience_tree_sapling", {"variants": {"": {"model": f"{NS}:block/experience_tree_sapling"}}})
    flat_block_item("experience_tree_sapling", f"{NS}:block/experience_tree_sapling")
    self_drop("experience_tree_sapling")

    # ---- Leaves (foliage-tinted; shears give the leaves, the fruit is dropped by the block itself) ----
    for name, theirs in LEAVES.items():
        texture(theirs, name)
        model(name, {"parent": "minecraft:block/leaves", "textures": {"all": f"{NS}:block/{name}"}})
        blockstate(name, {"variants": {"": {"model": f"{NS}:block/{name}"}}})
        item_model(name, f"{NS}:block/{name}", FOLIAGE_ITEM_TINT)
        loot(name, [{"rolls": 1, "condition": "minecraft:tool/can_shear", "entries": [{"type": "minecraft:item", "name": f"{NS}:{name}"}]}])

    # ---- Torches (BlockTorch: the whole texture on crossed planes, as 26.3's torch template draws it) ----
    for name, theirs in TORCHES.items():
        texture(theirs, name)
        model(name, {"parent": "minecraft:block/template_torch", "textures": {"torch": f"{NS}:block/{name}"}})
        model(f"{name}_wall", {"parent": "minecraft:block/template_torch_wall", "textures": {"torch": f"{NS}:block/{name}"}})
        blockstate(name, {"variants": {"": {"model": f"{NS}:block/{name}"}}})
        wall = f"{NS}:block/{name}_wall"
        blockstate(f"{name}_wall", {"variants": {"facing=east": {"model": wall}, "facing=north": {"model": wall, "y": 270},
                                                "facing=south": {"model": wall, "y": 90}, "facing=west": {"model": wall, "y": 180}}})
        flat_block_item(name, f"{NS}:block/{name}")
        self_drop(name)

    # ---- Pizza and Duct Tape: 4-pixel cakes of six slices (their items are flat, in the spec); they drop nothing ----
    for name, theirs in SLICED.items():
        for part in ("top", "bottom", "side", "inner"):
            texture(f"{theirs}_{part}", f"{name}_{part}")
        tex = {"particle": f"{NS}:block/{name}_side", "bottom": f"{NS}:block/{name}_bottom", "top": f"{NS}:block/{name}_top",
               "side": f"{NS}:block/{name}_side", "inside": f"{NS}:block/{name}_inner"}
        variants = {}
        for bite in range(6):
            west = "#inside" if bite > 0 else "#side"
            faces = {"down": {"texture": "#bottom", "cullface": "down"}, "up": {"texture": "#top"},
                     "north": {"texture": "#side"}, "south": {"texture": "#side"}, "west": {"texture": west},
                     "east": {"texture": "#side"}}
            part = name if bite == 0 else f"{name}_slice{bite}"
            model(part, {"textures": tex, "elements": [{"from": [1 + 2 * bite, 0, 1], "to": [15, 4, 15], "faces": faces}]})
            variants[f"bites={bite}"] = {"model": f"{NS}:block/{part}"}
        blockstate(name, {"variants": variants})
        loot(name, [])

    # ---- Worldgen: OreSpawnWorld.generateSurface (and generateNether's mosquito plants) ----
    # addStrawberries: 1 chunk in 20, 5 tries.
    feature("strawberry_plant_patch", column((state("orespawn:strawberry_plant", age=0), 1)))
    placed("strawberry_plant_patch", on_grass(20, 5))
    # addCorn: 1 chunk in 35, 6 tries, 9 free blocks above; a plant 1-5 high: a top alone, a stalk and its top, or a stalk,
    # ripe corn and its top.
    corn = [column((state("orespawn:corn_plant", stage=0, age=0), 1))]
    corn.append(column((state("orespawn:corn_plant", stage=1, age=0), 1), (state("orespawn:corn_plant", stage=0, age=0), 1)))
    for h in range(3, 6):
        corn.append(column((state("orespawn:corn_plant", stage=1, age=0), 1), (state("orespawn:corn_plant", stage=3, age=0), h - 1),
                           (state("orespawn:corn_plant", stage=0, age=0), 1)))
    feature("corn_plant_patch", {"type": "minecraft:simple_random_selector", "features": [inline(f) for f in corn]})
    placed("corn_plant_patch", on_grass(35, 6, air_above=9))
    # addTomatoes: 1 chunk in 70, 5 tries, a plant 1-3 high (the 3-high one ripe).
    tomato = [column((state("orespawn:tomato_plant", stage=0, age=0), 1)),
              column((state("orespawn:tomato_plant", stage=1, age=0), 1), (state("orespawn:tomato_plant", stage=0, age=0), 1)),
              column((state("orespawn:tomato_plant", stage=2, age=0), 1), (state("orespawn:tomato_plant", stage=3, age=0), 2),
                     (state("orespawn:tomato_plant", stage=0, age=0), 1))]
    feature("tomato_plant_patch", {"type": "minecraft:simple_random_selector", "features": [inline(f) for f in tomato]})
    placed("tomato_plant_patch", on_grass(70, 5, air_above=9))
    # addVeggies: 1 chunk in 15, 8 tries, one of six: carrots, potatoes, a radish, a lettuce, 1 in 10 a melon stem, nothing.
    # (OreSpawn's sixth was a 1 in 50 Duplicator Tree log, not ported.)
    veggies = [inline(column((state("minecraft:carrots", age=0), 1))), inline(column((state("minecraft:potatoes", age=0), 1))),
               inline(column((state("orespawn:radish_plant", age=0), 1))),
               inline(column((state("orespawn:lettuce_plant", stage=0, age=0), 1))),
               inline(column((state("minecraft:melon_stem", age=0), 1)), [{"type": "minecraft:rarity_filter", "chance": 10}]),
               inline({"type": "minecraft:no_op"})]
    feature("radish_plant_veggies", {"type": "minecraft:simple_random_selector", "features": veggies})
    placed("radish_plant_veggies", on_grass(15, 8))
    # addButterfliesAndMoths: 1 chunk in 10, 4 tries, a butterfly, moth or firefly plant.
    critters = [column((state(f"orespawn:{p}_plant", age=0), 1)) for p in ("butterfly", "moth", "firefly")]
    feature("butterfly_plant_critters", {"type": "minecraft:simple_random_selector", "features": [inline(f) for f in critters]})
    placed("butterfly_plant_critters", on_grass(10, 4))
    # addMosquitos: 1 chunk in 25, 2 tries.
    feature("mosquito_plant_patch", column((state("orespawn:mosquito_plant", age=0), 1)))
    placed("mosquito_plant_patch", on_grass(25, 2))
    # addNetherMosquitos: 1 chunk in 25, 3 tries, on netherrack under open air between y 21 and 90 (OreSpawn took the highest
    # such spot below 90; this looks down up to 32 blocks from a random height).
    feature("mosquito_plant_nether", column((state("orespawn:mosquito_plant", age=0), 1)))
    placed("mosquito_plant_nether", [
        {"type": "minecraft:rarity_filter", "chance": 25}, {"type": "minecraft:count", "count": 3}, {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform", "min_inclusive": {"absolute": 21},
                                                       "max_inclusive": {"absolute": 90}}},
        {"type": "minecraft:environment_scan", "direction_of_search": "down", "max_steps": 32, "target_condition": {
            "type": "minecraft:all_of", "predicates": [
                {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"},
                {"type": "minecraft:matching_blocks", "blocks": "minecraft:netherrack", "offset": [0, -1, 0]}]}},
        {"type": "minecraft:biome"}])

    print(f"farming_assets: wrote {len(WRITTEN)} files")
    if "-v" in sys.argv:
        for f in WRITTEN:
            print(" ", f)


if __name__ == "__main__":
    sys.exit(main())
