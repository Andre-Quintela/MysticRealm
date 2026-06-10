package com.nashgoldd.mysticrealm.supernatural.vampire.client;

import com.nashgoldd.mysticrealm.MysticRealm;
import com.nashgoldd.mysticrealm.config.MysticClientConfig;
import com.nashgoldd.mysticrealm.supernatural.ability.AbilityRegistry;
import com.nashgoldd.mysticrealm.supernatural.ability.AbilityWheelData;
import com.nashgoldd.mysticrealm.supernatural.vampire.network.ToggleAbilityPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Input;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CalculatePlayerTurnEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Roda de habilidades renderizada como overlay de HUD (RenderGuiEvent.Post),
 * sem usar Screen — evita o recentering de cursor causado por
 * MouseHandler.releaseMouse()/grabMouse(). A direção é calculada a partir
 * do delta acumulado de MouseHandler.xpos()/ypos(), que é atualizado
 * normalmente mesmo com o mouse "grabbed" (CURSOR_DISABLED).
 */
@EventBusSubscriber(modid = MysticRealm.MODID, value = Dist.CLIENT)
public final class VampireAbilityWheelHud {

    private static final int BASE_INNER_RADIUS = 30;
    private static final int BASE_OUTER_RADIUS = 80;
    private static final int BASE_DEAD_ZONE    = 18;
    private static final int NUM_SLOTS         = AbilityWheelData.SLOTS;

    // Cores ARGB
    private static final int COLOR_EMPTY          = 0x44333333;
    private static final int COLOR_INACTIVE       = 0xAA660022;
    private static final int COLOR_INACTIVE_HOVER = 0xCC990033;
    private static final int COLOR_ACTIVE         = 0xCC220066;
    private static final int COLOR_ACTIVE_HOVER   = 0xFF4400AA;
    private static final int COLOR_LABEL          = 0xFFFFDDDD;
    private static final int COLOR_LABEL_ACTIVE   = 0xFFBBAAFF;
    private static final int COLOR_CENTER         = 0xFFFFFFFF;
    private static final int COLOR_CENTER_HINT    = 0xFF888888;
    private static final int COLOR_CURSOR         = 0xFFFFFFFF;

    private static boolean open;
    private static int     selectedSlot = -1;

    // Impede que a roda reabra no mesmo "segurar Z" após confirmar via clique do mouse.
    private static boolean suppressUntilRelease;

    // Direção acumulada desde a abertura da roda (em "pixels" de mouse delta).
    private static double virtualX;
    private static double virtualY;
    private static double lastRawX;
    private static double lastRawY;

    private VampireAbilityWheelHud() {}

    private static double scale() {
        return MysticClientConfig.ABILITY_WHEEL_SCALE.get();
    }

    private static int innerRadius() {
        return (int) Math.round(BASE_INNER_RADIUS * scale());
    }

    private static int outerRadius() {
        return (int) Math.round(BASE_OUTER_RADIUS * scale());
    }

    private static int deadZone() {
        return (int) Math.round(BASE_DEAD_ZONE * scale());
    }

    public static boolean isOpen() {
        return open;
    }

    public static void openWheel() {
        open         = true;
        selectedSlot = -1;
        virtualX     = 0;
        virtualY     = 0;

        MouseHandler mouse = Minecraft.getInstance().mouseHandler;
        lastRawX = mouse.xpos();
        lastRawY = mouse.ypos();
    }

    public static void closeWheel() {
        open = false;
        confirmSelection();
    }

    /** Fecha a roda confirmando a seleção via clique do mouse, suprimindo reabertura enquanto a tecla seguir pressionada. */
    public static void closeWheelViaClick() {
        open = false;
        confirmSelection();
        suppressUntilRelease = true;
    }

    private static void confirmSelection() {
        if (selectedSlot != -1 && ClientAbilityState.slots.containsKey(selectedSlot)) {
            ClientPacketDistributor.sendToServer(new ToggleAbilityPacket(selectedSlot));
        }
        selectedSlot = -1;
    }

