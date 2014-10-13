package net.mineguild.Launcher;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JCheckBox;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import javax.swing.JTextField;
import javax.swing.JSeparator;

@SuppressWarnings("serial")
public class LaunchFrame extends JFrame {

  private JPanel contentPane;
  private JTextField launchPathField;
  private JTextField gameDirField;

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          LaunchFrame frame = new LaunchFrame();
          frame.setVisible(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Create the frame.
   */
  public LaunchFrame() {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 548, 290);
    contentPane = new JPanel();
    contentPane.setBorder(null);
    contentPane.setLayout(new BorderLayout(0, 0));
    setContentPane(contentPane);
    
    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    tabbedPane.setBorder(null);
    contentPane.add(tabbedPane, BorderLayout.CENTER);
    
    JPanel mainPanel = new JPanel();
    tabbedPane.addTab("Update/Launch", null, mainPanel, null);
    mainPanel.setLayout(new MigLayout("", "[fill][grow][center]", "[][][][][][][]"));
    
    JLabel lblInstalledVersion = new JLabel("Installed Version:");
    lblInstalledVersion.setHorizontalAlignment(SwingConstants.RIGHT);
    mainPanel.add(lblInstalledVersion, "flowx,cell 0 0");
    
    JLabel localVersion = new JLabel("-");
    mainPanel.add(localVersion, "cell 1 0");
    
    JButton btnRefreshLogin = new JButton("Refresh Login");
    mainPanel.add(btnRefreshLogin, "cell 2 0");
    
    JLabel lblLatestVersion = new JLabel("Latest Version:");
    lblLatestVersion.setHorizontalAlignment(SwingConstants.RIGHT);
    mainPanel.add(lblLatestVersion, "flowx,cell 0 1");
    
    JLabel lastestVersion = new JLabel("-");
    mainPanel.add(lastestVersion, "cell 1 1");
    
    JButton btnLogoutchangeAccount = new JButton("Logout/Change Account");
    mainPanel.add(btnLogoutchangeAccount, "cell 2 1");
    
    JCheckBox chckbxForceUpdate = new JCheckBox("Force Update");
    mainPanel.add(chckbxForceUpdate, "cell 0 3");
    
    JCheckBox chckbxFreshInstall = new JCheckBox("Fresh Install");
    mainPanel.add(chckbxFreshInstall, "cell 0 4");
    
    JButton btnUpdateModpack = new JButton("Update ModPack");
    mainPanel.add(btnUpdateModpack, "cell 0 5 3 1");
    
    JButton btnLaunch = new JButton("Launch");
    mainPanel.add(btnLaunch, "cell 0 6 3 1");
    
    JPanel settingsPanel = new JPanel();
    tabbedPane.addTab("Settings", null, settingsPanel, null);
    settingsPanel.setLayout(new FormLayout(new ColumnSpec[] {
        FormFactory.RELATED_GAP_COLSPEC,
        FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC,
        FormFactory.DEFAULT_COLSPEC,},
      new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC,}));
    
    JLabel lblGeneralSettings = DefaultComponentFactory.getInstance().createTitle("General Settings");
    settingsPanel.add(lblGeneralSettings, "2, 2");
    
    JSeparator separator_1 = new JSeparator();
    settingsPanel.add(separator_1, "4, 2, 3, 1");
    
    JLabel lblMinecraftFilesPath = DefaultComponentFactory.getInstance().createLabel("Minecraft Files Path");
    lblMinecraftFilesPath.setHorizontalAlignment(SwingConstants.RIGHT);
    settingsPanel.add(lblMinecraftFilesPath, "2, 4, right, default");
    
    launchPathField = new JTextField();
    settingsPanel.add(launchPathField, "4, 4, fill, default");
    launchPathField.setColumns(10);
    
    JButton browseLaunchPathBtn = new JButton("Browse...");
    settingsPanel.add(browseLaunchPathBtn, "6, 4");
    
    JLabel lblInstancePath = DefaultComponentFactory.getInstance().createLabel("Instance Path");
    lblInstancePath.setHorizontalAlignment(SwingConstants.RIGHT);
    settingsPanel.add(lblInstancePath, "2, 6, right, default");
    
    gameDirField = new JTextField();
    settingsPanel.add(gameDirField, "4, 6, fill, default");
    gameDirField.setColumns(10);
    
    JButton browseGameDirBtn = new JButton("Browse...");
    settingsPanel.add(browseGameDirBtn, "6, 6");
    
    JLabel lblJavaSettings = DefaultComponentFactory.getInstance().createTitle("Java Settings");
    settingsPanel.add(lblJavaSettings, "2, 8");
    
    JSeparator separator = new JSeparator();
    settingsPanel.add(separator, "4, 8, 3, 1");
  }

}
