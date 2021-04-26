package techeart.htu.objects.tank;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.PlayerContainer;
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
import net.minecraft.util.text.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import techeart.htu.utils.HTUTileEntityType;
import techeart.htu.utils.KeyboardHelper;
import techeart.htu.utils.ModUtils;
import techeart.htu.utils.registration.HTUBlock;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class BlockFluidTank extends HTUBlock implements ITileEntityProvider
{
    private TileEntityFluidTank tileEntity;
    private FluidStack fluidInTank;

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
        List<ItemStack> drops = new ArrayList<>();
        ItemStack item = new ItemStack(this.getBlock().asItem(), 1);
        drops.add(item);
        if(builder.getWorld().isRemote) return drops;
        if(fluidInTank == null || fluidInTank.isEmpty()) return drops;

        CompoundNBT data = item.getOrCreateTag();
        fluidInTank.writeToNBT(data);
        return drops;
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if(!worldIn.isRemote)
            fluidInTank = ((TileEntityFluidTank)worldIn.getTileEntity(pos)).getFluid();
        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        if(KeyboardHelper.isHoldingShift())
        {
            CompoundNBT data = stack.getTag();
            if (data != null)
            {
                Fluid fluid = FluidStack.loadFluidStackFromNBT(data).getFluid();
                InputStream is;
                BufferedImage image;
                int fluidColor = 248572;
                try
                {
                    ResourceLocation fluidRes = fluid.getAttributes().getStillTexture();
                    is = Minecraft.getInstance().getResourceManager().getResource(
                            new ResourceLocation(fluidRes.getNamespace(), "textures/" + fluidRes.getPath() + ".png")
                    ).getInputStream();
                    image = ImageIO.read(is);
                    fluidColor = image.getRGB(0, 0);
                    //TODO apply fluid overlay color to texture color
                }
                catch(Exception e){
                    //e.printStackTrace();
                }

                System.out.println(fluidColor);

                tooltip.add(new StringTextComponent("Fluid: ")
                        .append(new TranslationTextComponent(ModUtils.getFluidName(data)).setStyle(Style.EMPTY.setColor(Color.fromInt(fluidColor))))//.fromHex("#03cafc"))))
                        .append(new StringTextComponent(", " + data.getInt("Amount") + "mB")).setStyle(Style.EMPTY.setItalic(true)));
            }
            else tooltip.add(new TranslationTextComponent("htu.fluidtank.tooltip"));
        }
        else tooltip.add(new TranslationTextComponent("htu.moreinfo.tooltip"));

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos)
    {
        TileEntity te = world.getTileEntity(pos);
        if(!(te instanceof TileEntityFluidTank)) return 0;
        TileEntityFluidTank tankTile = ((TileEntityFluidTank)te);
        if(tankTile == null) return 0;
        FluidStack fluid = tankTile.getFluid();
        if(fluid.isEmpty()) return 0;
        return Math.max(fluid.getFluid().getAttributes().getLuminosity(), super.getLightValue(state, world, pos));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
    {
        return Block.makeCuboidShape(2,0 ,2, 14, 16, 14);
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
                ModUtils.playerInfoMessage("Fluid in tank: " + tankTile.getFluid().getAmount()+"mB",player);
                return ActionResultType.SUCCESS;
            }

            /*~~~~~ACHIEVEMENT~~~~~*/
            if(heldItem.getItem() == Items.PUFFERFISH_BUCKET ||heldItem.getItem() == Items.COD_BUCKET || heldItem.getItem() == Items.SALMON_BUCKET|| heldItem.getItem() == Items.TROPICAL_FISH_BUCKET )
                ModUtils.unlockAdvancement(player,"secret/wip");
            /*~~~~~ACHIEVEMENT~~~~~*/

            IFluidHandlerItem itemFluidHandler = new ItemStack(heldItem.getItem(),1).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).orElse(null);

            if (itemFluidHandler != null)
            {
                SoundEvent sound;

                FluidStack fluidInItem;

                //if tank is empty, trying to determine fluid in item stack. Else check amount of the required fluid.
                if(tankTile.isEmpty())
                    fluidInItem = itemFluidHandler.drain(tankTile.getCapacity(), FluidAction.SIMULATE);
                else
                    fluidInItem = itemFluidHandler.drain(new FluidStack(tankTile.getFluid(), tankTile.getCapacity() - tankTile.getFluid().getAmount()), FluidAction.SIMULATE);

                //All systems operable. Lets rock!
                if(fluidInItem.isEmpty())
                {
                    //if draining with max value of integer isn't empty, this means that the item cannot be partially emptied (like vanilla bucket).
                    if(!itemFluidHandler.drain(new FluidStack(tankTile.getFluid(), Integer.MAX_VALUE), FluidAction.SIMULATE).isEmpty())
                        return ActionResultType.SUCCESS;

                    //Ok, this item doesn't have any fluid (fill item + drain tank)

                    //check if we can fill item with tank fluid
                    int filled = itemFluidHandler.fill(tankTile.getFluid(), player.isCreative() ? FluidAction.SIMULATE : FluidAction.EXECUTE);
                    //if can't, do nothing
                    if(filled <= 0) return ActionResultType.SUCCESS;
                    //if can, drain tank and fill item with drained fluid (and save soundEvent ;) )
                    sound = tankTile.drain(new FluidStack(tankTile.getFluid(), filled), FluidAction.EXECUTE).getFluid().getAttributes().getFillSound();

                    ModUtils.addItemToPlayer(player,handIn,1,new ItemStack(itemFluidHandler.getContainer().getItem(), 1));

                    //if tank became empty, the light must be updated
                    if(tankTile.isEmpty())
                    {
                        world.getChunkProvider().getLightManager().checkBlock(pos);
                        world.addBlockEvent(pos, this, 1, 1);
                    }
                }
                else
                {
                    boolean wasEmpty = tankTile.isEmpty();

                    //It seems like this item have suitable fluid... Lets fill tank! (fill tank + drain item)
                    int filled = tankTile.fill(fluidInItem, FluidAction.EXECUTE);
                    sound  = itemFluidHandler.drain(new FluidStack(fluidInItem, filled), player.isCreative() ? FluidAction.SIMULATE : FluidAction.EXECUTE)
                            .getFluid().getAttributes().getEmptySound();

                    ModUtils.addItemToPlayer(player,handIn,1,new ItemStack(itemFluidHandler.getContainer().getItem(), 1));

                    //if tank got new fluid, the light must be updated
                    if(wasEmpty && !tankTile.isEmpty())
                    {
                        world.getChunkProvider().getLightManager().checkBlock(pos);
                        world.addBlockEvent(pos, this, 1, 1);
                    }
                }
                world.playSound(null, pos,sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int id, int param)
    {
        if(id == 1)
        {
            //updates client light
            if(worldIn.isRemote)
                ((ClientWorld)worldIn).getChunkProvider().getLightManager().checkBlock(pos);
            return true;
        }
        return super.eventReceived(state, worldIn, pos, id, param);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if(worldIn.isRemote) return;

        if(stack.getTag() != null && tileEntity != null)
        {
            FluidStack f = FluidStack.loadFluidStackFromNBT(stack.getTag());
            tileEntity.fill(f, FluidAction.EXECUTE);
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