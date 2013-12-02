
//This pdfbox source Class is Edited by Zia

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package PDF.pdfviewer;

import org.apache.pdfbox.pdmodel.PDPage;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.apache.pdfbox.pdfviewer.PageDrawer;
import pdfreader.PDFReader;

public class PageWrapper implements MouseListener
{
    private JPanel pageWrapper = new JPanel();
    public PDFPagePanel pagePanel = null;
    private pdfreader.PDFReader reader1 = null;             //For PDFReader Class
    public PageDrawer pdrawer;
    public int currentpage;
    
    private static final int SPACE_AROUND_DOCUMENT = 20;
    public String filename;
    JPopupMenu Pmenu;
    JMenuItem paragraph;
    JMenuItem table;
    JMenuItem list;
    JMenuItem image;
    JMenuItem textWithLineBreak;
    public static int mouseClickx;
    public static int mouseClicky;
    public static int mouseExitx;
    public static int mouseExity;
   
    public PageWrapper( PDFReader aReader ) throws IOException{
        reader1 = aReader;
         //<editor-fold defaultstate="collapsed" desc="JPopupMenu and functions of all its items">
        Pmenu = new JPopupMenu();
        paragraph = new JMenuItem("paragraph");
        paragraph.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                reader1.setTag("paragraph");
            }
        });
        Pmenu.add(paragraph);
        table = new JMenuItem("table");
        table.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                reader1.setTag("table");
                //reader1.getTableSinglePixel();        // This is called to determine the Table.
            }
        });
        Pmenu.add(table);
        list = new JMenuItem("list");
        list.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                reader1.setTag("list");
            }
        });
        Pmenu.add(list);
        image = new JMenuItem("image");
        image.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                reader1.setTag("image");
            }
        });
        Pmenu.add(image);
        textWithLineBreak = new JMenuItem("text with line break");
        textWithLineBreak.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                reader1.setTag("text_with_line_break");
            }
        });
        Pmenu.add(textWithLineBreak);
        //</editor-fold>
        
        pagePanel = new PDFPagePanel(filename,currentpage);
//        pagePanel.setSize(1000, 700);
//        Dimension dl = new Dimension(pagePanel.getSize().width,pagePanel.getSize().height);
//        pagePanel.setPreferredSize(dl);
        pageWrapper.setLayout( null );
        pageWrapper.add( pagePanel );
        pagePanel.setLocation( SPACE_AROUND_DOCUMENT, SPACE_AROUND_DOCUMENT );
        pageWrapper.setBorder( javax.swing.border.LineBorder.createBlackLineBorder() );
        pagePanel.addMouseListener(this);
//        DrawRectpanel drp = new DrawRectpanel();
//        pagePanel.add(drp);
        pdrawer = pagePanel.drawer;
