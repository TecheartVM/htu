package techeart.htu.objects.tank;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import techeart.htu.utils.HTUTileEntityType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityFluidTank extends TileEntity
{
    public static final int CAPACITY = 8000;

    private float prevFluidAmount = 0;

    private final FluidTank internalVolume = new FluidTank(CAPACITY)
    {
        @Override
        protected void onContentsChanged()
        {
            super.onContentsChanged();
            TileEntityFluidTank.this.syncClient();
        }
    };

    public TileEntityFluidTank()
    {
        super(HTUTileEntityType.FLUID_TANK.get());
    }

    /*TileEntity*/
    @Override
    public void read(BlockState state, CompoundNBT nbt)
    {
        super.read(state, nbt);
        internalVolume.readFromNBT(nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt)
    {
        super.write(nbt);
        internalVolume.writeToNBT(nbt);
        return nbt;
    }

    public void syncClient()
    {
        if(getWorld() == null) return;
        if(getWorld().isRemote) return;
        SUpdateTileEntityPacket pkt = getUpdatePacket();
        ((ServerWorld) getWorld()).getChunkProvider().chunkManager.getTrackingPlayers(new ChunkPos(pos), false).forEach(p -> {
            if(pkt != null)
            {
                p.connection.sendPacket(pkt);
            }
        });
    }

    @Override
    public CompoundNBT getUpdateTag() { return write(new CompoundNBT()); }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        CompoundNBT compound = new CompoundNBT();
        write(compound);
        return new SUpdateTileEntityPacket(this.pos, 0, compound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) { read(this.world.getBlockState(pkt.getPos()), pkt.getNbtCompound()); }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) { read(state, tag); }

    /*ICapabilityProvider*/
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability)
    {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
        {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> (internalVolume)));
        }
        return super.getCapability(capability);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
    {
        if(facing == Direction.DOWN || facing == Direction.UP)
        {
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            {
                //TODO add an ability to lock tank
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> (internalVolume)));
            }
        }
        return super.getCapability(capability, facing);
    }

    /*fluid accessors*/
    @Nonnull
    public FluidStack getFluid() { return internalVolume.getFluid(); }

    public boolean isEmpty() { return internalVolume.isEmpty(); }

    public int getCapacity() { return CAPACITY; }

    public int fill(FluidStack resource, FluidAction action) { return internalVolume.fill(resource, action); }

    @Nonnull
    public FluidStack drain(FluidStack resource, FluidAction action) { return internalVolume.drain(resource, action); }

    @Nonnull
    public FluidStack drain(int maxDrain, FluidAction action) { return internalVolume.drain(maxDrain, action); }

    public float getLerpedFluidAmount()
    {
        float result = MathHelper.lerp(0.1f, prevFluidAmount, internalVolume.getFluid().getAmount());
        prevFluidAmount = result;
        return result;
    }
}
