package techeart.htu.utils;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import techeart.htu.MainClass;
import techeart.htu.objects.BlockPropertyPatterns;
import techeart.htu.objects.HTUIconItem;
import techeart.htu.objects.boiler.BlockSteamBoiler;
import techeart.htu.objects.boiler.BlockSteamBoilerTop;
import techeart.htu.objects.boiler.ContainerSteamBoiler;
import techeart.htu.objects.boiler.TileEntitySteamBoiler;
import techeart.htu.objects.fluids.FluidSteam;
import techeart.htu.objects.furnace.BlockPrimitiveFurnace;
import techeart.htu.objects.furnace.TileEntityPrimitiveFurnace;
import techeart.htu.objects.pipe.BlockPipeFluid;
import techeart.htu.objects.pipe.TileEntityPipeFluid;
import techeart.htu.objects.pump.BlockWaterPump;
import techeart.htu.objects.pump.TileEntityWaterPump;
import techeart.htu.objects.smeltery.BlockSmeltery;
import techeart.htu.objects.smeltery.ContainerSmeltery;
import techeart.htu.objects.smeltery.TileEntitySmeltery;
import techeart.htu.objects.tank.BlockFluidTank;
import techeart.htu.objects.tank.TileEntityFluidTank;
import techeart.htu.recipes.alloying.AlloyRecipe;
import techeart.htu.recipes.alloying.RecipeTypeAlloying;
import techeart.htu.utils.registration.BIRegister;
import techeart.htu.utils.registration.DoubleRegisteredObject;
import techeart.htu.utils.registration.HTUBlock;
import techeart.htu.utils.registration.HTUItem;

public class RegistryHandler
{
//    public static final MultyDeferredRegister MDR = new MultyDeferredRegister();

    public static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MainClass.MODID);
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MainClass.MODID);
    public static final BIRegister BI_REGISTER = new BIRegister();
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, MainClass.MODID);

    public static void init()
    {

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BI_REGISTER.register(bus);
        FLUIDS.register(bus);
        CONTAINERS.register(bus);
        TILES.register(bus);
    }

//    public static final HTUMachine TEST = new HTUMachine.Builder().addBlock(new HTUMachine.MachineBlock(new BlockPrimitiveFurnace(), new TileEntitySteamBoiler()))/*.withContainer(ContainerSteamBoiler::new).withGUI(GuiSteamBoiler.class)*/.build();


    //items
    public static final RegistryObject<Item> INGOT_COPPER = BI_REGISTER.registerSingleItem("ingot_copper", () -> new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB).build());
    public static final RegistryObject<Item> INGOT_TIN = BI_REGISTER.registerSingleItem("ingot_tin", () -> new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB).build());
    public static final RegistryObject<Item> INGOT_BRONZE = BI_REGISTER.registerSingleItem("ingot_bronze", () -> new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB).build());

    //blocks
    public static final DoubleRegisteredObject<Block,Item> BLOCK_COPPER = BI_REGISTER.register("block_copper",new HTUBlock.Builder().withProperties(BlockPropertyPatterns.blockMetal()).build(),new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB));
    public static final DoubleRegisteredObject<Block,Item> BLOCK_TIN = BI_REGISTER.register("block_tin",new HTUBlock.Builder().withProperties(BlockPropertyPatterns.blockMetal()).build(),new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB));
    public static final DoubleRegisteredObject<Block,Item> BLOCK_BRONZE = BI_REGISTER.register("block_bronze",new HTUBlock.Builder().withProperties(BlockPropertyPatterns.blockMetal()).build(),new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB));

    //ores
    public static final DoubleRegisteredObject<Block,Item> ORE_COPPER = BI_REGISTER.register("block_copper_ore",new HTUBlock.Builder().withProperties(BlockPropertyPatterns.blockMetal()).build(),new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB));
    public static final DoubleRegisteredObject<Block,Item> ORE_TIN = BI_REGISTER.register("block_tin_ore",new HTUBlock.Builder().withProperties(BlockPropertyPatterns.blockOre()).build(),new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB));

                  /*~~~~~~~~~~~~~~~~~~~~~~~~~MACHINES~AREA~~~~~~~~~~~~~~~~~~~~~~~~~*/
    //primitive furnace
    public static final DoubleRegisteredObject<Block,Item> BLOCK_PRIMITIVE_FURNACE = BI_REGISTER.register("block_furnace",new HTUBlock.Builder().setBlock(BlockPrimitiveFurnace::new).build(),new HTUItem.Builder().itemGroup(MainClass.PRIMAL_CREATIVE_TAB));
    public static final RegistryObject<TileEntityType<TileEntityPrimitiveFurnace>> FURNACE_TE = TILES.register("furnace", () -> TileEntityType.Builder.create(TileEntityPrimitiveFurnace::new, RegistryHandler.BLOCK_PRIMITIVE_FURNACE.getPrimary()).build(null));

    //smeltery
    public static final DoubleRegisteredObject<Block,Item> BLOCK_SMELTERY = BI_REGISTER.register("block_smeltery",new HTUBlock.Builder().setBlock(BlockSmeltery::new).build(),new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB));
    public static final RegistryObject<TileEntityType<TileEntitySmeltery>> SMELTERY_TE = TILES.register("smeltery", () -> TileEntityType.Builder.create(TileEntitySmeltery::new, RegistryHandler.BLOCK_SMELTERY.getPrimary()).build(null));
    public static final RegistryObject<ContainerType<ContainerSmeltery>> SMELTERY_CONTAINER = CONTAINERS.register("smeltery", () -> IForgeContainerType.create(ContainerSmeltery::new));

    //steam boiler
    public static final DoubleRegisteredObject<Block,Item> BLOCK_STEAM_BOILER = BI_REGISTER.register("block_steam_boiler",new HTUBlock.Builder().setBlock(BlockSteamBoiler::new).build(),new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB));
    public static final RegistryObject<Block> BLOCK_STEAM_BOILER_TOP = BI_REGISTER.registerSingleBlock("block_steam_boiler_top", new HTUBlock.Builder().setBlock(BlockSteamBoilerTop::new).build());
    public static final RegistryObject<TileEntityType<TileEntitySteamBoiler>> STEAM_BOILER_TE = TILES.register("steam_boiler", () -> TileEntityType.Builder.create(TileEntitySteamBoiler::new, RegistryHandler.BLOCK_STEAM_BOILER.getPrimary()).build(null));
    public static final RegistryObject<ContainerType<ContainerSteamBoiler>> STEAM_BOILER_CONTAINER = CONTAINERS.register("steam_boiler", () -> IForgeContainerType.create(ContainerSteamBoiler::new));

    //pipe
    public static final DoubleRegisteredObject<Block,Item> BLOCK_PIPE = BI_REGISTER.register("block_pipe",new HTUBlock.Builder().setBlock(BlockPipeFluid::new).build(),new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB));
    public static final RegistryObject<TileEntityType<TileEntityPipeFluid>> FLUID_PIPE_TE = TILES.register("pipe_fluid", () -> TileEntityType.Builder.create(TileEntityPipeFluid::new, RegistryHandler.BLOCK_PIPE.getPrimary()).build(null));

    //pump
    public static final DoubleRegisteredObject<Block,Item> BLOCK_WATER_PUMP = BI_REGISTER.register("block_water_pump",new HTUBlock.Builder().setBlock(BlockWaterPump::new).build(),new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB));
    public static final RegistryObject<TileEntityType<TileEntityWaterPump>> WATER_PUMP_TE = TILES.register("water_pump", () -> TileEntityType.Builder.create(TileEntityWaterPump::new, RegistryHandler.BLOCK_WATER_PUMP.getPrimary()).build(null));

    //tank
    public static final DoubleRegisteredObject<Block,Item> BLOCK_FLUID_TANK = BI_REGISTER.register("block_fluid_tank",new HTUBlock.Builder().setBlock(BlockFluidTank::new).build(),new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB));
    public static final RegistryObject<TileEntityType<TileEntityFluidTank>> FLUID_TANK_TE = TILES.register("fluid_tank", () -> TileEntityType.Builder.create(TileEntityFluidTank::new, RegistryHandler.BLOCK_FLUID_TANK.getPrimary()).build(null));

    //fluid blocks TODO: Remove steam (as fluid)..
