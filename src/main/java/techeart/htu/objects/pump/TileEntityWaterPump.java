package techeart.htu.objects.pump;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;
import techeart.htu.objects.HTUFluidTank;
import techeart.htu.utils.HTUFluidHandler;
import techeart.htu.utils.HTUTileEntityType;
import techeart.htu.utils.RegistryHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityWaterPump extends TileEntity implements ITickableTileEntity, IFluidHandler
{
    protected static final int internalVolumeWater = 4000;
    protected static final int internalVolumeSteam = 4000;
    protected static final int pumpRate = 10;
    protected static final int steamConsumptionRate = 10;

    //fluid constant links
    private static final Fluid WATER = ForgeRegistries.FLUIDS.getValue(new ResourceLocation("minecraft:water")).getFluid();
    private static final Fluid STEAM = RegistryHandler.FLUID_STEAM.get();

    private final HTUFluidHandler fluidHandler;
    private final LazyOptional<IFluidHandler> fluidCapWater;
    private final LazyOptional<IFluidHandler> fluidCapSteam;

    public TileEntityWaterPump()
    {
        super(HTUTileEntityType.WATER_PUMP.get());

        fluidHandler = new HTUFluidHandler(
                new HTUFluidTank(internalVolumeWater, WATER, HTUFluidTank.Type.EJECT_ONLY),
                new HTUFluidTank(internalVolumeSteam, STEAM, HTUFluidTank.Type.INSERT_ONLY)
        );

        fluidCapWater = LazyOptional.of(() -> fluidHandler.getTank(0));
        fluidCapSteam = LazyOptional.of(() -> fluidHandler.getTank(1));
    }

    @Override
    public void tick()
    {
        if (!this.world.isRemote)
        {
            if (this.world.getBlockState(this.pos.down()) == Blocks.WATER.getDefaultState())
                fluidHandler.forceFill(new FluidStack(WATER, pumpRate), FluidAction.EXECUTE);

            //TODO: optimization
            int waterAmount = fluidHandler.getFluidInTank(0).getAmount();
            if(waterAmount > 0)
            {
                for (Direction face : Direction.values())
                {
                    if(face != Direction.DOWN)
                    {
                        TileEntity tileEntity = world.getTileEntity(pos.offset(face));
                        if(tileEntity != null)
                        {
                            LazyOptional<IFluidHandler> cap = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face.getOpposite());
                            if(cap.isPresent())
                            {
                                IFluidHandler fluidHandler = cap.orElse(null);
                                if(fluidHandler != null)
                                {
                                    int toDrain = fluidHandler.fill(new FluidStack(WATER, waterAmount), FluidAction.EXECUTE);
                                    drain(toDrain, FluidAction.EXECUTE);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt)
    {
        super.read(state, nbt);
        this.fluidHandler.read(nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        super.write(compound);
        this.fluidHandler.write(compound);
        return compound;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability)
    {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
        {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, fluidCapWater);
        }
        return super.getCapability(capability);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
    {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
        {
            if(facing == Direction.UP)
            {
                return (LazyOptional<T>) fluidCapWater;
            }
            else if(facing != Direction.DOWN)
            {
                return (LazyOptional<T>) fluidCapSteam;
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public int getTanks() { return 2; }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) { return fluidHandler.getFluidInTank(tank); }

    @Override
    public int getTankCapacity(int tank) { return fluidHandler.getTankCapacity(tank); }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return fluidHandler.isFluidValid(tank, stack); }

    @Override
    public int fill(FluidStack resource, FluidAction action) { return fluidHandler.fill(resource, action); }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) { return drain(resource, action); }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) { return fluidHandler.getTank(0).drain(maxDrain, action); }
}
