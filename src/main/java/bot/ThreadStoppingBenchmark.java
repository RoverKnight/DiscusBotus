package bot;

import bot.controlCenter.MsgListener;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class ThreadStoppingBenchmark extends Thread {
    String mode;
    int threadAmount;
    int testAmount;
    List<Thread> threads;
    TextChannel c;

    public ThreadStoppingBenchmark (String argument, int numberOfThreads, int iterations, List<Thread> runningThreads, TextChannel channel) {
        c = channel;
        threadAmount = numberOfThreads;
        testAmount = iterations;
        mode = argument;
        threads = runningThreads;
    }

    public void run () {
        MsgListener.isEnabled = false;
        c.sendMessage("Starting benchmark...").queue();
        c.sendMessage("Disabled bot for all commands for duration of benchmark.").queue();

        double runs = 0;
        double totalThreadsMissed = 0;
        for (int i = 0; i < testAmount; i++) {
            int missedThreads = benchmark();
            totalThreadsMissed += missedThreads;

            runs++;
            if (missedThreads != 0) c.sendMessage("Missed "+missedThreads+" threads on run "+runs+".").queue();


            if ((i+1) % 10 == 0) c.sendMessage("Currently at "+runs+" runs.").queue();
        }

        c.sendMessage("Benchmark done.").queue();
        c.sendMessage("Reenabled bot for all commands.").queue();

        double averageThreadsMissed = totalThreadsMissed / runs;

        int totalThreads = threadAmount * testAmount;
        c.sendMessage("Total number of threads run: "+totalThreads+".").queue();
        c.sendMessage("Total number of threads missed: "+totalThreadsMissed+".").queue();
        c.sendMessage("Average number of threads missed: "+averageThreadsMissed+".").queue();

        MsgListener.isEnabled = true;
    }

    public int benchmark () {
        //c.sendMessage("WARNING: This thread cannot be stopped via command!").queue();

        // STEP 1: clear all current threads
        ////////////////////////////////////
        SilentStop s = new SilentStop("all", threads, c);
        s.start();
        s.setName("Stop");

        //stalls until all other threads are shut down
        while (s.isAlive()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //c.sendMessage("Shutdown complete.").queue();

        // STEP 2: create threads
        /////////////////////////
        //c.sendMessage("Creating "+ threadAmount +" threads...").queue();

        for (int i = 0; i < threadAmount; i++) {
            bot.TestThread t = new bot.TestThread();
            t.start();
            t.setName("TestThread");
            threads.add(t);
            //MsgListener.runningThreads.add(t);
            int counter = i+1;
        }

        // STEP 3: run clearance benchmark by shutting down created threads
        ///////////////////////////////////////////////////////////////////

        //c.sendMessage("Benchmark starting...").queue();

        long startTime = System.currentTimeMillis();

        // shuts down threads
        SilentStop b = new SilentStop(mode, threads, c);
        b.start();
        b.setName("BenchmarkStop");

        // while shutdown is running, updates status of each test thread
        while (b.isAlive()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        updateThreads(threads);

        int missedThreads = threads.size();
        long currentTime = System.currentTimeMillis();
        long timeTaken = currentTime - startTime;

        //c.sendMessage("Shut down "+ threadAmount +" threads in "+timeTaken+" milliseconds.").queue();
        //c.sendMessage("Missed "+missedThreads+" threads.").queue();

        return missedThreads;
    }

    public void updateThreads (List<Thread> threads) {
        for (int i = 0; i < threads.size(); i++) {
            Thread thread = threads.get(i);
            if (!thread.isAlive()) {
                threads.remove(i);
                i--;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
