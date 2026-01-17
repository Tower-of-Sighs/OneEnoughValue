package com.sighs.oneenoughvalue.server.recipe.api;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

/**
 * 用于设置配方生成的价值<br>
 * 如果你有多产出,则可能需要自行实现此接口
 */
@FunctionalInterface
public interface IRecipeValueSetter {
    IRecipeValueSetter DEFAULT = (recipe, stacks, totalValue, setter) -> {
        setter.set(recipe, stacks.get(0), totalValue);
    };

    void accept(Recipe<?> recipe, List<ItemStack> stacks, int totalValue, IRecipeGenValueSetter setter);
}
