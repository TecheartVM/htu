package techeart.htu.utils;

import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import techeart.htu.objects.HTUFluidTank;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class HTUFluidHandler implements IFluidHandler
{
    ArrayList<FluidTank> tanks = new ArrayList<>();

    public HTUFluidHandler(FluidTank... internalTanks)
    {
        for (FluidTank tank : internalTanks)
        {
            tanks.add(tank);
        }
    }

    public void read(CompoundNBT compound)
    {
        for (int i = 0; i < tanks.size(); i++)
        {
            tanks.get(i).readFromNBT(compound.getCompound("Tank" + i));
        }
    }

    public CompoundNBT write(CompoundNBT compound)
    {
        for (int i = 0; i < tanks.size(); i++)
        {
            compound.put("Tank" + i, tanks.get(i).writeToNBT(new CompoundNBT()));
        }
        return compound;
    }

    @Override
    public int getTanks() { return tanks.size(); }

    public FluidTank getTank(int tank) { return tanks.get(tank); }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) { return tanks.get(tank).getFluid(); }

    @Override
    public int getTankCapacity(int tank) { return tanks.get(tank).getCapacity(); }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return tanks.get(tank).isFluidValid(stack); }

    @Override
    public int fill(FluidStack resource, FluidAction action)
    {
        for (FluidTank tank : tanks)
        {
            if(tank instanceof HTUFluidTank && ((HTUFluidTank) tank).getType() == HTUFluidTank.Type.EJECT_ONLY) continue;
            if(tank.isFluidValid(resource))
                return tank.fill(resource, action);
        }
        return 0;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action)
    {
        for (FluidTank tank : tanks)
        {
            if(tank instanceof HTUFluidTank && ((HTUFluidTank) tank).getType() == HTUFluidTank.Type.INSERT_ONLY) continue;
            if(tank.isFluidValid(resource))
                return tank.drain(resource, action);
        }
        return FluidStack.EMPTY;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action)
    {
        FluidStack result = FluidStack.EMPTY;
        for (FluidTank tank : tanks)
        {
            if(tank instanceof HTUFluidTank && ((HTUFluidTank) tank).getType() == HTUFluidTank.Type.INSERT_ONLY) continue;
            if(result.isEmpty() || tank.getFluid().getAmount() > result.getAmount())
                result = tank.drain(maxDrain, action);
        }
        return result;
    }

    public FluidStack drain(Fluid resource, int maxDrain, FluidAction action)
    {
        return drain(new FluidStack(resource, maxDrain), action);
    }

    /**Fills valid tank ignoring it's {@link techeart.htu.objects.HTUFluidTank.Type}*/
    public int forceFill(FluidStack resource, FluidAction action)
    {
        for (FluidTank tank : tanks)
        {
            if(tank.isFluidValid(resource))
                return tank.fill(resource, action);
        }
        return 0;
    }

    /**Drains valid tank ignoring it's {@link techeart.htu.objects.HTUFluidTank.Type}*/
    public FluidStack forceDrain(FluidStack resource, FluidAction action)
    {
        for (FluidTank tank : tanks)
        {
            if(tank.isFluidValid(resource))
                return tank.drain(resource, action);
        }
        return FluidStack.EMPTY;
    }
}
