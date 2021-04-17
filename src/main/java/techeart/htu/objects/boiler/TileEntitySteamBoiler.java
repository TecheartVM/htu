package techeart.htu.objects.boiler;

import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;
import techeart.htu.MainClass;
import techeart.htu.objects.HTUFluidTank;
import techeart.htu.utils.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class TileEntitySteamBoiler extends TileEntity implements IInventory, INamedContainerProvider, ITickableTileEntity, IFluidHandler
{
    private Random random = new Random();

    //internal tanks volume constants in mB
    public static final int internalVolumeWater = 4000;
    public static final int internalVolumeSteam = 4000;

    //temperature constants
    public static final int maxTemperature = 1200;
    public static final int minTemperature = -30;
    public static final int conversionTemperature = 100;

    //pressure constants *10
    public static final int maxPressure = 640;
    public static final int initialPressure = 40;
    public static final int ejectionPressure = 560;

    public static final int waterConsumptionRate = 1;
    public static final int conversionFactor = 3;

    //tracked fields
    private int burnTime;
    private int burnTimeTotal;
    private int waterAmount;
    private int steamAmount;
    private int temperature;
    private int pressure;

    private int fuelBurnTemperature;
    private int ambientTemperature = 16;

    /**Chance in percents to extinct the burning fire*/
    public static final int EXTINCTION_CHANCE = 5;

    private boolean ignited = false;
    protected void setIgnited(boolean value)
    {
        if(ignited != value)
        {
            ignited = value;
            this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(AbstractFurnaceBlock.LIT, this.isBurning()), 3);
            markDirty();
        }
    }

    private ITextComponent customName;

    //fluid constant links
    private static final Fluid WATER = ForgeRegistries.FLUIDS.getValue(new ResourceLocation("minecraft:water")).getFluid();
    private static final Fluid STEAM = RegistryHandler.FLUID_STEAM.get();

    private HTUFluidHandler fluidHandler;

    private NonNullList<ItemStack> inventory = NonNullList.<ItemStack>withSize(1, ItemStack.EMPTY);

    public TileEntitySteamBoiler()
    {
        super(HTUTileEntityType.STEAM_BOILER.get());

        int steamVolume = internalVolumeSteam + (Math.floorDiv(internalVolumeSteam, ejectionPressure) * (maxPressure - ejectionPressure));
        fluidHandler = new HTUFluidHandler(
                new HTUFluidTank(internalVolumeWater, WATER),
                new HTUFluidTank(steamVolume, STEAM)
        );

        if(world != null && pos != null)
            ambientTemperature = HTUHooks.getAmbientTemperature(world, pos);
        temperature = ambientTemperature;
        pressure = initialPressure;
    }

    @Override
    public void tick()
    {
        boolean flag = false;

        if (this.isBurning()) { --this.burnTime; }

        if (!this.world.isRemote)
        {
            //TODO: optimization
            if(pressure > initialPressure)
            {
                for (Direction face : Direction.values())
                {
                    if(face != Direction.UP && face != Direction.DOWN)
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
                                    int toDrain = fluidHandler.fill(new FluidStack(STEAM, steamAmount), FluidAction.EXECUTE);
                                    drain(toDrain, FluidAction.EXECUTE);
                                }
                            }
                        }
                    }
                }
            }

            if(ignited && !isBurning())
            {
                if(random.nextInt(100) < 99 - EXTINCTION_CHANCE) ignite();
                else setIgnited(false);
                flag = true;
            }

            if (this.isBurning())
            {
                if(temperature < maxTemperature && temperature < fuelBurnTemperature)
                    this.temperature++;
            }
            else if(temperature > ambientTemperature) temperature--;
            else if(temperature < ambientTemperature) temperature = ambientTemperature;

            boolean canEjectSteam = !this.world.getBlockState(this.pos.up(2)).isSolid();
            if(waterAmount > 0)
            {
                if (this.temperature >= conversionTemperature)
                {
                    int drained = this.drain(new FluidStack(WATER, waterConsumptionRate), FluidAction.EXECUTE).getAmount();
                    int toFill = drained * conversionFactor;
                    int filled = this.fill(new FluidStack(STEAM, toFill), FluidAction.EXECUTE);

                    if(filled < toFill)
                    {
                        if(!canEjectSteam)
                        {
                            pressure++;
                            if (pressure > maxPressure)
                            {
                                this.world.createExplosion(
                                        (Entity) null,
                                        this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D, 2.0F,
                                        Explosion.Mode.DESTROY);
                            }
                        }
                        else
                        {
                            this.world.addBlockEvent(this.pos, world.getBlockState(pos).getBlock(), 0, 0);
                        }
                    }
                    else
                    {
                        int steamToPressureCoeff = internalVolumeSteam / ejectionPressure;
                        pressure = MathHelper.clamp((this.steamAmount / steamToPressureCoeff), initialPressure, ejectionPressure);
                    }
                }
            }
            else
            {
                if(pressure > ejectionPressure && canEjectSteam)
                {
                    pressure--;
                    this.world.addBlockEvent(this.pos, world.getBlockState(pos).getBlock(), 0, 0);
                }
                else
                {
                    int steamToPressureCoeff = internalVolumeSteam / ejectionPressure;
                    pressure = MathHelper.clamp((this.steamAmount / steamToPressureCoeff), initialPressure, ejectionPressure);
                }
            }
        }

        if (flag)
        {
            this.markDirty();
        }

