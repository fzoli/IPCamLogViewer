package frame;

import fileinfo.FileInfo;
import fileinfo.FileInfoParser;
import fileinfo.FriendlyFileInfo;
import fileinfo.http.FileInfoUtils;
import fileinfo.impl.FriendlyFileInfoImpl;
import frame.combobox.TitledComboBox;
import frame.combobox.TitledComboBoxModel;
import frame.scrollpane.ScrollPaneUtils;
import frame.statusbar.StatusBarFrame;
import frame.statusbar.StatusBarUtils;
import frame.table.ListTable;
import frame.table.ListTableModel;
import frame.table.ListTableModelHelper;
import frame.table.ListTableSelectionListener;
import frame.toolbar.ToolbarUtils;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.swing.*;
import main.Server;
import net.sf.jipcam.axis.MjpegFrame;
import net.sf.jipcam.axis.MjpegInputStream;
import resources.ResourceUtils;
import sun.misc.BASE64Encoder;

public class DateChooser extends StatusBarFrame {

    private FileInfoListViewer viewer;
    
    private final boolean auth;
    private final UserInfoAsker asker;
    private final String camUrl;
    private final Server[] servers;
    
    private final TitledComboBox CB_SERVER = new TitledComboBox("Szerver");
    private final TitledComboBox CB_TYPE = new TitledComboBox("Típus");
    private final TitledComboBox CB_CAM = new TitledComboBox("Kamera");
    private final TitledComboBox CB_YEAR = new TitledComboBox("Év");
    private final TitledComboBox CB_MONTH = new TitledComboBox("Hónap");
    private final TitledComboBox CB_DAY = new TitledComboBox("Nap");
    private final JButton BT_CHECK = ToolbarUtils.createToolbarButton("check.png", "Képek böngészése (Szóköz)");
    private final JButton BT_CAMERA = ToolbarUtils.createToolbarButton("camera.png", "Élő kamerakép");
    private final HashMap<Object, String> tmp = new HashMap<Object, String>();
    
    private final ListTable table = new ListTable(new ListTableSelectionListener() {

        @Override
        public void tableRowChanged() {
            index = table.getSelectedRowIndex();
        }
        
    });
    
    private int index = 0;
    private List<FileInfo> listFi;
    private boolean updating = false;
    private JScrollPane scrollPane;
    private List<TitledComboBox> order;
    
    public DateChooser(Server[] servers, boolean auth, String camUrl, UserInfoAsker asker) {
        this.servers = servers;
        this.camUrl = camUrl;
        this.asker = asker;
        this.auth = auth;
        initFrame();
    }
    
    private void initFrame() {
        setTitle("Dátumválasztó");
        setButtonsEnabled(false);
        order = createOrderedList();
        
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = c.NORTH;
        c.fill = c.BOTH;
        c.weightx = 1;
        c.gridx = 1;
        
        JPanel cbPanel = new JPanel(new GridLayout(1, 5, 3, 3));
        cbPanel.setOpaque(false);
        cbPanel.add(CB_SERVER);
        cbPanel.add(CB_TYPE);
        cbPanel.add(CB_CAM);
        cbPanel.add(CB_YEAR);
        cbPanel.add(CB_MONTH);
        cbPanel.add(CB_DAY);
        
        c.gridy = 1;
        c.weighty = 0;
        JToolBar tb = ToolbarUtils.createDefaultToolbar();
        tb.setLayout(new GridBagLayout());
        GridBagConstraints tbc = new GridBagConstraints();
        tbc.fill = tbc.HORIZONTAL;
        tbc.weightx = 1;
        tbc.insets = new Insets(0, 5, 0, 5);
        tb.add(cbPanel, tbc);
        tbc.weightx = 0;
        tbc.insets = new Insets(2, 0, 2, 3);
        tb.add(BT_CHECK, tbc);
        tb.add(BT_CAMERA, tbc);
        add(tb, c);
        
        c.gridy = 2;
        c.weighty = 1;
        scrollPane = ScrollPaneUtils.createDefaultScrollPane(table, 660, 380);
        add(scrollPane, c);
        
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(this);
        initServerCb();
        setActionListeners();
        
        setVisible(true);
        StatusBarUtils.setMinimumFrameWidth(tb, this);
        updateTypeCb();
    }
    
