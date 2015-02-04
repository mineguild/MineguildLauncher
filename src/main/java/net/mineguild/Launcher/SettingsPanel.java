package net.mineguild.Launcher;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.mineguild.Launcher.minecraft.MCInstaller;
import net.mineguild.Launcher.utils.OSUtils;
import net.mineguild.Launcher.utils.json.Settings;
import net.mineguild.Launcher.utils.json.Settings.JavaSettings;

import com.google.common.collect.Lists;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class SettingsPanel extends JPanel {

	private JTextField launchPathField;
	private JTextField instancePathField;
	private JSpinner bufferSizeSpinner;
	private JCheckBox chckbxUseRedStyle;
	private JSpinner dlThreadsSpinner;
	private JTextField javaPathField;
	private JSlider memSlider;
	private JCheckBox optimizationBox;
	private JComboBox<String> permGenBox;
	private Settings set;

	private final String[] permGenSizes = new String[] { "192m", "256m",
			"512m", "1024m" };

	public SettingsPanel(Settings set) {
		this.set = set;
		;
		setBorder(null);
		setLayout(new FormLayout(
				new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("right:default"),
						FormFactory.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("default:grow"),
						FormFactory.RELATED_GAP_COLSPEC,
						FormFactory.DEFAULT_COLSPEC,
						FormFactory.RELATED_GAP_COLSPEC,
						FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] {
						FormFactory.RELATED_GAP_ROWSPEC,
						RowSpec.decode("default:grow"),
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
						RowSpec.decode("default:grow"),
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						RowSpec.decode("default:grow"), }));

		JLabel lblGeneralSettings = DefaultComponentFactory.getInstance()
				.createTitle("General Settings");
		add(lblGeneralSettings, "2, 2, left, default");

		JSeparator separator_1 = new JSeparator();
		add(separator_1, "3, 2, 6, 1");

		JLabel lblLaunchPath = DefaultComponentFactory.getInstance()
				.createLabel("Minecraft Files Path");
		lblLaunchPath.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblLaunchPath, "2, 4, right, default");

		launchPathField = new JTextField();
		add(launchPathField, "4, 4, fill, default");
		launchPathField.setColumns(10);

		JButton browseLaunchPathBtn = new JButton("Browse...");
		browseLaunchPathBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				launchPathField.setText(selectPath(launchPathField.getText()));
			}
		});
		add(browseLaunchPathBtn, "6, 4");

		JButton launchPathOpenBtn = new JButton("Open..");
		launchPathOpenBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openPath(launchPathField.getText());
			}
		});
		add(launchPathOpenBtn, "8, 4");

		JLabel lblInstancePath = DefaultComponentFactory.getInstance()
				.createLabel("Instance Path");
		lblInstancePath.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblInstancePath, "2, 6, right, default");

		instancePathField = new JTextField();
		add(instancePathField, "4, 6, fill, default");
		instancePathField.setColumns(10);

		JButton browseInstancePathBtn = new JButton("Browse...");
		browseInstancePathBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				instancePathField.setText(selectPath(instancePathField
						.getText()));
			}
		});
		add(browseInstancePathBtn, "6, 6");

		JButton instancePathOpen = new JButton("Open..");
		instancePathOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openPath(instancePathField.getText());
			}
		});
		add(instancePathOpen, "8, 6");

		JLabel lblConsoleBufferSize = DefaultComponentFactory.getInstance()
				.createLabel("Console Buffer Size");
		add(lblConsoleBufferSize, "2, 8");

		bufferSizeSpinner = new JSpinner();
		bufferSizeSpinner.setModel(new SpinnerNumberModel(new Long(0),
				new Long(0), null, new Long(1)));
		add(bufferSizeSpinner, "4, 8, 3, 1");

		JLabel lblDownloadThreads = DefaultComponentFactory.getInstance()
				.createLabel("Download Threads");
		add(lblDownloadThreads, "2, 10");

		dlThreadsSpinner = new JSpinner();
		dlThreadsSpinner.setModel(new SpinnerNumberModel(new Integer(1),
				new Integer(1), null, new Integer(1)));
		add(dlThreadsSpinner, "4, 10, 3, 1");

		JButton dlThreadsHelp = new JButton("?");
		dlThreadsHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showDlThreadHelp();
			}
		});
		add(dlThreadsHelp, "8, 10");

		JLabel lblUseRedStyle = DefaultComponentFactory.getInstance()
				.createLabel("Style");
		add(lblUseRedStyle, "2, 12");

		chckbxUseRedStyle = new JCheckBox("Use red style");
		chckbxUseRedStyle.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				showInfo("Requires restart", "To apply this setting you need to restart MineguildLauncher");
			}
		});
		add(chckbxUseRedStyle, "4, 12, 3, 1");

		JLabel lblJavaSettings = DefaultComponentFactory.getInstance()
				.createTitle("Java Settings");
		add(lblJavaSettings, "2, 14, left, default");

		JSeparator separator = new JSeparator();
		add(separator, "3, 14, 6, 1");

		JLabel lblJavaPath = DefaultComponentFactory.getInstance().createLabel(
				"Java Path");
		add(lblJavaPath, "2, 16, right, default");

		javaPathField = new JTextField();
		javaPathField.setColumns(10);
		add(javaPathField, "4, 16, 3, 1, fill, default");

		JButton autoDetectJavaBtn = new JButton("Auto-Detect");
		autoDetectJavaBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				javaPathField.setText(MCInstaller.getDefaultJavaPath());
			}
		});
		add(autoDetectJavaBtn, "8, 16");

		final JLabel lblMemory = DefaultComponentFactory.getInstance()
				.createLabel("Memory");
		add(lblMemory, "2, 18");

		memSlider = new JSlider();
		memSlider.setSnapToTicks(true);
		memSlider.setPaintTicks(true);
		memSlider.setValue(1);
		memSlider.setMinimum(1);
		memSlider.setMaximum((int) OSUtils.getOSTotalMemory() / 512);
		memSlider.setMinorTickSpacing(1);
		memSlider.setMajorTickSpacing(2);

		memSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				lblMemory.setText("Memory(" + ((float) source.getValue() * 512)
						/ 1024.0 + "gb)");
			}
		});
		add(memSlider, "4, 18, 3, 1");

		JLabel lblPermgen = DefaultComponentFactory.getInstance().createLabel(
				"PermGen");
		add(lblPermgen, "2, 20, right, default");

		permGenBox = new JComboBox<String>();
		permGenBox.setModel(new DefaultComboBoxModel<String>(permGenSizes));
		add(permGenBox, "4, 20, 3, 1, fill, default");

		JLabel lblOptimizationArgs = DefaultComponentFactory.getInstance()
				.createLabel("Optimization Args");
		add(lblOptimizationArgs, "2, 22");

		optimizationBox = new JCheckBox("Use optimization arguments");
		add(optimizationBox, "4, 22, 3, 1");
	}

	private String selectPath(String currentPath) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int result = chooser.showOpenDialog(this);

		if (result == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile().getAbsolutePath();
		}
		return currentPath;
	}

	public void showDlThreadHelp() {
		JOptionPane
				.showMessageDialog(
						this,
						"Decrease this if you're having problems with corrupt files or breaking dl's. Increase if you want it to be faster (there's a limit).\n Can break the downloader if too high.",
						"Download Threads Information",
						JOptionPane.INFORMATION_MESSAGE);
	}

	public void loadSettings() {
		// Launcher settings
		JavaSettings jSet = set.getJavaSettings();
		instancePathField.setText(set.getInstancesPath().getAbsolutePath());
		launchPathField.setText(set.getMinecraftResourcePath().getAbsolutePath());
		bufferSizeSpinner.setValue(set.getConsoleBufferSize());
		dlThreadsSpinner.setValue(set.getDownloadThreads());
		chckbxUseRedStyle.setSelected(set.isRedStyle());
		// Java settings
		if (Lists.newArrayList(permGenSizes).contains(jSet.getPermGen())) {
			permGenBox.setSelectedItem(jSet.getPermGen());
		} else {
			jSet.setPermGen((String) permGenBox.getSelectedItem());
		}

		memSlider.setValue(jSet.getMaxMemory() / 512);
		javaPathField.setText(jSet.getJavaPath());
		optimizationBox.setSelected(jSet.isOptimizationArgumentsUsed());
	}

	public void saveSettings() {
		JavaSettings jSet = set.getJavaSettings();
		// Launcher settings
		set.setInstancesPath(new File(instancePathField.getText()));
		set.setMinecraftResourcePath(new File(launchPathField.getText()));
		set.setConsoleBufferSize((Long) bufferSizeSpinner.getValue());
		set.setRedStyle(chckbxUseRedStyle.isSelected());
		set.setDownloadThreads((Integer) dlThreadsSpinner.getValue());
		// Java Settings
		jSet.setPermGen((String) permGenBox.getSelectedItem());
		jSet.setMaxMemory(memSlider.getValue() * 512);
		jSet.setJavaPath(javaPathField.getText());
		jSet.setOptimizationArgumentsUsed(optimizationBox.isSelected());
	}

	public void openPath(String path) {
		try {
			Desktop.getDesktop().open(new File(path));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void showInfo(String title, String message) {
		JOptionPane.showMessageDialog(this, message, title,
				JOptionPane.INFORMATION_MESSAGE);
	}

}
