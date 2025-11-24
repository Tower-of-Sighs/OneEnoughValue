package com.sighs.oneenoughvalue.network;

import com.google.gson.JsonParser;
import com.sighs.oneenoughvalue.manager.ItemValueManager;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DataSyncPack {
    public Map<ResourceLocation, Integer> baseValueMap = new HashMap<>();
    public Map<NbtPredicate, Integer> extraValueMap = new HashMap<>();

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(baseValueMap.size());
        for (Map.Entry<ResourceLocation, Integer> entry : baseValueMap.entrySet()) {
            buffer.writeResourceLocation(entry.getKey());
            buffer.writeInt(entry.getValue());
        }
        buffer.writeInt(extraValueMap.size());
        for (Map.Entry<NbtPredicate, Integer> entry : extraValueMap.entrySet()) {
            buffer.writeUtf(entry.getKey().serializeToJson().toString());
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
        size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            pack.extraValueMap.put(NbtPredicate.fromJson(JsonParser.parseString(buffer.readUtf())), buffer.readInt());
        }
        return pack;
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        if (ServerLifecycleHooks.getCurrentServer() == null) {
            ItemValueManager.instance.baseValueMap = baseValueMap;
            ItemValueManager.instance.extraValueMap = extraValueMap;
        }
        contextSupplier.get().setPacketHandled(true);
    }
}
