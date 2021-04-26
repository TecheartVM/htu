package techeart.htu.utils.registration;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import techeart.htu.MainClass;

import java.util.function.Supplier;

public class BIRegister {
    private final  DeferredRegister<Block> blockRegister;
    private final   DeferredRegister<Item> itemRegister;

    public BIRegister() {
        this.blockRegister = DeferredRegister.create(ForgeRegistries.BLOCKS, MainClass.MODID);
        this.itemRegister = DeferredRegister.create(ForgeRegistries.ITEMS, MainClass.MODID);;
    }

    public RegistryObject<Block> registerSingleBlock(String name, Supplier<? extends Block> primarySupplier)
    {
        return blockRegister.register(name, primarySupplier);
    }
    public RegistryObject<Item> registerSingleItem(String name, Supplier<? extends Item> itemSupplier)
    {
        return itemRegister.register(name, itemSupplier);
    }

    public void register(IEventBus bus)
    {
        blockRegister.register(bus);
        itemRegister.register(bus);
    }

    public DoubleRegisteredObject<Block,Item> register(String name, Supplier<? extends Block> primarySupplier,HTUItem.Builder itemBuilder)
    {
        RegistryObject<Block> o1 = blockRegister.register(name,primarySupplier);
        RegistryObject<Item> o2 = itemRegister.register(name,() ->itemBuilder.block(o1.get()).build());
        return new DoubleRegisteredObject<>(o1, o2);
    }

    public DeferredRegister<Item> getItemRegister() {
        return itemRegister;
    }
}
