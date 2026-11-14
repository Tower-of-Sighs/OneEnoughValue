package com.sighs.oneenoughvalue.api;

import net.minecraft.world.item.ItemStack;

/**
 * 加载器/脚本层与 common 之间的桥接点。<br>
 * common 不依赖 KubeJS 或具体加载器，由各 loader 模块在初始化时注入实现。
 */
public interface OEVPlatform {
    /** 无实现的空壳，保证 common 在没有任何注入时也能工作。 */
    OEVPlatform NOOP = new OEVPlatform() {
    };

    /**
     * 每轮价值重算开始时触发，供脚本层注册基础价值。
     */
    default void onValueInit() {
    }

    /** 每轮价值重算开始时触发，供脚本层清空并重新注册配方处理器。 */
    default void onRecipeHandlerInit() {
    }

    /** 默认处理器装入之后触发，供 Java 侧模组注册自己的处理器。 */
    default void onRecipeHandlersRegister(Object manager) {
    }

    /** 每轮价值重算开始时触发，供脚本层注册额外价值（额外价值会被清空后重建）。 */
    default void onExtraValueInit() {
    }

    /**
     * 对配方生成价值应用脚本层的修改器。
     *
     * @param type  配方类型 id
     * @param value 当前推导出的价值
     * @return 修改后的价值
     */
    default int modifyRecipeValue(String type, int value) {
        return value;
    }

    /** 额外价值被清空后重建前调用，供脚本层准备。 */
    default void beforeExtraValueRebuild() {
    }

    /** 供日志使用：由 loader 模块提供，common 不直接依赖日志实现。 */
    default void warn(String message, Object... args) {
    }

    default void info(String message, Object... args) {
    }

    default void error(String message, Object... args) {
    }

    default void error(String message, Throwable throwable) {
    }

    /** 便捷方法：判断一个 stack 是否命中某个 id 的额外价值（由 loader 层实现细节决定，默认不处理）。 */
    default int extraValueOf(ItemStack stack) {
        return 0;
    }
}