//        System.out.println("======================================================");
//        System.out.println("Water: " + this.getFluidAmount(0));
//        System.out.println("Steam: " + this.getFluidAmount(1));
//        System.out.println("Temp: " + this.temperature);
//        System.out.println("Pressure: " + this.pressure);
//        System.out.println("Burn time: " + this.burnTime);
//        System.out.println("Can eject steam: " + canEjectSteam);
    }

    public void ignite()
    {
        if(!world.isRemote)
        {
            ItemStack fuel = this.inventory.get(0);
            if (!fuel.isEmpty())
            {
                if (!isBurning())
                {
                    this.burnTimeTotal = getItemBurnTime(fuel);
                    this.burnTime = this.burnTimeTotal;
                    this.fuelBurnTemperature = FuelTemperatures.getBurnTemperature(fuel);
                    if (this.isBurning())
                    {
                        setIgnited(true);
                        if (fuel.hasContainerItem()) this.inventory.set(0, fuel.getContainerItem());
                        else
                        {
                            fuel.shrink(1);
                            if (fuel.isEmpty()) this.inventory.set(0, fuel.getContainerItem());
                        }

                        markDirty();
                    }
                }
            }
            else setIgnited(false);
        }
    }



    public int getFluidAmount(int index)
    {
        if(index == 0 || index == 1) return fluidHandler.getFluidInTank(index).getAmount();
        else return -1;
    }

    @Override
    public boolean receiveClientEvent(int id, int type)
    {
        if(id == 0)
        {
            if (this.world.isRemote)
                world.addParticle(ParticleTypes.CLOUD, this.pos.getX() + 0.5D, this.pos.getY() + 1, this.pos.getZ() + 0.5D, 0, 0.2D, 0);


            return true;
        }
        return false;
    }

    @Override
    public void read(BlockState state, CompoundNBT compound)
    {
        super.read(state, compound);

        this.burnTime = compound.getInt("BurnTime");
        this.burnTimeTotal = compound.getInt("BurnTimeTotal");
        this.temperature = compound.getInt("Temperature");
        this.pressure = compound.getInt("Pressure");

        this.ignited = compound.getBoolean("Ignited");
        this.fuelBurnTemperature = compound.getInt("FuelTemperature");

        this.inventory = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, this.inventory);

        this.fluidHandler.read(compound);
        waterAmount = fluidHandler.getFluidInTank(0).getAmount();
        steamAmount = fluidHandler.getFluidInTank(1).getAmount();
    }

    @Override
    public CompoundNBT write(CompoundNBT compound)
    {
        super.write(compound);

        compound.putInt("BurnTime", (short)this.burnTime);
        compound.putInt("BurnTimeTotal", (short)this.burnTimeTotal);
        compound.putInt("Temperature", (short)this.temperature);
        compound.putInt("Pressure", (short)this.pressure);

        compound.putBoolean("Ignited", this.ignited);
        compound.putInt("FuelTemperature", this.fuelBurnTemperature);

        ItemStackHelper.saveAllItems(compound, this.inventory);

        this.fluidHandler.write(compound);

        return compound;
    }

    @Override
    public CompoundNBT getUpdateTag() { return write(new CompoundNBT()); }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) { read(state, tag); }

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

    private static int getItemBurnTime(ItemStack stack)
    {
        return ForgeHooks.getBurnTime(stack);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) { return getItemBurnTime(stack) > 0; }

    public int getField(int id)
    {
        int field = -1;
        switch (id)
        {
            case 0: field = this.burnTime;
                break;
            case 1: field = this.burnTimeTotal;
                break;
            case 2: field = this.waterAmount;
                break;
            case 3: field = this.steamAmount;
                break;
            case 4: field = this.temperature;
                break;
            case 5: field = this.pressure;
                break;
        }
        return field;
    }

    public void setField(int id, int value)
    {
        switch (id)
        {
            case 0: this.burnTime = value;
                break;
            case 1: this.burnTimeTotal = value;
                break;
            case 2: this.waterAmount = value;
                break;
            case 3: this.steamAmount = value;
                break;
            case 4: this.temperature = value;
                break;
            case 5: this.pressure = value;
                break;
        }
    }

    public int getFieldCount() { return 6; }

    public boolean isBurning()
    {
        return this.burnTime > 0;
    }

    public ITextComponent getDefaultName() { return new TranslationTextComponent("container." + MainClass.MODID + ".steam_boiler"); }

    public void setCustomName(ITextComponent name) { this.customName = name; }

    @Override
    public int getSizeInventory()
    {
        return this.inventory.size();
    }

    @Override
    public boolean isEmpty()
    {
        return inventory.get(0).isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        return this.inventory.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) { return ItemStackHelper.getAndSplit(this.inventory, index, count); }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        return ItemStackHelper.getAndRemove(this.inventory, index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        this.inventory.set(index, stack);
        if(stack.getCount() > this.getInventoryStackLimit()) stack.setCount(this.getInventoryStackLimit());
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player)
    {
        return this.world.getTileEntity(this.pos) == this &&
                player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clear() { this.inventory.clear(); }

    @Override
    public ITextComponent getDisplayName()
    {
        return this.customName != null ? this.customName : this.getDefaultName();
    }

    @Override
    public int fill(FluidStack resource, FluidAction action)
    {
        //System.out.println("Filling");
        int res = fluidHandler.fill(resource, action);
        if(action == FluidAction.EXECUTE)
        {
            if(resource.getFluid() == WATER) waterAmount = fluidHandler.getFluidInTank(0).getAmount();
            else if(resource.getFluid() == STEAM) steamAmount = fluidHandler.getFluidInTank(1).getAmount();
            markDirty();
        }
        return res;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action)
    {
        FluidStack res = fluidHandler.drain(resource, action);
        if(action == FluidAction.EXECUTE)
        {
            if(resource.getFluid() == WATER) waterAmount = fluidHandler.getFluidInTank(0).getAmount();
            else if(resource.getFluid() == STEAM) steamAmount = fluidHandler.getFluidInTank(1).getAmount();
            markDirty();
        }
        return res;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, FluidAction action)
    {
        FluidStack res = fluidHandler.drain(STEAM, maxDrain, action);
        if(action == FluidAction.EXECUTE)
        {
            steamAmount = fluidHandler.getFluidInTank(1).getAmount();
            markDirty();
        }
        return res;
    }

    @Override
    public int getTanks() {
        return fluidHandler.getTanks();
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return fluidHandler.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return fluidHandler.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return fluidHandler.isFluidValid(tank, stack);
    }

    public NonNullList<ItemStack> getInventory() { return inventory; }

    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player) { return new ContainerSteamBoiler(windowId, playerInventory, this); }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability)
    {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
        {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
    {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != Direction.DOWN)
        {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, facing);
    }
}
