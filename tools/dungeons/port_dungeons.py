"""Ports OreSpawn 1.7.10's dungeon builders to 26.3 (the Dungeons content group).

    python -I tools/dungeons/port_dungeons.py <26.3 sources: the folder holding net/minecraft> [client jar]

Reads the decompiled GenericDungeon, BasiliskMaze, NightmareDungeon and RubyBirdDungeon and the dungeon placement
methods of OreSpawnWorld, and writes:

- src/main/java/orespawn/world/dungeons/<Name>.java: the same code, with 1.7.10's World, Block and tile entities
  replaced by DWorld / LB (see DWorld.java). The block placement code is kept line for line. OreSpawnWorld's
  placement methods (all but the overworld's, which are the Structures group's) go to OreSpawnWorldDungeons.java.
- src/main/java/orespawn/world/dungeons/OreSpawnDungeons.java: the public API, a method per dungeon and per placement.
- src/main/java/orespawn/world/dungeons/LB.java: the 1.7.10 blocks the code uses, and LegacyStates.java: every
  (block id, metadata) pair they can take, as a 26.3 block state (from 26.3's own flattening table, BlockStateData).
- src/main/resources/data/orespawn/loot_table/chests/dungeons/*.json: one chest loot table per WeightedRandomChestContent
  list (items other groups own by their ids; spawn eggs as <creature id>_spawn_egg).

OreSpawn has no license; the user allowed decompiling it and using its code for this private pack (2026-11-19).
"""
import json
import os
import re
import sys
import zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
PROJECT = os.path.normpath(os.path.join(HERE, "..", ".."))
SRC = "D:/Destinymods/_src_orespawn/decompiled/danger/orespawn"
OUT = os.path.join(PROJECT, "src", "main", "java", "orespawn", "world", "dungeons")
IDS = os.path.join(PROJECT, "tools", "ids.tsv")
MCSRC = sys.argv[1] if len(sys.argv) > 1 else None
CLIENT_JAR = sys.argv[2] if len(sys.argv) > 2 else os.path.expanduser("~/.gradle/caches/fabric-loom/26.3/minecraft-client.jar")

