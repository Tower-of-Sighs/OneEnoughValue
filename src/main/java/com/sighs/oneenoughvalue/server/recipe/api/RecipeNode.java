package com.sighs.oneenoughvalue.server.recipe.api;

import com.sighs.oneenoughvalue.server.ingredient.IngredientManager;
import com.sighs.oneenoughvalue.server.recipe.RecipeHandlerManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

public class RecipeNode {
    public List<Ingredient> ingredients = new ArrayList<>();
    public List<ItemStack> resultItems = new ArrayList<>();
    public RecipeHandler handler;
    public Recipe<?> recipe;
    public int staticValue;
    //这个,不知道留着做什么,反正先留着
    public int handleCount = 0;
    public int setValueCount = 0;
    public boolean isHandled = false;

    public RecipeNode(RecipeHandler handler, Recipe<?> recipe) {
        this.handler = handler;
        this.recipe = recipe;

        resultItems = this.handler.getOutputs(recipe, RecipeHandlerManager.INSTANCE.currentRegistryAccess);
        //如果不存在输出物品则不处理
        if (resultItems.isEmpty()) {
            isHandled = true;
            return;
        }
        staticValue = this.handler.getExtraValue(recipe);
        for (Ingredient ingredient : this.handler.getInputs(recipe)) {
            if (!IngredientManager.INSTANCE.isEmpty(ingredient)) {
                if (IngredientManager.INSTANCE.isStaticValue(ingredient)) {
                    staticValue += IngredientManager.INSTANCE.getStaticValue(ingredient);
                } else {
                    ingredients.add(ingredient);
                }
            }
        }
    }

    public void tryHandle() {
        if (isHandled) {
            return;
        }
        this.isHandled = true;
        handleCount++;
        int totalValue = staticValue;
        if (setValueCount < 10) {
            for (Ingredient ingredient : ingredients) {
                //如果有无价值物品,则取消输出物品价值生成
                if (IngredientManager.INSTANCE.getMinValueInCache(ingredient) < 0) {
                    return;
                }
                totalValue += IngredientManager.INSTANCE.getMinValueInCache(ingredient);
            }
        } else {
            //熔断
            totalValue = 0;
        }
        setValueCount++;
        this.handler.setValue(this.recipe, resultItems, totalValue);
    }

    public boolean hasIngredient(Ingredient ingredient) {
        return ingredients.contains(ingredient);
    }
}