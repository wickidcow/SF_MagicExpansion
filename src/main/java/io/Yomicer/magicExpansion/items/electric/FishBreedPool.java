package io.Yomicer.magicExpansion.items.electric;

import io.Yomicer.magicExpansion.MagicExpansion;
import io.Yomicer.magicExpansion.items.abstracts.MenuBlock;
import io.Yomicer.magicExpansion.items.misc.fish.FishAttributeGenerator;
import io.Yomicer.magicExpansion.items.misc.fish.Gen2Fish;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

import static io.Yomicer.magicExpansion.utils.ColorGradient.getGradientNameVer2;
import static io.Yomicer.magicExpansion.utils.Utils.doGlow;

/**
 * 育种池: 消耗两条二代鱼, 产出一条子代鱼。
 * <p>
 * 杂交算法(第二层):
 * <ul>
 *   <li>子代元素 = 父1×0.4 + 父2×0.4, 20% 概率逐维附加 ±0.05 变异偏移(随后归一化 sum=1)；</li>
 *   <li>子代品质系数 = (父1+父2)/2 ± 0.05, 钳制在 0.7~2.5；</li>
 *   <li>子代基因型: 30% 概率继承父母一方, 70% 概率随机(加速/合成 50/50)；</li>
 *   <li>子代鱼种(外观)随机取父本或母本一方。</li>
 * </ul>
 */
public class FishBreedPool extends MenuBlock {

    // ===== 27 格(3×9) 按用户图片布局 =====
    // 第一行第5格: 信息栏(提示杂交方式与系数公式)
    private static final int SLOT_INFO = 4;
    // 第二行第3格 / 第7格: 输入槽(放置父鱼/母鱼), 均为空槽可放鱼
    private static final int SLOT_PARENT_1 = 11;
    private static final int SLOT_PARENT_2 = 15;
    // 第二行第5格: 杂交按钮(点击Start Breeding), 材质=原版钟
    private static final int SLOT_BUTTON = 13;
    // 第三行第5格: 输出槽(取出子代)
    private static final int SLOT_OUTPUT = 22;

    // 第二行蓝色玻璃板说明槽(第2/4/6/8格; 材质与名字用二代渐变色, lore 由用户自行填写)
    private static final int SLOT_DECOR_A = 10;
    private static final int SLOT_DECOR_B = 12;
    private static final int SLOT_DECOR_C = 14;
    private static final int SLOT_DECOR_D = 16;

