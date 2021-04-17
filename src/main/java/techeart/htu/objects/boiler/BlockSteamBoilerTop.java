package techeart.htu.objects.boiler;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.network.NetworkHooks;
import techeart.htu.objects.HTUBlock;

public class BlockSteamBoilerTop extends HTUBlock
{
    public BlockSteamBoilerTop()
    {
        super(Block.Properties.create(Material.IRON)
                .harvestTool(ToolType.PICKAXE)
                .hardnessAndResistance(4.0f, 7.0f)
                .sound(SoundType.METAL)
        );
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit)
    {
        if(world != null && !world.isRemote)
        {
            TileEntity tileEntity = world.getTileEntity(pos.down());
            if(tileEntity instanceof TileEntitySteamBoiler)
            {
                NetworkHooks.openGui((ServerPlayerEntity)player, (INamedContainerProvider)tileEntity, pos.down());
            }
            else if(!(world.getBlockState(pos.down()).getBlock() instanceof BlockSteamBoiler))
            {
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving)
    {
        BlockState blockBelow = world.getBlockState(pos.down());
        if(blockBelow.getBlock() instanceof BlockSteamBoiler)
        {
            world.setBlockState(pos.down(), Blocks.AIR.getDefaultState());
        }
    }
}
