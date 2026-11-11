package com.sighs.oneenoughvalue.network;

import com.sighs.oneenoughvalue.OneEnoughValue;
import com.sighs.oneenoughvalue.manager.ItemValueManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DataSyncPack {
    public Map<ResourceLocation, Integer> baseValueMap = new HashMap<>();

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(baseValueMap.size());
        for (Map.Entry<ResourceLocation, Integer> entry : baseValueMap.entrySet()) {
            buffer.writeResourceLocation(entry.getKey());
            buffer.writeInt(entry.getValue());
        }
    }

    public static DataSyncPack fromBuffer(FriendlyByteBuf buffer) {
        DataSyncPack pack = new DataSyncPack();
        int size = buffer.readInt();
        pack.baseValueMap = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            pack.baseValueMap.put(buffer.readResourceLocation(), buffer.readInt());
        }
        return pack;
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        OneEnoughValue.LOGGER.debug("从服务器接收");
        OneEnoughValue.LOGGER.debug("└基础价值列表: {}",baseValueMap.size());
        if (ServerLifecycleHooks.getCurrentServer() == null) {
            ItemValueManager.instance.baseValueMap = baseValueMap;
        }
        contextSupplier.get().setPacketHandled(true);
    }
}
