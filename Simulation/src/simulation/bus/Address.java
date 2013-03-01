package simulation.bus;

public class Address {
  
  public int busIndex;
  public int bitIndex;
  
  public Address(int busIndex, int bitIndex){
    this.busIndex = busIndex;
    this.bitIndex = bitIndex;
  }
  
  public Address(Address a){
    busIndex = a.busIndex;
    bitIndex = a.bitIndex;
  }
  
  public String toString(){
    return "Address: bus index(" + busIndex +
      ") & bit index(" + bitIndex + ")";
  }
  
  public boolean isSameAs(Address a){
    if((a.bitIndex == bitIndex) && (a.busIndex == busIndex))
      return true;
    return false;
  }
}
