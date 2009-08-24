package org.gdteam.appupdater4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.ImageIcon;

import org.gdteam.appupdater4j.model.ApplicationVersion;
import org.gdteam.appupdater4j.model.Version;
import org.gdteam.appupdater4j.os.macosx.ReflectiveApplication;
import org.gdteam.appupdater4j.wrapper.ApplicationLauncher;

public class Main implements UpdateControllerListener {
    
    private File propertyFile;
    private Properties properties = null;
    private UpdateManager updateManager = null;
    private ApplicationLauncher applicationLauncher;
    
    public void loadProperties(String[] args) throws Exception {
        if (args.length != 1) {
            StringBuilder message = new StringBuilder("Usage : java -jar appupdater4j.jar <propertyfile>\n");
            message.append("      - propertyfile : path to file which contains appupdater4j configuration properties");
            
            throw new Exception(message.toString());
        }
        
        //First get properties from classpath
        this.properties = new Properties();
        try {
            this.properties.load(this.getClass().getClassLoader().getResourceAsStream("appupdater4j.cfg.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        //Override properties
        Properties fileProperties = new Properties();
        
        FileInputStream fis = null;
        try {
            propertyFile = new File(args[0]);
            fis = new FileInputStream(propertyFile);
            fileProperties.load(fis);
        } catch (Exception e) {
            throw new Exception("Cannot open/read property file : " + args[0]);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                }
            }
        }
        
        Iterator fileKeySet = fileProperties.keySet().iterator();
        while (fileKeySet.hasNext()) {
            Object key = (Object) fileKeySet.next();
            this.properties.put(key, fileProperties.get(key));
        }
        
        //Configure dock icon
        String dockIconPath = this.properties.getProperty("dockicon.path");
        if (dockIconPath == null) {
            ReflectiveApplication.getApplication().setDockIconImage(new ImageIcon(this.getClass().getClassLoader().getResource("refresh-128.png")).getImage());
        } else  {
            ReflectiveApplication.getApplication().setDockIconImage(new ImageIcon(dockIconPath).getImage());
        }
    }
    
    public void configureUpdateManager() {
        this.updateManager = new UpdateManager();
        this.updateManager.configure(properties);
    }
    
    public void configureApplicationLauncher(){
        this.applicationLauncher = new ApplicationLauncher(new File(this.properties.getProperty("application.jar")), new String[0]);
    }
    
    /**
     * Install updates which are stored in specific folder
     * @return installed version
     */
    public Version installAutoUpdate() {
        this.updateManager.installAutoUpdate();
        
        Version installedVersion = Version.createVersion(this.updateManager.getCurrentVersion());
        
        this.properties.put("application.version", installedVersion.toString());
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(this.propertyFile);
            this.properties.store(out, "Generated by AppUpdater4J at " + new Date().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return installedVersion;
    }
    
    /**
     * Check for update and install update if necessary. Wait for the end of this method to start application
     */
    public void performModalCheck() {
        this.updateManager.performCheckForUpdate();
        
        if (this.updateManager.needUpdate()) {
            UpdateController controller = UpdateControllerFactory.getUpdateController((String) this.properties.get("dialog.class"));
            controller.addUpdateControllerListener(this);
            
            this.updateManager.addUpdateListener(controller);
            
            controller.setVersionToInstall(this.updateManager.getVersionToInstallList());
            controller.displayController(this.properties.getProperty("dialog.title"));
        }
        
    }
    
    public void runApplication() throws Exception {
        this.applicationLauncher.extractManifestInfo();
        this.applicationLauncher.run();
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        
        Main application = new Main();
        try {
            application.loadProperties(args);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
        
        application.configureUpdateManager();
        Version installedVersion = application.installAutoUpdate();
        
        if (installedVersion == null) {
            //No autoupdate
            application.performModalCheck();
        }
        
        //Start application
        try {
            application.runApplication();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void canStartWrappedApplication(UpdateController source) {
        System.out.println("Done...");
    }

    public void startUpdate(UpdateController source, List<ApplicationVersion> versionList) {
        this.updateManager.startUpdate(versionList);
    }

}
