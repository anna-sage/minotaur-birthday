// Represents the guests.
class GuestVaseTask implements Runnable
{
    private Status myStatus; // Local counter and hasEaten tracker.
    private Labyrinth maze; // Reference to current maze.
    private VaseRoom room; // Reference to current vase room.

    public GuestVaseTask(Labyrinth mazeArg, VaseRoom roomArg)
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