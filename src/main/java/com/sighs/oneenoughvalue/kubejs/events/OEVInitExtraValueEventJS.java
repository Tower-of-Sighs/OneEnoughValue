package com.sighs.oneenoughvalue.kubejs.events;

import com.sighs.oneenoughvalue.manager.ItemValueManager;
import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

/**
 * 额外价值的注册入口，作用域为 startup。
 */
public class OEVInitExtraValueEventJS implements KubeEvent {
    public ItemValueManager valueManager;

    public OEVInitExtraValueEventJS(ItemValueManager manager) {
        valueManager = manager;
    }

    public void addExtraValue(String id, Function<ItemStack, Number> function) {
        valueManager.registerExtraValue(id, function);
    }

    public ItemValueManager getValueManager() {
        return valueManager;
    }
}
