package orespawn.client.content;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import orespawn.client.OreSpawnClient;
import orespawn.client.model.DungeonBeastModel;
import orespawn.client.model.EnderKnightModel;
import orespawn.client.model.EnderReaperModel;
import orespawn.client.model.FairyModel;
import orespawn.client.model.GhostModel;
import orespawn.client.model.GhostSkellyModel;
import orespawn.client.model.LeafMonsterModel;
import orespawn.client.model.TriffidModel;
import orespawn.content.Spooky;
import orespawn.entity.spooky.DungeonBeast;
import orespawn.entity.spooky.EnderKnight;
import orespawn.entity.spooky.Ghost;
import orespawn.entity.spooky.GhostSkelly;
import orespawn.entity.spooky.LeafMonster;
import orespawn.entity.spooky.Triffid;

/**
 * The Spooky group's renderers, as ClientProxyOreSpawn set them up: each model with its speed argument, OreSpawn's shadow
 * (its shadow argument times its scale, as RenderLiving got it) and scale, and the textures it picked.
 * <p>
 * state.flag: Ghost Pumpkin Skelly, the head spins this cycle; Fairy, the body is lit (blinking); Dungeon Beast, the jaws
 * snap this cycle; Triffid, the mouth is open. state.attacking: Ender Knight and Reaper screaming; Dungeon Beast, Leaf
 * Monster and Triffid attacking.
 */
@Environment(EnvType.CLIENT)
public final class SpookyClient {
	private static final Identifier[] FAIRIES = new Identifier[9];

	static {
		for (int i = 0; i < 9; i++) FAIRIES[i] = OreSpawnClient.tex(i == 0 ? "fairy.png" : "fairy" + (i + 1) + ".png");
	}

	private SpookyClient() {
	}

	public static void init() {
		ModelLayerLocation ghost = OreSpawnClient.layer("ghost", GhostModel::createBodyLayer);
		EntityRendererRegistry.register(Spooky.GHOST, c -> new GhostRenderer<Ghost>(c, new GhostModel(c.bakeLayer(ghost)), 0.0F, 0.65F,
			(e, s) -> s.texture = OreSpawnClient.tex("ghost.png")));
		ModelLayerLocation skelly = OreSpawnClient.layer("ghost_pumpkin_skelly", GhostSkellyModel::createBodyLayer);
		EntityRendererRegistry.register(Spooky.GHOST_PUMPKIN_SKELLY, c -> new GhostRenderer<GhostSkelly>(c, new GhostSkellyModel(c.bakeLayer(skelly)), 0.0F,
			1.05F, (e, s) -> {
				s.texture = OreSpawnClient.tex("ghost_pumpkin_skelly.png");
				// ModelGhostSkelly rolled a 1 in 3 chance of a spin each time cos(age * 0.05) started a new cycle.
				int cycle = Mth.floor(s.ageInTicks * 0.05F / (Mth.TWO_PI));
				s.flag = roll(e.getId(), cycle, 3) == 1;
			}));
		OreSpawnClient.<EnderKnight>mob(Spooky.ENDER_KNIGHT, OreSpawnClient.layer("ender_knight", EnderKnightModel::createBodyLayer),
			root -> new EnderKnightModel(root, 0.21F), 0.3F, 1.0F, (e, s) -> {
				s.texture = OreSpawnClient.tex("ender_knight.png");
				s.attacking = e.isScreaming();
			}, null);
		OreSpawnClient.<EnderKnight>mob(Spooky.ENDER_REAPER, OreSpawnClient.layer("ender_reaper", EnderReaperModel::createBodyLayer),
			root -> new EnderReaperModel(root, 0.23F), 0.2F, 1.0F, (e, s) -> {
				s.texture = OreSpawnClient.tex("ender_reaper.png");
				s.attacking = e.isScreaming();
			}, null);
		ModelLayerLocation fairy = OreSpawnClient.layer("fairy", FairyModel::createBodyLayer);
		EntityRendererRegistry.register(Spooky.FAIRY, c -> new FairyRenderer(c, fairy, 0.1F * 0.35F, 0.35F, (e, s) -> {
			s.texture = FAIRIES[Math.floorMod(e.variant(), 9)];
			s.flag = e.blinking();
		}));
		OreSpawnClient.<DungeonBeast>mob(Spooky.DUNGEON_BEAST, OreSpawnClient.layer("dungeon_beast", DungeonBeastModel::createBodyLayer),
			root -> new DungeonBeastModel(root, 0.62F), 0.25F, 1.0F, (e, s) -> {
				s.texture = OreSpawnClient.tex("dungeon_beast.png");
				s.attacking = e.attacking();
				// ModelDungeonBeast rerolled its jaws whenever cos(age * 2 * 0.62) turned upwards: snapping while attacking,
				// otherwise 1 in 15.
				int cycle = Mth.floor((s.ageInTicks * 2.0F * 0.62F + Mth.HALF_PI) / (Mth.TWO_PI));
				s.flag = s.attacking || roll(e.getId(), cycle, 15) == 0;
			}, null);
		OreSpawnClient.<LeafMonster>mob(Spooky.LEAF_MONSTER, OreSpawnClient.layer("leaf_monster", LeafMonsterModel::createBodyLayer),
			LeafMonsterModel::new, 0.65F, 1.0F, (e, s) -> {
				s.texture = OreSpawnClient.tex("leaf_monster.png");
				s.attacking = e.attacking();
			}, null);
		OreSpawnClient.<Triffid>mob(Spooky.TRIFFID, OreSpawnClient.layer("triffid", TriffidModel::createBodyLayer),
			root -> new TriffidModel(root, 1.0F), 0.3F, 1.0F, (e, s) -> {
				s.texture = OreSpawnClient.tex("triffid.png");
				s.flag = e.open();
				s.attacking = e.attacking();
			}, null);
	}

	/** A steady random number in [0, n) for this mob and animation cycle, standing in for the per-mob rolls OreSpawn kept. */
	private static int roll(int id, int cycle, int n) {
		return Math.floorMod(Mth.murmurHash3Mixer(id * 31 + cycle), n);
	}
}
