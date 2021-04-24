package techeart.htu.objects.pump;

import com.google.common.collect.Maps;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import techeart.htu.objects.HTUBlock;
import techeart.htu.utils.HTUTileEntityType;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.stream.Stream;

public class BlockWaterPump extends HTUBlock implements ITileEntityProvider
{
    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;

    private static final Map<Direction, VoxelShape> SHAPES = Util.make(Maps.newHashMap(), (map) -> {
        final VoxelShape NS = Stream.of(Block.makeCuboidShape(0,0,0,16,2,16), Block.makeCuboidShape(0,2,3,16,13,13))
                .reduce((v, k) -> VoxelShapes.combineAndSimplify(v, k, IBooleanFunction.OR)).get();
        final VoxelShape EW = Stream.of(Block.makeCuboidShape(0,0,0,16,2,16), Block.makeCuboidShape(3,2,0,13,13,16))
                .reduce((v, k) -> VoxelShapes.combineAndSimplify(v, k, IBooleanFunction.OR)).get();

        map.put(Direction.NORTH, NS);
        map.put(Direction.SOUTH, NS);
        map.put(Direction.EAST, EW);
        map.put(Direction.WEST, EW);
    });

    public BlockWaterPump()
    {
        super(Block.Properties.create(Material.IRON)
                .harvestTool(ToolType.PICKAXE)
                .hardnessAndResistance(4.0f, 7.0f)
                .sound(SoundType.METAL)
                .notSolid()
                .setOpaque((state, reader, pos) -> false)
        );
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) { return SHAPES.get(state.get(FACING)); }

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

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) { return state.with(FACING, rot.rotate(state.get(FACING))); }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) { return state.rotate(mirrorIn.toRotation(state.get(FACING))); }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) { builder.add(FACING); }

    public static Direction getFacing(BlockState state) { return state.get(FACING); }
}
