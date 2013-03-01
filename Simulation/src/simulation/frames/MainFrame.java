package simulation.frames;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import simulation.host.Host;
import simulation.host.HostConstants;
import simulation.bus.BUS;

public class MainFrame
  extends JFrame {
//  
//  public static void simulate(){
//    BUS bus = new BUS(1, 2500,  HostConstants.BIT_RATE_10);
//    Thread bT = new Thread(bus);
//    bT.setDaemon(true);
//    bT.start();
//    bus.write(bus.toString() + "\r\n");
//    for(int i = 0; i < Host.MAX_HOST; i++){
//      Host h = new Host(i, bus, bus.randomAddress());
//      Thread hT = new Thread(h);
//      hT.setDaemon(true);
//      hT.start();
//      bus.write(h.toString() + "\r\n");
//    }
//  }
  
  
  JPanel contentPane;
  JMenuBar jMenuBar1;
  JMenu jMenuFile;
  JMenuItem jMenuFileExit;
  JMenu jMenuHelp;
  JMenuItem jMenuHelpAbout;
  JToolBar jToolBar;
  JButton jButton1;
  JButton jButton2;
  JButton jButton3;
  ImageIcon image1;
  ImageIcon image2;
  ImageIcon image3;
  JLabel statusBar;
  BorderLayout borderLayout1;
  
  //Construct the frame
  public MainFrame() {
    //simulate();
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try {
      jbInit();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  //Component initialization
  private void jbInit() throws Exception {
    contentPane = (JPanel)this.getContentPane();
    contentPane.setLayout(borderLayout1);
    this.setSize(new Dimension(400, 300));
    this.setTitle("This is the Main Frame");
//    jToolBar = new JToolBar();
//    jButton1 = new JButton();
//    jButton2 = new JButton();
//    jButton3 = new JButton();
//    statusBar = new JLabel();
//    jMenuBar1 = new JMenuBar();
//    jMenuFile = new JMenu();
//    jMenuFileExit = new JMenuItem();
//    jMenuHelp = new JMenu();
//    jMenuHelpAbout = new JMenuItem();
//    borderLayout1 = new BorderLayout();
//    image1 = new ImageIcon(simulation.frames.MainFrame.class.getResource(
//      "openFile.png"));
//    image2 = new ImageIcon(simulation.frames.MainFrame.class.getResource(
//      "closeFile.png"));
//    image3 = new ImageIcon(simulation.frames.MainFrame.class.getResource(
//      "help.png"));
//    statusBar.setText(" ");
//    jMenuFile.setText("File");
//    jMenuFileExit.setText("Exit");
//    jMenuFileExit.addActionListener(new MainFrame_jMenuFileExit_ActionAdapter(this));
//    jMenuHelp.setText("Help");
//    jMenuHelpAbout.setText("About");
//    jMenuHelpAbout.addActionListener(new MainFrame_jMenuHelpAbout_ActionAdapter(this));
//    jButton1.setIcon(image1);
//    jButton1.setToolTipText("Open File");
//    jButton2.setIcon(image2);
//    jButton2.setToolTipText("Close File");
//    jButton3.setIcon(image3);
//    jButton3.setToolTipText("Help");
//    jToolBar.add(jButton1);
//    jToolBar.add(jButton2);
//    jToolBar.add(jButton3);
//    jMenuFile.add(jMenuFileExit);
//    jMenuHelp.add(jMenuHelpAbout);
//    jMenuBar1.add(jMenuFile);
//    jMenuBar1.add(jMenuHelp);
//    this.setJMenuBar(jMenuBar1);
//    contentPane.add(jToolBar, BorderLayout.NORTH);
//    contentPane.add(statusBar, BorderLayout.SOUTH);
  }
  
  //File | Exit action performed
  public void jMenuFileExit_actionPerformed(ActionEvent e) {
    System.exit(0);
  }
  
  //Help | About action performed
  public void jMenuHelpAbout_actionPerformed(ActionEvent e) {
    MainFrame_AboutBox dlg = new MainFrame_AboutBox(this);
    Dimension dlgSize = dlg.getPreferredSize();
    Dimension frmSize = getSize();
    Point loc = getLocation();
    dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
      (frmSize.height - dlgSize.height) / 2 + loc.y);
    dlg.setModal(true);
    dlg.pack();
    dlg.setVisible(true);
  }
  
  //Overridden so we can exit when window is closed
  protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      jMenuFileExit_actionPerformed(null);
    }
  }
}

class MainFrame_jMenuFileExit_ActionAdapter
  implements ActionListener {
  MainFrame adaptee;
  
  MainFrame_jMenuFileExit_ActionAdapter(MainFrame adaptee) {
    this.adaptee = adaptee;
  }
  
  public void actionPerformed(ActionEvent e) {
    adaptee.jMenuFileExit_actionPerformed(e);
  }
}

class MainFrame_jMenuHelpAbout_ActionAdapter
  implements ActionListener {
  MainFrame adaptee;
  
  MainFrame_jMenuHelpAbout_ActionAdapter(MainFrame adaptee) {
    this.adaptee = adaptee;
  }
  
  public void actionPerformed(ActionEvent e) {
    adaptee.jMenuHelpAbout_actionPerformed(e);
  }
}
