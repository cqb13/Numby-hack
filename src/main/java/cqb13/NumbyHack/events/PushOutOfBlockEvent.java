package cqb13.NumbyHack.events;

import meteordevelopment.meteorclient.events.Cancellable;

public class PushOutOfBlockEvent extends Cancellable {
  private static final PushOutOfBlockEvent INSTANCE = new PushOutOfBlockEvent();

  public double x;
  public double d;

  public static PushOutOfBlockEvent get(double x, double d) {
    INSTANCE.setCancelled(false);
    INSTANCE.x = x;
    INSTANCE.d = d;
    return INSTANCE;
  }
}
