package com.sighs.oneenoughvalue.server.recipe.api;

import net.minecraft.core.RegistryAccess;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取一个配方的输出物品。<br>
 * 26.x 起配方不再直接暴露 getResultItem，产物统一走 {@link RecipeDisplay#result()} 的
 * {@link SlotDisplay}，这里负责把它解析成 ItemStack。
 * <p>
 * 注意：{@code SlotDisplayContext.fromLevel(null)} 会 NPE（它内部无条件读 level.fuelValues()），
 * 所以这里用 registryAccess 自行构造上下文，FUEL_VALUES 是可选键，缺失不影响普通物品解析。
 */
@FunctionalInterface
public interface IRecipeOutputGetter {
    IRecipeOutputGetter DEFAULT = (recipe, registryAccess) -> {
        List<RecipeDisplay> displays = recipe.display();
        if (displays.isEmpty()) {
            return List.of();
        }
        ContextMap context = new ContextMap.Builder()
                .withParameter(SlotDisplayContext.REGISTRIES, registryAccess)
                .create(SlotDisplayContext.CONTEXT);

        SlotDisplay result = displays.get(0).result();
        List<ItemStack> stacks = result.resolveForStacks(context);
        List<ItemStack> out = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                out.add(stack);
            }
        }
        return out;
    };

    List<ItemStack> get(Recipe<?> recipe, RegistryAccess registryAccess);
}
