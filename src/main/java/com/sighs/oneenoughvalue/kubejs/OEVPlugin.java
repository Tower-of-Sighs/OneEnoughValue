package com.sighs.oneenoughvalue.kubejs;

import com.sighs.oneenoughvalue.kubejs.events.OEVEvents;
import dev.latvian.mods.kubejs.KubeJSPlugin;

public class OEVPlugin extends KubeJSPlugin {
    @Override
    public void registerEvents() {
        OEVEvents.GROUP.register();
    }
}
