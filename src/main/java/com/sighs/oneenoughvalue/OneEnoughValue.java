package com.sighs.oneenoughvalue;

import com.mojang.logging.LogUtils;
import com.sighs.oneenoughvalue.manager.ItemValueManager;
import com.sighs.oneenoughvalue.network.DataSyncPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

@Mod(OneEnoughValue.MODID)
public class OneEnoughValue {
    public static final String MODID = "oneenoughvalue";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OneEnoughValue(IEventBus eventBus, ModContainer container) {
        // 把公共逻辑的平台钩子接到本加载器的实现
        ItemValueManager.platform = new com.sighs.oneenoughvalue.neoforge.NeoForgePlatform();

        // 注册到 mod 事件总线，RegisterPayloadHandlersEvent 才会被派发到本实例
        eventBus.register(this);
        container.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        com.sighs.oneenoughvalue.server.CommonHandler.register(eventBus);
    }

    @SubscribeEvent
    public void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(DataSyncPayload.TYPE, DataSyncPayload.STREAM_CODEC, DataSyncPayload::handleClient);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
