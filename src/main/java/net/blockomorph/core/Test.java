package net.blockomorph.core;

import org.spongepowered.asm.mixin.Mixins;

public class Test {

    public static boolean get() {
        return defaultM();
    }

    public static boolean op1() {
        return true;
    }

    public static boolean op2() {
        return false;
    }


    public static boolean defaultM() {
        return System.getenv().isEmpty();
    }

    public static void test() {
        //Mixins.addConfiguration();
    }
}
