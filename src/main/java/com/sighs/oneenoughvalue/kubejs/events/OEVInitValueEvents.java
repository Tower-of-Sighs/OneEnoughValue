package com.sighs.oneenoughvalue.kubejs.events;

import com.sighs.oneenoughvalue.manager.ItemValueManager;
import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;

public class OEVInitValueEvents extends EventJS {
    public ItemValueManager valueManager;

    public OEVInitValueEvents(ItemValueManager manager) {
        valueManager = manager;
    }

    public void addBaseItemValue(Ingredient ingredient, int value) {
        for (ItemStack item : ingredient.getItems()) {
            valueManager.registerValue(item.getItem(), value);
        }
    }

    public void addExtraItemValue(CompoundTag tag, int value) {
        valueManager.registerExtraValue(tag, value);
    }

    public int getItemBaseValue(Item item) {
        return valueManager.baseValueMap.getOrDefault(ForgeRegistries.ITEMS.getKey(item), -1);
    }

    public ItemValueManager getValueManager() {
        return valueManager;
    }
}
