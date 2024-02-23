// Author: Anna MacInnis, Last update on 2/19/2024.
// Part 1: Minotaur's maze with cupcake at the end.
// Part 2: Minotaur's crystal vase viewing with mutual exclusion.
// TODO : make sure compilation instructions work.
// TODO : minimize synchronized behavior

import java.util.concurrent.*; // todo consolidate
import java.lang.Thread;
import java.lang.Runnable;
import java.util.Random;

public class Main
{
    public static final int NUM_GUESTS = 100;

    public static void main(String [] args)
    {
        // Initialize guests (threads) and the labyrinth.
        Labyrinth maze = new Labyrinth(NUM_GUESTS);
        VaseRoom room = new VaseRoom(NUM_GUESTS);
        Thread [] guests = new Thread [NUM_GUESTS];
        long [] guestIds = new long [NUM_GUESTS];

        // Create threads and the labyrinth.
        for (int i = 0; i < NUM_GUESTS; i++)
        {
            guests[i] = new Thread(new Guest(maze, room));
            guestIds[i] = guests[i].getId();
            System.out.println("Thread created with id " + guests[i].getId());
        }

        maze.setGuestIds(guestIds);
        room.setGuestIds(guestIds);

        long start = System.nanoTime();
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

        long end = System.nanoTime();
        System.out.println("\n" + (end - start));
    }
}

// Represents the guests.
class Guest implements Runnable
{
    private Status myStatus; // Local counter and hasEaten tracker.
    private Labyrinth maze; // Reference to current maze.
    private VaseRoom room; // Reference to current vase room.

    Guest(Labyrinth mazeArg, VaseRoom roomArg)
    {
        maze = mazeArg;
        room = roomArg;
        myStatus = new Status(0, false);
    }

    public void run()
    {
        long myId = Thread.currentThread().getId();

        // Problem 1: All guests traverse the labyrinth.
        while (!maze.getAllGuestsEntered())
        {
            // Wait for the minotaur to call me.
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
            }
        }

        // while (maze.getAmtInLabyrinth() > 0) {} // Let the labyrinth game end.

        // Problem 2: viewing the crystal vase with mutual exclusion.
        // Random rand = new Random(); // To help decide when to view vase.
        // room.viewVaseTTAS(myId, myStatus.hasSeenVase);
        // room.viewVaseCLHQ(myId, myStatus.hasSeenVase);
        // room.viewVaseMCSQ(myId, myStatus.hasSeenVase);
        // while (!room.allViewedVase())
        // {
        //     while (rand.nextInt(4) != 3) {}
        //     room.viewVase(myId, myStatus.hasSeenVase);
        //     myStatus.hasSeenVase = true;
        // }
    }
}

// The status of a guest.
class Status 
{
    public boolean hasEaten;
    public int counter;
    public boolean hasSeenVase;

    Status(int c, boolean eaten)
    {
        counter = c;
        hasEaten = eaten;
        hasSeenVase = false;
    }
}
