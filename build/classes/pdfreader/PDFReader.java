package pdfreader;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import Examples.pdmodel.CreateBookmarks;
import PDF.exceptions.COSVisitorException;
import PDF.exceptions.CryptographyException;
import PDF.exceptions.InvalidPasswordException;
import PDF.pdfviewer.PDFPagePanel;
import PDF.pdfviewer.PageWrapper;
import PDF.pdfviewer.ReaderBottomPanel;
import PDF.pdmodel.PDDocument;
import PDF.pdmodel.PDPage;
import PDF.util.ExtensionFileFilter;
import PDF.util.ImageIOUtil;
import PDF.util.PDFTextStripperByArea;
import PDF.util.TextPosition;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;


/**
 * An application to read PDF documents.  This will provide Acrobat Reader like
 * funtionality.
 *
 * @author <a href="ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.5 $
 */
public class PDFReader extends JFrame
{
    private File currentDir=new File("G:/From Fuad sir DOCX to PDF");
    private javax.swing.JMenuItem saveAsImageMenuItem;//Convert a PDF Page into an image
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem createBookMark;
    private javax.swing.JMenuItem saveAsHtml;
    private javax.swing.JMenuItem toHtml;    
    private javax.swing.JMenuItem saveAsList;
    private javax.swing.JMenuItem saveAsTable;    
    private javax.swing.JMenuItem SaveRegionText;    
    private javax.swing.JMenuItem SaveRegionTextallOnce;
    private javax.swing.JMenuItem listextract;
    private javax.swing.JMenuItem ExtractImageMenuItem;
    private javax.swing.JMenuItem printMenuItem;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JMenuItem nextPageItem;
    private javax.swing.JMenuItem previousPageItem;
    private JPanel documentPanel = new JPanel();
    private ReaderBottomPanel bottomStatusPanel = new ReaderBottomPanel();
    
    List<TextPosition>[] textsofAllPages;   
    
    private PDDocument document = null;
    private List<PDPage> pages= null;
    List<TaggedRegion> listOfSelectedRegionInRectangle;
    HashMap<Integer, Rectangle> pageAndSelectedRectangle = new HashMap<>();
    SortedMap<Integer, Rectangle> pageAndRectangleSelectedInIt = new TreeMap();
    
    private int currentPage = 0;
    private int numberOfPages = 0;
    private String currentFilename = null;
    private static final String PASSWORD = "-password";
    private static final String NONSEQ = "-nonSeq";
    private static boolean useNonSeqParser = false; 
    public PDFPagePanel pdfPagePanel;
    
    static String name;
    public Rectangle rec;    
    public int pageNumberr;
    String nameOfFile;
    Rectangle rectangle; 
    String lastTag;
    TaggedRegion tR;
    JScrollPane documentScroller;
    
    public PDFReader(){
        initComponents();
    }
   
    public File newFile(String file) {  
        File targetFile = new File(file);  
        return targetFile;  
    }
    
    private void initComponents(){
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();        
        createBookMark = new javax.swing.JMenuItem();
        saveAsHtml = new javax.swing.JMenuItem();        
        toHtml = new javax.swing.JMenuItem();
        saveAsList = new javax.swing.JMenuItem();        
        saveAsTable = new javax.swing.JMenuItem();
        ExtractImageMenuItem = new javax.swing.JMenuItem();
        saveAsImageMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        printMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        nextPageItem = new javax.swing.JMenuItem();
        previousPageItem = new javax.swing.JMenuItem();
        SaveRegionText = new javax.swing.JMenuItem();
        SaveRegionTextallOnce = new javax.swing.JMenuItem();
        listextract = new javax.swing.JMenuItem();
        setTitle("Apurba PDF Processor");
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                exitApplication();
            }
        });

        documentScroller = new javax.swing.JScrollPane();
        documentScroller.setViewportView( documentPanel );

        getContentPane().add( documentScroller, java.awt.BorderLayout.CENTER );
        getContentPane().add( bottomStatusPanel, java.awt.BorderLayout.SOUTH );

        fileMenu.setText("File");
        openMenuItem.setText("Open");
//        openMenuItem.setAccelerator(KeyStroke.getKeyStroke('o'));
        openMenuItem.setToolTipText("Open PDF file");
        openMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                openMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openMenuItem);
        
        //<editor-fold defaultstate="collapsed" desc="comment">
        ExtractImageMenuItem.setText("Extract Image");
        ExtractImageMenuItem.setToolTipText("Extract Image");
        ExtractImageMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                ExtImages(evt);
            }
        });
        
