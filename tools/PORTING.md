# Porting an OreSpawn creature to 26.3: how this project does it

OreSpawn 1.7.10-20.3 is being ported to Minecraft 26.3 Fabric for a private pack. The user allowed decompiling it and
using its textures, models and sounds. Be faithful: same stats, sizes, AI, sounds, drops, spawn rules and animations as
the original, unless the original depends on something not ported yet. Then leave that part out and say so in the
class's javadoc ("Not ported yet: ...").

## Where things are

- Decompiled source (read only, never run): `D:\Destinymods\_src_orespawn\decompiled\danger\orespawn\`
  (`<Name>.java` entity, `Model<Name>.java` model, `Render<Name>.java` renderer, `ClientProxyOreSpawn.java` renderer setup,
  `OreSpawnMain.java` registration, stats and spawns).
- OreSpawn's assets (textures, sounds.json): `D:\Destinymods\_src_orespawn\zip\assets\orespawn\`.
- Reading aid: `python -I tools/essentials.py <decompiled Entity.java>` prints the methods that matter, with common
  obfuscated names made readable.
- Minecraft 26.3 sources (read only): `C:\Users\ADMINI~1\AppData\Local\Temp\claude\cd07a9e34cc96a44\47c899b4-674a-419b-a7b7-6e200df005cd\scratchpad\mcsrc\net\minecraft\`.
  Note that 26.3 moved several things: entity types are in `EntityTypes`, `Entity.needsSync` replaced `hasImpulse`, biome spawns
  are environment attributes, game rules are `level.getGameRules().get(GameRules.MOB_GRIEFING)`, save data uses
  `ValueInput`/`ValueOutput` (`input.getIntOr`, `output.putInt`), synced data uses `defineSynchedData(SynchedEntityData.Builder)`.
- Done creatures to copy patterns from: `src/main/java/orespawn/entity/` (Butterfly, Moth, Firefly, Bird, Cricket, Frog,
  Rat, Beaver, Cassowary, Chipmunk, AppleCow) and their models in `src/main/java/orespawn/client/model/`.
  Shared helpers: `Flight` (OreSpawn's steering flight), `WanderGoal` (MyEntityAIWander / MyEntityAIWanderALot),
  `Ignored` (MyUtils.isIgnoreable), `ModSounds.get("<orespawn sound event name>")`.

## Mapping 1.7.10 to 26.3

- `getCanSpawnHere` -> a `public static boolean checkSpawn(EntityType<? extends Mob>, ServerLevelAccessor, EntitySpawnReason, BlockPos, RandomSource)`
  (spawners: `if (EntitySpawnReason.isSpawner(reason)) return true;`). OreSpawn's spawner-name checks can be dropped.
- `canDespawn` -> `removeWhenFarAway(double)`. `updateAITasks` / `updateAITick` -> `customServerAiStep(ServerLevel)`.
  `onUpdate` -> `tick()`. `attackEntityAsMob` -> `doHurtTarget(ServerLevel, Entity)`. `attackEntityFrom` -> `hurtServer(ServerLevel, DamageSource, float)`.
  `interact` -> `mobInteract(Player, InteractionHand)`. `fall` -> `causeFallDamage` / `calculateFallDamage`.
- Drops: a plain `getDropItem` + default `dropFewItems` (0-2, more with Looting) is a loot table; don't write it, list it
  in your report (item, min, max, looting yes/no). Drops that depend on state (tamed, variant, killed by player) go in
  `dropCustomDeathLoot`.
- 1.7.10 `EntityCannonFodder` / `EntityTameable` -> `TamableAnimal` (see Chipmunk for apple taming, sit, dead bush to free).
- Breeding with OreSpawn's Crystal Apple: not ported yet, so `isFood` returns false (say so in the javadoc).
- Monsters: `extends Monster`; their attack/health/armor come from `OreSpawnMain` `get_mobstats(config, mobs, "<Name>", health, attack, defense)`
  (defense is armor points).
- Riding (a mount): implement it the 26.3 way (see `AbstractHorse` / `Camel`: `getControllingPassenger`, `tickRidden`,
  `getRiddenInput`, `getRiddenSpeed`), matching OreSpawn's speeds.

## Models

1. `python -I tools/convert_model.py <decompiled Model<Name>.java> <Name>Model OreSpawnRenderState src/main/java/orespawn/client/model/<Name>Model.java`
   converts the geometry exactly and drafts `setupAnim` from OreSpawn's render() and setRotationAngles().
2. Fix every `// TODO port:` line by hand. The model's state is `orespawn.client.OreSpawnRenderState`; use only its fields:
   `texture, variant, sitting, singing, attacking, blink, verticalSpeed, glint, swirl, scale, ridden, inWater, flag, value`
   plus vanilla's (`ageInTicks, walkAnimationPos, walkAnimationSpeed, yRot, xRot, isBaby, ...`). If you need something
   else, use `flag`/`value` and say what it means, or report it; **don't edit OreSpawnRenderState or any shared file**.
