package techeart.htu.objects;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import techeart.htu.MainClass;

public class HTUItemBlock extends BlockItem
{
    public HTUItemBlock(Block block, ItemGroup itemGroup)
    {
        super(block, new Item.Properties().group(itemGroup));
    }

    public HTUItemBlock(Block blockIn) {
        super(blockIn, new Item.Properties().group(MainClass.STEAM_CREATIVE_TAB));
    }
}