//                fileMenu.add(ExtractImageMenuItem);
        
        createBookMark.setText("Create BookMark");
        createBookMark.setToolTipText("Create BookMark");
        createBookMark.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                try {
                    createbookmarks(evt);
                } catch (        IOException | COSVisitorException ex) {
                    Logger.getLogger(PDFReader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        //
        //        fileMenu.add(createBookMark);
        
        saveAsHtml.setText("Save as HTML");
        saveAsHtml.setToolTipText("Save as HTML");
        saveAsHtml.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                try {
                    converttoHTML(evt);
                } catch (IOException ex) {
                    Logger.getLogger(PDFReader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        //        fileMenu.add(saveAsHtml);
        //</editor-fold>
        
        toHtml.setText("Save as Regular HTML");
        toHtml.setToolTipText("Save as Regular HTML");
//        openMenuItem.setAccelerator(KeyStroke.getKeyStroke('h'));
        toHtml.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                convertToStandardHtml();
            }
        });        
        fileMenu.add(toHtml);
        
        //<editor-fold defaultstate="collapsed" desc="comment">
        SaveRegionText.setText("Region Text");
        SaveRegionText.setToolTipText("Region Text");
        SaveRegionText.setAccelerator(KeyStroke.getKeyStroke('r'));
        SaveRegionText.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                try {
                    regionText(evt);
                } catch (        IOException | CryptographyException ex) {
                    Logger.getLogger(PDFReader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        //        fileMenu.add(SaveRegionText);
        //</editor-fold>
        
        SaveRegionTextallOnce.setText("Save as EDGAR HTML");
        SaveRegionTextallOnce.setToolTipText("Save as EDGAR HTML");
//        SaveRegionTextallOnce.setAccelerator(KeyStroke.getKeyStroke('e'));
        SaveRegionTextallOnce.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                try {
                    regionTextAllOnce(evt);
                } catch (        IOException | CryptographyException ex) {
                    Logger.getLogger(PDFReader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });        
        fileMenu.add(SaveRegionTextallOnce);
        
        //<editor-fold defaultstate="collapsed" desc="comment">
        listextract.setText("List Extract");
        listextract.setToolTipText("List Extract");
        listextract.setAccelerator(KeyStroke.getKeyStroke('l'));
        listextract.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                listExtract(evt);
            }
        });
        
        //fileMenu.add(listextract);
        
        
        saveAsTable.setText("Save as Table");
        saveAsTable.setToolTipText("Save as Table");
        saveAsTable.setAccelerator(KeyStroke.getKeyStroke('t'));
        saveAsTable.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                try {
                    converttoTable(evt);
                } catch (        IOException | CryptographyException ex) {
                    Logger.getLogger(PDFReader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        //        fileMenu.add(saveAsTable);
        
        saveAsList.setText("Save as List");
        saveAsList.setToolTipText("Save as List");
        saveAsList.setAccelerator(KeyStroke.getKeyStroke('l'));
        saveAsList.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                try {
                    converttoList(evt);
                } catch (        IOException | CryptographyException ex) {
                    Logger.getLogger(PDFReader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        //        fileMenu.add(saveAsList);
        
        printMenuItem.setText( "Print" );
        printMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                try
                {
                    if (document != null)
                    {
                        document.print();
                    }
                }
                catch( PrinterException e )
                {
                    e.printStackTrace();
                }
            }
        });
        //        fileMenu.add( printMenuItem );
        
        saveAsImageMenuItem.setText( "Save as image" );
        saveAsImageMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                if (document != null)
                {
                    saveImage();
                }
            }
        });
                fileMenu.add( saveAsImageMenuItem );
        //</editor-fold>
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                exitApplication();
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        viewMenu.setText("View");
        nextPageItem.setText("Next page");
        nextPageItem.setAccelerator(KeyStroke.getKeyStroke('+'));
        nextPageItem.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                nextPage();
            }
        });
        viewMenu.add(nextPageItem);

        previousPageItem.setText("Previous page");
        previousPageItem.setAccelerator(KeyStroke.getKeyStroke('-'));
        previousPageItem.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                previousPage();
            }
        });
        viewMenu.add(previousPageItem);

        menuBar.add(viewMenu);

        setJMenuBar(menuBar);        
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        this.setAlwaysOnTop(false);
//        this.setSize(screenSize.width, screenSize.height);
//        setBounds(100, 100, 400, 400);
//        setBounds(0, 0, screenSize.width, screenSize.height-40);
//        this.setBounds(0, 0, 1366, 728);
        setBounds((screenSize.width-700)/2, (screenSize.height-600)/2, 700, 600);
