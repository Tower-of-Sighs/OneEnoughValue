package com.sighs.oneenoughvalue.server.recipe;

import com.sighs.oneenoughvalue.api.OEVLog;
import com.sighs.oneenoughvalue.server.ingredient.IngredientManager;
import com.sighs.oneenoughvalue.server.recipe.api.RecipeHandler;
import com.sighs.oneenoughvalue.server.recipe.api.RecipeNode;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class RecipeHandlerManager {
    public static RecipeHandlerManager INSTANCE = new RecipeHandlerManager();
    public Map<RecipeType<?>, RecipeHandler> handlers = new LinkedHashMap<>();
    public ArrayList<RecipeNode> allNodes = new ArrayList<>();
    /**
     * 按暂定代价升序处理(best-first),复现"从最便宜的推导路径开始带定标签"的顺序,
     * 使深链在第一次被取出时就拿到终值,避免 FIFO 下的反复重算。
     * 同一套松弛语义与同一个不动点,只改变处理顺序。
     */
    public PriorityQueue<RecipeNode> nodes = new PriorityQueue<>(Comparator.comparingInt(n -> n.tentativeCost));
    public RegistryAccess currentRegistryAccess;

    /**
     * 现在处理机制为<br>
     * 队列处理<br>
     * 如果生成的物品价值影响了其他的配方<br>
     * 则将其他配方再次加入队列
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void parseAllRecipes(RecipeManager manager, RegistryAccess registryAccess) {
        currentRegistryAccess = registryAccess;
        init();
        for (Map.Entry<RecipeType<?>, RecipeHandler> entry : handlers.entrySet()) {
            for (RecipeHolder<?> holder : recipesOf(manager, entry.getKey())) {
                var node = new RecipeNode(entry.getValue(), holder.value());
                if (node.resultItems.isEmpty()) {
                    continue;
                }
                allNodes.add(node);
            }
        }
        IngredientManager.INSTANCE.init();
        /*
        初始入队必须放在 IngredientManager.init() 之后:enqueue 会计算暂定代价,而暂定代价依赖
        ingredient2Item 反查表(由 init 建立)。若在 init 之前计算,getMinValue 面对空表会返回 -1
        并把假 -1 写入 ingredientMinValuesCache,导致构建期所有节点代价为 MAX 且缓存被污染。
         */
        for (RecipeNode node : allNodes) {
            enqueue(node);
        }
        /*
        如果用户脚本里的 modifier / 自定义 valueSetter / 特殊 ingredient 处理器破坏了价值的单调性,
        推导可能长时间不收敛。此时只告警并停止,保留已经算出的价值,绝不像旧熔断那样写入一个伪造值。
         */
        int stepBudget = Math.max(64, allNodes.size() * 10);
        int steps = 0;
        RecipeNode pending;
        while ((pending = nodes.poll()) != null) {
            //节点被重新入队后旧条目作废,跳过它以免重复处理
            if (!pending.queued) {
                continue;
            }
            pending.queued = false;
            if (++steps > stepBudget) {
                OEVLog.LOGGER.warn("配方价值推导达到步骤预算 {}({}),提前结束并保留已算出的价值;"
                        + "通常由非单调的 modifier/自定义 valueSetter/特殊 ingredient 处理器引起", stepBudget, steps);
                break;
            }
            pending.tryHandle();
        }
        clearCache();
    }

    /** 26.x 的 RecipeManager 只提供 getRecipes()，按类型过滤在本方法内完成。 */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static List<RecipeHolder<?>> recipesOf(RecipeManager manager, RecipeType<?> type) {
        List<RecipeHolder<?>> out = new java.util.ArrayList<>();
        for (RecipeHolder<?> holder : manager.getRecipes()) {
            if (holder.value().getType() == type) {
                out.add(holder);
            }
        }
        return out;
    }

    /**
     * 唯一入队入口:标记 queued 并同步暂定代价。暂定代价为 MAX 时表示输入尚不可核算,
     * 仍然入队(优先队列会把它排到最后),等它的输入有值后由 setItemStackValueChange 重新入队刷新。
     */
    public void enqueue(RecipeNode node) {
        if (node.queued) {
            return;
        }
        node.tentativeCost = node.computeTentativeCost();
        node.queued = true;
        nodes.offer(node);
    }

    public void registerRecipeHandler(RecipeType<?> type, RecipeHandler handler) {
        handlers.put(type, handler);
    }

    public void init() {
        registerRecipeHandler(RecipeType.CRAFTING, RecipeHandler.DEFAULT);
        registerRecipeHandler(RecipeType.SMELTING, RecipeHandler.DEFAULT);
        registerRecipeHandler(RecipeType.BLASTING, RecipeHandler.DEFAULT);
        registerRecipeHandler(RecipeType.SMOKING, RecipeHandler.DEFAULT);
        // 平台层注册：每轮 reload 都会重新触发，注册因此不随 clearCache 丢失
        com.sighs.oneenoughvalue.manager.ItemValueManager.platform.onRecipeHandlersRegister(this);
        com.sighs.oneenoughvalue.manager.ItemValueManager.platform.onRecipeHandlerInit();
    }

    public void clearCache() {
        handlers.clear();
        allNodes.clear();
        nodes.clear();
        IngredientManager.INSTANCE.clearCache();
    }
}