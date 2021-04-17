package techeart.htu.objects.pipes;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.capability.IFluidHandler;
import techeart.htu.objects.pipe.PipeGridFluid;

import java.util.List;
import java.util.Map;

public interface IPipe
{
    IFluidHandler getConnection(Direction side);

    Map<Direction, IFluidHandler> getConnections();

    void updateConnections();

    void update();

    PipeNetworkFluid getNetwork();

    boolean setNetwork(PipeNetworkFluid network);

    default void clearNetwork() { setNetwork(null); };
}
