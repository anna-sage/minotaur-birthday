// The maze for part 1 of the assignment.

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.Random;

public class Labyrinth
{
    // Fields
    private boolean cupcake; // Is a cupcake on the plate?
    private int numGuests;
    private boolean allGuestsEntered;

    // Local thread info node.
    ThreadLocal<GuestNode> guestInfo;

    // Thread task to traverse the labyrinth.
    class GuestMazeTask implements Runnable
    {
        public void run()
        {
            String name = Thread.currentThread().getName();
            guestTraverseLabyrinth(name);
            exitProcedure(name);
        }
    }

    public Labyrinth(int numG)
    {
        cupcake = true;
        numGuests = numG;
        allGuestsEntered = false;

        // Initialize thread local node.
        guestInfo = ThreadLocal.withInitial(() -> new GuestNode(false, false, 0));
    }

    // Summon random guests into the labyrinth until one of the
    // threads indicates that all have had the chance to enter.
    public void minotaurCallGuest(ExecutorService [] guests)
    {
        // Designate guest at idx 0 as the counter
        guests[0].submit(() -> setCounterThread());
        Random rand = new Random();

        while (!allGuestsEntered)
        {
            // Wait some random time amount of time.
            int waitTime = rand.nextInt(3) + 1;
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
            guests[guestIdx].submit(new GuestMazeTask());
        }

        System.out.println("A guest reports that all guests have traversed at least once!");
    }

    // The current thread will be responsible for counting replacements.
    public void setCounterThread()
    {
        GuestNode myNode = guestInfo.get();
        myNode.isCounter = true;
    }

    // A guest takes some random amount of time to traverse the labyrinth.
    public void guestTraverseLabyrinth(String guestName)
    {
        // System.out.println(guestName + " traversing.");

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
    public void exitProcedure(String name)
    {
        // Get the current thread's local status node.
        GuestNode node = guestInfo.get();

        // Try to eat if I haven't yet and I'm not the counter thread.
        if (!node.hasEaten && !node.isCounter)
        {
            node.hasEaten = tryEating(name);
        }
            
        if (node.isCounter)
        {
            // Replace the cupcake and increment my counter.
            if (!cupcake)
            {
                replaceCupcake(name);
                node.counter++;
            }

            if (node.counter == (numGuests - 1))
                allGuestsEntered = true;
        }
    }

    // Attempt to eat the cupcake. Returns whether the guest was able to eat.
    public synchronized boolean tryEating(String name)
    {
        if (cupcake)
        {
            System.out.println(name + " ate a cupcake!");
            cupcake = false;
            return true; // Was able to eat.
        }

        return false; // Unable to eat.
    }

    private void replaceCupcake(String name)
    {
        System.out.println(name + " is calling for a replacement");
        cupcake = true;
    }

    public boolean labyrinthGameFinished()
    {
        return allGuestsEntered;
    }
}

// Stores information about a particular guest.
class GuestNode
{
    public boolean isCounter;
    public boolean hasEaten;
    public int counter;

    public GuestNode(boolean first, boolean eaten, int ct)
    {
        isCounter = first;
        hasEaten = eaten;
        counter = ct;
    }
}
