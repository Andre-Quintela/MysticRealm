package com.nashgoldd.mysticrealm.supernatural.vampire.client.screen;

import com.nashgoldd.mysticrealm.MysticRealm;
import com.nashgoldd.mysticrealm.client.gui.widget.TexturedButton;
import com.nashgoldd.mysticrealm.registry.MysticAttachments;
import com.nashgoldd.mysticrealm.supernatural.multiblock.client.ClientBuildFeedback;
import com.nashgoldd.mysticrealm.supernatural.multiblock.client.ClientStructureFeedback;
import com.nashgoldd.mysticrealm.supernatural.multiblock.network.RequestStructureBuildPacket;
import com.nashgoldd.mysticrealm.supernatural.multiblock.network.RequestStructureValidationPacket;
import com.nashgoldd.mysticrealm.supernatural.vampire.attachment.VampireData;
import com.nashgoldd.mysticrealm.supernatural.vampire.multiblock.VampireStructures;
import com.nashgoldd.mysticrealm.supernatural.vampire.progression.VampireRank;
import com.nashgoldd.mysticrealm.supernatural.vampire.service.VampireService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

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

    private static final int VALIDATE_BUTTON_W = 120, VALIDATE_BUTTON_H = 20;
    private static final int VALIDATE_BUTTON_X = W / 2 - VALIDATE_BUTTON_W / 2, VALIDATE_BUTTON_Y = H - 30;
    private static final int FEEDBACK_X = W / 2, FEEDBACK_Y = H - 50;
    private static final int FEEDBACK_COLOR_OK = 0xFF55FF55;
    private static final int FEEDBACK_COLOR_BAD = 0xFFFF5555;
    private static final int FEEDBACK_COLOR_NEUTRAL = 0xFFDDDDDD;

    // Painel de informações do Blood Altar
    private static final int ALTAR_PANEL_X = 357, ALTAR_PANEL_Y = 279;
    private static final int ALTAR_PANEL_W = 186, ALTAR_PANEL_H = 111;
    private static final int ALTAR_LEVEL_X = ALTAR_PANEL_X + 10, ALTAR_LEVEL_Y = ALTAR_PANEL_Y + 10;

    private static final int PANEL_BUILD_BUTTON_W = 142, PANEL_BUILD_BUTTON_H = 33;
    private static final int PANEL_BUILD_BUTTON_X = ALTAR_PANEL_X + (ALTAR_PANEL_W - PANEL_BUILD_BUTTON_W) / 2;
    private static final int PANEL_BUILD_BUTTON_Y = ALTAR_PANEL_Y + ALTAR_PANEL_H - PANEL_BUILD_BUTTON_H - 12;
    private static final Identifier BUILD_BUTTON_SPRITE =
        Identifier.fromNamespaceAndPath(MysticRealm.MODID, "build_button");

    private static final int BUILD_FEEDBACK_X = ALTAR_PANEL_X + ALTAR_PANEL_W / 2;
    private static final int BUILD_FEEDBACK_Y = PANEL_BUILD_BUTTON_Y + PANEL_BUILD_BUTTON_H + 4;

    private int lorePage = 0;
    private final BlockPos obeliskPos;
    private TexturedButton buildButton;

    public VampireObeliskScreen(BlockPos obeliskPos) {
        super(Component.translatable("screen.mysticrealm.obelisk.title"));
        this.obeliskPos = obeliskPos;
    }

    @Override
    protected void init() {
        float scale = guiScale();
        float offsetX = (width - W * scale) / 2f;
        float offsetY = (height - H * scale) / 2f;

        addRenderableWidget(Button.builder(Component.translatable("screen.mysticrealm.obelisk.validate"),
                button -> ClientPacketDistributor.sendToServer(new RequestStructureValidationPacket(obeliskPos)))
            .bounds(
                (int) (offsetX + VALIDATE_BUTTON_X * scale),
                (int) (offsetY + VALIDATE_BUTTON_Y * scale),
                (int) (VALIDATE_BUTTON_W * scale),
                (int) (VALIDATE_BUTTON_H * scale)
            )
            .build());

        buildButton = TexturedButton.create(
            Component.translatable("screen.mysticrealm.obelisk.build"),
            BUILD_BUTTON_SPRITE, VALUE_COLOR,
            (int) (offsetX + PANEL_BUILD_BUTTON_X * scale),
            (int) (offsetY + PANEL_BUILD_BUTTON_Y * scale),
            (int) (PANEL_BUILD_BUTTON_W * scale),
            (int) (PANEL_BUILD_BUTTON_H * scale),
            button -> ClientPacketDistributor.sendToServer(new RequestStructureBuildPacket(obeliskPos))
        );
        addRenderableWidget(buildButton);
    }

    private float guiScale() {
        return Math.min(1.0f, Math.min((width - MARGIN) / (float) W, (height - MARGIN) / (float) H));
    }

    private static final int MARGIN = 20;

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        Font font = Minecraft.getInstance().font;

        // Escala a GUI para baixo quando a tela for menor que 600x442 (nunca amplia além de 1.0)
        float scale = guiScale();
        float offsetX = (width - W * scale) / 2f;
        float offsetY = (height - H * scale) / 2f;

        g.pose().pushMatrix();
        g.pose().translate(offsetX, offsetY);
        g.pose().scale(scale, scale);

        // Fundo da GUI
        g.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, 0, 0, 0, 0, W, H, W, H);

        // Título (renderizado em escala 1.5x adicional)
        String titleStr = title.getString();
        float titleScale = 1.5f;
        int titleX = W / 2 - (int) (font.width(titleStr) * titleScale) / 2;
        g.pose().pushMatrix();
        g.pose().scale(titleScale, titleScale);
        g.text(font, titleStr, (int) (titleX / titleScale), (int) (35 / titleScale), TITLE_COLOR, true);
        g.pose().popMatrix();

        Player player = Minecraft.getInstance().player;
        if (player != null && VampireService.isVampire(player)) {
            VampireData data = player.getData(MysticAttachments.VAMPIRE_DATA);
            VampireRank rank = data.getRank();
            long essence = data.getBloodEssence();
            long ageTicks = data.getVampireAgeTicks();

            g.text(font, "Vampire Level: " + rank.displayName(), LEVEL_X, LEVEL_Y, VALUE_COLOR, false);
            g.text(font, "Blood Essence: " + formatLong(essence), ESSENCE_X, ESSENCE_Y, VALUE_COLOR, false);
            g.text(font, "Age: " + formatAge(ageTicks), AGE_X, AGE_Y, VALUE_COLOR, false);
        }

        // Painel de informações do Blood Altar
        g.text(font, "Altar Level: " + VampireStructures.BLOOD_ALTAR_LVL1.tier(), ALTAR_LEVEL_X, ALTAR_LEVEL_Y, VALUE_COLOR, false);

        // Painel de Lore
        renderLore(g, font);

        // Feedback da última validação de estrutura (se houver e ainda não expirou)
        if (ClientStructureFeedback.isActive() && obeliskPos.equals(ClientStructureFeedback.controllerPos)) {
            String feedbackText = "Estrutura: " + (int) ClientStructureFeedback.percentCompleted + "%";
            int feedbackColor = ClientStructureFeedback.valid ? FEEDBACK_COLOR_OK : FEEDBACK_COLOR_BAD;
            int textX = FEEDBACK_X - font.width(feedbackText) / 2;
            g.text(font, feedbackText, textX, FEEDBACK_Y, feedbackColor, false);
        }

        // Feedback da última tentativa de construção automática (se houver e ainda não expirou)
        if (ClientBuildFeedback.isActive() && obeliskPos.equals(ClientBuildFeedback.controllerPos)) {
            String buildText = buildFeedbackText();
            int buildColor = buildFeedbackColor();
            int textX = BUILD_FEEDBACK_X - font.width(buildText) / 2;
            g.text(font, buildText, textX, BUILD_FEEDBACK_Y, buildColor, false);
        }

        g.pose().popMatrix();

        // Desenhado depois do fundo da tela, para garantir que apareça por cima dele.
        buildButton.renderDecoration(g, scale);
    }

    private String buildFeedbackText() {
        return switch (ClientBuildFeedback.reason) {
            case SUCCESS -> Component.translatable("screen.mysticrealm.obelisk.build.success").getString()
                + " (-" + String.format("%.1f", ClientBuildFeedback.healthCost) + ")";
            case ALREADY_COMPLETE -> Component.translatable("screen.mysticrealm.obelisk.build.alreadyComplete").getString();
            case INSUFFICIENT_HEALTH -> Component.translatable("screen.mysticrealm.obelisk.build.insufficientHealth").getString()
                + " (" + String.format("%.1f", ClientBuildFeedback.healthCost) + ")";
            case MISSING_ITEMS -> {
                StringBuilder sb = new StringBuilder(Component.translatable("screen.mysticrealm.obelisk.build.missingItems").getString());
                List<ItemStack> missing = ClientBuildFeedback.missingItems;
                for (int i = 0; i < missing.size(); i++) {
                    ItemStack stack = missing.get(i);
                    if (i > 0) sb.append(", ");
                    sb.append(stack.getCount()).append("x ").append(stack.getHoverName().getString());
                }
                yield sb.toString();
            }
        };
    }

    private int buildFeedbackColor() {
        return switch (ClientBuildFeedback.reason) {
            case SUCCESS -> FEEDBACK_COLOR_OK;
            case ALREADY_COMPLETE -> FEEDBACK_COLOR_NEUTRAL;
            case INSUFFICIENT_HEALTH, MISSING_ITEMS -> FEEDBACK_COLOR_BAD;
        };
    }

    private void renderLore(GuiGraphicsExtractor g, Font font) {
        String[] lines = wrapText(font, LORE_PAGES.get(lorePage), LORE_W);
        int maxLines = LORE_H / LORE_LINE_HEIGHT;
        int lineCount = Math.min(lines.length, maxLines);

        int ly = LORE_Y;
        for (int i = 0; i < lineCount; i++) {
            g.text(font, lines[i], LORE_X, ly, LORE_COLOR, false);
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
