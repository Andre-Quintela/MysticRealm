package com.nashgoldd.mysticrealm.supernatural.vampire.client;

import com.nashgoldd.mysticrealm.MysticRealm;
import com.nashgoldd.mysticrealm.supernatural.vampire.attachment.VampireData;
import com.nashgoldd.mysticrealm.supernatural.vampire.service.VampireService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = MysticRealm.MODID, value = Dist.CLIENT)
public final class VampireHudOverlay {

    private VampireHudOverlay() {}

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        if (!VampireService.isVampire(player)) return;

        VampireData data = VampireService.getData(player);
        Font font = mc.font;
        GuiGraphicsExtractor g = event.getGuiGraphics();

        // Indicador de sangue — canto superior esquerdo
        String bloodText = "Blood: " + data.getBloodLevel() + "/" + data.getMaxBlood();
        g.text(font, bloodText, 10, 10, 0xFFAA0000, true);

        // Mensagem de quase-morte
        if (data.isNearDeath()) {
            g.text(font, "Your immortal body struggles to regenerate...", 10, 22, 0xFFFF4444, true);
        }

        // Indicador de luz solar
        if (data.isSunlightBurning()) {
            g.text(font, "Sunlight burning!", 10, 34, 0xFFFF8800, true);
        }
    }
}