    public static boolean isSuppressed() {
        return suppressUntilRelease;
    }

    public static void clearSuppress() {
        suppressUntilRelease = false;
    }

    /** Chamado a cada client tick enquanto a roda está aberta. */
    public static void trackMouse() {
        MouseHandler mouse = Minecraft.getInstance().mouseHandler;
        double rawX = mouse.xpos();
        double rawY = mouse.ypos();

        virtualX += (rawX - lastRawX);
        virtualY += (rawY - lastRawY);

        lastRawX = rawX;
        lastRawY = rawY;

        double maxR = outerRadius();
        double dist = Math.sqrt(virtualX * virtualX + virtualY * virtualY);
        if (dist > maxR) {
            double scale = maxR / dist;
            virtualX *= scale;
            virtualY *= scale;
        }

        updateSelection();
    }

    private static void updateSelection() {
        double dist = Math.sqrt(virtualX * virtualX + virtualY * virtualY);
        if (dist < deadZone()) {
            selectedSlot = -1;
            return;
        }

        float angleDeg = (float) Math.toDegrees(Math.atan2(virtualY, virtualX));
        float norm     = ((angleDeg + 90f) % 360f + 360f) % 360f;
        selectedSlot   = (int)(norm / (360f / NUM_SLOTS)) + 1;
        if (selectedSlot > NUM_SLOTS) selectedSlot = NUM_SLOTS;
    }

    // Zera o giro de câmera enquanto a roda está aberta.
    // mouseSensitivity = -1/3 faz (sens*0.6+0.2) = 0, anulando o turnPlayer
    // sem depender de releaseMouse()/grabMouse() (evita o recentering do cursor).
    @SubscribeEvent
    public static void onCalculatePlayerTurn(CalculatePlayerTurnEvent event) {
        if (!open) return;
        event.setMouseSensitivity(-1.0 / 3.0);
    }

