# Minotaur's Birthday Party

## How to run:
Make sure you have a java compiler installed.
* Make sure you have navigated to the directory storing all the java files for this assignment.
* To compile and run: type "javac Main.java && java Main" and press enter.
Output describes what happens in each of the minotaur's birthday games.

## Part 1: Minotaur's Labyrinth <br>

Problem: <br>
Guests must signal when they have all traveled through the Minotaur's labyrinth using a cupcake 
on a plate as their only means of communication. <br>
Solution: <br>
* Designate the first thread to enter as the counting / replacing thread.
* Only this thread can replace the cupcake. Every replacement increments a counter.
* Other threads will eat the cupcake only once, if the cupcake is available.
* Once the counter thread's count reaches the amount of guests, they indicate to the minotaur that 
all threads have traversed the labyrinth at least once.
    * Note that the counter thread will also eat a single cupcake/replace what it ate and count 
    itself.

### Correctness
Correct results mean that the designated counter thread does not notify the minotaur that all 
guests have completed the labyrinth until they are 100% sure this is the case. It is possible for 
multiple guests to be traversing the maze at the same time. This means that the solution must 
prevent race conditions involving eating and replacing the cupcake.

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

## Part 2: Minotaur's Crystal Vase. <br>

### Viewing Strategies <br>
Strategy 1: TAS (Test and Set) <br>
- Pros: This strategy doesn't violate mutual exclusion. 
- Cons: getAndSet() calls cause lots of cache misses, which causes bus overuse that blocks other threads.
Strategy 2: TTAS (Test and Test and Set) <br>
- Pros: This strategy doesn't violate mutual exclusion and causes less cache misses than TAS since threads spin on local cache.
- Cons: When the lock is released, all spinning threads get a cache miss and call getAndSet() all at once, which causes a storm of bus traffic.
Strategy 3: Queue-based Spin Locks<br>
- Pros: Threads access the critical section in FIFO order. Lock releases don't cause an invalidation storm for all spinning threads.
- Cons: Significant overhead can impact runtime, although this is somewhat implementation dependent.
