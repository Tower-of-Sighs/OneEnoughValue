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