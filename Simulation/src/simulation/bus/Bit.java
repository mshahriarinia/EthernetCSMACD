package simulation.bus;

public class Bit{
  
  public static final int CODE_BITS = 2;
  public static final int FREELINE = 0;
  public static final int ZERO = 1;
  public static final int ONE = 2;
  public static final int COLLISION = 3;
  
  public final byte bit;
  public Address address;
  
  public Bit(int bit, Address a) {
    this.bit = (byte)bit;
    address = a;
  }
  
  public String toString(){
    return "Bit value = " + Long.toBinaryString(bit) +
      " in " + address;
  }
  
  public boolean isSameAs(Bit b){
    if(b.address.isSameAs(address) && (b.bit == bit))
      return true;
    return false;
  }
  
  public static byte codeTheBit(byte bit){
    byte b = 0;
    switch (bit) {
      case 0:
        b = Bit.ZERO;
        break;
      case 1:
        b = Bit.ONE;
        break;
    }
    return b;
  }
  
  public boolean isCollided(){
    if(bit == COLLISION)
      return true;
    return false;
  }
}
