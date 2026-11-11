package com.sighs.oneenoughvalue.kubejs;

import com.sighs.oneenoughvalue.manager.ItemValueManager;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.world.item.ItemStack;

import java.util.Set;
import java.util.function.Function;

/**
 * 额外价值的运行时管理入口，通过全局 {@code OEV$ExtraValueHelper} 使用。
 * <p>
 * 与 startup 事件里的注册不同，这里的调用发生在游戏运行期间：不会重新同步、也不会重跑脚本。
 * 在多人游戏里服务端与客户端各自持有一份额外价值表，运行时从一侧增删会让两端不一致，
 * 因此这些方法标记为不稳定，仅供单人/调试或自行保证双端同步的场景使用。
 */
public class ExtraValueHelper {
    private static final String UNSTABLE = "【不稳定】运行时调用不会同步到另一端，多人游戏会导致双端不同步，请自行保证双端一致";

    public ExtraValueHelper() {
    }

    @Info(UNSTABLE + "。添加额外价值，id 相同的会覆盖；function 接收 ItemStack 并返回额外价值，返回 0 表示不匹配")
    public void add(String id, Function<ItemStack, Number> function) {
        ItemValueManager.instance.registerExtraValue(id, function);
    }

    @Info(UNSTABLE + "。移除指定 id 的额外价值，返回是否真的移除了")
    public boolean remove(String id) {
        return ItemValueManager.instance.removeExtraValue(id);
    }

    @Info(UNSTABLE + "。获取当前全部额外价值的 id，用于自查")
    public Set<String> getIds() {
        return ItemValueManager.instance.getExtraValueIds();
    }
}
