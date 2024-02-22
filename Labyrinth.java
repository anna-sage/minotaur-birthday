// The maze for part 1 of the assignment.
import java.util.concurrent.*; // todo consolidate.
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Labyrinth
{
    // Fields
    private boolean cupcake; // Is a cupcake on the plate?
    private int numGuests;
    private long firstGuest; // ID of the first guest to be called in.
    private long [] guestIds;

    // Did the minotaur just call someone or is he waiting?
    private boolean minotaurCalledFirstGuest;
    private long nextToEnter; // Minotaur calls this guest.
    private boolean allGuestsEntered;

    // Amount of booleans to return to a guest when they exit.
    private static final int AMT_RESULTS = 2;

    Labyrinth(int numG)
    {
        // Get ids from main driver class.
        guestIds = new long [numGuests];

        cupcake = true;
        numGuests = numG;
        firstGuest = -1;
        minotaurCalledFirstGuest = false;
        nextToEnter = -1;
        allGuestsEntered = false;
    }

    // Summon random guests into the labyrinth until one of the
    // threads indicates that all have had the chance to enter.
    public void minotaurCallGuest()
    {
        Random rand = new Random();

        while (!allGuestsEntered)
        {
            // Wait some random time amount of time.
            int waitTime = rand.nextInt(6) + 1;
            try
            {
                TimeUnit.MILLISECONDS.sleep(waitTime);
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }
            
            // Call a random guest into the labyrinth.
            int guestIdx = rand.nextInt(numGuests);

            if (!minotaurCalledFirstGuest)
            {
                // Designate the counter guest.
                firstGuest = guestIds[guestIdx];
                minotaurCalledFirstGuest = true;
            }

            nextToEnter = guestIds[guestIdx];
        }
    }

    // A guest takes some random amount of time to traverse the labyrinth.
    public void guestTraverseLabyrinth(long guestId)
    {
        System.out.println("\t" + guestId + " traversing");
        Random rand = new Random();
        int traverseTime = rand.nextInt(6) + 1;
        try
        {
            TimeUnit.MILLISECONDS.sleep(traverseTime);
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }

    // Strategy for a guest trying to exit.
    public synchronized Status exitProcedure(long id, Status guestStatus)
    {
        System.out.println("Guest " + id + " has reached the end!");

        // Try to eat if I haven't yet.
        boolean ate = false;
        if (!guestStatus.hasEaten)
        {
            ate = tryEating(id);
            guestStatus.hasEaten = ate;
        }
            
        if (id == firstGuest)
        {
            // Replace the cupcake and update my counter.
            if (!cupcake)
            {
                replaceCupcake(id);
                guestStatus.counter++;
            }

            // Account for case where some thread finishes before
            // the first thread called gets a chance to eat.
            if (!ate && !guestStatus.hasEaten)
            {
                cupcake = false;
                System.out.println("\tGuest " + id + ": \"I ate!\"");
                replaceCupcake(id);
                guestStatus.hasEaten = true;
                guestStatus.counter++;
            }

            if (guestStatus.counter == numGuests)
            {
                allGuestsEntered = true;
                System.out.println("Guest " + id + ": \"All guests have traversed the labyrinth!\"");
            }
        }

        return guestStatus;
    }

    // Attempt to eat the cupcake. Returns whether the guest was able to eat.
    public synchronized boolean tryEating(long id)
    {
        System.out.println("\tGuest " + id + ": \"I'm hungry!\"");
        if (cupcake)
        {
            System.out.println("\tGuest " + id + ": \"I ate!\"");
            cupcake = false;
            return true; // Was able to eat.
        }

        System.out.println("\tGuest " + id + ": \"Maybe next time.\"");
        return false; // Unable to eat.
    }

    private void replaceCupcake(long id)
    {
        System.out.println("\tGuest " + id + " is calling for a replacement");
        cupcake = true;
    }

    // Logic necessary for encountering the end of the maze.
    public synchronized boolean updateCounter(long myId)
    {
        // Am I the guest responsible for counting? And do I need to ask for a replacement?
        if (myId == firstGuest && !cupcake)
        {
            replaceCupcake(myId);
            return true;
        }

        return false;
    }

    // Getters and setters.

    public long getNextToEnter()
    {
        return nextToEnter;
    }

    public void resetNextToEnter()
    {
        nextToEnter = -1;
    }

    public boolean getAllGuestsEntered()
    {
        return allGuestsEntered;
    }

    public void setAllGuestsEntered(boolean val)
    {
        allGuestsEntered = val;
    }

    public void setGuestIds(long [] ids)
    {
        guestIds = ids;
    }
}
