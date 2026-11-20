"""Checks that the ids the content groups look each other up by (ModItems.lazy, lazyBlock, Lookup.*, OreSpawn.id, and
"orespawn:..." strings) are all registered. Needs build/orespawn-ids.txt from IdDumpTest (a runClientGameTest run).

    python -I tools/id_audit.py
"""
import os
import re
import sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
IDS = os.path.join(ROOT, "build", "orespawn-ids.txt")
DATA = os.path.join(ROOT, "src", "main", "resources", "data", "orespawn")

# Ids that are never registered on purpose (the user left the Girlfriend and Boyfriend out of the port, and Love, their
# item).
SKIPPED = {"girlfriend", "boyfriend"}
SKIPPED_IDS = {"love"}
# OreSpawn's spawn eggs for vanilla mobs that 1.7.10 had none for; 26.3 has its own.
VANILLA_IDS = {"spawn_ender_dragon", "spawn_iron_golem", "spawn_snow_golem", "spawn_wither_boss", "spawn_wither_skeleton"}
# Canonical ids whose port id follows the creature's own id.
RENAMED = {
    "ancient_dried_crystal_cow_spawn_egg": "crystal_apple_cow_egg_ore",
    "spawn_crystal_cow": "crystal_apple_cow_spawn_egg",
    "ancient_dried_kraken_spawn_egg": "the_kraken_egg_ore",
    "uh_no_dont": "the_kraken_spawn_egg",
    "ancient_dried_urchin_spawn_egg": "crystal_urchin_egg_ore",
    "ancient_dried_waterdragon_spawn_egg": "water_dragon_egg_ore",
    "ancient_dried_mobzilla_spawn_egg_part": "mobzilla_egg_part_ore",
    "ancient_dried_the_king_spawn_egg_part": "the_king_egg_part_ore",
    "ancient_dried_the_queen_spawn_egg_part": "the_queen_egg_part_ore",
    "ancient_dried_wither_boss_spawn_egg": "wither_egg_ore",
    "ancient_dried_zombie_pigman_spawn_egg": "zombified_piglin_egg_ore",
    "spawn_brown_ant": "ant_spawn_egg",
    "spawn_giant_spider_robot": "robot_spider_spawn_egg",
    "spawn_red_ant_robot": "robot_red_ant_spawn_egg",
    "spawn_spider_robot_driver": "spider_driver_spawn_egg",
}

PATTERNS = [
    re.compile(r'OreSpawn\.id\("([a-z0-9_]+)"\)'),
    re.compile(r'\blazy\w*\("([a-z0-9_]+)"\)'),
    re.compile(r'Lookup\.\w+\("([a-z0-9_]+)"\)'),
    re.compile(r'"orespawn:([a-z0-9_]+)"'),
]


def port_ids(canonical):
    """The ids an OreSpawn item or block may have in the port: its canonical id, or tools/PORTING.md's exceptions."""
    ids = [canonical]
    if canonical in RENAMED:
        ids.append(RENAMED[canonical])
    m = re.fullmatch(r"ancient_dried_(\w+)_spawn_egg", canonical)
    if m:  # egg ores are <creature>_egg_ore
        ids.append(m.group(1) + "_egg_ore")
    m = re.fullmatch(r"spawn_(\w+)", canonical)
    if m:  # spawn eggs are <creature>_spawn_egg
        ids.append(m.group(1) + "_spawn_egg")
    if canonical.startswith("the_ultimate_"):  # batch 1's ultimate_sword and so on
        ids.append(canonical[len("the_"):])
    if canonical.startswith("lapis_lazuli_"):  # batch 1's lapis armor
        ids.append("lapis_" + canonical[len("lapis_lazuli_"):])
    return ids


def main():
    if not os.path.exists(IDS):
        sys.exit(f"{IDS} is missing: run the game tests first (IdDumpTest writes it)")
    registered = set()
    with open(IDS, encoding="utf-8") as f:
        for line in f:
            kind, path = line.rstrip("\n").split("\t")
            registered.add(path)
    # Data-driven ids (loot tables, worldgen, dimensions, tags) are files, not registry entries.
    for dirpath, _dirs, files in os.walk(DATA):
        for name in files:
            if name.endswith(".json"):
                registered.add(name[:-5])
    missing = {}
    for dirpath, _dirs, files in os.walk(os.path.join(ROOT, "src", "main", "java")):
        for name in files:
            if not name.endswith(".java"):
                continue
            path = os.path.join(dirpath, name)
            text = open(path, encoding="utf-8").read()
            for pattern in PATTERNS:
                for m in pattern.finditer(text):
                    ident = m.group(1)
                    if ident in registered or any(s in ident for s in SKIPPED):
                        continue
                    missing.setdefault(ident, set()).add(os.path.relpath(path, ROOT).replace(os.sep, "/"))
    for ident in sorted(missing):
        print(f"{ident}\t{', '.join(sorted(missing[ident]))}")
    print(f"{len(missing)} ids looked up but not registered")
    # Completeness: every OreSpawn item and block in tools/ids.tsv and ids_extra.tsv should be registered (or deliberately
    # skipped).
    unported = []
    for table in ("ids.tsv", "ids_extra.tsv"):
        with open(os.path.join(ROOT, "tools", table), encoding="utf-8") as f:
            for line in f:
                cols = line.rstrip("\n").split("\t")
                if line.startswith("#") or len(cols) < 2 or cols[1] not in ("item", "block"):
                    continue
                if cols[0] in SKIPPED_IDS or cols[0] in VANILLA_IDS or any(s in cols[0] for s in SKIPPED):
                    continue
                if not any(i in registered for i in port_ids(cols[0])):
                    unported.append(f"{table}: {cols[0]}\t{cols[1]}\t{cols[-1]}")
    print()
    for u in unported:
        print("unported:", u)
    print(f"{len(unported)} OreSpawn items and blocks from ids.tsv and ids_extra.tsv not registered")


if __name__ == "__main__":
    main()
