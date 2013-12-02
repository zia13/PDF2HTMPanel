package pdfreader;



import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.util.PDFImageWriter;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HtmlFile extends PDFTextStripper
{
    private BufferedWriter htmlFile;
    private BufferedWriter spanner;

    private int type = 0;
    private float zoom = (float) 2;

    private int marginTopBackground = 0;

    private int lastMarginTop = 0;
    private int max_gap = 15;

    float previousAveCharWidth = -1;
    private int resolution = 72; //default resolution

    private boolean needToStartNewSpan = false;

    private int lastMarginLeft = 0;
    private int lastMarginRight = 0;
    private int numberSpace = 0;
    private int sizeAllSpace = 0;
    private boolean addSpace;
    private int startXLine;
    private boolean wasBold = false;
    private boolean wasItalic = false;
    private int lastFontSizePx = 0;
    private String lastFontString = "";
	
    private StringBuffer currentLine = new StringBuffer();


   /**
    * Public constructor
    * @param outputFileName The html file
    * @param type represents how we are going to create the html file
    * 			0: we create a new block for every letters
    * 			1: we create a new block for every words
    * 			2: we create a new block for every line 
    * 			3: we create a new block for every line - using a cache to set the word-spacing property
    * @param zoom 1.5 - 2 is a good range
    * @throws IOException
    */
    public HtmlFile(String outputFileName, int type, float zoom) throws IOException
    {
    	try 
        {
            htmlFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileName),"UTF8"));
            spanner = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("d:/zia.txt"),"UTF8"));
            String header = "<html>" +
                            "<head>" +
                            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>" +
                            "<title>Html file</title>" +
                            "<link rel=\"stylesheet\" href=\"css/style.css\" />" +
                            "</head>" +
                            "<body>";
            htmlFile.write(header);
            this.type = type;
            this.zoom= zoom;		
        }
    	catch (UnsupportedEncodingException e) 
        {
            System.err.println( "Error: Unsupported encoding." );
            System.exit( 1 );		
        }
    	catch (FileNotFoundException e) 
        {
            System.err.println( "Error: File not found." );
            System.exit( 1 );		
        }
    	catch (IOException e) 
        {
            System.err.println( "Error: IO error, could not open html file." );
            System.exit( 1 );	
        }
	
    }
	
    /**
     * Close the HTML file
     */
	public void closeFile() 
        {		
            try 
            {
		htmlFile.close();
	
            } 
            catch (IOException e) 
            {
                System.err.println( "Error: IO error, could not close html file." );            
                System.exit( 1 );		
            }
	}
	
        int pageNumber;
	
    /**
     * Convert a PDF file to HTML
     *
     * @param pathToPdf Path to the PDF file
     *
     * @throws IOException If there is an error processing the operation.
     */
	public void convertPdfToHtml(String pathToPdf) throws Exception 
        {	
            int positionDotPdf = pathToPdf.lastIndexOf(".pdf");	
            if (positionDotPdf == -1)            
            {                    
                System.err.println("File doesn't have .pdf extension");                
                System.exit(1);		
            }	
            int positionLastSlash = pathToPdf.lastIndexOf("/");	
            if (positionLastSlash  == -1)             
            {	
                positionLastSlash  = 0;		
            }	
            else             
            {	
                positionLastSlash++;		
            }             
            String fileName = pathToPdf.substring(positionLastSlash, positionDotPdf);            
            PDDocument document = null;
            try 
            {
                document = PDDocument.load(pathToPdf);
                if(document.isEncrypted())
                {            	
                    try 
                    {
                        document.decrypt( "" );            	
                    }            	
                    catch( InvalidPasswordException e ) 
                    {          	
                        System.err.println( "Error: Document is encrypted with a password." );
            		System.exit( 1 );            	
                    }
            
                }
                List<PDPage> allPages = document.getDocumentCatalog().getAllPages();

                //PDPage pp = new PDPage(pdRectangle);
                
                // Retrieve and save text in the HTML file
                for( int i=0; i<allPages.size(); i++ ) 
                {
                    pageNumber = i;
                    System.out.println("Processing page "+i);
                    PDPage page = allPages.get(i);    
                    BufferedImage image = page.convertToImage(BufferedImage.TYPE_INT_RGB, resolution);
                    htmlFile.write( "<div class=\"background\" style=\"position: absolute; width: "
                            +zoom*image.getWidth()+"; height: "+zoom*image.getHeight()
                            +"; background: url('"+fileName+(int)(i+1)
                            +".png') top left no-repeat; margin-top: "+marginTopBackground+"\">");
                    marginTopBackground += zoom*image.getHeight();
                    PDStream contents = page.getContents();
                    if( contents != null ) {
                            this.processStream( page, page.findResources(), page.getContents().getStream() );
                    }
                    htmlFile.write("</span>");
                    htmlFile.write( "</div>");
                }
            
                // Remove the text
                for( int i=0; i<allPages.size(); i++ ) 
                {
                    PDPage page = (PDPage)allPages.get( i );
                    PDFStreamParser parser = new PDFStreamParser(page.getContents());
                    parser.parse();
                    List tokens = parser.getTokens();
                    List newTokens = new ArrayList();
                    for( int j=0; j<tokens.size(); j++)
                    {
                        Object token = tokens.get( j );
                        if( token instanceof PDFOperator )
                        {
                            PDFOperator op = (PDFOperator)token;
                            if( op.getOperation().equals( "TJ") || op.getOperation().equals( "Tj" ))
                            {
                                newTokens.remove( newTokens.size() -1 );
                                continue;
                            }
                        }
                        newTokens.add( token );
                    }
                    PDStream newContents = new PDStream( document );
                    ContentStreamWriter writer = new ContentStreamWriter( newContents.createOutputStream() );
                    writer.writeTokens( newTokens );
                    //newContents.addCompression(); //Looks like it faster without the compression, but no extensive tests have been run.
                    page.setContents( newContents );
                }

                //Save background images
                //TODO: Do not save the image if it's blank. (Retrieve the text of one page, remove it from the document, get the image, check if it's blank, save it or not and write the html file)
                PDFImageWriter imageWriter = new PDFImageWriter();

                String imageFormat = "png";
                String password = "";
                int startPage = 1;
                int endPage = Integer.MAX_VALUE;
                String outputPrefix = pathToPdf.substring(0, positionLastSlash)+fileName;
                int imageType = BufferedImage.TYPE_INT_RGB;


                boolean success = imageWriter.writeImage(document, imageFormat, password,
                        startPage, endPage, outputPrefix, imageType, (int) (resolution*zoom));
                if (!success)
                {
                    System.err.println( "Error: no writer found for image format '"
                            + imageFormat + "'" );
                    System.exit(1);
                }            
            }        
            finally 
            {            
                if( document != null ) 
                {                
                    document.close();
                }
            }
	}
	
    /**
     * A method provided as an event interface to allow a subclass to perform
     * some specific functionality when text needs to be processed.
     *
     * @param text The text to be processed
     */
    @Override
    protected void processTextPosition( TextPosition text )
    {
    	try 
        {
            int marginLeft = (int)((text.getXDirAdj())*zoom);
            int fontSizePx = Math.round(text.getFontSizeInPt()/72*resolution*zoom);
            int marginTop = (int)((text.getYDirAdj())*zoom-fontSizePx);


            String fontString = "";
            PDFont font = text.getFont();
            PDFontDescriptor fontDescriptor = font.getFontDescriptor();

            if (fontDescriptor != null) {
                    fontString = fontDescriptor.getFontName();    			
            }
            else {
                    fontString = "";	
            }

            int indexPlus = fontString.indexOf("+");
            if (indexPlus != -1) {
                    fontString = fontString.substring(indexPlus+1);
            }
            boolean isBold = fontString.contains("Bold");
            boolean isItalic = fontString.contains("Italic");

            int indexDash = fontString.indexOf("-");
            if (indexDash != -1) {
                    fontString = fontString.substring(0, indexDash);
            }
            int indexComa = fontString.indexOf(",");
            if (indexComa != -1) {
                    fontString = fontString.substring(0, indexComa);
            }

            switch (type)
            {                    
                case 0:
                renderingSimple(text, marginLeft,  marginTop, fontSizePx, fontString, isBold, isItalic);
                break;
                case 1:
                        renderingGroupByWord(text, marginLeft,  marginTop, fontSizePx, fontString, isBold, isItalic);
                break;
                case 2:
                        renderingGroupByLineNoCache(text, marginLeft,  marginTop, fontSizePx, fontString, isBold, isItalic);
                break;
                case 3:
                        renderingGroupByLineWithCache(text, marginLeft,  marginTop, fontSizePx, fontString, isBold, isItalic);
                break;
                default:
                renderingSimple(text, marginLeft,  marginTop, fontSizePx, fontString, isBold, isItalic);
                break;
            }
        } 
        catch (IOException e) 
        {	
            e.printStackTrace();
        }    
    }	
    
    
    /** 
     * The method that given one character is going to write it in the HTML file.
     * 
     * @param text
     * @param marginLeft
     * @param marginTop
     * @param fontSizePx
     * @param fontString
     * @param isBold
     * @param isItalic
     * @throws IOException 

     */
    private void renderingSimple(TextPosition text, int marginLeft, int marginTop, int fontSizePx, String fontString, boolean isBold, boolean isItalic) throws IOException 
    {            
        htmlFile.write("<span style=\"position: absolute; margin-left:"+marginLeft+"px; margin-top: "+marginTop+"px; font-size: "+fontSizePx+"px; font-family:"+fontString+";");
        if (isBold) {
                htmlFile.write("font-weight: bold;");
        }
        if (isItalic) {
                htmlFile.write("font-style: italic;");
        }
        htmlFile.write("\">");

        htmlFile.write(text.getCharacter());
        System.out.println(text.getCharacter());

        htmlFile.write("</span>"); 
    }
    
    
    /** 
     * The method that given one character is going to write it only if it's the end of a word in the HTML file.
     * 
     * @param text
     * @param marginLeft
     * @param marginTop
     * @param fontSizePx
     * @param fontString
     * @param isBold
     * @param isItalic
     * @throws IOException 

     */
    private void renderingGroupByWord(TextPosition text, int marginLeft, int marginTop, int fontSizePx, String fontString, boolean isBold, boolean isItalic) throws IOException 
    {    
        if (lastMarginTop == marginTop) 
        {   	
            if ((needToStartNewSpan) || (wasBold != isBold) || (wasItalic != isItalic) || (lastFontSizePx != fontSizePx) || (lastMarginLeft>marginLeft) || (marginLeft-lastMarginRight>max_gap))
            {    	
                if (lastMarginTop != 0) 
                {    		
                    htmlFile.write("</span>");    			
                }
                htmlFile.write("<span style=\"position: absolute; margin-left:"+marginLeft+"px; margin-top: "+marginTop+"px; font-size: "+fontSizePx+"px; font-family:"+fontString+";");
                if (isBold) 
                {
                    htmlFile.write("font-weight: bold;");
                }
                if (isItalic) 
                {
                    htmlFile.write("font-style: italic;");
                }
                htmlFile.write("\">");
                needToStartNewSpan = false;   			
            }   			
            if (text.getCharacter().equals(" "))
            {
                htmlFile.write(" ");  			    			
                needToStartNewSpan = true;
            }
            else 
            {
                htmlFile.write(text.getCharacter().replace("<", "&lt;").replace(">", "&gt;"));
            }	        
        }		
        else 
        {	
            if (text.getCharacter().equals(" ")) 
            {
                htmlFile.write("&nbsp;");
                needToStartNewSpan = true;		
            }	
            else 
            {    	
                needToStartNewSpan = false;      
                if (lastMarginTop != 0)                 
                {
                    htmlFile.write("</span>");    		
                }
                htmlFile.write("<span style=\"position: absolute; margin-left:"+marginLeft+"px; margin-top: "+marginTop+"px; font-size: "+fontSizePx+"px; font-family:"+fontString+";");        	
                if (isBold) 
                {        			
                    htmlFile.write("font-weight: bold;");        		
                }
                if (isItalic) 
                {                        
                    htmlFile.write("font-style: italic;");
                }
                htmlFile.write("\">");
                htmlFile.write(text.getCharacter().replace("<", "&lt;").replace(">", "&gt;"));  
            }	
            lastMarginTop = marginTop;		
        }   		
        lastMarginLeft = marginLeft;
        lastMarginRight = (int) (marginLeft + text.getWidth());
        wasBold = isBold;
        wasItalic = isItalic;
        lastFontSizePx = fontSizePx;	
    }
   
    /** 
     * The method that given one character is going to write it only if it's the end of a line in the HTML file.
     * 
     * @param text
     * @param marginLeft
     * @param marginTop
     * @param fontSizePx
     * @param fontString
     * @param isBold
     * @param isItalic
     * @throws IOException 

     */
    private void renderingGroupByLineNoCache(TextPosition text, int marginLeft, int marginTop, int fontSizePx, String fontString, boolean isBold, boolean isItalic) throws IOException 
    {		
        if (lastMarginTop == marginTop) 
        {   			
            if (lastMarginLeft>marginLeft) 
            {    			
                htmlFile.write("</span>");
            	htmlFile.write("<span style=\"position: absolute; margin-left:"+marginLeft+"px; margin-top: "+marginTop+"px; font-size: "+fontSizePx+"px; font-family:"+fontString+";");
                if (isBold) 
                {                       
                    htmlFile.write("font-weight: bold;");
                }
                if (isItalic) 
                {                       
                    htmlFile.write("font-style: italic;");
                }
                htmlFile.write("\">");
       		
            }
            lastMarginTop = marginTop;
        }
        else 
        {
            if (lastMarginTop != 0) 
            {                
                htmlFile.write("</span>");
            }	
            htmlFile.write("<span style=\"position: absolute; margin-left:"+marginLeft+"px; margin-top: "+marginTop+"px; font-size: "+fontSizePx+"px; font-family:"+fontString+";");
            if (isBold) 
            {
                    htmlFile.write("font-weight: bold;");
            }
            if (isItalic) 
            {
                    htmlFile.write("font-style: italic;");
            }			
            htmlFile.write("\">");			
            lastMarginTop = marginTop;		
        }
        htmlFile.write(text.getCharacter().replace("<", "&lt;").replace(">", "&gt;"));
        lastMarginLeft = marginLeft;	
    }	

    /** 
     * The method that given one character is going to write it only if it's the end of a line in the HTML file.
     * A cache is used to set the word-spacing property.
     * 
     * @param text
     * @param marginLeft
     * @param marginTop
     * @param fontSizePx
     * @param fontString
     * @param isBold
     * @param isItalic
     * @throws IOException 

     */
    private void renderingGroupByLineWithCacheRealHtml(TextPosition text, int marginLeft, int marginTop, int fontSizePx, String fontString, boolean isBold, boolean isItalic) throws IOException 
    {    	
        if (marginLeft-lastMarginRight> text.getWidthOfSpace()) 
        {    		
            currentLine.append(" "); 
            sizeAllSpace += (marginLeft-lastMarginRight);
            numberSpace++;
            addSpace = false;
    	}
    	if ((lastMarginTop != marginTop) || (!lastFontString.equals(fontString)) || (wasBold != isBold) || (wasItalic != isItalic) || (lastFontSizePx != fontSizePx) || (lastMarginLeft>marginLeft) || (marginLeft-lastMarginRight>150))
        {
            if (lastMarginTop != 0) 
            {				
                boolean display = true;				
                // if the bloc is empty, we do not display it (for a lighter result)
                if (currentLine.length() == 1) 
                {					
                    char firstChar = currentLine.charAt(0);
                    if (firstChar == ' ') 
                    {
                            display = false;
                    }
                }
                if (display) 
                {
                    if (numberSpace != 0) 
                    {
                        int spaceWidth = Math.round(((float) sizeAllSpace)/((float) numberSpace)-text.getWidthOfSpace());    
                        htmlFile.write("<span style=\"word-spacing:"+spaceWidth+"px;position: absolute; margin-left:"+startXLine+"px; margin-top: "+lastMarginTop+"px; font-size: "+lastFontSizePx+"px; font-family:"+lastFontString+";");
                    }
                    else 
                    {                		
                        htmlFile.write("<span style=\"position: absolute; margin-left:"+startXLine+"px; margin-top: "+lastMarginTop+"px; font-size: "+lastFontSizePx+"px; font-family:"+lastFontString+";");
                    }
                    if (wasBold) 
                    {            			
                        htmlFile.write("font-weight: bold;");            		
                    }            		
                    if (wasItalic)                         
                    {            			
                        htmlFile.write("font-style: italic;");            		
                    }        			
                    htmlFile.write("\">");   
                    htmlFile.write(currentLine.toString());
                    System.out.println(currentLine.toString());
                    htmlFile.write("</span>\n");
                }			
            }   			
            numberSpace = 0;   			
            sizeAllSpace = 0;
            currentLine = new StringBuffer();
            startXLine = marginLeft;
            lastMarginTop = marginTop;
            wasBold = isBold;
            wasItalic = isItalic;
            lastFontSizePx = fontSizePx;
            lastFontString = fontString;
            addSpace = false;		
        }		
        else 
        {
            int sizeCurrentSpace = (int) (marginLeft-lastMarginRight-text.getWidthOfSpace());
            if (sizeCurrentSpace > 5) 
            {
                if (lastMarginTop != 0) 
                {
                    if (numberSpace != 0) 
                    {
                        int spaceWidth = Math.round(((float) sizeAllSpace)/((float) numberSpace)-text.getWidthOfSpace());                		
                        htmlFile.write("<span style=\"word-spacing:"+spaceWidth+"px;position: absolute; margin-left:"+startXLine+"px; margin-top: "+lastMarginTop+"px; font-size: "+lastFontSizePx+"px; font-family:"+lastFontString+";");        			
                    }
                    else 
                    {
                        htmlFile.write("<span style=\"position: absolute; margin-left:"+startXLine+"px; margin-top: "+lastMarginTop+"px; font-size: "+lastFontSizePx+"px; font-family:"+lastFontString+";");        			
                    }
                    if (wasBold) 
                    {
                        htmlFile.write("font-weight: bold;");
                    }
            		
                    if (wasItalic) 
                    {                            
                        htmlFile.write("font-style: italic;");
                    }
                    htmlFile.write("\">");
                    htmlFile.write(currentLine.toString());
                    System.out.println(currentLine.toString());
                    htmlFile.write("</span>\n");    			
                }       			
                numberSpace = 0;
                sizeAllSpace = 0;
                currentLine = new StringBuffer();
                startXLine = marginLeft;
                lastMarginTop = marginTop;
                wasBold = isBold;
                wasItalic = isItalic;
                lastFontSizePx = fontSizePx;
                lastFontString = fontString;
                addSpace = false;    		
            }
            else 
            {
                if (addSpace) 
                {
                    currentLine.append(" ");
                    sizeAllSpace += (marginLeft-lastMarginRight);
                    numberSpace++;
                    addSpace = false;   	   			
                }	
            }
        }
        if (text.getCharacter().equals(" ")) 
        {
            addSpace = true;
            //sizeAllSpace += text.getWidthOfSpace();
        }
        else 
        {
            currentLine.append(text.getCharacter().replace("<", "&lt;").replace(">", "&gt;"));   		
        }			
        lastMarginLeft = marginLeft;
        lastMarginRight = (int) (marginLeft + text.getWidth()*zoom);	
    }  
    
    // From here it is used to get html from spanning including all tables
    List<TableFromSpan> tableFromSpan = new ArrayList();
