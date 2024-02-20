# Part 1: Minotaur's Labyrinth <br>

# Part 2: Minotaur's Crystal Vase. <br>

## Viewing Strategies <br>
### Strategy 1: TAS (Test and Set) <br>
- Pros: This strategy doesn't violate mutual exclusion. 
- Cons: getAndSet() calls cause lots of cache misses, which causes bus overuse that blocks other threads.
### Strategy 2: TTAS (Test and Test and Set) <br>
- Pros: This strategy doesn't violate mutual exclusion and causes less cache misses than TAS since threads spin on local cache.
- Cons: When the lock is released, all spinning threads get a cache miss and call getAndSet() all at once, which causes a storm of bus traffic.
### Strategy 3: Queue-based Spin Locks<br>
- Pros: Threads access the critical section in FIFO order. Lock releases don't cause an invalidation storm for all spinning threads.
- Cons: todo
