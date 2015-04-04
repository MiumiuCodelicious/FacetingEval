package Document;

/**
 * Created by Jewel Li on 15-4-4. ivanka@udel.edu
 */
public class PlainText extends Document {

    public PlainText(String filelocation){
        super(filelocation);
    }

    public PlainText(String filelocation, String docID){
        super(filelocation, docID);
    }


    /* This is the simplest implementation of plain text documents */
    public static void main (String args[]){
        String testXMLpath = "/Users/divinityelle/Documents/FacetingEval/src/Document/ArthurRimbaud.txt";

        System.out.println(" ------------ Testing Class PlainText ------------ ");
        PlainText testXML = new PlainText(testXMLpath);
        System.out.println(testXML.toString());

    }

}
