package bot;

import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class SilentStop extends Thread {
    String request = "";
    List<Thread> threads;
    TextChannel c;

    public SilentStop(String argument, List<Thread> runningThreads, TextChannel channel) {
        c = channel;
        request = argument;
        threads = runningThreads;
    }


    public void run () {
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
            }
        }
    }

    // lets thread sleep briefly, because otherwise thread.isAlive()
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
}
