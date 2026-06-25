package cc.sighs.oneenoughvalue.server.ingredient.api;

import net.minecraft.world.item.crafting.Ingredient;

/**
 * 特殊的Ingredient获取<br>
 * 用于自定义一些getItem获取的物品奇奇怪怪的情况(比如屏障)
 */
@FunctionalInterface
public interface ISpecialIngredientValueHandler {
    int getValue(Ingredient ingredient);
}