"""Converts one decompiled OreSpawn 1.7.10 model (ModelX.java, Techne style) into a 26.3 EntityModel class.

    python -I tools/convert_model.py <decompiled ModelX.java> <ClassName> <RenderStateClass> <output .java>

Geometry (boxes, pivots, starting rotations, texture offsets, mirroring, children) is converted exactly. The animation
code from render() and setRotationAngles() is translated mechanically into setupAnim() as a first draft: rotation and
pivot fields and MathHelper calls are renamed, and the old parameters become locals read from the render state.
Lines that touch the entity or OpenGL are left as comments starting with "// TODO port:" to be finished by hand.
The output is meant to be edited afterwards; this script isn't re-run over finished models.
"""
import re
import sys

src_path, cls, state_cls, out_path = sys.argv[1:5]
src = open(src_path, encoding="utf-8").read()

FIELDS = {"field_78795_f": "xRot", "field_78796_g": "yRot", "field_78808_h": "zRot",
          "field_78800_c": "x", "field_78797_d": "y", "field_78798_e": "z"}

tex_w = int(re.search(r"field_78090_t = (\d+)", src).group(1)) if "field_78090_t" in src else 64
tex_h = int(re.search(r"field_78089_u = (\d+)", src).group(1)) if "field_78089_u" in src else 32

_cm = re.search(r"public " + re.escape(src_path.replace("\\", "/").split("/")[-1][:-5]) + r"\(([^)]*)\) \{(.*?)\n   \}", src, re.S)
ctor = _cm.group(2)
CTOR_PARAMS = [p.split()[-1] for p in _cm.group(1).split(",") if p.strip()]
# Fields the constructor sets from its own arguments (OreSpawn's speed argument and the like) become constructor arguments.
PARAM_FIELDS = re.findall(r"this\.(\w+) = (" + "|".join(map(re.escape, CTOR_PARAMS)) + r");", ctor) if CTOR_PARAMS else []


def num(s):
    s = re.sub(r"\(float\)\s*", "", s.strip())
    s = re.sub(r"(?<=[0-9.])[Ff]", "", s).replace("Math.PI", "3.141592653589793")
    return float(eval(s, {"__builtins__": {}}))


parts = {}   # name -> dict
order = []
for line in ctor.split(";"):
    line = " ".join(line.split())
    m = re.match(r"this\.(\w+) = new ModelRenderer\(this, (-?\d+), (-?\d+)\)", line)
    if m:
        name, u, v = m.group(1), int(m.group(2)), int(m.group(3))
        parts[name] = {"u": u, "v": v, "mirror": False, "boxes": [], "pivot": (0.0, 0.0, 0.0), "rot": [0.0, 0.0, 0.0], "children": [],
                       "parent": None}
        order.append(name)
        continue
    m = re.match(r"this\.(\w+) = new ModelRenderer\(this\)", line)
    if m:
        parts[m.group(1)] = {"u": 0, "v": 0, "mirror": False, "boxes": [], "pivot": (0.0, 0.0, 0.0), "rot": [0.0, 0.0, 0.0],
                             "children": [], "parent": None}
        order.append(m.group(1))
        continue
    m = re.match(r"this\.(\w+)\.func_7878(9|0)_a\((.*)\)", line)
    if m:
        p = parts[m.group(1)]
        args = [a.strip() for a in m.group(3).split(",")]
        grow = num(args[6]) if len(args) > 6 else 0.0
        p["boxes"].append((p["u"], p["v"], p["mirror"], [num(a) for a in args[:6]], grow))
        continue
    m = re.match(r"this\.(\w+)\.func_78784_a\((-?\d+), (-?\d+)\)", line)
    if m:
        parts[m.group(1)]["u"], parts[m.group(1)]["v"] = int(m.group(2)), int(m.group(3))
        continue
    m = re.match(r"this\.(\w+)\.func_78793_a\((.*)\)", line)
    if m:
        parts[m.group(1)]["pivot"] = tuple(num(a) for a in m.group(2).split(","))
        continue
    m = re.match(r"this\.(\w+)\.field_78809_i = (true|false)", line)
    if m:
        parts[m.group(1)]["mirror"] = m.group(2) == "true"
        continue
    m = re.match(r"this\.setRotation\(this\.(\w+), (.*)\)", line)
    if m:
        parts[m.group(1)]["rot"] = [num(a) for a in m.group(2).split(",")]
        continue
    m = re.match(r"this\.(\w+)\.(field_78795_f|field_78796_g|field_78808_h) = (.*)", line)
    if m and m.group(1) in parts:
        parts[m.group(1)]["rot"][["field_78795_f", "field_78796_g", "field_78808_h"].index(m.group(2))] = num(m.group(3))
        continue
    # Pivot changes after the part is made (ModelStinkBug ends with rotationPointY += 6 on every part).
    m = re.match(r"this\.(\w+)\.(field_78800_c|field_78797_d|field_78798_e) (\+=|-=|=) (.*)", line)
    if m and m.group(1) in parts:
        try:
            value = num(m.group(4))
            axis = ["field_78800_c", "field_78797_d", "field_78798_e"].index(m.group(2))
            pv = list(parts[m.group(1)]["pivot"])
            pv[axis] = value if m.group(3) == "=" else pv[axis] + (value if m.group(3) == "+=" else -value)
            parts[m.group(1)]["pivot"] = tuple(pv)
            continue
        except Exception:
            pass
    m = re.match(r"this\.(\w+)\.func_78792_a\(this\.(\w+)\)", line)
    if m:
        parts[m.group(1)]["children"].append(m.group(2))
        parts[m.group(2)]["parent"] = m.group(1)
        continue

