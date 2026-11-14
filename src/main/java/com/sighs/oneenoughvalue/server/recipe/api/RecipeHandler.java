package com.sighs.oneenoughvalue.server.recipe.api;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

public record RecipeHandler(IRecipeInputGetter inputGetter, IRecipeOutputGetter outputGetter,
                            IRecipeExtraValueGetter extraValueGetter, IRecipeValueSetter valueSetter) {
    public static RecipeHandler DEFAULT = create(IRecipeInputGetter.DEFAULT, IRecipeOutputGetter.DEFAULT, IRecipeExtraValueGetter.DEFAULT, IRecipeValueSetter.DEFAULT);

    public static RecipeHandler create(IRecipeInputGetter inputGetter, IRecipeOutputGetter outputGetter, IRecipeExtraValueGetter extraValueGetter, IRecipeValueSetter valueSetter) {
        return new RecipeHandler(inputGetter, outputGetter, extraValueGetter, valueSetter);
    }

    public static RecipeHandler create(IRecipeInputGetter inputGetter, IRecipeOutputGetter outputGetter, IRecipeValueSetter valueSetter) {
        return new RecipeHandler(inputGetter, outputGetter, IRecipeExtraValueGetter.DEFAULT, valueSetter);
    }

    public static RecipeHandler create(IRecipeOutputGetter outputGetter, IRecipeValueSetter valueSetter) {
        return new RecipeHandler(IRecipeInputGetter.DEFAULT, outputGetter, IRecipeExtraValueGetter.DEFAULT, valueSetter);
    }

    /**
     * 单输出的最常见情形：给出输入、输出与额外价值，价值按输入之和落库。
     * 其它模组的配方大多只需这一种，不必自己拼四个接口。
     */
    public static RecipeHandler simple(IRecipeInputGetter inputGetter, IRecipeOutputGetter outputGetter, IRecipeExtraValueGetter extraValueGetter) {
        return new RecipeHandler(inputGetter, outputGetter, extraValueGetter, IRecipeValueSetter.DEFAULT);
    }

    /** 输入输出都用默认实现，只给额外价值（例如按烧炼时间加成）。 */
    public static RecipeHandler simple(IRecipeExtraValueGetter extraValueGetter) {
        return new RecipeHandler(IRecipeInputGetter.DEFAULT, IRecipeOutputGetter.DEFAULT, extraValueGetter, IRecipeValueSetter.DEFAULT);
    }

    public List<Ingredient> getInputs(Recipe<?> recipe) {
        return inputGetter.get(recipe);
    }

    public List<ItemStack> getOutputs(Recipe<?> recipe, RegistryAccess registryAccess) {
        return outputGetter.get(recipe, registryAccess);
    }

    public int getExtraValue(Recipe<?> recipe) {
        return extraValueGetter.get(recipe);
    }

    public void setValue(Recipe<?> recipe, List<ItemStack> stacks, int totalValue) {
        valueSetter.accept(recipe, stacks, totalValue, IRecipeGenValueSetter.DEFAULT);
    }
}