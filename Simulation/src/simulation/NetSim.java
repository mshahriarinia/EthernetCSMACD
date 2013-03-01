package simulation;
//
//import javax.swing.UIManager;
//import java.awt.*;
//import simulation.frames.MainFrame;
import simulation.host.Host;
import simulation.bus.BUS;
import simulation.bus.Address;
import simulation.host.HostConstants;

public class NetSim {
  
  public static Thread bT;
//  boolean packFrame = false;
  
  
  public static void main(String[] args) {
    int maxHosts = 3;
    boolean exponentialBackOff = true;
    double messageCreationProbability = 0.3;
    double persistency = 0.5;
    new NetSim(maxHosts, exponentialBackOff, messageCreationProbability, persistency);
//    InitializeFrame();
  }
  
  public NetSim(int maxHosts, boolean exponentialBackOff, double messageCreationProbability,
    double persistency) {
    Host.MAX_HOSTS = maxHosts;
    Host.MESSAGE_CREATION_PROBABILITY = messageCreationProbability;
    Host.PERSISTENCY = persistency;
    Host.MAX_SENT_MESSAGES_NO = maxHosts * 3;
    BUS b = createBusThread();
    createHostsThreads(b);
    waitUntilSendCompleted(b);
  }
  
  private BUS createBusThread(){
    BUS bus = new BUS(1, 2500,  HostConstants.BIT_RATE_10);
    bT = new Thread(bus, "bus");
    bT.setDaemon(true);
    bT.start();
    if(HostConstants.deeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeebug)
      bus.writeln("Created: " + bus.toString());
    return bus;
  }
  
  private void createHostsThreads(BUS bus){
    for(int i = 0; i < Host.MAX_HOSTS; i++){
      Host h = new Host(i, bus, bus.randomAddress());
      Thread hT = new Thread(h, "host " + i);
      hT.setDaemon(true);
      hT.start();
      bus.writeln("Created: " + h.toString());
    }
  }
  
  private void waitUntilSendCompleted(BUS b){
    while( ! b.terminationCondition())
      waitMinute(1);
  }
  
  private void waitMinute(int minutes){
    Object timer = new Object();
    try {
      synchronized(timer){
        timer.wait(minutes * 60000);
      }
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
  }
}

//    Host h0 = new Host(0, bus, new Address(3, 7), false, false, true);
//    b.addReceiver(h0.receiver);
//    Thread hT = new Thread(h0, "host " + 0);
//    hT.setDaemon(true);
//    hT.start();
//    if(HostConstants.deeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeebug)
//      bus.writeln("Created: " + h0.toString());
//    Host h1 = new Host(1, bus, new Address(1, 7), true, true, false);
//    b.addReceiver(h1.receiver);
//    Thread hT1 = new Thread(h1, "host " + 0);
//    hT1.setDaemon(true);
//    hT1.start();
//    if(HostConstants.deeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeebug)
//      bus.writeln("Created: " + h1.toString());


//  private void InitializeFrame(){
//    try {
//      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//    MainFrame frame = new MainFrame();
//    if (packFrame) {
//      frame.pack();
//    } else {
//      frame.validate();
//    }
//    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//    Dimension frameSize = frame.getSize();
//    if (frameSize.height > screenSize.height) {
//      frameSize.height = screenSize.height;
//    }
//    if (frameSize.width > screenSize.width) {
//      frameSize.width = screenSize.width;
//    }
//    frame.setLocation( (screenSize.width - frameSize.width) / 2,
//      (screenSize.height - frameSize.height) / 2);
//    frame.setVisible(true);
//  }