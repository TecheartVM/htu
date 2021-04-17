package techeart.htu.objects.pipe;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import techeart.htu.utils.HTUTileEntityType;

import java.util.ArrayList;
import java.util.List;

public class TileEntityGridTile extends TileEntity implements IPipe
{
    private PipeGridFluid grid;

    public TileEntityGridTile() { this(HTUTileEntityType.GRID_TILE.get()); }

    public TileEntityGridTile(TileEntityType<TileEntityGridTile> type) { super(type); }

    public TileEntityGridTile(PipeGridFluid grid)
    {
        super(HTUTileEntityType.GRID_TILE.get());
        setGrid(grid);
    }

    @Override
    public boolean setGrid(PipeGridFluid grid)
    {
        this.grid = grid;
        return true;
    }

    @Override
    public PipeGridFluid getGrid() { return grid; }

    @Override
    public List<IPipe> getAdjacentPipes()
    {
        List<IPipe> result = new ArrayList<>();
        for (Direction dir : Direction.values())
        {
            TileEntity tile = this.world.getTileEntity(this.pos.offset(dir));
            if(tile instanceof IPipe)
            {
                result.add((IPipe)tile);
            }
        }
        return result;
    }

//    public IFluidHandler getAdjacentFluidHandler(Direction dir)
//    {
//        TileEntity tile = this.world.getTileEntity(this.pos.offset(dir));
//        if(tile != null)
//        {
//            LazyOptional<IFluidHandler> cap = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite());
//            if(cap.isPresent())
//            {
//                return cap.orElse(null);
//            }
//        }
//        return null;
//    }
//
//    public List<IFluidHandler> getAdjacentFluidHandlers()
//    {
//        List<IFluidHandler> result = new ArrayList<>();
//        for (Direction dir : Direction.values())
//        {
//            IFluidHandler handler = getAdjacentFluidHandler(dir);
//            if(handler != null) result.add(handler);
//        }
//        return result;
//    }

    @Override
    public boolean canBeConnectedTo(Direction side)
    {
        TileEntity tile = this.world.getTileEntity(this.pos.offset(side));
        if(tile == null) return false;
        //TODO pipe side blocking logic can be placed here
        if(tile instanceof IPipe) return true;
        return tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite()).isPresent();
    }
}
