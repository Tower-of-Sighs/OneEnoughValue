package cc.sighs.oneenoughvalue;

import com.mojang.logging.LogUtils;
import cc.sighs.oneenoughvalue.network.DataSyncPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

@SuppressWarnings("removal")
@Mod(OneEnoughValue.MODID)
public class OneEnoughValue {
    public static final String MODID = "oneenoughvalue";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OneEnoughValue(IEventBus eventBus, ModContainer container) {
        eventBus.register(this);
        container.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }

    @SubscribeEvent
    public void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(DataSyncPayload.TYPE, DataSyncPayload.STREAM_CODEC, DataSyncPayload::handleClient);
    }

}
