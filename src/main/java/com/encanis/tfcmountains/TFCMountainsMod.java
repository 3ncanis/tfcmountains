package com.encanis.tfcmountains;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;

import com.encanis.tfcmountains.world.noise.MountainBlendKernel;

@Mod("tfcmountains")
public class TFCMountainsMod {
    public TFCMountainsMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, TFCMountainsConfig.SPEC);
        modEventBus.addListener(this::onConfigLoad);
        modEventBus.addListener(this::onConfigReload);
    }

    private void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == TFCMountainsConfig.SPEC) {
            MountainBlendKernel.rebuild(TFCMountainsConfig.HEIGHT_BLEND.get());
        }
    }

    private void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == TFCMountainsConfig.SPEC) {
            MountainBlendKernel.rebuild(TFCMountainsConfig.HEIGHT_BLEND.get());
        }
    }
}