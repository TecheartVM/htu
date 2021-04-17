package techeart.htu.utils;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import techeart.htu.MainClass;
import techeart.htu.objects.boiler.TileEntitySteamBoiler;
import techeart.htu.objects.furnace.TileEntityPrimitiveFurnace;
import techeart.htu.objects.pipe.TileEntityGridTile;
import techeart.htu.objects.pipes.TileEntityPipeFluid;
import techeart.htu.objects.pump.TileEntityWaterPump;
import techeart.htu.objects.smeltery.TileEntitySmeltery;

public class HTUTileEntityType
{
    public static final DeferredRegister<TileEntityType<?>> TILEENTITY_TYPES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MainClass.MODID);

    public static final RegistryObject<TileEntityType<TileEntityPrimitiveFurnace>> FURNACE =
            TILEENTITY_TYPES.register("furnace", () -> TileEntityType.Builder.create(TileEntityPrimitiveFurnace::new, RegistryHandler.BLOCK_PRIMITIVE_FURNACE.get()).build(null));

    public static final RegistryObject<TileEntityType<TileEntitySmeltery>> SMELTERY =
            TILEENTITY_TYPES.register("smeltery", () -> TileEntityType.Builder.create(TileEntitySmeltery::new, RegistryHandler.BLOCK_SMELTERY.get()).build(null));

    public static final RegistryObject<TileEntityType<TileEntitySteamBoiler>> STEAM_BOILER =
            TILEENTITY_TYPES.register("steam_boiler", () -> TileEntityType.Builder.create(TileEntitySteamBoiler::new, RegistryHandler.BLOCK_STEAM_BOILER.get()).build(null));

    //TODO
    public static final RegistryObject<TileEntityType<TileEntityPipeFluid>> PIPE_FLUID =
            TILEENTITY_TYPES.register("pipe_fluid", () -> TileEntityType.Builder.create(TileEntityPipeFluid::new, RegistryHandler.BLOCK_PIPE.get()).build(null));

    public static final RegistryObject<TileEntityType<TileEntityWaterPump>> WATER_PUMP =
            TILEENTITY_TYPES.register("water_pump", () -> TileEntityType.Builder.create(TileEntityWaterPump::new, RegistryHandler.BLOCK_WATER_PUMP.get()).build(null));

    public static final RegistryObject<TileEntityType<TileEntityGridTile>> GRID_TILE =
            TILEENTITY_TYPES.register("grid_tile", () -> TileEntityType.Builder.create(TileEntityGridTile::new, RegistryHandler.BLOCK_PIPE.get()).build(null));

}