    private void initServerCb() {
        for (Server s : servers) {
            CB_SERVER.addItem(s);
        }
        CB_SERVER.getModel().setSelectedItem(servers[0]);
    }
    
    private JLabel lb;
    private HttpURLConnection conn;
    
    private void showLiveStream() {
        BT_CAMERA.setEnabled(false);
        new Thread(new Runnable() {

            @Override
            public void run() {
                lb = new JLabel(new ImageIcon());
                new JFrame() {
                    {   
                        setTitle("Élő kamerakép");
                        setIconImage(ResourceUtils.getImage("icon.png"));
                        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                        getContentPane().setPreferredSize(new Dimension(640, 480));
                        add(lb);
                        pack();
                        setLocationRelativeTo(null);
                        setResizable(false);
                        setVisible(true);
                        try {
                            conn = (HttpURLConnection) new URL(camUrl).openConnection();
                            conn.setRequestMethod("GET");
                            if (auth) {
                                String userpass = asker.getUserName() + ":" + asker.getPassword();
                                BASE64Encoder encoder = new BASE64Encoder();
                                String basicAuth = "Basic " + encoder.encode(userpass.getBytes());
                                conn.setRequestProperty ("Authorization", basicAuth);
                            }
                            conn.connect();
                            MjpegInputStream mjpegin = new MjpegInputStream(conn.getInputStream());
                            MjpegFrame fr;
                            try {
                                while((fr = mjpegin.readMjpegFrame()) != null) {
                                    lb.setIcon(new ImageIcon(fr.getImage()));
                                }
                            }
                            catch (Exception ex) {
                                dispose();
                            }
                        }
                        catch (Exception ex) {
                            dispose();
                        }
                    }

                    @Override
                    public void dispose() {
                        if (lb != null) {
                            if (!DateChooser.this.isVisible()) DateChooser.this.setVisible(true);
                            BT_CAMERA.setEnabled(true);
                            conn.disconnect();
                            conn = null;
                            lb = null;
                            super.dispose();
                        }
                    }
                    
                };
            }
            
        }).start();
    }
    
    private Server getServer() {
        Object selected = CB_SERVER.getModel().getSelectedItem();
        if (selected == null) return servers[0];
        return (Server) selected;
    }
    
    private void enableNextCb(Object src, boolean b) {
        int i = order.indexOf(src) + 1;
        if (i >= order.size()) return;
        order.get(i).setEnabled(b);
    }
    
    private void resetAndDisableCbs(Object src) {
        int start = order.indexOf(src) + 1;
        resetAndDisableCbs(start);
    }
    
    private void resetAndDisableCbs(int start) {
        index = 0;
        ListTableModel m = new ListTableModel();
        table.setModel(m);
        
        for (int i = start; i < order.size(); i++) {
            TitledComboBox cb = order.get(i);
            cb.setEnabled(false);
            cb.setActionCommand("resetting");
            cb.removeAllItems();
            cb.setActionCommand("event");
            cb.resetTitle();
        }
    }
    
