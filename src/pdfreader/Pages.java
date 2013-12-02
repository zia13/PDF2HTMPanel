package pdfreader;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.List;
import javax.xml.bind.annotation.*;

/**
 *
 * @author Zia
 */

@XmlRootElement(name="Pages")
@XmlAccessorType(XmlAccessType.FIELD)
public class Pages {    

    
    @XmlElement(name="Page")
    private List<Page> pages; 
        
    
    void setPages(Page p)
    {
        pages.add(p);
    }
    
    List<Page> getPages()
    {
        return pages;
    }
}