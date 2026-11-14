package com.sighs.oneenoughvalue.manager;

import com.sighs.oneenoughvalue.OneEnoughValue;
import com.sighs.oneenoughvalue.kubejs.events.OEVEvents;
import com.sighs.oneenoughvalue.kubejs.events.OEVInitExtraValueEventJS;
import com.sighs.oneenoughvalue.kubejs.events.OEVInitRecipeHandleEventJS;
import com.sighs.oneenoughvalue.kubejs.events.OEVInitValueEventJS;
import com.sighs.oneenoughvalue.server.BaseValueReloadListener;
import com.sighs.oneenoughvalue.server.recipe.RecipeHandlerManager;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class ItemValueManager {
    public static ItemValueManager instance = new ItemValueManager();
    public Map<ResourceLocation, Integer> recipesGenValue = new HashMap<>();
    public Map<ResourceLocation, Integer> baseValueMap = new HashMap<>();
    public Map<String, ExtraValue> extraValueMap = new LinkedHashMap<>();
    private boolean extraValueSealed = false;

    /**
     * 额外价值条目：function 接收 ItemStack 并返回该物品的额外价值（0 表示不匹配）。
     * <p>
     * 返回类型用 Number 而非 Integer：JS 的数字求值结果是 Double，
     * 声明成 Integer 会在泛型桥接处插入 checkcast 并抛 ClassCastException。
     * <p>
     * equals/hashCode 只由 id 决定：function 是 lambda，没有有意义的 equals，
     * 把它算进去会让同一条目在两个实例之间永远不相等。
     */
    public record ExtraValue(String id, Function<ItemStack, Number> function) {
        /**
         * 取该物品的额外价值，统一收敛成 int。函数返回 null 或非数字时按 0 处理。
         */
        public int get(ItemStack itemStack) {
            try {
                Number raw = function.apply(itemStack);
                return raw == null ? 0 : raw.intValue();
            } catch (ClassCastException e) {
                if (warnedNonNumber.compareAndSet(false, true)) {
                    OneEnoughValue.LOGGER.warn("额外价值 {} 的函数返回了非数字，已按 0 处理；"
                            + "该提示只出现一次", id);
                }
                return 0;
            }
        }

        private static final AtomicBoolean warnedNonNumber = new AtomicBoolean();

        @Override
        public boolean equals(Object o) {
            return o instanceof ExtraValue other && Objects.equals(id, other.id);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id);
        }
    }

    /**
     * 基础价值来自数据包，但标签必须等到 TagsUpdatedEvent 才绑定完成，
     * 所以数据包的解析结果在这里才真正注册（BaseValueReloadListener 只负责解析）。
     */
    public void onReload(RecipeManager recipeManager, RegistryAccess registryAccess) {
        applyDatapackValues();
        OEVEvents.ADD_VALUE.post(new OEVInitValueEventJS(this));

        OEVInitRecipeHandleEventJS.modifiers.clear();
        RecipeHandlerManager.INSTANCE.parseAllRecipes(recipeManager, registryAccess);

        Map<ResourceLocation, Integer> result = new HashMap<>();
        /*
        先放人配方生成价值，再加入基础价值，保证基础价值优先级高于配方生成
         */
        result.putAll(recipesGenValue);
        result.putAll(baseValueMap);
        baseValueMap = result;
        recipesGenValue.clear();
    }

    /**
     * 注册数据包来源的基础价值。必须在 TagsUpdatedEvent 阶段调用：
     * 资源重载时标签尚未绑定，#标签 会展开成空集合。
     */
    private void applyDatapackValues() {
        for (BaseValueReloadListener.Entry entry : BaseValueReloadListener.INSTANCE.getEntries()) {
            String key = entry.key();
            if (key.startsWith("#")) {
                registerValue(ItemTags.create(new ResourceLocation(key.substring(1))), entry.value());
            } else {
                registerValue(key, entry.value());
            }
        }
    }

    //如果放入了新值则返回true,用于检测配方是否完全处理
    public boolean computeRecipeValue(String type, ItemStack stack, int value) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        value /= stack.getCount();
        for (OEVInitRecipeHandleEventJS.IRecipeModify modifier : OEVInitRecipeHandleEventJS.modifiers) {
            value = modifier.apply(type, value);
        }
        Integer oldValue = recipesGenValue.get(key);
        if (oldValue == null) {
            recipesGenValue.put(key, value);
            return true;
        }
        if (value < 0) {
            recipesGenValue.put(key, 0);
            return true;
        }
        if (value < oldValue) {
            recipesGenValue.put(key, value);
            return true;
        }
        return false;
    }

    @Info("获取一个物品的价值（基础价值+额外价值）")
    public int getValue(ItemStack itemStack) {
        return getBaseValue(itemStack) + getExtraValue(itemStack);
    }

    @HideFromJS//js别看这个，笨牛会找不到方法
    public int getBaseValue(ItemStack itemStack) {
        return getBaseValue(itemStack.getItem());
    }

    @Info("获取一个物品的基础价值")
    public int getBaseValue(Item item) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        return baseValueMap.getOrDefault(key, -1);
    }

    @Info("获取一个物品的额外价值")
    public int getExtraValue(ItemStack itemStack) {
        int value = 0;
        for (ExtraValue entry : extraValueMap.values()) {
            value += entry.get(itemStack);
        }
        return value;
    }

    /**
     * 额外价值表在 FMLCommonSetupEvent 定型。startup 脚本早于该事件，因此这里不能清空，
     * 只做定型和诊断输出；定型之后 registerExtraValue 仍然可以动态追加。
     */
    public void buildExtraValueTable() {
        if (extraValueSealed) return;
        extraValueSealed = true;
        OneEnoughValue.LOGGER.info("已定型额外价值表，共 {} 条: {}", extraValueMap.size(), extraValueMap.keySet());
    }

    public boolean isExtraValueSealed() {
        return extraValueSealed;
    }

    @HideFromJS
    public void registerValue(ResourceLocation key, Integer value) {
        if (ForgeRegistries.ITEMS.containsKey(key)) {
            baseValueMap.put(key, value);
        }
    }

    @HideFromJS
    public void registerValue(String key, Integer value) {
        ResourceLocation location = new ResourceLocation(key);
        if (ForgeRegistries.ITEMS.containsKey(location)) {
            baseValueMap.put(location, value);
        }
    }

    public void registerValue(Item item, Integer value) {
        registerValue(ForgeRegistries.ITEMS.getKey(item), value);
    }

    public void registerValue(TagKey<Item> tag, Integer value) {
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
            registerValue(holder.get(), value);
        }
    }


    @Info("添加额外价值，function 接收 ItemStack 并返回额外价值（0 表示不匹配）；相同的 id 会覆盖")
    public void registerExtraValue(String id, Function<ItemStack, Number> function) {
        if (id == null || id.isBlank()) {
            OneEnoughValue.LOGGER.error("额外价值的 id 为空，已忽略");
            return;
        }
        if (function == null) {
            OneEnoughValue.LOGGER.error("额外价值 {} 的 function 为空，已忽略", id);
            return;
        }
        extraValueMap.put(id, new ExtraValue(id, function));
    }

    @Info("移除指定 id 的额外价值")
    public boolean removeExtraValue(String id) {
        return extraValueMap.remove(id) != null;
    }

    @Info("获取全部额外价值的 id")
    public java.util.Set<String> getExtraValueIds() {
        return java.util.Set.copyOf(extraValueMap.keySet());
    }
}