    private void setActionListeners() {
        table.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (viewer == null && listFi != null && !listFi.isEmpty() && e.getKeyCode() == KeyEvent.VK_SPACE) {
                    showBrowser();
                }
            }
            
        });
        BT_CAMERA.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                showLiveStream();
            }
            
        });
        BT_CHECK.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!listFi.isEmpty()) showBrowser();
            }
            
        });
        
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowIconified(WindowEvent e) {
                if (lb != null) {
                    setVisible(false);
                }
            }
            
        });
        
        CB_SERVER.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                BT_CHECK.setEnabled(false);
                updateTypeCb();
                resetAndDisableCbs(1);
            }
            
        });
        
        ActionListener cbl = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("resetting")) return;
                Object src = e.getSource();
                String obj = (String)((TitledComboBox)src).getSelectedItem();
                if (tmp.get(src) == obj) return;
                tmp.put(src, obj);
                BT_CHECK.setEnabled(false);
                if (src == CB_DAY) {
                    updateFiList();
                }
                resetAndDisableCbs(src);
                String url = createUrl(src);
                List<TitledComboBox> l = order;
                int i = 0;
                for (TitledComboBox cb : l) {
                    if (l.size() - 1 > i && src == cb) updateCb(url, l.get(i+1), src);
                    i++;
                }
            }
            
        };
        CB_CAM.addActionListener(cbl);
        CB_DAY.addActionListener(cbl);
        CB_MONTH.addActionListener(cbl);
        CB_TYPE.addActionListener(cbl);
        CB_YEAR.addActionListener(cbl);
    }
    
    private List<FriendlyFileInfo> getFileInfoList(String url) {
        return FileInfoUtils.getFileInfoList(getServer().executor, getServer().parser, url);
    }
    
    private void setButtonsEnabled(boolean b) {
        CB_CAM.setEnabled(b);
        CB_DAY.setEnabled(b);
        CB_MONTH.setEnabled(b);
        CB_TYPE.setEnabled(b);
        CB_YEAR.setEnabled(b);
        BT_CHECK.setEnabled(b);
    }
    
    public void updateFiList() {
        CB_DAY.setEnabled(false);
        BT_CHECK.setEnabled(false);
        getStatusBar().setProgress("Képlista betöltése...");
        String url = createUrl(CB_DAY);
        getServer().executor.setUrl(url);
        SwingWorker sw = new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {
                getServer().parser.setSrc(getServer().executor.getResponse());
                listFi = getServer().parser.getFileInfoList("jpg");
                if (getServer().executor.getStatus() == FileInfoParser.OK) {
                    getStatusBar().reset();
                    if (listFi.isEmpty()) getStatusBar().setMessage("A kiválasztott dátumhoz nincs naplózás.");
                    else {
                        if (viewer != null && viewer.isVisible()) {
                            viewer.setFiList(listFi);
                        }
                        ListTableModel model = new ListTableModel();
                        List<ListTableModelHelper> lsFfi = new ArrayList<ListTableModelHelper>();
                        for (FileInfo fi : listFi) {
                            lsFfi.add(new FriendlyFileInfoImpl(fi));
                        }
                        model.updateList(lsFfi);
                        table.setModel(model);
                        BT_CHECK.setEnabled(true);
                    }
                }
                else {
                    getStatusBar().setError("Hiba a képlista letöltésekor!");
                }
                CB_DAY.setEnabled(true);
                return null;
            }
            
        };
        sw.execute();
    }
    
    private void showBrowser() {
        setButtonsEnabled(false);
        final String url = createUrl(CB_DAY);
        getServer().executor.setUrl(url);
        viewer = new FileInfoListViewer(url, getServer().executor, listFi, index);
        viewer.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {
                setButtonsEnabled(true);
                setVisible(true);
                viewer = null;
            }

        });
        setVisible(false);
    }
    
    private String createUrl(Object source) {
        return createUrl(-1, source);
    }
    
    private String createUrl(int limit, Object source) {
        List<TitledComboBox> order = createOrderedList();
        StringBuilder sb = new StringBuilder(getServer().rootUrl);
        int i = 0;
        for (TitledComboBox cb : order) {
            if (limit == i) break;
            if (!cb.isSelected() && !cb.equals(source)) break;
            Object o = cb.getModel().getSelectedItem();
            sb.append(o);
            sb.append('/');
            i++;
        }
        return sb.toString();
    }
    
    private List<TitledComboBox> createOrderedList() {
        TitledComboBox[] t = {CB_TYPE, CB_CAM, CB_YEAR, CB_MONTH, CB_DAY};
        return Arrays.asList(t);
    }
    
    private void updateTypeCb() {
        updateCb(getServer().rootUrl, CB_TYPE, null);
    }
    
    private void updateCb(final String url, final TitledComboBox cb, final Object src) {
        if (updating) return;
        updating = true;
        final Object o = src;
        enableNextCb(o, false);
        getStatusBar().setProgress("Kérem, várjon...");
        SwingWorker sw = new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {
                boolean isEx = false;
                try {
                    List<FriendlyFileInfo> rootList = getFileInfoList(url);
                    List<String> l = new ArrayList<String>();
                    for (FriendlyFileInfo ffi : rootList) {
                        l.add(ffi.getFileInfo().getName());
                    }
                    cb.setModel(new TitledComboBoxModel(l.toArray()));
                }
                catch(Exception ex) {
                    isEx = true;
                }
                int code = getServer().executor.getStatus();
                if (isEx || code != FileInfoParser.OK) {
                    getStatusBar().setError("Kapcsolódás hiba: " + code);
                    CB_TYPE.setEnabled(true);
                }
                else {
                    enableNextCb(o, true);
                    getStatusBar().reset();
                }
                updating = false;
                return null;
            }
            
        };
        sw.execute();
    }
    
}