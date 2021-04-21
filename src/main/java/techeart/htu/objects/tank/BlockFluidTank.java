package techeart.htu.objects.tank;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import techeart.htu.objects.HTUBlock;
import techeart.htu.utils.HTUTileEntityType;
import techeart.htu.utils.Utils;

import javax.annotation.Nullable;

public class BlockFluidTank extends HTUBlock implements ITileEntityProvider
{
    public BlockFluidTank()
    {
        super(Block.Properties.create(Material.IRON)
                .harvestTool(ToolType.PICKAXE)
                .hardnessAndResistance(4.0f, 7.0f)
                .sound(SoundType.METAL)
        );
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        //TODO: add vanilla bottles support
        //       + Rethink about _ActionResultType_
        //       + move tank tile interaction code to the separate method
        //       + add the fish feature :)
        if(world.isRemote || player.isSneaking()) return ActionResultType.SUCCESS;
        if(world.getTileEntity(pos) instanceof TileEntityFluidTank)
        {
            TileEntityFluidTank tankTile = ((TileEntityFluidTank) world.getTileEntity(pos));
            if(tankTile == null) return ActionResultType.SUCCESS;

            ItemStack heldItem = player.getHeldItem(handIn);
            if (heldItem.isEmpty())
            {
                Utils.playerInfoMessage("Fluid in tank: " + tankTile.getFluidInTank(0).getAmount()+"mb",player);
                return ActionResultType.SUCCESS;
            }

            IFluidHandlerItem itemFluidHandler = new ItemStack(heldItem.getItem(),1).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).orElse(null);
            if (itemFluidHandler != null)
            {
                FluidStack fluidInItem;

                //if tank is empty, trying to determine fluid in item stack. Else check amount of the required fluid.
                if(tankTile.getFluidInTank(0).isEmpty())
                    fluidInItem = itemFluidHandler.drain(tankTile.getTankCapacity(0), FluidAction.SIMULATE);
                else
                    fluidInItem = itemFluidHandler.drain(new FluidStack(tankTile.getFluidInTank(0), tankTile.getTankCapacity(0) - tankTile.getFluidInTank(0).getAmount()), FluidAction.SIMULATE);

                            //All systems operable. Lets rock!
                if(fluidInItem.isEmpty())
                {
                    //if draining with max value of integer isn't empty, this means that the item cannot be partially emptied (like vanilla bucket).
                    if(!itemFluidHandler.drain(new FluidStack(tankTile.getFluidInTank(0), Integer.MAX_VALUE), FluidAction.SIMULATE).isEmpty()) return ActionResultType.SUCCESS;

                    //Ok, this item doesn't have any fluid. So, drain the tank and fill an item!
                    //check if we can fill item with tank fluid
                    int filled = itemFluidHandler.fill(tankTile.getFluidInTank(0), FluidAction.SIMULATE);
                    //if can't, do nothing
                    if(filled <= 0) return ActionResultType.SUCCESS;
                    //if can, drain tank and fill item with drained fluid
                    FluidStack drained = tankTile.drain(new FluidStack(tankTile.getFluidInTank(0), filled), FluidAction.EXECUTE);
                    if(!player.isCreative())
                    {
                        ItemStack newItem = new ItemStack(heldItem.getItem(), 1);
                        IFluidHandlerItem newHandler = newItem.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).orElse(null);
                        int f = newHandler.fill(drained, FluidAction.EXECUTE);
                        newItem = newHandler.getContainer();
                        heldItem.shrink(1);
                        if(heldItem.isEmpty()) player.inventory.add(player.inventory.currentItem, newItem);
                        else if(!heldItem.isItemEqual(newItem) || !heldItem.isStackable() || heldItem.getCount() >= heldItem.getMaxStackSize() || heldItem.getCount() >= player.inventory.getInventoryStackLimit())
                        {
                            if(!player.inventory.addItemStackToInventory(newItem))
                                player.dropItem(newItem, false, true);
                        }
                        else heldItem.grow(1);
                    }

                    SoundEvent fillSound = drained.getFluid().getAttributes().getFillSound();
                    world.playSound(null, pos, fillSound, SoundCategory.BLOCKS, 1.0F, 1.0F);

                    return ActionResultType.SUCCESS;
                }
                else
                {
                    //It seems like this item have suitable fluid... Lets fill tank!
                    int filled = tankTile.fill(fluidInItem, FluidAction.EXECUTE);
                    FluidStack drained = itemFluidHandler.drain(new FluidStack(fluidInItem, filled), player.isCreative() ? FluidAction.SIMULATE : FluidAction.EXECUTE);
                    ItemStack heldItemContainer = itemFluidHandler.getContainer();

                    SoundEvent emptySound = drained.getFluid().getAttributes().getEmptySound();
                    world.playSound(null, pos, emptySound, SoundCategory.BLOCKS, 1.0F, 1.0F);

                    if(!heldItem.isItemEqual(heldItemContainer))
                        Utils.addItemToPlayer(player,player.inventory.getSlotFor(heldItem),1,new ItemStack(heldItemContainer.getItem(), 1));
                    return ActionResultType.SUCCESS;
                }
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) { return HTUTileEntityType.FLUID_TANK.get().create(); }
}
