package com.encanis.tfcmountains.mixin;

import com.encanis.tfcmountains.TFCMountainsConfig;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.world.biome.BiomeNoise;

@Mixin(value = BiomeNoise.class, remap = false)
public abstract class BiomeNoiseMountainsMixin {

    private static final double SEA_LEVEL_Y = 96.0;

    @Inject(
        method = "mountains(JII)Lnet/dries007/tfc/world/noise/Noise2D;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void tfcmountains$replaceMountains(
            long seed,
            int baseHeight,
            int scaleHeight,
            CallbackInfoReturnable<Noise2D> cir) {

        final double HEIGHT_SCALE = TFCMountainsConfig.HEIGHT_SCALE.get();
        final double HORIZONTAL_SCALE = TFCMountainsConfig.HORIZONTAL_SCALE.get();

        final float baseSpread  = (float) (0.14f  / HORIZONTAL_SCALE);
        final float ridgeSpread = (float) (0.02f  / HORIZONTAL_SCALE);
        final float cliffSpread = (float) (0.01f  / HORIZONTAL_SCALE);
        final Noise2D baseNoise = new OpenSimplex2D(seed) // A simplex noise forms the majority of the base
            .octaves(6)// High octaves to create highly fractal terrain
            .spread(baseSpread) // Use config value (horizontal) x 0.14
            .add(new OpenSimplex2D(seed + 1) // Ridge noise is added to mimic real mountain ridges. It is scaled smaller than the base noise to not be overpowering
                .octaves(4)
                .spread(ridgeSpread) // Use config value (horizontal) x0.02
                .scaled(-0.7f, 0.7f)
                .ridged() // Ridges are applied after octaves as it creates less directional artifacts this way
            )
            .map(x -> {
                final double x0 = 0.125f * (x + 1) * (x + 1) * (x + 1); // Power scaled, flattens most areas but maximizes peaks
                final double vanillaHeight = SEA_LEVEL_Y + baseHeight + scaleHeight * x0; // Get what vanilla tfc height would have been
                if (vanillaHeight > SEA_LEVEL_Y) {
                    return SEA_LEVEL_Y + (vanillaHeight - SEA_LEVEL_Y) * HEIGHT_SCALE; // Multiply everything above sea level by config value
                }
                return vanillaHeight;
            });

        // Cliff noise consists of noise that's been artificially clamped over half the domain, which is then selectively added above a base height level
        // This matches up with the distinction between dirt and stone
        final Noise2D cliffNoise = new OpenSimplex2D(seed + 2)
            .octaves(2)
            .spread(cliffSpread) // Use config value (horizontal) x 0.01
            .scaled(-25, 25)
            .map(x -> x > 0 ? x : 0);

        final double cliffCenter = SEA_LEVEL_Y + (140.0 - SEA_LEVEL_Y) * HEIGHT_SCALE; // Determine center of cliff zone, multiply by config value (cant use 140 anymore)
        final Noise2D cliffHeightNoise = new OpenSimplex2D(seed + 3)
            .octaves(2)
            .spread(cliffSpread) // Use config value (horizontal) x 0.01
            .scaled(cliffCenter - 20, cliffCenter + 20); // New center + or -20 blocks

        final double cliffTrigger = SEA_LEVEL_Y + (120.0 - SEA_LEVEL_Y) * HEIGHT_SCALE; // Determine new value for hardcoded 120 based on th config

        cir.setReturnValue((x, z) -> {
            double height = baseNoise.noise(x, z);
            if (height > cliffTrigger) { // If height is more than new config based value
                final double cliffHeight = cliffHeightNoise.noise(x, z) - height;
                if (cliffHeight < 0) {
                    final double mappedCliffHeight = Mth.clampedMap(cliffHeight, 0, -1, 0, 1);
                    height += mappedCliffHeight * cliffNoise.noise(x, z);
                }
            }
            return height;
        });
    }
}