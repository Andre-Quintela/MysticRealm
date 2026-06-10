package com.nashgoldd.mysticrealm.supernatural.vampire.client.screen;

import com.nashgoldd.mysticrealm.MysticRealm;
import com.nashgoldd.mysticrealm.registry.MysticAttachments;
import com.nashgoldd.mysticrealm.supernatural.vampire.attachment.VampireData;
import com.nashgoldd.mysticrealm.supernatural.vampire.progression.VampireRank;
import com.nashgoldd.mysticrealm.supernatural.vampire.service.VampireService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class VampireObeliskScreen extends Screen {

    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(MysticRealm.MODID, "textures/gui/vampire_obelisk_background.png");

    private static final int TITLE_COLOR = 0xFFCC2222;
    private static final int VALUE_COLOR = 0xFFFFDDDD;
    private static final int LORE_COLOR  = 0xFFAA9999;

    private static final int W = 600;
    private static final int H = 442;

    // Painel de progressão (apenas para vampiros)
    private static final int LEVEL_X = 95,   LEVEL_Y = 297;
    private static final int ESSENCE_X = 95, ESSENCE_Y = 335;
    private static final int AGE_X = 95,     AGE_Y = 373;

    // Painel de lore — preparado para paginação futura (botões anterior/próximo)
    private static final int LORE_X = 368, LORE_Y = 80;
    private static final int LORE_W = 160, LORE_H = 145;
    private static final int LORE_LINE_HEIGHT = 10;

    private static final List<String> LORE_PAGES = List.of(
        "In ages past, the ancient ones raised these pillars to guide the newly risen through the darkness..."
    );

    private int lorePage = 0;

    public VampireObeliskScreen() {
        super(Component.translatable("screen.mysticrealm.obelisk.title"));
    }

    @Override
    protected void init() {}

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        int x = (width - W) / 2;
        int y = (height - H) / 2;
        Font font = Minecraft.getInstance().font;

        // Fundo da GUI
        g.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, 0, 0, W, H, W, H);

        // Título (renderizado em escala 2x)
        String titleStr = title.getString();
        float titleScale = 1.5f;
        int titleX = x + W / 2 - (int) (font.width(titleStr) * titleScale) / 2;
        g.pose().pushMatrix();
        g.pose().scale(titleScale, titleScale);
        g.text(font, titleStr, (int) (titleX / titleScale), (int) ((y + 35) / titleScale), TITLE_COLOR, true);
        g.pose().popMatrix();

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if (VampireService.isVampire(player)) {
            VampireData data = player.getData(MysticAttachments.VAMPIRE_DATA);
            VampireRank rank = data.getRank();
            long essence = data.getBloodEssence();
            long ageTicks = data.getVampireAgeTicks();

            g.text(font, "Vampire Level: " + rank.displayName(), x + LEVEL_X, y + LEVEL_Y, VALUE_COLOR, false);
            g.text(font, "Blood Essence: " + formatLong(essence), x + ESSENCE_X, y + ESSENCE_Y, VALUE_COLOR, false);
            g.text(font, "Age: " + formatAge(ageTicks), x + AGE_X, y + AGE_Y, VALUE_COLOR, false);
        }

        // Painel de Lore
        renderLore(g, font, x, y);
    }

    private void renderLore(GuiGraphicsExtractor g, Font font, int x, int y) {
        String[] lines = wrapText(font, LORE_PAGES.get(lorePage), LORE_W);
        int maxLines = LORE_H / LORE_LINE_HEIGHT;
        int lineCount = Math.min(lines.length, maxLines);

        int ly = y + LORE_Y;
        for (int i = 0; i < lineCount; i++) {
            g.text(font, lines[i], x + LORE_X, ly, LORE_COLOR, false);
            ly += LORE_LINE_HEIGHT;
        }
    }

    /** Avança para a próxima página de lore (com wrap-around). Pronto para uso por um botão futuro. */
    public void nextLorePage() {
        lorePage = (lorePage + 1) % LORE_PAGES.size();
    }

    /** Volta para a página de lore anterior (com wrap-around). Pronto para uso por um botão futuro. */
    public void previousLorePage() {
        lorePage = (lorePage - 1 + LORE_PAGES.size()) % LORE_PAGES.size();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static String[] wrapText(Font font, String text, int maxWidth) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String test = current.isEmpty() ? word : current + " " + word;
            if (font.width(test) > maxWidth && !current.isEmpty()) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(test);
            }
        }
        if (!current.isEmpty()) lines.add(current.toString());
        return lines.toArray(new String[0]);
    }

    private static String formatLong(long value) {
        if (value >= 1_000_000) return String.format("%.1fM", value / 1_000_000.0);
        if (value >= 1_000) return String.format("%,d", value);
        return String.valueOf(value);
    }

    private static String formatAge(long ticks) {
        long totalMinutes = ticks / 1200;
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        if (hours == 0 && minutes == 0) return "< 1m";
        if (hours == 0) return minutes + "m";
        return hours + "h " + minutes + "m";
    }
}
