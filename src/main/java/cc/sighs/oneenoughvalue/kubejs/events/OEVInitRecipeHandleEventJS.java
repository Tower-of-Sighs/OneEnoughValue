package cc.sighs.oneenoughvalue.kubejs.events;

import cc.sighs.oneenoughvalue.server.ingredient.IngredientManager;
import cc.sighs.oneenoughvalue.server.ingredient.api.ISpecialIngredientValueHandler;
import cc.sighs.oneenoughvalue.server.recipe.RecipeHandlerManager;
import cc.sighs.oneenoughvalue.server.recipe.api.*;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.plugin.builtin.wrapper.IngredientWrapper;
import dev.latvian.mods.kubejs.plugin.builtin.wrapper.ItemWrapper;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class OEVInitRecipeHandleEventJS<C extends RecipeInput, T extends Recipe<C>> implements KubeEvent {
    public static List<IRecipeModify> modifiers = new ArrayList<>();

    public OEVInitRecipeHandleEventJS() {
    }

    public RecipeHandlerManager getRecipeHandlerManager() {
        return RecipeHandlerManager.INSTANCE;
    }

    public IngredientManager getIngredientManager() {
        return IngredientManager.INSTANCE;
    }

    //= = = = = = = = = = = = = = = = = = = = = = = = =
    //  配方处理器
    //= = = = = = = = = = = = = = = = = = = = = = = = =

    @Info("添加默认的配方处理方式，即输出物品价值等于输入物品价值总和")
    public void addSimpleRecipeHandler(RecipeType<T> recipeType) {
        RecipeHandlerManager.INSTANCE.registerRecipeHandler(recipeType, RecipeHandler.DEFAULT);
    }

    @Info("添加自定义的配方处理方法")
    public void addCustomRecipeHandler(RecipeType<T> recipeType, RecipeInputWrapperGetter inputGetter, RecipeOutputWrapperGetter outputGetter, IRecipeExtraValueGetter extraValueGetter, IRecipeValueSetter valueSetter) {
        RecipeHandlerManager.INSTANCE.registerRecipeHandler(recipeType, RecipeHandler.create(
                inputGetter, outputGetter, extraValueGetter, valueSetter
        ));
    }

    @Info("移除指定配方处理器")
    public void removeRecipeHandler(RecipeType<?> recipeType) {
        RecipeHandlerManager.INSTANCE.handlers.remove(recipeType);
    }

    @Info("获取全部配方处理器")
    public Map<RecipeType<?>, RecipeHandler> getRecipeHandlers() {
        return RecipeHandlerManager.INSTANCE.handlers;
    }


    @Info("对生成的配方值进行修改")
    public void modifyRecipeGenValue(IRecipeModify modifier) {
        modifiers.add(modifier);
    }

    //= = = = = = = = = = = = = = = = = = = = = = = = =
    //  特殊原料处理
    //= = = = = = = = = = = = = = = = = = = = = = = = =

    public void register(Class<? extends Ingredient> ingredientClass, ISpecialIngredientValueHandler specialHandler) {
        IngredientManager.INSTANCE.specialIngredientValueHandlers.put(ingredientClass, specialHandler);
    }

    public ISpecialIngredientValueHandler getSpecialIngredientHandler(Class<? extends Ingredient> ingredientClass) {
        return IngredientManager.INSTANCE.specialIngredientValueHandlers.get(ingredientClass);
    }

    public Set<Class<? extends Ingredient>> getHasHandlerIngredients() {
        return IngredientManager.INSTANCE.specialIngredientValueHandlers.keySet();
    }

    //= = = = = = = = = = = = = = = = = = = = = = = = =
    //  一些默认值
    //= = = = = = = = = = = = = = = = = = = = = = = = =

    public RecipeInputWrapperGetter getDefaultRecipeInputGetter() {
        return IRecipeInputGetter.DEFAULT::get;
    }

    public RecipeOutputWrapperGetter getDefaultRecipeOutputGetter() {
        return IRecipeOutputGetter.DEFAULT::get;
    }

    public IRecipeExtraValueGetter getDefaultRecipeExtraValueGetter() {
        return IRecipeExtraValueGetter.DEFAULT;
    }

    public IRecipeValueSetter getDefaultRecipeValueSetter() {
        return IRecipeValueSetter.DEFAULT;
    }

    //= = = = = = = = = = = = = = = = = = = = = = = = =
    //  配方处类型获取
    //= = = = = = = = = = = = = = = = = = = = = = = = =

    public List<String> getAllRecipeTypeName() {
        return BuiltInRegistries.RECIPE_TYPE.keySet().stream().map(Object::toString).toList();
    }

    public List<RecipeType<?>> getAllRecipeType() {
        return BuiltInRegistries.RECIPE_TYPE.stream().toList();
    }

    //= = = = = = = = = = = = = = = = = = = = = = = = =
    //  一些函数式接口
    //= = = = = = = = = = = = = = = = = = = = = = = = =

    @FunctionalInterface
    public interface IRecipeModify {
        int apply(String type, int oldValue);
    }

    /**
     * kjs的包装<br>
     * 因为kjs返回值会无视泛型，需要额外处理
     */

    @FunctionalInterface
    public interface RecipeInputWrapperGetter extends IRecipeInputGetter {
        List<Ingredient> kjs$get(Recipe<?> recipe);

        default List<Ingredient> get(Recipe<?> recipe) {
            //直接toList会输入kjs不可用的list
            return kjs$get(recipe).stream().map(IngredientWrapper::of).collect(Collectors.toList());
        }
    }

    @FunctionalInterface
    public interface RecipeOutputWrapperGetter extends IRecipeOutputGetter {
        List<ItemStack> kjs$get(Recipe<?> recipe, RegistryAccess registryAccess);

        default List<ItemStack> get(Recipe<?> recipe, RegistryAccess registryAccess) {
            return kjs$get(recipe, registryAccess).stream().map(ItemWrapper::of).collect(Collectors.toList());
        }
    }

}
