package cqb13.NumbyHack.modules.general;

import cqb13.NumbyHack.NumbyHack;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;

/**
 * made by cqb13
 */
public class Number81 extends Module {

    public Number81() {
        super(NumbyHack.CATEGORY, "81", "Counts to 81 in chat.");
    }

    private int timer;
    private int count;
    private boolean setTimer;

    @Override
    public void onActivate() {
        count = 0;
    }

    @Override
    public void onDeactivate() {
        if (count != 81) {
            assert mc.player != null;
            ChatUtils.sendPlayerMsg("I am a lazy bitch and did not count to 81!");
            ChatUtils.sendPlayerMsg("I am a disgrace and should be punished!");
            ChatUtils.sendPlayerMsg("I am a very bad person!");
            ChatUtils.sendPlayerMsg("Number81 is the best!");
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (setTimer){
            timer = (int)(Math.random() * 70 + 40);
            setTimer = false;
        }
        timer--;
        if(timer < 0){
            count++;
            assert mc.player != null;
            ChatUtils.sendPlayerMsg(String.valueOf(count));
            setTimer = true;
        }
        if(count == 81){
            assert mc.player != null;
            ChatUtils.sendPlayerMsg("Number81 on top!");
            toggle();
        }
    }
}


