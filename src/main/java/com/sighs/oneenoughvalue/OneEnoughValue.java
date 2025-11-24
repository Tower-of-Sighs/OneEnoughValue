package com.sighs.oneenoughvalue;

import com.sighs.oneenoughvalue.network.DataSyncPack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@SuppressWarnings("removal")
@Mod(OneEnoughValue.MODID)
public class OneEnoughValue {

    public static final String MODID = "oneenoughvalue";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID,"main"),
            ()->"1.0.0",
            "1.0.0"::equals,
            "1.0.0"::equals
    );

    public OneEnoughValue() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        NETWORK.messageBuilder(DataSyncPack.class,0, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(DataSyncPack::fromBuffer)
                .encoder(DataSyncPack::toBuffer)
                .consumerNetworkThread(DataSyncPack::handle)
                .add();
    }
}
