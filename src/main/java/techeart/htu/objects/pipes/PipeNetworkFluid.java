package techeart.htu.objects.pipes;

import com.google.common.collect.Maps;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.EnumSet;
import java.util.Map;

public class PipeNetworkFluid
{
    private final Map<BlockPos, IFluidHandler> externalHandlers = Maps.newHashMap();
    private final Map<BlockPos, EnumSet<Direction>> externalHandlersSides = Maps.newHashMap();
    private final Map<BlockPos, IPipe> pipes = Maps.newHashMap();

    public void tick()
    {

    }

    public int transfer(FluidStack toSend)
    {

        return 0;
    }
}
