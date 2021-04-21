package techeart.htu.utils;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import techeart.htu.MainClass;
import techeart.htu.objects.pipe.HorizontalPipeGrid;
import techeart.htu.objects.pipe.IPipeGrid;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WorldGridsManager extends WorldSavedData
{
    private static final String DATA_IDENTIFIER = MainClass.MODID + "_grids_nbt";

    private static final Set<IPipeGrid> worldGrids = new HashSet<>();

    private boolean isLoaded = false;

    public WorldGridsManager() { this(DATA_IDENTIFIER); }
    public WorldGridsManager(String name) { super(name); }

    public void registerGrid(IPipeGrid grid)
    {
        worldGrids.add(grid);
        markDirty();
    }

    public void unregisterGrid(IPipeGrid grid)
    {
        System.out.println("Unregistering grid!");
        worldGrids.remove(grid);
        markDirty();
    }

    @Override
    public void setDirty(boolean isDirty)
    {
        System.out.println("Setting dirty to " + isDirty);
        super.setDirty(isDirty);
    }

    public boolean isLoaded() { return isLoaded; }

    public int getGridsCount() { return worldGrids.size(); }

    public IPipeGrid getGrid(UUID id)
    {
        for (IPipeGrid grid : worldGrids)
        {
            System.out.println("Required id: " + id);
            System.out.println("Current id: " + grid.getId());
            System.out.println("Equation result: " + (id.equals(grid.getId())));
            if (grid.getId().equals(id)) return grid;
        }
        return null;
    }

    public void tick()
    {
        worldGrids.forEach(grid -> grid.tick());
    }

    public void onServerWorldTick(World world)
    {
        if(!isLoaded) loadOrCreate();
    }

    @Override
    public void read(CompoundNBT nbt)
    {
        System.out.println("READING!");
        worldGrids.clear();
        int count = nbt.getInt("htuGridsCount");
        for (int i = 0; i < count; i++)
        {
            CompoundNBT gridNBT = nbt.getCompound("htuGrid" + i);
            if(gridNBT.hasUniqueId("id"))
            {
                HorizontalPipeGrid grid = new HorizontalPipeGrid(gridNBT.getUniqueId("id"));
                grid.readFromNBT(gridNBT);
                registerGrid(grid);

                System.out.println(gridNBT.getUniqueId("id"));
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt)
    {
        System.out.println("WRITING!");
        nbt.putInt("htuGridsCount", worldGrids.size());
        int counter = 0;
        for (IPipeGrid grid : worldGrids)
        {
            nbt.put("htuGrid" + counter, grid.writeToNBT(new CompoundNBT()));
            counter++;
        }
        System.out.println("Grids written: " + counter);
        return nbt;
    }

    @Override
    public boolean isDirty()
    {
        System.out.println("CHECK DIRTY!");
        for (IPipeGrid grid : worldGrids)
        {
            System.out.println("Dirty?");
            if (grid.isDirty())
            {
                System.out.println("YEP!!!!!!!!!!!!!!!");
                return true;
            }
        }
        if(super.isDirty()) System.out.println("YEP!");
        return super.isDirty();
    }

    private void loadOrCreate()
    {
        MainClass.LOGGER.debug("World Grids Manager is Loading!");
        DimensionSavedDataManager storage = ServerLifecycleHooks.getCurrentServer().func_241755_D_().getSavedData();
        storage.getOrCreate(WorldGridsManager::new, DATA_IDENTIFIER);
        isLoaded = true;
    }

    public void reset()
    {
        worldGrids.clear();
        isLoaded = false;
    }

//    public static WorldGridsManager get()//World world)
//    {
//        //if(!(world instanceof ServerWorld)) throw new RuntimeException("Attempted to get data from client.");
//        DimensionSavedDataManager storage = ServerLifecycleHooks.getCurrentServer().func_241755_D_().getSavedData();
//        return storage.getOrCreate(WorldGridsManager::new, DATA_IDENTIFIER);
//    }
}
