package com.sighs.oneenoughvalue.api.event;

import com.sighs.oneenoughvalue.server.recipe.RecipeHandlerManager;
import com.sighs.oneenoughvalue.server.recipe.api.RecipeHandler;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.Event;

/**
 * 供其它模组在 Java 侧注册配方价值处理器的 Forge 事件。<br>
 * 每一轮价值重算都会在处理器表清空之后 post 一次，因此在事件里注册即自动支持 /reload；
 * 不需要（也不应该）在模组构造期做一次性注册——那样的结果会在下一次 reload 被清掉。
 * <p>
 * 用法：
 * <pre>{@code
 * @SubscribeEvent
 * public static void onRegisterRecipeHandlers(OEVRegisterRecipeHandlersEvent event) {
 *     event.register(MyRecipeTypes.CUSTOM, RecipeHandler.simple(recipe -> 0));
 * }
 * }</pre>
 * 监听器挂在 {@code MinecraftForge.EVENT_BUS} 上。
 */
public class OEVRegisterRecipeHandlersEvent extends Event {
    private final RecipeHandlerManager manager;

    public OEVRegisterRecipeHandlersEvent(RecipeHandlerManager manager) {
        this.manager = manager;
    }

    /** 为某个配方类型注册处理器；同一类型重复注册会覆盖。 */
    public void register(RecipeType<?> type, RecipeHandler handler) {
        manager.registerRecipeHandler(type, handler);
    }

    /** 用默认的四段实现注册，等价于 {@code register(type, RecipeHandler.DEFAULT)}。 */
    public void registerSimple(RecipeType<?> type) {
        register(type, RecipeHandler.DEFAULT);
    }

    public boolean hasHandler(RecipeType<?> type) {
        return manager.handlers.containsKey(type);
    }
}
