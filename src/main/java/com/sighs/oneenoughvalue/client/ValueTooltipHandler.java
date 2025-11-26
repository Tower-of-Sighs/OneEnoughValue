package com.sighs.oneenoughvalue.client;

import com.sighs.oneenoughvalue.OneEnoughValue;
import com.sighs.oneenoughvalue.manager.ItemValueManager;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OneEnoughValue.MODID, value = Dist.CLIENT)
public class ValueTooltipHandler {
    @SubscribeEvent
    public static void onValueTooltip(final ItemTooltipEvent event) {
        if (!event.getFlags().isAdvanced()) return;
        int itemValue = ItemValueManager.instance.getValue(event.getItemStack());
        if (itemValue <= 0) return;
        event.getToolTip().add(Component.translatable("tooltip.oev.value",itemValue));
    }
}
