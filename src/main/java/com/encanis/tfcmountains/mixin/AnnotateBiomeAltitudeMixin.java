package com.encanis.tfcmountains.mixin;

import com.encanis.tfcmountains.TFCMountainsConfig;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import net.dries007.tfc.world.region.AnnotateBiomeAltitude;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;

@Mixin(value = AnnotateBiomeAltitude.class, remap = false)
public class AnnotateBiomeAltitudeMixin
{
    @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
    private void tfcmountains$widenFoothills(RegionGenerator.Context context, CallbackInfo ci)
    {
        final int width = TFCMountainsConfig.FOOTHILL_WIDTH.get();

        if (width == 4)
            return;

        ci.cancel();

        // Altitude is a more direct measure of what points map to what biomes
        // Mountains - 3
        // High - 2
        // Mid - 1
        // Low - 0
        // Near Island -1

        // Begin a basic BFS out from mountain blobs, stepping down

        final Region region = context.region;
        final RandomSource random = context.random;
        final BitSet explored = new BitSet(region.size());
        final IntArrayFIFOQueue queue = new IntArrayFIFOQueue();

        // Seed the BFS from every mountain point
        for (final Region.Point point : region.points())
        {
            if (point.land() && point.mountain())
            {
                point.biomeAltitude = (byte) (3 * width);
                queue.enqueue(point.index);
                explored.set(point.index);
            }
        }

        // BFS outward, stepping down by 1 each hop
        while (!queue.isEmpty())
        {
            final int last = queue.dequeueInt();
            final Region.Point lastPoint = region.atIndex(last);
            final int nextAltitude = lastPoint.biomeAltitude - 1;
            if (nextAltitude < 0)
                continue;

            for (int dx = -1; dx <= 1; dx++)
            {
                for (int dz = -1; dz <= 1; dz++)
                {
                    final @Nullable Region.Point point = region.atOffset(last, dx, dz);
                    if (point != null && point.land() && point.biomeAltitude == 0 && !explored.get(point.index))
                    {
                        // Minor non-uniformity, makes regions a bit messier
                        if (random.nextInt(13) == 0 && lastPoint.biomeAltitude != 3 * width)
                        {
                            point.biomeAltitude = lastPoint.biomeAltitude;
                            queue.enqueueFirst(point.index);
                        }
                        else
                        {
                            point.biomeAltitude = (byte) nextAltitude;
                            queue.enqueue(point.index);
                        }
                        explored.set(point.index);
                    }
                }
            }
        }

        // Run another pass over the entire region, this time raising land from low -> mid
        for (final Region.Point point : region.points())
        {
            if (point.land())
            {
                if (point.discreteBiomeAltitude() == 0 && point.baseLandHeight >= 4)
                {
                    point.biomeAltitude = (byte) width;
                }
                else if (point.discreteBiomeAltitude() == 1 && point.baseLandHeight >= 11)
                {
                    point.biomeAltitude = (byte) (2 * width);
                }
            }
        }
    }
}