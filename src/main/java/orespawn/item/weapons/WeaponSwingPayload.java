package orespawn.item.weapons;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import orespawn.OreSpawn;

/**
 * Sent by the client when its player starts a main-hand swing while holding a Bertha-class sword or the Chainsaw,
 * standing in for 1.7.10's Item.onEntitySwing (26.3 has no item hook for it).
 */
public record WeaponSwingPayload() implements CustomPacketPayload {
	public static final WeaponSwingPayload INSTANCE = new WeaponSwingPayload();
	public static final Type<WeaponSwingPayload> TYPE = new Type<>(OreSpawn.id("weapon_swing"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WeaponSwingPayload> CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	/** Server side: the held weapon's swing. */
	public static void handle(ServerPlayer player) {
		ItemStack stack = player.getMainHandItem();
		if (stack.getItem() instanceof BerthaItem bertha) bertha.swing(player, stack);
		else if (stack.getItem() instanceof ChainsawItem chainsaw) chainsaw.swing(player);
	}

	/** Whether swinging this item needs telling the server. */
	public static boolean wanted(ItemStack stack) {
		return stack.getItem() instanceof BerthaItem || stack.getItem() instanceof ChainsawItem;
	}
}
