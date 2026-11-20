"""Writes the Crystal group's block assets from OreSpawn 1.7.10-20.3: the block textures (copied from the unzipped jar),
blockstates, block and item models, and the blocks' loot tables. Everything else of the group (items, the Crystal Apple
Cow, recipes, tags, names, the biome) is in tools/content/crystal.json, which tools/gen_assets.py reads.

    python -I tools/crystal_assets.py [path to the unzipped OreSpawn jar]   (default: D:/Destinymods/_src_orespawn/zip)

The files written here are hand-written as far as gen_assets.py is concerned (it never touches them). Keep the block
list in step with orespawn.content.Crystal. OreSpawn has no license; the user allowed using its textures for this
private pack (2026-11-19).
"""
import json
import os
import sys

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(HERE, "..", "src", "main", "resources")
NS = "orespawn"
ASSETS = os.path.join(ROOT, "assets", NS)
DATA = os.path.join(ROOT, "data", NS)
SRC = os.path.join(sys.argv[1] if len(sys.argv) > 1 else "D:/Destinymods/_src_orespawn/zip", "assets", "orespawn", "textures", "blocks")

# BlockCrystalLeaves' colour (getRenderColor / colorMultiplier 0xDDDDDD), baked into the leaves' textures.
LEAF_TINT = (0xDD, 0xDD, 0xDD)


