package cc.sighs.oneenoughvalue.server.ingredient;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import cc.sighs.oneenoughvalue.manager.ItemValueManager;
import cc.sighs.oneenoughvalue.server.ingredient.api.ISpecialIngredientValueHandler;
import cc.sighs.oneenoughvalue.server.recipe.RecipeHandlerManager;
import cc.sighs.oneenoughvalue.server.recipe.api.RecipeNode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class IngredientManager {
    public static IngredientManager INSTANCE = new IngredientManager();
    public Multimap<Ingredient, Item> ingredient2Item = HashMultimap.create();
    public Multimap<Item, Ingredient> item2Ingredient = HashMultimap.create();
    public Map<Ingredient, Integer> ingredientMinValuesCache = new HashMap<>();
    public Map<Class<? extends Ingredient>, ISpecialIngredientValueHandler> specialIngredientValueHandlers = new HashMap<>();

    public void clearCache() {
        ingredient2Item.clear();
        item2Ingredient.clear();
        ingredientMinValuesCache.clear();
        specialIngredientValueHandlers.clear();
    }

    /**
     * 生成双向映射<br>
     * 可以使用Item获取到被影响的Ingredient
     */
    public void init() {
        for (RecipeNode node : RecipeHandlerManager.INSTANCE.allNodes) {
            for (Ingredient ingredient : node.ingredients) {
                if (!ingredient2Item.containsKey(ingredient)) {
                    Arrays.stream(ingredient.getItems()).map(ItemStack::getItem).forEach(item -> {
                        ingredient2Item.put(ingredient, item);
                        item2Ingredient.put(item, ingredient);
                    });
                }
            }
        }
    }

    /**
     * 在缓存中获取值,如果不存在则生成一个值
     */
    public int getMinValueInCache(Ingredient ingredient) {
        if (ingredientMinValuesCache.containsKey(ingredient)) {
            return ingredientMinValuesCache.get(ingredient);
        }
        int value = getMinValue(ingredient);
        ingredientMinValuesCache.put(ingredient, value);
        return value;
    }

    /**
     * 获取值最低的值
     */
    public int getMinValue(Ingredient ingredient) {
        if (specialIngredientValueHandlers.containsKey(ingredient.getClass())) {
            return specialIngredientValueHandlers.get(ingredient.getClass()).getValue(ingredient);
        }
        int value = Integer.MAX_VALUE;
        //使用缓存映射,避免ItemStack的无效创建
        for (Item item : ingredient2Item.get(ingredient)) {
            int buffer = ItemValueManager.instance.getBaseValue(item);
            if (buffer == -1) {
                buffer = ItemValueManager.instance.recipesGenValue.getOrDefault(BuiltInRegistries.ITEM.getKey(item), Integer.MAX_VALUE);
            }
            value = Math.min(value, buffer);
        }
        if (value == Integer.MAX_VALUE) return -1;
        return value;
    }

    /**
     * 判断是否为空<br>
     * 即生成配方是否跳过此物品
     */
    public boolean isEmpty(Ingredient ingredient) {
        return ingredient.isEmpty() || ingredient.getItems().length == 0;
    }

    /**
     * 判断是否有固定值<br>
     * 即不会因为配方生成而改变价值的Ingredient
     */
    public boolean isStaticValue(Ingredient ingredient) {
        return Arrays.stream(ingredient.getItems())
                .map(ItemStack::getItem)
                .map(BuiltInRegistries.ITEM::getKey)
                .allMatch(ItemValueManager.instance.baseValueMap::containsKey);
    }

    public int getStaticValue(Ingredient ingredient) {
        int value = Integer.MAX_VALUE;
        for (ItemStack item : ingredient.getItems()) {
            value = Math.min(value, ItemValueManager.instance.getBaseValue(item));
        }
        return value;
    }

    /**
     * 设置物品是被改变了的<br>
     * 如果导致Ingredient的值降低<br>
     * 则将以这个Ingredient的价值作为输入的已处理配方重新加入队列
     */
    public void setItemStackValueChange(Item item) {
        for (Ingredient ingredient : item2Ingredient.get(item)) {
            //如果缓存的值大于新值,则重设缓存
            if (getMinValueInCache(ingredient) == -1 || getMinValueInCache(ingredient) > getMinValue(ingredient)) {
                ingredientMinValuesCache.put(ingredient, getMinValue(ingredient));

                for (RecipeNode node : RecipeHandlerManager.INSTANCE.allNodes) {
                    if (node.hasIngredient(ingredient) && node.isHandled) {
                        RecipeHandlerManager.INSTANCE.nodes.offer(node);
                        node.isHandled = false;
                    }
                }
            }
        }
    }

}