//        pageWrapper.add(drp);
//        pagePanel.addMouseMotionListener( this );
    }
    
    public PageWrapper(){
        
    }

    public void displayPage( PDPage page ){
        pagePanel.setPage( page );
        Dimension d = pagePanel.getSize();
        pagePanel.setPreferredSize( d );
        d.width+=(SPACE_AROUND_DOCUMENT*2);
        d.height+=(SPACE_AROUND_DOCUMENT*2);

        pageWrapper.setPreferredSize( d );
//        pageWrapper.setPreferredSize( dimens );
        pageWrapper.validate();
    }

    public JPanel getPanel(){
        return pageWrapper;
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        
        System.out.println(e.getX()+"  "+e.getY());
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseClickx = e.getX();
        mouseClicky = e.getY();
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    Rectangle rectangle = new Rectangle(48,223,522,155);
    
    @Override
    public void mouseReleased(MouseEvent e) {
        mouseExitx = e.getX();
        mouseExity = e.getY();
        Rectangle rect = new Rectangle(mouseClickx,mouseClicky,Math.abs(mouseExitx-mouseClickx),Math.abs(mouseExity-mouseClicky));
        //System.out.println(rect.x+","+rect.y+","+rect.width+","+rect.height);
        //send the selected region rectangle to the pdfreader
        reader1.setRectangle(rect);
        Pmenu.show(e.getComponent(), e.getX(), e.getY());
        // Get the table with the cells and send it to pdfpagepanel to draw the table.............
         /*/<editor-fold defaultstate="collapsed" desc="comment">
        int[] regioon = null;
        ExtractTextByArea ETB = new ExtractTextByArea();
        try {
            try {
                regioon = ETB.ExtractTextByArea(filename,rect,currentpage,0);
            } catch (CryptographyException ex) {
                Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
        } 
        int positionOfRowStart[] = ETB.getPointOfRowStart();
        int numberofRows = ETB.returnNumberofRows();
        int numberofColumns = ETB.returnNumberofColumns();
        
        
        
        ExtractTextByColumn ETBC = new ExtractTextByColumn();
        Rectangle[] ColumnWiseRect = new Rectangle[(numberofColumns-1)*numberofRows];
        int q = 0;
        for(int j = 0;j<=numberofRows-1;j++)
        {            
            for(int i = 0;i<numberofColumns-1 ;i++)
            {
                // when it is first row and first column [0,0]
                if(i ==0 && j==0)
                {
                    ColumnWiseRect[q] = new Rectangle(rect.x,rect.y,(regioon[i+1]*5-rect.x)-2,positionOfRowStart[j]-rect.y);
//                    System.out.println("Cell["+j+"]["+i+"]:--"+ColumnWiseRect.x+","+ColumnWiseRect.y+","+ColumnWiseRect.width+","+ColumnWiseRect.height);
                    try {
                        ETBC.ExtractTextByArea(filename,ColumnWiseRect[q],currentpage,j,i);
                    } catch (IOException ex) {
                        Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (CryptographyException ex) {
                        Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                // when j = 0 it means the first row "This is for first row but not first column :) [0,1] [0,2] [0,3] .............
                else if(j == 0 && i>0)
                {
                    ColumnWiseRect[q] = new Rectangle(regioon[i]*5-2,rect.y,(regioon[i+1]*5-regioon[i]*5),positionOfRowStart[j]-rect.y);
//                    System.out.println("Cell["+j+"]["+i+"]:--"+ColumnWiseRect.x+","+ColumnWiseRect.y+","+ColumnWiseRect.width+","+ColumnWiseRect.height);
                    try {
                        ETBC.ExtractTextByArea(filename,ColumnWiseRect[q],currentpage,j,i);
                    } catch (IOException ex) {
                        Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (CryptographyException ex) {
                        Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                // when i = 0 it means the first column "this is for first column but not first row :) [1,0] [2,0] [3,0] ........
                else if(i == 0 && j>0)
                {
                    ColumnWiseRect[q] = new Rectangle(rect.x,positionOfRowStart[j-1]+2,(regioon[i+1]*5-rect.x),(positionOfRowStart[j]-positionOfRowStart[j-1]));
//                    System.out.println("Cell["+j+"]["+i+"]:--"+ColumnWiseRect.x+","+ColumnWiseRect.y+","+ColumnWiseRect.width+","+ColumnWiseRect.height);
                    try {
                        ETBC.ExtractTextByArea(filename,ColumnWiseRect[q],currentpage,j,i);
                    } catch (IOException ex) {
                        Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (CryptographyException ex) {
                        Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                // this is for all others but first row and first column [1,1] [1,2] [1,3]........ [2,1] [2,2] [2,3]..........  
                else if(i>0 && j>0)
                {
                    ColumnWiseRect[q] = new Rectangle(regioon[i]*5-2,positionOfRowStart[j-1]+2,(regioon[i+1]*5-regioon[i]*5),(positionOfRowStart[j]-positionOfRowStart[j-1]));
//                    System.out.println("Cell["+j+"]["+i+"]:--"+ColumnWiseRect.x+","+ColumnWiseRect.y+","+ColumnWiseRect.width+","+ColumnWiseRect.height);
                    try {
                        ETBC.ExtractTextByArea(filename,ColumnWiseRect[q],currentpage,j,i);
                    } catch (IOException ex) {
                        Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (CryptographyException ex) {
                        Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                q++;
            }
        }
        System.out.println("Number of cells"+ColumnWiseRect.length);
        pagePanel.pdfpagepanelrec = ColumnWiseRect;
        try {
            ETBC.getTable(numberofRows,numberofColumns);
        } catch (IOException ex) {
            Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("Mouse Entered At:" + mouseClickx + "," + mouseClicky);
        System.out.println("Mouse Exited At:" + mouseExitx + "," + mouseExity);
        * /
        
        */
        //</editor-fold>
    }

    @Override
    public void mouseEntered(MouseEvent e) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseExited(MouseEvent e) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setRectangle(Rectangle x){
        rectangle = x;
    }

    public Rectangle getRectangle() {
//        System.out.println(rectangle.x+","+rectangle.y+","+rectangle.width+","+rectangle.height);
        return rectangle;
    }

}
