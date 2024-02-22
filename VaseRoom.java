// Room that holds the minotaur's crystal vase with a queue-based lock system.

import java.util.concurrent.*; // todo consolidate

public class VaseRoom
{

}

class CLHQ implements Lock
{
    AtomicReference<Qnode> tail;
    ThreadLocal<Qnode> myPred;
    ThreadLocal<QNode> myNode;

    // Constructor initializes the node and makes the predecessor null.
    CLHQ()
    {
        this.tail = new AtomicReference<Qnode>(new Qnode());

        // Initialize thread local node.
        this.myNode = new ThreadLocal<Qnode>() 
        {
            protected Qnode initialValue() 
            {
                return new Qnode();
            }
        };

        // Initialize the prev node to null.
        this.myPred = new ThreadLocal<Qnode>()
        {
            protected Qnode initialValue()
            {
                return null;
            }
        };
    }

    // Acquire the lock before entering a critical section.
    public void lock()
    {
        Qnode mynode = myNode.get();
        mynode.locked = true;
        Qnode pred = tail.getAndSet(mynode);
        myPred.set(pred);
    }
}

class Qnode
{
    AtomicBoolean locked = new AtomicBoolean(true);

    // Returns a reference to this node.
    public Qnode get()
    {
        return this; // todo is this valid?
    }

    public void set(Qnode newNode)
    {
        this = newNode;
    }
}