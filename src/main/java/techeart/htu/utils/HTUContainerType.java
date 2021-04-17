package techeart.htu.utils;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import techeart.htu.MainClass;
import techeart.htu.objects.boiler.ContainerSteamBoiler;
import techeart.htu.objects.smeltery.ContainerSmeltery;

public class HTUContainerType
{
    public static final DeferredRegister<ContainerType<?>> CONTAINER_TYPES = DeferredRegister.create(ForgeRegistries.CONTAINERS, MainClass.MODID);

    public static final RegistryObject<ContainerType<ContainerSmeltery>> SMELTERY =
            CONTAINER_TYPES.register("smeltery", () -> IForgeContainerType.create(ContainerSmeltery::new));

    public static final RegistryObject<ContainerType<ContainerSteamBoiler>> STEAM_BOILER =
            CONTAINER_TYPES.register("steam_boiler", () -> IForgeContainerType.create(ContainerSteamBoiler::new));
}
