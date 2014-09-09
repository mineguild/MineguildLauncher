package net.mineguild.Launcher.log;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.mineguild.Launcher.MineguildLauncher;
import net.mineguild.Launcher.minecraft.MCLauncher;

@SuppressWarnings("serial")
public class Console extends JFrame implements ILogListener {
  private final JEditorPane displayArea;
  private final HTMLEditorKit kit;
  private HTMLDocument doc;
  private final JComboBox<LogType> logTypeComboBox;
  private LogType logType = LogType.MINIMAL;
  private final JComboBox<LogSource> logSourceComboBox;
  private LogSource logSource = LogSource.ALL;
  private LogLevel logLevel = LogLevel.INFO;
  private JButton killMCButton;

  public Console() {
    setTitle("Mineguild Launcher Console");
    setMinimumSize(new Dimension(800, 400));
    setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/icon.png")));
    getContentPane().setLayout(new BorderLayout(0, 0));

    JPanel panel = new JPanel();

    getContentPane().add(panel, BorderLayout.SOUTH);
    panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

    JButton paste = new JButton("Paste log to pastebin");
    paste.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        JOptionPane pane =
            new JOptionPane("Do you want to paste the log to pastebin and open it in browser?");
        Object[] options = new String[] {"Yes", "Cancel"};
        pane.setOptions(options);
        JDialog dialog = pane.createDialog(new JFrame(), "Paste log to pastebin");
        dialog.setVisible(true);
        Object obj = pane.getValue();
        int result = -1;
        for (int i = 0; i < options.length; i++) {
          if (options[i].equals(obj)) {
            result = i;
          }
        }
        if (result == 0) {
          // PastebinPoster thread = new PastebinPoster();
          // thread.start();
        }
      }
    });
    panel.add(paste);

    JButton clipboard = new JButton("Copy to clipboard");
    clipboard.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        JOptionPane pane = new JOptionPane("Do you really want to do that?");
        Object[] options = new String[] {"Yes", "No"};
        pane.setOptions(options);
        JDialog dialog = pane.createDialog(new JFrame(), "Copy to clipboard");
        dialog.setVisible(true);
        Object obj = pane.getValue();
        int result = -1;
        for (int i = 0; i < options.length; i++) {
          if (options[i].equals(obj)) {
            result = i;
          }
        }
        if (result == 0) {
          StringSelection stringSelection =
              new StringSelection("FTB Launcher logs:\n" + Logger.getLogs() + "["
                  + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "]"
                  + " Logs copied to clipboard");
          Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
          clipboard.setContents(stringSelection, null);
        }
      }
    });
    panel.add(clipboard);

    logTypeComboBox = new JComboBox<LogType>(LogType.values());
    logTypeComboBox.setSelectedItem(logType);
    logTypeComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        logType = (LogType) logTypeComboBox.getSelectedItem();
        switch (logType) {
          case MINIMAL:
            logLevel = LogLevel.INFO;
            break;
          case EXTENDED:
            logLevel = LogLevel.INFO;
            break;
          case DEBUG:
            logLevel = LogLevel.DEBUG;
            break;
        }

        refreshLogs();
      }
    });
    panel.add(logTypeComboBox);

    logSourceComboBox = new JComboBox<LogSource>(LogSource.values());
    logSourceComboBox.setSelectedItem(logSource);
    logSourceComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        logSource = (LogSource) logSourceComboBox.getSelectedItem();
        refreshLogs();
      }
    });
    panel.add(logSourceComboBox);

    JButton ircButton = new JButton("Open MG IRC");
    ircButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {

        // OSUtils.browse(Locations.SUPPORTSITE);
      }
    });
    panel.add(ircButton);

    killMCButton = new JButton("Kill MC");
    killMCButton.setEnabled(false);
    killMCButton.setVisible(true);
    killMCButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        MCLauncher.killMC();
      }
    });
    panel.add(killMCButton);

    displayArea = new JEditorPane("text/html", "");
    UIDefaults defaults = new UIDefaults();
    defaults.put("EditorPane[Enabled].backgroundPainter", Color.BLACK);
    displayArea.putClientProperty("Nimbus.Overrides", defaults);
    displayArea.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
    displayArea.setBackground(Color.BLACK);
    displayArea.setEditable(false);
    kit = new HTMLEditorKit();
    displayArea.setEditorKit(kit);

    DefaultCaret caret = (DefaultCaret) displayArea.getCaret();
    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

    JScrollPane scrollPane = new JScrollPane(displayArea);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

    getContentPane().add(scrollPane);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    pack();

    refreshLogs();


    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        Logger.removeListener(MineguildLauncher.con);
        /*
         * if (LaunchFrame.trayMenu != null) { LaunchFrame.trayMenu.updateShowConsole(false); }
         */
      }
    });

  }

  synchronized private void refreshLogs() {
    doc = new HTMLDocument();
    displayArea.setDocument(doc);
    List<LogEntry> entries = Logger.getBufferedEntries();
    StringBuilder logHTML = new StringBuilder();
    for (LogEntry entry : entries) {
      // select only messages we want
      if ((logSource == LogSource.ALL || entry.source == logSource)
          && (logLevel == LogLevel.DEBUG || logLevel.includes(entry.level))) {
        logHTML.append(getMessage(entry));
      }
    }
    addHTML(logHTML.toString());
  }

  private void addHTML(String html) {
    synchronized (kit) {
      try {
        kit.insertHTML(doc, doc.getLength(), html, 0, 0, null);
      } catch (BadLocationException ignored) {
        Logger.logError(ignored.getMessage(), ignored);
      } catch (IOException ignored) {
        Logger.logError(ignored.getMessage(), ignored);
      }
      displayArea.setCaretPosition(displayArea.getDocument().getLength());
    }
  }

  public void scrollToBottom() {
    displayArea.setCaretPosition(displayArea.getDocument().getLength());
  }

  private String getMessage(LogEntry entry) {
    String color = "white";
    switch (entry.level) {
      case ERROR:
        color = "#FF7070";
        break;
      case WARN:
        color = "yellow";
      case INFO:
        break;
      case DEBUG:
        break;
      case UNKNOWN:
        break;
      default:
        break;
    }
    return "<font color=\""
        + color
        + "\">"
        + (entry.toString(logType).replace("<", "&lt;").replace(">", "&gt;").trim()
            .replace("\r\n", "\n").replace("\n", "<br/>")) + "</font><br/>";
  }

  public void minecraftStarted() {
    killMCButton.setEnabled(true);
  }

  public void minecraftStopped() {
    killMCButton.setEnabled(false);
  }

  @Override
  public void onLogEvent(LogEntry entry) {
    // drop unneeded messages as soon as possible

    if ((logSource == LogSource.ALL || entry.source == logSource)
        && (logLevel == LogLevel.DEBUG || logLevel.includes(entry.level))) {
      final LogEntry entry_ = entry;
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          addHTML(getMessage(entry_));
        }
      });
    }
  }
}