# ---------------------------------------------------------------------------------------------------------------------
# 1.7.10 names (MCP stable, Blocks / Items field order)
# ---------------------------------------------------------------------------------------------------------------------
# Blocks.<srg field>: (1.7.10 block id, MCP name)
BLOCKS = {
    "field_150350_a": (0, "air"), "field_150348_b": (1, "stone"), "field_150349_c": (2, "grass"), "field_150346_d": (3, "dirt"),
    "field_150347_e": (4, "cobblestone"), "field_150344_f": (5, "planks"), "field_150345_g": (6, "sapling"),
    "field_150357_h": (7, "bedrock"), "field_150358_i": (8, "flowing_water"), "field_150355_j": (9, "water"),
    "field_150356_k": (10, "flowing_lava"), "field_150353_l": (11, "lava"), "field_150354_m": (12, "sand"),
    "field_150351_n": (13, "gravel"), "field_150352_o": (14, "gold_ore"), "field_150366_p": (15, "iron_ore"),
    "field_150365_q": (16, "coal_ore"), "field_150364_r": (17, "log"), "field_150362_t": (18, "leaves"),
    "field_150360_v": (19, "sponge"), "field_150359_w": (20, "glass"), "field_150369_x": (21, "lapis_ore"),
    "field_150368_y": (22, "lapis_block"), "field_150367_z": (23, "dispenser"), "field_150322_A": (24, "sandstone"),
    "field_150323_B": (25, "noteblock"), "field_150324_C": (26, "bed"), "field_150318_D": (27, "golden_rail"),
    "field_150319_E": (28, "detector_rail"), "field_150320_F": (29, "sticky_piston"), "field_150321_G": (30, "web"),
    "field_150329_H": (31, "tallgrass"), "field_150330_I": (32, "deadbush"), "field_150331_J": (33, "piston"),
    "field_150332_K": (34, "piston_head"), "field_150325_L": (35, "wool"), "field_150326_M": (36, "piston_extension"),
    "field_150327_N": (37, "yellow_flower"), "field_150328_O": (38, "red_flower"), "field_150338_P": (39, "brown_mushroom"),
    "field_150337_Q": (40, "red_mushroom"), "field_150340_R": (41, "gold_block"), "field_150339_S": (42, "iron_block"),
    "field_150334_T": (43, "double_stone_slab"), "field_150333_U": (44, "stone_slab"), "field_150336_V": (45, "brick_block"),
    "field_150335_W": (46, "tnt"), "field_150342_X": (47, "bookshelf"), "field_150341_Y": (48, "mossy_cobblestone"),
    "field_150343_Z": (49, "obsidian"), "field_150478_aa": (50, "torch"), "field_150480_ab": (51, "fire"),
    "field_150474_ac": (52, "mob_spawner"), "field_150476_ad": (53, "oak_stairs"), "field_150486_ae": (54, "chest"),
    "field_150488_af": (55, "redstone_wire"), "field_150482_ag": (56, "diamond_ore"), "field_150484_ah": (57, "diamond_block"),
    "field_150462_ai": (58, "crafting_table"), "field_150464_aj": (59, "wheat"), "field_150458_ak": (60, "farmland"),
    "field_150460_al": (61, "furnace"), "field_150470_am": (62, "lit_furnace"), "field_150472_an": (63, "standing_sign"),
    "field_150466_ao": (64, "wooden_door"), "field_150468_ap": (65, "ladder"), "field_150448_aq": (66, "rail"),
    "field_150446_ar": (67, "stone_stairs"), "field_150444_as": (68, "wall_sign"), "field_150442_at": (69, "lever"),
    "field_150456_au": (70, "stone_pressure_plate"), "field_150454_av": (71, "iron_door"),
    "field_150452_aw": (72, "wooden_pressure_plate"), "field_150450_ax": (73, "redstone_ore"),
    "field_150439_ay": (74, "lit_redstone_ore"), "field_150437_az": (75, "unlit_redstone_torch"),
    "field_150429_aA": (76, "redstone_torch"), "field_150430_aB": (77, "stone_button"), "field_150431_aC": (78, "snow_layer"),
    "field_150432_aD": (79, "ice"), "field_150433_aE": (80, "snow"), "field_150434_aF": (81, "cactus"), "field_150435_aG": (82, "clay"),
    "field_150436_aH": (83, "reeds"), "field_150421_aI": (84, "jukebox"), "field_150422_aJ": (85, "fence"),
    "field_150423_aK": (86, "pumpkin"), "field_150424_aL": (87, "netherrack"), "field_150425_aM": (88, "soul_sand"),
    "field_150426_aN": (89, "glowstone"), "field_150427_aO": (90, "portal"), "field_150428_aP": (91, "lit_pumpkin"),
    "field_150414_aQ": (92, "cake"), "field_150413_aR": (93, "unpowered_repeater"), "field_150416_aS": (94, "powered_repeater"),
    "field_150399_cn": (95, "stained_glass"), "field_150415_aT": (96, "trapdoor"), "field_150418_aU": (97, "monster_egg"),
    "field_150417_aV": (98, "stonebrick"), "field_150420_aW": (99, "brown_mushroom_block"),
    "field_150419_aX": (100, "red_mushroom_block"), "field_150411_aY": (101, "iron_bars"), "field_150410_aZ": (102, "glass_pane"),
    "field_150440_ba": (103, "melon_block"), "field_150393_bb": (104, "pumpkin_stem"), "field_150394_bc": (105, "melon_stem"),
    "field_150395_bd": (106, "vine"), "field_150396_be": (107, "fence_gate"), "field_150389_bf": (108, "brick_stairs"),
    "field_150390_bg": (109, "stone_brick_stairs"), "field_150391_bh": (110, "mycelium"), "field_150392_bi": (111, "waterlily"),
    "field_150385_bj": (112, "nether_brick"), "field_150386_bk": (113, "nether_brick_fence"),
    "field_150387_bl": (114, "nether_brick_stairs"), "field_150388_bm": (115, "nether_wart"),
    "field_150381_bn": (116, "enchanting_table"), "field_150382_bo": (117, "brewing_stand"), "field_150383_bp": (118, "cauldron"),
    "field_150384_bq": (119, "end_portal"), "field_150378_br": (120, "end_portal_frame"), "field_150377_bs": (121, "end_stone"),
    "field_150380_bt": (122, "dragon_egg"), "field_150379_bu": (123, "redstone_lamp"), "field_150374_bv": (124, "lit_redstone_lamp"),
    "field_150373_bw": (125, "double_wooden_slab"), "field_150376_bx": (126, "wooden_slab"), "field_150375_by": (127, "cocoa"),
    "field_150372_bz": (128, "sandstone_stairs"), "field_150412_bA": (129, "emerald_ore"), "field_150477_bB": (130, "ender_chest"),
    "field_150479_bC": (131, "tripwire_hook"), "field_150473_bD": (132, "tripwire"), "field_150475_bE": (133, "emerald_block"),
    "field_150485_bF": (134, "spruce_stairs"), "field_150487_bG": (135, "birch_stairs"), "field_150481_bH": (136, "jungle_stairs"),
    "field_150483_bI": (137, "command_block"), "field_150461_bJ": (138, "beacon"), "field_150463_bK": (139, "cobblestone_wall"),
    "field_150457_bL": (140, "flower_pot"), "field_150459_bM": (141, "carrots"), "field_150469_bN": (142, "potatoes"),
    "field_150471_bO": (143, "wooden_button"), "field_150465_bP": (144, "skull"), "field_150467_bQ": (145, "anvil"),
    "field_150447_bR": (146, "trapped_chest"), "field_150445_bS": (147, "light_weighted_pressure_plate"),
    "field_150443_bT": (148, "heavy_weighted_pressure_plate"), "field_150441_bU": (149, "unpowered_comparator"),
    "field_150455_bV": (150, "powered_comparator"), "field_150453_bW": (151, "daylight_detector"),
    "field_150451_bX": (152, "redstone_block"), "field_150449_bY": (153, "quartz_ore"), "field_150438_bZ": (154, "hopper"),
    "field_150371_ca": (155, "quartz_block"), "field_150370_cb": (156, "quartz_stairs"), "field_150408_cc": (157, "activator_rail"),
    "field_150409_cd": (158, "dropper"), "field_150406_ce": (159, "stained_hardened_clay"), "field_150397_co": (160, "stained_glass_pane"),
    "field_150361_u": (161, "leaves2"), "field_150363_s": (162, "log2"), "field_150400_ck": (163, "acacia_stairs"),
    "field_150401_cl": (164, "dark_oak_stairs"), "field_150407_cf": (170, "hay_block"), "field_150404_cg": (171, "carpet"),
    "field_150405_ch": (172, "hardened_clay"), "field_150402_ci": (173, "coal_block"), "field_150403_cj": (174, "packed_ice"),
    "field_150398_cm": (175, "double_plant"),
}
# Items.<srg field>: 26.3 item id
ITEMS = {
    "field_151037_a": "iron_shovel", "field_151035_b": "iron_pickaxe", "field_151036_c": "iron_axe", "field_151033_d": "flint_and_steel",
    "field_151034_e": "apple", "field_151031_f": "bow", "field_151032_g": "arrow", "field_151044_h": "coal", "field_151045_i": "diamond",
    "field_151042_j": "iron_ingot", "field_151043_k": "gold_ingot", "field_151040_l": "iron_sword", "field_151048_u": "diamond_sword",
    "field_151046_w": "diamond_pickaxe", "field_151010_B": "golden_sword", "field_151007_F": "string", "field_151008_G": "feather",
    "field_151161_ac": "diamond_helmet", "field_151163_ad": "diamond_chestplate", "field_151173_ae": "diamond_leggings",
    "field_151175_af": "diamond_boots", "field_151169_ag": "golden_helmet", "field_151171_ah": "golden_chestplate",
    "field_151149_ai": "golden_leggings", "field_151151_aj": "golden_boots", "field_151147_al": "porkchop",
    "field_151157_am": "cooked_porkchop", "field_151153_ao": "golden_apple", "field_151135_aq": "oak_door", "field_151133_ar": "bucket",
    "field_151143_au": "minecart", "field_151139_aw": "iron_door", "field_151137_ax": "redstone", "field_151121_aF": "paper",
    "field_151111_aL": "compass", "field_151114_aO": "glowstone_dust", "field_151115_aP": "cod", "field_151101_aQ": "cooked_cod",
    "field_151100_aR": "dye", "field_151103_aS": "bone", "field_151102_aT": "sugar", "field_151104_aV": "red_bed",
    "field_151107_aW": "repeater", "field_151082_bd": "beef", "field_151076_bf": "chicken", "field_151077_bg": "cooked_chicken",
    "field_151078_bh": "rotten_flesh", "field_151079_bi": "ender_pearl", "field_151072_bj": "blaze_rod", "field_151074_bl": "gold_nugget",
    "field_151075_bm": "nether_wart", "field_151065_br": "blaze_powder", "field_151061_bv": "ender_eye", "field_151063_bx": "spawn_egg",
    "field_151062_by": "experience_bottle", "field_151059_bz": "fire_charge", "field_151166_bC": "emerald", "field_151162_bE": "flower_pot",
    "field_151148_bJ": "map", "field_151132_bS": "comparator", "field_151128_bU": "quartz", "field_151140_bW": "hopper_minecart",
}
DYES = ["ink_sac", "red_dye", "green_dye", "cocoa_beans", "lapis_lazuli", "purple_dye", "cyan_dye", "light_gray_dye", "gray_dye",
        "pink_dye", "lime_dye", "yellow_dye", "light_blue_dye", "magenta_dye", "orange_dye", "bone_meal"]
