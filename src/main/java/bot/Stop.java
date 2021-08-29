package bot;

import bot.controlCenter.MsgListener;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class Stop extends Thread {
    String request = "";
    List<Thread> threads;
    TextChannel c;

    public Stop (String argument, List<Thread> runningThreads, TextChannel channel) {
        c = channel;
        request = argument;
        threads = runningThreads;
    }


    public void run () {
        // disables message listener to avoid missing threads caused by incoming commands during operation
        MsgListener.isEnabled = false;
        c.sendMessage("Bot disabled for all standard & threaded commands. Please wait for reactivation.").queue();

        c.sendMessage("Attempting to interrupt "+request+" threads.").queue();

        // informs & interrupts self if no threads of requested type are running
        if (isFinished(request, threads)) {
            if (request.equalsIgnoreCase("all")) c.sendMessage("No threads are currently running.").queue();
            else c.sendMessage("No threads of type \""+request+"\" are currently running.").queue();
            reenable();
            return; // interrupts this thread; interrupt() doesn't work here, a thread can't interrupt itself
        }

        // handles the thread stopping
        while (!isFinished(request, threads)) {
            for (int i = 0; i < threads.size(); i++) {
                Thread thread = threads.get(i);
                String threadName = thread.getName();
                int threadNumber = i + 1;

                // skips over thread if it is not part of requested group of threads
                if (!request.equalsIgnoreCase("all") && !threadName.equalsIgnoreCase(request)) {
                    continue;
                }

                // interrupts thread, then gives it time to shut down
                if (thread.isAlive()) {
                    thread.interrupt();
                    brieflyWait();
                } else continue;


                // checks if thread was interrupted successfully
                if (thread.isAlive())
                    c.sendMessage("Failed to interrupt thread No. " + threadNumber + " \"" + threadName + "\".").queue();
                else
                    c.sendMessage("Successfully interrupted thread No. " + threadNumber + " \"" + threadName + "\".").queue();
            }
        }
        // informs that all requested threads have been shut down
        if (request.equalsIgnoreCase("all")) c.sendMessage("All threads have been interrupted.").queue();
        else c.sendMessage("All requested threads have been interrupted.").queue();

        // reenables message listener
        reenable();
    }

    // lets the thread sleep briefly, because otherwise thread.isAlive()
    // returns true, as it's still in the process of shutting down
    public void brieflyWait() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isFinished (String type, List<Thread> threads) {
        Thread thread;
        String threadName;

        // checks if any threads of requested type are still running
        for (int i = 0; i < threads.size(); i++) {
            thread = threads.get(i);
            threadName = thread.getName();

            if (threadName.equalsIgnoreCase(type) || request.equalsIgnoreCase("all")) {
                if (thread.isAlive())
                    return false;
            }
        }

        return true;
    }

    public void reenable () {
        MsgListener.isEnabled = true;
        c.sendMessage("Commands have been reenabled.").queue();
    }
}
