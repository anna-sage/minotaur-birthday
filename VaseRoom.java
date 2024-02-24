// Room that holds the minotaur's crystal vase with a queue-based lock system.

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.ExecutorService;
import java.util.Random;
import java.util.HashSet;

public class VaseRoom
{
    private final int TIME_TO_VIEW = 5; // Amount of milliseconds to view vase.

    int numGuests;
    // Which threads have viewed the vase at least once?
    HashSet<Integer> viewed;

    CLHQueue myCLHQ;

    // Thread task to enter the viewing room.
    class GuestVaseTask implements Runnable
    {
        public void run()
        {
            String name = Thread.currentThread().getName();
            viewVaseCLHQ(name);
        }
    }

    public VaseRoom(int num)
    {
        numGuests = num;
        viewed = new HashSet<>();
        myCLHQ = new CLHQueue();
    }

    // Starts the process of guests going to view the vase.
    public void beginVaseViewing(ExecutorService [] guests)
    {
        Random rand = new Random();

        while (viewed.size() < numGuests)
        {
            // Generate a random thread to view the vase.
            int guestIdx = rand.nextInt(numGuests);
            guests[guestIdx].submit(new GuestVaseTask());
            viewed.add(guestIdx);
        }
    }

    // CLHQ viewing.
    public void viewVaseCLHQ(String name)
    {
        myCLHQ.lock();
        System.out.println("\n" + name + " is viewing the vase...");

        try
        {
            TimeUnit.MILLISECONDS.sleep(TIME_TO_VIEW);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        System.out.println(name + " leaves the viewing room.");

        myCLHQ.unlock();
    }
}

class CLHQueue
{
    AtomicReference<CLHNode> tail;
    ThreadLocal<CLHNode> myPred;
    ThreadLocal<CLHNode> myNode;

    // Constructor initializes nodes.
    public CLHQueue()
    {
        tail = new AtomicReference<CLHNode>(new CLHNode());
        myNode = ThreadLocal.withInitial(() -> new CLHNode());
        myPred = ThreadLocal.withInitial(() -> null);
    }

    // Acquire the lock before entering a critical section.
    public void lock()
    {
        CLHNode node = myNode.get();
        node.locked = true;

        CLHNode pred = tail.getAndSet(node);
        myPred.set(pred);
        while (pred.locked) {}
    }

    public void unlock()
    {
        CLHNode node = myNode.get();
        node.locked = false;
        myNode.set(myPred.get());
    }
}

class CLHNode
{
    public volatile boolean locked;

    public CLHNode()
    {
        locked = false;
    }
}
