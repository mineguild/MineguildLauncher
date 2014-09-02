package net.mineguild.Launcher.minecraft;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Proxy;
import java.util.Arrays;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.mineguild.Launcher.MineguildLauncher;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.exceptions.UserMigratedException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LoginDialog extends JDialog {

  private JButton loginButton;
  private JButton cancelButton;
  private JPanel dataPanel;
  private JLabel lblUsernameemail;
  private JTextField userField;
  private JLabel lblPassword;
  private JPasswordField passwordField;
  private JCheckBox savePasswordBox;
  private JCheckBox saveTokenBox;
  private JPanel buttonPanel;
  private JPanel checkBoxPanel;

  public LoginResponse response;
  public boolean successfull = false;
  private JPanel spacerPanel;

  public LoginDialog(Frame parent) {
    super(parent);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        if(successfull == false || response == null){
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
        login();
      }
    });
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

    savePasswordBox = new JCheckBox("Save password");
    checkBoxPanel.add(savePasswordBox);

    saveTokenBox = new JCheckBox("Save MCToken");
    checkBoxPanel.add(saveTokenBox);

    spacerPanel = new JPanel();
    getContentPane().add(spacerPanel, BorderLayout.NORTH);

    dataPanel = new JPanel();
    spacerPanel.add(dataPanel);
    dataPanel.setToolTipText("");
    dataPanel
        .setLayout(new FormLayout(new ColumnSpec[] {FormFactory.PREF_COLSPEC,
            FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("pref:grow"),},
            new RowSpec[] {FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,}));

    lblUsernameemail = new JLabel("Username/Email");
    dataPanel.add(lblUsernameemail, "1, 1, right, default");

    userField = new JTextField();
    dataPanel.add(userField, "3, 1, fill, default");
    userField.setColumns(25);

    lblPassword = new JLabel("Password");
    dataPanel.add(lblPassword, "1, 3, right, default");

    passwordField = new JPasswordField();
    dataPanel.add(passwordField, "3, 3, fill, default");
    /*
     * if (MineguildLauncher.settings.getMCPassword().length() > 0 &&
     * MineguildLauncher.settings.getMCUser() != null) { if
     * (MineguildLauncher.settings.getMCUser().length() > 0) {
     * userField.setText(MineguildLauncher.settings.getMCUser());
     * passwordField.setText(MineguildLauncher.settings.getMCPassword());
     * savePasswordBox.setEnabled(true); } else { MineguildLauncher.settings.setMCPassword(""); }
     * }/* if (MineguildLauncher.settings.getMCToken() != null) { if
     * (MineguildLauncher.settings.getMCToken().length() > 0) { saveTokenBox.setEnabled(true); } }
     */
    saveTokenBox.setVisible(false); // Not implemented yet.
    pack();
    setModal(true);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);
  }

  public void run() {
    setVisible(true);
  }

  private void login() {
    String clientToken = UUID.randomUUID().toString();
    YggdrasilUserAuthentication authentication =
        (YggdrasilUserAuthentication) new YggdrasilAuthenticationService(Proxy.NO_PROXY,
            clientToken).createUserAuthentication(Agent.MINECRAFT);
    authentication.setUsername(userField.getText());
    authentication.setPassword(new String(passwordField.getPassword()));
    try {
      authentication.logIn();
    } catch (UserMigratedException e) {
      JOptionPane.showMessageDialog(this, "User migrated! Use E-Mail to sign in!");
      return;
    } catch (InvalidCredentialsException e) {
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

    if (authentication.isLoggedIn()) {
      authentication.getUserID();
      response =
          new LoginResponse(Integer.toString(authentication.getAgent().getVersion()), "token",
              userField.getText(), null, authentication.getSelectedProfile().getId().toString(),
              authentication);
      successfull = true;
      dispose();
    }

  }


}
