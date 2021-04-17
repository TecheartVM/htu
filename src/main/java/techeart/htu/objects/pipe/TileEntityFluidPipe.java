package techeart.htu.objects.pipe;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import techeart.htu.utils.HTUTileEntityType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

//TODO: optimization
public class TileEntityFluidPipe extends TileEntity implements IFluidHandler, IFluidTank, ITickableTileEntity
{
    private static final int transferRate = 125;
    private static final int internalVolume = 1000;

    private FluidStack currentFluidStack;

    private ArrayList<IFluidHandler> connections = new ArrayList<>();

    private boolean shouldTransfer = false;

    public TileEntityFluidPipe()
    {
        super(HTUTileEntityType.PIPE_FLUID.get());
        currentFluidStack = FluidStack.EMPTY;
    }

    public String getInfo()
    {
        return currentFluidStack.getFluid().getRegistryName() + ", " + currentFluidStack.getAmount() + "mb";
    }

    @Override
    public void tick()
    {
        if(!world.isRemote)
        {
            if (!shouldTransfer)
            {
                if (!currentFluidStack.isEmpty())
                    scheduleTransfer();
                return;
            }

            transfer();
        }
    }

    protected void transfer()
    {
        if(currentFluidStack.isEmpty())
        {
            shouldTransfer = false;
            return;
        }

        int amount = Math.floorDiv(currentFluidStack.getAmount(), connections.size());
        if(amount <= 0)
            amount = 1;
        else
            amount = Math.min(transferRate, amount);

        int toDrain = 0;
        for (IFluidHandler fluidHandler : connections)
        {
            if(fluidHandler.drain(currentFluidStack, FluidAction.SIMULATE).getAmount() >= currentFluidStack.getAmount()) continue;
            else
            {
                int filled = fluidHandler.fill(new FluidStack(currentFluidStack.getFluid(), amount), FluidAction.EXECUTE);
                //System.out.println(filled);
                toDrain += filled;
            }
        }
        this.drain(toDrain, FluidAction.EXECUTE).getAmount();
        if(currentFluidStack.isEmpty()) shouldTransfer = false;
    }

    protected int transfer(Direction side, int amount)
    {
        return 0;
    }

    private void scheduleTransfer()
    {
        updateConnectionsList();
        shouldTransfer = true;
    }

    private void updateConnectionsList()
    {
        connections = new ArrayList<>();
        for (Direction face : Direction.values())
        {
            TileEntity tileEntity = world.getTileEntity(pos.offset(face));
            if(tileEntity != null)
            {
                LazyOptional<IFluidHandler> cap = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face.getOpposite());
                if(cap.isPresent())
                {
                    IFluidHandler fluidHandler = cap.orElse(null);
                    if(fluidHandler != null)
                    {
                        connections.add(fluidHandler);
                    }
                }
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        super.write(compound);
        currentFluidStack.writeToNBT(compound);
        return compound;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt)
    {
        super.read(state, nbt);
        currentFluidStack = FluidStack.loadFluidStackFromNBT(nbt);
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action)
    {
        if(currentFluidStack == null || currentFluidStack.isEmpty())
        {
            int amount = Math.min(resource.getAmount(), internalVolume);
            if(action.execute())
            {
                currentFluidStack = new FluidStack(resource.getFluid(), amount);
            }
            return amount;
        }
        else if(resource.getFluid() == currentFluidStack.getFluid())
        {
            int result = resource.getAmount();
            int amount = resource.getAmount() + currentFluidStack.getAmount();
            if(amount > internalVolume)
            {
                amount = internalVolume;
                result = internalVolume - currentFluidStack.getAmount();
            }
            if(action.execute())
            {
                currentFluidStack.setAmount(amount);
            }
            return result;
        }

        return 0;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action)
    {
        if(currentFluidStack == null || currentFluidStack.isEmpty()) return FluidStack.EMPTY;
        else
        {
            int amount = Math.min(maxDrain, currentFluidStack.getAmount());
            if(action == FluidAction.EXECUTE)
            {
                currentFluidStack.setAmount(currentFluidStack.getAmount() - amount);
            }
            return new FluidStack(currentFluidStack.getFluid(), amount);
        }
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action)
    {
        if(currentFluidStack == null || currentFluidStack.isEmpty()) return FluidStack.EMPTY;
        else if(currentFluidStack.getFluid() != resource.getFluid()) return FluidStack.EMPTY;
        else
        {
            int amount = Math.min(resource.getAmount(), currentFluidStack.getAmount());
            if(action == FluidAction.EXECUTE)
            {
                currentFluidStack.setAmount(currentFluidStack.getAmount() - amount);
            }
            return new FluidStack(currentFluidStack.getFluid(), amount);
        }
    }

    @Nullable
    @Override
    public FluidStack getFluid() { return currentFluidStack; }

    @Override
    public int getFluidAmount() { return currentFluidStack.getAmount(); }

    @Override
    public int getCapacity() { return internalVolume; }

    @Override
    public boolean isFluidValid(FluidStack stack) { return currentFluidStack.isEmpty() || stack.getFluid() == currentFluidStack.getFluid(); }

    @Override
    public int getTanks() { return 1; }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) { return currentFluidStack; }

    @Override
    public int getTankCapacity(int tank) { return internalVolume; }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return isFluidValid(stack); }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability)
    {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
        {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
    {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
        {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, facing);
    }
}
