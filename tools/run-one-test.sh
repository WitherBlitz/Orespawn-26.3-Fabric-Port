#!/bin/sh
# Runs one client game test class, muted: sh tools/run-one-test.sh <Class> [-PallMods]. Writes build/one-<Class>.log and
# restores the full entrypoint list afterwards.
cd "$(dirname "$0")/.." || exit 1
cls=$1; shift
cp src/gametest/resources/fabric.mod.json build/fabric.mod.json.before-one
python -I - "$cls" <<'PY'
import json, sys
p = "src/gametest/resources/fabric.mod.json"
d = json.load(open(p, encoding="utf-8"))
d["entrypoints"]["fabric-client-gametest"] = ["orespawn.test." + sys.argv[1]]
open(p, "w", encoding="utf-8", newline="\n").write(json.dumps(d, indent="\t") + "\n")
PY
rm -rf build/run/clientGameTest
ALSOFT_DRIVERS=null ./run-gradle.sh --no-configuration-cache runClientGameTest "$@" --console=plain > "build/one-$cls.log" 2>&1
status=$?
cp build/fabric.mod.json.before-one src/gametest/resources/fabric.mod.json
grep -a "OS-[A-Z]* \(ok\|FAIL\|ALL\)\|BUILD\|error:" "build/one-$cls.log" | sed 's/.*\[STDOUT\]: //'
exit $status
