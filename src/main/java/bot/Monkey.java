package bot;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import java.lang.Math;

import java.util.List;

public class Monkey extends Thread {
    Guild g;
    TextChannel c;
    long d;
    long i;
    List<Member> nameList;
    Member v;
    String mention;
    String effectiveName;

    public Monkey (int duration, int interval, TextChannel channel, Guild guild) {
        // assigns inputs to variables
        g = guild;
        c = channel;
        d = duration;
        i = interval;
        nameList = c.getMembers();


    }

    public void run () {
        // Thread.sleep has to be surrounded by a try-catch, because it can throw an InterruptedException
        try {
            if (d != 0) c.sendMessage("Hello this is Monkey").queue();
            else c.sendMessage("Hello this is Monkey, UNCHAINED").queue();
            long startTime = System.currentTimeMillis();
            boolean timeOver = false;
            while (!timeOver) {

                // generates random number which selects a member from the channel
                double rand = Math.random();
                rand *= nameList.size();
                int selector = (int)rand;

                // retrieves an @mention for randomly selected user
                v = nameList.get(selector);
                mention = v.getAsMention();
                effectiveName = v.getEffectiveName();

                // initializes harassment messages (can't be initialized earlier because then mention = null)
                String[] messages = {
                        "*Threw poop at "+mention+".*",
                        "Do you like turtles "+mention+"?",
                        "I have come to the consensus that "+mention+" is an unpleasant human(?) being.",
                        "Hey "+mention+", where's Perry?",
                        mention+" is a total dillweed."
                };

                // generates random number which selects harassment message
                // (placed behind @mention retrieval since variables are reused)
                rand = Math.random();
                rand *= messages.length;
                selector = (int)rand;

                // harassment happens here
                c.sendMessage(messages[selector]).queue();

                // checks if the harassment period is over yet (or if period is undefined (d == 0))
                long currentTime = System.currentTimeMillis();
                if (d != 0 && startTime <= currentTime - d * 1000) {
                    c.sendMessage("Monkey tired").queue();
                    timeOver = true;
                }

                // makes the thread wait for specified interval before looping
                Thread.sleep(i * 1000);
            }
        } catch (InterruptedException e) {
            return;    // stops the thread
        }
    }
}
