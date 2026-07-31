package io.Yomicer.magicExpansion.utils.playerMessage;

import io.Yomicer.magicExpansion.utils.ColorGradient;
import org.bukkit.entity.Player;

public class MainHandMessage {
    public static void sendMainHandMessage(Player p){
        p.sendMessage(ColorGradient.getGradientNameVer2("Hold this item in your main hand to use it."));
    }
}
