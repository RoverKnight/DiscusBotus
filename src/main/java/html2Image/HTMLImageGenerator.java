//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package html2Image;

import html2Image.FormatNameUtil;
import html2Image.SynchronousHTMLEditorKit;
import html2Image.LinkHarvester;
import html2Image.LinkInfo;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import javax.imageio.ImageIO;
import javax.swing.JEditorPane;

public class HTMLImageGenerator {
    private JEditorPane editorPane = this.createJEditorPane();
    static final Dimension DEFAULT_SIZE = new Dimension(800, 800);

    public HTMLImageGenerator() {
    }

    public ComponentOrientation getOrientation() {
        return this.editorPane.getComponentOrientation();
    }

    public void setOrientation(ComponentOrientation orientation) {
        this.editorPane.setComponentOrientation(orientation);
    }

    public Dimension getSize() {
        return this.editorPane.getSize();
    }

    public void setSize(Dimension dimension) {
        this.editorPane.setSize(dimension);
    }

    public void loadUrl(URL url) {
        try {
            this.editorPane.setPage(url);
        } catch (IOException var3) {
            throw new RuntimeException(String.format("Exception while loading %s", url), var3);
        }
    }

    public void loadUrl(String url) {
        try {
            this.editorPane.setPage(url);
        } catch (IOException var3) {
            throw new RuntimeException(String.format("Exception while loading %s", url), var3);
        }
    }

    public void loadHtml(String html) {
        this.editorPane.setText(html);
        this.onDocumentLoad();
    }

    public String getLinksMapMarkup(String mapName) {
        StringBuilder markup = new StringBuilder();
        markup.append("<map name=\"").append(mapName).append("\">\n");
        Iterator i$ = this.getLinks().iterator();

        while(i$.hasNext()) {
            LinkInfo link = (LinkInfo)i$.next();
            List<Rectangle> bounds = link.getBounds();
            i$ = bounds.iterator();

            while(i$.hasNext()) {
                Rectangle bound = (Rectangle)i$.next();
                int x1 = (int)bound.getX();
                int y1 = (int)bound.getY();
                int x2 = (int)((double)x1 + bound.getWidth());
                int y2 = (int)((double)y1 + bound.getHeight());
                markup.append(String.format("<area coords=\"%s,%s,%s,%s\" shape=\"rect\"", x1, y1, x2, y2));
                i$ = link.getAttributes().entrySet().iterator();

                while(i$.hasNext()) {
                    Entry<String, String> entry = (Entry)i$.next();
                    String attName = (String)entry.getKey();
                    String value = (String)entry.getValue();
                    markup.append(" ").append(attName).append("=\"").append(value.replace("\"", "&quot;")).append("\"");
                }

                markup.append(">\n");
            }
        }

        markup.append("</map>\n");
        return markup.toString();
    }

    public List<LinkInfo> getLinks() {
        html2Image.LinkHarvester harvester = new html2Image.LinkHarvester(this.editorPane);
        return harvester.getLinks();
    }

    public void saveAsHtmlWithMap(String file, String imageUrl) {
        this.saveAsHtmlWithMap(new File(file), imageUrl);
    }

    public void saveAsHtmlWithMap(File file, String imageUrl) {
        FileWriter writer = null;

        try {
            writer = new FileWriter(file);
            writer.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
            writer.append("<html>\n<head></head>\n");
            writer.append("<body style=\"margin: 0; padding: 0; text-align: center;\">\n");
            String htmlMap = this.getLinksMapMarkup("map");
            writer.write(htmlMap);
            writer.append("<img border=\"0\" usemap=\"#map\" src=\"");
            writer.append(imageUrl);
            writer.append("\"/>\n");
            writer.append("</body>\n</html>");
        } catch (IOException var12) {
            throw new RuntimeException(String.format("Exception while saving '%s' html file", file), var12);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException var11) {
                }
            }

        }

    }

    public void saveAsImage(String file) {
        this.saveAsImage(new File(file));
    }

    public void saveAsImage(File file) {
        BufferedImage img = this.getBufferedImage();

        try {
            String formatName = FormatNameUtil.formatForFilename(file.getName());
            ImageIO.write(img, formatName, file);
        } catch (IOException var4) {
            throw new RuntimeException(String.format("Exception while saving '%s' image", file), var4);
        }
    }

    protected void onDocumentLoad() {
    }

    public Dimension getDefaultSize() {
        return DEFAULT_SIZE;
    }

    public BufferedImage getBufferedImage() {
        Dimension prefSize = this.editorPane.getPreferredSize();
        BufferedImage img = new BufferedImage(prefSize.width, this.editorPane.getPreferredSize().height, 2);
        Graphics graphics = img.getGraphics();
        this.editorPane.setSize(prefSize);
        this.editorPane.paint(graphics);
        return img;
    }

    protected JEditorPane createJEditorPane() {
        JEditorPane editorPane = new JEditorPane();
        editorPane.setSize(this.getDefaultSize());
        editorPane.setEditable(false);
        SynchronousHTMLEditorKit kit = new SynchronousHTMLEditorKit();
        editorPane.setEditorKitForContentType("text/html", kit);
        editorPane.setContentType("text/html");
        editorPane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("page")) {
                    HTMLImageGenerator.this.onDocumentLoad();
                }

            }
        });
        return editorPane;
    }
}
