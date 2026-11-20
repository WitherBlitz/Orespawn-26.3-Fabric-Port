"""Prints the parts of a decompiled OreSpawn entity that a port needs, with common obfuscated names made readable.

    python -I tools/essentials.py <decompiled Entity.java> [more...]

A reading aid for porting (not part of the build): constructor, attributes, health, sounds, drops, spawn and despawn
rules, interaction, AI tick, update, attack and damage handlers.
"""
import re
import sys

METHODS = {
    "func_110147_ax": "applyEntityAttributes", "func_70639_aQ": "getLivingSound", "func_70621_aR": "getHurtSound",
    "func_70673_aS": "getDeathSound", "func_146068_u": "getDropItem", "func_70628_a": "dropFewItems", "func_70601_bi": "getCanSpawnHere",
    "func_70692_ba": "canDespawn", "func_70085_c": "interact", "func_70619_bc": "updateAITasks", "func_70071_h_": "onUpdate",
    "func_70652_k": "attackEntityAsMob", "func_70097_a": "attackEntityFrom", "func_70636_d": "onLivingUpdate", "func_70629_bd": "updateAITick",
    "func_70599_aP": "getSoundVolume", "func_70648_aU": "canBreatheUnderwater", "func_70877_b": "isBreedingItem",
    "func_70069_a": "fall", "func_70664_aZ": "jump", "func_70658_aO": "getTotalArmorValue", "func_70090_H": "isInWater",
}
NAMES = {
    "field_70170_p": "world", "field_73012_v": "rand", "field_70146_Z": "rand", "field_70165_t": "posX", "field_70163_u": "posY",
    "field_70161_v": "posZ", "field_70159_w": "motionX", "field_70181_x": "motionY", "field_70179_y": "motionZ", "field_70177_z": "rotationYaw",
    "field_70125_A": "rotationPitch", "field_70128_L": "isDead", "field_72995_K": "isRemote", "func_70105_a": "setSize",
    "field_70714_bg": "tasks", "field_70715_bh": "targetTasks", "func_75776_a": "addTask", "func_110148_a": "getAttribute",
    "func_111128_a": "setBaseValue", "field_111267_a": "MAX_HEALTH", "field_111263_d": "MOVEMENT_SPEED", "field_111264_e": "ATTACK_DAMAGE",
    "func_70661_as": "getNavigator", "func_75491_a": "setAvoidsWater", "field_70728_aV": "experienceValue", "field_70174_ab": "fireResistance",
    "func_145779_a": "dropItem", "func_70068_e": "getDistanceSqToEntity", "func_75497_a": "tryMoveToEntityLiving",
    "func_75492_a": "tryMoveToXYZ", "func_72935_r": "isDaytime", "func_147439_a": "getBlock", "func_72872_a": "getEntitiesWithinAABB",
    "field_70121_D": "boundingBox", "func_72314_b": "expand", "func_70691_i": "heal", "func_110143_aJ": "getHealth",
    "func_70604_c": "setRevengeTarget", "func_85030_a": "playSound", "func_76346_g": "getEntity", "field_73013_u": "difficulty",
    "func_70093_af": "isSneaking", "field_71071_by": "inventory", "func_70448_g": "getCurrentItem", "func_77973_b": "getItem",
    "field_71075_bZ": "capabilities", "field_75098_d": "isCreative", "func_70909_n": "isTamed", "func_70906_o": "isSitting",
    "func_70904_g": "setSitting", "func_70631_g_": "isChild", "func_104002_bU": "isNoDespawnRequired", "func_70089_S": "isEntityAlive",
    "field_70160_al": "isAirBorne", "field_70122_E": "onGround", "func_70072_I": "isInWater", "field_70173_aa": "ticksExisted",
    "func_72838_d": "spawnEntityInWorld", "func_70106_y": "setDead", "func_70107_b": "setPosition", "func_70634_a": "setPositionAndUpdate",
    "field_70153_n": "riddenByEntity", "field_70154_o": "ridingEntity", "func_70078_a": "mountEntity", "field_70702_br": "moveStrafing",
    "field_70701_bs": "moveForward", "func_70635_at": "getEntitySenses", "func_75522_a": "canSee", "func_70690_d": "addPotionEffect",
    "func_82736_K": "getGameRules", "func_82766_b": "getGameRuleBooleanValue", "func_147465_d": "setBlock", "func_147449_b": "setBlock",
    "field_150350_a": "AIR", "field_150355_j": "WATER", "field_150358_i": "FLOWING_WATER", "field_150348_b": "STONE", "field_150349_c": "GRASS",
    "field_150346_d": "DIRT", "field_150354_m": "SAND", "func_76358_a": "causeMobDamage", "func_70045_F": "isImmuneToFire",
    "field_70172_ad": "hurtResistantTime", "func_76123_f": "ceil", "func_76142_g": "wrapAngleTo180", "func_70661_as().func_75499_g": "clearPath",
}


def body(src, start):
    depth = 0
    i = src.index("{", start)
    j = i
    while True:
        if src[j] == "{":
            depth += 1
        elif src[j] == "}":
            depth -= 1
            if depth == 0:
                return src[i + 1:j]
        j += 1


def clean(text):
    for k, v in sorted(NAMES.items(), key=lambda kv: -len(kv[0])):
        text = text.replace(k, v)
    for k, v in METHODS.items():
        text = text.replace(k, v)
    text = text.replace("this.", "")
    lines = [l.rstrip() for l in text.split("\n") if l.strip()]
    return "\n".join(lines)


for path in sys.argv[1:]:
    src = open(path, encoding="utf-8").read()
    cls = re.search(r"public class (\w+) extends (\w+)", src)
    print(f"=================== {cls.group(1)} extends {cls.group(2)}")
    fields = re.findall(r"^   (?:private|public|protected)(?: static)?(?: final)? [\w<>\[\]]+ \w+(?: = [^;]+)?;", src, re.M)
    print(clean("\n".join(f for f in fields if "ResourceLocation" not in f)))
    m = re.search(r"public " + cls.group(1) + r"\(World \w+\)", src)
    if m:
        print("-- constructor\n" + clean(body(src, m.start())))
    for obf, name in METHODS.items():
        for mm in re.finditer(r"\n   (?:public|protected|private)[^\n(]*\b" + obf + r"\(", src):
            print(f"-- {name}\n" + clean(body(src, mm.start())))
    for mm in re.finditer(r"\n   (?:public|protected|private)[^\n(=]* (\w+)\(", src):
        n = mm.group(1)
        if n.startswith("func_") or n == cls.group(1):
            continue
        print(f"-- {n}\n" + clean(body(src, mm.start())))
