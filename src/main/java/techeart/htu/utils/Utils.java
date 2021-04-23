package techeart.htu.utils;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerWorld;
import techeart.htu.MainClass;

import java.util.UUID;

public class Utils
{
    public static void playerChatMessage(String info, PlayerEntity playerEntity)
    {
        playerEntity.sendMessage(ITextComponent.getTextComponentOrEmpty(info), UUID.randomUUID());
    }

    public static void playerInfoMessage(String info, PlayerEntity playerEntity)
    {
        playerEntity.sendStatusMessage(ITextComponent.getTextComponentOrEmpty(info), true);
    }

    public static void addItemToPlayer(PlayerEntity player, Hand handIn, int shrinkAmount ,ItemStack newItem)
    {
        ItemStack oldItem = player.getHeldItem(handIn);
        if(oldItem.getCount() !=1) {
            oldItem.shrink(shrinkAmount);
            if (!player.inventory.addItemStackToInventory(newItem))
                player.dropItem(newItem, false, true);
        }
        else
            player.setHeldItem(handIn,newItem);
    }

    public static void addItemToPlayer(PlayerEntity player, ItemStack oldItem, int shrinkAmount ,ItemStack newItem)
    {
        oldItem.shrink(shrinkAmount);
        if(!player.inventory.add(player.inventory.getSlotFor(oldItem),newItem)) //TODO: Check this thing
            if(!player.inventory.addItemStackToInventory(newItem))
                player.dropItem(newItem, false, true);
    }

    public static void unlockAdvancement(PlayerEntity player, String name)
    {
        if(player instanceof ServerPlayerEntity)
        {
            PlayerAdvancements advancements = ((ServerPlayerEntity)player).getAdvancements();
            AdvancementManager manager = ((ServerWorld)player.getEntityWorld()).getServer().getAdvancementManager();
            Advancement advancement = manager.getAdvancement(new ResourceLocation(MainClass.MODID, name));
            if(advancement!=null)
                advancements.grantCriterion(advancement, "code_trigger");
        }
    }

    public static ResourceLocation getResourceByKey(String path) {
        return new ResourceLocation(MainClass.MODID, path);
    }
}
