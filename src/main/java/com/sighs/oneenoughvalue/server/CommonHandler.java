package com.sighs.oneenoughvalue.server;

import com.sighs.oneenoughvalue.OneEnoughValue;
import com.sighs.oneenoughvalue.manager.ItemValueManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

/**
 * 额外价值在 common setup 定型：先让 startup 脚本注册，再把表封闭。
 * <p>
 * 1.21.1 的 EventBusSubscriber.bus 已废弃，这里在 mod 构造期显式挂到 mod 事件总线上。
 */
public final class CommonHandler {
    private CommonHandler() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CommonHandler::onCommonSetup);
    }

    private static void onCommonSetup(net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ItemValueManager.platform.onExtraValueInit();
            ItemValueManager.instance.buildExtraValueTable();
        });
    }
}
