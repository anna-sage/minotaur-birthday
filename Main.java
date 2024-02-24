// Author: Anna MacInnis, Last update on 2/19/2024.
// Part 1: Minotaur's maze with cupcake at the end.
// Part 2: Minotaur's crystal vase viewing with mutual exclusion.
// TODO : eustis check.
// TODO : move queue code to separate folders

import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.Random;

public class Main
{
    public static final int NUM_GUESTS = 10;

    public static void main(String [] args)
    {
        // Initialize guests (threads) and the labyrinth.
        Labyrinth maze = new Labyrinth(NUM_GUESTS);
        VaseRoom room = new VaseRoom(NUM_GUESTS);

        ExecutorService [] guests = new ExecutorService [NUM_GUESTS];
        GuestFactory guestThreadMaker = new GuestFactory();

        // Create threads and the labyrinth.
        for (int i = 0; i < NUM_GUESTS; i++)
        {
            guests[i] = Executors.newSingleThreadExecutor(guestThreadMaker);
        }

        // Have the minotaur start calling guests.
        maze.minotaurCallGuest(guests);

        // Wait for guests to finish labyrinth game before printing problem 2.
        while (!maze.labyrinthGameFinished())
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

        // Allow guests to view the crystal vase.
        room.beginVaseViewing(guests);


        // Shut down threads.
        for (int i = 0; i < NUM_GUESTS; i++)
        {
            guests[i].shutdown();

            // Make sure program doesn't run forever.
            try 
            {
                if (!guests[i].awaitTermination(10000, TimeUnit.MILLISECONDS)) {
                    guests[i].shutdownNow();
                } 
            } 
            catch (Exception e) 
            {
                guests[i].shutdownNow();
            }
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
