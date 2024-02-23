// The maze for part 1 of the assignment.
import java.util.concurrent.*; // todo consolidate.
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Labyrinth
{
    // Fields
    private boolean cupcake; // Is a cupcake on the plate?
    // Did the minotaur just call someone or is he waiting?
    private boolean minotaurCalledFirstGuest;
    private int numGuests;
    private AtomicInteger amtInLabyrinth; // How many are currently inside?
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
        amtInLabyrinth = new AtomicInteger(0);
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
            int waitTime = rand.nextInt(21) + 1;
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
    }

    public void setCounterThread()
    {
        GuestNode myNode = guestInfo.get();
        myNode.isFirstCalled = true;
    }

    // A guest takes some random amount of time to traverse the labyrinth.
    public void guestTraverseLabyrinth(String guestName)
    {
        System.out.println(guestName + " traversing.");
        amtInLabyrinth.getAndIncrement();

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
        // System.out.println(name + "'s info:\n" + 
        //     "isCounter: " + node.isFirstCalled + "\n" + 
        //     "counter: " + node.counter + "\n" + 
        //     "hasEaten: " + node.hasEaten + "\n");

        // Try to eat if I haven't yet and I'm not the counter thread.
        if (!node.hasEaten && !node.isFirstCalled)
        {
            node.hasEaten = tryEating(name);
        }
            
        if (node.isFirstCalled)
        {
            // Replace the cupcake and increment my counter.
            if (!cupcake)
            {
                replaceCupcake(name);
                node.counter++;
            }

            if (node.counter == (numGuests - 1))
            {
                allGuestsEntered = true;
                System.out.println(name + " reports that all guests have traversed the labyrinth!");
            }
        }

        amtInLabyrinth.getAndDecrement();
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

    // Allows main driver to wait until all have finished traversing.
    public int getAmtInLabyrinth()
    {
        return amtInLabyrinth.get();
    }
}

class GuestNode
{
    public boolean isFirstCalled;
    public boolean hasEaten;
    public int counter;

    public GuestNode(boolean first, boolean eaten, int ct)
    {
        isFirstCalled = first;
        hasEaten = eaten;
        counter = ct;
    }
}
