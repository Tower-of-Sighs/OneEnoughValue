package com.sighs.oneenoughvalue.kubejs;

import com.sighs.oneenoughvalue.manager.ItemValueManager;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;

/**
 * 1.21.1 的 KubeJS 插件：注册事件组与全局绑定。
 */
public class OEVPlugin implements KubeJSPlugin {
    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(OEVEvents.GROUP);
    }

    @Override
    public void registerBindings(BindingRegistry registry) {
        registry.add("OEV$ItemValueManager", ItemValueManager.instance);
        registry.add("OEV$ExtraValueHelper", new ExtraValueHelper());
    }
}
