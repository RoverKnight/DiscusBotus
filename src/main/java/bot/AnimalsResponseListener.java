package bot;

import bot.controlCenter.Main;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class AnimalsResponseListener extends ListenerAdapter {

    // sets up useful variables
    String authorId;
    String hostId;
    Message message;
    Guild g;
    String msgContent;
    TextChannel c;
    TextChannel originalChannel;
    User authorU;
    Animals host;
    int stage = 1;
    List<String> responses = new ArrayList<>();


    public AnimalsResponseListener(String hostId0, Animals hostThread, TextChannel channel, Guild guild) {
        hostId = hostId0;
        originalChannel = channel;
        g = guild;
        host = hostThread;
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent e) {

        // assigns message specific variables
        message = e.getMessage();
        msgContent = message.getContentDisplay();
        authorU = message.getAuthor();
        authorId = authorU.getId();
        c = message.getTextChannel();

        // retrieves arguments from message, assigns to args[] (NOTE: DOES NOT REMOVE PREFIX, AS PREFIX
        // SHOULD ONLY BE USED TO INITIATE A COMMAND, NOT FOR RESPONSES TO IT
        String[] args = new String[msgContent.length()];
        try {
            args = msgContent.split(" ");
        } catch (StringIndexOutOfBoundsException ignored) {}

        // executes only if author is same as person who started listener via command and if response
        // is sent in the same channel where command was used and if message doesn't start with "!"
        // (because user should still be able to issue other commands)
        if (!authorU.isBot() && authorId.equalsIgnoreCase(hostId) && c == originalChannel && !msgContent.startsWith("!")) {
            boolean inputWasValid = true;
            boolean wasCommand = true;

            if (args[0].equalsIgnoreCase("test")) {
                c.sendMessage("ListenerAdapter reached.").queue();
            }
            else if (args[0].equalsIgnoreCase("abort")) {
                shutdown(true);
                return;
            }
            else if (args[0].equalsIgnoreCase("restart")) {
                restart();
                return;
            }
            else wasCommand = false;

            responses.add(args[0]);

            // response logic
            // NOTE THAT STAGE 0 IS SKIPPED, BECAUSE INITIAL HANDLING BY THREAD SHALL BE CONSIDERED STAGE 0!
            if (stage == 1) {
                if (currentResponseIs("yes")) {
                    c.sendMessage("Cool.").queue();
                    c.sendMessage("What about Golden Retrievers?").queue();
                }
                else if (currentResponseIs("no")) {
                    c.sendMessage(":(").queue();
                    c.sendMessage("What about ferrets?").queue();
                }
                else inputWasValid = false;
            }
            else if (stage == 2) {
                if (previousResponseWas("yes", 1) &&
                        currentResponseIs("yes")) {
                    c.sendMessage("They're awesome, right?").queue();
                }
                else if (previousResponseWas("yes", 1) &&
                        currentResponseIs("no")) {
                    c.sendMessage("Aw :(").queue();
                    c.sendMessage("Then Huskies?").queue();
                }
                else if (previousResponseWas("no", 1) &&
                        currentResponseIs("yes")) {
                    c.sendMessage("Yeah, they're cute right?").queue();
                }
                else if (previousResponseWas("no", 1) &&
                        currentResponseIs("no")) {
                    c.sendMessage("Well, do you like frogs..?").queue();
                }
                else inputWasValid = false;
            }
            else if (stage == 3) {
                if (previousResponseWas("yes", 2) &&
                        previousResponseWas("yes", 1) &&
                        currentResponseIs("yes")) {
                    c.sendMessage("Go Golden Retrievers! :D").queue();
                }
                else if (previousResponseWas("yes", 2) &&
                        previousResponseWas("yes", 1) &&
                        currentResponseIs("no")) {
                    c.sendMessage("Wait, what? :o").queue();
                }
                else if (previousResponseWas("yes", 2) &&
                        previousResponseWas("no", 1) &&
                        currentResponseIs("yes")) {
                    c.sendMessage("Go Huskies! :D").queue();
                }
                else if (previousResponseWas("yes", 2) &&
                        previousResponseWas("no", 1) &&
                        currentResponseIs("no")) {
                    c.sendMessage("Go dogs..?").queue();
                }
                else if (previousResponseWas("no", 2) &&
                        previousResponseWas("yes", 1) &&
                        currentResponseIs("yes")) {
                    c.sendMessage("Go ferrets! :D").queue();
                }
                else if (previousResponseWas("no", 2) &&
                        previousResponseWas("yes", 1) &&
                        currentResponseIs("no")) {
                    c.sendMessage("Go polecats..?").queue();
                }
                else if (previousResponseWas("no", 2) &&
                        previousResponseWas("no", 1) &&
                        currentResponseIs("yes")) {
                    c.sendMessage("Go frogs! :D").queue();
                }
                else if (previousResponseWas("no", 2) &&
                        previousResponseWas("no", 1) &&
                        currentResponseIs("no")) {
                    c.sendMessage("You're boring. :(").queue();
                }
                else inputWasValid = false;

                shutdown(false);
            } // holy fuck do I hate dialog trees now
            else {
                c.sendMessage("Invalid stage: "+stage+". Please check code.").queue();
            }

            if (inputWasValid) stage++;
            else if (!wasCommand) {
                c.sendMessage("Bad input :(").queue();
                responses.remove(stage-1);
            }
        }
    }

    public void shutdown (boolean shouldNotify) {
        if (shouldNotify) c.sendMessage("TRL shutting down.").queue();
        host.shouldLive = false;
        Main.jda.removeEventListener(this);
    }

    public void restart () {
        c.sendMessage("Restarted animal dialogue.").queue();
        Animals newHost = new Animals(authorId, c, g);
        newHost.start();
        shutdown(false);
    }

    public boolean previousResponseWas(String response, int numOfIndicesAgo) {
        return responses.get(stage - numOfIndicesAgo-1).equalsIgnoreCase(response);
    }

    public boolean currentResponseIs(String response) {
        return responses.get(stage-1).equalsIgnoreCase(response);
    }
}
