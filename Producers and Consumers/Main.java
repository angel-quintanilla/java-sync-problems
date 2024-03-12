import java.util.ArrayDeque;
import java.util.concurrent.Semaphore;

public class Main{

  static final int MAX_QUEUE_SIZE = 10; 
  static final int MIN = 5; 
  static final int MAX = 500; 
  public static void main(String[] args) {
    
    ArrayDeque<Integer> q = new ArrayDeque<Integer>(); 
    Semaphore sem = new Semaphore(1); 
    
    Producer p1 = new Producer(sem, q);
    Producer p2 = new Producer(sem, q);
    Producer p3 = new Producer(sem, q);

    Consumer c1 = new Consumer(sem, q);
    Consumer c2 = new Consumer(sem, q);
    Consumer c3 = new Consumer(sem, q);


    try{
      
      p1.start();
      p2.start();
      p3.start();

      c1.start();
      c2.start();
      c3.start(); 


      // These threads will run infinitely so our main thread will not run beyond this
      p1.join(); 
      p2.join(); 
      p3.join();
      c1.join();
      c2.join();
      c3.join();

    }catch (InterruptedException e){} //heh.
    
    System.exit(0);
  }
}


class Producer extends Thread{ 
  

  private Semaphore sem; 
  private ArrayDeque<Integer> q; 

  
  /**
   * 
   * @param sem   Semaphore used to signal when queue is free to modify
   * @param q     Array queue to produce into. 
   * */
  public Producer(Semaphore sem, ArrayDeque<Integer> q){
    this.sem = sem; 
    this.q = q; 
  }

  @Override
  public void run(){
    for(;;){
      int production = Main.MIN + (int)(Math.random()*(Main.MAX-Main.MIN+1)); 
      boolean posted = false; 
      
      //Post your message, if its currently full then release and try again
      while(!posted){
        try {
          sem.acquire();
          
          if(q.size() != Main.MAX_QUEUE_SIZE){
            q.add(production); 
            posted = true; 
            System.out.printf("Value of: %d has been produced.\n", production); 
          }
          
        } 
        //interruptions during acquire() don't post, so this just reloops 
        catch (InterruptedException e) {}
        finally { sem.release(); }
      }

      // When the thread finally produces, it takes 1.5 seconds until it can produce something else 
      // This can be completely commented out, only included for the sake of seeing a readable stream of output
      try { Thread.sleep(1500);}
      catch(InterruptedException e) {}

    }
  }
}

class Consumer extends Thread{ 
  
  private Semaphore sem; 
  private ArrayDeque<Integer> q; 


  /**
   * 
   * @param sem   Semaphore used to signal when the queue is free to modify
   * @param q     Array queue to consume from 
   */
  public Consumer(Semaphore sem, ArrayDeque<Integer> q){
    this.sem = sem; 
    this.q = q; 
  }
  
  @Override
  public void run(){
    for(;;){
      try{
        sem.acquire(); 
        Integer retrieval = q.poll(); 
        if(retrieval != null) System.out.printf("Value of: %d has been consumed.\n", retrieval); 
      }
      catch(InterruptedException e) {}
      finally { sem.release();}

    }
    
  }
}
