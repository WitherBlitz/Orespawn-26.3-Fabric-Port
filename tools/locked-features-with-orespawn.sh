#!/bin/sh
# The locked-features check (WithersCombatSystemExtensionFabric/test-locked-features.sh) with OreSpawn loaded. It does
# the same two runs: the pack's combat mods (-PwithPack), then every mod of AllMods-26.3, but adds OreSpawn's jar from
# build/libs to CSE's copy of AllMods (its build/allmods; the instance itself is only read, never changed). OreSpawn
# reads its textures, sounds, models and dungeons from the original OreSpawn 1.7.10 file, found through ORESPAWN_ORIGINAL
# (default: the zip in OreSpawnFabric/original). Exits 1 if any locked feature is broken in either run.
#
#   sh /d/Destinymods/OreSpawnFabric/tools/locked-features-with-orespawn.sh
cse=/d/Destinymods/WithersCombatSystemExtensionFabric
orespawn=$(ls /d/Destinymods/OreSpawnFabric/build/libs/OreSpawn-fabric-*.jar | grep -v sources | head -1)
[ -f "$orespawn" ] || { echo "no OreSpawn jar in build/libs: run ./run-gradle.sh build first"; exit 1; }
export ORESPAWN_ORIGINAL="${ORESPAWN_ORIGINAL:-$(ls /d/Destinymods/OreSpawnFabric/original/*.zip /d/Destinymods/OreSpawnFabric/original/*.jar 2>/dev/null | head -1)}"
[ -f "$ORESPAWN_ORIGINAL" ] || { echo "no original OreSpawn 1.7.10 file: put it in OreSpawnFabric/original or set ORESPAWN_ORIGINAL"; exit 1; }
cd "$cse" || exit 1
export CSE_TEST=LockedFeaturesTest
export ALSOFT_DRIVERS=null
mkdir -p build/locked-features
status=0

run() { # $1 = name, $2 = gradle property
	rm -rf build/run/clientGameTest "build/locked-features/$1"
	if ./run-gradle.sh --no-daemon --no-configuration-cache runClientGameTest "$2" > "build/locked-features/$1.log" 2>&1; then
		echo "locked features ($1): all checks passed"
	else
		echo "LOCKED FEATURES BROKEN ($1): see $cse/build/locked-features/$1.log"
		grep -a "LOCKED FEATURE BROKEN\|AssertionError" "build/locked-features/$1.log" | head -10
		status=1
	fi
	grep -a "CSE-TEST" "build/locked-features/$1.log" | sed 's/.*CSE-TEST /  /'
	[ -d build/run/clientGameTest/screenshots ] && cp -r build/run/clientGameTest/screenshots "build/locked-features/$1"
}

echo "OreSpawn jar: $orespawn (original: $ORESPAWN_ORIGINAL)"
run pack -PwithPack
rm -rf build/allmods
./run-gradle.sh --no-daemon --no-configuration-cache copyAllMods -PallMods > build/locked-features/copy.log 2>&1 || { echo "couldn't copy AllMods"; exit 1; }
cp "$orespawn" build/allmods/
ls build/allmods | grep OreSpawn
run allmods-orespawn -PallMods
exit $status
