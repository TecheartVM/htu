package techeart.htu.objects.pipe;

import net.minecraft.util.Direction;

import java.util.List;

public interface IPipe
{
    boolean canBeConnectedTo(Direction side);

    List<IPipe> getAdjacentPipes();

    PipeGridFluid getGrid();

    boolean setGrid(PipeGridFluid grid);

    default void clearGrid() { setGrid(null); };
}