field_names = {n: re.sub(r"[^A-Za-z0-9]", "_", n) for n in parts}


def f(x):
    s = repr(round(x, 7))
    return s + "F"


def part_java(name, parent_var, indent):
    p = parts[name]
    lines = []
    cubes = "CubeListBuilder.create()"
    for (u, v, mirror, (x, y, z, w, h, d), grow) in p["boxes"]:
        cubes += f".texOffs({u}, {v})" + (".mirror()" if mirror else ".mirror(false)")
        dims = ", ".join(f(a) for a in (x, y, z, w, h, d))
        cubes += f".addBox({dims}" + (f", new CubeDeformation({f(grow)})" if grow else "") + ")"
    px, py, pz = p["pivot"]
    rx, ry, rz = p["rot"]
    pose = f"PartPose.offsetAndRotation({f(px)}, {f(py)}, {f(pz)}, {f(rx)}, {f(ry)}, {f(rz)})"
    var = "p_" + field_names[name]
    lines.append(f"{indent}PartDefinition {var} = {parent_var}.addOrReplaceChild(\"{name.lower()}\", {cubes},\n{indent}\t{pose});")
    for c in p["children"]:
        lines += part_java(c, var, indent)
    return lines


def method_body(sig_regex):
    m = re.search(sig_regex + r"[^{]*\{(.*?)\n   \}", src, re.S)
    return m.group(1) if m else ""


def translate(body, params):
    out = []
    for raw in body.strip("\n").split("\n"):
        line = raw.strip()
        if not line:
            continue
        if re.match(r"(this\.\w+\.func_78785_a\(|super\.func_7808[78]_a\(|this\.func_78087_a\(|super\.func_78087_a\()", line):
            continue
        t = line
        for k, v in FIELDS.items():
            t = t.replace("." + k, "." + v)
        t = (t.replace("MathHelper.func_76134_b(", "Mth.cos(").replace("MathHelper.func_76126_a(", "Mth.sin(")
             .replace("MathHelper.func_76135_e(", "Mth.abs(").replace("MathHelper.func_76129_c(", "Mth.sqrt("))
        if re.search(r"GL11|entity|par7Entity|Entity|func_\w+|field_\w+", t):
            out.append("\t\t// TODO port: " + t)
        else:
            out.append("\t\t" + t)
    return out


def renamed_body(sig, names):
    """The body of the method sig matches (a regex capturing its parameter names), with the parameters renamed to names."""
    m = re.search(sig, src)
    if not m:
        return ""
    body = method_body(re.escape(m.group(0)))
    for old_name, new_name in zip(m.groups(), names):
        if old_name != new_name:
            body = re.sub(r"\b" + re.escape(old_name) + r"\b", "__P_" + new_name, body)
    return body.replace("__P_", "")


