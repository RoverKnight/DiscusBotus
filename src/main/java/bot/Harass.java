package bot;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

public class Harass extends Thread {
    Guild g;
    TextChannel c;
    long d;
    long i;
    List<Member> nameList;
    Member v;
    String mention;
    String effectiveName;

    public Harass (String victimName, int duration, int interval, TextChannel channel, Guild guild) {
        // assigns inputs to variables
        g = guild;
        c = channel;
        d = duration;
        i = interval;
        nameList = g.getMembersByEffectiveName(victimName, true);

        // retrieves an @mention for the requested user
        try {
            v = nameList.get(0);
            mention = v.getAsMention();
            effectiveName = v.getEffectiveName();
            c.sendMessage("Victim found!").queue();
        } catch (IndexOutOfBoundsException e) {
            c.sendMessage("No user found with the name \"" + victimName + "\" in this guild.").queue();
        }
    }

    public void run () {

        long startTime = System.currentTimeMillis();

        // Thread.sleep has to be surrounded by a try-catch, because it can throw an InterruptedException
        try {
            // triggers catch -> stops thread if no user with given name found
            if (mention == null) throw new InterruptedException();

            boolean timeOver = false;
            while (!timeOver) {
                // harassment happens here
                //c.sendMessage("Fuck you " + mention + "!").queue();
                c.sendMessage("Do you like turtles "+mention+"?").queue();

                // checks if the harassment period is over yet
                long currentTime = System.currentTimeMillis();
                if (startTime <= currentTime - d * 1000) {
                    c.sendMessage("Harassment of "+effectiveName+" over.").queue();
                    timeOver = true;
                }

                Thread.sleep(i * 1000);     // makes thread wait 1 sec before looping
            }
        } catch (InterruptedException e) {
            c.sendMessage("HEYS").queue();
            return;    // stops the thread
        }
    }
}
