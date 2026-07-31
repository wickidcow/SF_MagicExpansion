package io.Yomicer.magicExpansion.items.quickMachine;

import io.Yomicer.magicExpansion.specialActions.EntitySpawner;
import io.Yomicer.magicExpansion.utils.ItemPermissionUtils;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;

import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientName;
import static org.bukkit.inventory.EquipmentSlot.HAND;

public class MagicExpansionRandomSummon extends SimpleSlimefunItem<ItemUseHandler> implements NotPlaceable {

    private final EntitySpawner entitySpawner;

    public MagicExpansionRandomSummon(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
        this.entitySpawner = new EntitySpawner();
    }

    @Nonnull
    @Override
    public ItemUseHandler getItemHandler() {
        return e->{
            e.setUseItem(Event.Result.DENY);
            e.setUseBlock(Event.Result.DENY);

                Player player = e.getPlayer();
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                // 检查玩家手上是否有物品
                if (e.getHand()!= HAND) {
                    player.sendMessage(getGradientName("Hold this item in your main hand to use it.", createColorList()));
                    return;
                }
                if(!ItemPermissionUtils.hasPermissionRe(player)){
                    return;
                }

                    // 减少手上的物品数量
                    if (itemInHand.getAmount() > 1) {
                        itemInHand.setAmount(itemInHand.getAmount() - 1);
                    } else {
                        player.getInventory().setItemInMainHand(null); // 如果数量为 1,则直接移除
                    }
                    // 使用 entitySpawner
                    // 在距离玩家 3 格的位置随机召唤一个生物
                    // 调用 EntitySpawner 的实例方法
                    entitySpawner.spawnRandomEntityNearPlayer(player);


        };
    }





    /*覆写颜色*/
    /**
     * 将 RGB 值转换为 Minecraft 颜色代码字符
     *
     * @param value RGB 分量值(0-255)
     * @return 十六进制字符(0-9 或 A-F)
     */
    public static char codeColor(int value) {
        if (value < 0 || value > 15) {
            throw new IllegalArgumentException("Invalid color value: " + value);
        }
        return "0123456789ABCDEF".charAt(value);
    }

    /**
     * 生成带有渐变色的字符串(Minecraft §x 格式)
     *
     * @param text       输入的字符串
     * @param colorList  渐变色列表
     * @return 带有渐变色的字符串
     */
    public static String getGradientName(String text, List<Color> colorList) {
        StringBuilder stringBuilder = new StringBuilder();

        // 如果文本为空或长度不足,补全空格
        if (text.length() == 0) {
            text += " ";
        }
        if (text.length() == 1) {
            text += " ";
        }

        int length = text.length();
        for (int i = 0; i < length; i++) {
            double p = ((double) i) / (length - 1) * (colorList.size() - 1); // 插值进度
            Color color1 = colorList.get((int) Math.floor(p)); // 起始颜色
            Color color2 = colorList.get((int) Math.ceil(p));  // 结束颜色

            // 计算插值后的 RGB 值
            int red = (int) (color1.getRed() * (1 - p + Math.floor(p)) + color2.getRed() * (p - Math.floor(p)));
            int green = (int) (color1.getGreen() * (1 - p + Math.floor(p)) + color2.getGreen() * (p - Math.floor(p)));
            int blue = (int) (color1.getBlue() * (1 - p + Math.floor(p)) + color2.getBlue() * (p - Math.floor(p)));

            // 构建 Minecraft 颜色代码
            stringBuilder.append("§x")
                    .append("§").append(codeColor(red / 16))
                    .append("§").append(codeColor(red % 16))
                    .append("§").append(codeColor(green / 16))
                    .append("§").append(codeColor(green % 16))
                    .append("§").append(codeColor(blue / 16))
                    .append("§").append(codeColor(blue % 16));

            // 添加当前字符
            stringBuilder.append(text.charAt(i));
        }

        return stringBuilder.toString();
    }

    private static List<Color> createColorList() {
        List<Color> colorList = new ArrayList<>();
        colorList.add(Color.fromRGB(253, 183, 212)); // 淡粉色
        colorList.add(Color.fromRGB(250, 126, 179)); // 中间色
        colorList.add(Color.fromRGB(255, 105, 180)); // 亮粉色
        return colorList;
    }


}
