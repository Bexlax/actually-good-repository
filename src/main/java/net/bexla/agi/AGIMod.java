package net.bexla.agi;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import java.util.function.Supplier;

@Mod(AGIMod.MODID)
public class AGIMod {
    public static final String MODID = "actually_good_inventory";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(
                    Registries.DATA_COMPONENT_TYPE, MODID
            );

    public static final Supplier<DataComponentType<Boolean>> FAVORITE =
            DATA_COMPONENTS.registerComponentType(
                    "favorite",
                    builder -> builder
                            .persistent(Codec.BOOL)
                            .networkSynchronized(ByteBufCodecs.BOOL)
            );

    public AGIMod(IEventBus modEventBus, ModContainer modContainer) {
        DATA_COMPONENTS.register(modEventBus);

        AGIConfig.register(modContainer);
    }
}