SPAWN_EGG_IDS = {61: "blaze_spawn_egg"}
# Blocks whose 26.3 item has another id.
BLOCK_ITEMS = {"minecraft:wall_torch": "minecraft:torch", "minecraft:redstone_wall_torch": "minecraft:redstone_torch"}
# 1.13 flattened names that later versions renamed.
RENAMES = {"minecraft:grass": "minecraft:short_grass", "minecraft:stone_slab": "minecraft:smooth_stone_slab",
           "minecraft:sign": "minecraft:oak_sign", "minecraft:wall_sign": "minecraft:oak_wall_sign",
           "minecraft:grass_path": "minecraft:dirt_path", "minecraft:mob_spawner": "minecraft:spawner",
           "minecraft:oak_bark": "minecraft:oak_wood", "minecraft:spruce_bark": "minecraft:spruce_wood",
           "minecraft:birch_bark": "minecraft:birch_wood", "minecraft:jungle_bark": "minecraft:jungle_wood",
           "minecraft:acacia_bark": "minecraft:acacia_wood", "minecraft:dark_oak_bark": "minecraft:dark_oak_wood"}
# 1.7.10 piston_extension (36, a moving piston) has no tile entity when placed by setBlock, so it is invisible and has no
# collision: OreSpawn's White House pews place it between their quartz stairs. It becomes air, which looks and acts the
# same (26.3's moving_piston without a block entity would too, but lingers as an odd block).
AS_AIR = {36}

# OreSpawn's vanilla-mob eggs.
VANILLA_EGGS = {"WitherSkeletonEgg": "minecraft:wither_skeleton_spawn_egg", "EnderDragonEgg": "minecraft:ender_dragon_spawn_egg",
                "SnowGolemEgg": "minecraft:snow_golem_spawn_egg", "IronGolemEgg": "minecraft:iron_golem_spawn_egg",
                "WitherBossEgg": "minecraft:wither_spawn_egg"}
# OreSpawnMain fields the dungeon code uses that ids.tsv doesn't list, or that batch 1 already registered under another id.
ID_OVERRIDES = {
    "MyUltimateSword": "ultimate_sword", "MyUltimatePickaxe": "ultimate_pickaxe", "MyUltimateShovel": "ultimate_shovel",
    "MyUltimateAxe": "ultimate_axe", "MyUltimateHoe": "ultimate_hoe",
    "UltimateHelmet": "ultimate_helmet", "UltimateBody": "ultimate_chestplate", "UltimateLegs": "ultimate_leggings",
    "UltimateBoots": "ultimate_boots",
    "EmeraldHelmet": "emerald_helmet", "EmeraldBody": "emerald_chestplate", "EmeraldLegs": "emerald_leggings", "EmeraldBoots": "emerald_boots",
    "RubyHelmet": "ruby_helmet", "RubyBody": "ruby_chestplate", "RubyLegs": "ruby_leggings", "RubyBoots": "ruby_boots",
    "AmethystHelmet": "amethyst_helmet", "AmethystBody": "amethyst_chestplate", "AmethystLegs": "amethyst_leggings",
    "AmethystBoots": "amethyst_boots",
    "ExperienceHelmet": "experience_helmet", "ExperienceBody": "experience_chestplate", "ExperienceLegs": "experience_leggings",
    "ExperienceBoots": "experience_boots",
    "LavaEelHelmet": "lava_eel_helmet", "LavaEelLegs": "lava_eel_leggings",
    "MothScaleHelmet": "moth_scale_helmet", "MothScaleBody": "moth_scale_chestplate", "MothScaleLegs": "moth_scale_leggings",
    "MothScaleBoots": "moth_scale_boots",
    "TigersEyeHelmet": "tigers_eye_helmet", "TigersEyeBody": "tigers_eye_chestplate", "TigersEyeLegs": "tigers_eye_leggings",
    "TigersEyeBoots": "tigers_eye_boots",
    # Crop and flower blocks OreSpawn never named (ids.tsv has only their seed items).
    "MyStrawberryPlant": "strawberry_plant", "MyButterflyPlant": "butterfly_plant", "MyMothPlant": "moth_plant",
    "MyRadishPlant": "radish_plant", "MyRicePlant": "rice_plant",
    "MyFlowerPinkBlock": "pink_flower", "MyFlowerBlueBlock": "blue_flower", "MyFlowerBlackBlock": "black_flower",
    "MyFlowerScaryBlock": "dead_flower",
    # Blocks other groups own, looked up by id at runtime (air until ported).
    "CrystalGrass": "crystal_grass", "MyAppleLeaves": "apple_leaves", "MyScaryLeaves": "scary_leaves",
}
# OreSpawn's own creatures' eggs: egg field -> OreSpawn entity name (registerModEntity), whose snake case is the id
# ("Caterkiller": the Bugs group registered "CaterKiller" as caterkiller).
EGG_ENTITY = {
    "RedCowEgg": "Apple Cow", "GoldCowEgg": "Golden Apple Cow", "EnchantedCowEgg": "Enchanted Golden Apple Cow", "MOTHRAEgg": "Mothra",
    "AloEgg": "Alosaurus", "CryoEgg": "Cryolophosaurus", "CamaEgg": "Camarasaurus", "VeloEgg": "Velocity Raptor", "HydroEgg": "Hydrolisc",
    "BasilEgg": "Basilisk", "DragonflyEgg": "Dragonfly", "EmperorScorpionEgg": "Emperor Scorpion", "ScorpionEgg": "Scorpion",
    "CaveFisherEgg": "CaveFisher", "SpyroEgg": "Baby Dragon", "BaryonyxEgg": "Baryonyx", "CockateilEgg": "Bird",
    "GammaMetroidEgg": "WTF?", "KyuubiEgg": "Kyuubi", "AlienEgg": "Alien", "AttackSquidEgg": "Attack Squid",
    "WaterDragonEgg": "Water Dragon", "CephadromeEgg": "Cephadrome", "KrakenEgg": "The Kraken", "LizardEgg": "Lizard",
    "DragonEgg": "Dragon", "BeeEgg": "Bee", "TrooperBugEgg": "Jumpy Bug", "SpitBugEgg": "Spit Bug", "StinkBugEgg": "Stink Bug",
    "OstrichEgg": "Ostrich", "GazelleEgg": "Gazelle", "ChipmunkEgg": "Chipmunk", "CreepingHorrorEgg": "Creeping Horror",
    "TerribleTerrorEgg": "Terrible Terror", "CliffRacerEgg": "Cliff Racer", "TriffidEgg": "Triffid", "PitchBlackEgg": "Nightmare",
    "LurkingTerrorEgg": "Lurking Terror", "SmallWormEgg": "Small Worm", "MediumWormEgg": "Medium Worm", "LargeWormEgg": "Large Worm",
    "TRexEgg": "T. Rex", "GodzillaEgg": "Mobzilla", "MantisEgg": "Mantis", "HerculesEgg": "Hercules Beetle", "VortexEgg": "Vortex",
    "RatEgg": "Rat", "DungeonBeastEgg": "Dungeon Beast", "FairyEgg": "Fairy", "WhaleEgg": "Whale", "SkateEgg": "Skate",
    "IrukandjiEgg": "Irukandji", "Robot1Egg": "Bomb-Omb", "Robot2Egg": "Robo-Pounder", "Robot3Egg": "Robo-Gunner",
    "Robot4Egg": "Robo-Warrior", "Robot5Egg": "Robo-Sniper", "CriminalEgg": "Criminal", "CoinEgg": "Coin", "BoyfriendEgg": "Boyfriend",
    "EasterBunnyEgg": "EasterBunny", "MolenoidEgg": "Molenoid", "SeaMonsterEgg": "Sea Monster", "SeaViperEgg": "Sea Viper",
    "CaterKillerEgg": "Caterkiller", "LeonEgg": "Leonopteryx", "HammerheadEgg": "Hammerhead", "RubberDuckyEgg": "Rubber Ducky",
    "NastysaurusEgg": "Nastysaurus", "PointysaurusEgg": "Pointysaurus", "BrutalflyEgg": "Brutalfly", "CricketEgg": "Cricket",
    "FrogEgg": "Frog", "JefferyEgg": "Jeffery", "SpiderDriverEgg": "Spider Driver", "CrabEgg": "Crab", "CassowaryEgg": "Cassowary",
    "CloudSharkEgg": "Cloud Shark", "StinkyEgg": "Stinky", "UrchinEgg": "Crystal Urchin", "RotatorEgg": "Rotator",
    "EnderReaperEgg": "Ender Reaper", "ThePrinceEgg": "The Prince", "ThePrincessEgg": "The Princess", "TheKingEgg": "The King",
    "TheQueenEgg": "The Queen",
}
# Egg ores the dungeons build with: block field -> the creature they hold (vanilla ones keep their vanilla id).
EGG_ORES = {"MyWaterDragonSpawnBlock": "water_dragon", "MyEnderKnightSpawnBlock": "ender_knight",
            "MyEnderReaperSpawnBlock": "ender_reaper", "MyEndermanSpawnBlock": "enderman", "MyEnderDragonSpawnBlock": "ender_dragon"}


