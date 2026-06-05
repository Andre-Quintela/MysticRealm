package com.nashgoldd.mysticrealm.supernatural.vampire.client;

public final class ClientDrainState {

    public static boolean isDraining       = false;
    public static int ticksElapsed         = 0;
    public static int totalTicks           = 60;
    public static int cooldownTicks        = 0;
    public static float targetBloodCurrent = 0f;
    public static float targetBloodMax     = 0f;

    private ClientDrainState() {}

    public static void reset() {
        isDraining = false;
        ticksElapsed = 0;
        cooldownTicks = 0;
        targetBloodCurrent = 0f;
        targetBloodMax = 0f;
    }
}
