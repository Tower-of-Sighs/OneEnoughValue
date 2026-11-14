package com.sighs.oneenoughvalue;

import com.mojang.logging.LogUtils;
import com.sighs.oneenoughvalue.manager.ItemValueManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(OneEnoughValue.MODID)
public class OneEnoughValue {
    public static final String MODID = "oneenoughvalue";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OneEnoughValue(IEventBus modEventBus, ModContainer modContainer) {
        // 把公共逻辑的平台钩子接到本加载器的实现
        ItemValueManager.platform = new com.sighs.oneenoughvalue.neoforge.NeoForgePlatform();

        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        com.sighs.oneenoughvalue.server.CommonHandler.register(modEventBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
