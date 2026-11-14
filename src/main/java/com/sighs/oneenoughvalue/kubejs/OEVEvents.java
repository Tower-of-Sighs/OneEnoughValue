package com.sighs.oneenoughvalue.kubejs;

import com.sighs.oneenoughvalue.kubejs.events.OEVInitExtraValueEventJS;
import com.sighs.oneenoughvalue.kubejs.events.OEVInitValueEventJS;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface OEVEvents {
    EventGroup GROUP = EventGroup.of("OEVEvents");

    EventHandler ADD_VALUE = GROUP.server("addItemValue", () -> OEVInitValueEventJS.class);

    EventHandler ADD_EXTRA_VALUE = GROUP.startup("addExtraItemValue", () -> OEVInitExtraValueEventJS.class);
}
