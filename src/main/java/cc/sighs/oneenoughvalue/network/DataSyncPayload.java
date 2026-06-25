package cc.sighs.oneenoughvalue.network;

import cc.sighs.oneenoughvalue.OneEnoughValue;
import cc.sighs.oneenoughvalue.manager.ItemValueManager;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Map;

public record DataSyncPayload(Map<ResourceLocation, Integer> baseValueMap,
                              Map<DataComponentPredicate, Integer> extraValueMap) implements CustomPacketPayload {
    public static final Type<DataSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(OneEnoughValue.MODID, "data_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DataSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.map(
                            Object2IntOpenHashMap::new,
                            ResourceLocation.STREAM_CODEC,
                            ByteBufCodecs.INT
                    ),
                    DataSyncPayload::baseValueMap,
                    ByteBufCodecs.map(
                            Object2IntOpenHashMap::new,
                            DataComponentPredicate.STREAM_CODEC,
                            ByteBufCodecs.INT
                    ),
                    DataSyncPayload::extraValueMap,
                    DataSyncPayload::new
            );

    public static void handleClient(DataSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(()->{
            OneEnoughValue.LOGGER.debug("从服务器接收");
            OneEnoughValue.LOGGER.debug("├基础价值列表: {}",payload.baseValueMap.size());
            OneEnoughValue.LOGGER.debug("└额外价值列表: {}",payload.extraValueMap.size());
            if (ServerLifecycleHooks.getCurrentServer() == null) {
                ItemValueManager.instance.baseValueMap = payload.baseValueMap;
                ItemValueManager.instance.extraValueMap = payload.extraValueMap;
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
