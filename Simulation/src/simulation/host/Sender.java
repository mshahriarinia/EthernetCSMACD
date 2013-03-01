package simulation.host;

import simulation.bus.BUS;
import simulation.bus.BitWise;
import simulation.bus.Message;
import simulation.bus.Bit;
import simulation.bus.Address;
import static simulation.bus.BitWise.get_int_ith_bit;
//import java.util.concurrent.atomic.AtomicInteger;

class Sender {
  
  private BUS bus;
  private Address address;
  private int ID;
  
  private Message mL;
  private Message mR;
  private static boolean carrierSense;
  public boolean collided;
  private boolean send;
  
  private boolean firstBitAdded;
  
  public Sender(BUS b, Address a, int ID) {
    address = a;
    bus = b;
    this.carrierSense = carrierSense;
    this.ID = ID;
  }
  
  public Sender(BUS b, Address a, boolean carrierSense, int ID) {
    this(b, a, ID);
    this.carrierSense = carrierSense;
  }
  
  public boolean send(byte[] messageArr, int messageID) throws InterruptedException {
    newMessageInitialize();
    synchronized(bus.newRun){
      while(bus.newRun.get())
        bus.newRun.wait();
      bus.count_sendingHosts.incrementAndGet();
    }
    int bitNum = 0;
    for (int indexByte = 0; (indexByte < messageArr.length) && !collided; indexByte++) {
      for(int indexBit = 0; indexBit < Byte.SIZE && ! collided; indexBit++){
        bitNum++;
        Bit b = addBitToMessages(messageArr, indexByte, indexBit);
        if( ! firstBitAdded){
          bus.addMessage(mL);
          bus.addMessage(mR);
          firstBitAdded = true;
        }
        synchronized(bus.setSignal){
          bus.count_waitingSetHosts.incrementAndGet();
          bus.setSignal.wait();
        }
        if(carrierSense)
          collided = !checkMediumBit(b);
        bus.count_waitingSetHosts.decrementAndGet();
        synchronized(bus.shiftSignal){
          bus.count_waitingShiftHosts.incrementAndGet();
          bus.shiftSignal.wait();
        }
        bus.count_waitingShiftHosts.decrementAndGet();
      }
    }
    bus.count_sendingHosts.decrementAndGet();
    if(collided){
      return false;
    }
    return true;
  }
  
  private void newMessageInitialize(){
    firstBitAdded = false;
    collided = false;
    synchronized(Message.count){
      mL = new Message(bus, Message.LEFT);
      mR = new Message(bus, Message.RIGHT);
    }
  }
  
  private Bit addBitToMessages(byte[] messageArr, int byteIndex, int bitIndex){
    int bitVal = Bit.codeTheBit(BitWise.get_int_ith_bit(messageArr[byteIndex], bitIndex));
    Bit bL;
    Bit bR;
    bL = new Bit(bitVal, new Address(address));
    bR = new Bit(bitVal, new Address(address));
    mL.addBit(bL);
    mR.addBit(bR);
    return bL;
  }
  
  private boolean checkMediumBit(Bit b){
    boolean t = bus.getBit(address).isSameAs(b);
    return t;
  }
  
  private void print__Collision(int messageID){
    bus.writeln("C " + messageID + " " + bus.getTime());
  }
}