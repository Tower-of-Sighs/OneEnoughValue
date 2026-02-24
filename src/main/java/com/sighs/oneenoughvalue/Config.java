package com.sighs.oneenoughvalue;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.ConfigValue<Boolean> ADD_VALUE_TOOLTIP;

    static final ForgeConfigSpec SPEC;

    static {
        ADD_VALUE_TOOLTIP = BUILDER
                .comment("是否添加默认的价值tooltip")
                .comment("#default: true")
                .define("addValueTooltip", true);
        SPEC = BUILDER.build();
    }
}
