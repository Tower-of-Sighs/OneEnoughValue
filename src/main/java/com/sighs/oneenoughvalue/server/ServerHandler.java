package com.sighs.oneenoughvalue.server;

import com.sighs.oneenoughvalue.OneEnoughValue;
import com.sighs.oneenoughvalue.manager.ItemValueManager;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

@EventBusSubscriber(modid = OneEnoughValue.MODID)
public class ServerHandler {
    public static RecipeManager RECIPE_MANAGER;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTagUpdate(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) return;
        ItemValueManager.instance.onReload(RECIPE_MANAGER, event.getRegistryAccess());
        RECIPE_MANAGER = null;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onReload(AddReloadListenerEvent event) {
        RECIPE_MANAGER = event.getServerResources().getRecipeManager();
        event.addListener(BaseValueReloadListener.INSTANCE);
    }
}
