/*
 * Puzzle : Extend the solution so that when a writer arrives, the existing readers can finish 
 *          but no additional readers may enter
 */
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class Main {
    public static void main(String[] args) {
        Semaphore mutex = new Semaphore(1);
        Semaphore noReaders = new Semaphore(1); 
        Semaphore turnstile = new Semaphore(1);
        LinkedList<Integer> ll = new LinkedList<Integer>(); 


        Reader r1 = new Reader(mutex, noReaders, ll, turnstile, 1); 
        Reader r2 = new Reader(mutex, noReaders, ll, turnstile,  2); 
        Reader r3 = new Reader(mutex, noReaders, ll, turnstile,  3); 
        
        Writer w1 = new Writer(noReaders, ll, turnstile, 1);
        Writer w2 = new Writer(noReaders, ll, turnstile, 2);
        Writer w3 = new Writer(noReaders, ll, turnstile, 3);
        
        
        r1.start();
        r2.start();
        r3.start();
        
        w1.start();
        w2.start();
        w3.start();

        try{
            r1.join();
            r2.join(); 
            r3.join();

            w1.join();
            w2.join();
            w3.join();

        }
        catch(InterruptedException e){}
    }    
}






class Reader extends Thread{
    private LinkedList<Integer> ll; 
    private Semaphore mutex; 
    private Semaphore noReaders; 
    private Semaphore turnstile; 
    private static volatile int currentReaders = 0; 
    private int ID; 

    /**
     * 
     * @param sem           Shared Semaphore
     * @param noReaders
     * @param ll            Shared Linked List 
    */
    public Reader(Semaphore mutex, Semaphore noReaders, LinkedList<Integer> ll, Semaphore turnstile, int ID){ 
        this.mutex = mutex;
        this.noReaders = noReaders;
        this.ll = ll;
        this.turnstile = turnstile;
        this.ID = ID;
    }


    public void run(){

        for(;;){
            
            try {
                turnstile.acquire();
                turnstile.release();

                //first in locks
                mutex.acquire();
                    ++currentReaders; 
                    if(currentReaders == 1) {
                        noReaders.acquire();
                        System.out.printf("%d is first!\n", ID);
                    }
                mutex.release(); 

                //print out from reading the list
                if(ll.size() == 0) System.out.printf("Reader No.%d reads from an empty list\n", ID);
                else System.out.printf("Reader No.%d reads the list, it's latest element is: %d\n", ID,ll.getLast()); 


                //last out releases
                mutex.acquire();
                    --currentReaders; 
                    if(currentReaders == 0) {
                        System.out.printf("%d is last!\n", ID);
                        noReaders.release(); 
                    }        
                mutex.release(); 
                
            } 
            catch (InterruptedException e) {}
            
            // attempt to read again after 1.5 seconds.
            try {Thread.sleep(1500);} 
            catch (InterruptedException e) {}
        }
    }

}








class Writer extends Thread{
    private LinkedList<Integer> ll; 
    private Semaphore noReaders; 
    private Semaphore turnstile; 
    private int ID; 
 

    static final int MAX_LIST_SIZE = 10; 
    static final int MIN_RAND = 5; 
    static final int MAX_RAND = 500; 


    public Writer(Semaphore noReaders, LinkedList<Integer> ll, Semaphore turnstile,int ID){
        this.noReaders = noReaders; 
        this.ll = ll; 
        this.turnstile = turnstile; 
        this.ID = ID;
    }

    public void run(){ 
        for(;;){
            try {
                turnstile.acquire();
                int newEntry = MIN_RAND + (int)(Math.random()*MAX_RAND-MIN_RAND+1); 
                noReaders.acquire();
                
                if(ll.size() ==MAX_LIST_SIZE) ll.clear(); 
                else ll.add(newEntry); 
                
                printLL();
            } 
            catch (InterruptedException e) {} //heh.
            finally{
                turnstile.release();
                noReaders.release();
            }
        
            // Look to update the list every .5 seconds
            try {Thread.sleep(500);} 
            catch (InterruptedException e) {}
        }
    } 

    public void printLL(){
        System.out.printf("Writer No.%d has overwritten the Linked-List, the current values are: [ ", ID);
    
        for(Integer elem : ll){
            System.out.printf("%d ", elem);
        }
        System.out.println("]"); 

    }

}