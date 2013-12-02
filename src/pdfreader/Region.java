package pdfreader;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Zia
 */
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Region implements Comparable<Region> {

    @XmlAttribute(name = "x")
    private int x;
    
    @XmlAttribute(name = "y")
    private int y;
    
    @XmlAttribute(name = "width")
    private int width;
    
    @XmlAttribute(name = "height")
    private int height;
    
    @XmlAttribute(name = "type")
    private String type;
    
    @XmlElement(name="HtmlContent")
    private String htmlContent;
    
    void setX(int i){
        x = i;
    }
    
    int getX(){
        return x;
    }
    
    void setY(int i)
    {
        y = i;
    }
    int getY()
    {
        return y;
    }
    
    void setWidth(int w)
    {
        width = w;
    }
    int getWidth()
    {
        return width;
    }
    
    void setHeight(int h)
    {
        height = h;
    }
    int getHeight()
    {
        return height;
    }
    void setType(String s )
    {
        type = s;
    }
    String getType()
    {
        return type;
    }
    
    void setHtmlContent(String r)
    {
        htmlContent = r;
    }
    
    String getHtmlContent()
    {
        return htmlContent;
    }

    public int compareTo(Region o) {
        Integer i=y;
        return i.compareTo(o.y);
    }
}
