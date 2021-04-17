package techeart.htu.objects;

import net.minecraft.fluid.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.function.Predicate;

public class HTUFluidTank extends FluidTank
{
    private Type type;

    public HTUFluidTank(int capacity, Type type)
    {
        super(capacity);
        this.type = type;
    }

    public HTUFluidTank(int capacity, Predicate<FluidStack> validator, Type type)
    {
        super(capacity, validator);
        this.type = type;
    }

    public HTUFluidTank(int capacity, Fluid acceptedFluid, Type type) { this(capacity, content -> content.getFluid().equals(acceptedFluid), type); }
    public HTUFluidTank(int capacity) { this(capacity, Type.DEFAULT); }
    public HTUFluidTank(int capacity, Predicate<FluidStack> validator) { this(capacity, validator, Type.DEFAULT); }
    public HTUFluidTank(int capacity, Fluid acceptedFluid) { this(capacity, content -> content.getFluid().equals(acceptedFluid), Type.DEFAULT); }

    public Type getType() { return type; }

    public void setType(Type type) { this.type = type; }

    public enum Type
    {
        DEFAULT,
        INSERT_ONLY,
        EJECT_ONLY
    }
}
