import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.TimeUnit;

public class TTASLock
{
    AtomicBoolean sign;

    public TTASLock()
    {
        sign = new AtomicBoolean(false);
    }

    // Simple TTAS lock.
    public void lockTTAS()
    {
        while (true)
        {
            while (sign.get()) {}

            if (!sign.getAndSet(true))
                return;
        }
    }

    // Simple TTAS unlock.
    public void unlockTTAS()
    {
        sign.set(false);
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
}