def snake(name):
    s = re.sub(r"([a-z])([A-Z])", r"\1_\2", name)
    s = re.sub(r"[^A-Za-z0-9]+", "_", s).strip("_").lower()
    return s


def fail(msg):
    raise SystemExit("port_dungeons: " + msg)


# ---------------------------------------------------------------------------------------------------------------------
# 26.3 ids (for checking) and the flattening table
# ---------------------------------------------------------------------------------------------------------------------
def modern_ids():
    blocks, items = set(), set()
    with zipfile.ZipFile(CLIENT_JAR) as z:
        for n in z.namelist():
            m = re.fullmatch(r"assets/minecraft/(blockstates|items)/([a-z0-9_]+)\.json", n)
            if m:
                (blocks if m.group(1) == "blockstates" else items).add("minecraft:" + m.group(2))
    return blocks, items


def parse_block_state_data(path):
    text = open(path, encoding="utf-8").read()
    consts = {}
    for m in re.finditer(r"Map<String, String> (\w+) = Map\.of\((.*?)\);", text, re.S):
        vals = re.findall(r'"([^"]*)"', m.group(2))
        consts[m.group(1)] = dict(zip(vals[0::2], vals[1::2]))
    table = {}
    for m in re.finditer(r"\bregister\(\s*(\d+)\s*,\s*create\(\s*\"([^\"]+)\"\s*(?:,\s*(Map\.of\([^)]*\)|\w+))?\s*\)", text):
        props = {}
        if m.group(3):
            if m.group(3).startswith("Map.of"):
                vals = re.findall(r'"([^"]*)"', m.group(3))
                props = dict(zip(vals[0::2], vals[1::2]))
            else:
                props = consts[m.group(3)]
        table[int(m.group(1))] = (m.group(2), props)
    if len(table) < 1500:
        fail(f"only {len(table)} states parsed from {path}")
    return table


def legacy_state(table, block_id, meta):
    if block_id in AS_AIR:
        return "minecraft:air"
    key = block_id * 16 + meta
    if key not in table:
        key = min((k for k in table if k >> 4 == block_id), default=None)
        if key is None:
            return None
    name, props = table[key]
    name = RENAMES.get(name, name)
    if not props:
        return name
    return name + "[" + ",".join(f"{k}={v}" for k, v in sorted(props.items())) + "]"


# ---------------------------------------------------------------------------------------------------------------------
# Java helpers
# ---------------------------------------------------------------------------------------------------------------------
def split_args(s):
    """Splits a Java argument list at top-level commas."""
    out, depth, cur = [], 0, ""
    for ch in s:
        if ch in "([{":
            depth += 1
        elif ch in ")]}":
            depth -= 1
        if ch == "," and depth == 0:
            out.append(cur.strip())
            cur = ""
        else:
            cur += ch
    if cur.strip():
        out.append(cur.strip())
    return out


def call_end(text, open_paren):
    """Index just past the parenthesis that closes the one at open_paren."""
    depth = 0
    for i in range(open_paren, len(text)):
        if text[i] == "(":
            depth += 1
        elif text[i] == ")":
            depth -= 1
            if depth == 0:
                return i + 1
    fail("unbalanced parentheses")


def replace_calls(text, start_pattern, fn):
    """Replaces every call that starts with start_pattern (ending in '(') with fn(list of args)."""
    out, pos = "", 0
    for m in re.finditer(re.escape(start_pattern), text):
        if m.start() < pos:
            continue
        end = call_end(text, m.end() - 1)
        out += text[pos:m.start()] + fn(split_args(text[m.end():end - 1]))
        pos = end
    return out + text[pos:]


