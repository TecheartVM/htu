package techeart.htu.utils.registration;

import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class DoubleRegisteredObject<PRIMARY extends IForgeRegistryEntry<? super PRIMARY>, SECONDARY extends IForgeRegistryEntry<? super SECONDARY>> {
    private final RegistryObject<PRIMARY> primaryRO;
    private final RegistryObject<SECONDARY> secondaryRO;

    public DoubleRegisteredObject(RegistryObject<PRIMARY> primaryRO, RegistryObject<SECONDARY> secondaryRO) {
        this.primaryRO = primaryRO;
        this.secondaryRO = secondaryRO;
    }

    public PRIMARY getPrimary() {
        return primaryRO.get();
    }

    public SECONDARY getSecondary() {
        return secondaryRO.get();
    }
}
