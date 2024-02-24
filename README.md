# Minotaur's Birthday Party

## How to run:
Make sure you have a java compiler installed.
* Make sure you have navigated to the directory storing all the java files for this assignment.
* To compile and run: type "javac Main.java && java Main" and press enter.
Output describes what happens in each of the minotaur's birthday games.

## Part 1: Minotaur's Labyrinth <br>

### Problem:
N Guests must signal when they have all traveled through the Minotaur's labyrinth using a cupcake 
on a plate as their only means of communication. <br>
Solution: <br>
* Designate the first thread to enter as the counting / replacing thread. This thread will never 
eat a cupcake.
* Only this thread can replace the cupcake. Every replacement increments a counter.
* Other threads will eat the cupcake only once, if the cupcake is available.
* Once the counter thread's count reaches N - 1, they indicate to the minotaur that all threads 
have traversed the labyrinth at least once.

### Correctness
Correct results mean that the designated counter thread does not notify the minotaur that all 
guests have completed the labyrinth until they are 100% sure this is the case. It is possible for 
multiple guests to be traversing the maze at the same time. This means that the solution must 
prevent race conditions involving eating the cupcake.

Allowing only the counter thread to replace the cupcake and dictating that all threads may only eat 
once ensures that the counter thread's count is accurate. In addition, making the labyrinth exit 
logic synchronized enforces mutual exclusion for interacting with the cupcake.

### Efficiency
For the program to run efficiently, there should be no excess of sequential operations. The only 
critical section that must only be accessed by a single thread at a time is the logic to interact 
with the cupcake plate.

Runtime calculation is not feasible due to the random nature of who the minotaur summons into the 
labyrinth and how long a particular guest takes to traverse.

### Experiemental Evaluation
I included many "debugging" print statements that are no longer present in the final version to 
help ensure correctness. These print statements helped me keep track of who the minotaur summoned, 
who actually entered, and how guests interacted with the cupcake. They also helped me monitor how 
the counter thread was keeping track of cupcake replacements.

I checked that exactly N - 1 "Thread-0 is calling for a replacement!" statements were printed.

## Part 2: Minotaur's Crystal Vase.

### Problem:
Allow only a single guest at a time in the Minotaur's crystal vase viewing room.

### Viewing Strategies
Strategy 1: TAS (Test and Set) <br>
- Pros: This strategy doesn't violate mutual exclusion. It is also simple to implement.
- Cons: getAndSet() calls cause lots of cache misses, which causes bus overuse that blocks other threads.
Strategy 2: TTAS (Test and Test and Set) <br>
- Pros: This strategy doesn't violate mutual exclusion and causes less cache misses than TAS since threads spin on local cache. It is also still simple to implement.
- Cons: When the lock is released, all spinning threads get a cache miss and call getAndSet() all at once, which causes a storm of bus traffic.
Strategy 3: Queue-based Spin Locks<br>
- Pros: Threads access the critical section in FIFO order. Lock releases don't cause an invalidation storm for all spinning threads.
- Cons: Significant overhead can impact runtime, although this is somewhat implementation/scenario dependent.

Chosen strategy:<br>
My implementation uses a CLH queue spin lock. My reasons for doing so are outlined in the 
"Experimental Evaluation" section below.

### Correctness
A correct implementation ensures mutual exclusion. My solution uses a spin lock that threads must 
acquire before accessing the critical section, which is being inside the vase viewing room. When 
one thread is in this critical section, other threads must wait to acquire the lock.

### Efficiency
The only operations that use locks are those of the critical section. Runtime is also difficult to 
accurately determine for this problem due to the random nature of when guests decide they want to 
view the crystal vase.

### Experimental Evaluation
To decide between strategies 2 and 3, I implemented both and ran some tests. I modified the code to 
exclude any randomness only for testing purposes, then compared the performance of the TTAS lock, 
the CLH queue lock, and the MCS queue lock for various amounts of guests. <br>

For small amounts of guests, the differences in runtime were trivial. However, for a large number 
of guests (100), the TTAS lock took a little longer than the queue locks. This is why my 
implementation uses strategy 3. I predict this difference is due to the TTAS lock's invalidation 
storm of many threads on the shared bus when one thread releases the lock. <br>

There was little difference between the execution times of the CLH queue versus the MCS queue, so 
I chose the CLH queue. The code for the TTAS and MCS queue implementations is in the 
"unused-strategies" directory.