    public FishBreedPool(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe);
    }

    @Override
    protected void setup(BlockMenuPreset preset) {
        // 菜单尺寸 27 格(3×9)
        preset.setSize(27);

        // 背景: 浅灰玻璃板铺满"除功能槽/信息栏/按钮/彩色指示玻璃板"外的格子
        // 功能槽(11/15/22)保持空槽, 可放鱼取鱼
        preset.drawBackground(new CustomItemStack(Material.WHITE_STAINED_GLASS_PANE, " "), new int[]{
                0, 1, 2, 3, 5, 6, 7, 8,
                9, 17,
                18, 19, 20, 24, 25, 26
        });

        // ===== 第一行第5格 (下标4): 信息栏 —— 杂交方式 + 系数公式 =====
        preset.addItem(SLOT_INFO, new CustomItemStack(doGlow(Material.BOOK),
                        getGradientNameVer2("Breeding Pool · Crossbreeding Rules"),
                        getGradientNameVer2("Offspring elements = Parent 1×0.4 + Parent 2×0.4"),
                        getGradientNameVer2("20% chance per element for a ±5% mutation"),
                        getGradientNameVer2("Offspring quality = parent average ±0.05"),
                        getGradientNameVer2("Trait: 30% inherited, 70% random"),
                        getGradientNameVer2("Offspring species is inherited from either parent")),
                (p, s, i, a) -> false);

        // 说明 lore 每行用 getGradientNameVer2 包成"魔法2代渐变色"
        preset.addItem(SLOT_DECOR_A, new CustomItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                        getGradientNameVer2("Parent Fish 1"),
                        getGradientNameVer2("")),
                (p, s, i, a) -> false);
        preset.addItem(SLOT_DECOR_B, new CustomItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                        getGradientNameVer2(""),
                        getGradientNameVer2("")),
                (p, s, i, a) -> false);
        preset.addItem(SLOT_DECOR_C, new CustomItemStack(Material.BLUE_STAINED_GLASS_PANE,
                        getGradientNameVer2("Parent Fish 2"),
                        getGradientNameVer2("")),
                (p, s, i, a) -> false);
        preset.addItem(SLOT_DECOR_D, new CustomItemStack(Material.BLUE_STAINED_GLASS_PANE,
                        getGradientNameVer2(""),
                        getGradientNameVer2("")),
                (p, s, i, a) -> false);

        // ===== 第三行输出槽两侧的橙色指示玻璃板 (下标 21/23), 每个槽位独立 =====
        preset.addItem(21, new CustomItemStack(Material.ORANGE_STAINED_GLASS_PANE,
                        getGradientNameVer2(""),
                        getGradientNameVer2("")),
                (p, s, i, a) -> false);   // 21 橙
        preset.addItem(23, new CustomItemStack(Material.ORANGE_STAINED_GLASS_PANE,
                        getGradientNameVer2(""),
                        getGradientNameVer2("")),
                (p, s, i, a) -> false);   // 23 橙

        // ===== 第二行第5格 (下标13): 杂交按钮 = 原版钟 =====
        preset.addItem(SLOT_BUTTON, new CustomItemStack(doGlow(Material.BELL),
                        getGradientNameVer2("Start Breeding"),
                        getGradientNameVer2("Consumes two parent fish and produces one offspring"),
                        getGradientNameVer2("Processing time: 2 seconds")),
                (p, s, item, a) -> {
                    onBreedClick(p);
                    return false;
                });
    }

    @Override
    protected int[] getInputSlots() {
        return new int[]{SLOT_PARENT_1, SLOT_PARENT_2};
    }

    @Override
    protected int[] getOutputSlots() {
        return new int[]{SLOT_OUTPUT};
    }

    @Override
    protected int[] getInputSlots(DirtyChestMenu menu, ItemStack item) {
        return getInputSlots();
    }

    /** 点击杂交按钮: 先做前置校验, 若通过则进入 2 秒制作阶段, 到期后真正产出子代 */
    private void onBreedClick(Player player) {
        if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof BlockMenu menu)) {
            return;
        }

        ItemStack f1 = menu.getItemInSlot(SLOT_PARENT_1);
        ItemStack f2 = menu.getItemInSlot(SLOT_PARENT_2);
        if (!FishAttributeGenerator.isGen2Fish(f1) || !FishAttributeGenerator.isGen2Fish(f2)) {
            player.sendMessage(getGradientNameVer2("Breeding Pool: insert two Generation 2 fish."));
            return;
        }

        // 输出槽为空才能开始制作
        ItemStack occupied = menu.getItemInSlot(SLOT_OUTPUT);
        if (occupied != null && !occupied.getType().isAir()) {
            player.sendMessage(getGradientNameVer2("Breeding Pool: output slot is full; remove the offspring first."));
            return;
        }

        // 校验用户是否持有该机器放置权限(可选, 沿用 MenuBlock 默认行为即可)

        // 锁定按钮为"制作中", 防止 2 秒内重复点击
        menu.replaceExistingItem(SLOT_BUTTON, craftingItem());
        player.sendMessage(getGradientNameVer2("Breeding... processing for 2 seconds."));

        // 2 秒(40 tick)后执行真正杂交
        Bukkit.getScheduler().runTaskLater(MagicExpansion.getInstance(), () -> {
            // 重新从菜单读取当前父鱼, 避免制作期间玩家取走/更换
            ItemStack cur1 = menu.getItemInSlot(SLOT_PARENT_1);
            ItemStack cur2 = menu.getItemInSlot(SLOT_PARENT_2);
            BlockMenu fresh = menu;
            if (!FishAttributeGenerator.isGen2Fish(cur1) || !FishAttributeGenerator.isGen2Fish(cur2)) {
                player.sendMessage(getGradientNameVer2("Breeding Pool: a parent fish was removed; breeding cancelled."));
                resetButton(fresh);
                return;
            }
            completeBreed(player, fresh, cur1, cur2);
        }, 40L);
    }

    /** 制作中的按钮图标 */
    private CustomItemStack craftingItem() {
        return new CustomItemStack(doGlow(Material.CLOCK),
                getGradientNameVer2("Breeding..."),
                getGradientNameVer2("Processing for 2 seconds"));
    }

    /** 还原按钮为初始状态 */
    private void resetButton(BlockMenu menu) {
        menu.replaceExistingItem(SLOT_BUTTON, new CustomItemStack(doGlow(Material.BELL),
                getGradientNameVer2("Start Breeding"),
                getGradientNameVer2("Consumes two parent fish and produces one offspring"),
                getGradientNameVer2("The output slot must be empty to breed."),
                getGradientNameVer2("Breeding completes 2 seconds after clicking.")));
    }

    /** 真正执行杂交: 读取两条父鱼 → 计算子代 → 放入输出槽 → 清空输入槽 → 还原按钮 */
    private void completeBreed(Player player, BlockMenu menu, ItemStack f1, ItemStack f2) {
        Gen2Fish type1 = FishAttributeGenerator.getType(f1);
        Gen2Fish type2 = FishAttributeGenerator.getType(f2);
        double[] e1 = FishAttributeGenerator.getElements(f1);
        double[] e2 = FishAttributeGenerator.getElements(f2);
        if (type1 == null || type2 == null || e1 == null || e2 == null) {
            player.sendMessage(getGradientNameVer2("Breeding Pool: invalid parent data; breeding cancelled."));
            resetButton(menu);
            return;
        }

        // 子代鱼种(外观): 随机取父本或母本一方
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        Gen2Fish childType = rnd.nextBoolean() ? type1 : type2;

        // 子代元素 = 父1×0.4 + 父2×0.4; 20% 概率逐维 ±0.05 变异偏移(breed 内部会钳制并归一化)
        double[] childElements = new double[5];
        for (int i = 0; i < 5; i++) {
            childElements[i] = e1[i] * 0.4 + e2[i] * 0.4;
            if (rnd.nextDouble() < 0.20) {
                childElements[i] += (rnd.nextDouble() - 0.5) * 0.1;
            }
        }

        // 子代品质 = 父母均值 ± 0.05(内部钳制 0.7~2.5)
        double q1 = FishAttributeGenerator.getQuality(f1);
        double q2 = FishAttributeGenerator.getQuality(f2);
        double childQuality = (q1 + q2) / 2.0 + (rnd.nextDouble() - 0.5) * 0.1;

        // 基因型: 30% 继承父母一方(老鱼无基因时回退鱼种默认), 70% 随机
        Gen2Fish.Trait t1 = FishAttributeGenerator.getTrait(f1);
        Gen2Fish.Trait t2 = FishAttributeGenerator.getTrait(f2);
        Gen2Fish.Trait effT1 = t1 != null ? t1 : type1.getDefaultTrait();
        Gen2Fish.Trait effT2 = t2 != null ? t2 : type2.getDefaultTrait();
        Gen2Fish.Trait childTrait;
        if (rnd.nextDouble() < 0.30) {
            childTrait = rnd.nextBoolean() ? effT1 : effT2;
        } else {
            childTrait = rnd.nextBoolean() ? Gen2Fish.Trait.ACCEL : Gen2Fish.Trait.SYNTH;
        }

        // 输出槽再次确认仍为空(制作期间可能被占用)
        ItemStack occupied = menu.getItemInSlot(SLOT_OUTPUT);
        if (occupied != null && !occupied.getType().isAir()) {
            player.sendMessage(getGradientNameVer2("Breeding Pool: output slot is full; remove the offspring first."));
            resetButton(menu);
            return;
        }

        // 传父母鱼种 ID 以便子代 lore 反查亲本名字
        ItemStack child = FishAttributeGenerator.breed(childType, childElements, childQuality,
                childTrait, type1.getId(), type2.getId());
        menu.replaceExistingItem(SLOT_OUTPUT, child);
        menu.replaceExistingItem(SLOT_PARENT_1, null);
        menu.replaceExistingItem(SLOT_PARENT_2, null);
        resetButton(menu);
        player.sendMessage(getGradientNameVer2("Breeding complete!"));
    }
}