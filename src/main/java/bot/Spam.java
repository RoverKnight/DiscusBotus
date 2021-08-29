package bot;

import net.dv8tion.jda.api.entities.TextChannel;

public class Spam extends Thread {
    String id;
    int a;
    TextChannel c;

    public Spam (String authorId, int amount, TextChannel channel) {
        id = authorId;
        a = amount;
        c = channel;
    }

    public void run () {

        try {
            for (int i = 0; i < a; i++) {
                c.sendMessage("I like turtles!").queue();
                if (i == a - 1 && a >= 5) c.sendMessage("We hate you <@" + id + ">").queue();
                Thread.sleep(1000);
            }

        } catch (InterruptedException e) {
            return;
        }

    }
}
