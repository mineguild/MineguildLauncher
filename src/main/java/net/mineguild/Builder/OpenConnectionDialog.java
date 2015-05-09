package net.mineguild.Builder;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.mineguild.Launcher.utils.json.BuilderSettings.UploadSettings;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class OpenConnectionDialog extends JDialog {

  private final JPanel contentPanel = new JPanel();
  private UploadSettings newSettings;
  private JComboBox<UploadSettings> comboBox = new JComboBox<UploadSettings>();
  @SuppressWarnings("unused") private UploadSettings result = null;

  /**
   * Create the dialog.
   */
  public OpenConnectionDialog() {
    setBounds(100, 100, 450, 144);
    setModal(true);
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setLayout(new FlowLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    {

      contentPanel.add(comboBox);
      newSettings = new UploadSettings();
      newSettings.setName("");
      newSettings.setAddress("Create new");
      comboBox.addItem(newSettings);
      for (UploadSettings uS : ModpackBuilder.settings.getUploadSettings()) {
        comboBox.addItem(uS);
      }
    }
    {
      JPanel buttonPane = new JPanel();
      buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      {
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (comboBox.getSelectedItem().equals(newSettings)) {
              ConnectionCreationDialog dialog = new ConnectionCreationDialog();
              dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
              dialog.setTitle("Create new Connection");
              dialog.setVisible(true);
              if (dialog.settings != null) {
                ModpackBuilder.settings.getUploadSettings().add(dialog.settings);
                comboBox.removeAllItems();
                newSettings = new UploadSettings();
                newSettings.setName("");
                newSettings.setAddress("Create new");
                comboBox.addItem(newSettings);
                for (UploadSettings uS : ModpackBuilder.settings.getUploadSettings()) {
                  comboBox.addItem(uS);
                }
              }

            } else {
              result = (UploadSettings) comboBox.getSelectedItem();
              dispose();
            }
          }
        });
        okButton.setActionCommand("OK");
        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);
      }
      {
        JButton btnChange = new JButton("Change...");
        btnChange.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (!comboBox.getSelectedItem().equals(newSettings)) {
              ConnectionCreationDialog dialog = new ConnectionCreationDialog();
              dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
              dialog.setTitle("Edit Connection");
              dialog.loadSettings((UploadSettings) comboBox.getSelectedItem());
              dialog.setVisible(true);
              if (dialog.settings != null) {
                ModpackBuilder.settings.getUploadSettings().remove(comboBox.getSelectedItem());
                ModpackBuilder.settings.getUploadSettings().add(dialog.settings);
                comboBox.removeAllItems();
                newSettings = new UploadSettings();
                newSettings.setName("");
                newSettings.setAddress("Create new");
                comboBox.addItem(newSettings);
                for (UploadSettings uS : ModpackBuilder.settings.getUploadSettings()) {
                  comboBox.addItem(uS);
                }
              }
            }
          }
        });
        buttonPane.add(btnChange);
      }
      {
        JButton btnRemove = new JButton("Remove");
        btnRemove.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (!comboBox.getSelectedItem().equals(newSettings)) {
              ModpackBuilder.settings.getUploadSettings().remove(comboBox.getSelectedItem());
              comboBox.removeAllItems();
              newSettings = new UploadSettings();
              newSettings.setName("");
              newSettings.setAddress("Create new");
              comboBox.addItem(newSettings);
              for (UploadSettings uS : ModpackBuilder.settings.getUploadSettings()) {
                comboBox.addItem(uS);
              }

            }
          }
        });
        buttonPane.add(btnRemove);
      }
      {
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("Cancel");
        buttonPane.add(cancelButton);
      }
    }
  }

}
