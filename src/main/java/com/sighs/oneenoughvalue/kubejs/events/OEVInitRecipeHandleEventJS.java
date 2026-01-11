package com.sighs.oneenoughvalue.kubejs.events;

import com.sighs.oneenoughvalue.server.ServerRecipeHandler;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class OEVInitRecipeHandleEventJS<C extends Container, T extends Recipe<C>> extends EventJS {
    public ServerRecipeHandler recipeHandler;
    public static List<IRecipeModify> modifiers = new ArrayList<>();

    public OEVInitRecipeHandleEventJS(ServerRecipeHandler recipeHandler) {
        this.recipeHandler = recipeHandler;
    }

    @Info("添加默认的配方处理方式，即输出物品价值等于输入物品价值总和")
    public  void addSimpleRecipeHandler(RecipeType<T> recipeType) {
        recipeHandler.registerSimpleRecipeHandler(recipeType);
    }

    @Info("添加自定义的配方处理方法，返回true表示当前配方处理完全")
    public void addCustomRecipeHandler(RecipeType<T> recipeType, Predicate<T> handler) {
        recipeHandler.registerRecipeHandler(recipeType, handler);
    }

    public  void modifyRecipeGenValue(IRecipeModify modifier) {
        modifiers.add(modifier);
    }

    @Info("移除指定配方处理器")
    public void removeRecipeHandler(RecipeType<?> recipeType) {
        recipeHandler.recipeHandlers.removeIf(recipeHandler1 -> recipeHandler1.type.equals(recipeType));
    }

    @Info("获取全部配方处理器")
    public List<ServerRecipeHandler.RecipeHandler<?, ?>> getRecipeHandlers() {
        return recipeHandler.recipeHandlers;
    }

    public boolean defaultSetRecipeValue(Recipe<?> recipe, Number value) {
        return ServerRecipeHandler.defSetValue.test(recipe, value);
    }

    public int getMinIngredientValue(Ingredient ingredient) {
        return ServerRecipeHandler.getMinIngredientValue(ingredient);
    }

    public List<String> getAllRecipeTypeName(){
        return ForgeRegistries.RECIPE_TYPES.getKeys().stream().map(Object::toString).toList();
    }

    public List<RecipeType<?>> getAllRecipeType(){
        return ForgeRegistries.RECIPE_TYPES.getValues().stream().toList();
    }

    public int getMaxInteger(){
        return Integer.MAX_VALUE;
    }

    public ServerRecipeHandler getRecipeHandler() {
        return recipeHandler;
    }

    @FunctionalInterface
    public interface IRecipeModify{
        int apply(String type, int oldValue);
    }
}
