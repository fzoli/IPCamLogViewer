package frame;

import fileinfo.FileInfo;
import fileinfo.FriendlyFileInfo;
import fileinfo.impl.FriendlyFileInfoImpl;
import frame.scrollpane.ScrollPaneUtils;
import frame.statusbar.StatusBarFrame;
import frame.statusbar.StatusBarUtils;
import frame.toolbar.ToolbarUtils;
import http.HttpExecutor;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import main.Main;
import resources.ResourceUtils;

public class FileInfoListViewer extends StatusBarFrame {
    
    private int index;
    private double scale = 1;
    private boolean lastLoadOk, loading = false;
    private boolean playing = false;
    private JScrollPane p;
    private Point pm;
    private final String ROOT;
    private final HttpExecutor EXECUTOR;
    private final List<FileInfo> IMGS;
    private BufferedImage lastImg;
    private final JLabel LB_IMG = new JLabel(new ImageIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)));
    private final JButton BT_REFRESH = ToolbarUtils.createToolbarButton("refresh.png", "Képlista frissítése");
    private final JButton BT_FULLSIZE = ToolbarUtils.createToolbarButton("fullsize.png", "Eredeti méret");
    private final JToggleButton CB_FITSIZE = ToolbarUtils.createToggleToolbarButton("fitsize.png", "Ablakhoz igazítás");
    private final JToggleButton CB_QUALITYZOOM = ToolbarUtils.createToggleToolbarButton("star.png", "Minőségi nagyítás");
    private final JButton BT_ZOOM = ToolbarUtils.createToolbarButton("zoom.png", "Nagyítás");
    private final JButton BT_UNZOOM = ToolbarUtils.createToolbarButton("unzoom.png", "Kicsinyítés");
    private final JButton BT_BACK = ToolbarUtils.createToolbarButton("back.png", "Előző (Balra nyíl)");
    private final JButton BT_NEXT = ToolbarUtils.createToolbarButton("next.png", "Következő (Jobbra nyíl)");
    private final JButton BT_SFBACK = ToolbarUtils.createToolbarButton("sfback.png", "Első");
    private final JButton BT_SFNEXT = ToolbarUtils.createToolbarButton("sfnext.png", "Utolsó");
    private final JButton BT_FFBACK = ToolbarUtils.createToolbarButton("ffback.png", "Hátra 100 (Ctrl + Balra nyíl)");
    private final JButton BT_FFNEXT = ToolbarUtils.createToolbarButton("ffnext.png", "Előre 100 (Ctrl + Jobbra nyíl)");
    private final JButton BT_PLAY_PAUSE = ToolbarUtils.createToolbarButton("play.png", "Lejátszás (Szóköz)");
    private final JLabel LB_COUNTER = new JLabel("-");
    private final JLabel LB_SIZE = new JLabel("-");
    private final JLabel LB_DATE = new JLabel("-");
    private final JLabel LB_NAME = new JLabel("-");
    private final JToggleButton CB_INV_PLAY = ToolbarUtils.createToggleToolbarButton("playback.png", "Lejátszás visszafelé");
    private final File cacheDir = new File(".cache");

    private final Timer PLAY_TIMER = new Timer(100, new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!loading) {
                setIndex();
                loadImage(false);
            }
            if (index >= getLastIndex() || index <= 0) stopPlayImage();
        }
        
    });
    
    public FileInfoListViewer(String root, HttpExecutor executor, List<FileInfo> images) {
        this(root, executor, images, 0);
    }
    
    public FileInfoListViewer(String root, HttpExecutor executor, List<FileInfo> images, int startIndex) {
        this.ROOT = root;
        this.EXECUTOR = executor;
        this.IMGS = images;
        this.index = startIndex;
        deleteDirectory(cacheDir);
        cacheDir.mkdir();
        checkImageList();
        initFrame();
        setInfoPanel();
        setBackNextEnabled();
        loadImage();
    }
    
    private void setIndex() {
        index = CB_INV_PLAY.isSelected() ? index - 1 : index + 1;
        checkIndex();
    }
    
    private void checkIndex() {
        if (index < 0) index = 0;
        if (index > getLastIndex()) index = getLastIndex();
    }
    
    private void addInfoSeparator(JPanel p, GridBagConstraints c) {
        c.insets = new Insets(0, 4, 0, 4);
        p.add(new JSeparator(JSeparator.VERTICAL), c);
        c.insets = new Insets(0, 0, 0, 0);
    }
    
    private void setInfoPanel() {
        JPanel p = getStatusBar().getRightPanel();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = c.LINE_START;
        c.fill = c.BOTH;
        c.gridy = 1;
        
        c.gridx = 1;
        p.add(LB_COUNTER, c);
        
        c.gridx = 2;
        addInfoSeparator(p, c);
        
        c.gridx = 3;
        p.add(LB_SIZE, c);
        
        c.gridx = 4;
        addInfoSeparator(p, c);
        
        c.gridx = 5;
        p.add(LB_DATE, c);
        
        c.gridx = 6;
        addInfoSeparator(p, c);
        
        c.gridx = 7;
        p.add(LB_NAME, c);
    }
    
    private void checkImageList() {
        if (IMGS == null || IMGS.isEmpty()) {
            throw new RuntimeException("ImageList is empty");
        }
        checkIndex();
    }
    
    private void showImageInfo() {
        FriendlyFileInfo ffi = new FriendlyFileInfoImpl(IMGS.get(index));
        LB_COUNTER.setText((index + 1) + "/" + IMGS.size());
        LB_DATE.setText(ffi.getFriendlyLastModified());
        LB_NAME.setText(ffi.getFileInfo().getName());
        LB_SIZE.setText(ffi.getFriendlySize());
    }
    
    private BufferedImage createResizedImg(BufferedImage img) {
        try {
            Dimension is = new Dimension(img.getWidth(), img.getHeight());
            if (CB_FITSIZE.isSelected()) {
                Dimension ps = p.getSize();
                if (ps.equals(is)) return img;
                double wr = is.width/(double)is.height;
                double hr = is.height/(double)is.width;
                double w = ps.width;
                if (ps.width*hr > ps.height) w = ps.height*wr;
                return resizeImg(img, (int)w);
            }
            else {
                if (scale != 1) {
                    int sc = (int)(scale*is.getWidth());
                    return resizeImg(img, sc);
                }
            }
        }
        catch (Exception ex) {
            return null;
        }
        return img;
    }
    
    private BufferedImage resizeImg(BufferedImage img, int sc) {
        return CB_QUALITYZOOM.isSelected() ? ResourceUtils.resizeQuality(img, sc) : ResourceUtils.resizeSpeed(img, sc);
    }
    
    private void refreshCachedImg() {
        BufferedImage img = lastImg;
        if (img != null) {
            img = createResizedImg(img);
            if (img != null) {
                flushLbImg();
                LB_IMG.setIcon(new ImageIcon(img));
            }
        }
    }
    
    private void flushLbImg() {
        ((ImageIcon)LB_IMG.getIcon()).getImage().flush();
    }
    
    private int errcode = 0;
    
    private boolean showImage() {
        if (loading) return true;
        loading = true;
        flushLbImg();
        showImageInfo();
        BufferedImage img = null;
        String filename = IMGS.get(index).getName();
        File cachedImg = new File(cacheDir, filename);
        if (cachedImg.exists()) {
            try {
                img = ImageIO.read(cachedImg);
            }
            catch (Exception ex) {}
        }
        if (img == null) {
            try {
                String url = ROOT + IMGS.get(index).getName();
                EXECUTOR.setUrl(url);
                errcode = 1;
                InputStream stream = EXECUTOR.getResponseStream();
                img = ImageIO.read(stream);
                stream.close();
                errcode = 2;
                ImageIO.write(img, "jpg", cachedImg);
                errcode = 0;
            }
            catch (Exception ex) {
                loading = false;
                return false;
            }
        }
        lastImg = img;
        img = createResizedImg(img);
        LB_IMG.setIcon(new ImageIcon(img));
        loading = false;
        return true;
    }

    private static boolean deleteDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
        return(path.delete());
    }
    
    @Override
    public void dispose() {
        stopPlayImage();
        deleteDirectory(cacheDir);
        super.dispose();
    }
    
    private void startPlayImage() {
        playing = true;
        setPlayPauseIcon();
        setBackNextButtonEnabled(false);
        getStatusBar().setProgress("Lejátszás...");
        PLAY_TIMER.start();
    }
    
    private void stopPlayImage() {
        playing = false;
        setPlayPauseIcon();
        setPlayPauseEnabled();
        getStatusBar().reset();
        PLAY_TIMER.stop();
        setBackNextEnabled();
    }
    
    private void loadImage() {
        loadImage(true);
    }
    
    private void setBackNextButtonEnabled(boolean enabled) {
        BT_BACK.setEnabled(enabled);
        BT_NEXT.setEnabled(enabled);
        BT_SFBACK.setEnabled(enabled);
        BT_SFNEXT.setEnabled(enabled);
        BT_FFBACK.setEnabled(enabled);
        BT_FFNEXT.setEnabled(enabled);
    }
    
    private void loadImage(final boolean setStatusBarAndButtons) {
        setBackNextButtonEnabled(false);
        if (setStatusBarAndButtons) getStatusBar().setProgress("Kép betöltése...");
        SwingWorker sw = new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {
                try {
                    lastLoadOk = showImage();
                }
                catch(Exception ex) {
                    lastLoadOk = false;
                }
                if (setStatusBarAndButtons) {
                    setBackNextEnabled();
                    setPlayPauseEnabled();
                    if (lastLoadOk) getStatusBar().reset();
                    else {
                        String err = "";
                        switch (errcode) {
                            case 1:
                                err = ": Hálózati hiba";
                                break;
                            case 2:
                                err = ": Írás jog hiba";
                                break;
                        }
                        getStatusBar().setError("A kép nem tölthető be" + err + '.');
                    }
                }
                return null;
            }
            
        };
        sw.execute();
    }
    
    private void initFrame() {
        setTitle("Képnézegető");
        LB_IMG.setOpaque(true);
        CB_QUALITYZOOM.setSelected(true);
        BT_FULLSIZE.setEnabled(false);
        setRefreshButtonEnabled(true);
        
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = c.NORTH;
        c.fill = c.BOTH;
        c.weightx = 1;
        c.gridx = 1;
        
        c.gridy = 1;
        c.weighty = 0;
        JToolBar tb = ToolbarUtils.createDefaultToolbar();
        tb.add(BT_PLAY_PAUSE);
        tb.add(BT_SFBACK);
        tb.add(BT_FFBACK);
        tb.add(BT_BACK);
        tb.add(BT_NEXT);
        tb.add(BT_FFNEXT);
        tb.add(BT_SFNEXT);
        tb.add(CB_INV_PLAY);
        tb.addSeparator();
        tb.add(BT_REFRESH);
        tb.addSeparator();
        tb.add(CB_QUALITYZOOM);
        tb.add(BT_ZOOM);
        tb.add(BT_UNZOOM);
        tb.add(BT_FULLSIZE);
        tb.add(CB_FITSIZE);
        add(tb, c);
        
        
        c.gridy = 2;
        c.weighty = 1;
        p = ScrollPaneUtils.createDefaultScrollPane(LB_IMG, 640, 480);
        p.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        p.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(p, c);
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBackNextActionListener();
        setPlayPauseActionListener();
        setCheckboxInverseActionListener();
        setScaleActionListener();
        setMouseScrollListener();
        
        pack();
        setLocationRelativeTo(this);
        
        setVisible(true);
        setWindowResizeListener();
        StatusBarUtils.setMinimumFrameWidth(tb, this);
    }
    
    private void setBackNextEnabled() {
        BT_BACK.setEnabled(index > 0);
        BT_NEXT.setEnabled(index < getLastIndex());
        BT_SFBACK.setEnabled(index > 0);
        BT_SFNEXT.setEnabled(index < getLastIndex());
        BT_FFBACK.setEnabled(index > 0);
        BT_FFNEXT.setEnabled(index < getLastIndex());
    }
    
    private int getLastIndex() {
        return IMGS.size() - 1;
    }
    
    private void setPlayPauseIcon() {
        BT_PLAY_PAUSE.setIcon(ResourceUtils.getToolbarIcon(playing ? "pause.png" : "play.png"));
        BT_PLAY_PAUSE.setToolTipText(playing ? "Szüneteltetés" : "Lejátszás");
    }
    
    private void setPlayPauseActionListener() {
        BT_PLAY_PAUSE.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                playPause();
            }
            
        });
    }
    
    private void playPause() {
        if (!playing) startPlayImage();
        else stopPlayImage();
    }
    
    private void setPlayPauseEnabled() {
        boolean enabled;
        if (!CB_INV_PLAY.isSelected() && index >= getLastIndex()) enabled = false;
        else if (CB_INV_PLAY.isSelected() && index <= 0) enabled = false;
        else enabled = true;
        BT_PLAY_PAUSE.setEnabled(enabled);
    }
    
    private void setCheckboxInverseActionListener() {
        CB_INV_PLAY.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                 setPlayPauseEnabled();
            }
            
        });
    }
    
    private void fastForward(boolean next) {
        if (next) index += 100;
        else index -= 100;
        checkIndex();
        loadImage();
    }
    
    public void setFiList(List<FileInfo> l) {
        IMGS.clear();
        IMGS.addAll(l);
        checkIndex();
        getStatusBar().reset();
        loadImage();
        setRefreshButtonEnabled(true);
    }
    
    private void setRefreshButtonEnabled(boolean b) {
        boolean test = true;
        if (!IMGS.isEmpty()) {
            Calendar cn = Calendar.getInstance();
            Calendar c = Calendar.getInstance();
            c.setTime(IMGS.get(0).getLastModified());
            test = c.get(Calendar.YEAR) == cn.get(Calendar.YEAR) && c.get(Calendar.MONTH) == cn.get(Calendar.MONTH) && c.get(Calendar.DAY_OF_MONTH) == cn.get(Calendar.DAY_OF_MONTH);
        }
        BT_REFRESH.setEnabled(b && test);
    }
    
    private void updateList() {
        setRefreshButtonEnabled(false);
        getStatusBar().setProgress("Képlista frissítése...");
        Main.dateChooser.updateFiList();
    }
    
    private void forward(boolean next) {
        if (next) index++;
        else index--;
        checkIndex();
        loadImage();
    }
    
    private void setBackNextActionListener() {
        BT_BACK.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                forward(false);
            }
            
        });
        BT_NEXT.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                forward(true);
            }
            
        });
        BT_SFBACK.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                index = 0;
                loadImage();
            }
            
        });
        BT_SFNEXT.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                index = getLastIndex();
                loadImage();
            }
            
        });
        BT_FFBACK.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fastForward(false);
            }
            
        });
        BT_FFNEXT.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fastForward(true);
            }
            
        });
        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                switch(e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        if (e.isControlDown()) {
                            fastForward(false);
                        }
                        else {
                            forward(false);
                        }
                        break;
                    case KeyEvent.VK_RIGHT:
                        if (e.isControlDown()) {
                            fastForward(true);
                        }
                        else {
                            forward(true);
                        }
                        break;
                    case KeyEvent.VK_SPACE:
                        playPause();
                        break;
                }
            }
            
        });
    }
    
    private void setZoomButtons() {
        boolean b = !CB_FITSIZE.isSelected();
        BT_ZOOM.setEnabled(b);
        BT_UNZOOM.setEnabled(b);
        BT_FULLSIZE.setEnabled(scale != 1);
    }
    
    private void scaleImg(boolean bigger) {
        // Save the previous coordinates
        double oldZoom = scale;
        Rectangle oldView = p.getViewport().getViewRect();
        Dimension is = LB_IMG.getSize();
        // resize the panel for the new zoom
        if (bigger) scale += 0.2;
        else scale -= 0.2;
        refreshCachedImg();
        is.width *= scale;
        is.height *= scale;
        // calculate the new view position
        if (scale == 1.2 && oldZoom < scale) {
            Point np = new Point();
            np.x = (int)(is.width/2.0 - oldView.width/2.0);
            np.y = (int)(is.height/2.0 - oldView.height/2.0);
            p.getViewport().setViewPosition(np);
        }
        else {
            Point newViewPos = new Point();
            newViewPos.x = (int)Math.max(0, (oldView.x + oldView.width / 2.0) * scale / oldZoom - oldView.width / 2.0);
            newViewPos.y = (int)Math.max(0, (oldView.y + oldView.height / 2.0) * scale / oldZoom - oldView.height / 2.0);
            p.getViewport().setViewPosition(newViewPos);
        }
    }
    
    private void setScaleActionListener() {
        CB_QUALITYZOOM.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                refreshCachedImg();
            }
            
        });
        CB_FITSIZE.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                refreshCachedImg();
                setZoomButtons();
                BT_FULLSIZE.setEnabled(!CB_FITSIZE.isSelected());
            }
            
        });
        BT_REFRESH.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                updateList();
            }
            
        });
        BT_ZOOM.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                scaleImg(true);
                setZoomButtons();
                BT_ZOOM.setEnabled(scale < 5);
            }
            
        });
        BT_UNZOOM.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                scaleImg(false);
                setZoomButtons();
                int sc = (int)((scale-0.2)*LB_IMG.getSize().getWidth());
                BT_UNZOOM.setEnabled(sc > 0);
            }
            
        });
        BT_FULLSIZE.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                scale = 1;
                refreshCachedImg();
                BT_FULLSIZE.setEnabled(false);
                BT_ZOOM.setEnabled(true);
                BT_UNZOOM.setEnabled(true);
            }
            
        });
    }
    
    private void setMouseScrollListener() {
        MouseAdapter ma = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                pm = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Dimension ps = p.getSize();
                Dimension is = LB_IMG.getSize();
                if (is.width > ps.width || is.height > ps.height) {
                    double x = pm.getX() - e.getX();
                    double y = pm.getY() - e.getY();
                    pm = e.getPoint();
                    JViewport vp = p.getViewport();
                    Point vpp = vp.getViewPosition();
                    vpp.setLocation(vpp.getX() + x, vpp.getY() + y);
                    if (vpp.getX() < 0) vpp.setLocation(0, vpp.getY());
                    if (vpp.getY() < 0) vpp.setLocation(vpp.getX(), 0);
                    if (is.width-1 < vpp.x+vp.getSize().width) vpp.setLocation(is.width-1-ps.width, vpp.getY());
                    if (is.height-1 < vpp.y+vp.getSize().height) vpp.setLocation(vpp.getX(), is.height-1-ps.height);
                    vp.setViewPosition(vpp);
                    p.repaint();
                }
            }
        };
        p.addMouseMotionListener(ma);
        p.addMouseListener(ma);
    }
    
    private void setWindowResizeListener() {
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                refreshCachedImg();
            }
            
        });
    }
    
}