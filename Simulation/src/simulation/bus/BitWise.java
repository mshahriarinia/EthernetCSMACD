

package simulation.bus;

public class BitWise {
  
public static byte get_int_ith_bit(int num, int index){
    try{
      if(index < 0 || index > 31)
        throw new Exception();
    }catch(Exception e){
      e.printStackTrace();
    }
    int mask = 1;
    mask <<= index;
    mask &= num;
    mask >>= index;
    return (byte)mask;
  }

public static byte get_int_ith_byte(int num, int ith_byte){
    int mask = 0xFF;
    mask <<= (ith_byte * 8);
    return (byte)((num & mask) >> (ith_byte * 8));
  }

public static int get_int_unsignedOR_byte(int a, byte b){
    int mask = 0x000000FF;
    int temp = b & mask;
    return temp | a;
  }
  
}
