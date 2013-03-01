package simulation.host;

public interface HostConstants {
  
  public static final boolean deeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeebug = !false;
  
  static final int BIT_RATE_10 = 10 * 1000 * 1000;
  
  static final int BITS_NUM_COLLISION_WAIT = 51;//512;
  static final int BITS_NUM_FREE_LINE_DETECTION = 10;//BITS_NUM_COLLISION_WAIT;
  static final long DELAY_TIME_FOR_RECOVEY_FROM_SEND = 20;
  static final long DELAY_LOW = 2;
  static final long DELAY_TIME_FOR_A_NEW_MESSAGE = 3;
  
  static final int MAXIMUM_NANO_WAIT = 999999;
  
  static enum frameStates  {state_preambleHead, state_receiverID, state_senderID,
  state_messageID, state_dataLength, state_data, state_crc, state_preambleTail};
  
  static final byte[] PREAMBLE = {(byte)0xAB};
  static final byte CRC_POLINOMIAL_BYTES_LENGTH = 4;
  static final byte ID_LENGTH = Integer.SIZE / 8;
  static final byte MAX_DATA_BYTES_LENGTH = 2;
  static final byte MAX_DATA_LENGTH_COUNTER_LENGTH = 2;
  static final int OVERHEAD_LENGTH = 2 * PREAMBLE.length + 3 * ID_LENGTH +
    MAX_DATA_LENGTH_COUNTER_LENGTH + CRC_POLINOMIAL_BYTES_LENGTH; // = 20
}
