package com.sighs.oneenoughvalue.server.recipe.api;

import com.sighs.oneenoughvalue.server.ingredient.IngredientManager;
import com.sighs.oneenoughvalue.server.recipe.RecipeHandlerManager;
import com.sighs.oneenoughvalue.manager.ItemValueManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.List;

public class RecipeNode {
    private static final int NO_SIGNATURE = Integer.MIN_VALUE;
    public List<Ingredient> ingredients = new ArrayList<>();
    public List<ItemStack> resultItems = new ArrayList<>();
    public RecipeHandler handler;
    public Recipe<?> recipe;
    public int staticValue;
    //这个,不知道留着做什么,反正先留着
    public int handleCount = 0;
    public int setValueCount = 0;
    public boolean isHandled = false;
    //上一次真正下发时使用的输入和,未下发过为 NO_SIGNATURE
    public int lastInputsValue = NO_SIGNATURE;
    //优先队列用的暂定代价(越小越先处理),以及是否已在队列中,避免重复入队
    public int tentativeCost = Integer.MAX_VALUE;
    public boolean queued = false;

    public RecipeNode(RecipeHandler handler, Recipe<?> recipe) {
        this.handler = handler;
        this.recipe = recipe;

        resultItems = this.handler.getOutputs(recipe, RecipeHandlerManager.INSTANCE.currentRegistryAccess);
        //如果不存在输出物品则不处理
        if (resultItems.isEmpty()) {
            isHandled = true;
            return;
        }
        staticValue = this.handler.getExtraValue(recipe);
        for (Ingredient ingredient : this.handler.getInputs(recipe, RecipeHandlerManager.INSTANCE.currentRegistryAccess)) {
            ItemStack[] stacks = ingredient.getValues().stream().map(net.minecraft.core.Holder::value).map(net.minecraft.world.item.ItemStack::new).toArray(net.minecraft.world.item.ItemStack[]::new);
            if (ingredient.isEmpty() || stacks.length == 0) {
                continue;
            }
            boolean allHasBaseValue = true;
            int minBaseValue = Integer.MAX_VALUE;
            for (ItemStack stack : stacks) {
                Item item = stack.getItem();
                if (!ItemValueManager.instance.baseValueMap.containsKey(BuiltInRegistries.ITEM.getKey(item))) {
                    allHasBaseValue = false;
                }
                minBaseValue = Math.min(minBaseValue, ItemValueManager.instance.getBaseValue(item));
            }
            if (allHasBaseValue) {
                staticValue += minBaseValue;
            } else {
                ingredients.add(ingredient);
            }
        }
    }

    public void tryHandle() {
        if (isHandled) {
            return;
        }
        this.isHandled = true;
        handleCount++;
        int inputsValue = 0;
        for (Ingredient ingredient : ingredients) {
            //如果有无价值物品,则取消输出物品价值生成,等它变化后重新入队
            int value = IngredientManager.INSTANCE.getMinValueInCache(ingredient);
            if (value < 0) {
                return;
            }
            inputsValue += value;
        }
        /*
        输入和未变则跳过:setValue 的全部入参由 (recipe, resultItems, staticValue + inputsValue) 决定,
        其中 recipe 与 resultItems 在构造期已固定、staticValue 亦固定,所以 inputsValue 相同必然得到
        相同的 totalValue、相同的 modifier 结果与相同的落库结论,本轮下发是纯空转。
        这是精确剪枝而非启发式,替代了原先的 setValueCount<10 熔断(totalValue=0)。
         */
        if (inputsValue == lastInputsValue) {
            return;
        }
        lastInputsValue = inputsValue;
        setValueCount++;
        this.handler.setValue(this.recipe, resultItems, staticValue + inputsValue);
    }

    /**
     * 暂定代价:staticValue + 输入 value 之和。任一无值输入时返回 MAX_VALUE,
     * 表示"暂时无法核算",应延后到队列末尾之外,等该输入有值后再入队。
     */
    public int computeTentativeCost() {
        int sum = staticValue;
        for (Ingredient ingredient : ingredients) {
            int value = IngredientManager.INSTANCE.getMinValueInCache(ingredient);
            if (value < 0) {
                return Integer.MAX_VALUE;
            }
            sum += value;
        }
        return sum;
    }

    public boolean hasIngredient(Ingredient ingredient) {
        return ingredients.contains(ingredient);
    }
}