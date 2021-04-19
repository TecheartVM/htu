package techeart.htu.objects.pipe;

import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.*;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import techeart.htu.MainClass;
import techeart.htu.utils.ISaveable;
import techeart.htu.utils.NBTHandler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HorizontalPipeGrid implements IPipeGrid, ISaveable
{
    private final Set<TileEntityPipeFluid> pipes = new HashSet();
    private final Set<PipeConnectionFluid> externals = new HashSet<>();

    private final FluidTank pipesContent = new FluidTank(0);

    private boolean dirty = false;

    public HorizontalPipeGrid()
    {
        pipesContent.setValidator(fluidStack -> (fluidStack.isEmpty() || pipesContent.isEmpty() || pipesContent.getFluid().isFluidEqual(fluidStack)));
        MainClass.registerPipeGrid(this);
        new NBTHandler().addSubscriber(this);
    }

    public void addExternal(PipeConnectionFluid connection) { externals.add(connection); }

    public Set<TileEntityPipeFluid> getPipes() { return pipes; }
    public Set<PipeConnectionFluid> getExternals() { return externals; }

    /*grid tank control methods*/
    public int getCapacity() { return pipesContent.getCapacity(); }

    public FluidStack getContent() { return pipesContent.getFluid(); }

    public void recalculateCapacity()
    {
        int newCapacity = TileEntityPipeFluid.CAPACITY * pipes.size();
        pipesContent.setCapacity(newCapacity);
    }

    public boolean isFluidValid(FluidStack fluid) { return pipesContent.isFluidValid(fluid); }

    public FluidStack getFluidPerPipe()
    {
        if(pipes.isEmpty())
        {
            System.out.println("No pipes in grid!");
            return FluidStack.EMPTY;
        }
        return new FluidStack(pipesContent.getFluid(), Math.floorDiv(pipesContent.getFluidAmount(), pipes.size()));
    }

    public int fill(FluidStack resource, FluidAction action)
    {
        int result = pipesContent.fill(resource, action);
        if(result > 0)
        {
            if(action.execute()) markDirty();
            return result;
        }
        return transferUp(resource, action);
    }

    public FluidStack drain(FluidStack resource, FluidAction action)
    {
        FluidStack result = pipesContent.drain(resource, action);
        if(!result.isEmpty() && action.execute()) markDirty();
        return result;
    }
    public FluidStack drain(int maxDrain, FluidAction action)
    {
        FluidStack result = pipesContent.drain(maxDrain, action);
        if(!result.isEmpty() && action.execute()) markDirty();
        return result;
    }

    /*fluid transfer methods*/
    private int transferAround()
    {
        int result = 0;
        Map<IFluidHandler, Integer> bottomConnections = Maps.newHashMap();
        Set<IFluidHandler> sideConnections = new HashSet();
        for (PipeConnectionFluid c : externals)
        {
            int toFill = c.fluidHandler.fill(pipesContent.getFluid(), FluidAction.SIMULATE);
            result += toFill;
            if(toFill > 0)
            {
                if(c.side == Direction.DOWN) bottomConnections.put(c.fluidHandler, toFill);
                else if(c.side != Direction.UP) sideConnections.add(c.fluidHandler);
            }
        }

        if(result > 0 && result <= bottomConnections.size())
        {
            int remainder = result;
            for (Map.Entry<IFluidHandler, Integer> entry : bottomConnections.entrySet())
            {
                entry.getKey().fill(new FluidStack(pipesContent.getFluid().getFluid(), 1), FluidAction.EXECUTE);
                remainder--;
                if(remainder <= 0) break;
            }
        }
        else
        {
            int total = Math.min(result, pipesContent.getFluidAmount());
            int unclamped = result;
            result = 0;

            for (Map.Entry<IFluidHandler, Integer> entry : bottomConnections.entrySet())
            {
                float f = unclamped / (float) entry.getValue();
                int toFill = Math.min((int) Math.floor((float) total / f), TileEntityPipeFluid.TRANSFER_RATE);
                result += entry.getKey().fill(new FluidStack(pipesContent.getFluid().getFluid(), toFill), FluidAction.EXECUTE);
            }
        }

        if(result >= pipesContent.getFluidAmount()) return result;
        int remainder = pipesContent.getFluidAmount() - result;

        for (IFluidHandler fh : sideConnections)
        {
            int filled = fh.fill(new FluidStack(pipesContent.getFluid().getFluid(), Math.min(TileEntityPipeFluid.TRANSFER_RATE, remainder)), FluidAction.EXECUTE);
            result += filled;
            remainder -= filled;
            if(remainder <= 0) break;
        }

        if(result > 0) markDirty();
        return Math.min(result, pipesContent.getFluidAmount());
    }

    /**Used in fill method (only if grid is full).*/
    private int transferUp(FluidStack resource, FluidAction action)
    {
        int result = 0;
        Set<IFluidHandler> connections = new HashSet<>();
        for (PipeConnectionFluid c : externals)
        {
            if(c.side == Direction.UP)
            {
                int filled = c.fluidHandler.fill(resource, FluidAction.SIMULATE);
                if(filled > 0)
                {
                    result += filled;
                    connections.add(c.fluidHandler);
                }
            }
        }

        if(result == 0 || connections.isEmpty()) return 0;

        result = Math.min(result, resource.getAmount());
        int toFill = Math.floorDiv(result, connections.size());
        int remainder = result % connections.size();
        result = 0;
        for (IFluidHandler fh : connections)
        {
            result += fh.fill(new FluidStack(resource.getFluid(), toFill + remainder), action);
            if(remainder > 0) remainder--;
        }

        if(result > 0 && action.execute()) markDirty();
        return result;
    }

    /*IPipeGrid*/
    @Override
    public boolean isEmpty() { return pipesContent.isEmpty(); }

    @Override
    public boolean isFull() { return pipesContent.getFluidAmount() >= pipesContent.getCapacity(); }

    @Override
    public void tick()
    {
        if(pipesContent.isEmpty()) return;

        int toDrain = transferAround();
        if(toDrain > 0)
            drain(toDrain, FluidAction.EXECUTE);
    }

    @Override
    public boolean addPipe(IPipe pipe)
    {
        HorizontalPipeGrid pipeGrid = (HorizontalPipeGrid) pipe.getGrid();
        if(pipe instanceof TileEntityPipeFluid && pipeGrid != this)
        {
            if(pipeGrid == null || pipeGrid.isEmpty())
            {
                boolean result = pipes.add((TileEntityPipeFluid) pipe);
                if (result)
                {
                    if(pipeGrid != null) pipeGrid.destroy();
                    pipe.setGrid(this);
                    markDirty();
                }
                return result;
            }
            else if(((TileEntityPipeFluid) pipe).isFluidValid(0, getFluidPerPipe()))
            {
                mergeWith(pipeGrid);
                return true;
            }
        }
        return false;
    }

    protected void mergeWith(IPipeGrid grid)
    {
        FluidStack toFill = ((HorizontalPipeGrid)grid).getContent().copy();
        externals.addAll(((HorizontalPipeGrid) grid).getExternals());
        Set<TileEntityPipeFluid> gridPipes = new HashSet<>(((HorizontalPipeGrid) grid).getPipes());
        grid.destroy();
        gridPipes.forEach(pipe -> {
            if(pipes.add(pipe)) pipe.setGrid(this);
        });
        recalculateCapacity();
        fill(toFill, FluidAction.EXECUTE);
        markDirty();
    }

    @Override
    public void markDirty() { dirty = true; }

    @Override
    public boolean isDirty() { return dirty; }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT nbt)
    {
        System.out.println("Writing!");
        pipesContent.writeToNBT(nbt);

        dirty = false;
        return nbt;
    }

    @Override
    public void readFromNBT(CompoundNBT nbt)
    {
        System.out.println("Reading!");
        pipesContent.readFromNBT(nbt);
    }

    @Override
    public FluidStack destroy()
    {
        pipes.clear();
        externals.clear();
        MainClass.unregisterPipeGrid(this);
        return pipesContent.drain(pipesContent.getCapacity(), FluidAction.EXECUTE).copy();
    }
}
