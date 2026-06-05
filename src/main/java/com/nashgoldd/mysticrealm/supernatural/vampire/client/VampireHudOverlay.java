package com.nashgoldd.mysticrealm.supernatural.vampire.client;

import com.nashgoldd.mysticrealm.MysticRealm;
import com.nashgoldd.mysticrealm.supernatural.vampire.attachment.VampireData;
import com.nashgoldd.mysticrealm.supernatural.vampire.service.VampireService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = MysticRealm.MODID, value = Dist.CLIENT)
public final class VampireHudOverlay {

    // Sprites registrados no GUI atlas — arquivos em textures/gui/sprites/blood_*.png
    // O atlas é construído automaticamente pelo MC a partir de textures/gui/sprites/
    private static final Identifier BLOOD_FULL  = Identifier.fromNamespaceAndPath(MysticRealm.MODID, "blood_full");
    private static final Identifier BLOOD_HALF  = Identifier.fromNamespaceAndPath(MysticRealm.MODID, "blood_half");
    private static final Identifier BLOOD_EMPTY = Identifier.fromNamespaceAndPath(MysticRealm.MODID, "blood_empty");

    private VampireHudOverlay() {}

    // Cancela a barra de fome vanilla para vampiros
    @SubscribeEvent
    public static void onRenderFoodPre(RenderGuiLayerEvent.Pre event) {
        if (!event.getName().equals(VanillaGuiLayers.FOOD_LEVEL)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !VampireService.isVampire(mc.player)) return;
        event.setCanceled(true);
    }

    // Desenha gotas de sangue e textos de estado após toda a GUI ser renderizada
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !VampireService.isVampire(player)) return;
        if (player.isCreative() || player.isSpectator()) return;

        int foodLevel = player.getFoodData().getFoodLevel(); // 0-20
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();
        GuiGraphicsExtractor g = event.getGuiGraphics();

        // 10 ícones na mesma posição da barra de fome vanilla (da direita para esquerda)
        // Ícone i=0 é o mais à direita e representa food units 20 e 19
        for (int i = 0; i < 10; i++) {
            int x = screenW / 2 + 82 - i * 8;
            int y = screenH - 39;
            int hi = (i + 1) * 2; // food unit superior deste ícone (esquerda esvazia primeiro)

            Identifier sprite;
            if (foodLevel >= hi)          sprite = BLOOD_FULL;
            else if (foodLevel >= hi - 1) sprite = BLOOD_HALF;
            else                          sprite = BLOOD_EMPTY;

            // blitSprite usa o GUI sprite atlas — abordagem correta para MC 1.21.4+
            g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 9, 9);
        }

        // Textos de estado vampírico
        VampireData data = VampireService.getData(player);
        Font font = mc.font;

        if (data.isNearDeath()) {
            g.text(font, "Your immortal body struggles to regenerate...", 10, 22, 0xFFFF4444, true);
        }
        if (data.isSunlightBurning()) {
            g.text(font, "Sunlight burning!", 10, 34, 0xFFFF8800, true);
        }

        // Barra de progresso de drenagem de sangue
        if (ClientDrainState.isDraining && ClientDrainState.totalTicks > 0) {
            int barW = 80;
            int barH = 5;
            int bx = screenW / 2 - barW / 2;
            int by = screenH - 60;

            g.fill(bx - 1, by - 1, bx + barW + 1, by + barH + 1, 0xFF000000);
            g.fill(bx, by, bx + barW, by + barH, 0xFF330000);
            int filled = barW * ClientDrainState.ticksElapsed / ClientDrainState.totalTicks;
            g.fill(bx, by, bx + filled, by + barH, 0xFFAA0000);
            g.text(font, "Draining...", bx, by - 10, 0xFFFF4444, true);
        }
    }
}
