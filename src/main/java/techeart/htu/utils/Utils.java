package techeart.htu.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;

import java.util.UUID;

public class Utils {
    public static void playerChatMessage(String info, PlayerEntity playerEntity)
    {
        playerEntity.sendMessage(ITextComponent.getTextComponentOrEmpty(info), UUID.randomUUID());
    }

    public static void playerInfoMessage(String info, PlayerEntity playerEntity)
    {
        playerEntity.sendStatusMessage(ITextComponent.getTextComponentOrEmpty(info), true);
    }

    public static void addItemToPlayer(PlayerEntity player, Hand handIn, int inventorySlot, int shrinkAmount ,ItemStack newItem)
    {
        ItemStack oldItem = player.getHeldItem(handIn);

        oldItem.shrink(shrinkAmount);
        if(!player.inventory.add(inventorySlot,newItem))
            if(!player.inventory.addItemStackToInventory(newItem))
                player.dropItem(newItem, false, true);
    }

    public static void addItemToPlayer(PlayerEntity player, ItemStack oldItem, int shrinkAmount ,ItemStack newItem)
    {
        oldItem.shrink(shrinkAmount);
        if(!player.inventory.add(player.inventory.getSlotFor(oldItem),newItem))
            if(!player.inventory.addItemStackToInventory(newItem))
                player.dropItem(newItem, false, true);
    }
}
