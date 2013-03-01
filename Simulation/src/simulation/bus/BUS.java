package simulation.bus;

import simulation.host.Receiver;
import java.util.Random;
import java.io.*;
import simulation.host.HostConstants;
import simulation.host.Host;
import java.util.GregorianCalendar ;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import static simulation.bus.BitWise.get_int_ith_bit;

public class BUS
  implements Runnable {
  
  public final int ID;
  public final int NUM_BUS_WORDS;
  public final int NUM_OF_VALID_BITS_IN_LAST_WORD;
  
  public Object setSignal = new Object();
  public Object shiftSignal = new Object();
  public static AtomicInteger count_sendingHosts = new AtomicInteger();
  public static AtomicInteger count_waitingSetHosts = new AtomicInteger();
  public static AtomicInteger count_waitingShiftHosts = new AtomicInteger();
  public AtomicBoolean newRun = new AtomicBoolean();
  
  private AtomicLong clock = new AtomicLong();
  private static final int SPEED = 2 * 1000 * 1000 * 100;
  public static final int BITS_IN_WORD = Byte.SIZE;
  private final int MAX_MEDIUM_BITS;//max non-overlapping bits in medium
  private final long BIT_TIME_milliSec;
  private final int BIT_TIME_nanoSec;
  private final byte[][] medium;
  private Vector<Message> messageBox = new Vector<Message>();
  private Vector<Receiver> receiversBox = new Vector<Receiver>();
  private static Random random;
  private FileWriter fw;
  
  public BUS(int ID, int length_In_Meters, int bitRate) {
    this.ID = ID;
    double BIT_TIME = 1.0 / bitRate;
    BIT_TIME_milliSec = Math.round(BIT_TIME * 1000);
    int temp = (int)Math.round(BIT_TIME * 1000 * 1000 * 1000);
    BIT_TIME_nanoSec = (temp > HostConstants.MAXIMUM_NANO_WAIT || temp < 0) ?
      HostConstants.MAXIMUM_NANO_WAIT : temp;
    double bit_Length_In_Meters = SPEED * BIT_TIME;
    MAX_MEDIUM_BITS = (int)Math.round(Math.ceil(length_In_Meters / bit_Length_In_Meters));
    NUM_BUS_WORDS = MAX_MEDIUM_BITS / BITS_IN_WORD + 1;
    medium = new byte[Bit.CODE_BITS][NUM_BUS_WORDS];
    NUM_OF_VALID_BITS_IN_LAST_WORD = MAX_MEDIUM_BITS % BITS_IN_WORD;
    random = new Random();
    try {
      fw = new FileWriter(new File("c:/my_NetSim_DEBUG.txt"));
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
  
  public void run(){
    while ( ! terminationCondition()) {
      try {
        trigger();
//        showMedium(0, 15);
        waitBusCyclePeriod();
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    }
  }
  
  public boolean terminationCondition(){
    return ! (Host.count_successfullSent.get() < Host.MAX_SENT_MESSAGES_NO ||
      ! isFree());
  }
  
  private void trigger() throws InterruptedException{
    clock.incrementAndGet();
    setMedium_FREELINE();
    newRun.set(true);
    while(count_sendingHosts.get() != count_waitingSetHosts.get())
      offerExecution();
    setAllMessages();
    allHostsReceive();
    notifyAll(setSignal);
    while(count_sendingHosts.get() != count_waitingShiftHosts.get())
      offerExecution();
    shiftAllMessages();
    notifyAll(shiftSignal);
    newRun.set(false);
    notifyAll(newRun);
  }
  
  private void offerExecution() throws InterruptedException{
    synchronized(this){
      this.wait(0, 1);
    };
  }
  
  private void notifyAll(Object o){
    synchronized(o){
      o.notifyAll();
    }
  }
  
  private void waitBusCyclePeriod() throws InterruptedException{
    synchronized(this){
      this.wait(BIT_TIME_milliSec, BIT_TIME_nanoSec);
    }
  }
  
  private void shiftAllMessages() {
    synchronized(messageBox){
      int i = 0;
      while(i < messageBox.size()){
        messageBox.get(i).shift();
        if(messageBox.get(i).size() != 0)
          i++;
        else
          messageBox.remove(i);
      }
    }
  }
  
  private void setAllMessages(){
    synchronized(messageBox){
      for(int i = 0; i < messageBox.size(); i++){
        messageBox.get(i).setBus();
//        writeln(messageBox.get(i).toString());
      }
    }
  }
  
  private void setMedium_FREELINE(){
    for(int i = 0; i < Bit.CODE_BITS; i++)
      for(int j = 0; j < NUM_BUS_WORDS; j++)
        medium[i][j] = Bit.FREELINE;
  }
  
  public void setBit(Bit b){
    for(int i = 0; i < Bit.CODE_BITS; i++){
      medium[i][b.address.busIndex] |= (get_int_ith_bit(b.bit, i) << b.address.bitIndex);
    }
  }
  
  
  
  public Bit getBit(Address a){
    byte b = 0;
    for(int i = Bit.CODE_BITS - 1; i >= 0; i--){
      b <<= 1;
      b += get_int_ith_bit(medium[i][a.busIndex], a.bitIndex);
    }
    return new Bit(b, a);
  }
  
  public Address randomAddress(){
    return new Address(random.nextInt(NUM_BUS_WORDS),
      random.nextInt(BITS_IN_WORD));
  }
  
  public String toString(){
    return "BUS(" + ID + "): Speed = " + SPEED +
      " ,bit rate = " + (1000 * BIT_TIME_milliSec +
      1000 * 1000 * 1000 * BIT_TIME_nanoSec) + " ,NUM_BUS_WORDS = " + NUM_BUS_WORDS;
  }
  
  public void writeln(String s){
    write(s + '\r' + '\n');
  }
  
  public void write(String s){
    boolean tofile = !true;
    if(!tofile)
      System.out.print(s);
    else
      try {
        fw.write(s);
        fw.flush();
      } catch (Exception e) {
      }
  }
  
  public static String getSystemTime(){
    GregorianCalendar d = new GregorianCalendar();
    return d.get(d.HOUR) + ":" + d.get(d.MINUTE) +
      ":" + d.get(d.SECOND) +":" + d.get(d.MILLISECOND);
  }
  
  public String getTime(){
    return clock.get() + "";
  }
  
  public void addMessage(Message m){
    synchronized(messageBox){
      messageBox.add(m);
    }
  }
  
  public void showMedium(int first, int last){
    for(int j = 0; j < Bit.CODE_BITS; j++){
      for(int i = first; i <= last; i++){
        showByte(medium[j][i]);
        write(",");
      }
      writeln("");
    }
    writeln("");
    writeln("");
  }
  
  public void showByte(byte b){
    for(int k = 7; k >= 0; k--)
      write(get_int_ith_bit(b, k) + "");
  }
  
  synchronized public void addReceiver(Receiver r){
    receiversBox.add(r);
  }
  
  public boolean isFree(){
    return (messageBox.size() == 0);
  }
  
  private void allHostsReceive(){
    Receiver r;
    for(int i = 0; i < receiversBox.size(); i++){
      r = receiversBox.get(i);
      if(!r.host.sendInProgress){
        Address a = r.host.address;
        Bit b = getBit(a);
        r.receive(b);
      }
    }
  }
}