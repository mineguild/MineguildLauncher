package net.mineguild.Launcher.minecraft;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.mineguild.Launcher.Constants;
import net.mineguild.Launcher.MineguildLauncher;
import net.mineguild.Launcher.log.Logger;
import net.mineguild.Launcher.utils.json.Settings;

import org.apache.commons.io.IOUtils;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.exceptions.UserMigratedException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

@SuppressWarnings("serial")
public class LoginDialog extends JDialog {

  private JButton loginButton;
  private JButton cancelButton;
  private JPanel dataPanel;
  private JTextField userField;
  private JLabel lblPassword;
  private JPasswordField passwordField;
  private JCheckBox savePasswordBox;
  private JCheckBox saveTokenBox;
  private JPanel buttonPanel;
  private JPanel checkBoxPanel;

  public LoginResponse response;
  public int accountLevel;
  public boolean successfull = false;
  public boolean forceUpdate = false;
  public boolean launchBuilder = false;
  public boolean isRelogin = false;
  private JLabel lblUsernameemail;
  private JCheckBox chckbxForceUpdate;
  private JPanel panel;
  private JPanel panel_1;
  private JCheckBox chckbxLaunchModpackbuilder;
  private JCheckBox chckbxAutologin;
  private Settings settings;

  public LoginDialog(Frame parent) {
    super(parent);
    settings = MineguildLauncher.getSettings();
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        if (successfull == false || response == null) {
          if (!isRelogin) {
            System.exit(0);
          } else {
            dispose();
          }
        }
      }
    });
    setAlwaysOnTop(true);
    setTitle("Login with MC-Account");
    setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/icon.png")));
    JPanel bottomPanel = new JPanel();
    getContentPane().add(bottomPanel, BorderLayout.PAGE_END);
    bottomPanel.setLayout(new BorderLayout(0, 0));

    buttonPanel = new JPanel();
    bottomPanel.add(buttonPanel, BorderLayout.CENTER);
    loginButton = new JButton("Login");
    loginButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {

          @Override
          public void run() {
            getContentPane().setEnabled(false);
            login();
            getContentPane().setEnabled(true);
          }
        });
      }
    });
    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    buttonPanel.add(loginButton);
    cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!isRelogin) {
          System.exit(0);
        } else {
          dispose();
        }
      }
    });

    buttonPanel.add(cancelButton);

    checkBoxPanel = new JPanel();
    bottomPanel.add(checkBoxPanel, BorderLayout.NORTH);
    checkBoxPanel.setLayout(new BorderLayout(0, 0));

    panel = new JPanel();
    checkBoxPanel.add(panel, BorderLayout.SOUTH);
    panel.setLayout(new GridLayout(0, 1, 0, 0));

    chckbxAutologin = new JCheckBox("Auto-Login");
    chckbxAutologin.setHorizontalAlignment(SwingConstants.CENTER);
    panel.add(chckbxAutologin);

    chckbxForceUpdate = new JCheckBox("Force Update");
    chckbxForceUpdate.setHorizontalAlignment(SwingConstants.CENTER);
    panel.add(chckbxForceUpdate);


    chckbxLaunchModpackbuilder = new JCheckBox("Launch ModPackBuilder");
    chckbxLaunchModpackbuilder.setHorizontalAlignment(SwingConstants.CENTER);
    panel.add(chckbxLaunchModpackbuilder);

    panel_1 = new JPanel();
    checkBoxPanel.add(panel_1, BorderLayout.NORTH);

    savePasswordBox = new JCheckBox("Save password");
    panel_1.add(savePasswordBox);
    savePasswordBox.setHorizontalAlignment(SwingConstants.CENTER);

    saveTokenBox = new JCheckBox("Save MCToken");
    panel_1.add(saveTokenBox);
    saveTokenBox.setHorizontalAlignment(SwingConstants.LEFT);

    dataPanel = new JPanel();
    dataPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(dataPanel, BorderLayout.NORTH);
    dataPanel.setToolTipText("");
    dataPanel.setLayout(new FormLayout(new ColumnSpec[] {FormFactory.DEFAULT_COLSPEC,
        FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("default:grow(3)"),},
        new RowSpec[] {RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC,
            RowSpec.decode("default:grow"),}));

    lblUsernameemail = DefaultComponentFactory.getInstance().createLabel("Username/Email");
    dataPanel.add(lblUsernameemail, "1, 1, right, default");

    userField = new JTextField();
    dataPanel.add(userField, "3, 1, fill, fill");
    userField.setColumns(25);
    userField.getDocument().addDocumentListener(new DocumentListener() {

      @Override
      public void removeUpdate(DocumentEvent e) {
        resetStuff();
      }

      @Override
      public void insertUpdate(DocumentEvent e) {
        resetStuff();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        resetStuff();
      }

      private void resetStuff() {
        if (settings.getProfile() != null) {
          if (!((String) settings.getProfile().get("username")).equals(userField.getText())) {
            saveTokenBox.setSelected(false);
            settings.setProfile(null);
            loginButton.setText("Login");
            passwordField.setEnabled(true);
          }
        }

      }
    });

    lblPassword = new JLabel("Password");
    dataPanel.add(lblPassword, "1, 3, right, default");

    passwordField = new JPasswordField();
    dataPanel.add(passwordField, "3, 3, fill, fill");

    boolean token = false;

    if (settings.getProfile() != null) {
      saveTokenBox.setSelected(true);
      token = true;
      loginButton.setText("Login(MCToken)");
      passwordField.setEnabled(false);
    }

    if (settings.getMCUser().length() > 0) {
      userField.setText(settings.getMCUser());
      if (token) {
        if (settings.getMCPassword().length() > 0) {
          passwordField.setText(settings.getMCPassword());
          passwordField.setEnabled(true);
          savePasswordBox.setSelected(true);
        }
      } else if (settings.getMCPassword().length() > 0) {
        passwordField.setText(settings.getMCPassword());
        savePasswordBox.setSelected(true);
      }
    } else {
      settings.setMCPassword("");
    }


    pack();
    setMinimumSize(getSize());
    setModal(true);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);
    getRootPane().setDefaultButton(loginButton);
  }

  public void run() {
    setVisible(true);
  }

  private LoginWorker login() {
    YggdrasilUserAuthentication authentication =
        (YggdrasilUserAuthentication) new YggdrasilAuthenticationService(Proxy.NO_PROXY,
            settings.getClientToken()).createUserAuthentication(Agent.MINECRAFT);
    Map<String, Object> m = settings.getProfile();
    final String user = userField.getText();
    final String pass = new String(passwordField.getPassword());
    boolean mojangData = false;
    boolean hasPassword = false;
    authentication.setUsername(user);
    if (pass != null && pass.length() > 0) {
      authentication.setPassword(pass);
      hasPassword = true;
    }
    if (m != null) {
      authentication.loadFromStorage(m);
      mojangData = true;
    } else {
      authentication.setUsername(user);
    }

    loginButton.setText("Logging in...");
    LoginWorker w = new LoginWorker(authentication);
    final boolean mJ_ = mojangData;
    final boolean hP_ = hasPassword;
    w.addPropertyChangeListener(new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("done")) {
          LoginWorker w = (LoginWorker) evt.getSource();
          loggedIn(w, mJ_, hP_, user, pass);
        }
      }
    });
    loginButton.setEnabled(false);
    cancelButton.setEnabled(false);
    passwordField.setEnabled(false);
    userField.setEnabled(false);
    savePasswordBox.setEnabled(false);
    saveTokenBox.setEnabled(false);
    w.execute();
    return w;
  }

  public void loggedIn(LoginWorker w, boolean mojangData, boolean hasPassword, String user,
      String pass) {
    boolean loginSuccess = w.success;
    loginButton.setEnabled(true);
    cancelButton.setEnabled(true);
    passwordField.setEnabled(true);
    userField.setEnabled(true);
    savePasswordBox.setEnabled(true);
    saveTokenBox.setEnabled(true);
    try {
      response = w.get();
      if (!loginSuccess) {
        throw w.t;
      }
    } catch (Exception e) {
      if (e instanceof InvalidCredentialsException) {
        if (mojangData) {
          if (hasPassword) {
            Logger.logInfo("Unable to login with MCToken, trying stored password.");
            settings.setProfile(null);
            login();
          } else {
            loginButton.setText("Login");
            settings.setProfile(null);
            JOptionPane.showMessageDialog(this,
                "MCToken is probably invalid! Please retry auth with password.");
            passwordField.setEnabled(true);
          }
        } else {
          JOptionPane.showMessageDialog(this, "Invalid username and/or password");
        }
      } else if (e instanceof UserMigratedException) {
        JOptionPane.showMessageDialog(this, "User migrated! Use E-Mail to sign in!");
      } else if (e instanceof AuthenticationUnavailableException) {
        JOptionPane.showMessageDialog(this,
            "Couldn't authenticate with mojang. Either them or you are offline.");
      } else if (e instanceof AuthenticationException) {
        JOptionPane.showMessageDialog(this, "Other error occurred: " + e.getLocalizedMessage());
      } else if (e instanceof MGAuthException) {
        JOptionPane
            .showMessageDialog(this, "Can't authenticate with Mineguild!\n" + e.getMessage());
      } else {
        Logger.logError("Other exception!", e);
      }
    }

    if (!loginSuccess) {
      setTitle("Login with MC-Account");
      loginButton.setText("Login");
      return;
    }

    settings.setMCUser(user);
    if (savePasswordBox.isSelected()) {
      settings.setMCPassword(pass);
    } else {
      settings.clearPassword();
    }
    if (saveTokenBox.isSelected()) {
      settings.setProfile(w.auth.saveForStorage());
    } else {
      settings.setProfile(null);
    }

    launchBuilder = chckbxLaunchModpackbuilder.isSelected();
    forceUpdate = chckbxForceUpdate.isSelected();
    successfull = true;
    dispose();
  }

  public static int mg_login(String uuid) throws IOException {
    String out = IOUtils.toString(new URL(Constants.MG_LOGIN_SCRIPT + "?uuid=" + uuid));
    String[] split = out.split("\n");
    if (split.length == 2) {
      return Integer.parseInt(split[0].split("=")[1]);
    }
    return 0;
  }

  public void setReLogin() {
    panel.setVisible(false);
    setMinimumSize(null);
    pack();
    setMinimumSize(getSize());
  }

  public void trySilentLogin() {
    setReLogin();
    setTitle("Logging in...");
    if (!userField.getText().isEmpty()
        && (passwordField.getPassword().length > 0 || saveTokenBox.isSelected())) {
      login();
    }
    setVisible(true);
  }



}
