package com.sighs.oneenoughvalue.server;

import com.sighs.oneenoughvalue.manager.ItemValueManager;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ServerRecipeHandler {
    public static ServerRecipeHandler instance = new ServerRecipeHandler();
    public List<RecipeHandler<?, ?>> recipeHandlers = new ArrayList<>();
    public static RegistryAccess currentRegistryAccess;

    public ServerRecipeHandler() {
        init();
    }

    public static Consumer<Recipe<?>> defHandler = (recipe) -> {
        int value = 0;
        for (Ingredient ingredient : recipe.getIngredients()) {
            int ingredientMinValue = getMinIngredientValue(ingredient);
            if (ingredientMinValue == Integer.MAX_VALUE) {
                continue;
            } else if (ingredientMinValue == -1) {
                return;
            }
            value += ingredientMinValue;
        }
        ItemStack result = recipe.getResultItem(currentRegistryAccess);
        if (result.isEmpty()) return;
        ItemValueManager.instance.computeRecipeValue(result, value);
    };

    public void init() {
        registerSimpleRecipeHandler(RecipeType.CRAFTING);
        registerSimpleRecipeHandler(RecipeType.SMELTING);
        registerSimpleRecipeHandler(RecipeType.SMOKING);
        registerSimpleRecipeHandler(RecipeType.BLASTING);
    }

    public <T extends Recipe<?>> Consumer<T> handleDefault(RecipeType<T> recipeType) {
        return t -> defHandler.accept(t);
    }

    public static int getMinIngredientValue(Ingredient ingredient) {
        int ingredientMinValue = Integer.MAX_VALUE;
        for (ItemStack item : ingredient.getItems()) {
            ingredientMinValue = Math.min(ItemValueManager.instance.getBaseValue(item), ingredientMinValue);
        }
        return ingredientMinValue;
    }

    @Info("注册简单的处理，即输出物品价值等于输入物品总和")
    public <C extends Container, T extends Recipe<C>> void registerSimpleRecipeHandler(RecipeType<T> recipeType) {
        registerRecipeHandler(recipeType, handleDefault(recipeType));
    }

    public void registerRecipeHandler(RecipeHandler<?, ?> recipeHandler) {
        recipeHandlers.add(recipeHandler);
    }

    public <C extends Container, T extends Recipe<C>> void registerRecipeHandler(RecipeType<T> type, Consumer<T> handler) {
        registerRecipeHandler(new RecipeHandler<>(type, handler));
    }

    public void parse(RecipeManager recipeManager, RegistryAccess registryAccess) {
        currentRegistryAccess = registryAccess;
        for (RecipeHandler<?, ?> recipeHandler : recipeHandlers) {
            recipeHandler.handle(recipeManager);
        }
    }

    public static class RecipeHandler<C extends Container, T extends Recipe<C>> {
        public RecipeType<T> type;
        public Consumer<T> handler;

        public RecipeHandler(RecipeType<T> type, Consumer<T> handler) {
            this.type = type;
            this.handler = handler;
        }

        public void handle(RecipeManager manager) {
            manager.getAllRecipesFor(type).forEach(handler);
        }

    }
}
