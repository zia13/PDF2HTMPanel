package pdfreader;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Zia
 */
import java.util.List;   
import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Page implements Comparable<Page> {

    @XmlAttribute(name="no")
    private int pageNumber; 
    
    @XmlElement(name="Region")
    private List<Region> regions;
        
    void setPageNumber(int i)
    {
        pageNumber = i;
    }
    
    int getPageNumber()
    {
        return pageNumber;
    }
    
    void setRegions(List<Region> r)
    {
        regions = r;
    }
    
    List<Region> getRegions()
    {
        return regions;
    }

    public int compareTo(Page o) {
        Integer i = pageNumber;
        return i.compareTo(o.pageNumber);
    }

}