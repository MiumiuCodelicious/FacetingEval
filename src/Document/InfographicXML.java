package Document;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Jewel Li on 15-4-3. ivanka@udel.edu
 */
public class InfographicXML extends LuceneSolrXML {

    public InfographicXML(String GXMLid){
        super(GXMLid);
        removeGraphWord();
    }

    private void removeGraphWord(){
        this.processedContent = Utility.StopWordHandler.removeGraphWord(this.processedContent);
        if (this.fieldMap != null) {
            for (String field : this.fieldMap.keySet()) {
                String[] values = this.fieldMap.get(field);
                for (int i = 0; i < values.length ; i ++) {
                    values[i] = Utility.StopWordHandler.removeGraphWord( this.fieldMap.get(field)[i] ) ;
                }
                this.fieldMap.put(field, values);
            }
        }
    }




    public static void main(String args[]) throws IOException {

        String testXMLpath = "/Users/divinityelle/Documents/FacetingEval/src/Document/testInfographicXML.xml";
        System.out.println(" ------------ Testing Class LuceneSolrXML ------------ ");
        LuceneSolrXML testXML = new LuceneSolrXML(testXMLpath);
        System.out.println( "Plain Graphic XML: " + testXML.getPlainContent());
        System.out.println( "Processed (stripped) Graphic XML: " + testXML.getProcessedContent());

        HashMap<String, String[]> XMLfields = testXML.getFieldMap();
        System.out.println("Graphic XML Field - Values: ");

        if (XMLfields != null) {
            for (String field : XMLfields.keySet()) {
                String values = "";
                for (String value : XMLfields.get(field)) {
                    values += value + "\n";
                }
                System.out.println(field + ": \n" + values );
            }
        }


    }

}
