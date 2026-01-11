package com.sighs.oneenoughvalue;

import net.minecraftforge.common.ForgeConfigSpec;

//万一以后还写配置文件呢
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    //public static final ForgeConfigSpec.ConfigValue<Boolean> REPLACE_EXISTED_BLOCK;

    static final ForgeConfigSpec SPEC;

    static {
        SPEC = BUILDER.build();
    }
}
