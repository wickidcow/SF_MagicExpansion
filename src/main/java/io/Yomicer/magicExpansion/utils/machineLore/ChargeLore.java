package io.Yomicer.magicExpansion.utils.machineLore;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;

public class ChargeLore {

    private static final DecimalFormat FORMAT = new DecimalFormat("###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###,###.#");
    private static final double TPS = (double)20.0F / (double) Slimefun.getTickerTask().getTickRate();
    private static final String PREFIX = "⇨ ⚡ ";

    @Nonnull
    public static String energyPerSecond(int energy) {
        return "⇨ ⚡ " + formatEnergy(energy) + " J/s";
    }

    @Nonnull
    public static String energyBuffer(int energy) {
        return "⇨ ⚡ " + format((double)energy) + "J";
    }

    @Nonnull
    public static String energyPerTick(int energy) {
        return "⇨ ⚡ " + format((double)energy) + " J/tick ";
    }

    @Nonnull
    public static String speed(int speed) {
        return "⇨ ⚡ Speed: " + speed + 'x';
    }


    @Nonnull
    public static String formatEnergyTick(int energy) {
        return FORMAT.format((double)energy);
    }


    @Nonnull
    public static String formatEnergy(int energy) {
        return FORMAT.format((double)energy * TPS);
    }

    @Nonnull
    public static String formatEnergyMin(int energy) {
        return FORMAT.format((double)energy * TPS *60);
    }

    @Nonnull
    public static String formatEnergyHour(int energy) {
        return FORMAT.format((double)energy * TPS *60*60);
    }
    @Nonnull
    public static String formatEnergyDay(int energy) {
        return FORMAT.format((double)energy * TPS *60*60*12);
    }
    @Nonnull
    public static String formatEnergyWeek(int energy) {
        return FORMAT.format((double)energy * TPS *60*60*12*7);
    }
    @Nonnull
    public static String formatEnergyMonth(int energy) {
        return FORMAT.format((double)energy * TPS *60*60*12*30);
    }
    @Nonnull
    public static String formatEnergyYear(int energy) {
        return FORMAT.format((double)energy * TPS *60*60*12*30*12);
    }
    @Nonnull
    public static String formatEnergyCentury(int energy) {
        return FORMAT.format((double)energy * TPS *60*60*12*30*12*100);
    }

    @Nonnull
    public static String format(double number) {
        return FORMAT.format(number);
    }

}
