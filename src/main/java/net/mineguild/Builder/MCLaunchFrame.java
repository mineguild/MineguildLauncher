package net.mineguild.Builder;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.mineguild.Launcher.MineguildLauncher;
import net.mineguild.Launcher.minecraft.LoginDialog;
import net.mineguild.Launcher.minecraft.MCInstaller;
import net.mineguild.Launcher.utils.OSUtils;

import org.apache.commons.io.FileUtils;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class MCLaunchFrame extends JFrame {

  private JPanel contentPane;
  private JTextField launchPathField;
  private JTextField javaPathField;
  private JSlider memSlider;
  private MCLaunchFrame instance;
  private JButton btnSaveAndLaunch;
  private JComboBox<String> permGenComboBox;
  private JTextField gameDirField;

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          MCLaunchFrame frame = new MCLaunchFrame();
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
  public MCLaunchFrame() {
    instance = this;
    setTitle("Launch MC");
    setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/icon.png")));
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    contentPane.setLayout(new FormLayout(new ColumnSpec[] {FormFactory.RELATED_GAP_COLSPEC,
        FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
        FormFactory.DEFAULT_COLSPEC,}, new RowSpec[] {FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,}));

    JLabel lblGeneralSettings =
        DefaultComponentFactory.getInstance().createTitle("General Settings");
    contentPane.add(lblGeneralSettings, "2, 2");

    JSeparator separator = new JSeparator();
    contentPane.add(separator, "3, 2, 4, 1");

    JLabel lblLaunchpath = DefaultComponentFactory.getInstance().createLabel("Install/Launch Path");
    contentPane.add(lblLaunchpath, "2, 4, right, default");

    launchPathField = new JTextField();
    contentPane.add(launchPathField, "4, 4, fill, default");
    launchPathField.setColumns(10);

    JButton btnBrowseLaunch = new JButton("Browse...");
    btnBrowseLaunch.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        File start = new File(".");
        if (ModpackBuilder.modpackDirectory != null) {
          start = ModpackBuilder.modpackDirectory;
        }
        JFileChooser chooser = new JFileChooser(start);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle("Select Launch Path(FOLDER)");
        int result = chooser.showDialog(instance, "Select");
        if (result == JFileChooser.APPROVE_OPTION) {
          launchPathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
      }
    });
    contentPane.add(btnBrowseLaunch, "6, 4");

    JLabel lblGamedir = DefaultComponentFactory.getInstance().createLabel("GameDir");
    contentPane.add(lblGamedir, "2, 6, right, default");

    gameDirField = new JTextField();
    contentPane.add(gameDirField, "4, 6, fill, default");
    gameDirField.setColumns(10);

    JButton gameDirBrowseButton = new JButton("Browse...");
    gameDirBrowseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        File start = new File(".");
        if (ModpackBuilder.modpackDirectory != null) {
          start = ModpackBuilder.modpackDirectory;
        }
        JFileChooser chooser = new JFileChooser(start);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle("Select GameDir containing mods/configs");
        int result = chooser.showDialog(instance, "Select");
        if (result == JFileChooser.APPROVE_OPTION) {
          gameDirField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
      }
    });
    contentPane.add(gameDirBrowseButton, "6, 6");

    JLabel lblJavaSettings = DefaultComponentFactory.getInstance().createTitle("Java Settings");
    contentPane.add(lblJavaSettings, "2, 8");

    JSeparator separator_1 = new JSeparator();
    contentPane.add(separator_1, "3, 8, 4, 1");

    JLabel lblJavaPath = DefaultComponentFactory.getInstance().createLabel("Java Path");
    lblJavaPath.setHorizontalAlignment(SwingConstants.CENTER);
    contentPane.add(lblJavaPath, "2, 10, right, default");

    javaPathField = new JTextField();
    contentPane.add(javaPathField, "4, 10, fill, default");
    javaPathField.setColumns(10);

    JButton btnAutodetect = new JButton("Auto-Detect");
    btnAutodetect.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        autoDetectJava();
      }
    });
    contentPane.add(btnAutodetect, "6, 10");

    final JLabel lblMemory = DefaultComponentFactory.getInstance().createLabel("Memory");
    lblMemory.setHorizontalAlignment(SwingConstants.CENTER);
    contentPane.add(lblMemory, "2, 12, right, default");

    memSlider = new JSlider();
    memSlider.addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseDragged(MouseEvent e) {
        JSlider source = (JSlider) e.getComponent();
        lblMemory.setText("Memory("
            + FileUtils.byteCountToDisplaySize(((long) source.getValue()) * 512 * 1024 * 1024)
            + ")");
      }
    });
    memSlider.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        lblMemory.setText("Memory("
            + FileUtils.byteCountToDisplaySize(((long) source.getValue()) * 512 * 1024 * 1024)
            + ")");
      }
    });
    memSlider.setPaintTicks(true);
    memSlider.setMajorTickSpacing(2);
    memSlider.setValue(1);
    memSlider.setSnapToTicks(true);
    memSlider.setMinorTickSpacing(1);
    memSlider.setMinimum(1);
    memSlider.setMaximum((int) OSUtils.getOSTotalMemory() / 512);
    contentPane.add(memSlider, "4, 12");

    JLabel lblPermgen = DefaultComponentFactory.getInstance().createLabel("PermGen");
    contentPane.add(lblPermgen, "2, 14, right, default");

    permGenComboBox = new JComboBox<String>();
    permGenComboBox.setModel(new DefaultComboBoxModel<String>(
        new String[] {"256m", "512m", "1024m"}));
    permGenComboBox.setSelectedIndex(0);
    contentPane.add(permGenComboBox, "4, 14, fill, default");

    JButton btnRefreshLogin = new JButton("Refresh Login");
    btnRefreshLogin.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        refreshLogin();
      }
    });
    contentPane.add(btnRefreshLogin, "2, 16");

    btnSaveAndLaunch = new JButton("Save and Launch");
    btnSaveAndLaunch.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveSettings();
        launchMC();

      }
    });
    contentPane.add(btnSaveAndLaunch, "4, 16");

    JButton btnReset = new JButton("Reset");
    contentPane.add(btnReset, "6, 16");
    
    setLocationRelativeTo(null);
    pack();
    setMinimumSize(getSize());

  }


  public void autoDetectJava() {
    getJavaPathField().setText(MCInstaller.getDefaultJavaPath());
  }

  public void loadSettings() {
    if (ModpackBuilder.settings.getLaunchPath() != null) {
      launchPathField.setText(ModpackBuilder.settings.getLaunchPath());
    }
    if (ModpackBuilder.settings.getGameDir() != null) {
      getGameDirField().setText(ModpackBuilder.settings.getLaunchPath());
    }
    if (ModpackBuilder.settings.getMem() >= 512) {
      memSlider.setValue((int) ModpackBuilder.settings.getMem() / 512);
    }
    if (ModpackBuilder.settings.getPermGen() != null) {
      permGenComboBox.setSelectedItem(ModpackBuilder.settings.getPermGen());
    }
    if (ModpackBuilder.settings.getJavaPath() != null) {
      getJavaPathField().setText(ModpackBuilder.settings.getJavaPath());
    } else {
      autoDetectJava();
    }
  }

  public void saveSettings() {
    ModpackBuilder.settings.setLaunchPath(launchPathField.getText());
    ModpackBuilder.settings.setMem(((long) memSlider.getValue()) * 512);
    ModpackBuilder.settings.setPermGen((String) permGenComboBox.getSelectedItem());
    ModpackBuilder.settings.setJavaPath(getJavaPathField().getText());
    ModpackBuilder.settings.setGameDir(getGameDirField().getText());
  }

  public void launchMC() {
    Thread t = new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          MCInstaller.setup(ModpackBuilder.workPack, new File(launchPathField.getText()), new File(
              getGameDirField().getText()));
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        MCInstaller.launchMinecraft(ModpackBuilder.workPack, MineguildLauncher.res,
            Long.toString(((long) memSlider.getValue()) * 512),
            (String) permGenComboBox.getSelectedItem());
      }
    });
    t.start();
    btnSaveAndLaunch.setEnabled(false);
  }

  public void mcStopped() {
    btnSaveAndLaunch.setEnabled(true);
  }

  public void refreshLogin() {
    LoginDialog dialog = new LoginDialog(this);
    dialog.setReLogin();
    dialog.setVisible(true);
    if (dialog.successfull) {
      MineguildLauncher.res = dialog.response;
    }
  }

  public JTextField getJavaPathField() {
    return javaPathField;
  }

  public JTextField getGameDirField() {
    return gameDirField;
  }
}
