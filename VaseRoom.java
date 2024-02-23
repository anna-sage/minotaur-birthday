// Room that holds the minotaur's crystal vase with a queue-based lock system.

import java.util.concurrent.*; // todo consolidate
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.Random;

public class VaseRoom
{
    private final int TIME_TO_VIEW = 500; // Amount of milliseconds to view vase.

    // Acts as the lock for the critical section (viewing the vase in the room).
    AtomicBoolean roomTaken;
    long [] guestIds;
    int numGuests;
    int numViewers; // How many have seen the case?

    CLHQ myQ;

    VaseRoom(int num)
    {
        roomTaken = new AtomicBoolean(false);
        guestIds = new long [numGuests];
        numGuests = num;
        numViewers = 0;
        myQ = new CLHQ();
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
    public void viewVaseTTAS(long id, boolean hasSeenVase)
    {
        lockTTAS();
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

        if (!hasSeenVase)
        {
            numViewers++;
        }

        unlockTTAS();
    }

    // CLHQ viewing.
    public void viewVaseCLHQ(long id, boolean hasSeenVase)
    {
        myQ.lock();
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

        if (!hasSeenVase)
        {
            numViewers++;
        }

        myQ.unlock();
    }

    // Get the amount of guests who have viewed the vase at least once.
    public boolean allViewedVase()
    {
        return !(numViewers < numGuests);
    }

    public void setGuestIds(long [] ids)
    {
        guestIds = ids;
    }
}

class CLHQ
{
    AtomicBoolean tail;
    // ThreadLocal<QNode> myPred;
    // ThreadLocal<QNode> myNode;

    // Constructor initializes the node and makes the predecessor null.
    CLHQ()
    {
        this.tail.set(false);

        // Initialize thread local node.
        this.myNode = new ThreadLocal<QNode>() 
        {
            protected QNode initialValue() 
            {
                return new QNode();
            }
        };

        // Initialize the prev node to null.
        this.myPred = new ThreadLocal<QNode>()
        {
            protected QNode initialValue()
            {
                return null;
            }
        };
    }

    // Acquire the lock before entering a critical section.
    public void lock()
    {
        System.out.println("Trying to get lock");
        boolean myVal = Thread.currentThread().getMyVal();
        Thread.currentThread().setMyVal(true);

        boolean pred = tail.getAndSet(myVal);
        boolean myPred = Thread.currentThread().getMyPred();
        Thread.currentThread().setMyPred(pred);
        while (pred.locked) {}
    }

    public void unlock()
    {
        boolean myVal = Thread.currentThread().getMyVal();
        Thread.currentThread().setMyVal(false);
        boolean myPred = Thread.currentThreas().getMyPred();
        Thread.currentThread().setMyVal(myPred);
        System.out.println("unlocked");
    }
}

class QNode
{
    public boolean locked;

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