// Author: Anna MacInnis, Last update on 2/19/2024.
// Part 1: Minotaur's maze with cupcake at the end.
// Part 2: Minotaur's crystal vase viewing with mutual exclusion.

import java.util.concurrent.*; // todo consolidate
import java.lang.Thread;
import java.lang.Runnable;

public class Main
{
    public static final int NUM_GUESTS = 8;

    public static void main(String [] args)
    {
        // Initialize guests (threads) and the labyrinth.
        Labyrinth maze = new Labyrinth(NUM_GUESTS);
        Thread [] guests = new Thread [NUM_GUESTS];
        long [] guestIds = new long [NUM_GUESTS];

        // Create threads and the labyrinth.
        for (int i = 0; i < NUM_GUESTS; i++)
        {
            guests[i] = new Thread(new Game(maze));
            guestIds[i] = guests[i].getId();
            System.out.println("Thread created with id " + guests[i].getId());
        }

        maze.setGuestIds(guestIds);

        for (int i = 0; i < NUM_GUESTS; i++)
        {
            guests[i].start();
        }

        // Have the minotaur start calling guests.
        maze.minotaurCallGuest();

        // Wait for all threads to conclude.
        for (int i = 0; i < NUM_GUESTS; i++)
        {
            try
            {
                guests[i].join();
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }
        }
    }
}

// Represents the guests.
class Game implements Runnable
{
    private Status myStatus; // Local counter and hasEaten tracker.
    private Labyrinth maze; // Reference to current maze.

    Game(Labyrinth mazeArg)
    {
        maze = mazeArg;
        myStatus = new Status(0, false);
    }

    public void run()
    {
        while (!maze.getAllGuestsEntered())
        {
            // Wait for the minotaur to call me.
            long myId = Thread.currentThread().getId();
            long idCalled = -1;
            while (idCalled != myId && !maze.getAllGuestsEntered()) 
            {
                // Do nothing since this guest hasn't been called yet.
                System.out.print("");
                idCalled = maze.getNextToEnter();
            }

            // The minotaur called me.
            if (idCalled == myId)
            {
                System.out.println("Minotaur called guest" + myId);
                maze.resetNextToEnter();
                maze.guestTraverseLabyrinth(myId);
                myStatus = maze.exitProcedure(myId, myStatus);
    
                if (!myStatus.hasEaten)
                    myStatus.hasEaten = maze.tryEating(myId);
    
                // Update the counter and request new cupcake if necessary.
                myStatus.counter = maze.updateCounter(myId) ? myStatus.counter + 1 : myStatus.counter;
            }
        }
    }
}

// The status of a guest.
class Status 
{
    public boolean hasEaten;
    public int counter;

    Status(int c, boolean eaten)
    {
        counter = c;
        hasEaten = eaten;
    }
}
