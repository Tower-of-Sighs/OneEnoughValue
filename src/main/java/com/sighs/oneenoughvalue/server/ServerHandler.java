package com.sighs.oneenoughvalue.server;

import com.sighs.oneenoughvalue.OneEnoughValue;
import com.sighs.oneenoughvalue.manager.ItemValueManager;
import com.sighs.oneenoughvalue.network.DataSyncPack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = OneEnoughValue.MODID)
public class ServerHandler {
    public static RecipeManager RECIPE_MANAGER;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTagUpdate(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) return;
        ItemValueManager.instance.onReload(RECIPE_MANAGER, event.getRegistryAccess());
        RECIPE_MANAGER = null;
        if(ServerLifecycleHooks.getCurrentServer()!=null) {
            OneEnoughValue.NETWORK.send(PacketDistributor.ALL.noArg(),getDataSyncPack());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        OneEnoughValue.NETWORK.send(PacketDistributor.PLAYER.with(() -> (net.minecraft.server.level.ServerPlayer) event.getEntity()), getDataSyncPack());
    }

    public static DataSyncPack getDataSyncPack() {
        DataSyncPack pack = new DataSyncPack();
        pack.baseValueMap = ItemValueManager.instance.baseValueMap;
        pack.extraValueMap = ItemValueManager.instance.extraValueMap;
        return pack;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onReload(AddReloadListenerEvent event) {
        RECIPE_MANAGER = event.getServerResources().getRecipeManager();
    }

}
