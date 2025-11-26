package com.sighs.oneenoughvalue.kubejs;

import com.sighs.oneenoughvalue.kubejs.events.OEVEvents;
import com.sighs.oneenoughvalue.manager.ItemValueManager;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;

public class OEVPlugin extends KubeJSPlugin {
    @Override
    public void registerEvents() {
        OEVEvents.GROUP.register();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("OEV$ItemValueManager", ItemValueManager.instance);
    }
}
