package techeart.htu.utils;

import net.minecraft.block.Block;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
import techeart.htu.objects.*;
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
    public static final RegistryObject<Item> INGOT_COPPER = ITEMS.register("ingot_copper", () -> new HTUItem(MainClass.CREATIVE_TAB));
    public static final RegistryObject<Item> INGOT_TIN = ITEMS.register("ingot_tin", () -> new HTUItem(MainClass.CREATIVE_TAB));
    public static final RegistryObject<Item> INGOT_BRONZE = ITEMS.register("ingot_bronze", () -> new HTUItem(MainClass.CREATIVE_TAB));

    //blocks
    public static final RegistryObject<Block> BLOCK_COPPER = BLOCKS.register("block_copper", () -> new HTUBlock(BlockPropertyPatterns.blockMetal()));
    public static final RegistryObject<Block> BLOCK_TIN = BLOCKS.register("block_tin", () -> new HTUBlock(BlockPropertyPatterns.blockMetal()));
    public static final RegistryObject<Block> BLOCK_BRONZE = BLOCKS.register("block_bronze", () -> new HTUBlock(BlockPropertyPatterns.blockMetal()));

    //block items
    public static final RegistryObject<Item> BLOCK_COPPER_ITEM = ITEMS.register("block_copper", () -> new HTUItemBlock(BLOCK_COPPER.get(), MainClass.CREATIVE_TAB));
    public static final RegistryObject<Item> BLOCK_TIN_ITEM = ITEMS.register("block_tin", () -> new HTUItemBlock(BLOCK_TIN.get(), MainClass.CREATIVE_TAB));
    public static final RegistryObject<Item> BLOCK_BRONZE_ITEM = ITEMS.register("block_bronze", () -> new HTUItemBlock(BLOCK_BRONZE.get(), MainClass.CREATIVE_TAB));

    //ores
    public static final RegistryObject<Block> ORE_COPPER = BLOCKS.register("block_copper_ore", () -> new HTUOre(BlockPropertyPatterns.blockOre()));
    public static final RegistryObject<Block> ORE_TIN = BLOCKS.register("block_tin_ore", () -> new HTUOre(BlockPropertyPatterns.blockOre()));

    //ore items
    public static final RegistryObject<Item> ORE_COPPER_ITEM = ITEMS.register("block_copper_ore", () -> new HTUItemBlock(ORE_COPPER.get(), MainClass.CREATIVE_TAB));
    public static final RegistryObject<Item> ORE_TIN_ITEM = ITEMS.register("block_tin_ore", () -> new HTUItemBlock(ORE_TIN.get(), MainClass.CREATIVE_TAB));

    //primitive furnace
    public static final RegistryObject<Block> BLOCK_PRIMITIVE_FURNACE = BLOCKS.register("block_furnace", BlockPrimitiveFurnace::new);
    public static final RegistryObject<Item> BLOCK_PRIMITIVE_FURNACE_ITEM = ITEMS.register("block_furnace", () -> new HTUItemBlock(BLOCK_PRIMITIVE_FURNACE.get(), MainClass.CREATIVE_TAB));

    //smeltery
    public static final RegistryObject<Block> BLOCK_SMELTERY = BLOCKS.register("block_smeltery", BlockSmeltery::new);
    public static final RegistryObject<Item> BLOCK_SMELTERY_ITEM = ITEMS.register("block_smeltery", () -> new HTUItemBlock(BLOCK_SMELTERY.get(), MainClass.CREATIVE_TAB));

    //steam boiler
    public static final RegistryObject<Block> BLOCK_STEAM_BOILER = BLOCKS.register("block_steam_boiler", BlockSteamBoiler::new);
    public static final RegistryObject<Block> BLOCK_STEAM_BOILER_TOP = BLOCKS.register("block_steam_boiler_top", BlockSteamBoilerTop::new);
    public static final RegistryObject<Item> BLOCK_STEAM_BOILER_ITEM = ITEMS.register("block_steam_boiler", () -> new HTUItemBlock(BLOCK_STEAM_BOILER.get(), MainClass.CREATIVE_TAB));

    //pipe
    public static final RegistryObject<Block> BLOCK_PIPE = BLOCKS.register("block_pipe", BlockPipeFluid::new);
    public static final RegistryObject<Item> BLOCK_PIPE_ITEM = ITEMS.register("block_pipe", () -> new HTUItemBlock(BLOCK_PIPE.get(), MainClass.CREATIVE_TAB));

    //pump
    public static final RegistryObject<Block> BLOCK_WATER_PUMP = BLOCKS.register("block_water_pump", BlockWaterPump::new);
    public static final RegistryObject<Item> BLOCK_WATER_PUMP_ITEM = ITEMS.register("block_water_pump", () -> new HTUItemBlock(BLOCK_WATER_PUMP.get(), MainClass.CREATIVE_TAB));

    //tank
    public static final RegistryObject<Block> BLOCK_FLUID_TANK = BLOCKS.register("block_fluid_tank", BlockFluidTank::new);
    public static final RegistryObject<Item> BLOCK_FLUID_TANK_ITEM = ITEMS.register("block_fluid_tank", () -> new HTUItemBlock(BLOCK_FLUID_TANK.get(), MainClass.CREATIVE_TAB));

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
