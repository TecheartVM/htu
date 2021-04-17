package techeart.htu.objects.pipe;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class PipeGridFluid
{
    protected List<IPipe> pipes = new ArrayList<>();
    //protected List<IFluidHandler> externalHandlers = new ArrayList<>();

    public boolean addPipe(IPipe pipe)
    {
        return pipes.add(pipe);
    }

    public boolean removePipe(IPipe pipe)
    {
        return pipes.remove(pipe);
    }

    public void mergeWith(PipeGridFluid grid)
    {

    }

    public void refresh()
    {

    }

    public void tick()
    {

    }

    public void init(World world, IPipe firstPipe, BlockPos pos)
    {
        if(world.isRemote) throw new UnsupportedOperationException();

        for (Direction dir : Direction.values())
        {
            if(!firstPipe.canBeConnectedTo(dir)) continue;

            TileEntity tile = world.getTileEntity(pos.offset(dir));
            if(tile instanceof IPipe)
            {
                ((IPipe)tile).getGrid().remove();
            }
        }

        createGrid(firstPipe);
    }

    public void createGrid(IPipe firstPipe)
    {
        List<IPipe> gridPipes = new ArrayList<>();
        gridPipes.add(firstPipe);

        while(!gridPipes.isEmpty())
        {
            //taking the first pipe in list of potential grid pipes
            IPipe curPipe = gridPipes.remove(0);
            if(curPipe == null) continue;

            //checking if pipe already has a grid (removing it if true)
            PipeGridFluid curPipeGrid = curPipe.getGrid();
            if(curPipeGrid == this) continue;
            else if(curPipeGrid != null) curPipeGrid.remove();

            //setting the pipe grid to this
            if(curPipe.setGrid(this))
            {
                addPipe(curPipe);
                //adding all pipes adjacent to current pipe to the list
                //to do the same procedure
                //this helps to include all of connected pipes to the grid
                gridPipes.addAll(curPipe.getAdjacentPipes());
            }
        }
    }

    public void remove()
    {
        for (IPipe pipe : pipes)
        {
            pipe.clearGrid();
        }
        pipes.clear();
    }
}
