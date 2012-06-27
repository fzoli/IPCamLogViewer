package frame;

import fileinfo.FileInfoParser;
import frame.statusbar.StatusBarFrame;
import frame.tabindex.IndexedFocusTraversalPolicy;
import http.HttpExecutor;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;

public class UserInfoAsker extends StatusBarFrame {

    private int cancelled = 0;
    private boolean checking = false, disposed = false;
    private final HttpExecutor executor;
    private static final Pattern pattUsr = Pattern.compile("^[a-z0-9_-]{3,15}$");
    private static final Pattern pattPwd = Pattern.compile("^[a-z0-9]{6,18}$");
    private SwingWorker checker;
    private final JButton BT = new JButton("Bejelentkezés");
    private final JTextField TF_NAME = new JTextField(10);
    private final JPasswordField TF_PASSWD = new JPasswordField();
    
    public UserInfoAsker(HttpExecutor executor) {
        this.executor = executor;
        initFrame();
    }
    
    private boolean isUsrValid() {
        if (getUserName() == null) return false;
        Matcher matcher = pattUsr.matcher(getUserName());
        return matcher.matches();
    }
    
    private boolean isPwdValid() {
        if (getPassword() == null) return false;
        Matcher matcher = pattPwd.matcher(getPassword());
        return matcher.matches();
    }
    
    public String getUserName() {
        return isDisposed() ? null : TF_NAME.getText();
    }
    
    public String getPassword() {
        return isDisposed() ? null : new String(TF_PASSWD.getPassword());
    }
    
    public boolean waitValidInfo() {
        while (!isValidated() && !isDisposed()) {
            try {
                Thread.sleep(50);
            } catch(Exception ex) {}
        }
        return isValidated();
    }

    public boolean isDisposed() {
        return disposed;
    }
    
    private boolean isValidated() {
        if (executor != null) return executor.getStatus() == FileInfoParser.OK;
        return false;
    }
    
    private void initExecutor(String user, String password) {
        executor.setUsrAndPswd(user, password);
    }
    
    @Override
    public void dispose() {
        disposed = true;
        super.dispose();
    }
    
    private void setTabIndex() {
        ArrayList<Component> tabIndex = new ArrayList<Component>();
        tabIndex.add(TF_NAME);
        tabIndex.add(TF_PASSWD);
        IndexedFocusTraversalPolicy.apply(this, tabIndex);
    }
    
    private void initFrame() {
        setTitle("Bejelentkezés");
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTabIndex();
        
        c.gridx = 1;
        c.weightx = 0;
        c.anchor = c.LINE_END;
        c.insets = new Insets(0, 5, 0, 5);
        
        c.gridy = 1;
        add(new JLabel("Név:"), c);
        
        c.gridy = 2;
        add(new JLabel("Jelszó:"), c);
        
        c.gridx = 2;
        c.weightx = 1;
        c.fill = c.BOTH;
        
        c.gridy = 1;
        c.insets = new Insets(5, 0, 5, 5);
        add(TF_NAME, c);
        
        c.gridy = 2;
        c.insets = new Insets(0, 0, 5, 5);
        add(TF_PASSWD, c);
        
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 2;
        c.fill = c.NONE;
        c.anchor = c.CENTER;
        c.insets = new Insets(5, 5, 5, 5);
        add(BT, c);
        
        setActionListener();
        
        pack();
        setResizable(false);
        setLocationRelativeTo(this);
        setVisible(true);
    }
    
    private void setLabelsEnabled(boolean b) {
        TF_NAME.setEnabled(b);
        TF_PASSWD.setEnabled(b);
    }
    
    private void startCheck() {
        if (!isUsrValid()) {
            getStatusBar().setError("Névformátum hiba!");
            wroteError = true;
            return;
        }
        if (!isPwdValid()) {
            getStatusBar().setError("Jelszóformátum hiba!");
            wroteError = true;
            return;
        }
        cancelled++;
        checking = true;
        BT.setText("Megszakítás");
        BT.requestFocus();
        setLabelsEnabled(false);
        initExecutor(getUserName(), getPassword());
        getStatusBar().setProgress("Adatok ellenőrzése...");
        checker = new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {
                try {
                    int tmp = cancelled;
                    executor.getResponse();
                    if (tmp == cancelled) {
                        if (executor.getStatus() == FileInfoParser.UNAUTHORIZED) {
                            getStatusBar().setError("Sikertelen azonosítás!");
                        }
                        else if (executor.getStatus() != FileInfoParser.OK) {
                            getStatusBar().setError("Kapcsolódás hiba!");
                        }
                        else {
                            getStatusBar().reset();
                        }
                        stopCheck();
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }
            
        };
        checker.execute();
    }
    
    private void interruptCheck() {
        checker.cancel(true);
        cancelled++;
        getStatusBar().reset();
        stopCheck();
    }
    
    private void stopCheck() {
        BT.setText("Bejelentkezés");
        TF_NAME.requestFocus();
        setLabelsEnabled(true);
        checking = false;
    }
    
    boolean wroteError = false;
    
    private void setActionListener() {
        ActionListener al = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (checking) interruptCheck();
                else startCheck();
            }
            
        };
        TF_NAME.addActionListener(al);
        TF_PASSWD.addActionListener(al);
        BT.addActionListener(al);
        KeyListener kl = new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (wroteError) {
                    if ((e.getSource() == TF_NAME && !isUsrValid()) || (e.getSource() == TF_PASSWD && !isPwdValid())) {
                        wroteError = false;
                        getStatusBar().reset();
                    }
                }
            }
            
        };
        TF_NAME.addKeyListener(kl);
        TF_PASSWD.addKeyListener(kl);
    }
    
}