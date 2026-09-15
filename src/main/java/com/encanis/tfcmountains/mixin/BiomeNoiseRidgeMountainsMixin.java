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
public abstract class BiomeNoiseRidgeMountainsMixin {

    private static final double SEA_LEVEL_Y = net.dries007.tfc.world.TFCChunkGenerator.SEA_LEVEL_Y;

    @Inject(
            method = "ridgeMountains(JDDFII)Lnet/dries007/tfc/world/noise/Noise2D;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void tfcmountains$replaceRidgeMountains(
            long seed,
            double baseHeight,
            double scaleHeight,
            float spreadFactor,
            int cliffStartHeight,
            int cliffStartVariance,
            CallbackInfoReturnable<Noise2D> cir) {

        final double HEIGHT_SCALE = TFCMountainsConfig.MOUNTAIN_HEIGHT_SCALE.get();
        final double HORIZONTAL_SCALE = TFCMountainsConfig.MOUNTAIN_HORIZONTAL_SCALE.get();

        // Basic ridge shapes following zeroes in the noise
        final Noise2D ridges = new OpenSimplex2D(seed + 3987677L).octaves(4).spread(0.022f).map(y -> {
            return 1 - 2.8 * y * y; // We want to drag the valleys down to the base biome level, at which point flat valley noise takes over
        });

        // Continuous paths through ridges to make them more passable. Power-scaled to round them. Steepened so that they don't apply everywhere.
        final Noise2D passes = new OpenSimplex2D(seed + 454379L).octaves(2).spread(0.003f).map(y -> 16 * y * y);

        // We want passes to cut more deeply into terrain near ridges, and fade out in lower areas
        // This gives the height at the bottom of the pass, as a function of the height of the ridge
        final Noise2D passHeight = ridges.map(y -> Mth.clampedMap(y, 0.3, 0.9, 0.3, 0.5));

        final Noise2D carvedRidges = ridges.min(passes.add(passHeight));

        // Apply peaks to the tops of ridges
        final double ridgePeakIntensity = TFCMountainsConfig.MOUNTAIN_RIDGE_PEAK_INTENSITY.get();
        final OpenSimplex2D warp = new OpenSimplex2D(seed).octaves(3).spread(0.025f).scaled(-50f, 50f);
        final Noise2D peaks = new OpenSimplex2D(seed + 4242L).octaves(3).spread(0.045).scaled(-0.6, 1).warped(warp).easeIn(0.4, 0.8, 0.1, 1, carvedRidges).map(y -> y * ridgePeakIntensity);

        // Need a scale noise so peaks aren't all the same height
        // We ease it in over ridges before we scale it
        final Noise2D scale = new OpenSimplex2D(seed + 245L).octaves(4).spread(0.012).easeIn(0.3, 0.9, 0, 1, ridges).map(y -> 1 + 0.35 * y);

        // Bases of valleys cut off below a point
        final Noise2D flatValleys = BiomeNoise.hills(seed + 525L, (int) (baseHeight - 15), (int) (baseHeight + 15));

        // Add texture everywhere + apply config values
        final double textureAmp = TFCMountainsConfig.MOUNTAIN_TEXTURE_AMPLITUDE.get();
        final float textureFreq = TFCMountainsConfig.MOUNTAIN_TEXTURE_FREQUENCY.get().floatValue();
        final Noise2D textureNoise = new OpenSimplex2D(seed + 5).octaves(6).spread(textureFreq).scaled(-textureAmp, textureAmp);

        // Base shape of the terrain, scaled up to full size
        final Noise2D baseNoise = carvedRidges.add(peaks).lazyProduct(scale)
                .scaled(0, 1, SEA_LEVEL_Y + baseHeight, SEA_LEVEL_Y + baseHeight + scaleHeight)
                .max(flatValleys)
                .add(textureNoise)
                .map(y -> {
                    if (y > SEA_LEVEL_Y) {
                        return SEA_LEVEL_Y + (y - SEA_LEVEL_Y) * HEIGHT_SCALE;
                    }
                    return y;
                })
                .spread(spreadFactor / HORIZONTAL_SCALE);

        // Cliff noise consists of noise that's been artificially clamped over half the domain, which is then selectively added above a base height level
        // This matches up with the distinction between dirt and stone
        final Noise2D cliffNoise = new OpenSimplex2D(seed + 2)
                .octaves(2)
                .spread((float) (0.01f * spreadFactor / HORIZONTAL_SCALE))
                .scaled((float) (-25 * HEIGHT_SCALE), (float) (25 * HEIGHT_SCALE))
                .map(x -> x > 0 ? x : 0);

        final double cliffCenter = SEA_LEVEL_Y + (cliffStartHeight - SEA_LEVEL_Y) * HEIGHT_SCALE;
        final double scaledCliffStartVariance = cliffStartVariance * HEIGHT_SCALE;

        final Noise2D cliffHeightNoise = new OpenSimplex2D(seed + 3)
                .octaves(2)
                .spread((float) (0.01f * spreadFactor / HORIZONTAL_SCALE))
                .scaled(cliffCenter - scaledCliffStartVariance, cliffCenter + scaledCliffStartVariance);

        final boolean erosionEnabled = TFCMountainsConfig.MOUNTAIN_EROSION_NOISE.get();
        final double erosionStrength = TFCMountainsConfig.MOUNTAIN_EROSION_NOISE_STRENGTH.get();
        final double erosionAmplitude = 18 * HEIGHT_SCALE * erosionStrength;
        final double erosionFadeStart = SEA_LEVEL_Y + baseHeight * HEIGHT_SCALE;
        final double erosionFadeEnd = SEA_LEVEL_Y + (baseHeight + scaleHeight) * HEIGHT_SCALE;

        // High frequency jagged noise mimicking erosion
        final Noise2D erosionNoise = new OpenSimplex2D(seed + 778231L)
                .octaves(5)
                .spread((float) (0.05f * spreadFactor / HORIZONTAL_SCALE))
                .ridged()
                .scaled(-1, 1);

        // Sharp spires added to peaks
        final double peakSharpness = TFCMountainsConfig.MOUNTAIN_PEAK_SHARPNESS.get();
        final double spireAmplitude = 40 * HEIGHT_SCALE * (peakSharpness / 8.0);
        final double spireFadeStart = erosionFadeStart + 0.85 * (erosionFadeEnd - erosionFadeStart);
        final double spireExponent = 2 + peakSharpness;

        final Noise2D spireNoise = new OpenSimplex2D(seed + 991331L)
                .octaves(3)
                .spread((float) (0.09f * spreadFactor / HORIZONTAL_SCALE))
                .ridged()
                .scaled(0, 1)
                .map(y -> Math.pow(y, spireExponent));

        // Macro variation
        final double macroVariation = TFCMountainsConfig.MOUNTAIN_MACRO_VARIATION.get();
        final Noise2D macroNoise = new OpenSimplex2D(seed + 35117L)
                .octaves(2)
                .spread((float) (0.003f * TFCMountainsConfig.MOUNTAIN_MACRO_VARIATION_SCALE.get() / HORIZONTAL_SCALE))
                .scaled(-1, 1);

        cir.setReturnValue((x, z) -> {
            double height = baseNoise.noise(x, z);

            if (macroVariation > 0)
            {
                final double aboveBase = height - erosionFadeStart;
                if (aboveBase > 0)
                {
                    final double macroFactor = Math.max(0.15, 1 + macroVariation * macroNoise.noise(x, z));
                    height = erosionFadeStart + aboveBase * macroFactor;
                }
            }

            if (height > cliffCenter - scaledCliffStartVariance) // Only sample each cliff noise layer if the base noise could be influenced by it
            {
                final double cliffHeight = cliffHeightNoise.noise(x, z) - height;
                if (cliffHeight < 0)
                {
                    final double mappedCliffHeight = Mth.clampedMap(cliffHeight, 0, -1, 0, 1);
                    height += mappedCliffHeight * cliffNoise.noise(x, z);
                }
            }

            if (erosionEnabled && height > erosionFadeStart)
            {
                final double fade = Mth.clampedMap(height, erosionFadeStart, erosionFadeEnd, 0, 1);
                height += erosionNoise.noise(x, z) * erosionAmplitude * fade;
            }

            if (peakSharpness > 0 && height > spireFadeStart)
            {
                final double fade = Mth.clampedMap(height, spireFadeStart, erosionFadeEnd, 0, 1);
                height += spireNoise.noise(x, z) * spireAmplitude * fade;
            }

            return height;
        });
    }
}