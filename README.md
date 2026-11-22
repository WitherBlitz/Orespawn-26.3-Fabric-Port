# OreSpawn 26.3 Fabric Port

A port of OreSpawn 1.7.10-20.3, by TheyCallMeDanger, to Minecraft 26.3 on Fabric. Its mod id is `orespawn`.

**This repository holds none of OreSpawn's files, and neither does the jar it builds.** The game needs your own copy of
the original OreSpawn 1.7.10-20.3 in the mods folder. The port reads OreSpawn's textures, sounds, model shapes and
dungeon code from that file when the game starts.

## What you need

- Minecraft 26.3, Fabric Loader 0.20.1 or newer, Fabric API and Java 25.
- This mod's jar.
- The original OreSpawn 1.7.10-20.3, the file `orespawn-1.7.10-20.3.zip` from that mod's own download. Put it in the
  same mods folder as this mod.
  - Fabric ignores it: Fabric only loads `.jar` files that have a `fabric.mod.json`.
  - The port recognises the file by what's inside it, so it can have any name.
  - Without it, the game stops at startup with a message saying what to download and where to put it.
  - The port was made from the release whose SHA-1 is `d43dbe9a400dc8df06418da3e04d36422b2176d7`. Any other file
    works too, but the port logs a warning.
- On a server, the original file goes in the server's mods folder too, because the dungeon code comes from it.

## How the port uses the original file

- **Textures and sounds:** a built-in resource pack called "OreSpawn 1.7.10" serves them straight from the file
  (`orespawn.client.original.OriginalPack`).
  - The list in `src/main/resources/orespawn/original/assets.tsv` says which OreSpawn file each texture and sound comes
    from.
  - Most are used unchanged. Three leaf textures are darkened. The baby armor textures are 26.3's baby layout, built
    from OreSpawn's two armor layers.
  - `sounds.json` is made from OreSpawn's own.
  - The pack sits below every other pack, so a resource pack can still replace any of these.
- **Model shapes:** `orespawn.client.original.OriginalModels` reads each OreSpawn model's constructor from the file's
  bytecode: its parts, boxes, pivots, starting rotations and texture offsets. The port's model classes only hold the
  animation and the way some parts are grouped.
- **Dungeons:** `orespawn.original.OriginalDungeons` runs OreSpawn's own dungeon code from the file, unchanged:
  - the builders GenericDungeon, BasiliskMaze, NightmareDungeon and RubyBirdDungeon;
  - the dungeon placement methods of OreSpawnWorld.

  It runs them in a class loader of their own. Small stand-ins for the 1.7.10 classes they call
  (`orespawn.original.legacy`) place everything into the 26.3 world through the port's `DWorld`. The name tables in
  `src/main/resources/orespawn/original/` map OreSpawn's and 1.7.10's block and item names to 26.3 ids. Chests are
  filled the way 1.7.10 did it.

Everything else is the port's own code, written for 26.3: the creatures' behaviour, the items, blocks, dimensions,
rendering and animation. So are the block and item model files, recipes, loot tables, tags, worldgen and names.

## What's in it

Everything in OreSpawn 1.7.10-20.3 except the Girlfriend and Boyfriend. Those are left out on purpose, along with the
frog's kiss, Girlfriend Island, Love, and their cages, eggs and loot.

