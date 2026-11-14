package com.sighs.oneenoughvalue;

import com.mojang.logging.LogUtils;
import com.sighs.oneenoughvalue.manager.ItemValueManager;
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
import org.slf4j.Logger;

@SuppressWarnings("removal")
@Mod(OneEnoughValue.MODID)
public class OneEnoughValue {
    public static final String MODID = "oneenoughvalue";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID,"main"),
            ()->"1.1.0",
            "1.1.0"::equals,
            "1.1.0"::equals
    );

    public OneEnoughValue() {
        // 把 common 的平台钩子接到本加载器的实现上，否则 KubeJS 脚本注册不会生效
        ItemValueManager.platform = new com.sighs.oneenoughvalue.forge.ForgePlatform();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        NETWORK.messageBuilder(DataSyncPack.class,0, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(DataSyncPack::fromBuffer)
                .encoder(DataSyncPack::toBuffer)
                .consumerNetworkThread(DataSyncPack::handle)
                .add();
    }
}
