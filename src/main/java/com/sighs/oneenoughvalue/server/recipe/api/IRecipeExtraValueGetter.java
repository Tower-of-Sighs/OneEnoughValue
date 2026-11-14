package com.sighs.oneenoughvalue.server.recipe.api;
import net.minecraft.world.item.crafting.Recipe;

/**
 * 额外价值设置<br>
 * 如果你需要熔炉那种根据烧炼时间修改价值,则你需要这个
 */
@FunctionalInterface
public interface IRecipeExtraValueGetter {
    IRecipeExtraValueGetter DEFAULT = recipe -> 0;

    int get(Recipe<?> recipe);
}
