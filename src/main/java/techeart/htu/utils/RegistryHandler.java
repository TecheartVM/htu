package techeart.htu.utils;

import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.event.RegistryEvent;
import techeart.htu.MainClass;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import techeart.htu.objects.*;
import techeart.htu.objects.boiler.BlockSteamBoiler;
import techeart.htu.objects.boiler.BlockSteamBoilerTop;
import techeart.htu.objects.fluids.FluidSteam;
import techeart.htu.objects.furnace.BlockPrimitiveFurnace;
import techeart.htu.objects.pipes.BlockPipeFluid;
import techeart.htu.objects.pump.BlockWaterPump;
import techeart.htu.objects.smeltery.BlockSmeltery;
import techeart.htu.recipes.alloying.AlloyRecipe;
import techeart.htu.recipes.alloying.RecipeTypeAlloying;

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
    public static final Item INGOT_COPPER = ITEMS.register("ingot_copper",HTUItem::new).get();
    public static final Item INGOT_TIN = ITEMS.register("ingot_tin", HTUItem::new).get();
    public static final Item INGOT_BRONZE = ITEMS.register("ingot_bronze", HTUItem::new).get();

    //blocks
    public static final Block BLOCK_COPPER = BLOCKS.register("block_copper", () -> new HTUBlock(BlockPropertyPatterns.blockMetal())).get();
    public static final Block BLOCK_TIN = BLOCKS.register("block_tin", () -> new HTUBlock(BlockPropertyPatterns.blockMetal())).get();
    public static final Block BLOCK_BRONZE = BLOCKS.register("block_bronze", () -> new HTUBlock(BlockPropertyPatterns.blockMetal())).get();

    //block items
    public static final Item BLOCK_COPPER_ITEM = ITEMS.register("block_copper", () -> new HTUItemBlock(BLOCK_COPPER)).get();
    public static final Item BLOCK_TIN_ITEM = ITEMS.register("block_tin", () -> new HTUItemBlock(BLOCK_TIN)).get();
    public static final Item BLOCK_BRONZE_ITEM = ITEMS.register("block_bronze", () -> new HTUItemBlock(BLOCK_BRONZE)).get();

    //ores
    public static final Block ORE_COPPER = BLOCKS.register("block_copper_ore", () -> new HTUOre(BlockPropertyPatterns.blockOre())).get();
    public static final Block ORE_TIN = BLOCKS.register("block_tin_ore", () -> new HTUOre(BlockPropertyPatterns.blockOre())).get();

    //ore items
    public static final Item ORE_COPPER_ITEM = ITEMS.register("block_copper_ore", () -> new HTUItemBlock(ORE_COPPER)).get();
    public static final Item ORE_TIN_ITEM = ITEMS.register("block_tin_ore", () -> new HTUItemBlock(ORE_TIN)).get();

    //primitive furnace
    public static final Block BLOCK_PRIMITIVE_FURNACE = BLOCKS.register("block_furnace", BlockPrimitiveFurnace::new).get();
    public static final Item BLOCK_PRIMITIVE_FURNACE_ITEM = ITEMS.register("block_furnace", () -> new HTUItemBlock(BLOCK_PRIMITIVE_FURNACE)).get();

    //smeltery
    public static final Block BLOCK_SMELTERY = BLOCKS.register("block_smeltery", BlockSmeltery::new).get();
    public static final Item BLOCK_SMELTERY_ITEM = ITEMS.register("block_smeltery", () -> new HTUItemBlock(BLOCK_SMELTERY)).get();

    //steam boiler
    public static final Block BLOCK_STEAM_BOILER = BLOCKS.register("block_steam_boiler", BlockSteamBoiler::new).get();
    public static final Block BLOCK_STEAM_BOILER_TOP = BLOCKS.register("block_steam_boiler_top", BlockSteamBoilerTop::new).get();
    public static final Item BLOCK_STEAM_BOILER_ITEM = ITEMS.register("block_steam_boiler", () -> new HTUItemBlock(BLOCK_STEAM_BOILER)).get();

    //pipe
    public static final RegistryObject<Block> BLOCK_PIPE = BLOCKS.register("block_pipe", BlockPipeFluid::new);
    public static final Item BLOCK_PIPE_ITEM = ITEMS.register("block_pipe", () -> new HTUItemBlock(BLOCK_PIPE.get())).get();

    //pipe
    public static final RegistryObject<Block> BLOCK_WATER_PUMP = BLOCKS.register("block_water_pump", BlockWaterPump::new);
    public static final Item BLOCK_WATER_PUMP_ITEM = ITEMS.register("block_water_pump", () -> new HTUItemBlock(BLOCK_WATER_PUMP.get())).get();

    //fluid blocks
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
