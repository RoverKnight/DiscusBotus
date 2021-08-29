package bot;

import bot.controlCenter.MsgListener;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.HierarchyException;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

public class Yeet extends Thread {
    String target;
    TextChannel c;
    Guild g;
    public boolean hasReplied = false;
    public boolean confirmedKick;

    public Yeet (String targetName, TextChannel channel, Guild guild) {
        target = targetName;
        c = channel;
        g = guild;
    }

    public void run () {
        c.sendMessage("Are you sure you want to yeet "+target+"?").queue();
        while (!hasReplied) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (confirmedKick) c.sendMessage("Yeet confirmed.").queue();
        else {
            c.sendMessage("Yeet aborted.").queue();
            return;
        }

        // retrieves an @mention for the target
        List<Member> nameList = g.getMembersByEffectiveName(target, true);
        try {
            target = nameList.get(0).getAsMention();
        } catch (IndexOutOfBoundsException e) {
            c.sendMessage("No user found with the name \"" + target + "\" in this guild.").queue();
        }

        Member targetM = null;
        if (!nameList.isEmpty()) {
            targetM = nameList.get(0);
            User targetU = targetM.getUser();
            String invite = c.createInvite().complete().getUrl();
            try {
                // reinvites kicked member
                targetU.openPrivateChannel().queue((channel) -> {
                    channel.sendMessage("Welp, you just got yeeted... Wanna rejoin?").queue();

                    channel.sendMessage(invite).queue();
                });

                // gives time for invite to send to avoid ContextException
                try {
                    int timer = 5;
                    for (int i = timer; i > 0; i--) {
                        if (i == timer) c.sendMessage("Yeeting "+target+" in t- "+i).queue();
                        else c.sendMessage("t- "+i).queue();
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    return;
                }

                // kicks given member if possible
                g.kick(targetM, "Get yeeted.").queue();
                c.sendMessage("Yeeted "+target+" from the server.").queue();

            } catch (HierarchyException e) {
                MsgListener.error403MissingPermission(c);
            }
        }
    }
}
