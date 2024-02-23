// Room that holds the minotaur's crystal vase with a queue-based lock system.

import java.util.concurrent.*; // todo consolidate
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Random;

public class VaseRoom
{
    private final int TIME_TO_VIEW = 500; // Amount of milliseconds to view vase.

    // Acts as the lock for the critical section (viewing the vase in the room).
    AtomicBoolean roomTaken;
    long [] guestIds;
    int numGuests;
    int numViewers; // How many have seen the case?

    VaseRoom(int num)
    {
        roomTaken = new AtomicBoolean(false);
        guestIds = new long [numGuests];
        numGuests = num;
        numViewers = 0;
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
    public void viewVase(long id, boolean hasSeenVase)
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

// class CLHQ implements Lock
// {
//     AtomicReference<Qnode> tail;
//     ThreadLocal<Qnode> myPred;
//     ThreadLocal<QNode> myNode;

//     // Constructor initializes the node and makes the predecessor null.
//     CLHQ()
//     {
//         this.tail = new AtomicReference<Qnode>(new Qnode());

//         // Initialize thread local node.
//         this.myNode = new ThreadLocal<Qnode>() 
//         {
//             protected Qnode initialValue() 
//             {
//                 return new Qnode();
//             }
//         };

//         // Initialize the prev node to null.
//         this.myPred = new ThreadLocal<Qnode>()
//         {
//             protected Qnode initialValue()
//             {
//                 return null;
//             }
//         };
//     }

//     // Acquire the lock before entering a critical section.
//     public void lock()
//     {
//         Qnode mynode = myNode.get();
//         mynode.locked = true;
//         Qnode pred = tail.getAndSet(mynode);
//         myPred.set(pred);
//     }
// }

// class Qnode
// {
//     AtomicBoolean locked = new AtomicBoolean(true);

//     // Returns a reference to this node.
//     public Qnode get()
//     {
//         return this; // todo is this valid?
//     }

//     public void set(Qnode newNode)
//     {
//         this = newNode;
//     }
// }