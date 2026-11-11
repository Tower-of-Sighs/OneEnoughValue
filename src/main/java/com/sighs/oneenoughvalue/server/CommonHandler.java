package com.sighs.oneenoughvalue.server;

import com.sighs.oneenoughvalue.OneEnoughValue;
import com.sighs.oneenoughvalue.kubejs.events.OEVEvents;
import com.sighs.oneenoughvalue.kubejs.events.OEVInitExtraValueEventJS;
import com.sighs.oneenoughvalue.manager.ItemValueManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * 额外价值在 common setup 定型：先让 startup 脚本注册，再把表封闭。
 * startup 脚本在 mod 构造期就已加载，因此这里 post 时脚本已经就位。
 */
@Mod.EventBusSubscriber(modid = OneEnoughValue.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonHandler {
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        // 该事件并行派发，改动共享 Map 需要回到主线程
        event.enqueueWork(() -> {
            OEVEvents.ADD_EXTRA_VALUE.post(new OEVInitExtraValueEventJS(ItemValueManager.instance));
            ItemValueManager.instance.buildExtraValueTable();
        });
    }
}
