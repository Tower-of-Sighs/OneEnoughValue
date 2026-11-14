package com.sighs.oneenoughvalue.network;

import com.sighs.oneenoughvalue.OneEnoughValue;
import com.sighs.oneenoughvalue.manager.ItemValueManager;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Map;

/**
 * 基础价值同步包：服务端算好的 baseValueMap 推给客户端，
 * 客户端的 tooltip 才有值可显示。
 */
public record DataSyncPayload(Map<ResourceLocation, Integer> baseValueMap) implements CustomPacketPayload {
    public static final Type<DataSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OneEnoughValue.MODID, "data_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DataSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.map(Object2IntOpenHashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.INT),
                    DataSyncPayload::baseValueMap,
                    DataSyncPayload::new
            );

    public static void handleClient(DataSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            OneEnoughValue.LOGGER.debug("从服务器接收");
            OneEnoughValue.LOGGER.debug("└基础价值列表: {}", payload.baseValueMap.size());
            if (ServerLifecycleHooks.getCurrentServer() == null) {
                ItemValueManager.instance.baseValueMap = payload.baseValueMap;
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