//    List<TextPosition> allCharacters = new ArrayList<>();
    TableFromSpan tfs;
    private void renderingGroupByLineWithCache(TextPosition text, int marginLeft, int marginTop, int fontSizePx, String fontString, boolean isBold, boolean isItalic) throws IOException 
    {
//        allCharacters.add(text);
//        if(pageNumber==230)
//            System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSize is"+allCharacters.size());
        if (marginLeft-lastMarginRight> text.getWidthOfSpace()) 
        {    		
            currentLine.append(" "); 
            sizeAllSpace += (marginLeft-lastMarginRight);
            numberSpace++;
            addSpace = false;
    	}
    	if ((lastMarginTop != marginTop) || (!lastFontString.equals(fontString)) || (wasBold != isBold) || (wasItalic != isItalic) || (lastFontSizePx != fontSizePx) || (lastMarginLeft>marginLeft) || (marginLeft-lastMarginRight>150))
        {
            if (lastMarginTop != 0) 
            {				
                boolean display = true;				
                // if the bloc is empty, we do not display it (for a lighter result)
                if (currentLine.length() == 1) 
                {					
                    char firstChar = currentLine.charAt(0);
                    if (firstChar == ' ') 
                    {
                            display = false;
                    }
                }
                if (display) 
                {
                    tfs = new TableFromSpan();
                    if (numberSpace != 0) 
                    {
                        int spaceWidth = Math.round(((float) sizeAllSpace)/((float) numberSpace)-text.getWidthOfSpace());    
                        htmlFile.write("<p style=\"position: absolute; margin-left:"+startXLine+"px; margin-top: "+lastMarginTop+"px; font-size: "+lastFontSizePx+"px; font-family:"+lastFontString+";");
                    }
                    else 
                    {                		
                        htmlFile.write("<p style=\"position: absolute; margin-left:"+startXLine+"px; margin-top: "+lastMarginTop+"px; font-size: "+lastFontSizePx+"px; font-family:"+lastFontString+";");
                    }
                    if (wasBold) 
                    {            			
                        htmlFile.write("font-weight: bold;");   
                        tfs.isBold = true;
                    }            		
                    if (wasItalic)                         
                    {            			
                        htmlFile.write("font-style: italic;");
                        tfs.isItalic = true;
                    }        			
                    htmlFile.write("\">");
                    tfs.startXLine = Math.round((startXLine+fontSizePx)/zoom);
                    tfs.lastMarginTop = Math.round((lastMarginTop+fontSizePx)/zoom);
                    tfs.lastFontSizePx = lastFontSizePx;
                    tfs.lastFontString = lastFontString;
                    tfs.pageNumber = pageNumber;
                    tfs.spannedText = currentLine.toString();
                    tableFromSpan.add(tfs);
                    htmlFile.write(currentLine.toString());
                    System.out.println(currentLine.toString());
                    htmlFile.write("</p>\n");
                }			
            }   			
            numberSpace = 0;   			
            sizeAllSpace = 0;
            currentLine = new StringBuffer();
            startXLine = marginLeft;
            lastMarginTop = marginTop;
            wasBold = isBold;
            wasItalic = isItalic;
            lastFontSizePx = fontSizePx;
            lastFontString = fontString;
            addSpace = false;		
        }		
        else 
        {
            int sizeCurrentSpace = (int) (marginLeft-lastMarginRight-text.getWidthOfSpace());
            if (sizeCurrentSpace > 5) 
            {
                if (lastMarginTop != 0) 
                {
                    tfs = new TableFromSpan();
                    if (numberSpace != 0) 
                    {
                        int spaceWidth = Math.round(((float) sizeAllSpace)/((float) numberSpace)-text.getWidthOfSpace());                		
                        htmlFile.write("<p style=\"position: absolute; margin-left:"+startXLine+"px; margin-top: "+lastMarginTop+"px; font-size: "+lastFontSizePx+"px; font-family:"+lastFontString+";");        			
                    }
                    else 
                    {
                        htmlFile.write("<p style=\"position: absolute; margin-left:"+startXLine+"px; margin-top: "+lastMarginTop+"px; font-size: "+lastFontSizePx+"px; font-family:"+lastFontString+";");        			
                    }
                    if (wasBold) 
                    {
                        htmlFile.write("font-weight: bold;");
                        tfs.isBold = true;
                    }
            		
                    if (wasItalic) 
                    {                            
                        htmlFile.write("font-style: italic;");
                        tfs.isItalic = true;
                    }
                    htmlFile.write("\">");
                    tfs.startXLine = Math.round((startXLine+fontSizePx)/zoom);
                    tfs.lastMarginTop = Math.round((lastMarginTop+fontSizePx)/zoom);
                    tfs.lastFontSizePx = lastFontSizePx;
                    tfs.lastFontString = lastFontString;
                    tfs.pageNumber = pageNumber;
                    tfs.spannedText = currentLine.toString();
                    tableFromSpan.add(tfs);
                    htmlFile.write(currentLine.toString());
                    System.out.println(currentLine.toString());
                    htmlFile.write("</p>\n");    			
                }       			
                numberSpace = 0;
                sizeAllSpace = 0;
                currentLine = new StringBuffer();
                startXLine = marginLeft;
                lastMarginTop = marginTop;
                wasBold = isBold;
                wasItalic = isItalic;
                lastFontSizePx = fontSizePx;
                lastFontString = fontString;
                addSpace = false;    		
            }
            else 
            {
                if (addSpace) 
                {
                    currentLine.append(" ");
                    sizeAllSpace += (marginLeft-lastMarginRight);
                    numberSpace++;
                    addSpace = false;   	   			
                }	
            }
        }
        if (text.getCharacter().equals(" ")) 
        {
            addSpace = true;
            //sizeAllSpace += text.getWidthOfSpace();
        }
        else 
        {
            currentLine.append(text.getCharacter().replace("<", "&lt;").replace(">", "&gt;"));   		
        }			
        lastMarginLeft = marginLeft;
        lastMarginRight = (int) (marginLeft + text.getWidth()*zoom);	
    }

    private void textPositionOfAllChars()
    {
        //System.out.println("No. of characters: "+ allCharacters.size());
    }
    
    public void endTable()
    {
        tfs = new TableFromSpan();
        tfs.startXLine = startXLine;
        tfs.lastMarginTop = lastMarginTop;
        tfs.lastFontSizePx = lastFontSizePx;
        tfs.lastFontString = lastFontString;
        tfs.pageNumber = pageNumber;
        tfs.isBold = wasBold;
        tfs.isItalic = wasItalic;
        tfs.spannedText = currentLine.toString();
        tableFromSpan.add(tfs);
        
    }
    
    public void getAllCell() 
    {
        int topChange = tableFromSpan.get(0).lastMarginTop;
        for(int i=0; i<tableFromSpan.size();i++)
        {
            if(Math.abs(topChange-tableFromSpan.get(i).lastMarginTop)>3)
            {
                System.out.println();
                try {
                    spanner.write("\n");
                } catch (IOException ex) {
                    Logger.getLogger(HtmlFile.class.getName()).log(Level.SEVERE, null, ex);
                }
                topChange = tableFromSpan.get(i).lastMarginTop;
            }
            System.out.print("Span "+i+": Page Number: "+tableFromSpan.get(i).pageNumber+", Texts: "+tableFromSpan.get(i).spannedText+" StartXLine "+tableFromSpan.get(i).startXLine+" ; Last Margin Top: "+tableFromSpan.get(i).lastMarginTop);
            try {
                spanner.write("Span "+i+": "+tableFromSpan.get(i).spannedText+"  ");
            } catch (IOException ex) {
                Logger.getLogger(HtmlFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            spanner.close();
        } catch (IOException ex) {
            Logger.getLogger(HtmlFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void getSelectedRegionsCell(int pageNumber, Rectangle rectangle)
    {
        //textPositionOfAllChars();
        int topChange = tableFromSpan.get(0).startXLine;
        //int xChange = 1000;//tableFromSpan.get(0).startXLine;
        int columnCount = 0;
        int rowCount = 1;
        int maxNoOfColumn = 0;
        int maxNoOfColumnInRowNo = 0;
//        System.out.println("<html><head><><>");
        for(int i=0; i<tableFromSpan.size();i++)
        {
            if(tableFromSpan.get(i).pageNumber == pageNumber)
            {
                if(tableFromSpan.get(i).startXLine>=rectangle.x && tableFromSpan.get(i).startXLine<(rectangle.x+rectangle.width) && tableFromSpan.get(i).lastMarginTop>=rectangle.y && tableFromSpan.get(i).lastMarginTop<rectangle.y+rectangle.height)
                {
                    if(Math.abs(topChange-tableFromSpan.get(i).lastMarginTop)>3)
                    //if(xChange>tableFromSpan.get(i).startXLine)
                    {
//                        System.out.println("No. of columns in Row "+rowCount+" is :"+columnCount);
                        rowCount++;
                        if(columnCount>maxNoOfColumn)
                        {
                            maxNoOfColumn = columnCount;
                            maxNoOfColumnInRowNo = rowCount;
                        }
//                        System.out.println("</tr><tr>");
                        System.out.println();
                        topChange = tableFromSpan.get(i).lastMarginTop;
                        //xChange = tableFromSpan.get(i).startXLine;
                        columnCount = 0;
                    }
                    System.out.print("<p style=\"position: absolute; margin-left:"+(tableFromSpan.get(i).startXLine*zoom-tableFromSpan.get(i).lastFontSizePx) +"px; margin-top:"+(tableFromSpan.get(i).lastMarginTop*zoom-tableFromSpan.get(i).lastFontSizePx)+"px; font-size:"+tableFromSpan.get(i).lastFontSizePx+"px; font-family:"+tableFromSpan.get(i).lastFontString+";\">"+tableFromSpan.get(i).spannedText+"</p>");
//                    System.out.print("<td><p style=\"position: absolute; margin-left:"+tableFromSpan.get(i).startXLine+"px; margin-top:"+tableFromSpan.get(i).lastMarginTop+"px; font-size:"+tableFromSpan.get(i).lastFontSizePx+"px; font-family:"+tableFromSpan.get(i).lastFontString+";\">"+tableFromSpan.get(i).spannedText+"</p></td>");
//                    System.out.print("<td><p>"+tableFromSpan.get(i).spannedText+"</p></td>");
                    columnCount++;
//                    System.out.print(" Span "+i+"; Texts:"+tableFromSpan.get(i).spannedText+"; StartXLine:"+tableFromSpan.get(i).startXLine+"; Last Margin Top:"+tableFromSpan.get(i).lastMarginTop);
                }
            }
        }
        System.out.println("Maximum Column is: "+maxNoOfColumn+", in row No. "+ maxNoOfColumnInRowNo);
//        System.out.println("</body></html>");
    }
    
    public void allSelectedRegions(List<TaggedRegion> tR)
    {
        System.out.println("Here are the Selected Region textsssssssssssss:");
        for(int i =0;i<tR.size();i++)
        {
            System.out.print("Region "+i+": ");
            TaggedRegion t = tR.get(i);
            switch (t.tag) 
            {
                case "table":
                    System.out.println("Page Number: "+t.pageNumber+"; Rentangle: "+t.rectangle);
                    getSelectedRegionsCell(t.pageNumber,t.rectangle);
                    break;
                case "table1":
                    System.out.println("Page Number: "+t.pageNumber+"; Rentangle: "+t.rectangle);
                    getSelectedRegionsCell(t.pageNumber,t.rectangle);
                    break;
                case "paragraph":
                    System.out.println("Page Number: "+t.pageNumber+"; Rentangle: "+t.rectangle);
                    getSelectedRegionsCell(t.pageNumber,t.rectangle);
                    break;
                case "text_with_line_break":
                    System.out.println("Page Number: "+t.pageNumber+"; Rentangle: "+t.rectangle);
                    getSelectedRegionsCell(t.pageNumber,t.rectangle);
                    break;                        
                case "list":
                    System.out.println("Page Number: "+t.pageNumber+"; Rentangle: "+t.rectangle);
                    getSelectedRegionsCell(t.pageNumber,t.rectangle);
                    break;
                case "image":
                    System.out.println("Page Number: "+t.pageNumber+"; Rentangle: "+t.rectangle);
                    getSelectedRegionsCell(t.pageNumber,t.rectangle);
                    break;
            }
        }        
    }
}