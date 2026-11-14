package com.sighs.oneenoughvalue.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** common 的日志入口：不依赖任何加载器，直接走 SLF4J。 */
public final class OEVLog {
    public static final Logger LOGGER = LoggerFactory.getLogger("OneEnoughValue");

    private OEVLog() {
    }
}
