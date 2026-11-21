# **MAKE SURE TO HAVE THE ORIGINAL 1.7.10 MOD IN YOUR MODS FOLDER.**

This port contains none of OreSpawn's files. It reads OreSpawn's textures, sounds, models and dungeons from the
original OreSpawn 1.7.10-20.3 file (`orespawn-1.7.10-20.3.zip`). Without that file, the game stops at startup with a
message saying what's missing.

## Install

1. Install Minecraft 26.3 with Fabric Loader 0.20.1 or newer, Fabric API and Java 25.
2. Put `OreSpawn-fabric-1.0.1-26.3.jar` (below) in your mods folder. If you had 1.0.0, remove
   `OreSpawn-fabric-1.0.0-26.3.jar`.
3. Put the original `orespawn-1.7.10-20.3.zip`, from OreSpawn 1.7.10's own download, in the same mods folder.
   - Leave it zipped. Fabric ignores it, and this mod finds it by what's inside, so any file name works.
   - On a server, it goes in the server's mods folder too.

## What's new since 1.0.0

- **3D weapons in the hand:**
  - Big Bertha, Slice, the Royal Guardian Sword, the Attitude Adjuster, the Battle Axe, the Queen Scale Battle Axe,
    the Chainsaw and the SquidZooka show OreSpawn's own 3D models when held, in first and third person.
  - The Chainsaw's chain runs along the blade.
  - In the inventory, on the ground and in frames they show their flat icons, as in OreSpawn.
- **With Wither's Animations:**
  - The weapons keep their 3D models. The mod ships the `locomotion:keeps_hand_model` item tag for them.
  - In its first-person hand, the axes and the Attitude Adjuster rise from the fist like a sword, and the SquidZooka
    points forward.
  - This needs a Wither's Animations build with that tag and its first-person hand weight; with an older one, they
    show flat.
- **Tests:** a new `HeldWeaponsTest` checks every hand view and takes first- and third-person screenshots.

See the [README](https://github.com/WitherBlitz/Orespawn-26.3-Fabric-Port#readme) for everything in the port and how it
uses the original file.

The jar's SHA-1 is `527467b08c620cb79313b4e248b0bb3b66c41e11`.