//    public static final RegistryObject<FlowingFluidBlock> BLOCK_FLUID_STEAM = BLOKS.register("block_fluid_steam",() -> new FlowingFluidBlock(() -> FluidSteam.STEAM, Block.Properties.create(Material.WATER).noDrops()));

    //fluids
    public static final RegistryObject<Fluid> FLUID_STEAM = FLUIDS.register("fluid_steam", FluidSteam.Source::new);
    public static final RegistryObject<Fluid> FLUID_STEAM_FLOWING = FLUIDS.register("fluid_steam_flowing", FluidSteam.Flowing::new);

    public static final IRecipeType<AlloyRecipe> RECIPE_TYPE_ALLOYING = new RecipeTypeAlloying();
    public static void registerRecipeSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event)
    {
        Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(RECIPE_TYPE_ALLOYING.toString()), RECIPE_TYPE_ALLOYING);
        event.getRegistry().register(AlloyRecipe.SERIALIZER);
    }

    public static final RegistryObject<Item> ICON_SMORC = BI_REGISTER.registerSingleItem("icon_smorc",HTUIconItem::new);





//    @SubscribeEvent
//    public static void onBlockRegister(RegistryEvent.Register<Block> event)
//    {
//        event.getRegistry().registerAll(Initialization.BLOCKS.toArray(new Block[0]));
//
//        FluidRegistry.registerFluid(Initialization.FLUID_STEAM);
//        //FluidRegistry.addBucketForFluid(Initialization.FLUID_STEAM);
//
//        TileEntityHandler.registerTileEntities();
//    }
//
//    @SubscribeEvent
//    public static void onItemRegister(RegistryEvent.Register<Item> event)
//    {
//        event.getRegistry().registerAll(Initialization.ITEMS.toArray(new Item[0]));
//    }
//
//    @SubscribeEvent
//    public static void onModelRegister(ModelRegistryEvent event)
//    {
//        for (Block block : Initialization.BLOCKS)
//        {
//            if (block instanceof IHasModel)
//            {
//                ((IHasModel)block).registerModels();
//            }
//        }
//
//        for (Item item : Initialization.ITEMS)
//        {
//            if (item instanceof IHasModel)
//            {
//                ((IHasModel)item).registerModels();
//            }
//        }
//    }
//
//    public static void initRegistries()
//    {
//        NetworkRegistry.INSTANCE.registerGuiHandler(MainClass.instance, new GuiHandler());
//
//        OreDict.registerInOreDict();
//    }
}