render = renamed_body(r"public void func_78088_a\(Entity (\w+), float (\w+), float (\w+), float (\w+), float (\w+), float (\w+), float (\w+)\)",
                      ["entity", "f", "f1", "f2", "f3", "f4", "f5"])
angles = renamed_body(r"public void func_78087_a\(float (\w+), float (\w+), float (\w+), float (\w+), float (\w+), float (\w+), Entity (\w+)\)",
                      ["par1", "par2", "par3", "par4", "par5", "par6", "par7Entity"])
extra_fields = re.findall(r"^   private (float|int|boolean) (\w+) = ([^;]+);", src, re.M)

roots = [n for n in order if parts[n]["parent"] is None]
J = []
J.append("package orespawn.client.model;\n")
J.append("import net.fabricmc.api.EnvType;\nimport net.fabricmc.api.Environment;\nimport net.minecraft.client.model.EntityModel;")
J.append("import net.minecraft.client.model.geom.ModelPart;\nimport net.minecraft.client.model.geom.PartPose;")
J.append("import net.minecraft.client.model.geom.builders.CubeDeformation;\nimport net.minecraft.client.model.geom.builders.CubeListBuilder;")
J.append("import net.minecraft.client.model.geom.builders.LayerDefinition;\nimport net.minecraft.client.model.geom.builders.MeshDefinition;")
J.append("import net.minecraft.client.model.geom.builders.PartDefinition;\nimport net.minecraft.util.Mth;")
J.append("import orespawn.client.OreSpawnRenderState;\n")
J.append(f"/** Ported from OreSpawn's {src_path.replace(chr(92), '/').split('/')[-1][:-5]} (geometry converted by tools/convert_model.py). */")
J.append("@SuppressWarnings(\"unused\")\n@Environment(EnvType.CLIENT)")
J.append(f"public class {cls} extends EntityModel<{state_cls}> {{")
for n in order:
    J.append(f"\tprivate final ModelPart {field_names[n]};")
param_names = [name for name, _ in PARAM_FIELDS]
for t, n, v in extra_fields:
    J.append(f"\tprivate final {t} {n};" if n in param_names else f"\tprivate {t} {n} = {v};")
ctor_args = "".join(f", float {n}" for n in param_names)
J.append(f"\n\tpublic {cls}(ModelPart root{ctor_args}) {{\n\t\tsuper(root);")
for n in param_names:
    J.append(f"\t\tthis.{n} = {n};")


def getters(name, expr):
    out = [f"\t\tthis.{field_names[name]} = {expr}.getChild(\"{name.lower()}\");"]
    for c in parts[name]["children"]:
        out += getters(c, "this." + field_names[name])
    return out


for r in roots:
    J += getters(r, "root")
J.append("\t}\n")
J.append("\tpublic static LayerDefinition createBodyLayer() {\n\t\tMeshDefinition mesh = new MeshDefinition();\n\t\tPartDefinition root = mesh.getRoot();")
for r in roots:
    J += part_java(r, "root", "\t\t")
J.append(f"\t\treturn LayerDefinition.create(mesh, {tex_w}, {tex_h});\n\t}}\n")
J.append(f"\t@Override\n\tpublic void setupAnim({state_cls} state) {{\n\t\tsuper.setupAnim(state);")
J.append("\t\tfloat f = state.walkAnimationPos, f1 = state.walkAnimationSpeed, f2 = state.ageInTicks, f3 = state.yRot, f4 = state.xRot;")
J.append("\t\tfloat par1 = f, par2 = f1, par3 = f2, par4 = f3, par5 = f4;")
J.append("\t\t// from setRotationAngles:")
J += translate(angles, None)
J.append("\t\t// from render:")
J += translate(render, None)
J.append("\t}")
J.append("}")
text = "\n".join(J) + "\n"
for n in order:
    text = text.replace("this." + n + ".", "this." + field_names[n] + ".")
import os
os.makedirs(os.path.dirname(out_path), exist_ok=True)
open(out_path, "w", encoding="utf-8", newline="\n").write(text)
print(f"{cls}: {len(order)} parts, {sum(len(p['boxes']) for p in parts.values())} boxes, {tex_w}x{tex_h}, "
      f"{text.count('TODO port')} lines to finish by hand")
