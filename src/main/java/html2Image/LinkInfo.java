package html2Image;

import java.awt.Rectangle;
import java.util.List;
import java.util.Map;

public class LinkInfo {
    private Map<String, String> attributes;
    private List<Rectangle> bounds;

    public LinkInfo(Map<String, String> attributes, List<Rectangle> bounds) {
        this.attributes = attributes;
        this.bounds = bounds;
    }

    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    public List<Rectangle> getBounds() {
        return this.bounds;
    }
}