3. If OreSpawn's render() draws a part only sometimes, set `part.visible` the same way. If it uses GL11 (scale,
   translate, colour) in the model, handle it in setupAnim or report it for the renderer.
4. The convert script makes a constructor `(ModelPart root, float wingspeed)` when the model has OreSpawn's speed
   argument; pass the value ClientProxyOreSpawn passes.

## What not to touch

Only create new files: `src/main/java/orespawn/entity/<Name>.java` and `src/main/java/orespawn/client/model/<Name>Model.java`
(plus small helpers in `orespawn/entity` if a creature needs one, named after it). **Do not edit** ModEntities, OreSpawnClient,
OreSpawnRenderState, OreSpawnMobRenderer, ModItems, ModBlocks, ModSounds, gen_assets.py, tests, or other creatures' files.
Other agents are porting other creatures at the same time. Entity types are referenced as `ModEntities.<ID_UPPER>`: the
main session adds them. Items you need that already exist: `ModItems.PEACOCK_FEATHER, RAW_PEACOCK, COOKED_PEACOCK,
DEAD_STINK_BUG, RAW_CRAB_MEAT, CRAB_MEAT, URANIUM_NUGGET, TITANIUM_NUGGET, RUBY, AMETHYST`. Don't run Gradle builds or game
tests (the main session builds everything together); write carefully against the 26.3 sources instead.

## Report back (one block per creature)

```
id: <snake_case id>            class: <Name>        OreSpawn name: <registerModEntity name>
category: AMBIENT|CREATURE|MONSTER|WATER_CREATURE|...   size: <w> x <h>   tracking range: <chunks>
attributes: <how createAttributes is defined>
placement: ON_GROUND|NO_RESTRICTIONS|IN_WATER   heightmap: MOTION_BLOCKING_NO_LEAVES|OCEAN_FLOOR   predicate: <Name>::checkSpawn (or vanilla's)
spawn table: <the "biome:weight:min:max" string given in your task, or "none">
renderer: model new <Name>Model(root[, speed]), shadow <f>, scale <f>, extractor: (e, s) -> { s.texture = ...; ... }, light: null|...
textures: <OreSpawn file> -> textures/entity/<our name>.png (each)
spawn egg texture: <OreSpawn items/egg... name>     egg ore texture: <blocks/ore... name or none>
sounds: <every orespawn sound event name the class uses>
loot table: <items/counts/looting, or "none" / "in code">
ragdoll: from_model | scatter | none   (top-level model part count: N; Wither's Combat allows at most 24 pieces)
left out: <what wasn't ported and why>
```

# Content groups (batch 3 onwards)

From batch 3 on, each agent ports a whole **content group** (a set of creatures, items, blocks, or a dimension) and owns
its registration, so groups never edit the same files. The main session only adds one line per group to
`orespawn.content.Content.init()`, `orespawn.client.ClientContent.init()` and the test entrypoints.

## Files a group creates (and only these)

