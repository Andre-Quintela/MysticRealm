package com.nashgoldd.mysticrealm.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Botão genérico que desenha uma textura customizada (esticada para o tamanho do botão) no
 * lugar do sprite vanilla, com o texto centralizado em cima. Reutilizável por qualquer tela
 * (vampiro, lobisomem, bruxaria).
 *
 * A textura/texto não são desenhados em {@link #extractContents}: como os widgets são
 * extraídos antes do fundo da tela, qualquer coisa desenhada ali ficaria atrás dele. Em vez
 * disso, a tela deve chamar {@link #renderDecoration} explicitamente depois de desenhar o
 * fundo, para que o botão apareça por cima.
 */
public class TexturedButton extends Button {

    private final Identifier sprite;
    private final int textColor;

    protected TexturedButton(Builder builder, Identifier sprite, int textColor) {
        super(builder);
        this.sprite = sprite;
        this.textColor = textColor;
    }

    public static TexturedButton create(Component message, Identifier sprite, int textColor,
                                          int x, int y, int width, int height, OnPress onPress) {
        return (TexturedButton) Button.builder(message, onPress)
            .bounds(x, y, width, height)
            .build(builder -> new TexturedButton(builder, sprite, textColor));
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // Textura e texto são desenhados via renderDecoration(), após o fundo da tela.
    }

    /**
     * Desenha a textura e o texto do botão. Deve ser chamado depois do fundo da tela.
     *
     * @param textScale escala aplicada ao texto (deve ser a mesma escala usada para calcular o
     *                   tamanho/posição do botão), para que o texto encolha junto com o botão e
     *                   nunca ultrapasse seus limites.
     */
    public void renderDecoration(GuiGraphicsExtractor graphics, float textScale) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, getX(), getY(), getWidth(), getHeight());

        Font font = Minecraft.getInstance().font;
        Component message = getMessage();
        float textX = getX() + (getWidth() - font.width(message) * textScale) / 2f;
        float textY = getY() + (getHeight() - font.lineHeight * textScale) / 2f;

        graphics.pose().pushMatrix();
        graphics.pose().translate(textX, textY);
        graphics.pose().scale(textScale, textScale);
        graphics.text(font, message, 0, 0, textColor, false);
        graphics.pose().popMatrix();
    }
}
