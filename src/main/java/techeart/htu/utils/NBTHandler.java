package techeart.htu.utils;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import techeart.htu.MainClass;

import java.util.HashSet;
import java.util.Set;

public class NBTHandler extends WorldSavedData
{
    private static final String DATA_IDENTIFIER = MainClass.MODID + "_nbt";

    public final Set<ISaveable> subscribers = new HashSet();

    public NBTHandler() { this(DATA_IDENTIFIER); }
    public NBTHandler(String name) { super(name); }

    public static NBTHandler get(World world)
    {
        if(!(world instanceof ServerWorld)) throw new RuntimeException("Attempted to get data from client.");
        ServerWorld w = (ServerWorld)world;
        DimensionSavedDataManager storage = w.getSavedData();
        return storage.getOrCreate(NBTHandler::new, DATA_IDENTIFIER);
    }

    public void addSubscriber(ISaveable s) { subscribers.add(s); }

    @Override
    public boolean isDirty()
    {
        boolean flag = false;
        for (ISaveable item : subscribers) flag &= item.isDirty();
        return super.isDirty() || flag;
    }

    @Override
    public void read(CompoundNBT nbt)
    {
        subscribers.forEach(item -> item.readFromNBT(nbt));
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt)
    {
        subscribers.forEach(item -> item.writeToNBT(nbt));
        return nbt;
    }
}
