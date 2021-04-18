package techeart.htu.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;

import java.util.UUID;

public class Utils {
    public static void messageToPlayer(String info, PlayerEntity playerEntity)
    {
        playerEntity.sendMessage(ITextComponent.getTextComponentOrEmpty(info), UUID.randomUUID());
    }
}
