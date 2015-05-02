package Document;

/**
 * @author Jewel Li on 15-4-4. ivanka@udel.edu
 * This is the simplest implementation of plain text documents
 */

public class PlainText extends Document {

    public PlainText(String filelocation){
        super(filelocation);
    }

    public PlainText(String filelocation, String docID){
        super(filelocation, docID);
    }

    public PlainText(String filelocation, String filecontent, String docID) { super(filelocation, filecontent, docID); }


    public static void main (String args[]){
        String testXMLpath = "/Users/divinityelle/Documents/FacetingEval/src/Document/ArthurRimbaud.txt";

        System.out.println(" ------------ Testing Class PlainText ------------ ");
        PlainText testXML = new PlainText(testXMLpath);
        System.out.println(testXML.toString());

    }

}
