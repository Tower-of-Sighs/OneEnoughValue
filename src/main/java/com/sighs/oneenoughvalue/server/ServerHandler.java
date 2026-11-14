package com.sighs.oneenoughvalue.server;

import com.sighs.oneenoughvalue.OneEnoughValue;
import com.sighs.oneenoughvalue.manager.ItemValueManager;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

@EventBusSubscriber(modid = OneEnoughValue.MODID)
public class ServerHandler {
    public static RecipeManager RECIPE_MANAGER;

    /** 26.x 起改用 ServerDataLoad 子类，根事件的 getUpdateCause() 已废弃。 */
    @SubscribeEvent
    public static void onTagUpdate(TagsUpdatedEvent.ServerDataLoad event) {
        ItemValueManager.instance.onReload(RECIPE_MANAGER, event.getRegistries());
        RECIPE_MANAGER = null;
    }

    @SubscribeEvent
    public static void onReload(AddServerReloadListenersEvent event) {
        RECIPE_MANAGER = event.getServerResources().getRecipeManager();
        event.addListener(OneEnoughValue.id("base_values"), BaseValueReloadListener.INSTANCE);
    }
}
