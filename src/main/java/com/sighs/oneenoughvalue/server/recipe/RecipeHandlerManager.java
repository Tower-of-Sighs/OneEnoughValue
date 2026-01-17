package com.sighs.oneenoughvalue.server.recipe;

import com.sighs.oneenoughvalue.kubejs.events.OEVEvents;
import com.sighs.oneenoughvalue.kubejs.events.OEVInitRecipeHandleEventJS;
import com.sighs.oneenoughvalue.server.ingredient.IngredientManager;
import com.sighs.oneenoughvalue.server.recipe.api.RecipeHandler;
import com.sighs.oneenoughvalue.server.recipe.api.RecipeNode;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RecipeHandlerManager {
    public static RecipeHandlerManager INSTANCE = new RecipeHandlerManager();
    public Map<RecipeType<?>, RecipeHandler> handlers = new HashMap<>();
    public ArrayList<RecipeNode> allNodes = new ArrayList<>();
    public ArrayDeque<RecipeNode> nodes = new ArrayDeque<>();
    public RegistryAccess currentRegistryAccess;

    /**
     * 现在处理机制为<br>
     * 队列处理<br>
     * 如果生成的物品价值影响了其他的配方<br>
     * 则将其他配方再次加入队列
     */
    @SuppressWarnings("unchecked")
    public <C extends Container, T extends Recipe<C>> void parseAllRecipes(RecipeManager manager, RegistryAccess registryAccess) {
        currentRegistryAccess = registryAccess;
        init();
        for (Map.Entry<RecipeType<?>, RecipeHandler> entry : handlers.entrySet()) {
            for (Recipe<?> recipe : manager.getAllRecipesFor((RecipeType<T>) entry.getKey())) {
                var node = new RecipeNode(entry.getValue(), recipe);
                allNodes.add(node);
                nodes.offer(node);
            }
        }
        IngredientManager.INSTANCE.init();
        while (!nodes.isEmpty()) {
            nodes.pop().tryHandle();
        }
        clearCache();
    }

    public void registerRecipeHandler(RecipeType<?> type, RecipeHandler handler) {
        handlers.put(type, handler);
    }

    public void init() {
        registerRecipeHandler(RecipeType.CRAFTING, RecipeHandler.DEFAULT);
        registerRecipeHandler(RecipeType.SMELTING, RecipeHandler.DEFAULT);
        registerRecipeHandler(RecipeType.BLASTING, RecipeHandler.DEFAULT);
        registerRecipeHandler(RecipeType.SMOKING, RecipeHandler.DEFAULT);
        OEVEvents.ADD_RECIPE_HANDLER.post(new OEVInitRecipeHandleEventJS<>());
    }

    public void clearCache() {
        handlers.clear();
        allNodes.clear();
        IngredientManager.INSTANCE.clearCache();
    }
}