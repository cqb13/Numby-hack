package cqb13.NumbyHack.modules.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.Dimension;
import net.minecraft.command.CommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class CoordinateConverter extends Command {
  public CoordinateConverter() {
    super("coordinate-converter", "Converts coordinates to opposite dimensions (ow/end <-> nether)", "coord-convert",
        "cc");
  }

  @Override
  public void build(LiteralArgumentBuilder<CommandSource> builder) {
    builder.then(argument("x", IntegerArgumentType.integer(-2147483648, 2147483647))
        .then(argument("z", IntegerArgumentType.integer(-2147483648, 2147483647))
            .executes(context -> {
              int xLoc = context.getArgument("x", Integer.class);
              int zLoc = context.getArgument("z", Integer.class);

              Dimension targetDimension = getOppositeDimension();

              Vec3d coords = convertCoords(xLoc, zLoc, targetDimension);
              MutableText text = Text.literal("Location in ");
              text.append(targetDimension.toString() + ": ");
              text.append(ChatUtils.formatCoords(coords));
              text.append(".");
              info(text);
              return SINGLE_SUCCESS;
            })));
  }

  private static Dimension getOppositeDimension() {
    Dimension dimension = PlayerUtils.getDimension();

    if (dimension.equals(Dimension.End) || dimension.equals(Dimension.Overworld)) {
      return Dimension.Nether;
    } else {
      return Dimension.Overworld;
    }
  }

  private static Vec3d convertCoords(int xLoc, int zLoc, Dimension targetDimension) {
    if (targetDimension.equals(Dimension.Nether)) {
      return new Vec3d(xLoc / 8, mc.player.getY(), zLoc / 8);
    } else {
      return new Vec3d(xLoc * 8, mc.player.getY(), zLoc * 8);
    }
  }
}
