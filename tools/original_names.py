"""Writes the name tables the port needs to run OreSpawn's own dungeon code from the original OreSpawn 1.7.10-20.3 file
(orespawn.original.OriginalDungeons), into src/main/resources/orespawn/original/:

- legacy_blocks.tsv: each 1.7.10 vanilla block (its Blocks field's SRG name, numeric id and name) and the 26.3 item for
  each of its 16 metadata values (from 26.3's own flattening table, BlockStateData).
- legacy_items.tsv: each 1.7.10 vanilla item (its Items field's SRG name) and its 26.3 item for each metadata value.
- orespawn_fields.tsv: the OreSpawnMain fields the dungeon code reads, and what each stands for in the port:
  "block <orespawn id>", "item <item id>", "dungeon <OreSpawn class>" or "config <value>".

These are names only, no OreSpawn code. They reuse tools/dungeons/port_dungeons.py's tables and rules.

    python -I tools/original_names.py <26.3 sources: the folder holding net/minecraft> <decompiled OreSpawn: folder holding danger/orespawn>
"""
import importlib.util
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, "..", "src", "main", "resources", "orespawn", "original")
MCSRC, DECOMPILED = sys.argv[1], os.path.join(sys.argv[2], "danger", "orespawn")

spec = importlib.util.spec_from_file_location("port_dungeons", os.path.join(HERE, "dungeons", "port_dungeons.py"))
pd = importlib.util.module_from_spec(spec)
sys.argv = [sys.argv[0]]
spec.loader.exec_module(pd)

table = pd.parse_block_state_data(os.path.join(MCSRC, "net", "minecraft", "util", "datafix", "fixes", "BlockStateData.java"))
os.makedirs(OUT, exist_ok=True)

with open(os.path.join(OUT, "legacy_blocks.tsv"), "w", encoding="utf-8", newline="\n") as f:
    f.write("# 1.7.10 Blocks field, block id, name, then the 26.3 item for metadata 0-15 (see tools/original_names.py)\n")
    for srg, (bid, name) in sorted(pd.BLOCKS.items(), key=lambda kv: kv[1][0]):
        items = []
        for meta in range(16):
            state = pd.legacy_state(table, bid, meta)
            item = state.split("[")[0] if state else "minecraft:air"
            items.append(pd.BLOCK_ITEMS.get(item, item))
        f.write("\t".join([srg, str(bid), name] + items) + "\n")

with open(os.path.join(OUT, "legacy_items.tsv"), "w", encoding="utf-8", newline="\n") as f:
    f.write("# 1.7.10 Items field, the 26.3 item for any metadata (- for none), then metadata=item for those that differ"
            " (see tools/original_names.py)\n")
    for srg, name in sorted(pd.ITEMS.items()):
        if name == "dye":
            row = ["minecraft:" + pd.DYES[0]] + [f"{m}=minecraft:{d}" for m, d in enumerate(pd.DYES) if m]
        elif name == "spawn_egg":
            row = ["-"] + [f"{m}=minecraft:{egg}" for m, egg in sorted(pd.SPAWN_EGG_IDS.items())]
        else:
            row = ["minecraft:" + name]
        f.write("\t".join([srg] + row) + "\n")

# The OreSpawnMain fields the code that runs from the original file reads: the four dungeon classes, and the dungeon
# placement methods of OreSpawnWorld.
texts = [open(os.path.join(DECOMPILED, c + ".java"), encoding="utf-8").read()
         for c in ("GenericDungeon", "BasiliskMaze", "NightmareDungeon", "RubyBirdDungeon")]
world = open(os.path.join(DECOMPILED, "OreSpawnWorld.java"), encoding="utf-8").read().replace("\r\n", "\n")
texts += pd.world_methods(world, pd.PLACEMENT)
used = sorted({m for t in texts for m in re.findall(r"\bOreSpawnMain\.(\w+)\b(?!\s*\()", t)})
main = open(os.path.join(DECOMPILED, "OreSpawnMain.java"), encoding="utf-8").read()
types = dict((n, t) for t, n in re.findall(r"public static (?:final )?([\w.]+) (\w+)\s*[;=]", main))
DUNGEONS = {"MyDungeon": "GenericDungeon", "RubyDungeon": "RubyBirdDungeon", "BMaze": "BasiliskMaze"}
CONFIG = {"LessLag": 0, "RotatorEnable": 1, "UrchinEnable": 1, "SpiderDriverEnable": 1, "DisableOverworldDungeons": 0, "MosquitoEnable": 1}
def kind(cls):
    """'block' or 'item': what an OreSpawn class (or a vanilla one by simple name) is, by its superclasses."""
    for _ in range(20):
        path = os.path.join(DECOMPILED, cls + ".java")
        if not os.path.exists(path):
            break
        src = open(path, encoding="utf-8").read()
        m = re.search(r"\bclass " + cls + r" extends (\w+)", src)
        if not m:
            break
        parent = m.group(1)
        imp = re.search(r"import ([\w.]+)\." + parent + ";", src)
        if imp and imp.group(1) == "net.minecraft.block":
            return "block"
        if imp and imp.group(1) == "net.minecraft.item":
            return "item"
        cls = parent
    if cls.startswith("Block"):
        return "block"
    if cls.startswith("Item"):
        return "item"
    raise SystemExit(f"don't know whether {cls} is a block or an item")


porter = pd.Porter()
porter.table = table
rows = []
for field in used:
    if field in DUNGEONS:
        rows.append((field, "dungeon", DUNGEONS[field]))
    elif field in CONFIG:
        rows.append((field, "config", str(CONFIG[field])))
    elif kind(types[field]) == "block":
        rows.append((field, "block", porter.orespawn_id(field)))
    else:
        rows.append((field, "item", porter.item_id("OreSpawnMain." + field)))
with open(os.path.join(OUT, "orespawn_fields.tsv"), "w", encoding="utf-8", newline="\n") as f:
    f.write("# OreSpawnMain fields OreSpawn's dungeon code reads, and what they are in the port (see tools/original_names.py)\n")
    for r in rows:
        f.write("\t".join(r) + "\n")
print(len(pd.BLOCKS), "blocks,", len(pd.ITEMS), "items,", len(rows), "OreSpawnMain fields")
