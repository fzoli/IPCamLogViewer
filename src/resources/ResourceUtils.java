package resources;

import com.thebuzzmedia.imgscalr.Scalr;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ResourceUtils {
    
    private static Map<String, BufferedImage> imgs = new HashMap<String, BufferedImage>();

    public static ImageIcon getToolbarIcon(String filename) {
        return new ImageIcon(getToolbarImage(filename));
    }
    
    private static BufferedImage getToolbarImage(String filename) {
        return getImage("toolbaricons/" + filename);
    }
    
    public static BufferedImage getBusyImage(String filename) {
        return getImage("busyicons/" + filename);
    }
    
    public static BufferedImage resizeSpeed(BufferedImage img, int width) {
        return resize(img, width, Scalr.Method.SPEED);
    }
    
    public static BufferedImage resizeQuality(BufferedImage img, int width) {
        return resize(img, width, Scalr.Method.QUALITY);
    }
    
    private static BufferedImage resize(BufferedImage img, int width, Scalr.Method method) {
        return Scalr.resize(img, method, width, 0, Scalr.OP_ANTIALIAS);
    }
    
    public static BufferedImage getImage(String path) {
        if (imgs.containsKey(path)) return imgs.get(path);
        try {
            BufferedImage img = ImageIO.read(ResourceUtils.class.getResource(path));
            imgs.put(path, img);
            return img;
        }
        catch (Exception ex) {
            return null;
        }
    }
    
}