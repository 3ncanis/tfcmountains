package com.encanis.tfcmountains;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TFCMountainsConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue SNOW_CAPS;
    public static final ModConfigSpec.IntValue SNOW_LINE_Y;
    public static final ModConfigSpec.BooleanValue ENFORCE_SNOW_LINE;
    public static final ModConfigSpec.DoubleValue VOLCANO_HEIGHT_SCALE;
    public static final ModConfigSpec.IntValue FOOTHILL_WIDTH;
    public static final ModConfigSpec.IntValue HEIGHT_BLEND;
    public static final ModConfigSpec.BooleanValue EXTENDED_MOUNTAIN_RANGES;
    public static final ModConfigSpec.DoubleValue EXTENDED_MOUNTAIN_RANGES_MULTIPLIER;

    public static final ModConfigSpec.DoubleValue MOUNTAIN_HEIGHT_SCALE;
    public static final ModConfigSpec.DoubleValue MOUNTAIN_HORIZONTAL_SCALE;
    public static final ModConfigSpec.BooleanValue MOUNTAIN_EROSION_NOISE;
    public static final ModConfigSpec.DoubleValue MOUNTAIN_EROSION_NOISE_STRENGTH;
    public static final ModConfigSpec.DoubleValue MOUNTAIN_PEAK_SHARPNESS;
    public static final ModConfigSpec.DoubleValue MOUNTAIN_RIDGE_PEAK_INTENSITY;
    public static final ModConfigSpec.DoubleValue MOUNTAIN_MACRO_VARIATION;
    public static final ModConfigSpec.DoubleValue MOUNTAIN_MACRO_VARIATION_SCALE;
    public static final ModConfigSpec.DoubleValue MOUNTAIN_TEXTURE_AMPLITUDE;
    public static final ModConfigSpec.DoubleValue MOUNTAIN_TEXTURE_FREQUENCY;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        SNOW_CAPS = builder
                .comment("If true, all mountains will generate snow caps regardless of the surrounding climate.")
                .define("snowCaps", true);

        SNOW_LINE_Y = builder
                .comment("The Y level above which snow caps will form (if snowCaps is enabled).")
                .defineInRange("snowLineY", 230, 0, 1024);

        ENFORCE_SNOW_LINE = builder
                .comment("If true, TFC's natural climate-based snow will also be prevented below snowLineY. " +
                        "Keep disabled if you prefer vanilla TFC snow behaviour in colder climates.")
                .define("enforceSnowLine", true);

        VOLCANO_HEIGHT_SCALE = builder
                .comment("Vertical scale multiplier for all volcanoes. " +
                        "Vanilla TFC uses 1.")
                .defineInRange("volcanoHeightScale", 1.0, 0.1, 5.0);

        FOOTHILL_WIDTH = builder
                .comment("Controls how wide the transitional altitude bands (foothills) around mountains are. " +
                        "Vanilla TFC uses 4.")
                .defineInRange("foothillWidth", 8, 4, 8);

        HEIGHT_BLEND = builder
                .comment(
                        "How smoothly the terrain transitions from differing heights. " +
                        "Note that this applies to ALL biomes and will increase world gen times",
                        "Vanilla TFC uses 4."
                )
                .defineInRange("heightBlend", 16, 4, 32);

        EXTENDED_MOUNTAIN_RANGES = builder
                .comment(
                        "If true, individual mountain ranges are allowed to grow much longer before stopping"
                )
                .define("extendedMountainRanges", false);

        EXTENDED_MOUNTAIN_RANGES_MULTIPLIER = builder
                .comment(
                        "Only used if extendedMountainRanges is enabled. ",
                        "Scales how far a single mountain range is allowed to grow before stopping"
                )
                .defineInRange("extendedMountainRangesMultiplier", 3.0, 0.1, 20.0);

        MOUNTAIN_HEIGHT_SCALE = builder
                .comment("Vertical scale multiplier for these biomes' mountains.")
                .defineInRange("heightScale", 0.85, 0.1, 20.0);

        MOUNTAIN_HORIZONTAL_SCALE = builder
                .comment("Horizontal scale multiplier for these biomes' mountains.")
                .defineInRange("horizontalScale", 2.0, 0.1, 20.0);

        MOUNTAIN_EROSION_NOISE = builder
                .comment("If true, adds extra jagged/eroded detail noise to slopes and peaks.")
                .define("erosionNoise", true);

        MOUNTAIN_EROSION_NOISE_STRENGTH = builder
                .comment("Strength of the erosion detail noise (if erosionNoise is enabled). " +
                        "Note that increasing this also increases the overall height of the mountains.")
                .defineInRange("erosionNoiseStrength", 5.0, 0.0, 20.0);

        MOUNTAIN_PEAK_SHARPNESS = builder
                .comment("Adds sharp spires/pinnacles at the top of peaks, on top of " +
                        "whatever height erosionNoise already produces. 0 disables this entirely. " +
                        "Higher values add taller, narrower spikes near summits.")
                .defineInRange("peakSharpness", 1.0, 0.0, 8.0);

        MOUNTAIN_RIDGE_PEAK_INTENSITY = builder
                .comment("Multiplier for peaks of mountains.")
                .defineInRange("ridgePeakIntensity", 1.0, 0.0, 4.0);

        MOUNTAIN_MACRO_VARIATION = builder
                .comment("Adds large scale variation to overall peak height along a mountain range, so " +
                        "some massifs tower over their neighbors and others are noticeably lower, rather " +
                        "than a uniform ridgeline of similar-height peaks. 0 disables this.")
                .defineInRange("macroVariation", 1.5, 0.0, 5.0);

        MOUNTAIN_MACRO_VARIATION_SCALE = builder
                .comment("Controls how wide each tall/short stretch produced by macroVariation is. Lower " +
                        "values make each section wider and more spread out, higher values pack them closer together.")
                .defineInRange("macroVariationScale", 1.0, 0.1, 5.0);

        MOUNTAIN_TEXTURE_AMPLITUDE = builder
                .comment("Amplitude of TFC's fine-grained surface texture noise applied everywhere on these " +
                        "biomes. Distinct from erosionNoise (which only affects slopes/peaks).")
                .defineInRange("textureNoiseAmplitude", 50.0, 0.0, 100.0);

        MOUNTAIN_TEXTURE_FREQUENCY = builder
                .comment("Frequency of the surface texture noise. Lower values produce larger surface " +
                        "undulations, higher values produce finer, rockier texture.")
                .defineInRange("textureNoiseFrequency", 1.2, 0.05, 4.0);

        SPEC = builder.build();
    }
}