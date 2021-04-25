package techeart.htu.utils;

import net.minecraft.block.Block;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import techeart.htu.MainClass;
import techeart.htu.objects.BlockPropertyPatterns;
import techeart.htu.objects.HTUIconItem;
import techeart.htu.objects.boiler.BlockSteamBoiler;
import techeart.htu.objects.boiler.BlockSteamBoilerTop;
import techeart.htu.objects.fluids.FluidSteam;
import techeart.htu.objects.furnace.BlockPrimitiveFurnace;
import techeart.htu.objects.pipe.BlockPipeFluid;
import techeart.htu.objects.pump.BlockWaterPump;
import techeart.htu.objects.smeltery.BlockSmeltery;
import techeart.htu.objects.tank.BlockFluidTank;
import techeart.htu.recipes.alloying.AlloyRecipe;
import techeart.htu.recipes.alloying.RecipeTypeAlloying;
import techeart.htu.utils.registration.HTUBlock;
import techeart.htu.utils.registration.HTUItem;

public class RegistryHandler
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, techeart.htu.MainClass.MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MainClass.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, MainClass.MODID);

    public static void init()
    {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        FLUIDS.register(FMLJavaModLoadingContext.get().getModEventBus());
        HTUContainerType.CONTAINER_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        HTUTileEntityType.TILEENTITY_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }


    //items
    public static final RegistryObject<Item> INGOT_COPPER = ITEMS.register("ingot_copper", () -> new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB).build());
    public static final RegistryObject<Item> INGOT_TIN = ITEMS.register("ingot_tin", () -> new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB).build());
    public static final RegistryObject<Item> INGOT_BRONZE = ITEMS.register("ingot_bronze", () -> new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB).build());

    //blocks
    public static final RegistryObject<Block> BLOCK_COPPER = BLOCKS.register("block_copper",  new HTUBlock.Builder().withProperties(BlockPropertyPatterns.blockMetal()).build());
    public static final RegistryObject<Block> BLOCK_TIN = BLOCKS.register("block_tin",  new HTUBlock.Builder().withProperties(BlockPropertyPatterns.blockMetal()).build());
    public static final RegistryObject<Block> BLOCK_BRONZE = BLOCKS.register("block_bronze", new HTUBlock.Builder().withProperties(BlockPropertyPatterns.blockMetal()).build());

    //block items
    public static final RegistryObject<Item> BLOCK_COPPER_ITEM = ITEMS.register("block_copper", () -> new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB).block(BLOCK_COPPER.get()).build());
    public static final RegistryObject<Item> BLOCK_TIN_ITEM = ITEMS.register("block_tin", () -> new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB).block(BLOCK_TIN.get()).build());
    public static final RegistryObject<Item> BLOCK_BRONZE_ITEM = ITEMS.register("block_bronze", () -> new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB).block(BLOCK_BRONZE.get()).build());

    //ores
    public static final RegistryObject<Block> ORE_COPPER = BLOCKS.register("block_copper_ore",  new HTUBlock.Builder().withProperties(BlockPropertyPatterns.blockOre()).build());
    public static final RegistryObject<Block> ORE_TIN = BLOCKS.register("block_tin_ore",  new HTUBlock.Builder().withProperties(BlockPropertyPatterns.blockOre()).build());

    //ore items
    public static final RegistryObject<Item> ORE_COPPER_ITEM = ITEMS.register("block_copper_ore", () -> new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB).block(ORE_COPPER.get()).build());
    public static final RegistryObject<Item> ORE_TIN_ITEM = ITEMS.register("block_tin_ore",  () -> new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB).block(ORE_TIN.get()).build());

    //primitive furnace
    public static final RegistryObject<Block> BLOCK_PRIMITIVE_FURNACE = BLOCKS.register("block_furnace", new HTUBlock.Builder().setBlock(BlockPrimitiveFurnace::new).build());
    public static final RegistryObject<Item> BLOCK_PRIMITIVE_FURNACE_ITEM = ITEMS.register("block_furnace",  () -> new HTUItem.Builder().itemGroup(MainClass.PRIMAL_CREATIVE_TAB).block(BLOCK_PRIMITIVE_FURNACE.get()).build());

    //smeltery
    public static final RegistryObject<Block> BLOCK_SMELTERY = BLOCKS.register("block_smeltery", new HTUBlock.Builder().setBlock(BlockSmeltery::new).build());
    public static final RegistryObject<Item> BLOCK_SMELTERY_ITEM = ITEMS.register("block_smeltery",  () -> new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB).block(BLOCK_SMELTERY.get()).build());

    //steam boiler
    public static final RegistryObject<Block> BLOCK_STEAM_BOILER = BLOCKS.register("block_steam_boiler",new HTUBlock.Builder().setBlock(BlockSteamBoiler::new).build());
    public static final RegistryObject<Block> BLOCK_STEAM_BOILER_TOP = BLOCKS.register("block_steam_boiler_top", new HTUBlock.Builder().setBlock(BlockSteamBoilerTop::new).build());
    public static final RegistryObject<Item> BLOCK_STEAM_BOILER_ITEM = ITEMS.register("block_steam_boiler",  () -> new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB).block(BLOCK_STEAM_BOILER.get()).build());

    //pipe
    public static final RegistryObject<Block> BLOCK_PIPE = BLOCKS.register("block_pipe",new HTUBlock.Builder().setBlock(BlockPipeFluid::new).build());
    public static final RegistryObject<Item> BLOCK_PIPE_ITEM = ITEMS.register("block_pipe", () -> new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB).block(BLOCK_PIPE.get()).build());

    //pump
    public static final RegistryObject<Block> BLOCK_WATER_PUMP = BLOCKS.register("block_water_pump", new HTUBlock.Builder().setBlock(BlockWaterPump::new).build());
    public static final RegistryObject<Item> BLOCK_WATER_PUMP_ITEM = ITEMS.register("block_water_pump", () -> new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB).block(BLOCK_WATER_PUMP.get()).build());

    //tank
    public static final RegistryObject<Block> BLOCK_FLUID_TANK = BLOCKS.register("block_fluid_tank",new HTUBlock.Builder().setBlock(BlockFluidTank::new).build());
    public static final RegistryObject<Item> BLOCK_FLUID_TANK_ITEM = ITEMS.register("block_fluid_tank", () -> new HTUItem.Builder().itemGroup(MainClass.STEAM_CREATIVE_TAB).block(BLOCK_FLUID_TANK.get()).build());

    //fluid blocks TODO: Remove this thing..
    public static final RegistryObject<FlowingFluidBlock> BLOCK_FLUID_STEAM = BLOCKS.register("block_fluid_steam",() -> new FlowingFluidBlock(() -> FluidSteam.STEAM, Block.Properties.create(Material.WATER).noDrops()));

    //fluids
    public static final RegistryObject<Fluid> FLUID_STEAM = FLUIDS.register("fluid_steam", FluidSteam.Source::new);
    public static final RegistryObject<Fluid> FLUID_STEAM_FLOWING = FLUIDS.register("fluid_steam_flowing", FluidSteam.Flowing::new);

    public static final IRecipeType<AlloyRecipe> RECIPE_TYPE_ALLOYING = new RecipeTypeAlloying();
    public static void registerRecipeSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event)
    {
        Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(RECIPE_TYPE_ALLOYING.toString()), RECIPE_TYPE_ALLOYING);
        event.getRegistry().register(AlloyRecipe.SERIALIZER);
    }

    public static final RegistryObject<Item> ICON_SMORC = ITEMS.register("icon_smorc",HTUIconItem::new);

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
