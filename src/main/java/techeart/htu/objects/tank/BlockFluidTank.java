package techeart.htu.objects.tank;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
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
        //TODO: Fix Bug(Vanilla bottles does not supported!) + Rethink about _ActionResultType_  +   add the fish feature :)
        if(world.isRemote || player.isSneaking()) return ActionResultType.SUCCESS;
        if(world.getTileEntity(pos) instanceof TileEntityFluidTank)
        {
            TileEntityFluidTank tankTileEntity = ((TileEntityFluidTank) world.getTileEntity(pos));
            if(tankTileEntity == null) return ActionResultType.SUCCESS;

            ItemStack heldItem = player.getHeldItem(handIn);
            if (heldItem.isEmpty())
            {
                Utils.playerInfoMessage("Fluid in tank: " + tankTileEntity.getFluidInTank(0).getAmount()+"mb",player);
                return ActionResultType.SUCCESS;
            }

            IFluidHandlerItem fluidHandlerItem = new ItemStack(heldItem.getItem(),1).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).orElse(null);
            if (fluidHandlerItem != null)
            {
                FluidStack fluidInItem;

                //if tank is empty, trying to determine fluid in item stack. Else check amount of the required fluid.
                if(tankTileEntity.getFluidInTank(0).isEmpty())
                    fluidInItem = fluidHandlerItem.drain(tankTileEntity.getTankCapacity(0), FluidAction.SIMULATE);
                else
                    fluidInItem = fluidHandlerItem.drain(new FluidStack(tankTileEntity.getFluidInTank(0), tankTileEntity.getTankCapacity(0) - tankTileEntity.getFluidInTank(0).getAmount()), FluidAction.SIMULATE);

                            //All systems operable. Lets rock!
                if(fluidInItem.isEmpty())
                {
                    //Ok, this item doesnt have any of fluid. Lets fix this!

                    //if draining with max value of integer isn't empty, this means that the item cannot be partially emptied (like vanilla bucket).
                    if(!fluidHandlerItem.drain(new FluidStack(tankTileEntity.getFluidInTank(0), Integer.MAX_VALUE), FluidAction.SIMULATE).isEmpty()) return ActionResultType.SUCCESS;
                    int filled = fluidHandlerItem.fill(tankTileEntity.getFluidInTank(0), player.isCreative() ? FluidAction.SIMULATE : FluidAction.EXECUTE);
                    if(filled <= 0) return ActionResultType.SUCCESS;
                    tankTileEntity.drain(new FluidStack(tankTileEntity.getFluidInTank(0), filled), FluidAction.EXECUTE);

                    if(!heldItem.isItemEqual(fluidHandlerItem.getContainer()))
                        Utils.addItemToPlayer(player,handIn,player.inventory.getSlotFor(heldItem),1,new ItemStack(fluidHandlerItem.getContainer().getItem(), 1));
                    return ActionResultType.SUCCESS;
                }
                else
                {
                    //It seems like this item have suitable fluid... Lets pour into tank!
                    int filled = tankTileEntity.fill(fluidInItem, FluidAction.EXECUTE);
                    fluidHandlerItem.drain(new FluidStack(fluidInItem, filled), player.isCreative() ? FluidAction.SIMULATE : FluidAction.EXECUTE);
                    ItemStack heldItemContainer = fluidHandlerItem.getContainer();

                    if(!heldItem.isItemEqual(heldItemContainer))
                        Utils.addItemToPlayer(player,handIn,player.inventory.getSlotFor(heldItem),1,new ItemStack(heldItemContainer.getItem(), 1));
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
