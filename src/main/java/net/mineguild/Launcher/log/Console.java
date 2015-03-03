package net.mineguild.Launcher.log;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.commons.io.FileUtils;

import net.mineguild.Launcher.MineguildLauncher;
import net.mineguild.Launcher.minecraft.MCLauncher;
import net.mineguild.Launcher.utils.OSUtils;
import net.mineguild.Launcher.utils.PastebinPoster;

@SuppressWarnings("serial") public class Console extends JFrame implements ILogListener {
    private final JTextPane displayArea;
    private final Document displayAreaDoc;
    private final JComboBox<LogType> logTypeComboBox;
    private LogType logType = LogType.MINIMAL;
    private final JComboBox<LogSource> logSourceComboBox;
    private LogSource logSource = LogSource.ALL;
    private LogLevel logLevel = LogLevel.INFO;
    private JButton killMCButton;
    private final Font FONT = new Font("Monospaced", 0, 12);
    private SimpleAttributeSet RED = new SimpleAttributeSet();
    private SimpleAttributeSet YELLOW = new SimpleAttributeSet();
    private SimpleAttributeSet WHITE = new SimpleAttributeSet();

    public Console() {
        setTitle("Mineguild Launcher Console");
        setMinimumSize(new Dimension(800, 400));
        setIconImage(
            Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/icon.png")));
        getContentPane().setLayout(new BorderLayout(0, 0));

        StyleConstants.setForeground(RED, Color.RED);
        StyleConstants.setForeground(YELLOW, Color.YELLOW);
        StyleConstants.setForeground(WHITE, Color.WHITE);
        JPanel panel = new JPanel();

        getContentPane().add(panel, BorderLayout.SOUTH);
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        JButton paste = new JButton("Paste log to pastebin");
        paste.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent arg0) {
                JOptionPane pane = new JOptionPane(
                    "Do you want to paste the log to pastebin and open it in browser?");
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
                    File logFile = new File(OSUtils.getLocalDir(), "combined.log");
                    String log = null;
                    try {
                        log = FileUtils.readFileToString(logFile);
                    } catch (Exception e) {
                        Logger.logError("Unable to read logfile! Using internally stored messages",
                            e);
                        log = Logger.getLogs();
                    }
                    PastebinPoster poster = new PastebinPoster(log);
                    poster.execute();
                }
            }
        });
        panel.add(paste);

        JButton clipboard = new JButton("Copy to clipboard");
        clipboard.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent arg0) {
                JOptionPane pane = new JOptionPane(
                    "This will copy the contents of the log to your clipboard! Are you sure you want to do that?");
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
                    File logFile = new File(OSUtils.getLocalDir(), "combined.log");
                    String log = null;
                    try {
                        log = FileUtils.readFileToString(logFile);
                    } catch (Exception e) {
                        Logger.logError("Unable to read logfile! Using internally stored messages",
                            e);
                        log = Logger.getLogs();
                    }
                    StringSelection stringSelection = new StringSelection(
                        "MG Launcher logs:\n" + log + "[" + new SimpleDateFormat("HH:mm:ss")
                            .format(new Date()) + "]" + " Logs copied to clipboard");
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
            @Override public void actionPerformed(ActionEvent arg0) {

                // OSUtils.browse(Locations.SUPPORTSITE);
            }
        });
        panel.add(ircButton);

        killMCButton = new JButton("Kill MC");
        killMCButton.setEnabled(false);
        killMCButton.setVisible(true);
        killMCButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent arg0) {
                MCLauncher.killMC();
            }
        });
        panel.add(killMCButton);



        displayArea = new JTextPane() {
            @Override public boolean getScrollableTracksViewportWidth() {
                return true;
            }
        };

        UIDefaults defaults = new UIDefaults();
        defaults.put("TextPane[Enabled].backgroundPainter", Color.BLACK);
        defaults.put("TextPane[Disabled].backgroundPainter", Color.BLACK);
        displayArea.putClientProperty("Nimbus.Overrides", defaults);
        displayArea.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
        displayArea.setBackground(Color.BLACK);
        displayArea.setFont(FONT);
        displayArea.getDocument().addDocumentListener(new DocumentListener() {

            @Override public void removeUpdate(DocumentEvent e) {
            }

            @Override public void insertUpdate(final DocumentEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        long BUFFER_SIZE = MineguildLauncher.getSettings() != null ?
                            MineguildLauncher.getSettings().getConsoleBufferSize() :
                            500;
                        Document document = displayAreaDoc;
                        Element root = document.getDefaultRootElement();
                        while (root.getElementCount() > BUFFER_SIZE) {
                            try {
                                document.remove(0, root.getElement(0).getEndOffset());
                                scrollToBottom();
                                
                                //insertUpdate(e);
                            } catch (BadLocationException localBadLocationException) {
                            }
                        }
                    }
                });
            }

            @Override public void changedUpdate(DocumentEvent e) {
            }
        });
        displayArea.setBorder(null);
        displayArea.setMargin(null);
        displayAreaDoc = displayArea.getDocument();

        DefaultCaret caret = (DefaultCaret) displayArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        new SmartScroller(scrollPane);
        getContentPane().add(scrollPane);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();

        refreshLogs();


        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                Logger.removeListener(MineguildLauncher.con);
        /*
         * if (LaunchFrame.trayMenu != null) { LaunchFrame.trayMenu.updateShowConsole(false); }
         */
            }
        });

    }

    synchronized private void refreshLogs() {
        try {
            displayAreaDoc.remove(0, displayAreaDoc.getLength());
        } catch (Exception ignored) {
        }

        List<LogEntry> entries = Logger.getLogEntries();
        for (LogEntry entry : entries) {
            if ((logSource == LogSource.ALL || entry.source == logSource) && (
                logLevel == LogLevel.DEBUG || logLevel.includes(entry.level))) {
                addEntry(entry, displayAreaDoc);
            }
        }
        try {
            displayAreaDoc.remove(0, 1);
        } catch (Exception e) {
            // ignore
        }
    }

    synchronized private void addEntry(LogEntry entry, Document doc) {
        SimpleAttributeSet col = WHITE;
        switch (entry.level) {
            case ERROR:
                col = RED;
            case WARN:
                col = YELLOW;
            case INFO:
                break;
            case DEBUG:
                break;
            case UNKNOWN:
                break;
            default:
                break;
        }

        try {
            doc.insertString(doc.getLength(), "\n" + entry.toString(logType), col);
        } catch (BadLocationException ingored) {
        }

    }

    public void scrollToBottom() {
        displayArea.setCaretPosition(displayArea.getDocument().getLength());
    }

    public void minecraftStarted() {
        killMCButton.setEnabled(true);
    }

    public void minecraftStopped() {
        killMCButton.setEnabled(false);
    }

    @Override public void onLogEvent(LogEntry entry) {
        // drop unneeded messages as soon as possible

        if ((logSource == LogSource.ALL || entry.source == logSource) && (logLevel == LogLevel.DEBUG
            || logLevel.includes(entry.level))) {
            final LogEntry entry_ = entry;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    addEntry(entry_, displayAreaDoc);
                }
            });
        }
    }
}
