package net.bexla.agi.packets;

import net.bexla.agi.AGIMod;
import net.bexla.agi.content.ArmorLoadouts;
import net.bexla.agi.content.CuriosLoadouts;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LoadoutSelectC2SPacket(int loadout) implements CustomPacketPayload {

    public static final Type<LoadoutSelectC2SPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AGIMod.MODID,
                    "loadout_select"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LoadoutSelectC2SPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    LoadoutSelectC2SPacket::loadout,
                    LoadoutSelectC2SPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send(int loadout) {
        PacketDistributor.sendToServer(new LoadoutSelectC2SPacket(loadout));
    }

    static void handle(LoadoutSelectC2SPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (packet.loadout() < 0 || packet.loadout() >= 3) return;
            if (player.containerMenu != player.inventoryMenu) return;

            ArmorLoadouts.switchTo(player, packet.loadout());

            if (ModList.get().isLoaded("curios")) {
                CuriosLoadouts.switchTo(player, packet.loadout());
            }

            LoadoutStateS2CPacket.send(player, packet.loadout());
        });
    }
}