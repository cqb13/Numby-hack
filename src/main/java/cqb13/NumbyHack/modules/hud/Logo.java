package cqb13.NumbyHack.modules.hud;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.util.Identifier;

public class Logo extends HudElement {
    public static final HudElementInfo<Logo> INFO = new HudElementInfo<>(NumbyHack.HUD_GROUP, "logo", "Shows the Numby Hack logo in the HUD.", Logo::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
            .name("scale")
            .description("The scale of the logo.")
            .defaultValue(3)
            .min(0.1)
            .sliderRange(0.1, 10)
            .build()
    );

    public Logo() {
        super(INFO);
    }

    private final Identifier TEXTURE = new Identifier("numby-hack", "textures/icon.png");

    @Override
    public void tick(HudRenderer renderer) {
        box.setSize(64 * scale.get(), 64 * scale.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        GL.bindTexture(TEXTURE);
        Renderer2D.TEXTURE.begin();
        Renderer2D.TEXTURE.texQuad(this.x, this.y, this.getWidth(), this.getHeight(), Color.WHITE);
        Renderer2D.TEXTURE.render(null);
    }
}