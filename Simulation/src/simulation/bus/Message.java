package simulation.bus;

import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.atomic.My_AtomicInteger;

public class Message {
  
  public static final int RIGHT = 1;
  public static final int LEFT = 0;
  public static AtomicInteger count = new AtomicInteger();
  
  
  private final BUS bus;
  private Vector<Bit> data;
  private int direction;
  
  public Message(BUS b, int direction) {
    synchronized(Message.count){
      count.incrementAndGet();
      bus = b;
      data = new Vector<Bit>();
      this.direction = direction;
    }
  }
  
  public void addBit(Bit m) {
    data.add(m);
  }
  
  public int size(){
    return data.size();
  }
  
  public void shift(){
    switch(direction){
      case(LEFT):
        shiftLeft();
        break;
      case(RIGHT):
        shiftRight();
        break;
    }
  }
  
  private void shiftRight(){
    Address a;
    boolean removedBit = false;
    int indexRemovedBit = 0;
    for(int i = 0; i < data.size(); i++){
      a = data.get(i).address;
      if(a.busIndex < bus.NUM_BUS_WORDS - 1){
        if(a.bitIndex > 0){
          a.bitIndex--;
        } else{
          a.busIndex++;
          a.bitIndex = bus.BITS_IN_WORD - 1;
        }
      } else if(a.busIndex == bus.NUM_BUS_WORDS - 1){
        if(a.bitIndex > bus.BITS_IN_WORD - bus.NUM_OF_VALID_BITS_IN_LAST_WORD + 1)
          a.bitIndex--;
        else{
          removedBit = true;
          indexRemovedBit = i;
        }
      }
    }
    if(removedBit)
      data.remove(indexRemovedBit);
  }
  
  private void shiftLeft(){
    Address a;
    int indexRemovedBit = 0;
    boolean removedBit = false;
    for(int i = 0; i < data.size(); i++){
      a = data.get(i).address;
      if(a.bitIndex < bus.BITS_IN_WORD - 1){
        a.bitIndex++;
      } else if(a.busIndex > 0 ) {
        a.busIndex--;
        a.bitIndex = 0;
      }else{
        removedBit = true;
        indexRemovedBit = i;
      }
    }
    if(removedBit)
      data.remove(indexRemovedBit);
  }
  
  public void setBus(){
    for(int i = 0; i < data.size(); i++)
      bus.setBit(data.get(i));
  }
  
  public String toString(){
    String s = "";
    for(int i = 0; i < data.size(); i++)
      s += "[" + data.get(i).address.busIndex + ", " +
        data.get(i).address.bitIndex + "](" +
        data.get(i).bit + ") ";
    return s;
  }
}
