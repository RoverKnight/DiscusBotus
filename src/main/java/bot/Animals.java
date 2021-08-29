package bot;

import bot.controlCenter.Main;
import net.dv8tion.jda.api.entities.*;

public class Animals extends Thread {

    //int stage;
    String author;
    TextChannel c;
    Guild g;
    boolean shouldLive = true;
    AnimalsResponseListener listener;

    public Animals(String authorId, TextChannel channel, Guild guild) {
        //stage = commandStage;
        author = authorId;
        c = channel;
        g = guild;
        listener = new AnimalsResponseListener(author, this, c, g);
        Main.jda.addEventListener(listener);
    }

    public void run () {
        c.sendMessage("Do you like dogs?").queue();

        while (shouldLive) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Main.jda.removeEventListener(listener);
            }
        }
    }
}
