package com.sighs.oneenoughvalue.kubejs.events;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface OEVEvents {
    EventGroup GROUP = EventGroup.of("OEVEvents");
    EventHandler ADD_VALUE = GROUP.server("addItemValue",() -> OEVInitValueEvents.class);
}
