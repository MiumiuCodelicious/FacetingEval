package Document;

/**
 * @author Jewel Li on 15-4-4. ivanka@udel.edu
 * This is the simplest implementation of plain text documents
 */

public class PlainText extends Document {

    public PlainText(String filelocation, boolean stem_choice){
        super(filelocation, stem_choice);
    }

    public PlainText(String filelocation, String docID, boolean stem_choice){
        super(filelocation, docID, stem_choice);
    }

    public PlainText(String filelocation, String filecontent, String docID, boolean stem_choice) { super(filelocation, filecontent, docID, stem_choice); }


    public static void main (String args[]){
        String testXMLpath = "/Users/divinityelle/Documents/FacetingEval/src/Document/ArthurRimbaud.txt";

        System.out.println(" ------------ Testing Class PlainText ------------ ");
        PlainText testXML = new PlainText(testXMLpath, true);
        System.out.println(testXML.toString());

    }

}
