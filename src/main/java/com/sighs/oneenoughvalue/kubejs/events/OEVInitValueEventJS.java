package com.sighs.oneenoughvalue.kubejs.events;

import com.sighs.oneenoughvalue.manager.ItemValueManager;
import net.minecraft.core.registries.BuiltInRegistries;
import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.world.item.Item;
import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * 基础价值的脚本入口，作用域为 server（每轮 reload 触发一次）。
 * <p>
 * 1.21.1 的 KubeJS 2101 已移除 EventJS 基类，事件类改为普通对象。
 */
public class OEVInitValueEventJS implements KubeEvent {
    public ItemValueManager valueManager;

    public OEVInitValueEventJS(ItemValueManager manager) {
        valueManager = manager;
    }

    public void removeBaseValue(Item item) {
        valueManager.baseValueMap.remove(BuiltInRegistries.ITEM.getKey(item));
    }

    public void addBaseItemValue(Ingredient ingredient, int value) {
        for (ItemStack item : ingredient.getItems()) {
            valueManager.registerValue(item.getItem(), value);
        }
    }

    public int getItemBaseValue(Item item) {
        return valueManager.getBaseValue(item);
    }

    public ItemValueManager getValueManager() {
        return valueManager;
    }
}
