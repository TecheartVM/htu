package techeart.htu.objects;

import net.minecraft.block.Block;
import techeart.htu.utils.interfaces.IHasModel;

public class HTUBlock extends Block implements IHasModel
{
    public HTUBlock(Properties props)
    {
        super(props);
    }

    @Override
    public void registerModels()
    {
        //MainClass.proxy.registerItemModel(Item.getItemFromBlock(this), 0, "inventory");
    }

}
