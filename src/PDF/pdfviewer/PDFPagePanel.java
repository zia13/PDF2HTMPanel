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
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.apache.pdfbox.pdfviewer.PageDrawer;

/**
 * This is a simple JPanel that can be used to display a PDF page.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class PDFPagePanel extends JPanel
{
    private int pointCount =0;
    private Point points[]  = new Point[100];
    private Point points2[] = new Point[100];
    private Point start = new Point();
    private Point end   = new Point();
    Rectangle rect = new Rectangle();
    private static final long serialVersionUID = -4629033339560890669L;
    
    private PDPage page;
    public PageDrawer drawer = null;
    private Dimension pageDimension = null;
    private Dimension drawDimension = null;
    public String filenamee;
    public int currentpagee;
    public Rectangle[][] pdfpagepanelrec;
    
    public PDFPagePanel(String filename,int currentpage) throws IOException{
        filenamee = filename;
        currentpagee = currentpage;
        addMouseMotionListener( new MouseMotionAdapter()
        {
            @Override
            public void mouseDragged(MouseEvent ev)
            {
                end = ev.getPoint();
                rect.setFrameFromDiagonal(start, end);
                repaint();
            }//end mouse drag
            
            @Override
            public void mouseMoved(MouseEvent ev)
            {
                ReaderBottomPanel readerBottomPanel = new ReaderBottomPanel();
                readerBottomPanel.statusLabel.setText(ev.getX()+","+ev.getY());
                
            }
        });
        addMouseListener(new MouseAdapter()
        {
            int MouseClickx;
            int MouseClicky;
            int MouseExitx;
            int MouseExity;
           
            @Override
            public void mousePressed(MouseEvent e)
            {
                MouseClickx = e.getX();
                MouseClicky = e.getY();
                start = e.getPoint();
            }
 
            @Override
            public void mouseReleased(MouseEvent ev)
            {
                MouseExitx = ev.getX();
                MouseExity = ev.getY();
                points[pointCount] = start;
                points2[pointCount] = ev.getPoint();
                pointCount++;
                rect.setFrameFromDiagonal(start, start);
                repaint();                
                System.out.println("Mouse Entered At:" + MouseClickx + "," + MouseClicky);
                System.out.println("Mouse Exited At:" + MouseExitx + "," + MouseExity);
                
            }
        });
        drawer = new PageDrawer();
    }
    
    public void getSelectedRegionTextValue(){
        
    }
        
    public void setPage( PDPage pdfPage ){
        page = pdfPage;
        PDRectangle cropBox = page.findCropBox();
        drawDimension = cropBox.createDimension();
        int rotation = page.findRotation();
        if (rotation == 90 || rotation == 270)
        {
            pageDimension = new Dimension(drawDimension.height, drawDimension.width);
        }
        else
        {
            pageDimension = drawDimension;
        }
        setSize( pageDimension );
//        Dimension dim = new Dimension(1300, 800);
//        setSize( dim );
        setBackground( java.awt.Color.white );
    }
    
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        // Draw line being dragged.
        g2.setPaint(Color.red);
        
        g2.draw(rect);
        // Draw lines between points in arrays.
        g2.setPaint(Color.pink);
        Rectangle r = new Rectangle();
        for (int i =0; i < pointCount; i++)
        {
            r.setFrameFromDiagonal(points[i], points2[i]);
            g2.fill(r);
        }
        for(int i=0;pdfpagepanelrec != null && i<pdfpagepanelrec.length;i++)
        {
            for(int j = 0;j<pdfpagepanelrec[i].length;j++)
            {                
                g2.setPaint(Color.RED);        
                g2.draw(pdfpagepanelrec[i][j]);
            }
        }
        try {
            drawer.drawPage( g2, page, drawDimension );
        } catch (IOException ex) {
            Logger.getLogger(PDFPagePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
