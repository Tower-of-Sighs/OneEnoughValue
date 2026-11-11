package com.sighs.oneenoughvalue.kubejs.events;

import com.sighs.oneenoughvalue.manager.ItemValueManager;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

/**
 * 额外价值的注册入口，作用域为 startup：脚本放在 kubejs/startup_scripts 下。
 * <p>
 * startup 脚本在客户端与服务端都会执行，因此额外价值表天然双端一致，不再走网络同步。
 */
public class OEVInitExtraValueEventJS extends EventJS {
    public ItemValueManager valueManager;

    public OEVInitExtraValueEventJS(ItemValueManager manager) {
        valueManager = manager;
    }

    @Info("添加额外价值，id 相同的会覆盖；function 接收 ItemStack 并返回额外价值，返回 0 表示不匹配")
    public void addExtraValue(String id, Function<ItemStack, Number> function) {
        valueManager.registerExtraValue(id, function);
    }

    public ItemValueManager getValueManager() {
        return valueManager;
    }
}
