package techeart.htu.objects.pipes;

import com.google.common.collect.Maps;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
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
import java.util.Map;

public class TileEntityPipeFluid extends TileEntity implements IFluidHandler, IPipe, ITickableTileEntity
{
    protected static final int transferRate = 125;
    protected static final int internalVolume = 1000;
    private final FluidTank internalTank;

    private PipeNetworkFluid network = null;
    private final Map<Direction, IFluidHandler> connections = Maps.newHashMap();

    public TileEntityPipeFluid()
    {
        super(HTUTileEntityType.PIPE_FLUID.get());
        internalTank = new FluidTank(internalVolume);
    }

    /*TileEntity*/
    @Override
    public void tick()
    {
        if(internalTank.isEmpty())return;
        if(connections.isEmpty()) return;

        int fluidLeft = internalTank.getFluidAmount();
        Map<IFluidHandler, Integer> equivalentReceivers = Maps.newHashMap();
        int fluidAround = 0;

        for (Map.Entry<Direction, IFluidHandler> e : connections.entrySet())
        {
            IFluidHandler fh = e.getValue();
            if(fh == null)
            {
                updateConnections();
                return;
            }

            if(e.getKey() == Direction.DOWN)
            {
                fluidLeft -= fh.fill(internalTank.getFluid(), FluidAction.EXECUTE);
                if(fluidLeft <= 0)
                {
                    drain(internalTank.getFluidAmount(), FluidAction.EXECUTE);
                    return;
                }
            }
            else if(e.getKey() != Direction.UP)
            {
                if(fh.fill(internalTank.getFluid(), FluidAction.SIMULATE) > 0)
                {
                    if(fh instanceof IPipe)
                    {
                        //System.out.println("Found pipe at the side: " + e.getKey());
                        int fluidAmount = fh.drain(new FluidStack(internalTank.getFluid(), internalVolume + 1), FluidAction.SIMULATE).getAmount();
                        if (fluidAmount < this.getFluidInTank(0).getAmount())
                        {
                            equivalentReceivers.put(fh, fluidAmount);
                        }
                        fluidAround += fluidAmount;
                    }
                    else
                    {
                        //System.out.println("Found external tank at the side: " + e.getKey());
                        equivalentReceivers.put(fh, 0);
                    }
                }
                //else System.out.println("No empty space in tank at side: " + e.getKey());
            }
        }

        //System.out.println("Fluid left: " + fluidLeft);
        //System.out.println("Valid receivers count: " + equivalentReceivers.size());

        if(equivalentReceivers.isEmpty())
        {
            if(fluidLeft != internalTank.getFluidAmount())
                //System.out.println("Drained to bottom: " + drain(internalTank.getFluidAmount() - fluidLeft, FluidAction.EXECUTE).getAmount());
                drain(internalTank.getFluidAmount() - fluidLeft, FluidAction.EXECUTE);
            return;
        }

        fluidAround += internalTank.getFluid().getAmount();
        int average = Math.floorDiv(fluidAround, equivalentReceivers.size() + 1);
        for (Map.Entry<IFluidHandler, Integer> e : equivalentReceivers.entrySet())
        {
            if(e.getValue() > average) continue;
            int filled = e.getKey().fill(new FluidStack(getFluidInTank(0), average - e.getValue()), FluidAction.EXECUTE);
            fluidLeft -= filled;
            //System.out.println("Filled side tank: " + filled);
        }
        int toDrain = internalTank.getFluidAmount() - fluidLeft;
        //System.out.println("Drained: " + drain(internalTank.getFluidAmount() - fluidLeft, FluidAction.EXECUTE).getAmount());
        drain(toDrain, FluidAction.EXECUTE);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        super.write(compound);
        internalTank.writeToNBT(compound);
        return compound;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt)
    {
        super.read(state, nbt);
        internalTank.readFromNBT(nbt);
        updateConnections();
    }

    /*IPipe*/
    @Override
    public void update()
    {

    }

    @Override
    public void updateConnections()
    {
        //System.out.println("Connections updated!");
        //new Exception().printStackTrace();
        connections.clear();
        for(Direction side : Direction.values())
        {
            IFluidHandler fh = getConnection(side);
            if(fh != null) connections.put(side, fh);
        }
    }

    @Override
    public IFluidHandler getConnection(Direction side)
    {
        TileEntity te = world.getTileEntity(pos.offset(side));
        if(te == null) return null;
        LazyOptional<IFluidHandler> lo = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite());
        if(!lo.isPresent()) return null;
        return lo.orElse(null);
    }

    @Override
    public Map<Direction, IFluidHandler> getConnections() { return connections; }

    @Override
    public PipeNetworkFluid getNetwork() { return network; }

    @Override
    public boolean setNetwork(PipeNetworkFluid network)
    {
        this.network = network;
        return true;
    }

    /*ICapabilityProvider*/
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap)
    {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> this));

        return super.getCapability(cap);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> this));

        return super.getCapability(cap, side);
    }

    /*IFluidHandler*/
    @Override
    public int getTanks() { return 1; }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) { return internalTank.getFluid(); }

    @Override
    public int getTankCapacity(int tank) { return internalVolume; }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return internalTank.isFluidValid(tank, stack); }

    @Override
    public int fill(FluidStack resource, FluidAction action)
    {
        int result = internalTank.fill(resource, action);
        if(action.execute() && result > 0)
        {
            //System.out.println("Filled: " + result);
            markDirty();
        }
        return result;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action)
    {
        FluidStack result = internalTank.drain(resource, action);
        if(action.execute() && !result.isEmpty())
        {
            //System.out.println("Drained: " + result.getAmount());
            markDirty();
        }
        return result;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action)
    {
        FluidStack result = internalTank.drain(maxDrain, action);
        if(action.execute() && !result.isEmpty())
        {
            //System.out.println("Drained: " + result.getAmount());
            markDirty();
        }
        return result;
    }
}
