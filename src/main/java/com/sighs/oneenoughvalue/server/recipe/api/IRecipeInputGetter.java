package com.sighs.oneenoughvalue.server.recipe.api;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

/**
 * 获取输入<br>
 * 正常配方应该无需关系此处
 */
@FunctionalInterface
public interface IRecipeInputGetter {
    IRecipeInputGetter DEFAULT = Recipe::getIngredients;

    List<Ingredient> get(Recipe<?> recipe);
}
