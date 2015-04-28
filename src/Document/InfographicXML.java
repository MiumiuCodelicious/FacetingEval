package Document;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Jewel Li on 15-4-3. ivanka@udel.edu
 */
public class InfographicXML extends LuceneSolrXML {

    public InfographicXML(String GXMLpath, boolean stem_choice){
        super(GXMLpath, stem_choice);
        setProcessedContent();
        setFieldMap();
    }

    public static String process(String filecontent, boolean stem){
        return Utility.StopWordHandler.removeGraphWord( LuceneSolrXML.process(filecontent, stem) );
    }

    private void setProcessedContent(){
        this.processedContent = this.process(this.plainContent, this.stem);
    }

    private void setFieldMap(){
        this.fieldMap = this.getFields();
    }


    public HashMap<String, ArrayList> getFields(){
        this.fieldMap = super.getFields();
        if (this.fieldMap != null) {
            for (String field : this.fieldMap.keySet()) {
                ArrayList<String> values = this.fieldMap.get(field);
                for (int i = 0; i < values.size() ; i ++)
                    values.set(i, Utility.StopWordHandler.removeGraphWord(values.get(i)) );
                this.fieldMap.put(field, values);
            }
        }
        return this.fieldMap;
    }




    public static void main(String args[]) {

        String testXMLpath = "/Users/divinityelle/Documents/FacetingEval/src/Var/TestDocuments/infographic/XYFacets/set1_1_exp.xml";
        System.out.println(" ------------ Testing Class LuceneSolrXML ------------ ");
        LuceneSolrXML testXML = new LuceneSolrXML(testXMLpath, true);

        System.out.println(testXML.toString());


    }

}
