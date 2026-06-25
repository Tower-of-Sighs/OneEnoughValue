package cc.sighs.oneenoughvalue.kubejs.events;

import cc.sighs.oneenoughvalue.manager.ItemValueManager;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class OEVInitValueEventJS implements KubeEvent {
    public ItemValueManager valueManager;

    public OEVInitValueEventJS(ItemValueManager manager) {
        valueManager = manager;
    }

    @Info("移除指定物品的基础价值")
    public void removeBaseValue(Item item) {
        valueManager.baseValueMap.remove(BuiltInRegistries.ITEM.getKey(item));
    }

    @Info("为物品添加基础价值")
    public void addBaseItemValue(Ingredient ingredient, int value) {
        for (ItemStack item : ingredient.getItems()) {
            valueManager.registerValue(item.getItem(), value);
        }
    }

    @Info("添加NBT额外价值")
    public void addExtraItemValue(DataComponentMap tag, int value) {
        valueManager.registerExtraValue(tag, value);
    }

    @Info("获取物品基础价值(效果等同于使用:OEV$ItemValueManager.getBaseValue)")
    public int getItemBaseValue(Item item) {
        return valueManager.getBaseValue(item);
    }

    public ItemValueManager getValueManager() {
        return valueManager;
    }
}
