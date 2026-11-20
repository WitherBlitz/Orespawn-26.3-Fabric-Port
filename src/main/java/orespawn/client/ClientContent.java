package orespawn.client;

/**
 * Every content group's client setup (model layers and renderers), in order. Each group has a class in
 * orespawn.client.content with a {@code static void init()} using OreSpawnClient's helpers; groups add one line here.
 */
public final class ClientContent {
	private ClientContent() {
	}

	public static void init() {
		orespawn.client.content.DinosaursClient.init();
		orespawn.client.content.BugsClient.init();
		orespawn.client.content.SpookyClient.init();
		orespawn.client.content.SeaClient.init();
		orespawn.client.content.PortalsClient.init();
		orespawn.client.content.TerrorsClient.init();
		orespawn.client.content.ToolsClient.init();
		orespawn.client.content.FarmingClient.init();
		orespawn.client.content.OdditiesClient.init();
		orespawn.client.content.RobotsClient.init();
		orespawn.client.content.WeaponsClient.init();
		orespawn.client.content.CrystalClient.init();
		orespawn.client.content.BossesClient.init();
	}
}
