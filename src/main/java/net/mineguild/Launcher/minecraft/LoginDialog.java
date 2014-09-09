package net.mineguild.Launcher.minecraft;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.mineguild.Launcher.Constants;
import net.mineguild.Launcher.MineguildLauncher;

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
import javax.swing.SwingConstants;
import java.awt.FlowLayout;

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
  private JLabel lblUsernameemail;
  private JCheckBox chckbxForceUpdate;
  private JPanel panel;
  private JButton btnChangeInstallLocation;
  private JPanel panel_1;

  public LoginDialog(Frame parent) {
    super(parent);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        if (successfull == false || response == null) {
          System.exit(0);
        }
      }
    });
    setAlwaysOnTop(true);
    setTitle("Login with MC-Account");
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
        System.exit(0);
      }
    });

    buttonPanel.add(cancelButton);

    checkBoxPanel = new JPanel();
    bottomPanel.add(checkBoxPanel, BorderLayout.NORTH);
    checkBoxPanel.setLayout(new BorderLayout(0, 0));
    
    panel = new JPanel();
    checkBoxPanel.add(panel, BorderLayout.SOUTH);
    panel.setLayout(new BorderLayout(0, 0));
    
    chckbxForceUpdate = new JCheckBox("Force Update");
    chckbxForceUpdate.setHorizontalAlignment(SwingConstants.CENTER);
    panel.add(chckbxForceUpdate, BorderLayout.NORTH);
    
    btnChangeInstallLocation = new JButton("Change install location");
    btnChangeInstallLocation.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MineguildLauncher.settings.setModpackPath(MineguildLauncher.getInstallPath(getRootPane()));
      }
    });
    panel.add(btnChangeInstallLocation);
    
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
        if (MineguildLauncher.settings.getMojangdata() != null) {
          if (!((String) MineguildLauncher.settings.getMojangdata().get("username"))
              .equals(userField.getText())) {
            saveTokenBox.setSelected(false);
            MineguildLauncher.settings.setMojangdata(null);
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

    if (MineguildLauncher.settings.getMojangdata() != null) {
      saveTokenBox.setSelected(true);
      token = true;
      loginButton.setText("Login(MCToken)");
      passwordField.setEnabled(false);
    }

    if (MineguildLauncher.settings.getMCUser().length() > 0) {
      userField.setText(MineguildLauncher.settings.getMCUser());
      if (token) {
        if (MineguildLauncher.settings.getMCPassword().length() > 0) {
          passwordField.setText(MineguildLauncher.settings.getMCPassword());
          passwordField.setEnabled(true);
          savePasswordBox.setSelected(true);
        }
      } else if (MineguildLauncher.settings.getMCPassword().length() > 0) {
        passwordField.setText(MineguildLauncher.settings.getMCPassword());
        savePasswordBox.setSelected(true);
      }
    } else {
      MineguildLauncher.settings.setMCPassword("");
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

  private void login() {
    YggdrasilUserAuthentication authentication =
        (YggdrasilUserAuthentication) new YggdrasilAuthenticationService(Proxy.NO_PROXY,
            MineguildLauncher.settings.getClientToken()).createUserAuthentication(Agent.MINECRAFT);
    Map<String, Object> m = MineguildLauncher.settings.getMojangdata();
    String user = userField.getText();
    String pass = new String(passwordField.getPassword());
    boolean mojangData = false;
    authentication.setUsername(user);
    if (pass != null && pass.length() > 0) {
      authentication.setPassword(pass);
    }
    if (m != null) {
      authentication.loadFromStorage(m);
      mojangData = true;
    } else {
      authentication.setUsername(user);
      authentication.setPassword(pass);
    }
    try {
      authentication.logIn();
    } catch (UserMigratedException e) {
      JOptionPane.showMessageDialog(this, "User migrated! Use E-Mail to sign in!");
      return;
    } catch (InvalidCredentialsException e) {
      if (mojangData) {
        loginButton.setText("Login");
        MineguildLauncher.settings.setMojangdata(null);
        JOptionPane.showMessageDialog(this,
            "MCToken is probably invalid! Please retry auth with password.");
        passwordField.setEnabled(true);
        return;
      }
      JOptionPane.showMessageDialog(this, "Invalid username and/or password");
      return;
    } catch (AuthenticationUnavailableException e) {
      JOptionPane.showMessageDialog(this,
          "Couldn't authenticate with mojang. Either them or you are offline.");
      return;
    } catch (AuthenticationException e) {
      JOptionPane.showMessageDialog(this, "Other error occurred: " + e.getLocalizedMessage());
      return;
    }

    if (authentication.isLoggedIn() && authentication.canPlayOnline()) {
      try {
        int result = mg_login(authentication.getSelectedProfile().getId().toString());
        if (result < 1) {
          throw new Exception("Not whitelisted!");
        }
      } catch (Exception e) {
        e.printStackTrace();
        JOptionPane
            .showMessageDialog(this, "Can't authenticate with Mineguild!\n" + e.getMessage());
        return;
      }
      response =
          new LoginResponse(Integer.toString(authentication.getAgent().getVersion()), "token",
              authentication.getSelectedProfile().getName(), null, authentication
                  .getSelectedProfile().getId().toString(), authentication);
      MineguildLauncher.settings.setMCUser(user);
      if (savePasswordBox.isSelected()) {
        MineguildLauncher.settings.setMCPassword(pass);
      } else {
        MineguildLauncher.settings.clearPassword();
      }
      if (saveTokenBox.isSelected()) {
        MineguildLauncher.settings.setMojangdata(authentication.saveForStorage());
      } else {
        MineguildLauncher.settings.setMojangdata(null);
      }
      
      forceUpdate = chckbxForceUpdate.isSelected();
      successfull = true;
      dispose();
    }

  }

  private int mg_login(String uuid) throws IOException {
    String out = IOUtils.toString(new URL(Constants.MG_LOGIN_SCRIPT + "?uuid=" + uuid));
    String[] split = out.split("\n");
    if (split.length == 2) {
      return Integer.parseInt(split[0].split("=")[1]);
    }
    return 0;
  }


}
