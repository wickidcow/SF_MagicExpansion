package io.Yomicer.magicExpansion.extraUtils.items;

import io.Yomicer.magicExpansion.core.MagicExpansionItems;
import io.Yomicer.magicExpansion.utils.ColorGradient;
import io.Yomicer.magicExpansion.utils.itemUtils.sfItemUtils;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

import static io.Yomicer.magicExpansion.utils.ConvertItem.stoneCreateItem;

public class BasicCreation {



    /*
    魔法2.0二代自适应渐变字体
    js脚本调用示例:
    var BasicCreation= Java.type("io.Yomicer.magicExpansion.extraUtils.items.BasicCreation")
    var s = BasicCreation.MagicExpansionVer2ColorBuild("you requires");
    ---> s 为 已经处理过渐变的字符串
    使用示例:
    例如在onUse(e)中调用
    let p = e.getPlayer();
    p.sendMessage(s);
    ----------------------
    java调用示例:
    import io.Yomicer.magicExpansion.extraUtils.items;
    String s = BasicCreation.MagicExpansionVer2ColorBuild("you requires");
    使用示例:
    java就不用多描述了,s只是一个字符串,你想哪里用都可以,只要在这个方法里面
    */
    public static String MagicExpansionVer2ColorBuild(String s) {
        return ColorGradient.getGradientNameVer2(s);
    }



    /*
    * 根据sfid创建制定数量的堆叠物
    * 若sfid不存在,则物品变为石头
    * */
    public static ItemStack SfItemWithAmount(String s, int amount) {
        ItemStack i = stoneCreateItem(s);
        i.setAmount(amount);
        return i;
    }

    /*
    * 生成ID卡-中文-乱码科技
    * */
    public static ItemStack IDCard() {
        char ans;
        int start = 0x3400;
        int end = 0x9FFF;
        int range = end - start;
        int randomOffset = new Random().nextInt(range);
        ans = (char) (start + randomOffset);
        ItemStack it = new CustomItemStack(Material.SUGAR, ColorGradient.getGradientNameVer2("Symbol"), "&f" + ans, ("§7The foundation of creating matter from nothing."));
        ItemStack s = new SlimefunItemStack("MOMOTECH_LETTER", it);
        return s;
    }






}
