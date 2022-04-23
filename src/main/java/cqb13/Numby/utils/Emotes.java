package cqb13.Numby.utils;

public class Emotes {

    public static String apply(String msg) {
        if (msg.contains(":smile:")) msg = msg.replace(":smile:", "☺");
        if (msg.contains(":sad:")) msg = msg.replace(":sad:", "☹");
        if (msg.contains(":heart:")) msg = msg.replace(":heart:", "❤");
        if (msg.contains(":skull:")) msg = msg.replace(":skull:", "☠");
        if (msg.contains(":star:")) msg = msg.replace(":star:", "★");
        if (msg.contains(":flower:")) msg = msg.replace(":flower:", "❀");
        if (msg.contains(":pick:")) msg = msg.replace(":pick:", "⛏");
        if (msg.contains(":wheelchair:")) msg = msg.replace(":wheelchair:", "♿");
        if (msg.contains(":lightning:")) msg = msg.replace(":lightning:", "⚡");
        if (msg.contains(":rod:")) msg = msg.replace(":rod:", "🎣");
        if (msg.contains(":potion:")) msg = msg.replace(":potion:", "🧪");
        if (msg.contains(":fire:")) msg = msg.replace(":fire:", "🔥");
        if (msg.contains(":shears:")) msg = msg.replace(":shears:", "✂");
        if (msg.contains(":bell:")) msg = msg.replace(":bell:", "🔔");
        if (msg.contains(":bow:")) msg = msg.replace(":bow:", "🏹");
        if (msg.contains(":trident:")) msg = msg.replace(":trident:", "🔱");
        if (msg.contains(":cloud:")) msg = msg.replace(":cloud:", "☁");
        return msg;
    }

}
