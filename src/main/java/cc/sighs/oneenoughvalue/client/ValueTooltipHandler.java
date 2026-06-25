package cc.sighs.oneenoughvalue.client;

import cc.sighs.oneenoughvalue.Config;
import cc.sighs.oneenoughvalue.OneEnoughValue;
import cc.sighs.oneenoughvalue.manager.ItemValueManager;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = OneEnoughValue.MODID, value = Dist.CLIENT)
public class ValueTooltipHandler {
    @SubscribeEvent
    public static void onValueTooltip(final ItemTooltipEvent event) {
        if (!Config.ADD_VALUE_TOOLTIP.get()) return;
        if (!event.getFlags().isAdvanced()) return;
        int itemValue = ItemValueManager.instance.getValue(event.getItemStack());
        if (itemValue <= 0) return;
        event.getToolTip().add(Component.translatable("tooltip.oev.value", itemValue));
    }
}
