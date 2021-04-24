package techeart.htu.objects.tank;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import techeart.htu.utils.HTUTileEntityType;
import techeart.htu.utils.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockFluidTank extends Block implements ITileEntityProvider
{
    private TileEntityFluidTank tileEntity;

    public BlockFluidTank()
    {
        super(Block.Properties.create(Material.IRON)
                .harvestTool(ToolType.PICKAXE)
                .setRequiresTool()
                .hardnessAndResistance(4.0f, 7.0f)
                .sound(SoundType.METAL)
                .notSolid()
                .setOpaque((state, reader, pos) -> false)
                .setBlocksVision((state, reader, pos) -> false)
        );
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
    {
        List<ItemStack> drop = new ArrayList<>();
        ItemStack item = new ItemStack(this.getBlock().asItem(), 1);
        drop.add(item);
        if(tileEntity == null) return drop;
        CompoundNBT data = item.getOrCreateTag();
        tileEntity.write(data);
        return drop;
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos)
    {
        TileEntityFluidTank tankTile = ((TileEntityFluidTank)world.getTileEntity(pos));
        if(tankTile == null) return 0;
        FluidStack fluid = tankTile.getFluidInTank(0);
        if(fluid.isEmpty()) return 0;
        return Math.max(fluid.getFluid().getAttributes().getLuminosity(), super.getLightValue(state, world, pos));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
    {
        return Block.makeCuboidShape(2,0 ,2, 14, 16, 14);
    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side)
    {
        return false;
    }

    @Override
    public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos)
    {
        return 0;
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

            /*~~~~~ACHIEVEMENT~~~~~*/
            if(heldItem.getItem() == Items.PUFFERFISH_BUCKET ||heldItem.getItem() == Items.COD_BUCKET || heldItem.getItem() == Items.SALMON_BUCKET|| heldItem.getItem() == Items.TROPICAL_FISH_BUCKET )
                Utils.unlockAdvancement(player,"secret/wip");
            /*~~~~~ACHIEVEMENT~~~~~*/

            IFluidHandlerItem itemFluidHandler = new ItemStack(heldItem.getItem(),1).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).orElse(null);

            if (itemFluidHandler != null)
            {
                SoundEvent sound;

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
                    if(!itemFluidHandler.drain(new FluidStack(tankTile.getFluidInTank(0), Integer.MAX_VALUE), FluidAction.SIMULATE).isEmpty())
                        return ActionResultType.SUCCESS;

                    //Ok, this item doesn't have any fluid (fill item + drain tank)

                    //check if we can fill item with tank fluid
                    int filled = itemFluidHandler.fill(tankTile.getFluidInTank(0), player.isCreative() ? FluidAction.SIMULATE : FluidAction.EXECUTE);
                    //if can't, do nothing
                    if(filled <= 0) return ActionResultType.SUCCESS;
                    //if can, drain tank and fill item with drained fluid (and save soundEvent ;) )
                    sound = tankTile.drain(new FluidStack(tankTile.getFluidInTank(0), filled), FluidAction.EXECUTE).getFluid().getAttributes().getFillSound();

                    Utils.addItemToPlayer(player,handIn,1,new ItemStack(itemFluidHandler.getContainer().getItem(), 1));
                }
                else
                {
                    //It seems like this item have suitable fluid... Lets fill tank! (fill tank + drain item)
                    int filled = tankTile.fill(fluidInItem, FluidAction.EXECUTE);
                    sound  = itemFluidHandler.drain(new FluidStack(fluidInItem, filled), player.isCreative() ? FluidAction.SIMULATE : FluidAction.EXECUTE)
                            .getFluid().getAttributes().getEmptySound();
                    Utils.addItemToPlayer(player,handIn,1,new ItemStack(itemFluidHandler.getContainer().getItem(), 1));
                }
                world.playSound(null, pos,sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if(stack.getTag() != null && tileEntity != null)
        {
            FluidTank f = new FluidTank(TileEntityFluidTank.CAPACITY);
            f.readFromNBT(stack.getTag());
            tileEntity.fill(f.getFluid(), FluidAction.EXECUTE);
        }
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn)
    {
        TileEntity te = HTUTileEntityType.FLUID_TANK.get().create();
        tileEntity = (TileEntityFluidTank) te;
        return te;
    }
}