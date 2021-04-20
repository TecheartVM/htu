package techeart.htu.objects.tank;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import techeart.htu.utils.HTUTileEntityType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityFluidTank extends TileEntity implements IFluidHandler
{
    public static final int CAPACITY = 8000;

    private final FluidTank internalVolume = new FluidTank(CAPACITY);

    public TileEntityFluidTank()
    {
        super(HTUTileEntityType.FLUID_TANK.get());
    }

    /*TileEntity*/
    @Override
    public void read(BlockState state, CompoundNBT nbt)
    {
        super.read(state, nbt);
        internalVolume.readFromNBT(nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt)
    {
        super.write(nbt);
        internalVolume.writeToNBT(nbt);
        return nbt;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability)
    {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
        {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> (internalVolume)));
        }
        return super.getCapability(capability);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
    {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
        {
            //TODO add an ability to lock tank
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> (internalVolume)));
        }
        return super.getCapability(capability, facing);
    }

    /*IFluidHandler*/
    @Override
    public int getTanks() { return 1; }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) { return internalVolume.getFluid(); }

    @Override
    public int getTankCapacity(int tank) { return CAPACITY; }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return  internalVolume.isEmpty() || stack.isFluidEqual(internalVolume.getFluid()) || stack.isEmpty(); }

    @Override
    public int fill(FluidStack resource, FluidAction action) { return internalVolume.fill(resource, action); }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) { return internalVolume.drain(resource, action); }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) { return internalVolume.drain(maxDrain, action); }
}
