package com.nashgoldd.mysticrealm.supernatural.vampire.client.screen;

import com.nashgoldd.mysticrealm.registry.MysticAttachments;
import com.nashgoldd.mysticrealm.supernatural.vampire.attachment.VampireData;
import com.nashgoldd.mysticrealm.supernatural.vampire.progression.VampireProgressionService;
import com.nashgoldd.mysticrealm.supernatural.vampire.progression.VampireRank;
import com.nashgoldd.mysticrealm.supernatural.vampire.service.VampireService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public class VampireObeliskScreen extends Screen {

    private static final int BG_COLOR     = 0xDD0A0A0F;
    private static final int BORDER_COLOR = 0xFF6B0000;
    private static final int TITLE_COLOR  = 0xFFCC2222;
    private static final int LABEL_COLOR  = 0xFFAA6666;
    private static final int VALUE_COLOR  = 0xFFFFDDDD;
    private static final int LORE_COLOR   = 0xFFAA9999;
    private static final int MAXED_COLOR  = 0xFFFFD700;

    private static final int W = 300;
    private static final int H = 180;

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

        // Fundo e bordas
        g.fill(x, y, x + W, y + H, BG_COLOR);
        g.fill(x,         y,         x + W,     y + 1,     BORDER_COLOR);
        g.fill(x,         y + H - 1, x + W,     y + H,     BORDER_COLOR);
        g.fill(x,         y,         x + 1,     y + H,     BORDER_COLOR);
        g.fill(x + W - 1, y,         x + W,     y + H,     BORDER_COLOR);
        // Divisor central
        g.fill(x + W / 2, y + 16, x + W / 2 + 1, y + H - 8, BORDER_COLOR);

        // Título
        String titleStr = title.getString();
        int titleX = x + W / 2 - font.width(titleStr) / 2;
        g.text(font, titleStr, titleX, y + 6, TITLE_COLOR, true);

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        boolean isVampire = VampireService.isVampire(player);

        // --- Painel Esquerdo: Progressão (vampiros) ou dica (humanos) ---
        int lx = x + 8;
        int ly = y + 20;

        if (isVampire) {
            VampireData data = player.getData(MysticAttachments.VAMPIRE_DATA);
            VampireRank rank = data.getRank();
            long essence = data.getBloodEssence();
            long ageTicks = data.getVampireAgeTicks();

            g.text(font, "Rank: " + rank.displayName(), lx, ly, LABEL_COLOR, false);
            ly += 12;

            g.text(font, "Blood Essence: " + formatLong(essence), lx, ly, VALUE_COLOR, false);
            ly += 12;

            g.text(font, "Vampire Age: " + formatAge(ageTicks), lx, ly, VALUE_COLOR, false);
            ly += 16;

            Optional<VampireRank> next = rank.next();
            if (next.isPresent()) {
                VampireRank nextRank = next.get();
                g.text(font, "Next Rank: " + nextRank.displayName(), lx, ly, LABEL_COLOR, false);
                ly += 10;

                long needed = VampireProgressionService.getRequiredEssence(rank);
                long missing = Math.max(0, needed - essence);
                g.text(font, "Required: " + formatLong(needed) + "  (Missing: " + formatLong(missing) + ")", lx, ly, VALUE_COLOR, false);
                ly += 10;

                long neededHours = VampireProgressionService.getRequiredAgeHours(rank);
                g.text(font, "Required age: " + neededHours + "h", lx, ly, VALUE_COLOR, false);
            } else {
                g.text(font, "You have reached the pinnacle of vampiric power.", lx, ly, MAXED_COLOR, false);
            }
        } else {
            g.text(font, "Drink the Vampire Blood Vial", lx, ly, LORE_COLOR, false);
            ly += 10;
            g.text(font, "to begin your descent into darkness.", lx, ly, LORE_COLOR, false);
        }

        // --- Painel Direito: Lore ---
        int rx = x + W / 2 + 8;
        int ry = y + 20;
        int lineWidth = W / 2 - 16;
        String[] loreLines = wrapText(font,
            "In ages past, the ancient ones raised these pillars to guide the newly risen through the darkness...",
            lineWidth);
        for (String line : loreLines) {
            g.text(font, line, rx, ry, LORE_COLOR, false);
            ry += 10;
        }

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
