package simulation.host;

import simulation.bus.BUS;
import simulation.bus.Bit;
import simulation.bus.Address;
import simulation.NetSim;

import static simulation.host.HostConstants.frameStates;
import static simulation.bus.BitWise.*;

public class Receiver{
  
  private boolean receive;
  public final Host host;
  
  frameStates state;
  int numOfValidBitsInByteBuffer;
  byte byteBuffer;
  int byteCounter;
  
  int receiverID;
  int senderID;
  int messageID;
  int dataLength;
  byte[] dataArr;
  int crc;
  
  public Receiver(Host h) {
    this(h, true);
  }
  
  public Receiver(Host h, boolean receive) {
    initialize();
    host = h;
    this.receive = receive;
  }
  
  private void initialize(){
    state = frameStates.state_preambleHead;
    numOfValidBitsInByteBuffer = 0;
    byteBuffer = 0;
    receiverID = 0;
    senderID = 0;
    messageID = 0;
    dataLength = 0;
    dataArr = null;
    crc = 0;
    byteCounter = 0;
  }
  
  public void receive(Bit b){
    int newBit = 0;
    switch(b.bit){
      case Bit.FREELINE :
        return;
      case Bit.COLLISION :
        initialize();
        return;
      case Bit.ONE :
        newBit = 1;
        break;
      case Bit.ZERO :
        newBit = 0;
        break;
    }
    updateByteBuffer(newBit);
    if(numOfValidBitsInByteBuffer == 8)
      addByte(byteBuffer);
    
  }
  
  private void updateByteBuffer(int newBit){
    byteBuffer = addBit(byteBuffer, newBit);
    numOfValidBitsInByteBuffer++;
  }
  
  private byte addBit(byte temp, int newBit){
    newBit <<= numOfValidBitsInByteBuffer;
    temp = (byte) (temp | newBit);
    return temp;
  }
  
  //enum switfch must be unqualified name
  private void addByte(byte newByte){
    byteCounter++;
    switch (state){
      case state_preambleHead:
        state_preambleHead(newByte);
        break;
      case state_receiverID:
        state_receiverID(newByte);
        break;
      case state_senderID:
        state_senderID(newByte);
        break;
      case state_messageID:
        state_messageID(newByte);
        break;
      case state_dataLength:
        state_dataLength(newByte);
        break;
      case state_data:
        state_data(newByte);
        break;
      case state_crc:
        state_crc(newByte);
        break;
      case state_preambleTail:
        state_preambleTail(newByte);
        break;
    }
    numOfValidBitsInByteBuffer = 0;
    byteBuffer = 0;
  }
  
  
  private void state_preambleHead(byte newByte){
    if(getPreamble(newByte))
      state = frameStates.state_receiverID;
    else
      initialize();
  }
  
  private void state_receiverID(byte newByte){
    if(getReceiverID(newByte))
      state = frameStates.state_senderID;
  }
  
  private void state_senderID(byte newByte){
    if(getSenderID(newByte))
      state = frameStates.state_messageID;
  }
  
  private void state_messageID(byte newByte){
    if(getMessageID(newByte))
      state = frameStates.state_dataLength;
  }
  
  private void state_dataLength(byte newByte){
    if(getDataLength(newByte)){
      if(dataLength > 0){
        state = frameStates.state_data;
        dataArr = new byte[3 * HostConstants.ID_LENGTH +
          HostConstants.MAX_DATA_LENGTH_COUNTER_LENGTH + dataLength];
        setDataArr_TO_crcArr();
      }else
        initialize();
    }
  }
  
  private void state_data(byte newByte){
    if(getData(newByte))
      state = frameStates.state_crc;
  }
  
  private void state_crc(byte newByte){
    if(getCRC(newByte)){
      int realCRC = host.getCRC(dataArr);
      if(crc == realCRC)
        state = frameStates.state_preambleTail;
    }
  }
  
  private void state_preambleTail(byte newByte){
    if(getPreamble(newByte)){
      host.receivedMessage(dataArr);
      if(HostConstants.deeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeebug)
        host.print__Message(dataArr, false);
    }
    initialize();
  }
  
  private boolean getPreamble(byte newByte){
    return (newByte == HostConstants.PREAMBLE[0])? true : false;
  }
  
  private boolean getReceiverID(byte newByte){
    receiverID <<= 8;
    receiverID = get_int_unsignedOR_byte(receiverID, newByte);
    if(byteCounter == HostConstants.PREAMBLE.length + HostConstants.ID_LENGTH)
      return true;
    else
      return false;
  }
  
  private boolean getSenderID(byte newByte){
    senderID <<= 8;
    senderID = get_int_unsignedOR_byte(senderID, newByte);
    if(byteCounter == HostConstants.PREAMBLE.length + 2 * HostConstants.ID_LENGTH)
      return true;
    else
      return false;
  }
  
  private boolean getMessageID(byte newByte){
    messageID <<= 8;
    messageID = get_int_unsignedOR_byte(messageID, newByte);
    if(byteCounter == HostConstants.PREAMBLE.length + 3 * HostConstants.ID_LENGTH)
      return true;
    else
      return false;
  }
  
  private boolean getDataLength(byte newByte){
    dataLength <<= 8;
    dataLength = get_int_unsignedOR_byte(dataLength, newByte);
    if(byteCounter == HostConstants.PREAMBLE.length + 3 * HostConstants.ID_LENGTH +
      HostConstants.MAX_DATA_LENGTH_COUNTER_LENGTH)
      return true;
    else
      return false;
  }
  
  private boolean getData(byte newByte){
    int index = byteCounter - HostConstants.PREAMBLE.length - 1;
    dataArr[index] = newByte;
    if(byteCounter == HostConstants.PREAMBLE.length + 3 * HostConstants.ID_LENGTH +
      HostConstants.MAX_DATA_LENGTH_COUNTER_LENGTH + dataLength)
      return true;
    else
      return false;
  }
  
  private boolean getCRC(byte newByte){
    crc <<= 8;
    crc = get_int_unsignedOR_byte(crc, newByte);
    if(byteCounter == HostConstants.PREAMBLE.length + 3 * HostConstants.ID_LENGTH +
      HostConstants.MAX_DATA_LENGTH_COUNTER_LENGTH + dataLength +
      HostConstants.CRC_POLINOMIAL_BYTES_LENGTH)
      return true;
    else
      return false;
  }
  
  private void setDataArr_TO_crcArr(){
    dataArr[0] = get_int_ith_byte(receiverID, 3);
    dataArr[1] = get_int_ith_byte(receiverID, 2);
    dataArr[2] = get_int_ith_byte(receiverID, 1);
    dataArr[3] = get_int_ith_byte(receiverID, 0);
    dataArr[4] = get_int_ith_byte(senderID, 3);
    dataArr[5] = get_int_ith_byte(senderID, 2);
    dataArr[6] = get_int_ith_byte(senderID, 1);
    dataArr[7] = get_int_ith_byte(senderID, 0);
    dataArr[8] = get_int_ith_byte(messageID, 3);
    dataArr[9] = get_int_ith_byte(messageID, 2);
    dataArr[10] = get_int_ith_byte(messageID, 1);
    dataArr[11] = get_int_ith_byte(messageID, 0);
    dataArr[12] = get_int_ith_byte(dataLength, 1);
    dataArr[13] = get_int_ith_byte(dataLength, 0);
  }
}