class Porter:
    def __init__(self):
        self.ids = {}
        for line in open(IDS, encoding="utf-8"):
            if line.startswith("#") or not line.strip():
                continue
            f = line.rstrip("\n").split("\t")
            self.ids.setdefault(f[2], (f[0], f[1], f[3]))
        self.used_blocks = {}    # LB constant name -> ("vanilla", id) / ("mod", orespawn id)
        self.loot = {}           # loot table path -> json
        self.missing = set()
        self.item_ids = set()

    # ---- items and blocks ----
    def orespawn_id(self, field):
        if field in ID_OVERRIDES:
            return ID_OVERRIDES[field]
        if field in VANILLA_EGGS:
            return None
        if field in EGG_ENTITY:
            return snake(EGG_ENTITY[field]) + "_spawn_egg"
        if field in EGG_ORES:
            return EGG_ORES[field] + "_egg_ore"
        if field not in self.ids:
            fail(f"OreSpawnMain.{field} isn't in ids.tsv (add it to ID_OVERRIDES)")
        return self.ids[field][0]

    def item_id(self, expr, meta=0):
        item = self._item_id(expr, meta)
        self.item_ids.add(item)
        return item

    def _item_id(self, expr, meta=0):
        expr = expr.strip()
        m = re.fullmatch(r"Item\.func_150898_a\((.*)\)", expr)
        if m:
            expr = m.group(1).strip()
        m = re.fullmatch(r"Items\.(field_\w+)", expr)
        if m:
            name = ITEMS.get(m.group(1)) or fail(f"unknown item {expr}")
            if name == "dye":
                name = DYES[meta]
            elif name == "spawn_egg":
                name = SPAWN_EGG_IDS.get(meta) or fail(f"spawn egg meta {meta}")
            elif meta:
                fail(f"meta {meta} on {expr}")
            return "minecraft:" + name
        m = re.fullmatch(r"Blocks\.(field_\w+)", expr)
        if m:
            bid = BLOCKS[m.group(1)][0]
            name = legacy_state(self.table, bid, meta).split("[")[0]
            return BLOCK_ITEMS.get(name, name)
        m = re.fullmatch(r"OreSpawnMain\.(\w+)", expr)
        if m:
            if m.group(1) in VANILLA_EGGS:
                return VANILLA_EGGS[m.group(1)]
            return "orespawn:" + self.orespawn_id(m.group(1))
        fail(f"can't resolve item {expr}")

    def lb(self, expr):
        """A block expression -> the LB constant that stands for it."""
        m = re.fullmatch(r"Blocks\.(field_\w+)", expr)
        if m:
            if m.group(1) not in BLOCKS:
                fail(f"unknown block {expr}")
            bid, name = BLOCKS[m.group(1)]
            self.used_blocks[name.upper()] = ("vanilla", bid)
            return "LB." + name.upper()
        m = re.fullmatch(r"OreSpawnMain\.(\w+)", expr)
        if m:
            oid = self.orespawn_id(m.group(1))
            const = oid.upper()
            self.used_blocks[const] = ("mod", oid)
            return "LB." + const
        fail(f"can't resolve block {expr}")

    # ---- loot ----
    def loot_list(self, cls, field, body):
        path = "chests/dungeons/" + (snake(cls) if field == "chestContentsList" and cls != "GenericDungeon"
                                     else snake(re.sub(r"(ContentsList|ChestContentsList)$", "", field)) or snake(cls))
        if field == "chestContentsList" and cls == "GenericDungeon":
            path = "chests/dungeons/dungeon"
        entries = []
        for args in re.findall(r"new WeightedRandomChestContent\(((?:[^()]|\([^()]*\))*)\)", body):
            a = split_args(args)
            item, meta, lo, hi, weight = a[0], int(a[1]), int(a[2]), int(a[3]), int(a[4])
            e = {"type": "minecraft:item", "name": self.item_id(item, meta), "weight": weight}
            # The user asked for the Girlfriend and Boyfriend to be left out of the port, so their eggs and cages are too.
            if "girlfriend" in e["name"] or "boyfriend" in e["name"]:
                continue
            if (lo, hi) != (1, 1):
                e["modifier"] = [{"type": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}}]
            entries.append(e)
        self.loot[path] = {"type": "minecraft:chest", "pools": [{"rolls": 1, "entries": entries}],
                           "random_sequence": "orespawn:" + path}
        return "orespawn:" + path

    # ---- translation ----
    def translate(self, cls, text):
        text = text.replace("\r\n", "\n")
        # A decompiler artifact (an unused local in makeBouncyCastle).
        text = text.replace("      int var24 = true;\n", "")
        # Loot lists become loot table ids.
        def lists(m):
            return f"{m.group(1)}final String {m.group(2)} = \"{self.loot_list(cls, m.group(2), m.group(3))}\";"
        text = re.sub(r"((?:private |public |protected )?(?:static )?)final WeightedRandomChestContent\[\] (\w+) = "
                      r"new WeightedRandomChestContent\[\]\{(.*?)\n   \};", lists, text, flags=re.S)
        text = text.replace("WeightedRandomChestContent[]", "String")
        # Items placed straight into chests.
        def stack(a):
            count = a[1] if len(a) > 1 else "1"
            meta = int(a[2]) if len(a) > 2 else 0
            return f"new DWorld.Stack(\"{self.item_id(a[0], meta)}\", {count})"
        text = replace_calls(text, "new ItemStack(", stack)
        text = replace_calls(text, "WeightedRandomChestContent.func_76293_a(",
                             lambda a: f"world.fillChest({a[1]}, {a[2]}, {a[3]})")
        # Tile entities.
        text = re.sub(r"\(TileEntityMobSpawner\)(\w+)\.func_147438_o\(", r"\1.spawnerAt(", text)
        text = re.sub(r"\(TileEntityChest\)(\w+)\.func_147438_o\(", r"\1.chestAt(", text)
        text = re.sub(r"(private TileEntityChest getChestTileEntity\(World world, int cposx, int cposy, int cposz\) \{).*?\n   \}",
                      r"\1\n      return world.chestAt(cposx, cposy, cposz);\n   }", text, flags=re.S)
        text = re.sub(r"(private TileEntityMobSpawner getSpawnerTileEntity\(World world, int cposx, int cposy, int cposz\) \{).*?\n   \}",
                      r"\1\n      return world.spawnerAt(cposx, cposy, cposz);\n   }", text, flags=re.S)
        text = text.replace("TileEntityMobSpawner", "DWorld.Spawner").replace("TileEntityChest", "DWorld.Chest")
        text = text.replace(".func_145881_a().func_98272_a(", ".setEntityName(")
        text = text.replace(".func_70299_a(", ".setInventorySlotContents(")
        # Entities.
        text = re.sub(r"EntityList\.func_75620_a\(([^,]+), (\w+)\)", r"\2.createEntityByName(\1)", text)
        text = re.sub(r"new EntityEnderCrystal\((\w+)\)", r'\1.createEntityByName("EnderCrystal")', text)
        text = re.sub(r"\(\(EntityLiving\)\w+\)\.func_70642_aH\(\);\n\s*", "", text)
        text = re.sub(r"\bEntityEnderCrystal\b", "DWorld.Ent", text)
        text = re.sub(r"\bEntity\b", "DWorld.Ent", text)
        text = text.replace(".func_70012_b(", ".setLocationAndAngles(").replace(".func_110163_bv()", ".setPersistent()")
        text = re.sub(r"(\w+)\.func_72838_d\(", r"\1.spawnEntityInWorld(", text)
        text = re.sub(r"ItemDoor\.func_150924_a\((\w+), ", r"\1.placeDoorBlock(", text)
        # The world.
        text = re.sub(r"OreSpawnMain\.setBlockFast\(\s*(\w+),\s*", r"\1.setBlockFast(", text)
        for srg, name in (("func_147465_d", "setBlock"), ("func_147449_b", "setBlock"), ("func_72921_c", "setBlockMetadataWithNotify"),
                          ("func_147439_a", "getBlock"), ("func_147437_c", "isAirBlock"), ("field_73012_v", "rand"),
                          ("field_72995_K", "isRemote")):
            text = re.sub(r"(\w+)\." + srg + r"\b", r"\1." + name, text)
        text = re.sub(r"BiomeGenBase (\w+) = (\w+)\.func_72807_a\(", r"String \1 = \2.getBiomeName(", text)
        text = re.sub(r"(\w+)\.field_76791_y\.equals\(", r"\1.equals(", text)
        # Blocks.
        text = re.sub(r"\bBlocks\.field_\w+", lambda m: self.lb(m.group(0)), text)
        text = re.sub(r"\bOreSpawnMain\.(MyDungeon|RubyDungeon|BMaze)\.", lambda m: {"MyDungeon": "DUNGEON.", "RubyDungeon": "RUBY.",
                                                                                     "BMaze": "MAZE."}[m.group(1)], text)
        text = re.sub(r"\bOreSpawnMain\.(LessLag|RotatorEnable|UrchinEnable|SpiderDriverEnable|DisableOverworldDungeons|MosquitoEnable)\b",
                      r"Config.\1", text)
        text = re.sub(r"\bOreSpawnMain\.\w+", lambda m: self.lb(m.group(0)), text)
        text = re.sub(r"\bWorld\b", "DWorld", text)
        text = re.sub(r"\bBlock\b", "LB", text)
        # Header.
        text = re.sub(r"^package danger\.orespawn;\n\n(import [^\n]+\n)+\n", "", text)
        leftovers = sorted(set(re.findall(r"\b(?:func|field)_\d+_\w+|\bOreSpawnMain\b|\bBlocks\.|\bItems\.|\bItemStack\b", text)))
        if leftovers:
            self.missing.update(f"{cls}: {x}" for x in leftovers)
        return text


