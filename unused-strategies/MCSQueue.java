import java.util.concurrent.atomic.AtomicReference;
import java.lang.ThreadLocal;
import java.util.concurrent.TimeUnit;

public class MCSQueue
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