- `src/main/java/orespawn/content/<Group>.java`: the group's registrations. Entity types, items and blocks are `public static
  final` fields, and `public static void init()` does the rest. Use the shared helpers:
  - `ModEntities.register(id, Factory::new, MobCategory, width, height, trackingRange)` for a creature. It also makes its spawn egg
    `<id>_spawn_egg`.
  - `ModEntities.misc(id, Factory::new, w, h, range, updateInterval)` for projectiles and other non-mob entities.
  - In init: `FabricDefaultAttributeRegistry.register(TYPE, X.createAttributes())`,
    `SpawnPlacements.register(TYPE, SpawnPlacementTypes.ON_GROUND|IN_WATER|NO_RESTRICTIONS, Heightmap.Types..., X::checkSpawn)`,
    and `ModEntities.spawns(TYPE, "<OreSpawn spawn table: biome:weight:min:max ...>")` (OreSpawn's 1.7.10 biome names; the
    table in ModEntities.BIOMES maps them. If a biome you need is missing, report it).
  - `ModBlocks.eggOre(id, "<OreSpawn ore texture>", OreSpawn.id(id + "_spawn_egg"))` for a creature's Ancient Dried egg ore.
  - `ModItems.register(id, new SomeItem(ModItems.props(id)...))` / `ModItems.item(id, props)`, `ModItems.food(n, sat)`.
  - `ModBlocks.block(id, factory, props)` (makes its BlockItem too).
  - `ModItems.lazy("<id>")`: an item another group owns, looked up when needed (air until that group exists). Use it instead
    of referring to another group's class.
- `src/main/java/orespawn/entity/<group>/...`: the group's entity classes (package `orespawn.entity.<group>`), and
  `src/main/java/orespawn/item/<group>/...`, `src/main/java/orespawn/block/<group>/...` for item and block classes.
- `src/main/java/orespawn/client/model/<Name>Model.java`: models (unique names; check the folder first).
- `src/main/java/orespawn/client/content/<Group>Client.java`: `public static void init()` registering renderers with
  `OreSpawnClient.mob(TYPE, OreSpawnClient.layer("<id>", XModel::createBodyLayer), root -> new XModel(root, speed), shadow,
  scale, (e, s) -> { s.texture = OreSpawnClient.tex("<file>.png"); ... }, null)` (see OreSpawnClient for the batch 1 calls),
  or `EntityRendererRegistry.register(...)` for other renderers (ThrownItemRenderer for item-like projectiles, or your own
  renderer class in orespawn.client.content).
- `tools/content/<group>.json`: the group's assets, which tools/gen_assets.py reads:
  ```json
  {
    "creatures": {"<id>": {"name": "<OreSpawn name>", "egg": "<OreSpawn items/egg... texture>",
                           "egg_ore": {"texture": "<blocks/ore... texture>", "name": "<name as in 'Ancient Dried X Spawn Egg'>"},
                           "textures": {"<OreSpawn file under assets/orespawn/>": "<our file>.png"},
                           "loot": [{"item": "minecraft:feather", "min": 0, "max": 2, "looting": true}],
                           "ragdoll": "from_model" | "scatter" | null}},
    "items": {"<id>": {"texture": "<OreSpawn items/ texture name>", "name": "<English name>", "handheld": true}},
    "sounds": ["<orespawn sound event names the group uses>"],
    "recipes": {"<file name>": { <a 26.3 recipe json object> }},
    "tags": {"item/minecraft/swords": ["orespawn:<id>"]},
    "lang": {"<translation key>": "<English>"},
    "blocks": {"<id>": {"texture": "<OreSpawn blocks/ texture>", "name": "<English>", "model": "cube" | "cross" | "none",
                        "textures": {"top": "..", "bottom": "..", "side": ".."}, "drops": "self" | "none" | "<item id>"}},
    "worldgen": {"feature/<name>": { <worldgen json> }, "placed_feature/<name>": { ... }, "biome/<name>": { ... }},
    "data": {"<path under data/orespawn/, with .json>": { ... }},
    "assets": {"<path under assets/orespawn/, with .json>": { ... }},
    "block_textures": {"<OreSpawn blocks/ texture>": "<our textures/block/ name>"}
  }
  ```
  `blocks`: "cube" (default) uses "texture" on every face, or "textures" for a top/bottom/side block; "cross" is a plant;
  "none" means the group ships its own blockstate and models. `worldgen` files go to `data/orespawn/worldgen/<key>.json`.
  gen_assets.py deletes only the files it wrote on its last run (listed in tools/.generated_files.txt), so hand-written
  files elsewhere under `src/main/resources` are safe.
  Entity textures go to `textures/entity/<our file>`; reference them as `OreSpawnClient.tex("<our file>")`. Item textures
  come from OreSpawn's `textures/items/`. For a block, or anything the spec can't express, say what you need in your report,
  and the main session adds it to gen_assets.py.
- Optional: `src/gametest/java/orespawn/test/<Group>Test.java`, a `FabricClientGameTest` that checks your group's own
  behaviour (copy the style of CreatureTest). The main session registers it. Keep it short.

## Ids

- `tools/ids.tsv` lists every OreSpawn item and block, with a canonical id (its English name in snake_case), its OreSpawn
  field, class and texture.
- **Use those ids for new things**, except:
  - spawn eggs, which are `<creature id>_spawn_egg` and come from ModEntities.register;
  - egg ores, which are `<creature id>_egg_ore`;
  - anything already registered: check ModItems.ITEMS, ModBlocks.BLOCKS and the content classes. Batch 1 already used
    `ruby_sword`, `ultimate_sword`, `emerald_helmet` and so on.
- Creature ids are the OreSpawn entity name in snake_case: "Velocity Raptor" is `velocity_raptor`.
- When an item and a block share a name, like pizza, the block's item is the item.

## Building

- You may run `./run-gradle.sh compileJava --console=plain` (in D:\Destinymods\OreSpawnFabric) to check your code.
- Other agents are writing files at the same time, so ignore errors in files that aren't yours, and fix all of yours.
- Run `python -I tools/gen_assets.py` before compiling if you add a spec.
- Never run game tests, and never touch other groups' files or the shared ones listed above.
