package cc.sighs.oneenoughvalue.server.recipe.api;

import cc.sighs.oneenoughvalue.manager.ItemValueManager;
import cc.sighs.oneenoughvalue.server.ingredient.IngredientManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

/**
 * 用于配方生成的输出价值的设置<br>
 * 一般来说你无需修改此处
 */
@FunctionalInterface
public interface IRecipeGenValueSetter {
    IRecipeGenValueSetter DEFAULT = (recipe, stack, var) -> {
        if (ItemValueManager.instance.computeRecipeValue(BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType()).toString(), stack, var)) {
            IngredientManager.INSTANCE.setItemStackValueChange(stack.getItem());
        }
    };

    void set(Recipe<?> recipe, ItemStack stack, int value);
}