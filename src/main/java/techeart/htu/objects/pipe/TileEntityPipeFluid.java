package techeart.htu.objects.pipe;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import techeart.htu.utils.HTUTileEntityType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class TileEntityPipeFluid extends TileEntity implements IFluidHandler, IPipe
{
    public static int CAPACITY = FluidAttributes.BUCKET_VOLUME;
    public static int TRANSFER_RATE = 125;

    private HorizontalPipeGrid grid = null;

    public TileEntityPipeFluid()
    {
        super(HTUTileEntityType.PIPE_FLUID.get());
    }

    /*IPipe*/
    @Override
    public IPipeGrid getGrid() { return grid; }

    @Override
    public void setGrid(IPipeGrid grid)
    {
        if(!(grid instanceof HorizontalPipeGrid)) return;
        this.grid = (HorizontalPipeGrid)grid;
        for (PipeConnectionFluid c : getConnections())
        {
            if(!c.isPipe()) { ((HorizontalPipeGrid) grid).addExternal(c); }
            else if(c.side.getAxis() != Direction.Axis.Y) grid.addPipe((IPipe) c.tile);
            else ((HorizontalPipeGrid) grid).addExternal(c);
        }
        this.grid.recalculateCapacity();
    }

    @Override
    public void createGrid() {
        System.out.println("Created grid!"); new HorizontalPipeGrid().addPipe(this); }

    @Override
    public void updateGrid() { setGrid(grid); }

    @Override
    public Set<PipeConnectionFluid> getConnections()
    {
        if(world == null) return null;
        Set<PipeConnectionFluid> result = new HashSet<>();
        for (Direction side : Direction.values())
        {
            TileEntity te = world.getTileEntity(pos.offset(side));
            if(te == null) continue;
            LazyOptional<IFluidHandler> lo = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite());
            IFluidHandler fh = lo.orElse(null);
            if(lo.isPresent()) result.add(new PipeConnectionFluid(te, fh, side));
        }
        return result;
    }

    /*TileEntity*/
    @Override
    public void remove()
    {
        if(grid != null)
        {
            FluidStack gridPerPipeContent = grid.getFluidPerPipe();
            grid.destroy();
            for (PipeConnectionFluid c : getConnections())
            {
                if (!c.isPipe()) continue;
                if (c.side.getAxis() == Direction.Axis.Y) continue;
                ((TileEntityPipeFluid) c.tile).createGrid();
                HorizontalPipeGrid newGrid = (HorizontalPipeGrid) ((TileEntityPipeFluid) c.tile).getGrid();
                newGrid.fill(new FluidStack(gridPerPipeContent.getFluid(), gridPerPipeContent.getAmount() * newGrid.getPipes().size()), FluidAction.EXECUTE);
            }
        }

        super.remove();
    }

    /*IFluidHandler*/
    @Override
    public int getTanks() { return 1; }

    @Override
    public int getTankCapacity(int tank) { return CAPACITY; }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) { return ((HorizontalPipeGrid)getGrid()).getFluidPerPipe(); }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return ((HorizontalPipeGrid)getGrid()).isFluidValid(stack); }

    @Override
    public int fill(FluidStack resource, FluidAction action) { return ((HorizontalPipeGrid)getGrid()).fill(resource, action); }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) { return ((HorizontalPipeGrid)getGrid()).drain(resource, action); }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) { return ((HorizontalPipeGrid)getGrid()).drain(maxDrain, action); }

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
}