HEADER = """package orespawn.world.dungeons;

{imports}/**
 * {doc}
 * <p>
 * Ported from OreSpawn 1.7.10-20.3's {source} by tools/dungeons/port_dungeons.py, which keeps the block placement code as
 * it was: {{@link DWorld}} stands in for 1.7.10's World and {{@link LB}} for its blocks (with their metadata). Don't edit
 * by hand; change the script and run it again.
 */
"""

DOCS = {
    "GenericDungeon": "OreSpawn's dungeons and structures (GenericDungeon): the mob dungeon, castles, hives, the haunted "
                      "houses, altars, islands, the Inca pyramid, the robot lab, the White House and the rest.",
    "BasiliskMaze": "The Basilisk maze: a sandstone entrance tower over a shaft down to an obsidian maze and a lava hall "
                    "with three Basilisks and the treasure chests (OreSpawn's Mining dimension).",
    "NightmareDungeon": "OreSpawn's Nightmare dungeon: a bedrock and obsidian room with an Emperor Scorpion or Nightmare "
                        "spawner and two chests. OreSpawn 1.7.10-20.3 never builds it (nothing calls it); it's ported for "
                        "completeness.",
    "RubyBirdDungeon": "The Ruby Bird dungeon: a mossy room with a Ruby Bird spawner and a chest (Utopia and the Islands).",
    "OreSpawnWorldDungeons": "Where OreSpawn builds its dungeons: the dungeon placement methods of OreSpawnWorld (the "
                             "odds, biome checks and the search for ground), unchanged.",
}


