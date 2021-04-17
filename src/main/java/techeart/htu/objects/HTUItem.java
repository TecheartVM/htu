package techeart.htu.objects;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import techeart.htu.utils.interfaces.IHasModel;

public class HTUItem extends Item implements IHasModel
{
    public HTUItem(ItemGroup itemGroup)
    {
        super(new Item.Properties().group(itemGroup));
    }

    public HTUItem(Properties props)
    {
        super(props);
    }

    @Override
    public void registerModels()
    {
        //MainClass.proxy.registerItemModel(this, 0, "inventory");
    }

}
