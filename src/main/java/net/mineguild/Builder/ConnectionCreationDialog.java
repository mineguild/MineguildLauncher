package net.mineguild.Builder;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPasswordField;

import net.mineguild.Launcher.utils.json.BuilderSettings.UploadSettings;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class ConnectionCreationDialog extends JDialog {

  private final JPanel contentPanel = new JPanel();
  private JTextField address;
  private JTextField txtUnnamed;
  private JTextField repoPath;
  private JTextField versionsPath;
  private JTextField filePath;
  private JTextField username;
  private JPasswordField password;
  public UploadSettings settings;

  /**
   * Create the dialog.
   */
  public ConnectionCreationDialog() {
    setBounds(100, 100, 450, 265);
    setModal(true);
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new FormLayout(new ColumnSpec[] {
        FormFactory.RELATED_GAP_COLSPEC,
        FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"),},
      new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC,}));
    {
      JLabel lblNewLabel = new JLabel("Name");
      contentPanel.add(lblNewLabel, "2, 2, right, default");
    }
    {
      txtUnnamed = new JTextField();
      txtUnnamed.setText("Unnamed");
      contentPanel.add(txtUnnamed, "4, 2, fill, default");
      txtUnnamed.setColumns(10);
    }
    {
      JLabel lblAddress = new JLabel("Address (host:port)");
      contentPanel.add(lblAddress, "2, 4, right, default");
    }
    {
      address = new JTextField();
      contentPanel.add(address, "4, 4, fill, default");
      address.setColumns(10);
    }
    {
      JLabel lblRepopath = new JLabel("RepoPath");
      contentPanel.add(lblRepopath, "2, 6, right, default");
    }
    {
      repoPath = new JTextField();
      contentPanel.add(repoPath, "4, 6, fill, default");
      repoPath.setColumns(10);
    }
    {
      JLabel lblNewLabel_1 = new JLabel("VersionsPath");
      contentPanel.add(lblNewLabel_1, "2, 8, right, default");
    }
    {
      versionsPath = new JTextField();
      contentPanel.add(versionsPath, "4, 8, fill, default");
      versionsPath.setColumns(10);
    }
    {
      JLabel lblAssetspath = new JLabel("AssetsPath");
      contentPanel.add(lblAssetspath, "2, 10, right, default");
    }
    {
      filePath = new JTextField();
      contentPanel.add(filePath, "4, 10, fill, default");
      filePath.setColumns(10);
    }
    {
      JLabel lblUsername = new JLabel("Username");
      contentPanel.add(lblUsername, "2, 12, right, default");
    }
    {
      username = new JTextField();
      contentPanel.add(username, "4, 12, fill, default");
      username.setColumns(10);
    }
    {
      JLabel lblPassword = new JLabel("Password");
      contentPanel.add(lblPassword, "2, 14, right, default");
    }
    {
      password = new JPasswordField();
      contentPanel.add(password, "4, 14, fill, default");
      password.setColumns(10);
    }
    {
      JPanel buttonPane = new JPanel();
      buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      {
        settings = new UploadSettings();
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            boolean success = true;
            settings.setName(txtUnnamed.getText());
            if(address.getText().length() > 0){
              settings.setAddress(address.getText());
            } else {
              success = false;
              showEmptyError("Address");
            }
            if(repoPath.getText().length() > 0){
              settings.setRepoPath(repoPath.getText());
            } else {
              success = false;
              showEmptyError("RepoPath");
            }
            if(versionsPath.getText().length() > 0){
              settings.setVersionsPath(versionsPath.getText());
            } else {
              success = false;
              showEmptyError("VersionPath");
            }
            if(filePath.getText().length() > 0){
              settings.setFilePath(filePath.getText());
            } else {
              success = false;
              showEmptyError("AssetDir");
            }
            if(username.getText().length() > 0){
              settings.setUsername(username.getText());
            } else {
              success = false;
              showEmptyError("Username");
            }
            if(password.getPassword().length > 0){
              settings.setPassword(String.copyValueOf(password.getPassword()));
            } else {
              success = false;
              showEmptyError("Password");
            }
            if(success)
              dispose();
            
          }
        });
        okButton.setActionCommand("OK");
        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);
      }
      {
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("Cancel");
        buttonPane.add(cancelButton);
      }
    }
    
    
    
  }
  
  public void loadSettings(UploadSettings settings){
    this.settings = settings;
    txtUnnamed.setText(settings.getName());
    address.setText(settings.getAddress());
    repoPath.setText(settings.getRepoPath());
    versionsPath.setText(settings.getVersionsPath());
    filePath.setText(settings.getFilePath());
    username.setText(settings.getUsername());
    password.setText(settings.getPassword()); 
  }

  
  private void showEmptyError(String field){
    JOptionPane.showMessageDialog(this, String.format("%s can't be empty!", field), String.format("Error!", field), JOptionPane.ERROR_MESSAGE);
  }

}
