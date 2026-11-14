package com.sighs.oneenoughvalue.server.recipe.api;

import net.minecraft.core.Holder;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 获取输入。26.x 的配方不再统一暴露 getIngredients，输入改为从
 * {@link RecipeDisplay} 的 ingredients 槽位（{@link SlotDisplay}）反推。
 * <p>
 * 每个 SlotDisplay 解析成一组成员，包成 {@link Ingredient}；无法解析的槽位跳过。
 */
@FunctionalInterface
public interface IRecipeInputGetter {
    IRecipeInputGetter DEFAULT = (recipe, registryAccess) -> {
        List<RecipeDisplay> displays = recipe.display();
        if (displays.isEmpty()) {
            return List.of();
        }
        ContextMap context = new ContextMap.Builder()
                .withParameter(SlotDisplayContext.REGISTRIES, registryAccess)
                .create(SlotDisplayContext.CONTEXT);

        List<Ingredient> out = new ArrayList<>();
        for (SlotDisplay slot : ingredientSlots(displays.get(0))) {
            Ingredient ingredient = toIngredient(slot, context);
            if (ingredient != null) {
                out.add(ingredient);
            }
        }
        return out;
    };

    List<Ingredient> get(Recipe<?> recipe, RegistryAccess registryAccess);

    /** 从各类 RecipeDisplay 中取出 ingredients 槽位，取不到的返回空列表。 */
    private static List<SlotDisplay> ingredientSlots(RecipeDisplay display) {
        if (display instanceof net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay shaped) {
            return shaped.ingredients();
        }
        if (display instanceof net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay shapeless) {
            return shapeless.ingredients();
        }
        if (display instanceof net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay furnace) {
            return List.of(furnace.ingredient());
        }
        if (display instanceof net.minecraft.world.item.crafting.display.StonecutterRecipeDisplay stonecutter) {
            return List.of(stonecutter.input());
        }
        return List.of();
    }

    private static Ingredient toIngredient(SlotDisplay slot, ContextMap context) {
        List<ItemStack> stacks = slot.resolveForStacks(context);
        if (stacks.isEmpty()) {
            return null;
        }
        Set<Holder<Item>> items = new LinkedHashSet<>();
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                items.add(stack.typeHolder());
            }
        }
        if (items.isEmpty()) {
            return null;
        }
        return Ingredient.of(items.stream().map(Holder::value));
    }
}
