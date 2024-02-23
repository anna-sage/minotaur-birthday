// Author: Anna MacInnis, Last update on 2/19/2024.
// Part 1: Minotaur's maze with cupcake at the end.
// Part 2: Minotaur's crystal vase viewing with mutual exclusion.
// TODO : make sure compilation instructions work.
// TODO : minimize synchronized behavior

import java.util.concurrent.*; // todo consolidate
import java.lang.Thread;
import java.lang.Runnable;
import java.util.concurrent.Executors;
import java.util.Random;

public class Main
{
    public static final int NUM_GUESTS = 4;

    public static void main(String [] args)
    {
        // Initialize guests (threads) and the labyrinth.
        Labyrinth maze = new Labyrinth(NUM_GUESTS);
        VaseRoom room = new VaseRoom(NUM_GUESTS);
        // Thread [] guests = new Thread [NUM_GUESTS];
        ExecutorService [] guests = new ExecutorService [NUM_GUESTS];
        GuestFactory guestThreadMaker = new GuestFactory();
        long [] guestIds = new long [NUM_GUESTS];

        // Create threads and the labyrinth.
        for (int i = 0; i < NUM_GUESTS; i++)
        {
            // guests[i] = new Thread(new Guest(maze, room));
            guests[i] = Executors.newSingleThreadExecutor(guestThreadMaker);
            guestIds[i] = i + 1;
        }

        // long start = System.nanoTime();
        // for (int i = 0; i < NUM_GUESTS; i++)
        // {
        //     // guests[i].start();

        // }

        // Have the minotaur start calling guests.
        System.out.println("Problem 1: Labyrinth");
        System.out.println("================================");
        maze.minotaurCallGuest(guests);

        // Wait for guests to finish labyrinth game before printing problem 2.
        while (maze.getAmtInLabyrinth() > 0)
        {
            try
            {
                TimeUnit.MILLISECONDS.sleep(500);
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }
        }


        for (int i = 0; i < NUM_GUESTS; i++)
        {
            guests[i].shutdown();
        }
    }
}

// To make guests.
class GuestFactory implements ThreadFactory 
{
    public Thread newThread(Runnable r) {
      return new Thread(r);
    }
}