| Group | What it has |
|---|---|
| Egg ores, ores, gear | 29 Ancient Dried spawn egg ores for vanilla mobs (more for OreSpawn's own); amethyst, ruby, uranium, titanium and salt ores; Emerald, Ruby, Amethyst and Ultimate tools; Emerald, Ruby, Amethyst, Lapis and Ultimate armor, with OreSpawn's stats and built-in enchantments |
| Creatures (`orespawn.entity`) | Apple cows, butterfly, moth, firefly, bird, chipmunk, cricket, frog, beaver, cassowary, rat, peacock, stink bug, mosquito, dragonfly, goldfish, flounder, skate, whale, gazelle, ostrich (rideable), lizard, crab, scorpion, the three worms |
| Dinosaurs | Alosaurus, T. Rex, Nastysaurus, Pointysaurus, Cryolophosaurus, Baryonyx, Camarasaurus, Velocity Raptor |
| Bugs | Mantis, Hercules Beetle, Bee, Brutalfly, CaterKiller, Emperor Scorpion, Cave Fisher, Molenoid, Molenoid Dirt |
| Spooky | Ghost and Ghost Pumpkin Skelly (Halloween only), Ender Knight, Ender Reaper, Fairy, Dungeon Beast, Leaf Monster, Triffid |
| Sea | Attack Squid, Hammerhead, Irukandji, Sea Monster, Sea Viper, Cloud Shark, Hydrolisc and Water Dragon (tameable), Crystal Urchin |
| Terrors | Creeping Horror, Terrible Terror, Lurking Terror, Nightmare, Kyuubi, WTF?, Alien, Basilisk, Cephadrome (rideable), Cliff Racer |
| Oddities | Leonopteryx and Dragon (rideable), Baby Dragon, Stinky, Mothra, Ruby Bird, Rotator, Vortex, Criminal, T-Shirt, Coin, Easter Bunny, Rubber Ducky, Spit Bug, Jumpy Bug |
| Robots | Bomb-Omb, Robo-Pounder, -Gunner, -Warrior, -Sniper, Jeffery, the rideable Robot Spider and Red Ant Robot, Spider Driver, floating islands |
| Bosses | Mobzilla, The King, The Queen, the Prince, Princess and growing princes (rideable), The Kraken, with boss bars |
| Portals | Ants and termites (right-click with an empty hand to travel), nests, troll blocks, OreSpawn's teleporter |
| Weapons | OreSpawn's swords (Big Bertha and kin, Chainsaw, Rat and Fairy swords), 3D held models, Ray Gun, Thunder Staff, SquidZooka, bows and the Ultimate Fishing Rod, nine armor sets |
| Tools | Critter cages (113), Zoo cage, ZooKeeper, Instant Shelter and Garden, Miner's Dream, Experience Catcher, Wrench, Sifter, Hoverboard, shoes, rocks |
| Farming | Crops, seeds, foods, creature plants, fruit and experience trees, Magic Apple, pizza, duct tape, Extreme Torch and repellents, Duplicator and Sky Tree Wood |
| Gadgets, Leftovers | Insta-Bridge, stairs builders, Nether Tracker, Rose Sword, Worm Tooth; day/night flowers, Ender-Pearl and Eye-of-Ender blocks, Lava Foam |
| Structures, Dungeons | The overworld structures at OreSpawn's odds; every dungeon (End dungeons, Random Dungeon item, the dimensions' dungeons) |
| Utopia, Mining, Village Mania, Islands, Crystal, Chaos | The six dimensions, each with OreSpawn's 1.7.10 terrain, biome, features, structures and spawn lists |

**3D weapons:** Big Bertha, Slice, the Royal Guardian Sword, the Attitude Adjuster, the Battle Axe, the Queen Scale
Battle Axe, the Chainsaw and the SquidZooka are drawn in the hand with OreSpawn's own models and 1.7.10 placement, and the
Chainsaw's chain runs. Elsewhere (inventory, ground, frames) they show their flat icons, as in OreSpawn. The mod ships the
item tag `locomotion:keeps_hand_model` with these eight, so Wither's Animations keeps their 3D models in the hand.

Differences from OreSpawn 1.7.10 that come from 26.3:
- **Health:** 26.3 caps max health at 1024 on its own. With a mod that raises the cap, such as AttributeFix (its
  default cap is 1,000,000), every mob has OreSpawn's own health: Mobzilla 4000, The King 7000, The Queen 6000, the
  young adult Prince 3000, the young Prince and the Robot Spider 1500.
  - Without one, they get 1024. Mobzilla, The King, The Queen and the two princes then scale the hits and heals they
    take, so each still needs as many hits as in OreSpawn.
  - The cap is read when it matters, so the same jar works either way.
  - A mob saved under one cap and loaded under another keeps the same fraction of its health.
  - The same goes for armor: Ultimate armor's 34 points need a raised armor cap, since 26.3 caps total armor at 30.
- **Attack speed:** tools and weapons use 26.3's normal attack speeds, because 1.7.10 had no attack cooldown.
- **Riding:** mounts use 26.3's rider controls, with the jump key in place of OreSpawn's Left Alt fly-up key.
- **Big structures:** where 26.3 can't place a structure across chunks, the parts wait, saved with the dimension, and
  go in at the end of a tick once their chunk is fully loaded.
- **Structure spacing:** OreSpawn's `recently_placed` spacing is approximate, because 26.3 generates chunks in parallel.
- **Each group's other differences** are listed in its classes' javadoc.

Not ported: the Girlfriend, Boyfriend and their items; EntityAIMoveIndoors (26.3 has none); fire resistance settings
(not in 26.3); the default-off config options such as PlayNicely; the "cannon fodder" hats.

## Test results (2026-11-20)

Every run was a muted client game test, with the original file read at runtime.

- **Plain** (OreSpawn, Fabric API and the test mods): all 13 test classes passed. That's 139 screenshots: 114
  creatures, 19 items and blocks, and the 6 dimensions.
  - The Ender Reaper and the Baby Dragon move on their own even with their AI off, and got out of reach before the
    client check. The check then spawned a fresh one of each next to the player, and both reached the client.
