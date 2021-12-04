package html2Image;

import html2Image.LinkInfo;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTML.Tag;

public class LinkHarvester {
    private final JTextComponent textComponent;
    private final List<LinkInfo> links = new ArrayList();

    public LinkHarvester(JEditorPane textComponent) {
        this.textComponent = textComponent;
        this.harvestElement(textComponent.getDocument().getDefaultRootElement());
    }

    public List<LinkInfo> getLinks() {
        return this.links;
    }

    private void harvestElement(Element element) {
        if (element != null) {
            AttributeSet attributes = element.getAttributes();
            Enumeration attributeNames = attributes.getAttributeNames();

            while(attributeNames.hasMoreElements()) {
                Object key = attributeNames.nextElement();
                if (Tag.A.equals(key)) {
                    Map<String, String> linkAttributes = this.harvestAttributes(element);
                    List<Rectangle> bounds = this.harvestBounds(element);
                    if (!linkAttributes.isEmpty() && !bounds.isEmpty()) {
                        this.links.add(new LinkInfo(linkAttributes, bounds));
                    }
                }
            }

            for(int i = 0; i < element.getElementCount(); ++i) {
                Element child = element.getElement(i);
                this.harvestElement(child);
            }

        }
    }

    private Map<String, String> harvestAttributes(Element element) {
        Object value = element.getAttributes().getAttribute(Tag.A);
        if (value instanceof SimpleAttributeSet) {
            SimpleAttributeSet attributeSet = (SimpleAttributeSet)value;
            Map<String, String> result = new HashMap();
            this.addAttribute(attributeSet, result, Attribute.HREF);
            this.addAttribute(attributeSet, result, Attribute.TARGET);
            this.addAttribute(attributeSet, result, Attribute.TITLE);
            this.addAttribute(attributeSet, result, Attribute.CLASS);
            this.addAttribute(attributeSet, result, "tabindex");
            this.addAttribute(attributeSet, result, "dir");
            this.addAttribute(attributeSet, result, "lang");
            this.addAttribute(attributeSet, result, "accesskey");
            this.addAttribute(attributeSet, result, "onblur");
            this.addAttribute(attributeSet, result, "onclick");
            this.addAttribute(attributeSet, result, "ondblclick");
            this.addAttribute(attributeSet, result, "onfocus");
            this.addAttribute(attributeSet, result, "onmousedown");
            this.addAttribute(attributeSet, result, "onmousemove");
            this.addAttribute(attributeSet, result, "onmouseout");
            this.addAttribute(attributeSet, result, "onmouseover");
            this.addAttribute(attributeSet, result, "onmouseup");
            this.addAttribute(attributeSet, result, "onkeydown");
            this.addAttribute(attributeSet, result, "onkeypress");
            this.addAttribute(attributeSet, result, "onkeyup");
            return result;
        } else {
            return Collections.emptyMap();
        }
    }

    private void addAttribute(SimpleAttributeSet attributeSet, Map<String, String> result, Object attribute) {
        String attName = attribute.toString();
        String attValue = (String)attributeSet.getAttribute(attribute);
        if (attValue != null && !attValue.equals("")) {
            result.put(attName, attValue);
        }

    }

    private List<Rectangle> harvestBounds(Element element) {
        ArrayList boundsList = new ArrayList();

        try {
            int startOffset = element.getStartOffset();
            int endOffset = element.getEndOffset();
            Rectangle lastBounds = null;

            for(int i = startOffset; i <= endOffset; ++i) {
                Rectangle bounds = this.textComponent.modelToView(i);
                if (bounds != null) {
                    if (lastBounds == null) {
                        lastBounds = bounds;
                    } else if (bounds.getY() == lastBounds.getY()) {
                        lastBounds = lastBounds.union(bounds);
                    } else {
                        if (lastBounds.getWidth() > 1.0D && lastBounds.getHeight() > 1.0D) {
                            boundsList.add(lastBounds);
                        }

                        lastBounds = null;
                    }
                }
            }

            if (lastBounds != null && lastBounds.getWidth() > 1.0D && lastBounds.getHeight() > 1.0D) {
                boundsList.add(lastBounds);
            }

            return boundsList;
        } catch (BadLocationException var8) {
            throw new RuntimeException("Got BadLocationException", var8);
        }
    }
}
