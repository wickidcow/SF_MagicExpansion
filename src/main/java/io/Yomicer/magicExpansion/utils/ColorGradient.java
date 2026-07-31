package io.Yomicer.magicExpansion.utils;

import org.bukkit.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ColorGradient {
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
        // 如果未提供颜色列表,使用默认颜色列表
        if (colorList == null || colorList.isEmpty()) {
            colorList = createColorList();
        }

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

    /**
     * 生成带有渐变色的字符串(Minecraft §x 格式)
     *
     * @param text       输入的字符串
     * @param colorList  渐变色列表
     * @return 带有渐变色的字符串
     */
    public static String getGradientNameVer2(String text, List<Color> colorList) {
        // 如果未提供颜色列表,使用默认颜色列表
        if (colorList == null || colorList.isEmpty()) {
            colorList = createCustomColorListV2();
        }

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


    /**
     * 生成纵向渐变的 Lore 列表
     * - 自动计算总行数
     * - 自动使用默认颜色列表
     * - 行内颜色统一,行与行之间渐变
     *
     * @param lore 原始文本列表
     * @return 处理后的带颜色文本列表
     */
    public static List<String> getVerticalGradientLineV2(List<String> lore) {
        // 处理空列表情况
        if (lore == null || lore.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 自动获取默认颜色列表
        List<Color> colorList = createCustomColorListV2();

        // 2. 自动计算总行数
        int totalLines = lore.size();
        List<String> result = new ArrayList<>();

        // 3. 遍历每一行进行计算
        for (int lineIndex = 0; lineIndex < totalLines; lineIndex++) {
            String text = lore.get(lineIndex);

            if (text == null || text.isEmpty()) {
                result.add("");
                continue;
            }

            // 计算进度 p (0.0 ~ 1.0)
            double p;
            if (totalLines <= 1) {
                p = 0;
            } else {
                p = ((double) lineIndex) / (totalLines - 1);
            }

            // 确定起始和结束颜色索引
            int maxIndex = colorList.size() - 1;
            int index1 = (int) Math.floor(p * maxIndex);
            int index2 = (int) Math.ceil(p * maxIndex);

            // 边界保护
            if (index1 > maxIndex) index1 = maxIndex;
            if (index2 > maxIndex) index2 = maxIndex;

            Color color1 = colorList.get(index1);
            Color color2 = colorList.get(index2);

            Color finalColor;

            // 计算插值颜色
            if (index1 == index2) {
                finalColor = color1;
            } else {
                double localP = (p * maxIndex) - index1;

                int r1 = color1.getRed();
                int g1 = color1.getGreen();
                int b1 = color1.getBlue();

                int r2 = color2.getRed();
                int g2 = color2.getGreen();
                int b2 = color2.getBlue();

                int red = (int) (r1 * (1 - localP) + r2 * localP);
                int green = (int) (g1 * (1 - localP) + g2 * localP);
                int blue = (int) (b1 * (1 - localP) + b2 * localP);

                finalColor = Color.fromRGB(red, green, blue);
            }

            // 应用颜色并添加结果
            result.add(applySingleColor(text, finalColor));
        }

        return result;
    }

    private static String applySingleColor(String text, Color color) {
        StringBuilder sb = new StringBuilder();
        sb.append("§x")
                .append("§").append(codeColor(color.getRed() / 16))
                .append("§").append(codeColor(color.getRed() % 16))
                .append("§").append(codeColor(color.getGreen() / 16))
                .append("§").append(codeColor(color.getGreen() % 16))
                .append("§").append(codeColor(color.getBlue() / 16))
                .append("§").append(codeColor(color.getBlue() % 16));

        return sb.toString() + text;
    }







    /**
     * 生成带有渐变色的字符串(Minecraft § 格式)
     *
     * @param text 输入的字符串
     * @return 带有渐变色的字符串
     */
    public static String getGradientName(String text) {
        return getGradientName(text, null); // 调用原始方法,使用默认颜色列表
    }

    public static String getGradientNameVer2(String text) {
        return getGradientNameVer2(text, null); // 调用原始方法,使用默认颜色列表
    }



    //default color
    private static List<Color> createColorList() {
        List<Color> colorList = new ArrayList<>();
        colorList.add(Color.fromRGB(253, 183, 212)); // 淡粉色
        colorList.add(Color.fromRGB(250, 126, 179)); // 中间色
        colorList.add(Color.fromRGB(255, 105, 180)); // 亮粉色
        return colorList;
    }

    public static List<Color> createCustomColorListV2() {
        List<Color> colorList = new ArrayList<>();

        colorList.add(Color.fromRGB(0x6b, 0xee, 0xd1)); // #6beed1
        colorList.add(Color.fromRGB(0xaa, 0xcd, 0xd0)); // #aacdd0
        colorList.add(Color.fromRGB(0xbf, 0xba, 0xd0)); // #bfbad0
        colorList.add(Color.fromRGB(0x9c, 0x52, 0x8b)); // #9c528b
        colorList.add(Color.fromRGB(0xff, 0x32, 0xce)); // #ff32ce

        return colorList;
    }



    /**
     * 生成指定数量的随机颜色列表
     *
     * @param count 随机颜色的数量
     * @return 随机颜色列表
     */
    private static List<Color> createRandomColorList(int count) {
        List<Color> colorList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            int red = random.nextInt(256);
            int green = random.nextInt(256);
            int blue = random.nextInt(256);
            colorList.add(Color.fromRGB(red, green, blue));
        }
        return colorList;
    }

    /**
     * 生成带有随机渐变色的字符串(Minecraft §x 格式)
     *
     * @param text  输入的字符串
     * @param count 渐变色数量
     * @return 带有随机渐变色的字符串
     */
    public static String getRandomGradientName(String text, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be greater than zero");
        }
        List<Color> randomColorList = createRandomColorList(count);
        return getGradientName(text, randomColorList);
    }

    // 如果需要直接调用,不指定颜色数量,默认使用3种颜色进行渐变
    public static String getRandomGradientName(String text) {
        return getRandomGradientName(text, 3); // 默认三种颜色渐变
    }



}
