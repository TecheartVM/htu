package techeart.htu.objects.pump;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import techeart.htu.objects.HTUBlock;
import techeart.htu.utils.HTUTileEntityType;

import javax.annotation.Nullable;

public class BlockWaterPump extends HTUBlock implements ITileEntityProvider
{

    public BlockWaterPump()
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
        if(!world.isRemote && world.getTileEntity(pos) instanceof TileEntityWaterPump)
        {
            System.out.println("Water amount: " + ((TileEntityWaterPump) world.getTileEntity(pos)).getFluidInTank(0).getAmount());
            System.out.println("Steam amount: " + ((TileEntityWaterPump) world.getTileEntity(pos)).getFluidInTank(1).getAmount());
        }
        return super.onBlockActivated(state, world, pos, player, handIn, hit);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) { return HTUTileEntityType.WATER_PUMP.get().create(); }
}
