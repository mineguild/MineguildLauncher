package net.mineguild.Launcher;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;
import net.mineguild.Launcher.utils.OSUtils;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class LaunchFrame extends JFrame {

  private JPanel contentPane;
  private JTextField launchPathField;
  private JTextField gameDirField;
  private JTextField textField;

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
    mainPanel.setLayout(new MigLayout("", "[fill][grow][center]", "[][][][][][grow][]"));

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
        ColumnSpec.decode("right:default"),
        FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC,
        FormFactory.DEFAULT_COLSPEC,},
      new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC,
        RowSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC,
        RowSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC,}));

    JLabel lblGeneralSettings =
        DefaultComponentFactory.getInstance().createTitle("General Settings");
    settingsPanel.add(lblGeneralSettings, "2, 2, left, default");

    JSeparator separator_1 = new JSeparator();
    settingsPanel.add(separator_1, "3, 2, 4, 1");

    JLabel lblMinecraftFilesPath =
        DefaultComponentFactory.getInstance().createLabel("Minecraft Files Path");
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
    settingsPanel.add(lblJavaSettings, "2, 8, left, default");

    JSeparator separator = new JSeparator();
    settingsPanel.add(separator, "3, 8, 4, 1");

    JLabel lblJavaPath = DefaultComponentFactory.getInstance().createLabel("Java Path");
    settingsPanel.add(lblJavaPath, "2, 10, right, default");

    textField = new JTextField();
    textField.setColumns(10);
    settingsPanel.add(textField, "4, 10, fill, default");

    JButton autoDetectJavaBtn = new JButton("Auto-Detect");
    settingsPanel.add(autoDetectJavaBtn, "6, 10");

    JLabel lblMemory = DefaultComponentFactory.getInstance().createLabel("Memory");
    settingsPanel.add(lblMemory, "2, 12");

    JSlider memSlider = new JSlider();
    memSlider.setSnapToTicks(true);
    memSlider.setPaintTicks(true);
    memSlider.setValue(1);
    memSlider.setMinimum(1);
    memSlider.setMaximum((int) OSUtils.getOSTotalMemory() / 512);
    memSlider.setMinorTickSpacing(1);
    memSlider.setMajorTickSpacing(2);
    settingsPanel.add(memSlider, "4, 12");

    JLabel lblPermgen = DefaultComponentFactory.getInstance().createLabel("PermGen");
    settingsPanel.add(lblPermgen, "2, 14, right, default");

    JComboBox<String> permGenBox = new JComboBox<String>();
    permGenBox.setModel(new DefaultComboBoxModel<String>(new String[] {"128m", "192m", "256m",
        "512m", "1024m"}));
    settingsPanel.add(permGenBox, "4, 14, fill, default");
  }

}