def write_json(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def texture(src, dest, tint=None):
    path = os.path.join(SRC, src + ".png")
    if not os.path.exists(path):
        raise SystemExit(f"missing OreSpawn texture {path}")
    image = Image.open(path).convert("RGBA")
    if tint:
        r, g, b, a = image.split()
        r, g, b = (band.point(lambda v, t=t: v * t // 255) for band, t in zip((r, g, b), tint))
        image = Image.merge("RGBA", (r, g, b, a))
    out = os.path.join(ASSETS, "textures", "block", dest + ".png")
    os.makedirs(os.path.dirname(out), exist_ok=True)
    image.save(out)


def ref(name):
    return f"{NS}:block/{name}"


def model(name, parent, textures):
    write_json(f"{ASSETS}/models/block/{name}.json", {"parent": parent, "textures": textures})


def single_state(name, model_name=None):
    write_json(f"{ASSETS}/blockstates/{name}.json", {"variants": {"": {"model": ref(model_name or name)}}})


def block_item(name):
    write_json(f"{ASSETS}/items/{name}.json", {"model": {"type": "minecraft:model", "model": ref(name)}})


def flat_item(name, tex):
    write_json(f"{ASSETS}/models/item/{name}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": ref(tex)}})
    write_json(f"{ASSETS}/items/{name}.json", {"model": {"type": "minecraft:model", "model": f"{NS}:item/{name}"}})


def loot(name, pools):
    write_json(f"{DATA}/loot_table/blocks/{name}.json", {"type": "minecraft:block", "pools": pools, "random_sequence": f"{NS}:blocks/{name}"})


def drops(name, item=None, chance=None):
    """Drops one of `item` (itself by default), maybe only with a chance, if it survives an explosion."""
    entry = {"type": "minecraft:item", "name": f"{NS}:{item or name}"}
    conditions = [{"type": "minecraft:survives_explosion"}]
    if chance is not None:
        conditions.append({"type": "minecraft:random_chance", "chance": chance})
    pool = {"rolls": 1, "condition": conditions[0] if len(conditions) == 1 else {"type": "minecraft:all_of", "terms": conditions},
            "entries": [entry]}
    loot(name, [pool])


def cube(name, src, tint=None):
    texture(src, name, tint)
    model(name, "minecraft:block/cube_all", {"all": ref(name)})
    single_state(name)
    block_item(name)


def bottom_top(name, side, top, bottom):
    texture(side, name)
    texture(top, name + "_top")
    texture(bottom, name + "_bottom")
    model(name, "minecraft:block/cube_bottom_top", {"side": ref(name), "top": ref(name + "_top"), "bottom": ref(name + "_bottom")})
    single_state(name)
    block_item(name)


def cross(name, src):
    texture(src, name)
    model(name, "minecraft:block/cross", {"cross": ref(name)})
    single_state(name)
    flat_item(name, name)


def main():
    # ---- Full blocks ----
    for name, src in (("kyanite", "crystalstone"), ("crystal_energy", "crystalcoal"), ("pink_tourmaline", "crystalcrystal"),
                      ("tigers_eye", "tigerseye"), ("crystalized_rats", "crystalrat"), ("crystalized_fairies", "crystalfairy"),
                      ("pink_tourmaline_block", "crystalpink_block"), ("tigers_eye_block", "tigerseye_block"),
                      ("crystal_planks", "crystalplanks")):
        cube(name, src)
    for name, src in (("red_crystal_tree_leaves", "crystaltreeleaves"), ("yellow_crystal_tree_leaves", "crystaltreeleaves2"),
                      ("blue_crystal_tree_leaves", "crystaltreeleaves3")):
        cube(name, src, LEAF_TINT)
    bottom_top("crystal_grass", "crystalgrass_side", "crystalgrass_top", "crystalgrass_bottom")
    bottom_top("crystal_termite_nest", "crystaltermite_side", "crystaltermite_top", "crystaltermite_bottom")

    # ---- Crystal Tree Wood: a log ----
    texture("crystaltreelog", "crystal_tree_wood")
    texture("crystaltreelog_top", "crystal_tree_wood_top")
    wood = {"side": ref("crystal_tree_wood"), "end": ref("crystal_tree_wood_top")}
    model("crystal_tree_wood", "minecraft:block/cube_column", wood)
    model("crystal_tree_wood_horizontal", "minecraft:block/cube_column_horizontal", wood)
    write_json(f"{ASSETS}/blockstates/crystal_tree_wood.json", {"variants": {
        "axis=x": {"model": ref("crystal_tree_wood_horizontal"), "x": 90, "y": 90},
        "axis=y": {"model": ref("crystal_tree_wood")},
        "axis=z": {"model": ref("crystal_tree_wood_horizontal"), "x": 90}}})
    block_item("crystal_tree_wood")

    # ---- Crystal Workbench: OreSpawn's icons by side (top; north and west the "_bottom" picture; the rest "_side") ----
    texture("crystalworkbench_side", "crystal_workbench")
    texture("crystalworkbench_top", "crystal_workbench_top")
    texture("crystalworkbench_bottom", "crystal_workbench_front")
    side, front = ref("crystal_workbench"), ref("crystal_workbench_front")
    model("crystal_workbench", "minecraft:block/cube", {"particle": side, "up": ref("crystal_workbench_top"), "down": side,
                                                         "north": front, "west": front, "south": side, "east": side})
    single_state("crystal_workbench")
    block_item("crystal_workbench")

    # ---- Crystal Furnace: top and bottom "_top", the front "_front_off" lit or not (as OreSpawn drew it) ----
    texture("crystalfurnace_side", "crystal_furnace")
    texture("crystalfurnace_front_off", "crystal_furnace_front")
    texture("crystalfurnace_top", "crystal_furnace_top")
    side, top = ref("crystal_furnace"), ref("crystal_furnace_top")
    model("crystal_furnace", "minecraft:block/cube", {"particle": side, "up": top, "down": top, "north": ref("crystal_furnace_front"),
                                                       "south": side, "west": side, "east": side})
    rotations = {"north": 0, "east": 90, "south": 180, "west": 270}
    variants = {}
    for facing, y in rotations.items():
        for lit in ("false", "true"):
            v = {"model": ref("crystal_furnace")}
            if y:
                v["y"] = y
            variants[f"facing={facing},lit={lit}"] = v
    write_json(f"{ASSETS}/blockstates/crystal_furnace.json", {"variants": variants})
    block_item("crystal_furnace")

    # ---- Plants ----
    for name, src in (("red_crystal_tree_sapling", "crystalsapling"), ("yellow_crystal_tree_sapling", "crystalsapling2"),
                      ("blue_crystal_tree_sapling", "crystalsapling3"), ("red_crystal_flower", "crystalflower_red"),
                      ("green_crystal_flower", "crystalflower_green"), ("blue_crystal_flower", "crystalflower_blue"),
                      ("yellow_crystal_flower", "crystalflower_yellow")):
        cross(name, src)

    # ---- Crystal Torch, standing and on a wall ----
    texture("crystaltorch", "crystal_torch")
    model("crystal_torch", "minecraft:block/template_torch", {"torch": ref("crystal_torch")})
    model("crystal_wall_torch", "minecraft:block/template_torch_wall", {"torch": ref("crystal_torch")})
    single_state("crystal_torch")
    write_json(f"{ASSETS}/blockstates/crystal_wall_torch.json", {"variants": {
        "facing=east": {"model": ref("crystal_wall_torch")},
        "facing=north": {"model": ref("crystal_wall_torch"), "y": 270},
        "facing=south": {"model": ref("crystal_wall_torch"), "y": 90},
        "facing=west": {"model": ref("crystal_wall_torch"), "y": 180}}})
    flat_item("crystal_torch", "crystal_torch")

    # ---- Loot tables ----
    for name in ("kyanite", "crystal_grass", "crystal_energy", "pink_tourmaline", "crystalized_rats", "crystalized_fairies",
                 "pink_tourmaline_block", "tigers_eye_block", "crystal_termite_nest", "crystal_planks", "crystal_tree_wood",
                 "crystal_workbench", "crystal_furnace", "red_crystal_flower", "green_crystal_flower", "blue_crystal_flower",
                 "yellow_crystal_flower", "crystal_torch", "red_crystal_tree_sapling", "blue_crystal_tree_sapling"):
        drops(name)
    drops("tigers_eye", chance=0.5)                                      # quantityDropped: rand.nextInt(2)
    drops("yellow_crystal_tree_sapling", "blue_crystal_tree_sapling")    # OreSpawn's getItemDropped slip
    sharp = {"type": "minecraft:any_of", "terms": ["minecraft:tool/can_shear", "minecraft:tool/can_silk_touch"]}
    for leaves, sapling in (("red_crystal_tree_leaves", "red_crystal_tree_sapling"), ("yellow_crystal_tree_leaves", "yellow_crystal_tree_sapling"),
                            ("blue_crystal_tree_leaves", "blue_crystal_tree_sapling")):
        loot(leaves, [
            {"rolls": 1, "condition": sharp, "entries": [{"type": "minecraft:item", "name": f"{NS}:{leaves}"}]},
            {"rolls": 1, "condition": {"type": "minecraft:inverted", "term": sharp}, "entries": [
                {"type": "minecraft:item", "name": f"{NS}:crystal_apple", "condition": {"type": "minecraft:random_chance", "chance": 0.01}}]},
            {"rolls": 1, "condition": {"type": "minecraft:inverted", "term": sharp}, "entries": [
                {"type": "minecraft:item", "name": f"{NS}:{sapling}", "condition": {"type": "minecraft:random_chance", "chance": 0.02}}]},
        ])
    print("wrote the Crystal group's block assets")


if __name__ == "__main__":
    sys.exit(main())
