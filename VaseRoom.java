// Room that holds the minotaur's crystal vase with a queue-based lock system.

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.Random;
import java.util.HashSet;

public class VaseRoom
{
    private final int TIME_TO_VIEW = 5; // Amount of milliseconds to view vase.

    // Acts as the lock for the critical section (viewing the vase in the room).
    AtomicBoolean roomTaken;
    int numGuests;

    CLHQueue myCLHQ;
    MCSQueue myMCSQ;

    // Thread task to enter the viewing room.
    class GuestVaseTask implements Runnable
    {
        public void run()
        {
            String name = Thread.currentThread().getName();
            viewVaseTTAS(name);
        }
    }

    public VaseRoom(int num)
    {
        roomTaken = new AtomicBoolean(false);
        numGuests = num;
        myCLHQ = new CLHQueue();
        myMCSQ = new MCSQueue();
    }

    // Starts the process of guests going to view the vase.
    public void beginVaseViewing(ExecutorService [] guests)
    {
        Random rand = new Random();
        HashSet<Integer> idxsViewed = new HashSet<>();

        // Stop earlier when there are more guests.
        int stoppingPoint = numGuests >= 50 ? numGuests / 2 : numGuests;

        while (idxsViewed.size() < stoppingPoint)
        {
            // Generate a random thread to view the vase.
            int guestIdx = rand.nextInt(numGuests);
            guests[guestIdx].submit(new GuestVaseTask());
            idxsViewed.add(guestIdx);
        }
    }

    // Simple TTAS lock.
    public void lockTTAS()
    {
        while (true)
        {
            while (roomTaken.get()) {}

            if (!roomTaken.getAndSet(true))
                return;
        }
    }

    // Simple TTAS unlock.
    public void unlockTTAS()
    {
        roomTaken.set(false);
    }

    // Actually try to enter the room.
    public void viewVaseTTAS(String name)
    {
        lockTTAS();
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

        unlockTTAS();
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

    // MCSQ viewing.
    public void viewVaseMCSQ(String name)
    {
        myMCSQ.lock();
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

        myMCSQ.unlock();
    }
}

class CLHQueue
{
    AtomicReference<CLHNode> tail;
    ThreadLocal<CLHNode> myPred;
    ThreadLocal<CLHNode> myNode;

    public CLHQueue()
    {
        tail = new AtomicReference<CLHNode>(new CLHNode());

        // Initialize thread local node.
        myNode = ThreadLocal.withInitial(() -> new CLHNode());

        // Initialize the prev node.
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

class MCSQueue
{
    AtomicReference<MCSNode> tail;
    ThreadLocal<MCSNode> myNode;

    public MCSQueue()
    {
        tail = new AtomicReference<MCSNode>(null);

        // Initialize thread local node.
        myNode = ThreadLocal.withInitial(() -> new MCSNode());
    }

    public void lock()
    {
        MCSNode node = myNode.get();
        MCSNode pred = tail.getAndSet(node);

        if (pred != null)
        {
            node.locked = true;
            pred.next = node;
            while (node.locked) {}
        }
    }

    public void unlock()
    {
        MCSNode node = myNode.get();
        if (node.next == null)
        {
            if (tail.compareAndSet(node, null))
                return;

            while (node.next == null) {}
        }

        node.next.locked = false;
        node.next = null;
    }
}

class MCSNode
{
    public volatile boolean locked;
    public volatile MCSNode next;

    public MCSNode()
    {
        locked = false;
        next = null;
    }
}
