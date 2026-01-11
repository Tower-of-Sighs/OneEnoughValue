package com.sighs.oneenoughvalue.server;

import com.sighs.oneenoughvalue.manager.ItemValueManager;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class ServerRecipeHandler {
    public static ServerRecipeHandler instance = new ServerRecipeHandler();
    public List<RecipeHandler<?, ?>> recipeHandlers = new ArrayList<>();
    public static RegistryAccess currentRegistryAccess;

    public ServerRecipeHandler() {
    }

    //默认的设置配方价值的函数
    public static BiPredicate<Recipe<?>,Number> defSetValue = (recipe, value) -> {
        ItemStack result = recipe.getResultItem(currentRegistryAccess);
        if (result.isEmpty()) return true;
        if (ItemValueManager.instance.baseValueMap.containsKey(ForgeRegistries.ITEMS.getKey(result.getItem())))
            return true;
        return !ItemValueManager.instance.computeRecipeValue(ForgeRegistries.RECIPE_TYPES.getKey(recipe.getType()).toString(), result, value.intValue() / result.getCount());
    };

    //返回是否完全处理（即未生成新的价值
    public static Predicate<Recipe<?>> defHandler = (recipe) -> {
        int value = 0;

        for (Ingredient ingredient : recipe.getIngredients()) {
            int ingredientMinValue = getMinIngredientValue(ingredient);
            if (ingredientMinValue == Integer.MAX_VALUE) {
                continue;
            } else if (ingredientMinValue == -1) {
                return true;
            }
            value += ingredientMinValue;
        }
        return defSetValue.test(recipe,value);
    };

    public void init() {
        recipeHandlers.clear();
        registerSimpleRecipeHandler(RecipeType.CRAFTING);
        registerSimpleRecipeHandler(RecipeType.SMELTING);
        registerSimpleRecipeHandler(RecipeType.BLASTING);
        registerSimpleRecipeHandler(RecipeType.SMOKING);
    }

    public <T extends Recipe<?>> Predicate<T> handleDefault() {
        return t -> defHandler.test(t);
    }

    public static int getMinIngredientValue(Ingredient ingredient) {
        ItemStack[] stacks = ingredient.getItems();
        if (stacks.length == 0) return Integer.MAX_VALUE;
        int ingredientMinValue = Integer.MAX_VALUE;
        for (ItemStack item : stacks) {

            var a = ItemValueManager.instance.getBaseValue(item);
            if (a == -1) {
                a = ItemValueManager.instance.recipesGenValue.getOrDefault(ForgeRegistries.ITEMS.getKey(item.getItem()), Integer.MAX_VALUE);
            }
            ingredientMinValue = Math.min(a, ingredientMinValue);
        }
        if (ingredientMinValue == Integer.MAX_VALUE) return -1;
        return ingredientMinValue;
    }

    @Info("注册简单的处理，即输出物品价值等于输入物品总和")
    public <C extends Container, T extends Recipe<C>> void registerSimpleRecipeHandler(RecipeType<T> recipeType) {
        registerRecipeHandler(recipeType, handleDefault());
    }

    public void registerRecipeHandler(RecipeHandler<?, ?> recipeHandler) {
        recipeHandlers.removeIf(recipeHandler1 -> recipeHandler1.type==recipeHandler.type);
        recipeHandlers.add(recipeHandler);
    }

    public <C extends Container, T extends Recipe<C>> void registerRecipeHandler(RecipeType<T> type, Predicate<T> handler) {
        registerRecipeHandler(new RecipeHandler<>(type, handler));
    }

    public void parse(RecipeManager recipeManager, RegistryAccess registryAccess) {
        currentRegistryAccess = registryAccess;
        boolean needHandle = true;
        while (needHandle) {
            needHandle = false;
            for (RecipeHandler<?, ?> recipeHandler : recipeHandlers) {
                //如果有未完全处理的配方就再次处理
                if (!recipeHandler.handle(recipeManager)) {
                    needHandle = true;
                }
            }
        }
    }

    public static class RecipeHandler<C extends Container, T extends Recipe<C>> {
        public RecipeType<T> type;
        public Predicate<T> handler;

        public RecipeHandler(RecipeType<T> type, Predicate<T> handler) {
            this.type = type;
            this.handler = handler;
        }

        //如果完全处理返回true
        public boolean handle(RecipeManager manager) {
            boolean result = true;
            for (T t : manager.getAllRecipesFor(type)) {
                if (!handler.test(t)) {
                    result = false;
                }
            }
            return result;
        }

    }
}
