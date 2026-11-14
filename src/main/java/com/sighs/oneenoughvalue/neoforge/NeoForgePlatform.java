package com.sighs.oneenoughvalue.neoforge;

import com.sighs.oneenoughvalue.api.OEVPlatform;
import com.sighs.oneenoughvalue.api.event.OEVRegisterRecipeHandlersEvent;
import com.sighs.oneenoughvalue.kubejs.OEVEvents;
import com.sighs.oneenoughvalue.kubejs.events.OEVInitExtraValueEventJS;
import com.sighs.oneenoughvalue.kubejs.events.OEVInitValueEventJS;
import com.sighs.oneenoughvalue.manager.ItemValueManager;
import com.sighs.oneenoughvalue.server.recipe.RecipeHandlerManager;
import net.neoforged.neoforge.common.NeoForge;

/**
 * 1.21.1 NeoForge 平台适配：把 common 的平台钩子接到事件总线与 KubeJS 脚本事件上。
 */
public class NeoForgePlatform implements OEVPlatform {
    @Override
    public void onValueInit() {
        OEVEvents.ADD_VALUE.post(new OEVInitValueEventJS(ItemValueManager.instance));
    }

    @Override
    public void onExtraValueInit() {
        OEVEvents.ADD_EXTRA_VALUE.post(new OEVInitExtraValueEventJS(ItemValueManager.instance));
    }

    @Override
    public void onRecipeHandlersRegister(Object manager) {
        NeoForge.EVENT_BUS.post(new OEVRegisterRecipeHandlersEvent(manager));
    }
}
