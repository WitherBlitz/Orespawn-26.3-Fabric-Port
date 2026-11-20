"""Writes src/main/resources/orespawn/original/assets.tsv: where each of the port's textures and sounds comes from in the
original OreSpawn 1.7.10-20.3 file. The port ships none of them; orespawn.client.original.OriginalPack reads them from
that file at runtime, using this list. It also writes sound_events.txt, the sound events the port uses (their files are
named by OreSpawn's own sounds.json, read at runtime too).

    python -I tools/original_assets.py <assets folder made by gen_assets.py etc.> <unzipped OreSpawn 1.7.10-20.3>

The first folder is a full build of the old resources (src/main/resources/assets/orespawn when it still held the
textures and sounds). Each file is matched to OreSpawn's by content:
  copy   <ours>  <theirs>                  the same file (or the same pixels, saved again)
  tint   <ours>  <theirs>  <r,g,b>         the same pixels with each colour channel scaled by r/255, g/255, b/255
  baby   <ours>  <layer 1>  <layer 2>      26.3's baby armor layout, made from OreSpawn's two worn armor layers
A file that matches nothing stops the script.
"""
import hashlib
import json
import os
import sys

from PIL import Image

OURS, THEIRS = sys.argv[1], os.path.join(sys.argv[2], "assets", "orespawn")
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources", "orespawn", "original")
TINTS = [(0xDD, 0xDD, 0xDD)]   # crystal_assets.py's LEAF_TINT


def sha1(path):
    return hashlib.sha1(open(path, "rb").read()).hexdigest()


def rel(root, path):
    return os.path.relpath(path, root).replace(os.sep, "/")


def walk(root):
    for d, _, files in os.walk(root):
        for f in files:
            yield os.path.join(d, f)


theirs_by_hash = {}
theirs_pngs = []
for p in sorted(walk(THEIRS)):
    theirs_by_hash.setdefault(sha1(p), rel(THEIRS, p))
    if p.endswith(".png"):
        theirs_pngs.append(p)
pixels = {}


def rgba(path):
    if path not in pixels:
        pixels[path] = Image.open(path).convert("RGBA")
    return pixels[path]


def tinted(image, tint):
    r, g, b, a = image.split()
    r, g, b = (band.point(lambda v, t=t: v * t // 255) for band, t in zip((r, g, b), tint))
    return Image.merge("RGBA", (r, g, b, a))


rows = []
babies = []
for p in sorted(walk(OURS)):
    ours = rel(OURS, p)
    if not (ours.startswith("textures/") or ours.startswith("sounds/")):
        continue
    if ours.startswith("textures/entity/equipment/humanoid_baby/"):
        babies.append(ours)
        continue
    h = sha1(p)
    if h in theirs_by_hash:
        rows.append(("copy", ours, "assets/orespawn/" + theirs_by_hash[h]))
        continue
    image = rgba(p)
    found = None
    for t in theirs_pngs:
        candidate = rgba(t)
        if candidate.size != image.size:
            continue
        if candidate.tobytes() == image.tobytes():
            found = ("copy", ours, "assets/orespawn/" + rel(THEIRS, t))
            break
        for tint in TINTS:
            if tinted(candidate, tint).tobytes() == image.tobytes():
                found = ("tint", ours, "assets/orespawn/" + rel(THEIRS, t), ",".join(str(c) for c in tint))
                break
        if found:
            break
    if not found:
        raise SystemExit(f"no source in OreSpawn for {ours}")
    rows.append(found)

# Baby armor: the material's humanoid and humanoid_leggings textures are OreSpawn's layer 1 and 2.
by_ours = {r[1]: r for r in rows}
for ours in babies:
    name = ours.rsplit("/", 1)[1]
    layer1 = by_ours["textures/entity/equipment/humanoid/" + name][2]
    layer2 = by_ours["textures/entity/equipment/humanoid_leggings/" + name][2]
    rows.append(("baby", ours, layer1, layer2))

os.makedirs(OUT, exist_ok=True)
with open(os.path.join(OUT, "assets.tsv"), "w", encoding="utf-8", newline="\n") as f:
    f.write("# Where each texture and sound of the port comes from in OreSpawn 1.7.10-20.3 (see tools/original_assets.py)\n")
    for r in sorted(rows, key=lambda r: r[1]):
        f.write("\t".join(r) + "\n")
events = sorted(json.load(open(os.path.join(OURS, "sounds.json"), encoding="utf-8")))
with open(os.path.join(OUT, "sound_events.txt"), "w", encoding="utf-8", newline="\n") as f:
    f.write("\n".join(events) + "\n")
counts = {}
for r in rows:
    counts[r[0]] = counts.get(r[0], 0) + 1
print(counts, len(events), "sound events")
