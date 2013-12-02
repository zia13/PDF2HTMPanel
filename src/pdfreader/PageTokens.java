/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pdfreader;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.examples.pdmodel.ReplaceString;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.PDFOperator;

/**
 *
 * @author Zia
 */
public class PageTokens extends ReplaceString{

    public PageTokens() {
    }

    @Override
    public void doIt(String inputFile, String outputFile, String strToFind, String message) throws IOException, COSVisitorException {
        super.doIt(inputFile, outputFile, strToFind, message); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void getTokens(String inputFile) throws IOException{
        // the document
        PDDocument doc = null;
        try
        {
            doc = PDDocument.load( inputFile );
            List pages = doc.getDocumentCatalog().getAllPages();
            for( int i=0; i<pages.size(); i++ )
            {
                PDPage page = (PDPage)pages.get( i );
                PDStream contents = page.getContents();
                PDFStreamParser parser = new PDFStreamParser(contents.getStream() );
                parser.parse();
                List tokens = parser.getTokens();
                for( int j=0; j<tokens.size(); j++ )
                {
                    Object next = tokens.get( j );
                    if( next instanceof PDFOperator )
                    {
                        PDFOperator op = (PDFOperator)next;
                        switch (op.getOperation()) {
                            case "Tj":
                                {
                                    COSString previous = (COSString)tokens.get( j-1 );
                                    String string = previous.getString();
                                    System.out.println("New Line Token :"+string);
                                    break;
                                }
                            case "TJ":
                                {  
                                    COSArray previous = (COSArray)tokens.get( j-1 );
                                    //                            System.out.println(previous.toString());
                                    String arr = "";
                                    for( int k=0; k<previous.size(); k++ )
                                    {
                                        Object arrElement = previous.getObject( k );
                                        if( arrElement instanceof COSString )
                                        {
                                            COSString cosString = (COSString)arrElement;
                                            String string = cosString.getString();  
                                            arr += string;
                                        }
                                    }
                                    System.out.println("token :"+arr);
                                    break;
                                }
                        }
                    }
                }
            }
        }
        finally
        {
            if( doc != null )
            {
                doc.close();
            }
        }
    }
    
    public static void main(String[] args){
        
        PageTokens pt = new PageTokens();
        try {
            pt.getTokens("C:/1.pdf");
        } catch (IOException ex) {
            Logger.getLogger(PageTokens.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
