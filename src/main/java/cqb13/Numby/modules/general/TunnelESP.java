package cqb13.NumbyHack.modules.general;
import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;

public class TunnelESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
            .name("color")
            .description("")
            .defaultValue(new SettingColor(146,188,98, 255))
            .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("delay")
            .description("delay")
            .defaultValue(1)
            .min(0)
            .sliderMax(20)
            .build()
    );

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
            .name("range").description("Range")
            .defaultValue(50)
            .min(0)
            .sliderMax(150)
            .build()
    );

    private final List<BlockPos> poses = new ArrayList();
    public Vec3d prevPos;
    private double[] rPos;

    public TunnelESP() {
        super(NumbyHack.CATEGORY, "tunnel-esp", "Shows you were tunnels are. (meteor has it, this one was first)");
    }

    @EventHandler
    public void onTick(TickEvent.Pre e) {
        if (mc.player.age % delay.get() == 0) {
            update(range.get());
        }
    }

    public void update(int range) {
        poses.clear();
        BlockPos player = mc.player.getBlockPos();
        prevPos = mc.player.getPos();

        for (int y = -Math.min(range, player.getY()); y < Math.min(range, 255 - player.getY()); y++) {
            for (int x = -range; x < range; x++) {
                for (int z = -range; z < range; z++) {
                    BlockPos pos = player.add(x, y, z);
                    if (mc.world.getBlockState(pos).isAir() && mc.world.getBlockState(pos.up(1)).isAir() && !mc.world.getBlockState(pos.down(1)).isAir() && !mc.world.getBlockState(pos.up(2)).isAir() && !mc.world.getBlockState(pos.north(1)).isAir() && !mc.world.getBlockState(pos.south(1)).isAir() && !mc.world.getBlockState(pos.up(1).north(1)).isAir() && !mc.world.getBlockState(pos.up(1).south(1)).isAir() && mc.world.getBlockState(pos.west(1)).isAir() && mc.world.getBlockState(pos.west(1).up(1)).isAir() && !mc.world.getBlockState(pos.west(1).down(1)).isAir() && !mc.world.getBlockState(pos.west(1).up(2)).isAir() && !mc.world.getBlockState(pos.west(1).north(1)).isAir() && !mc.world.getBlockState(pos.west(1).south(1)).isAir() && !mc.world.getBlockState(pos.west(1).up(1).north(1)).isAir() && !mc.world.getBlockState(pos.west(1).up(1).south(1)).isAir() || mc.world.getBlockState(pos).isAir() && mc.world.getBlockState(pos.up(1)).isAir() && !mc.world.getBlockState(pos.down(1)).isAir() && !mc.world.getBlockState(pos.up(2)).isAir() && !mc.world.getBlockState(pos.west(1)).isAir() && !mc.world.getBlockState(pos.east(1)).isAir() && !mc.world.getBlockState(pos.up(1).west(1)).isAir() && !mc.world.getBlockState(pos.up(1).east(1)).isAir() && mc.world.getBlockState(pos.north(1)).isAir() && mc.world.getBlockState(pos.north(1).up(1)).isAir() && !mc.world.getBlockState(pos.north(1).down(1)).isAir() && !mc.world.getBlockState(pos.north(1).up(2)).isAir() && !mc.world.getBlockState(pos.north(1).west(1)).isAir() && !mc.world.getBlockState(pos.north(1).east(1)).isAir() && !mc.world.getBlockState(pos.north(1).up(1).west(1)).isAir() && !mc.world.getBlockState(pos.north(1).up(1).east(1)).isAir()) {
                        poses.add(pos);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onRender(Render3DEvent a) {

        for (BlockPos p : poses) {
            a.renderer.box(p, color.get(), color.get(), ShapeMode.Lines, 0);
            a.renderer.box(p.up(), color.get(), color.get(), ShapeMode.Lines, 0);
        }
    }
}
