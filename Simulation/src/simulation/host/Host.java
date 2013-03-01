package simulation.host;

import java.util.concurrent.atomic.AtomicInteger;
import simulation.bus.BUS;
import simulation.bus.Bit;
import simulation.bus.Address;
import java.util.Random;
import java.util.Vector;
import java.util.zip.CRC32;
import static simulation.host.HostConstants.*;
import static simulation.bus.BitWise.get_int_ith_byte;
//implementing an interface just for its constants

public class Host
  implements Runnable{
  
  public static double MESSAGE_CREATION_PROBABILITY = 0.3;
  public static double PERSISTENCY = 0.5;
  public static int MAX_SENT_MESSAGES_NO = 11;
  public static int MAX_HOSTS = 2;
  
  public final BUS bus;
  public final int ID;
  public final Address address;
  private final Sender sender;
  public final Receiver receiver;
  private final boolean send;
  public boolean sendInProgress;
  public boolean acked;
  private int sendingMessageID;
  private Random random = new Random();
  private static CRC32 crc = new CRC32();
  
  private int exponentCounter;
  
  public static AtomicInteger count_TotalSent = new AtomicInteger();
  public static AtomicInteger count_successfullSent = new AtomicInteger();
  public static AtomicInteger count_successfullReceit = new AtomicInteger();
  public static AtomicInteger count_messageID = new AtomicInteger();
  
  private Vector<byte[]> received_Buffer = new Vector<byte[]>();
  
  public Host(int ID, BUS b, Address a){
    this(ID, b, a, true, true, true);
  }
  
  public Host(int ID, BUS b, Address a, boolean send, boolean carrierSense, boolean receive) {
    this.ID = ID;
    bus = b;
    address = a;
    this.send = send;
    if(send)
      sender = new Sender(bus, address, carrierSense, ID);
    else
      sender = null;
    receiver = new Receiver(this, receive);
  }
  
  public void run() {
    try {
      if(send)
        startSending();
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
    if(deeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeebug)
      bus.writeln("Host(" + ID + ") sender terminated.");
  }
  
  private void startSending() throws InterruptedException{
    while(count_successfullSent.get() < MAX_SENT_MESSAGES_NO){
      wait_until_timeForANewMessage();
      sendNewMessage();
      synchronized(this){
        this.wait(DELAY_TIME_FOR_RECOVEY_FROM_SEND);
      }
    }
  }
  
  private void wait_until_timeForANewMessage() throws InterruptedException{
    while ( !timeForANewMessage())
      synchronized(this){
      this.wait(DELAY_TIME_FOR_A_NEW_MESSAGE);
      }
  }
  
  private boolean timeForANewMessage(){
    return Math.random() < MESSAGE_CREATION_PROBABILITY;
  }
  
  private void sendNewMessage() throws InterruptedException{
    exponentCounter = 0;
    byte[] messageArr = newMessage();
    if(deeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeebug)
      print__Message(messageArr, true);
    count_TotalSent.incrementAndGet();
    do{
      boolean sendSuccess = false;
      do{
        wait_Until_line_Is_Free_with_Persistency();
        sendInProgress = true;
        sendSuccess = sender.send(messageArr, sendingMessageID);
        sendInProgress = false;
        if(sendSuccess)
          continue;
        if(deeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeebug){
          bus.writeln("(" + ID + ")~S~");
        }
        waitExponentialBackOff();
      }while(!sendSuccess);
      print__sendSuccess(sendingMessageID);
      for(int i = 0; i < HostConstants.BITS_NUM_COLLISION_WAIT && ! acked; i++){
        synchronized(bus.setSignal){
          bus.setSignal.wait();
        }
      }
    }while( ! acked);
    acked = false;
    count_successfullSent.incrementAndGet();
    if(deeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeebug)
      print__Debug__SendNewMessageSuccess(messageArr);
    if(deeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeebug)
      if(count_successfullSent.get() % 10 == 0)
        print__Debug__successModulo10();
  }
  
  private void wait_Until_line_Is_Free_with_Persistency() throws InterruptedException{
    double rand = random.nextDouble();
    if(rand <= PERSISTENCY)
      wait_FREE_LINE_DETECTION_BITS_NUM();
    else{
      if(deeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeebug)
        bus.write("persistency Exp. ");
      waitExponentialBackOff();
    }
  }
  
  private void wait_FREE_LINE_DETECTION_BITS_NUM() throws InterruptedException{
    int counter = 0;
    while(counter < HostConstants.BITS_NUM_FREE_LINE_DETECTION){
      byte checkBit = bus.getBit(address).bit;
      if(checkBit != Bit.FREELINE)
        counter = 0;
      counter++;
      synchronized (bus.setSignal){
        bus.setSignal.wait();
      }
    }
  }
  
  private void waitExponentialBackOff() throws InterruptedException{
    exponentCounter++;
    if(exponentCounter > 16)
      error();
    int powered = 1;
    powered <<= exponentCounter;
    int rand = 0;
    rand = random.nextInt(powered) + 1;
    if(deeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeebug)
      bus.writeln("Host(" + ID + ") Exp = " + exponentCounter + " rand = " + rand);
    for(int i = 0; i < rand * BITS_NUM_COLLISION_WAIT; i++)
      synchronized(bus.setSignal){
      bus.setSignal.wait();
      }
    if(deeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeebug)
      bus.writeln("Host(" + ID + ") Exp = " + exponentCounter + " rand = " + rand + " OUT.");
  }
  
  private void error() throws InterruptedException{
    bus.writeln("******** Exponent cycle finished.\n Error!\t\t Host(" +ID +
      ") is halted.*********");
    synchronized(this){
      this.wait();
    }
  }
  
  private byte[] newMessage(){
    sendingMessageID = count_messageID.getAndIncrement();
    byte[] message = randomMessage();
    int destinationID = randomID();
    //addPreamble
    message[0] = message[message.length - 1] = PREAMBLE[0];
    //add DestID
    message[1] = get_int_ith_byte(destinationID, 3);
    message[2] = get_int_ith_byte(destinationID, 2);
    message[3] = get_int_ith_byte(destinationID, 1);
    message[4] = get_int_ith_byte(destinationID, 0);
    //add SelfID
    message[5] = get_int_ith_byte(ID, 3);
    message[6] = get_int_ith_byte(ID, 2);
    message[7] = get_int_ith_byte(ID, 1);
    message[8] = get_int_ith_byte(ID, 0);
    //message ID
    message[9] = get_int_ith_byte(sendingMessageID, 3);
    message[10] = get_int_ith_byte(sendingMessageID, 2);
    message[11] = get_int_ith_byte(sendingMessageID, 1);
    message[12] = get_int_ith_byte(sendingMessageID, 0);
    //add dataLength
    int length = message.length - OVERHEAD_LENGTH;
    message[13] = get_int_ith_byte(length, 1);
    message[14] = get_int_ith_byte(length, 0);
    //add CRC
    int crc = getCRC(message, 1, message.length -
      (2 * HostConstants.PREAMBLE.length + HostConstants.CRC_POLINOMIAL_BYTES_LENGTH));
    message[message.length - PREAMBLE.length - CRC_POLINOMIAL_BYTES_LENGTH] = get_int_ith_byte(crc, 3);
    message[message.length - PREAMBLE.length - CRC_POLINOMIAL_BYTES_LENGTH + 1] = get_int_ith_byte(crc, 2);
    message[message.length - PREAMBLE.length - CRC_POLINOMIAL_BYTES_LENGTH + 2] = get_int_ith_byte(crc, 1);
    message[message.length - PREAMBLE.length - CRC_POLINOMIAL_BYTES_LENGTH + 3] = get_int_ith_byte(crc, 0);
    print__newFrame(sendingMessageID, ID, destinationID, message.length);
    return message;
  }
  
  public void print__Message(byte[] message, boolean sent){
    simulation.NetSim.bT.yield();
    String banner;
    if(sent)
      banner= "#######################################################################";
    else
      banner = "-----------------------WWWWWWWWWWWWWWWW----------------------------";
    bus.writeln(banner);
    for(int i = 0; i < message.length; i++){
      bus.showByte(message[i]);
      bus.write("_");
    }
    bus.writeln("");
    if(sent)
      print__Debug__SendNewMessageStart(message);
    bus.writeln(banner);
    synchronized(simulation.NetSim.bT){
      simulation.NetSim.bT.notify();
    }
  }
  
  private int randomID(){
    int temp;
    while(ID == (temp = random.nextInt(MAX_HOSTS)))
      ;
    return temp;
  }
  
  private byte[] randomMessage(){
    int randomLength = 0;
    while(randomLength == 0)
      randomLength = random.nextInt(MAX_DATA_BYTES_LENGTH);
    byte[] b = new byte[OVERHEAD_LENGTH + randomLength];
    random.nextBytes(b);
    return b;
  }
  
  public static int getCRC(byte[] b){
    return getCRC(b, 0, b.length);
  }
  
  public static int getCRC(byte[] b, int offset, int length){
    crc.reset();
    crc.update(b, offset, length);
    return (int)crc.getValue();
  }
  
  
  
  public String toString(){
    return "Host(" + ID + ") in BUS(" + bus.ID + ") in " + address;
  }
  
  private int getDestinationID(byte[] message){
    int temp = message[1];
    temp <<= 8;
    temp += message[2];
    temp <<= 8;
    temp += message[3];
    temp <<= 8;
    temp += message[4];
    return temp;
  }
  
  private int getMessageID(byte[] message){
    int temp = message[8];
    temp <<= 8;
    temp += message[9];
    temp <<= 8;
    temp += message[10];
    temp <<= 8;
    temp += message[11];
    return temp;
  }
  
  public void receivedMessage(byte[] messageArr){
    received_Buffer.add(messageArr);
    count_successfullReceit.incrementAndGet();
    if(deeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeebug)
      bus.writeln("_________________________________  Received messageID(" +
        getMessageID(messageArr) + ") _______________________________________");
  }
  
  private void print__Debug__SendNewMessageStart(byte[] messageArr){
    bus.writeln("Message(" + count_TotalSent + ") created from" + ID +
      " to " + getDestinationID(messageArr) + " length = " + messageArr.length);
  }
  
  private void print__Debug__SendNewMessageSuccess(byte[] messageArr){
    bus.writeln("Host(" + ID + ") to (" + getDestinationID(messageArr) +
      ") success. " + count_successfullSent + " of " + count_TotalSent);
  }
  
  private void print__Debug__successModulo10(){
    bus.writeln("****** successed Modulo 10: " + count_successfullSent + " at " +
      bus.getTime() + "******");
  }
  
  private void print__newFrame(int messageID, int IDsender, int IDreceiver, int length){
    bus.writeln("F " + messageID + " " + IDsender + " " + IDreceiver + " " + length +
      " " + bus.getTime());
  }
  
  private void print__sendSuccess(int messageID){
    bus.writeln("S " + messageID + " " + bus.getTime());
  }  
}
