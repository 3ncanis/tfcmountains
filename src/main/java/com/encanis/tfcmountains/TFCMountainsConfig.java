package com.encanis.tfcmountains;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TFCMountainsConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue HEIGHT_SCALE;
    public static final ModConfigSpec.DoubleValue HORIZONTAL_SCALE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("TFC Mantle Mountains Configuration");

        HEIGHT_SCALE = builder
                .comment("Vertical scale multiplier for mountains. Default is 2.0 (2x taller than vanilla TFC).")
                .defineInRange("heightScale", 2.0, 0.1, 20.0);

        HORIZONTAL_SCALE = builder
                .comment("Horizontal scale multiplier for mountains. Default is 2.0. (2x wider than vanilla TFC).")
                .defineInRange("horizontalScale", 2.0, 0.1, 20.0);

        SPEC = builder.build();
    }
}