- **AllMods-26.3** (every mod in the instance, with its performance mods, Sodium and Iris): every class passed.
  `CompatTest` passed too:
  - Epic Siege: a zombie walked to an OreSpawn ruby sword and chestplate and put them on.
  - Knockdown: a Bomb-Omb blowing up next to a player knocked them down.
- **The pack's locked features** (`tools/locked-features-with-orespawn.sh`): all checks passed, both with the pack's
  combat mods alone and with every AllMods mod plus OreSpawn. They cover the lunge, the vanilla left-click hit and its
  timing, dual wielding, door shields, and the Dragonslayer and Legendary Sword models.
- **Before the old copies were removed,** each part read from the original was checked against them:
  - Textures and sounds: all 1,137 files the pack serves have the same bytes as the old copies. The tinted and
    baby-armor textures have the same pixels instead.
  - Models: all 109 are the same part for part, within 0.00001.
  - Dungeons: 739 builds and placements, with set seeds, placed the same blocks, chest items, spawners, doors and
    creatures in the same order. That's 3.05 million steps. The Basilisk maze is the exception: OreSpawn carves it
    with `Math.random()`, so no two builds match.
- **The repository** has no file identical to any of the 1,956 files in the original, and no images, sounds or class
  files.

## Building and testing

Put the original `orespawn-1.7.10-20.3.zip` in `original/` (git ignores that folder), or pass
`-PorespawnOriginal=<path>`. Then:

```
./run-gradle.sh build                    # the jar, in build/libs (it doesn't need the original file)
./run-gradle.sh runClient                # a dev client, with the original file found as above
./run-gradle.sh runClientGameTest        # the client game tests, muted
```

`run-gradle.sh` runs Gradle with JDK 25. Dev runs and tests get the original file through the `orespawn.original`
system property. The game itself also reads the `ORESPAWN_ORIGINAL` environment variable.

The game tests are in `src/gametest`:

| Test | What it checks |
|---|---|
| `OreSpawnTest` | Blocks, items, gear and worldgen |
| `CreatureTest` | Every creature, with screenshots |
| `SeaTest`, `PortalsTest`, `TerrorsTest`, `ToolsTest`, `OdditiesTest`, `DungeonsTest`, `MiningTest`, `CrystalTest`, `ChaosTest` | One content group each |
| `HeldWeaponsTest` | The eight 3D weapons use their 3D model in every hand view, with screenshots; the Chainsaw's chain runs |
| `BossHealthTest` | The six mobs over 1024 health: OreSpawn's health with AttributeFix (`-PwithMods=<folder with AttributeFix and Prickle>`), 1024 with the scaled hits without it, and old saves keep their health fraction |
| `DimensionsTest` | All six dimensions |
| `CompatTest` | Epic Siege and Knockdown, only when those mods are loaded |
| `IdDumpTest` | Writes `build/orespawn-ids.txt` for `tools/id_audit.py` |

For the Destiny pack:

```
./run-gradle.sh --no-configuration-cache copyAllMods -Prenderer=iris   # the AllMods-26.3 instance's mods, read only
./run-gradle.sh --no-configuration-cache runClientGameTest -PallMods
sh tools/locked-features-with-orespawn.sh                              # the pack's locked features, with OreSpawn loaded
python -I tools/id_audit.py                                            # after a test run: every OreSpawn item and block registered
```

The locked-features script works in `WithersCombatSystemExtensionFabric`'s folder, so tell that mod's session before
running it.

## Files

- **`src/main/java/orespawn`**
  - Blocks, items, the Ultimate tools and the worldgen hooks.
  - `entity`: creatures and spawns.
  - `client`: renderers, render state and models.
  - `content`: one class per content group.
  - `world`: dimensions, structures and dungeons.
  - `original`: finding the original file and running its dungeon code.
  - `client/original`: its textures, sounds and model shapes.
- **`src/main/resources/orespawn/original`:** the lists that say what to read from the original file.
  - `assets.tsv` and `sound_events.txt`, written by `tools/original_assets.py`.
  - `legacy_blocks.tsv`, `legacy_items.tsv` and `orespawn_fields.tsv`, written by `tools/original_names.py`.

  They hold paths and names only.
- **`tools`:** the scripts the port was made with.
  - `PORTING.md`: how a content group was ported.
  - `id_audit.py`, `ids.tsv`, `ids_extra.tsv`: the id audit.
  - `gen_assets.py`, `crystal_assets.py`, `farming_assets.py`: write the models, loot, recipes, tags, worldgen and
    names. They also copy OreSpawn's textures into `src/main/resources`, which git ignores; the mod doesn't use those
    copies.
  - `convert_model.py`: drafted the model classes from a decompiled model.
  - `dungeons/port_dungeons.py`: translated the dungeon code. The mod runs the original now. The translated classes git
    ignores; `LB.java` and `LegacyStates.java` still come from this script.
