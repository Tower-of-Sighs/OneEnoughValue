package com.sighs.oneenoughvalue.server.recipe.api;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

/**
 * 获取一个配方的输出物品<br>
 * 如果有多产出<br>
 * 你可能需要自行实现此接口
 */
@FunctionalInterface
public interface IRecipeOutputGetter {
    IRecipeOutputGetter DEFAULT = (recipe, registryAccess) -> {
        ItemStack stack = recipe.getResultItem(registryAccess);
        if (stack.isEmpty()) {
            return List.of();
        }
        return List.of(stack);
    };

    List<ItemStack> get(Recipe<?> recipe, RegistryAccess registryAccess);
}