    // Clique do mouse (ataque/uso de item/pick block) confirma a fatia selecionada
    // e fecha a roda imediatamente, sem precisar soltar a tecla da roda.
    @SubscribeEvent
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!open) return;
        event.setCanceled(true);
        closeWheelViaClick();
    }

    // Zera o input de movimento enquanto a roda está aberta — mesmo comportamento do inventário.
    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!open) return;
        event.getInput().keyPresses = Input.EMPTY;
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!open) return;

        GuiGraphicsExtractor g = event.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        int cx = mc.getWindow().getGuiScaledWidth()  / 2;
        int cy = mc.getWindow().getGuiScaledHeight() / 2;

        drawRingSlices(g, cx, cy);
        drawLabels(g, cx, cy);
        drawCenterLabel(g, cx, cy);
        drawCursor(g, cx, cy);
    }

    // ── Renderização das fatias via scanline ──────────────────────────────────

    private static void drawRingSlices(GuiGraphicsExtractor g, int cx, int cy) {
        int innerRadius = innerRadius();
        int outerRadius = outerRadius();

        for (int dy = -outerRadius; dy <= outerRadius; dy++) {
            double outerX = Math.sqrt(Math.max(0.0, (double)outerRadius * outerRadius - (double)dy * dy));
            boolean hasInner = Math.abs(dy) < innerRadius;
            double innerX = hasInner
                ? Math.sqrt((double)innerRadius * innerRadius - (double)dy * dy)
                : 0.0;

            int xOuter = (int) outerX;
            int xInner = (int) Math.ceil(innerX);

            drawScanlineSegment(g, cx, cy + dy, dy, xInner, xOuter);
            drawScanlineSegment(g, cx, cy + dy, dy, -xOuter, -xInner);
        }
    }

    private static void drawScanlineSegment(GuiGraphicsExtractor g, int cx, int y, int dy, int xFrom, int xTo) {
        if (xFrom >= xTo) return;

        int currentSlice = -1;
        int segStart     = xFrom;

        for (int dx = xFrom; dx <= xTo; dx++) {
            int slice = sliceAt(dy, dx);
            if (slice != currentSlice) {
                if (currentSlice != -1 && dx > segStart) {
                    g.fill(cx + segStart, y, cx + dx, y + 1, colorForSlice(currentSlice));
                }
                currentSlice = slice;
                segStart     = dx;
            }
        }
        if (currentSlice != -1 && xTo >= segStart) {
            g.fill(cx + segStart, y, cx + xTo + 1, y + 1, colorForSlice(currentSlice));
        }
    }

    private static int sliceAt(int dy, int dx) {
        float angleDeg = (float) Math.toDegrees(Math.atan2(dy, dx));
        float norm     = ((angleDeg + 90f) % 360f + 360f) % 360f;
        int   s        = (int)(norm / (360f / NUM_SLOTS)) + 1;
        return Math.min(s, NUM_SLOTS);
    }

    private static int colorForSlice(int slot) {
        String  id       = ClientAbilityState.slots.get(slot);
        boolean hasAbil  = (id != null);
        boolean isActive = hasAbil && ClientAbilityState.activeAbilities.contains(id);
        boolean isHover  = (slot == selectedSlot);

        if (!hasAbil)   return COLOR_EMPTY;
        if (isActive)   return isHover ? COLOR_ACTIVE_HOVER   : COLOR_ACTIVE;
        return               isHover ? COLOR_INACTIVE_HOVER : COLOR_INACTIVE;
    }

    // ── Rótulos nas fatias ────────────────────────────────────────────────────

    private static void drawLabels(GuiGraphicsExtractor g, int cx, int cy) {
        Font  font = Minecraft.getInstance().font;
        float midR = (innerRadius() + outerRadius()) / 2f;

        for (int s = 0; s < NUM_SLOTS; s++) {
            int    slot  = s + 1;
            String id    = ClientAbilityState.slots.get(slot);
            if (id == null) continue;

            String name  = AbilityRegistry.get(id).map(a -> a.getDisplayName()).orElse(id);
            boolean active = ClientAbilityState.activeAbilities.contains(id);
            int    color = active ? COLOR_LABEL_ACTIVE : COLOR_LABEL;

            float  midAngle = (float)(-Math.PI / 2) + (s + 0.5f) * (float)(2 * Math.PI / NUM_SLOTS);
            int    lx = cx + (int)(Math.cos(midAngle) * midR) - font.width(name) / 2;
            int    ly = cy + (int)(Math.sin(midAngle) * midR) - font.lineHeight / 2;

            g.text(font, name, lx, ly, color, true);
        }
    }

    // ── Texto central ─────────────────────────────────────────────────────────

    private static void drawCenterLabel(GuiGraphicsExtractor g, int cx, int cy) {
        Font font = Minecraft.getInstance().font;

        if (selectedSlot == -1) {
            drawCentered(g, font, "Select an ability", cx, cy, COLOR_CENTER_HINT);
            return;
        }

        String id = ClientAbilityState.slots.get(selectedSlot);
        if (id == null) {
            drawCentered(g, font, "Empty slot", cx, cy, COLOR_CENTER_HINT);
            return;
        }

        String  name    = AbilityRegistry.get(id).map(a -> a.getDisplayName()).orElse(id);
        boolean active  = ClientAbilityState.activeAbilities.contains(id);
        String  label   = (active ? "[ON] " : "[OFF] ") + name;
        int     color   = active ? COLOR_LABEL_ACTIVE : COLOR_CENTER;

        drawCentered(g, font, label, cx, cy, color);
    }

    private static void drawCentered(GuiGraphicsExtractor g, Font font, String text, int cx, int cy, int color) {
        g.text(font, text, cx - font.width(text) / 2, cy - font.lineHeight / 2, color, true);
    }

    // ── Indicador de direção ─────────────────────────────────────────────────

    private static void drawCursor(GuiGraphicsExtractor g, int cx, int cy) {
        int px = cx + (int) Math.round(virtualX);
        int py = cy + (int) Math.round(virtualY);
        g.fill(px - 2, py - 2, px + 2, py + 2, COLOR_CURSOR);
    }
}
