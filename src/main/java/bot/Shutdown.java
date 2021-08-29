package bot;

import net.dv8tion.jda.api.entities.TextChannel;

public class Shutdown extends Thread {
    TextChannel c;
    public boolean hasReplied = false;
    public boolean confirmedShutdown;

    public Shutdown (TextChannel channel) {
        c = channel;
    }

    public void run () {
        c.sendMessage("Once shut down the bot can only be re-enabled by it's developer. Are you sure you " +
            "want to proceed?").queue();
        while (!hasReplied) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (confirmedShutdown) System.exit(0);
        else c.sendMessage("Shutdown aborted.").queue();
    }
}
