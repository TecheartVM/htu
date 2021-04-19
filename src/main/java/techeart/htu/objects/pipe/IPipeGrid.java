package techeart.htu.objects.pipe;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;

public interface IPipeGrid
{
    boolean isEmpty();
    boolean isFull();

    boolean addPipe(IPipe pipe);

    void tick();

    void markDirty();
    CompoundNBT writeToNBT(CompoundNBT nbt);
    void readFromNBT(CompoundNBT nbt);

    FluidStack destroy();
}
