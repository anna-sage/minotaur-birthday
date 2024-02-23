// Room that holds the minotaur's crystal vase with a queue-based lock system.

import java.util.concurrent.*; // todo consolidate
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.Random;
import java.util.HashSet;

public class VaseRoom
{
    private final int TIME_TO_VIEW = 5; // Amount of milliseconds to view vase.

    // Acts as the lock for the critical section (viewing the vase in the room).
    AtomicBoolean roomTaken;
    int numGuests;
    // int numViewers; // How many have seen the vase?

    CLHQ myCLHQ;
    MCSQ myMCSQ;

    // Thread local tracker for if thread has seen the vase.
    ThreadLocal<Boolean> seenVase;

    // Thread task to enter the viewing room.
    class GuestVaseTask implements Runnable
    {
        public void run()
        {
            String name = Thread.currentThread().getName();
            viewVaseTTAS(name, getSeenVase());
        }
    }

    public VaseRoom(int num)
    {
        roomTaken = new AtomicBoolean(false);
        numGuests = num;
        // numViewers = 0;
        myCLHQ = new CLHQ();
        myMCSQ = new MCSQ();

        seenVase = ThreadLocal.withInitial(() -> false);
    }

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

    public boolean getSeenVase()
    {
        return seenVase.get();
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
    public void viewVaseTTAS(String name, boolean hasSeenVase)
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

        // if (!hasSeenVase)
        // {
        //     numViewers++;
        // }

        unlockTTAS();
    }

    // CLHQ viewing.
    public void viewVaseCLHQ(long id, boolean hasSeenVase)
    {
        myCLHQ.lock();
        System.out.println("\nGuest " + id + " is viewing the vase...");

        try
        {
            TimeUnit.MILLISECONDS.sleep(TIME_TO_VIEW);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        System.out.println("Guest " + id + " leaves the viewing room.");

        // if (!hasSeenVase)
        // {
        //     numViewers++;
        // }

        myCLHQ.unlock();
    }

    // MCSQ viewing.
    public void viewVaseMCSQ(long id, boolean hasSeenVase)
    {
        myMCSQ.lock();
        System.out.println("\nGuest " + id + " is viewing the vase...");

        try
        {
            TimeUnit.MILLISECONDS.sleep(TIME_TO_VIEW);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        System.out.println("Guest " + id + " leaves the viewing room.");

        // if (!hasSeenVase)
        // {
        //     numViewers++;
        // }

        myMCSQ.unlock();
    }

    // Get the amount of guests who have viewed the vase at least once.
    // public boolean allViewedVase()
    // {
    //     return !(numViewers < numGuests);
    // }
}

class CLHQ
{
    AtomicReference<QNode> tail;
    ThreadLocal<QNode> myPred;
    ThreadLocal<QNode> myNode;

    // Constructor initializes the node and makes the predecessor null.
    CLHQ()
    {
        tail = new AtomicReference<QNode>(new QNode());

        // Initialize thread local node.
        myNode = ThreadLocal.withInitial(() -> new QNode());

        // Initialize the prev node.
        myPred = ThreadLocal.withInitial(() -> null);
    }

    // Acquire the lock before entering a critical section.
    public void lock()
    {
        QNode node = myNode.get();
        node.locked = true;

        QNode pred = tail.getAndSet(node);
        myPred.set(pred);
        while (pred.locked) {}
    }

    public void unlock()
    {
        QNode node = myNode.get();
        node.locked = false;
        myNode.set(myPred.get());
    }
}

class QNode
{
    public volatile boolean locked;

    public QNode()
    {
        locked = false;
    }

    // // Returns a reference to this node.
    // public QNode get()
    // {
    //     return this; // todo is this valid?
    // }

    // public void set(QNode newNode)
    // {
    //     this = newNode;
    // }
}

class MCSQ
{
    AtomicReference<MCSNode> tail;
    ThreadLocal<MCSNode> myNode;

    public MCSQ()
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