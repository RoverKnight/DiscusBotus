package bot.controlCenter;

import bot.Animals;
import bot.Shutdown;
import bot.Yeet;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class MsgListener extends ListenerAdapter {


    public static List<Thread> runningThreads = new ArrayList<>();

    public static boolean isEnabled = true;

    public static String[] commands = {
            "spam",
            "harass",
            "monkey",
            "stop",
            "commandList",
            "sayHello",
            "threadStoppingBenchmark",
            "runTestThreads",
            "debugCommandList",
            "printMembers",
            "runningThreads",
            "isOwner",
            "getConnectedName",
            "enable",
            "disable",
            "dev",
            "info",
            "inspiro",
            "yeet",
            "shutdown",
            "animals"
    };

    // sets up useful variables
    Message message;
    Guild g;
    String msgContent;
    TextChannel c;
    String authorId;
    String ownerId;
    String authorName;
    Member owner;
    Member authorM;
    User authorU;
    List<Role> authorRoles;
    List<Role> guildRoles = new ArrayList<>();
    boolean isDev;
    boolean isAdmin;
    boolean isTester;
    Member botM;


    @Override
    // executes when a message is received on server
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent e) {
        // removes any threads from the runningThreads list that have died
        if(!runningThreads.isEmpty()) {
            for (int i = 0; i < runningThreads.size(); i++) {
                Thread thread = runningThreads.get(i);
                if (!thread.isAlive()) {
                    runningThreads.remove(i);
                    i--;
                }
            }
        }

        // assigns useful variables
        message = e.getMessage();
        g = e.getGuild();
        msgContent = message.getContentDisplay();
        c = message.getTextChannel();
        authorId = message.getAuthor().getId();
        ownerId = g.getOwnerId();
        owner = g.getOwner();
        authorName = message.getAuthor().getName();
        authorU = message.getAuthor();
        authorM = message.getMember();
        authorRoles = authorM.getRoles();
        guildRoles.addAll(g.getRoles());
        for (Role role : authorRoles) {
            String roleName = role.toString();
            if (roleName.contains("DB Admin")) isAdmin = true;
            if (roleName.contains("DB Dev")) isDev = true;
            if (roleName.contains("DB Tester")) isTester = true;
        }
        botM = g.getSelfMember();

        // separates set prefix from message (in a try catch because an embed sent by bot causes error)
        String[] args = new String[msgContent.length()];
        try {
            args = msgContent.substring(Main.prefix.length()).split(" ");
        } catch (StringIndexOutOfBoundsException ignored) {}

        // executes if message starts with prefix determined in Main()
        if (msgContent.startsWith(Main.prefix) && !authorU.isBot()) {

            // === TESTING GROUNDS ===

            // === END OF TESTING GROUNDS ===

            boolean commandFound = false;

            // command access logic
            if (isDev) {
                if (isEnabled && (standardCommands(args) || debugCommands(args)) || adminCommands(args) || devCommands(args)) {
                    commandFound = true;
                }
                else if (standardNonThreadedCommands(args) || debugNonThreadedCommands(args) || adminCommands(args) || devCommands(args)) {
                    commandFound = true;
                }
            }

            else if (isAdmin) {
                if (isEnabled && (standardCommands(args) || debugCommands(args)) || adminCommands(args)) {
                    commandFound = true;
                }
                else if (standardNonThreadedCommands(args) || debugNonThreadedCommands(args) || adminCommands(args)) {
                    commandFound = true;
                }
            }

            else if (isTester) {
                if (isEnabled && (standardCommands(args) || debugCommands(args))) {
                    commandFound = true;
                }
                else if (standardNonThreadedCommands(args) || debugNonThreadedCommands(args)) {
                    commandFound = true;
                }
            }

            else {
                if (isEnabled && standardCommands(args)) {
                    commandFound = true;
                }
            }


            // command access report
            if (!commandFound) {
                boolean commandExists = false;
                for (String command : commands) {
                    if (args[0].equalsIgnoreCase(command)) {
                        commandExists = true;
                        break;
                    }
                }
                if (commandExists) {
                    c.sendMessage("You do not currently have permission to use this command.").queue();
                } else {
                    error404NotFound();
                }
            }
        }
    }

    public boolean debugCommands (String[] args) {
        return debugThreadedCommands(args) || debugNonThreadedCommands(args);
    }

    public boolean standardCommands (String[] args) {
        // returns true if a command was found
        return standardThreadedCommands(args) || standardNonThreadedCommands(args);
    }

    public boolean standardThreadedCommands (String[] args) {
        boolean commandFound = true;

        if (args[0].equalsIgnoreCase("spam")){
            try {
                int spamAmount = Integer.parseInt(args[1]);
                bot.Spam s = new bot.Spam(authorId, spamAmount, c);
                s.start();
                s.setName("Spam");
                runningThreads.add(s);
            } catch (ArrayIndexOutOfBoundsException e) {
                error400BadInput("commandList");
            }
        }

        else if (args[0].equalsIgnoreCase("harass")){
            String victim = getConnectedName(args, 1, 2);
            try {
                int duration = Integer.parseInt(args[args.length - 2]);
                int interval = Integer.parseInt(args[args.length - 1]);

                bot.Harass h = new bot.Harass(victim, duration, interval, c, g);
                h.start();
                h.setName("Harass "+victim);
                runningThreads.add(h);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                error400BadInput("harass");
            }
        }

        else if (args[0].equalsIgnoreCase("monkey")) {
            int duration = -1;
            int interval = -1;

            //

            // gets inputs
            try {
                duration = Integer.parseInt(args[1]);
                interval = Integer.parseInt(args[2]);
            } catch (ArrayIndexOutOfBoundsException e) { // input format = !monkey
                // when duration equals 0, monkey runs indefinitely
                if (args.length == 1) {
                    duration = 0;
                    interval = 3;
                }
            } catch (NumberFormatException e) { // input format = !monkey + "d"/"i" + [int duration/interval]
                try {
                    if (args[1].equalsIgnoreCase("i")) {
                        interval = Integer.parseInt(args[2]);
                        duration = 0;
                    }
                    else if (args[1].equalsIgnoreCase("d")) {
                        duration = Integer.parseInt(args[2]);
                        interval = 3;
                    }
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException ignored) {}
            }

            // prevents Monkey() thread from initializing if input deemed invalid (so that no new values were assigned)
            if (duration != -1 && interval != -1) {
                bot.Monkey m = new bot.Monkey(duration, interval, c, g);
                m.start();
                m.setName("Monkey");
                runningThreads.add(m);
            }
            else error400BadInput("monkey");
        }

        else if (args[0].equalsIgnoreCase("stop")) {
            String request = args[1];
            bot.Stop s = new bot.Stop(request, runningThreads, c);
            s.start();
            s.setName("Stop");
            // IMPORTANT: DO NOT ADD STOP-THREAD TO runningThreads!
            // This will cause an endless loop in the stop thread
        }

        else commandFound = false;
        return commandFound;
    }

    public boolean standardNonThreadedCommands (String[] args) {
        boolean commandFound = true;

        if (args[0].equalsIgnoreCase("commandList")){
            String[] text = {
                    "These are the commands I react to:",
                    "!commandList - guess what you're looking at dillweed.",
                    "!debugCommandList - prints a list of available debug commands with explanations for each.",
                    "!spam + [int] - I'll spam as many messages specified in the channel it was requested.",
                    "!harass + [username on server] + [int duration] + [int interval] - " +
                            "I'll harass the given user for the specified duration (in seconds) after every interval (also in seconds).",
                    "yeetus this fetus"
            };
            for (String line : text) {
                c.sendMessage(line).queue();
            }
        }

        else if (args[0].equalsIgnoreCase("sayHello")) {
            String target;
            String mention;

            // gets inputs
            try {
                if (args.length < 2) throw new ArrayIndexOutOfBoundsException();
                target = getConnectedName(args, 1, 0);

                // retrieves @mention
                try {
                    mention = g.getMembersByEffectiveName(target, true).get(0).getAsMention();
                } catch (IndexOutOfBoundsException e) {
                    mention = null;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                target = authorId;
                mention = "<@"+target+">";
            }

            // sends appropriate message
            if (mention != null) c.sendMessage("Hello " + mention + "!").queue();
            else c.sendMessage("No user found with the name \"" + target + "\" in this guild.").queue();
        }

        else if (args[0].equalsIgnoreCase("info")) {
            String request;
            try {
                request = args[1];
                boolean requestFound = false;
                // recommended to NOT use commandExistsInLog, since what's used here automatically
                // corrects capitalization in the embedded message
                for (String command : commands) {
                    if (command.equalsIgnoreCase(request)) {
                        requestFound = true;
                        printInfo(command);
                        break;
                    }
                }
                if (!requestFound) c.sendMessage("Command \""+request+"\" does not exist, " +
                    "so there is no info-log for it.").queue();

            } catch (ArrayIndexOutOfBoundsException e) {
                error400BadInput("info");
            }
        }

        else if (args[0].equalsIgnoreCase("inspiro")) {
            try {
                // connects to quote-url generator
                URLConnection connection = new URL("https://inspirobot.me/api?generate=true").openConnection();

                // assigns input stream (used to get url from quote-url generator)
                InputStream input = connection.getInputStream();

                // gets generated url from webpage, then assigns to a URL variable
                String imgURLString = new BufferedReader(new InputStreamReader(input)).readLine();
                URL imgURL = new URL(imgURLString);

                // gets generated image from generated url
                BufferedImage img = ImageIO.read(imgURL);

                // turns image into file (discord only accepts String or File)
                File imgFile = new File("inspirationalQuote.jpg");
                ImageIO.write(img, "jpg", imgFile);

                // sends image
                c.sendFile(imgFile).queue();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 'http://inspirobot.me/api?generate=true';

        else commandFound = false;
        return commandFound;
    }

    public boolean debugThreadedCommands (String[] args) {
        boolean commandFound = true;

        if (args[0].equalsIgnoreCase("threadStoppingBenchmark")) {
            String argument;
            int threads;
            int iterations;

            try {
                argument = args[1];
                if (!argument.equalsIgnoreCase("all") && !argument.equalsIgnoreCase("testThread")) {
                    throw new NumberFormatException();
                }
                threads = Integer.parseInt(args[2]);
                iterations = Integer.parseInt(args[3]);
                bot.ThreadStoppingBenchmark tsb = new bot.ThreadStoppingBenchmark(argument, threads, iterations, runningThreads, c);
                tsb.start();
                tsb.setName("ThreadStoppingBenchmark");
                // IMPORTANT: DO NOT ADD TSB-THREAD TO runningThreads!
                // This will cause an endless loop in the stop threads run by tsb
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                error400BadInput("threadStoppingBenchmark");
            }
        }

        else if (args[0].equalsIgnoreCase("runTestThreads")) {
            try {
                int amount = Integer.parseInt(args[1]);
                for (int i = 0; i < amount; i++) {
                    bot.TestThread tt = new bot.TestThread();
                    tt.start();
                    tt.setName("TestThread");
                    runningThreads.add(tt);
                }
                c.sendMessage("Created "+amount+" test threads.").queue();
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                error400BadInput("runTestThreads");
            }
        }

        else if (args[0].equalsIgnoreCase("animals")) {
            Animals a = new Animals(authorId, c, g);
            a.start();
            a.setName("Test");
            runningThreads.add(a);
        }

        else commandFound = false;
        return commandFound;
    }

    public boolean debugNonThreadedCommands (String[] args) {
        boolean commandFound = true;

        if (args[0].equalsIgnoreCase("debugCommandList")) {
            c.sendMessage("!printMembers - prints all server members as recognized by the bot").queue();
            c.sendMessage("!runningThreads - prints all threads that are currently running with corresponding names").queue();
        }

        else if (args[0].equalsIgnoreCase("printMembers")) {
            // members in channel
            List<Member> members = c.getMembers();
            String numOfMembers = String.valueOf(members.size());
            c.sendMessage("I see "+numOfMembers+" members in this channel:").queue();
            for (Member member : members) {
                String memberName = member.getEffectiveName();
                c.sendMessage(memberName).queue();
            }

            // members in guild (server)
            members = g.getMembers();
            numOfMembers = String.valueOf(members.size());
            c.sendMessage("I see "+numOfMembers+" members in this server:").queue();
            for (Member member : members) {
                String memberName = member.getEffectiveName();
                c.sendMessage(memberName).queue();
            }
        }

        else if (args[0].equalsIgnoreCase("runningThreads")) {
            if (runningThreads.isEmpty()) {
                c.sendMessage("No threads are running right now.").queue();
            } else {
                c.sendMessage("Total threads running: "+ runningThreads.size()).queue();
                for (int i = 0; i < runningThreads.size(); i++) {
                    String threadName = runningThreads.get(i).getName();
                    int count = i + 1;
                    c.sendMessage("Thread " + count +": " + threadName).queue();
                }
            }
        }

        else if (args[0].equalsIgnoreCase("isOwner")) {
            try {
                String name = args[1];
                Member target = g.getMembersByEffectiveName(name, true).get(0);
                String targetName = target.getEffectiveName();
                if (target == owner) c.sendMessage("Yes, " + targetName + " is the server owner.").queue();
                else c.sendMessage("No, " + targetName + " is not the server owner.").queue();
            } catch (IndexOutOfBoundsException e) {
                error400BadInput("isOwner");
            }
        }

        else if (args[0].equalsIgnoreCase("getConnectedName")) {
            int startOffset = Integer.parseInt(args[args.length -2]);
            int endOffset = Integer.parseInt(args[args.length -1]);
            String connectedName = getConnectedName(args, startOffset, endOffset);
            c.sendMessage(connectedName).queue();
        }

        else commandFound = false;
        return commandFound;
    }

    public boolean adminCommands (String[] args) {
        boolean commandFound = true;

        if (args[0].equalsIgnoreCase("enable")) {
            if (isEnabled) c.sendMessage("Bot is already enabled.").queue();
            else {
                c.sendMessage("Bot has been forcibly enabled.").queue();
                isEnabled = true;
            }
        }

        else if (args[0].equalsIgnoreCase("disable")) {
            if (isEnabled) {
                c.sendMessage("Bot has been forcibly disabled.").queue();
                isEnabled = false;
            }
            else c.sendMessage("Bot is already disabled.").queue();
        }

        else if (args[0].equalsIgnoreCase("shutdown")) {
            if (!isRunning("shutdown")) {
                bot.Shutdown sd = new bot.Shutdown(c);
                sd.start();
                sd.setName("Shutdown");
                runningThreads.add(sd);
            }
            else if (args[1].equalsIgnoreCase("yes")) {
                for (Thread thread : runningThreads) {
                    if (thread.getName().equalsIgnoreCase("shutdown")) {
                        bot.Shutdown sd = (Shutdown)thread;
                        sd.hasReplied = true;
                        sd.confirmedShutdown = true;
                        break;
                    }
                }
            }
            else if (args[1].equalsIgnoreCase("no")) {
                for (Thread thread : runningThreads) {
                    if (thread.getName().equalsIgnoreCase("shutdown")) {
                        bot.Shutdown sd = (Shutdown)thread;
                        sd.hasReplied = true;
                        sd.confirmedShutdown = false;
                        break;
                    }
                }
            }
            else {
                c.sendMessage("Invalid response. Please type either \"!yeet yes\" or \"!yeet no\".").queue();
            }

            /*
            c.sendMessage("Bot shutting down...").queue();
            System.exit(0);
            */
        }

        else if (args[0].equalsIgnoreCase("yeet")) {
            if (!isRunning("yeet")) {
                String target = getConnectedName(args, 1, 0);
                bot.Yeet y = new bot.Yeet(target, c, g);
                y.start();
                y.setName("Yeet");
                runningThreads.add(y);
            }
            else if (args[1].equalsIgnoreCase("yes")) {
                for (Thread thread : runningThreads) {
                    if (thread.getName().equalsIgnoreCase("yeet")) {
                        bot.Yeet y = (Yeet)thread;
                        y.hasReplied = true;
                        y.confirmedKick = true;
                        break;
                    }
                }
            }
            else if (args[1].equalsIgnoreCase("no")) {
                for (Thread thread : runningThreads) {
                    if (thread.getName().equalsIgnoreCase("yeet")) {
                        bot.Yeet y = (Yeet)thread;
                        y.hasReplied = true;
                        y.confirmedKick = false;
                        break;
                    }
                }
            }
            else {
                c.sendMessage("Invalid response. Please type either \"!yeet yes\" or \"!yeet no\".").queue();
            }
        }

        else commandFound = false;
        return commandFound;
    }

    public boolean devCommands (String[] args) {
        boolean commandFound = true;

        if (args[0].equalsIgnoreCase("dev")) {
            c.sendMessage("hmmmmmmmmmmmmmm").queue();
        }

        else commandFound = false;
        return commandFound;
    }

    public String getConnectedName (String[] args, int msgStartOffset, int msgEndOffset) {
        String target = "";
        String space = " ";
        String[] targetArray = new String[args.length -msgStartOffset];
        int combinedOffset = msgStartOffset + msgEndOffset;

        for (int i = 0; i < args.length -combinedOffset; i++) {
            try {
                targetArray[i] = args[i + msgStartOffset];
            } catch (ArrayIndexOutOfBoundsException e) {
                return null;
            }
            if (i != 0) target = target.concat(space);
            target = target.concat(targetArray[i]);
        }

        return target;
    }

    public void error400BadInput (String commandName) {
        c.sendMessage("Error 400: Bad input. Please type \"!info "+commandName+"\" to see proper input formats.").queue();
    }

    public void error404NotFound () {
        c.sendMessage("Error 404: command not found.").queue();
    }

    public static void error403MissingPermission(TextChannel c) {
        c.sendMessage("Error 403: missing permission.").queue();
    }

    public void printInfo (String command) { // TODO: implement !rickroll
        String nl = System.lineSeparator(); // nl is short for newline
        String permissionClass = null;
        String isThreaded = null;
        String reactsToStop = null;
        String inputFormats = null;
        String function = null;

        EmbedBuilder info = new EmbedBuilder();
        info.setTitle("The "+command+" command:");


        // spam
        if (command.equalsIgnoreCase("spam")) {
            permissionClass = "Standard";
            isThreaded = "Yes";
            reactsToStop = "Yes";
            inputFormats = "!spam + [int amount]";
            function = "Spams the specified amount of messages in 1 second intervals. If the amount " +
                "surpasses 4, the bot also informs you of what the other members now think of you.";
        }
        // harass
        else if (command.equalsIgnoreCase("harass")) {
            permissionClass = "Everyone";
            isThreaded = "Yes";
            reactsToStop = "Yes";
            inputFormats = "!harass + [String nickname] + [int duration] + [int interval]";
            function = "@mentions the specified user every interval for the given duration. Unit " +
                "of both integers is seconds.";
        }
        // monkey
        else if (command.equalsIgnoreCase("monkey")) {
            permissionClass = "Everyone";
            isThreaded = "Yes";
            reactsToStop = "Yes";
            inputFormats = "!monkey"+nl+
                "!monkey + \"d\" / \"i\" + [int duration/interval]"+nl+
                "!monkey + [int duration] + [int interval]"
            ;
            function = "@mentions randomly selected users in the server in semi-random messages every " +
                "interval for the given duration. Unit of both integers is seconds."+nl+
                "If the second input is \"d\", the monkey thread will run for the duration specified " +
                "by the integer, with a predetermined interval of 3."+nl+
                "If the second input is \"i\", the monkey thread will run using the given integer as " +
                "the interval, for an undefined duration."+nl+
                "If no inputs are given, the monkey thread will run for an undefined duration with a " +
                "predetermined interval of 3."
            ;
        }
        // stop
        else if (command.equalsIgnoreCase("stop")) {
            permissionClass = "Everyone";
            isThreaded = "Yes";
            reactsToStop = "It *is* stop";
            inputFormats = "!stop + \"all\""+nl+
                "!stop + [String threadType/-Name]"
            ;
            function = "Stops all threads or all threads of the type/name specified.";
        }
        // commandList
        else if (command.equalsIgnoreCase("commandList")) {
            permissionClass = "Everyone";
            isThreaded = "No";
            reactsToStop = "No";
            inputFormats = "!commandList";
            function = "Prints a list of all standard commands. For other commands, see also: " +
                "*!debugCommandList*, *!adminCommandList* & *!devCommandList*."
            ;
        }
        // debugCommandList
        else if (command.equalsIgnoreCase("debugCommandList")) {
            permissionClass = "DB Tester";
            isThreaded = "No";
            reactsToStop = "No";
            inputFormats = "!debugCommandList";
            function = "Prints a list of all debug commands.";
        }
        // sayHello
        else if (command.equalsIgnoreCase("sayHello")) {
            permissionClass = "Everyone";
            isThreaded = "No";
            reactsToStop = "No";
            inputFormats = "!sayHello"+nl+
                "!sayHello + [String nickname]"
            ;
            function = "Says hello to the specified user, or to the message's author if no name is given.";
        }
        // threadStoppingBenchmark
        else if (command.equalsIgnoreCase("threadStoppingBenchmark")) {
            permissionClass = "DB Tester";
            isThreaded = "Yes";
            reactsToStop = "No";
            inputFormats = "!threadStoppingBenchmark + [String shutdownType] + " +
                "[integer threadsPerRun] + [integer runs]"
            ;
            function = "Runs a benchmark where test threads are continuously created and stopped. " +
                "Originally implemented to determine causes to determine the cause of the *!stop* " +
                "command occasionally missing threads."+nl+
                "String shutdownType is used to test the shutdown of all and specific threads " +
                "respectively, and must either equal \"all\" or \"testThread\"."+nl+
                "This command will also disable the bot for all standard & all threaded commands " +
                "for it's duration."
            ;
        }
        // runTestThreads
        else if (command.equalsIgnoreCase("runTestThreads")) {
            permissionClass = "DB Tester";
            isThreaded = "Yes";
            reactsToStop = "Yes";
            inputFormats = "!runTestThreads + [int amount]";
            function = "Starts the specified amount of test threads. These threads do nothing and " +
                "shut themselves down after 60 seconds."
            ;
        }
        // printMembers
        else if (command.equalsIgnoreCase("printMembers")) {
            permissionClass = "DB Tester";
            isThreaded = "No";
            reactsToStop = "No";
            inputFormats = "!printMembers";
            function = "Prints all server members and all members that have access to the channel " +
                "in separate lists respectively."
            ;
        }
        // runningThreads
        else if (command.equalsIgnoreCase("runningThreads")) {
            permissionClass = "DB Tester";
            isThreaded = "No";
            reactsToStop = "No";
            inputFormats = "!runningThreads";
            function = "Prints all server members and all members that have access to the channel " +
                    "in separate lists respectively."
            ;
        }
        // isOwner
        else if (command.equalsIgnoreCase("isOwner")) {
            permissionClass = "DB Tester";
            isThreaded = "No";
            reactsToStop = "No";
            inputFormats = "!isOwner + [String nickname]";
            function = "Checks if the given user is the server owner.";
        }
        // getConnectedName
        else if (command.equalsIgnoreCase("getConnectedName")) {
            permissionClass = "DB Tester";
            isThreaded = "No";
            reactsToStop = "No";
            inputFormats = "!getConnectedName + [String nickname] + [int startOffset] + [int endOffset]";
            function = "Prints the full nickname of the given user in a single string."+nl+
                "The nickname is meant to be made up of multiple strings separated by spaces " +
                "(e.g. \"Discus Botus\"). The integers determine the number of strings between " +
                "the beginning / end of the full command and the first/last part of the username" +
                "respectively. You may also insert any amount of arbitrary strings inbetween " +
                "the command & the user's nickname and between the user's nickname and the " +
                "offset-integers, though the offsets should be adjusted accordingly."
            ;
        }
        // enable
        else if (command.equalsIgnoreCase("enable")) {
            permissionClass = "DB Admin";
            isThreaded = "No";
            reactsToStop = "No";
            inputFormats = "!enable";
            function = "Forcefully reenables the bot for all commands.";
        }
        // disable
        else if (command.equalsIgnoreCase("disable")) {
            permissionClass = "DB Admin";
            isThreaded = "No";
            reactsToStop = "No";
            inputFormats = "!disable";
            function = "Forcefully disables the bot for all standard & all threaded commands.";
        }
        // dev
        else if (command.equalsIgnoreCase("dev")) {
            permissionClass = "DB Dev";
            isThreaded = "No";
            reactsToStop = "No";
            inputFormats = "!dev";
            function = "Literally does jack shit.";
        }
        // info
        else if (command.equalsIgnoreCase("info")) {
            permissionClass = "Everyone";
            isThreaded = "No";
            reactsToStop = "No";
            inputFormats = "!info + [String commandName]";
            function = "Prints an embedded message containing useful details for the usage of a " +
                "given command."
            ;
        }
        // inspiro
        else if (command.equalsIgnoreCase("inspiro")) {
            permissionClass = "Everyone";
            isThreaded = "No";
            reactsToStop = "No";
            inputFormats = "!inspiro";
            function = "Uses artificial intelligence from InspiroBot.me to generate an inspirational " +
                "quote/image, which is then sent."
            ;
        }
        // yeet
        else if (command.equalsIgnoreCase("yeet")) {
            permissionClass = "Admin";
            isThreaded = "Yes";
            reactsToStop = "Yes";
            inputFormats = "!yeet + [String nickname] - initiates kick"+nl+
                "!yeet +\"yes\"/\"no\" - confirms kick"
            ;
            function = "Kicks the given user from the server and reinvites them. Author must confirm " +
                "the kick."
            ;
        }
        // shutdown
        else if (command.equalsIgnoreCase("shutdown")) {
            permissionClass = "Admin";
            isThreaded = "No";
            reactsToStop = "No";
            inputFormats = "!shutdown";
            function = "Shuts down the bot. It can only be re-launched by the developer.";
        }

        // checks if command exists but does not have an info-entry
        else {
            info.clearFields()
                .addField("Missing info", "This command exists, but it's usage details " +
                    "have not yet been logged for the !info command.", true)
            ;
        }

        // checks if info-fields for requested command are null
        try {
            info.addField("Permission class", permissionClass, true)
                    .addField("Is threaded?", isThreaded, true)
                    .addField("Reacts to *!stop*?", reactsToStop, true)
                    .addField("Input format/s", inputFormats, false)
                    .addField("Function", function, false)
            ;
        } catch (IllegalArgumentException ignored) {}


        MessageEmbed sendableEmbed = info.build();
        c.sendMessage(sendableEmbed).queue();

        // 'http://inspirobot.me/api?generate=true';
    }

    public boolean commandExistsInLog (String commandName) {
        for (String command : commands) {
            if (commandName.equalsIgnoreCase(command)) return true;
        }
        return false;
    }

    public boolean isRunning (String commandName) {
        for (Thread thread : runningThreads) {
            if (thread.getName().equalsIgnoreCase(commandName)) return true;
        }
        return false;
    }
}