def emit_class(porter, cls, text, extra_imports=""):
    body = porter.translate(cls, text)
    body = re.sub(r"public class (\w+)", r"public final class \1", body, count=1) if cls != "GenericDungeon" else body
    source = "OreSpawnWorld" if cls == "OreSpawnWorldDungeons" else cls
    out = HEADER.format(imports=extra_imports, doc=DOCS[cls], source=source) + body.lstrip()
    out = out.replace("\n   ", "\n\t")
    # Tabs for the remaining indentation levels (the decompiler indents by 3).
    out = re.sub(r"^((?:\t)+)((?:   )+)", lambda m: m.group(1) + "\t" * (len(m.group(2)) // 3), out, flags=re.M)
    with open(os.path.join(OUT, cls + ".java"), "w", encoding="utf-8", newline="\n") as f:
        f.write(out)


def world_methods(text, names):
    """Pulls the named methods (and their javadoc-less bodies) out of OreSpawnWorld."""
    out = []
    for name in names:
        m = re.search(r"\n   (?:public|private) (?:boolean|void) " + name + r"\(.*?\n   \}\n", text, re.S)
        if not m:
            fail(f"OreSpawnWorld.{name} not found")
        out.append(m.group(0).strip("\n"))
    return out


PLACEMENT = [
    # The End (generateEnd). The overworld's (generateSurface) belong to the Structures group (orespawn.world.structures).
    "addEndKnights", "addEndReapers", "addHospital", "addEnderCastle",
    # Village Mania.
    "addDamselInDistress", "addSpiderHangout", "addRedAntHangout",
    # Crystal.
    "addRotatorStation", "addRoundRotator", "addUrchinSpawner", "addCrystalHauntedHouse", "addCrystalBattleTower",
    # Utopia, Mining, Islands.
    "addRubyDungeon", "addGenericDungeon", "addBeeHive", "addAlienWTF", "addEnderKnight", "addLeonNest", "addShadowDungeon",
    "addD4RubyDungeon", "addD4CephadromeAltar", "addD4Castle", "addD4Greenhouse", "addD4NightmareRookery", "addD4StinkyHouse",
    "addD4WhiteHouse", "addD4EnderCastle", "addD4IncaPyramid", "addD4RobotLab", "addD4Mini", "addPumpkin", "addD4CloudShark",
    "addD4Rainbow", "addD4GenericDungeon", "addKingAltar", "addBasiliskMaze", "addKyuubiDungeon",
    "quickSpaceCheck", "quickBigSpaceCheck", "quickReallyBigSpaceCheck", "D4BigSpaceCheck",
]


def main():
    if not MCSRC:
        fail("usage: port_dungeons.py <folder holding net/minecraft of the 26.3 sources> [client jar]")
    porter = Porter()
    porter.table = parse_block_state_data(os.path.join(MCSRC, "net", "minecraft", "util", "datafix", "fixes", "BlockStateData.java"))
    modern_blocks, modern_items = modern_ids()
    os.makedirs(OUT, exist_ok=True)

    for cls in ("GenericDungeon", "BasiliskMaze", "NightmareDungeon", "RubyBirdDungeon"):
        text = open(os.path.join(SRC, cls + ".java"), encoding="utf-8").read()
        if cls == "BasiliskMaze":
            # java.awt.Point -> a small value class with the same equals/hashCode (Vector.indexOf needs them).
            text = text.replace("import java.awt.Point;\n", "")
            text = text.replace("   private Object rndElement(Vector v) {",
                                "   private static final class Point {\n      final int x;\n      final int y;\n\n"
                                "      Point(int x, int y) {\n         this.x = x;\n         this.y = y;\n      }\n\n"
                                "      @Override\n      public boolean equals(Object o) {\n"
                                "         return o instanceof Point p && p.x == this.x && p.y == this.y;\n      }\n\n"
                                "      @Override\n      public int hashCode() {\n         return this.x * 31 + this.y;\n      }\n   }\n\n"
                                "   private Object rndElement(Vector v) {")
            text = re.sub(r"Basilisk b = \(Basilisk\)ent;\n\s*b\.func_110163_bv\(\);", "ent.setPersistent();", text)
            text = text.replace("Vector outlist", "Vector<Point> outlist").replace("Vector inlist", "Vector<Point> inlist") \
                .replace("Vector frontlist", "Vector<Point> frontlist") \
                .replace("new Vector(", "new Vector<>(").replace("Vector v, Vector w", "Vector<Point> v, Vector<Point> w") \
                .replace("Vector outlist, Vector frontlist", "Vector<Point> outlist, Vector<Point> frontlist") \
                .replace("Vector inlist)", "Vector<Point> inlist)").replace("Object rndElement(Vector v)", "Object rndElement(Vector<Point> v)")
        emit_class(porter, cls, text, "import java.util.Vector;\n\n" if cls == "BasiliskMaze" else "")

    # OreSpawnWorld's placement methods, in a class of their own.
    world = open(os.path.join(SRC, "OreSpawnWorld.java"), encoding="utf-8").read().replace("\r\n", "\n")
    methods = world_methods(world, PLACEMENT)
    body = ("public final class OreSpawnWorldDungeons {\n"
            "   /** OreSpawn's config options these methods read, at their defaults. */\n"
            "   static final class Config {\n      static final int LessLag = 0;\n      static final int RotatorEnable = 1;\n"
            "      static final int UrchinEnable = 1;\n      static final int SpiderDriverEnable = 1;\n"
            "      static final int DisableOverworldDungeons = 0;\n      static final int MosquitoEnable = 1;\n   }\n\n"
            "   static final GenericDungeon DUNGEON = new GenericDungeon();\n"
            "   static final RubyBirdDungeon RUBY = new RubyBirdDungeon();\n"
            "   static final BasiliskMaze MAZE = new BasiliskMaze();\n"
            "   /** OreSpawnWorld.recently_placed: chunks to wait after a structure before the next (shared by every dimension). */\n"
            "   public static volatile int recently_placed = 50;\n\n"
            + "\n\n".join(methods).replace("this.quick", "quick").replace("this.D4", "D4") + "\n}\n")
    body = body.replace("   public boolean ", "   public static boolean ").replace("   public void ", "   public static void ") \
        .replace("   private boolean ", "   public static boolean ")
    body = body.replace("(World world, Random random,", "(World world, Random random,")
    emit_class(porter, "OreSpawnWorldDungeons", body, "import java.util.Random;\n\n")

    if porter.missing:
        fail("left untranslated:\n  " + "\n  ".join(sorted(porter.missing)))
    write_api()

    # ---- LB.java and LegacyStates.java ----
    consts = sorted(porter.used_blocks.items(), key=lambda kv: (kv[1][0] != "vanilla", kv[1][1] if kv[1][0] == "vanilla" else 0, kv[0]))
    lb_lines, state_lines, bad = [], [], []
    for const, (kind, val) in consts:
        if kind == "vanilla":
            lb_lines.append(f"\tpublic static final LB {const} = vanilla({val}, \"{const.lower()}\");")
            metas = []
            for meta in range(16):
                st = legacy_state(porter.table, val, meta)
                if st.split("[")[0] not in modern_blocks:
                    bad.append(f"{const}:{meta} -> {st}")
                metas.append(st)
            state_lines.append(f"\t\t{{\"{val}\", " + ", ".join(f"\"{s}\"" for s in metas) + "},")
        else:
            lb_lines.append(f"\tpublic static final LB {const} = mod(\"{val}\");")
    if bad:
        fail("flattened states missing from 26.3:\n  " + "\n  ".join(bad))
    with open(os.path.join(OUT, "LB.java"), "w", encoding="utf-8", newline="\n") as f:
        f.write(LB_JAVA.replace("%CONSTANTS%", "\n".join(lb_lines)))
    with open(os.path.join(OUT, "LegacyStates.java"), "w", encoding="utf-8", newline="\n") as f:
        f.write(STATES_JAVA.replace("%TABLE%", "\n".join(state_lines)))

    # ---- loot tables (gen_assets.py leaves files it didn't write alone) ----
    for item in sorted(porter.item_ids):
        if item.startswith("minecraft:") and item not in modern_items:
            fail(f"no 26.3 item {item}")
    loot_dir = os.path.join(PROJECT, "src", "main", "resources", "data", "orespawn", "loot_table", "chests", "dungeons")
    os.makedirs(loot_dir, exist_ok=True)
    for f in os.listdir(loot_dir):
        os.remove(os.path.join(loot_dir, f))
    for path, table in sorted(porter.loot.items()):
        with open(os.path.join(PROJECT, "src", "main", "resources", "data", "orespawn", "loot_table", path + ".json"), "w",
                  encoding="utf-8", newline="\n") as f:
            json.dump(table, f, indent=2)
            f.write("\n")
    print(f"wrote {len(porter.loot)} loot tables, {len(consts)} legacy blocks")


def lower_first(s):
    return s[0].lower() + s[1:]


def write_api():
    """OreSpawnDungeons: one public static method per dungeon (build it here) and per placement (OreSpawn's odds)."""
    generic = open(os.path.join(OUT, "GenericDungeon.java"), encoding="utf-8").read()
    builds = []
    for name in re.findall(r"\n\tpublic void (make[A-Z]\w*)\(DWorld world, int cposx, int cposy, int cposz\)", generic):
        api = lower_first(name[4:])
        if api == "dungeon":
            api = "genericDungeon"
        builds.append((api, f"OreSpawnWorldDungeons.DUNGEON.{name}", "GenericDungeon." + name))
    builds += [("basiliskMaze", "OreSpawnWorldDungeons.MAZE.buildBasiliskMaze", "BasiliskMaze.buildBasiliskMaze"),
               ("rubyBirdDungeon", "OreSpawnWorldDungeons.RUBY.makeDungeon", "RubyBirdDungeon.makeDungeon"),
               ("nightmareDungeon", "NIGHTMARE.makeDungeon", "NightmareDungeon.makeDungeon")]
    placements = re.findall(r"\n\tpublic static (boolean|void) (add\w+)\(DWorld world, Random random, int chunkX, int chunkZ\)",
                            open(os.path.join(OUT, "OreSpawnWorldDungeons.java"), encoding="utf-8").read())
    lines = []
    for api, call, src in sorted(builds):
        lines.append(f"\t/** Builds it at pos, as OreSpawn's {src} did. */\n"
                     f"\tpublic static void {api}(WorldGenLevel level, BlockPos pos, RandomSource random) {{\n"
                     f"\t\tMcWorld.run(level, random, w -> {{\n\t\t\t{call}(w, pos.getX(), pos.getY(), pos.getZ());\n"
                     f"\t\t\treturn null;\n\t\t}});\n\t}}\n")
    for ret, name in placements:
        body = (f"return McWorld.run(level, random, w -> OreSpawnWorldDungeons.{name}(w, w.rand, chunk.getMinBlockX(), "
                f"chunk.getMinBlockZ()));") if ret == "boolean" else (
                f"McWorld.run(level, random, w -> {{\n\t\t\tOreSpawnWorldDungeons.{name}(w, w.rand, chunk.getMinBlockX(), "
                f"chunk.getMinBlockZ());\n\t\t\treturn null;\n\t\t}});")
        lines.append(f"\t/** OreSpawnWorld.{name}: OreSpawn's odds and search for ground in this chunk, then the build. */\n"
                     f"\tpublic static {ret} {name}(WorldGenLevel level, ChunkPos chunk, RandomSource random) {{\n"
                     f"\t\t{body}\n\t}}\n")
    with open(os.path.join(OUT, "OreSpawnDungeons.java"), "w", encoding="utf-8", newline="\n") as f:
        f.write(API_JAVA.replace("%METHODS%", "\n".join(lines)))


API_JAVA = """package orespawn.world.dungeons;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;

/**
 * OreSpawn's dungeons, for other code (the dimension groups' world generation, commands, items).
 * <p>
 * The plain methods build one dungeon at a position, exactly as OreSpawn's GenericDungeon (and BasiliskMaze,
 * RubyBirdDungeon, NightmareDungeon) did. The {@code add...} methods are OreSpawnWorld's placement: OreSpawn's odds,
 * its biome checks and its search for ground in the given chunk, then the build; they return whether they built.
 * OreSpawnWorld.generate decides, per dimension, which of them to try in each chunk (see the Dungeons content class
 * for which dimension calls which), and most of them check and set {@link OreSpawnWorldDungeons#recently_placed}.
 * <p>
 * {@code level} is a ServerLevel, or the WorldGenLevel a feature or chunk generator is placing into: parts of a big
 * dungeon outside the chunks world generation may write to are placed when those chunks load (see McWorld).
 * Creatures and blocks of groups that aren't ported yet are skipped. Written by tools/dungeons/port_dungeons.py.
 */
public final class OreSpawnDungeons {
	private static final NightmareDungeon NIGHTMARE = new NightmareDungeon();

	private OreSpawnDungeons() {
	}

%METHODS%
	/**
	 * OreSpawnWorld.D4BigSpaceCheck: whether the 65 x 55 square at y + 4 around (x, z) is clear (air, logs and apple or
	 * scary leaves allowed). The Islands tried their structures only where it passed, at (chunk x, 7, chunk z).
	 */
	public static boolean d4BigSpaceCheck(WorldGenLevel level, int x, int y, int z, RandomSource random) {
		return McWorld.run(level, random, w -> OreSpawnWorldDungeons.D4BigSpaceCheck(w, x, y, z));
	}
}
"""


LB_JAVA = """package orespawn.world.dungeons;

import java.util.HashMap;
import java.util.Map;

/**
 * A 1.7.10 block as OreSpawn's dungeon code names it: a vanilla block by its 1.7.10 numeric id (its metadata picks the
 * 26.3 state, see {@link LegacyStates}), or one of OreSpawn's own blocks by its id here (looked up when placed; blocks
 * of groups that aren't ported yet are skipped). Written by tools/dungeons/port_dungeons.py.
 */
public final class LB {
	private static final Map<Integer, LB> BY_ID = new HashMap<>();
	private static final Map<String, LB> BY_MOD_ID = new HashMap<>();

	/** The 1.7.10 block id, or -1 for one of OreSpawn's blocks. */
	public final int id;
	/** The 1.7.10 name (vanilla) or the orespawn: id path (OreSpawn's). */
	public final String name;

	/** What {@link DWorld#getBlock} returns for a 26.3 block none of the constants below stands for. */
	public static final LB OTHER = new LB(-2, "other");
%CONSTANTS%

	private LB(int id, String name) {
		this.id = id;
		this.name = name;
	}

	private static LB vanilla(int id, String name) {
		LB lb = new LB(id, name);
		BY_ID.put(id, lb);
		return lb;
	}

	private static LB mod(String id) {
		LB lb = new LB(-1, id);
		BY_MOD_ID.put(id, lb);
		return lb;
	}

	/** A vanilla block (its 26.3 state comes from {@link LegacyStates}), not one of OreSpawn's. */
	public boolean isVanilla() {
		return this.id >= 0;
	}

	/** One of OreSpawn's blocks, placed by its id. */
	public boolean isModBlock() {
		return this.id == -1;
	}

	/** The constant for a 1.7.10 block id, or null if the dungeon code never uses it. */
	public static LB byId(int id) {
		return BY_ID.get(id);
	}

	/** The constant for one of OreSpawn's blocks, or null if the dungeon code never uses it. */
	public static LB byModId(String id) {
		return BY_MOD_ID.get(id);
	}

	public static Iterable<LB> vanillaBlocks() {
		return BY_ID.values();
	}

	public static Iterable<LB> modBlocks() {
		return BY_MOD_ID.values();
	}

	@Override
	public String toString() {
		return this.name;
	}
}
"""

STATES_JAVA = """package orespawn.world.dungeons;

/**
 * Every 1.7.10 block the dungeon code uses, with all 16 metadata values, as 26.3 block states: 26.3's own flattening
 * table (BlockStateData, which upgrades 1.12 worlds; 1.12 kept 1.7.10's ids and metadata for these blocks) plus the
 * renames since. Fences, panes, walls and stairs get their connections from their neighbours when placed (see McWorld).
 * Written by tools/dungeons/port_dungeons.py.
 */
final class LegacyStates {
	/** Rows of {1.7.10 id, state for metadata 0, ..., state for metadata 15}. */
	static final String[][] TABLE = {
%TABLE%
	};

	private LegacyStates() {
	}
}
"""

if __name__ == "__main__":
    main()
