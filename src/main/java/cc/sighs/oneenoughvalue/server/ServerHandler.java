package cc.sighs.oneenoughvalue.server;

import cc.sighs.oneenoughvalue.OneEnoughValue;
import cc.sighs.oneenoughvalue.manager.ItemValueManager;
import cc.sighs.oneenoughvalue.network.DataSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@EventBusSubscriber(modid = OneEnoughValue.MODID)
public class ServerHandler {
    public static RecipeManager RECIPE_MANAGER;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTagUpdate(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) return;
        ItemValueManager.instance.onReload(RECIPE_MANAGER, event.getRegistryAccess());
        RECIPE_MANAGER = null;
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            PacketDistributor.sendToAllPlayers(getDataSyncPack());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        PacketDistributor.sendToPlayer((ServerPlayer) event.getEntity(), getDataSyncPack());
    }

    public static DataSyncPayload getDataSyncPack() {
        return new DataSyncPayload(ItemValueManager.instance.baseValueMap, ItemValueManager.instance.extraValueMap);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onReload(AddReloadListenerEvent event) {
        RECIPE_MANAGER = event.getServerResources().getRecipeManager();
    }

}
