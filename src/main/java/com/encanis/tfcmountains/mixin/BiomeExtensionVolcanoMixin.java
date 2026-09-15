package com.encanis.tfcmountains.mixin;

import com.encanis.tfcmountains.TFCMountainsConfig;
import net.dries007.tfc.world.biome.BiomeExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BiomeExtension.class, remap = false)
public abstract class BiomeExtensionVolcanoMixin
{
    @Inject(method = "getCenteredFeatureScaleHeight()I", at = @At("RETURN"), cancellable = true)
    private void tfcmountains$scaleVolcanoHeight(CallbackInfoReturnable<Integer> cir)
    {
        final BiomeExtension self = (BiomeExtension) (Object) this;
        if (!isVolcanoBiome(self)) return;

        final double scale = TFCMountainsConfig.VOLCANO_HEIGHT_SCALE.get();
        if (scale == 1.0) return;

        cir.setReturnValue((int) Math.round(cir.getReturnValue() * scale));
    }

    @Inject(method = "getCenteredFeatureBaseHeight()I", at = @At("RETURN"), cancellable = true)
    private void tfcmountains$scaleVolcanoBaseHeight(CallbackInfoReturnable<Integer> cir)
    {
        final BiomeExtension self = (BiomeExtension) (Object) this;
        if (!isVolcanoBiome(self)) return;

        final double scale = TFCMountainsConfig.VOLCANO_HEIGHT_SCALE.get();
        if (scale == 1.0) return;

        final int base = cir.getReturnValue();
        if (base > 0)
        {
            cir.setReturnValue((int) Math.round(base * scale));
        }
    }

    private static boolean isVolcanoBiome(BiomeExtension biome)
    {
        return biome.hasCinderCones()
                || biome.hasTuffRings()
                || biome.hasTuyas()
                || biome.hasStratovolcanoes()
                || biome.hasAtolls();
    }
}