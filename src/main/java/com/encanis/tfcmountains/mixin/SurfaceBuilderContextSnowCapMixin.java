package com.encanis.tfcmountains.mixin;

import com.encanis.tfcmountains.TFCMountainsConfig;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SurfaceBuilderContext.class, remap = false)
public abstract class SurfaceBuilderContextSnowCapMixin {
    @Unique
    private static final int DITHER_AMPLITUDE = 5;

    @Unique private int tfcmountains$columnSurfaceY = Integer.MIN_VALUE;
    @Unique private int tfcmountains$columnX        = Integer.MIN_VALUE;
    @Unique private int tfcmountains$columnZ        = Integer.MIN_VALUE;

    @Inject(
            method = "buildSurface(Lnet/dries007/tfc/world/biome/BiomeExtension;Lnet/dries007/tfc/world/biome/BiomeExtension;DZLnet/dries007/tfc/world/surface/builder/SurfaceBuilder;IIIDI)V",
            at = @At("HEAD"),
            remap = false
    )
    private void tfcmountains$captureSurfaceHeight(
            BiomeExtension biome, BiomeExtension originalBiome, double weight,
            boolean salty, SurfaceBuilder builder,
            int x, int surfaceHeight, int z, double slope, int preVolcanicHeight,
            CallbackInfo ci) {
        this.tfcmountains$columnX        = x;
        this.tfcmountains$columnZ        = z;
        this.tfcmountains$columnSurfaceY = surfaceHeight;
    }

    @Inject(
            method = "averageTemperature()F",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tfcmountains$snowCap(CallbackInfoReturnable<Float> cir) {
        final boolean snowCaps = TFCMountainsConfig.SNOW_CAPS.get();
        final boolean enforceSnowLine = TFCMountainsConfig.ENFORCE_SNOW_LINE.get();

        if (!snowCaps && !enforceSnowLine) {
            return;
        }

        final SurfaceBuilderContext self = (SurfaceBuilderContext) (Object) this;
        final BlockPos cursor = self.pos();
        final int currentX = cursor.getX();
        final int currentZ = cursor.getZ();

        if (tfcmountains$columnSurfaceY == Integer.MIN_VALUE
                || currentX != tfcmountains$columnX
                || currentZ != tfcmountains$columnZ) {
            return;
        }

        final int seaLevel = self.getSeaLevel();
        final int surfaceY = tfcmountains$columnSurfaceY;

        if (surfaceY <= seaLevel) {
            return;
        }

        long h = 0x534E4F57L
                ^ ((long) currentX * 1610612741L)
                ^ ((long) currentZ * 805306457L);
        h ^= (h >>> 33);
        h *= 0xff51afd7ed558ccdL;
        h ^= (h >>> 33);
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= (h >>> 33);

        final double hashDither = ((double) (h & 0x7FFFFFFFL) / (double) 0x7FFFFFFFL) * 2.0 - 1.0;
        final double snowLine = TFCMountainsConfig.SNOW_LINE_Y.get() + hashDither * DITHER_AMPLITUDE;

        if (surfaceY >= snowLine) {
            if (snowCaps) {
                cir.setReturnValue(-25.0f);
            }
        } else {
            if (enforceSnowLine) {
                cir.setReturnValue(15.0f);
            }
        }
    }
}