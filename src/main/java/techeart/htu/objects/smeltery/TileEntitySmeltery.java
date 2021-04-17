package techeart.htu.objects.smeltery;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;
import techeart.htu.MainClass;
import techeart.htu.recipes.alloying.AlloyRecipe;
import techeart.htu.recipes.alloying.AlloyRecipes;
import techeart.htu.utils.HTUTileEntityType;
import techeart.htu.utils.InventoryHelper;

import javax.annotation.Nullable;

public class TileEntitySmeltery extends TileEntity implements ISidedInventory, ITickableTileEntity, INamedContainerProvider
{
    private NonNullList<ItemStack> inventory = NonNullList.withSize(6, ItemStack.EMPTY);

    private ITextComponent customName;
    private AlloyRecipe currentRecipe = null;

    protected final int tier = 0;

    private int burnTime;
    private int burnTimeTotal;
    private int smeltTime;
    private int smeltTimeTotal;

    private boolean isSmelting = false;

    private final int[] SLOTS_TOP = {0,1,2,3};
    private final int[] SLOTS_BOTTOM = {4,5};
    private final int[] SLOTS_SIDE = {4,5};

    public TileEntitySmeltery()
    {
        this(HTUTileEntityType.SMELTERY.get());
    }

    public TileEntitySmeltery(TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    @Override
    public int getSizeInventory() {
        return this.inventory.size();
    }

    @Override
    public boolean isEmpty()
    {
        for(ItemStack stack : this.inventory)
        {
            if(!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) { return (ItemStack)this.inventory.get(index); }

    @Override
    public ItemStack decrStackSize(int index, int count) { return ItemStackHelper.getAndSplit(this.inventory, index, count); }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        return ItemStackHelper.getAndRemove(this.inventory, index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack inputStack)
    {
        ItemStack stackInSlot = this.inventory.get(index);
        boolean flag = !inputStack.isEmpty() && inputStack.isItemEqual(stackInSlot) && ItemStack.areItemStackTagsEqual(inputStack, stackInSlot);

        this.inventory.set(index, inputStack);

        if(inputStack.getCount() > this.getInventoryStackLimit())
        {
            inputStack.setCount(this.getInventoryStackLimit());
        }

        if((index >= 0 && index < 4))
        {
            if(!flag)
            {
                this.smeltTimeTotal = getCurrentRecipeTime();
                this.smeltTime = 0;
                this.markDirty();
            }
        }
    }

    @Override
    public int getInventoryStackLimit() { return 64; }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        if(index == 5) return false;
        else if(index != 4) return true;
        else return isItemFuel(stack);
    }

    public NonNullList<ItemStack> getInventory() { return inventory; }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player)
    {
        return this.world.getTileEntity(this.pos) == this &&
                player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clear() { this.inventory.clear(); }

    @Override
    public void read(BlockState state, CompoundNBT nbt)
    {
        super.read(state, nbt);
        if(nbt.contains("CustomName", Constants.NBT.TAG_STRING))
            this.customName = ITextComponent.Serializer.getComponentFromJson(nbt.getString("CustomName"));
        this.inventory = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(nbt, this.inventory);
        determineCurrentRecipe();
        this.burnTime = nbt.getInt("BurnTime");
        this.smeltTime = nbt.getInt("SmeltTime");
        //this.totalCookTime = nbt.getInt("CookTimeTotal");
        this.burnTimeTotal = nbt.getInt("burnTimeTotal"); //getItemBurnTime((ItemStack)this.inventory.get(4));
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt)
    {
        super.write(nbt);
        if(this.customName != null)
            nbt.putString("CustomName", ITextComponent.Serializer.toJson(customName));
        nbt.putInt("BurnTime", (short)this.burnTime);
        nbt.putInt("SmeltTime", (short)this.smeltTime);
        //nbt.putInt("CookTimeTotal", (short)this.totalCookTime);
        nbt.putInt("burnTimeTotal", (short)this.burnTimeTotal);
        ItemStackHelper.saveAllItems(nbt, this.inventory);

        return nbt;
    }

    public boolean isSmelting() { return isSmelting; }

    public boolean isBurning() { return this.burnTime > 0; }

    private boolean canSmelt()
    {
        if(currentRecipe == null) return false;

        ItemStack result = currentRecipe.result;
        if(result.isEmpty()) return false;
        else
        {
            ItemStack output = (ItemStack) this.inventory.get(5);
            if (output.isEmpty()) return true;
            if (!output.isItemEqual(result)) return false;
            int resCount = output.getCount() + result.getCount();
            return resCount <= getInventoryStackLimit() && resCount <= output.getMaxStackSize();
        }
    }

    public void smeltItem()
    {
        if(currentRecipe == null) return;

        ItemStack result = currentRecipe.result;
        ItemStack output = this.inventory.get(5);

        if(InventoryHelper.takeItems(inventory, currentRecipe.ingredientsList, true, 0, 4))
        {
            if (output.isEmpty()) this.inventory.set(5, result.copy());
            else if (output.getItem() == result.getItem()) output.grow(result.getCount());
        }
    }

    public static int getItemBurnTime(ItemStack stack) { return ForgeHooks.getBurnTime(stack); }

    public static boolean isItemFuel(ItemStack stack) { return getItemBurnTime(stack) > 0; }

    public int getCurrentRecipeTime()
    {
        if(currentRecipe == null) return 400;
        return currentRecipe.time;
    }

    public int getField(int id)
    {
        switch(id)
        {
            case 0: return this.burnTime;
            case 1: return this.burnTimeTotal;
            case 2: return this.smeltTime;
            case 3: return smeltTimeTotal;

            default: return 0;
        }
    }

    public void setField(int id, int value)
    {
        switch(id)
        {
            case 0: this.burnTime = value;
                break;
            case 1: this.burnTimeTotal = value;
                break;
            case 2: this.smeltTime = value;
                break;
            case 3: this.smeltTimeTotal = value;
                break;
        }
    }

    public int getFieldCount() { return 4; }

    @Override
    public void tick()
    {
        boolean burning = this.isBurning();
        boolean isDirty = false;

        if(this.isBurning()) { --this.burnTime; }

        if(!this.world.isRemote)
        {
            ItemStack fuel = (ItemStack)this.inventory.get(4);

            if(this.isBurning() || !fuel.isEmpty()
                    && (!((ItemStack)this.inventory.get(0)).isEmpty()
                    || !((ItemStack)this.inventory.get(1)).isEmpty()
                    || !((ItemStack)this.inventory.get(2)).isEmpty()
                    || !((ItemStack)this.inventory.get(3)).isEmpty()))
            {
                if(!isSmelting) determineCurrentRecipe();

                if(!this.isBurning() && this.canSmelt())
                {
                    this.burnTime = getItemBurnTime(fuel);
                    this.burnTimeTotal = this.burnTime;

                    if(this.isBurning())
                    {
                        isDirty = true;


                        if(!fuel.isEmpty())
                        {
                            Item item = fuel.getItem();
                            fuel.shrink(1);

                            if(fuel.isEmpty())
                            {
                                ItemStack item1 = item.getContainerItem(fuel);
                                this.inventory.set(4, item1);
                            }
                        }
                    }
                }
                if(this.isBurning() && this.canSmelt())
                {
                    ++this.smeltTime;
                    isSmelting = true;
                    if(this.smeltTime >= this.smeltTimeTotal)
                    {
                        this.isSmelting = false;
                        this.smeltTime = 0;
                        this.smeltItem();
                        isDirty = true;
                        determineCurrentRecipe();
                    }
                }
                else
                {
                    this.isSmelting = false;
                    this.smeltTime = 0;
                }
            }
            else if(!this.isBurning() && smeltTime > 0)
            {
                this.smeltTime = MathHelper.clamp(this.smeltTime - 2, 0, this.smeltTimeTotal);
            }

            if(burning != this.isBurning())
            {
                isDirty = true;
                BlockSmeltery.setState(this.isBurning(), this.world, this.pos);
            }
        }
        if(isDirty) this.markDirty();
    }

    @Override
    public int[] getSlotsForFace(Direction side) { return side == Direction.UP ? this.SLOTS_TOP : side == Direction.DOWN ? this.SLOTS_BOTTOM : this.SLOTS_SIDE; }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, @Nullable Direction direction) { return isItemValidForSlot(index, itemStackIn); }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, Direction direction) { return index == 5; }

    @Override
    public ITextComponent getDisplayName() { return this.customName != null ? this.customName : this.getDefaultName(); }

    public ITextComponent getDefaultName() { return new TranslationTextComponent("container." + MainClass.MODID + ".smeltery"); }

    public void setCustomName(ITextComponent name) { this.customName = name; }

    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player) { return new ContainerSmeltery(windowId, playerInventory, this); }

    protected void determineCurrentRecipe()
    {
        currentRecipe = AlloyRecipes.getValidRecipe(tier, inventory, 0, 4);
        this.smeltTimeTotal = this.getCurrentRecipeTime();
    }
}
