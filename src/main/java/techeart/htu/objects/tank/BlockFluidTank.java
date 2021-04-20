package techeart.htu.objects.tank;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
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
import net.minecraftforge.fluids.capability.IFluidHandler.*;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import techeart.htu.objects.HTUBlock;
import techeart.htu.utils.HTUTileEntityType;

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
        if(world.isRemote || player.isSneaking()) return ActionResultType.PASS;
        if(world.getTileEntity(pos) instanceof TileEntityFluidTank)
        {
            TileEntityFluidTank tank = ((TileEntityFluidTank) world.getTileEntity(pos));
            ItemStack heldItem = player.getHeldItem(handIn);
            if(tank == null) return ActionResultType.PASS;

            if (heldItem.isEmpty())
            {
                System.out.println("Fluid in tank: " + tank.getFluidInTank(0).getAmount());
                return ActionResultType.SUCCESS;
            }
            if (heldItem.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent())
            {
                IFluidHandlerItem fhItem = heldItem.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).orElse(null);
                FluidStack fluidInItem;
                //if tank is empty, trying to determine fluid in item stack. Else check amount of the required fluid.
                if(tank.getFluidInTank(0).isEmpty()) fluidInItem = fhItem.drain(tank.getTankCapacity(0), FluidAction.SIMULATE);
                else fluidInItem = fhItem.drain(new FluidStack(tank.getFluidInTank(0), tank.getTankCapacity(0) - tank.getFluidInTank(0).getAmount()), FluidAction.SIMULATE);

                if(fluidInItem.isEmpty())
                {
                    //try to drain tank
                    System.out.println("Drain tank");

                    //if draining with max value of integer isn't empty, this means that the item cannot be partially emptied (like vanilla bucket).
                    if(!fhItem.drain(new FluidStack(tank.getFluidInTank(0), Integer.MAX_VALUE), FluidAction.SIMULATE).isEmpty()) return ActionResultType.PASS;

                    int filled = fhItem.fill(tank.getFluidInTank(0), FluidAction.EXECUTE);
                    System.out.println("Filled: " + filled);
                    if(filled <= 0) return ActionResultType.PASS;
                    if(!heldItem.isItemEqual(fhItem.getContainer()))
                    {
                        heldItem.shrink(1);
                        if(heldItem.isEmpty()) player.setHeldItem(handIn, ItemStack.EMPTY);
                        if(!player.inventory.addItemStackToInventory(new ItemStack(fhItem.getContainer().getItem(), 1)))
                        {
                            world.addEntity(player.dropItem(new ItemStack(fhItem.getContainer().getItem(), 1), false, true));
                        }
                    }
                    FluidStack drained = tank.drain(new FluidStack(tank.getFluidInTank(0), filled), FluidAction.EXECUTE);
                    System.out.println("Drained: " + drained.getAmount());

                    return ActionResultType.SUCCESS;
                }
                else    //TODO add the fish feature :)
                {
                    //fill tank
                    int filled = tank.fill(fluidInItem, FluidAction.EXECUTE);
                    ItemStack copy = heldItem.copy();
                    copy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).orElse(null).drain(new FluidStack(fluidInItem, filled), FluidAction.EXECUTE);
                    copy = copy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).orElse(null).getContainer();
                    if(!heldItem.isItemEqual(copy));
                    {
                        heldItem.shrink(1);
                        if(heldItem.isEmpty()) player.setHeldItem(handIn, ItemStack.EMPTY);
                        if(!player.inventory.addItemStackToInventory(new ItemStack(copy.getItem(), 1)))
                        {
                            world.addEntity(player.dropItem(new ItemStack(copy.getItem(), 1), false, true));
                        }
                    }
                    return ActionResultType.SUCCESS;
                }
            }
        }
        return ActionResultType.PASS;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) { return HTUTileEntityType.FLUID_TANK.get().create(); }
}
