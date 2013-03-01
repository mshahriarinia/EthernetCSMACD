

package simulation.bus;


public class My_AtomicBoolean {
  
  boolean value;
  
  public My_AtomicBoolean() {
  value = false;
  }
  
  synchronized public boolean get(){
    return value;
  }
  
  synchronized public void set(boolean val){
    value = val;
  }
  
  synchronized public String toString(){
    return value + "";
  }
}
