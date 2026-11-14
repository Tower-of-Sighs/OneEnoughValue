package com.sighs.oneenoughvalue.kubejs;

import com.sighs.oneenoughvalue.manager.ItemValueManager;
import net.minecraft.world.item.ItemStack;

import java.util.Set;
import java.util.function.Function;

/**
 * 额外价值的运行时管理入口，通过全局 {@code OEV$ExtraValueHelper} 使用。
 * <p>
 * 运行时改动不会同步，多人游戏会导致双端不同步，需自行保证一致。
 */
public class ExtraValueHelper {
    public void add(String id, Function<ItemStack, Number> function) {
        ItemValueManager.instance.registerExtraValue(id, function);
    }

    public boolean remove(String id) {
        return ItemValueManager.instance.removeExtraValue(id);
    }

    public Set<String> getIds() {
        return ItemValueManager.instance.getExtraValueIds();
    }
}
