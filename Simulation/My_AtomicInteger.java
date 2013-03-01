
package simulation.bus;


public class My_AtomicInteger {
  
  int value;
 
  public My_AtomicInteger(int val) {
    value = val;
  }
  
  public My_AtomicInteger(){
    value = 0;
  }
  
  synchronized public int addAndGet(int val){
    return value += val;
  }
  
  synchronized public int incrementAndGet(){
    return ++value;
  }
  
  synchronized public int getAndIncrement(){
    return value++;
  }
  
  synchronized public int decrementAndGet(){
    return --value;
  }
  
  synchronized public int get(){
    return value;
  }
  
  synchronized public String toString(){
    return value + "";
  }
}
