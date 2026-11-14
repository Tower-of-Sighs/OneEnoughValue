package com.sighs.oneenoughvalue.forge;

import com.sighs.oneenoughvalue.api.OEVPlatform;
import com.sighs.oneenoughvalue.kubejs.events.OEVEvents;
import com.sighs.oneenoughvalue.kubejs.events.OEVInitExtraValueEventJS;
import com.sighs.oneenoughvalue.kubejs.events.OEVInitRecipeHandleEventJS;
import com.sighs.oneenoughvalue.kubejs.events.OEVInitValueEventJS;
import com.sighs.oneenoughvalue.manager.ItemValueManager;
import com.sighs.oneenoughvalue.server.recipe.RecipeHandlerManager;

/**
 * 1.20.1 Forge 平台适配：把 common 的平台钩子接到 Forge 事件总线与 KubeJS 脚本事件上。
 */
public class ForgePlatform implements OEVPlatform {
    @Override
    public void onValueInit() {
        OEVEvents.ADD_VALUE.post(new OEVInitValueEventJS(ItemValueManager.instance));
    }

    @Override
    public void onRecipeHandlerInit() {
        OEVInitRecipeHandleEventJS.modifiers.clear();
        OEVEvents.ADD_RECIPE_HANDLER.post(new OEVInitRecipeHandleEventJS<>());
    }

    @Override
    public void onRecipeHandlersRegister(Object manager) {
        // Java 侧模组注册：每轮 reload 都会重新 post，注册因此不随清空丢失
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                new com.sighs.oneenoughvalue.api.event.OEVRegisterRecipeHandlersEvent((RecipeHandlerManager) manager));
    }

    @Override
    public int modifyRecipeValue(String type, int value) {
        for (OEVInitRecipeHandleEventJS.IRecipeModify modifier : OEVInitRecipeHandleEventJS.modifiers) {
            value = modifier.apply(type, value);
        }
        return value;
    }
}