//        pack();
    }

    private void updateTitle(){
        setTitle( "Apurba PDF Processor - " + currentFilename + " ("+(currentPage+1)+"/"+numberOfPages+")");
    }
    
    private void nextPage(){
        if (currentPage < numberOfPages-1) 
        {
            currentPage++;
            updateTitle();
            showPage(currentPage);
            //textsOfPage(currentPage);
        }        
    }
    
    private void previousPage(){
        if (currentPage > 0 ) 
        {
            currentPage--;
            updateTitle();
            showPage(currentPage);
        }
    }    
    
    private void converttoHTML(java.awt.event.ActionEvent evt) throws IOException{
//        ToHtml extractor = new ToHtml();
//        try {
//                extractor.startExtraction(name);
//            } 
//
//        catch (Exception ex) {
//            Logger.getLogger(PDFReader.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
        
    public void setSomething(String filename,Rectangle re,int pageNumber ){
        rec = re;
        pageNumberr = pageNumber;
    }
    
    public void convertToStandardHtml(){
        JFileChooser jfc = new JFileChooser("G:/From Fuad sir DOCX to PDF");
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setSelectedFile(newFile(nameOfFile.substring(0, nameOfFile.length()-4)+".html"));
        jfc.showSaveDialog(PDFReader.this);
        File saveFile = jfc.getSelectedFile();                
        String outputFileName =  saveFile.toString();

//        String typee = JOptionPane.showInputDialog("Type","3");
//        int type =  Integer.parseInt(typee);	
//        String zooom = JOptionPane.showInputDialog("Zoom","2");
//        int zoom = new Float(zooom);
//        System.out.println(type+"<---type and zoom--->"+zoom);

        try 
        {
//                    HtmFile htmlFile = new HtmFile(outputFileName, type, zoom);
            HtmlFile htmlFile;
            htmlFile = new HtmlFile(outputFileName, 3, 2);
            htmlFile.convertPdfToHtml(name);
            htmlFile.endTable();//used for spanning
            htmlFile.getAllCell();//used for spanning
            htmlFile.closeFile();
        } 
        catch (Exception e) 
        {
            System.err.println( "Filed to convert Pdf to Html." );			
            e.printStackTrace();
        }
    }
        
    public void regionText(java.awt.event.ActionEvent evt) throws IOException, CryptographyException{   
        for(int i =0;i<textsofAllPages[0].size();i++)
        {
            System.out.println(textsofAllPages[0].get(i));
        }
//        htmlFile.allSelectedRegions(listOfSelectedRegionInRectangle);
//        listOfSelectedRegionInRectangle = new ArrayList<>();
        //before 01-07-2013
//        ExtractTextByArea ETB = new ExtractTextByArea();
//        ETB.extractRegionText(name, rectangle, currentPage, 0);        
    }    
    
    public void regionTextAllOnce(java.awt.event.ActionEvent evt) throws IOException, CryptographyException{ 
        
        JFileChooser jfc = new JFileChooser("G:/From Fuad sir DOCX to PDF");
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        jfc.setSelectedFile(newFile(nameOfFile.substring(0, nameOfFile.length()-4)+".html"));
        jfc.showSaveDialog(PDFReader.this);
        File saveFile = jfc.getSelectedFile();                
        String outputFileName =  saveFile.toString();
        HtmlFileGen htmlFileGen = new HtmlFileGen(name);
        TaggedRegion tr;
        try (BufferedWriter htmlFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileName),"UTF8"))) {
            for(int i = 0; i<listOfSelectedRegionInRectangle.size();i++)
            {
                tr = listOfSelectedRegionInRectangle.get(i);
                String s = htmlFileGen.getHtmlContent(tr.pageNumber, tr.rectangle, tr.tag).toString();
                htmlFile.write(s);
    //            System.out.println(s);
            }
        }
        listOfSelectedRegionInRectangle = new ArrayList<>();
    } 
    
    private void listExtract(java.awt.event.ActionEvent evt){
        String pdfFileName = name;
        JFileChooser jfc = new JFileChooser("G:/From Fuad sir DOCX to PDF");
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        jfc.setSelectedFile(newFile(nameOfFile.substring(0, nameOfFile.length()-4)+".html"));
        jfc.showSaveDialog(PDFReader.this);
        File saveFile = jfc.getSelectedFile();                
        String outputFileName =  saveFile.toString();
        try 
        {
            ListExtraction list = new ListExtraction(outputFileName, 2, 2);
            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.setSortByPosition( true );
            stripper.addRegion( "class1", rectangle );
            List allPages = document.getDocumentCatalog().getAllPages();
            PDPage firstPage = (PDPage)allPages.get( currentPage );
            stripper.extractRegions( firstPage );
            System.out.println( "Text in the area:" + rectangle );
            String region = stripper.getTextForRegion( "class1" );
            System.out.println( region );
            List<TextPosition> TextinArea = stripper.getAllSelectedText();
            for (Iterator<TextPosition> it = TextinArea.iterator(); it.hasNext();) 
            {
                TextPosition text = it.next();
                list.processTextt(text);
            }
//            list.endOfTable();
            list.closeFile();
        } 
        catch (Exception e) 
        {
            System.err.println( "Filed to convert Pdf to Html." );			
            e.printStackTrace();
        }
    }
    
    private void converttoTable(java.awt.event.ActionEvent evt) throws IOException, CryptographyException{
                                
        JFileChooser jfc = new JFileChooser("G:/From Fuad sir DOCX to PDF");
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        jfc.setSelectedFile(newFile(nameOfFile.substring(0, nameOfFile.length()-4)+".html"));
        jfc.showSaveDialog(PDFReader.this);
        File saveFile = jfc.getSelectedFile();                                
                        
        int[] regioon = null;
        ExtractTextByArea ETB = new ExtractTextByArea();
        try {
            regioon = ETB.extractTextByArea(name,rectangle,currentPage,0);
        } catch (IOException ex) {
            Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CryptographyException ex) {
            Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        int positionOfRowStart[] = ETB.getPointOfRowStart();
        int numberofRows = ETB.returnNumberofRows();
        int numberofColumns = ETB.returnNumberofColumns();
        
        ExtractTextByColumn ETBC = new ExtractTextByColumn();
        Rectangle[] ColumnWiseRect = new Rectangle[(numberofColumns-1)*numberofRows];
        int q = 0;
        for(int row = 0;row<=numberofRows-1;row++)
        {
            for(int column = 0;column<numberofColumns-1 ;column++)
            {
                if(column ==0 && row==0)
                {
                    ColumnWiseRect[q] = new Rectangle(rectangle.x,rectangle.y,(regioon[column+1]*5-rectangle.x)-2,positionOfRowStart[row]-rectangle.y);
//                    pdfPagePanel.drawrect(rectangle.x,rectangle.y,(regioon[column+1]*5-rectangle.x)-2,positionOfRowStart[row]-rectangle.y);
//                    System.out.println("Cell["+row+"]["+column+"]:--"+ColumnWiseRect[q].x+","+ColumnWiseRect[q].y+","+ColumnWiseRect[q].width+","+ColumnWiseRect[q].height);
//                    try {
//                        ETBC.ExtractTextByArea(name,ColumnWiseRect[q],currentPage,row,column);
//                    } catch (IOException ex) {
//                        Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
//                    } catch (CryptographyException ex) {
//                        Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
//                    }
                }
                else if(row == 0 && column>0)
                {
                    ColumnWiseRect[q] = new Rectangle(regioon[column]*5-2,rectangle.y,(regioon[column+1]*5-regioon[column]*5),positionOfRowStart[row]-rectangle.y);
//                    pdfPagePanel.drawrect(regioon[column]*5-2,rectangle.y,(regioon[column+1]*5-regioon[column]*5),positionOfRowStart[row]-rectangle.y);
//                    System.out.println("Cell["+row+"]["+column+"]:--"+ColumnWiseRect[q].x+","+ColumnWiseRect[q].y+","+ColumnWiseRect[q].width+","+ColumnWiseRect[q].height);
//                    try {
//                        ETBC.ExtractTextByArea(name,ColumnWiseRect[q],currentPage,row,column);
//                    } catch (IOException ex) {
//                        Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
//                    } catch (CryptographyException ex) {
//                        Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
//                    }
                }
                else if(column == 0 && row>0)
                {
                    ColumnWiseRect[q] = new Rectangle(rectangle.x,positionOfRowStart[row-1],(regioon[column+1]*5-rectangle.x),(positionOfRowStart[row]-positionOfRowStart[row-1]));
//                    pdfPagePanel.drawrect(rectangle.x,positionOfRowStart[row-1]+2,(regioon[column+1]*5-rectangle.x),(positionOfRowStart[row]-positionOfRowStart[row-1]));
//                    System.out.println("Cell["+row+"]["+column+"]:--"+ColumnWiseRect[q].x+","+ColumnWiseRect[q].y+","+ColumnWiseRect[q].width+","+ColumnWiseRect[q].height);
//                    try {
//                        ETBC.ExtractTextByArea(name,ColumnWiseRect[q],currentPage,row,column);
//                    } catch (IOException ex) {
//                        Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
//                    } catch (CryptographyException ex) {
//                        Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
//                    }
                }
                else if(column>0 && row>0)
                {
                    ColumnWiseRect[q] = new Rectangle(regioon[column]*5-2,positionOfRowStart[row-1],(regioon[column+1]*5-regioon[column]*5),(positionOfRowStart[row]-positionOfRowStart[row-1]));
//                    pdfPagePanel.drawrect(regioon[column]*5-2,positionOfRowStart[row-1]+2,(regioon[column+1]*5-regioon[column]*5),(positionOfRowStart[row]-positionOfRowStart[row-1]));
//                    System.out.println("Cell["+row+"]["+column+"]:--"+ColumnWiseRect[q].x+","+ColumnWiseRect[q].y+","+ColumnWiseRect[q].width+","+ColumnWiseRect[q].height);
//                    try {
//                        ETBC.ExtractTextByArea(name,ColumnWiseRect[q],currentPage,row,column);
//                    } catch (IOException ex) {
//                        Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
//                    } catch (CryptographyException ex) {
//                        Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
//                    }
                }
                q++;
            }
        }
        
//        pdfPagePanel.pdfpagepanelrec = ColumnWiseRect;
//        try {
//            ETBC.getTable(numberofRows,numberofColumns,saveFile,true);
//        } catch (IOException ex) {
//            Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
//        }        
    }
    
    private void converttoList(java.awt.event.ActionEvent evt) throws IOException, CryptographyException{
                                
        JFileChooser jfc = new JFileChooser("G:/From Fuad sir DOCX to PDF");
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        jfc.setSelectedFile(newFile(nameOfFile.substring(0, nameOfFile.length()-4)+".html"));
        jfc.showSaveDialog(PDFReader.this);
        File saveFile = jfc.getSelectedFile(); 
        int[] regioon = null;
        ExtractTextByArea ETB = new ExtractTextByArea();
        try {
            regioon = ETB.extractTextByArea(name,rectangle,currentPage,0);
        } catch (IOException ex) {
            Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CryptographyException ex) {
            Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        int positionOfRowStart[] = ETB.getPointOfRowStart();
        int numberofRows = ETB.returnNumberofRows();
        int numberofColumns = ETB.returnNumberofColumns();
        
        ExtractTextByColumn ETBC = new ExtractTextByColumn();
        Rectangle[] ColumnWiseRect = new Rectangle[(numberofColumns-1)*numberofRows];
        int q = 0;
        for(int row = 0;row<=numberofRows-1;row++)
        {
            for(int column = 0;column<numberofColumns-1 ;column++)
            {
                if(column ==0 && row==0)
                {
                    ColumnWiseRect[q] = new Rectangle(rectangle.x,rectangle.y,(regioon[column+1]*5-rectangle.x)-2,positionOfRowStart[row]-rectangle.y);
//                    pdfPagePanel.drawrect(rectangle.x,rectangle.y,(regioon[column+1]*5-rectangle.x)-2,positionOfRowStart[row]-rectangle.y);
//                    System.out.println("Cell["+row+"]["+column+"]:--"+ColumnWiseRect[q].x+","+ColumnWiseRect[q].y+","+ColumnWiseRect[q].width+","+ColumnWiseRect[q].height);
//                    try {
//                        ETBC.ExtractTextByArea(name,ColumnWiseRect[q],currentPage,row,column);
//                    } catch (IOException ex) {
//                        Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
//                    } catch (CryptographyException ex) {
//                        Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
//                    }
                }
                else if(row == 0 && column>0)
                {
                    ColumnWiseRect[q] = new Rectangle(regioon[column]*5-2,rectangle.y,(regioon[column+1]*5-regioon[column]*5),positionOfRowStart[row]-rectangle.y);
//                    pdfPagePanel.drawrect(regioon[column]*5-2,rectangle.y,(regioon[column+1]*5-regioon[column]*5),positionOfRowStart[row]-rectangle.y);
//                    System.out.println("Cell["+row+"]["+column+"]:--"+ColumnWiseRect[q].x+","+ColumnWiseRect[q].y+","+ColumnWiseRect[q].width+","+ColumnWiseRect[q].height);
//                    try {
//                        ETBC.ExtractTextByArea(name,ColumnWiseRect[q],currentPage,row,column);
//                    } catch (IOException ex) {
//                        Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
//                    } catch (CryptographyException ex) {
//                        Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
//                    }
                }
                else if(column == 0 && row>0)
                {
                    ColumnWiseRect[q] = new Rectangle(rectangle.x,positionOfRowStart[row-1],(regioon[column+1]*5-rectangle.x),(positionOfRowStart[row]-positionOfRowStart[row-1]));
//                    pdfPagePanel.drawrect(rectangle.x,positionOfRowStart[row-1]+2,(regioon[column+1]*5-rectangle.x),(positionOfRowStart[row]-positionOfRowStart[row-1]));
//                    System.out.println("Cell["+row+"]["+column+"]:--"+ColumnWiseRect[q].x+","+ColumnWiseRect[q].y+","+ColumnWiseRect[q].width+","+ColumnWiseRect[q].height);
//                    try {
//                        ETBC.ExtractTextByArea(name,ColumnWiseRect[q],currentPage,row,column);
//                    } catch (IOException ex) {
//                        Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
//                    } catch (CryptographyException ex) {
//                        Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
//                    }
                }
                else if(column>0 && row>0)
                {
                    ColumnWiseRect[q] = new Rectangle(regioon[column]*5-2,positionOfRowStart[row-1],(regioon[column+1]*5-regioon[column]*5),(positionOfRowStart[row]-positionOfRowStart[row-1]));
//                    pdfPagePanel.drawrect(regioon[column]*5-2,positionOfRowStart[row-1]+2,(regioon[column+1]*5-regioon[column]*5),(positionOfRowStart[row]-positionOfRowStart[row-1]));
//                    System.out.println("Cell["+row+"]["+column+"]:--"+ColumnWiseRect[q].x+","+ColumnWiseRect[q].y+","+ColumnWiseRect[q].width+","+ColumnWiseRect[q].height);
//                    try {
//                        ETBC.ExtractTextByArea(name,ColumnWiseRect[q],currentPage,row,column);
//                    } catch (IOException ex) {
//                        Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
//                    } catch (CryptographyException ex) {
//                        Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
//                    }
                }
                q++;
            }
        }
        
        //pdfPagePanel.pdfpagepanelrec = ColumnWiseRect;
        try {
            ETBC.getList(numberofRows,numberofColumns,saveFile);
        } catch (IOException ex) {
            Logger.getLogger(PageWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private void ExtImages(java.awt.event.ActionEvent evt){
//        GetImages extractor = new GetImages();       
//        try 
//        {
//                extractor.extractImages( name );
//        } 
//        catch (Exception ex) {
//            Logger.getLogger(PDFReader.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
        
    private void createbookmarks(java.awt.event.ActionEvent evt) throws IOException, COSVisitorException{
        CreateBookmarks bookmarks = new CreateBookmarks(name);
    }
        
    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt){
        listOfSelectedRegionInRectangle = new ArrayList<TaggedRegion>();
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(currentDir);

        ExtensionFileFilter pdfFilter = new ExtensionFileFilter(new String[] {"PDF"}, "PDF Files");
        chooser.setFileFilter(pdfFilter);
        int result = chooser.showOpenDialog(PDFReader.this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            name = chooser.getSelectedFile().getPath();
            System.out.println(name);
            nameOfFile = chooser.getSelectedFile().getName();
            currentDir = new File(name).getParentFile();
            try
            {
                openPDFFile("");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void exitApplication(){
        try
        {
            if( document != null )
            {
                document.close();
            }
        }
        catch( IOException io )
        {
            //do nothing because we are closing the application
        }
        this.setVisible( false );
        this.dispose();
    }

    public static void main(String[] args) throws Exception{
        PDFReader viewer = new PDFReader();
        String password = "";
        String filename = null;
//        String filename = "g:/From Fuad sir DOCX to PDF/Open.pdf";
        name = filename;
        for( int i = 0; i < args.length; i++ )
        {
            if( args[i].equals( PASSWORD ) )
            {
                i++;
                if( i >= args.length )
                {
                    usage();
                }
                password = args[i];
            }
            if( args[i].equals( NONSEQ ) )
            {
                useNonSeqParser = true;
            }
            else
            {
                filename = args[i];
            }
        }
        // open the pdf if present
        if (filename != null)
        {
            viewer.openPDFFile( password );
        }
        viewer.setVisible(true);
    }

    private void openPDFFile(String password) throws Exception{
        if( document != null )
        {
            document.close();
            documentPanel.removeAll();
        }
        
        File file = new File( name );
        parseDocument( file, password );
        pages = document.getDocumentCatalog().getAllPages();
        numberOfPages = pages.size();
        currentFilename = file.getAbsolutePath();
        currentPage = 0;
        updateTitle();
        showPage(0);
//        textsofAllPages = new ArrayList[pages.size()];
//        pageTexts = new PageTexts(name);
        //textsofAllPages[0] = pageTexts.getTextsOfThePage(0);
    }
     
    private void showPage(int pageNumber){
        try 
        {
            PageWrapper wrapper = new PageWrapper( PDFReader.this );
            wrapper.filename = name;
            wrapper.currentpage = pageNumber;
            wrapper.displayPage( (PDPage)pages.get(pageNumber) );
            if (documentPanel.getComponentCount() > 0)
            {
                documentPanel.remove(0);
            }
            documentPanel.add( wrapper.getPanel() );
            rectangle = wrapper.getRectangle();
            pdfPagePanel = wrapper.pagePanel;
            pack();
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

    private void saveImage(){
        try 
        {
            
            PDPage pageToSave = (PDPage)pages.get(currentPage);
            BufferedImage pageAsImage = pageToSave.convertToImage();   
            String imageFilename = name;
            if (imageFilename.toLowerCase().endsWith(".pdf"))
            {
                imageFilename = imageFilename.substring(0, imageFilename.length()-4);
            }
            imageFilename += "_" + (currentPage + 1); 
            ImageIOUtil.writeImage(pageAsImage, "jpeg", imageFilename,  BufferedImage.TYPE_USHORT_565_RGB, 300);
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }
    
    private void parseDocument( File file, String password )throws IOException{        
        document = null;
        if (useNonSeqParser)
        {
            document = PDDocument.loadNonSeq(file, null, password);
        }
        else
        {
            document = PDDocument.load(file);
            if( document.isEncrypted() )
            {
                try
                {
                    document.decrypt( password );
                }
                catch( InvalidPasswordException e )
                {
                    System.err.println( "Error: The document is encrypted." );
                }
                catch( PDF.exceptions.CryptographyException e )
                {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public ReaderBottomPanel getBottomStatusPanel(){
        return bottomStatusPanel;
    }
    
    public void setBottomStatusPanel(ReaderBottomPanel rBP){
        bottomStatusPanel = rBP;
    }
    
    private static void usage(){
        System.err.println(
                "usage: java -jar pdfbox-app-x.y.z.jar PDFReader [OPTIONS] <input-file>\n" +
                "  -password <password>      Password to decrypt the document\n" +
                "  -nonSeq                   Enables the new non-sequential parser\n" +
                "  <input-file>              The PDF document to be loaded\n"
                );
    }

    public void setTag(String s){
        lastTag = s;  
        tR = new TaggedRegion(name);
        tR.setTaggedRegion(currentPage,rectangle, lastTag);
        listOfSelectedRegionInRectangle.add(tR);
    }
        
    public void setRectangle(Rectangle rect) {
        rectangle = rect;
        pageAndSelectedRectangle.put(currentPage, rect);
        pageAndRectangleSelectedInIt.put(currentPage, rect);
    }   

}
