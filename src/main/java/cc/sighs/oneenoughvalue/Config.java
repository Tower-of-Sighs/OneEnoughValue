package cc.sighs.oneenoughvalue;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.ConfigValue<Boolean> ADD_VALUE_TOOLTIP;

    static final ModConfigSpec SPEC;

    static {
        ADD_VALUE_TOOLTIP = BUILDER
                .comment("是否添加默认的价值tooltip")
                .comment("#default: true")
                .define("addValueTooltip", true);
        SPEC = BUILDER.build();
    }
}
