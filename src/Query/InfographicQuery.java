package Query;

import java.util.HashMap;
import java.util.ArrayList;

/**
 * @author Jewel Li on 15-5-3. ivanka@udel.edu
 */

public class InfographicQuery extends LuceneQuery{

    /**
     * Comply to the most common Luncene/Solr query field deliminer: "AND".
     * */
    private String deliminer = "AND";


    public InfographicQuery(){
        super();
    }

    public InfographicQuery(String query){
        super(query);
    }

    public InfographicQuery(String query, String[] fieldsWithoutStopwords){
        super(query, fieldsWithoutStopwords);
    }

    public InfographicQuery(String query, String[] fieldsWithoutStopwords, String[] fieldsToStem){
        super(query, fieldsWithoutStopwords, fieldsToStem);
    }



    /**
     * @param query   A lLucene/Solr query fitting the lucene XML query standard.
     *
     * --------------------------------------------------------+
     * field1:(value1) AND field2:(value2) AND field3:(value3) |
     * --------------------------------------------------------+
     *
     * @return  a HashMap with keys as String field names and values listed in an ArrayList of Strings.
     */
    protected HashMap<String, ArrayList<String>> queryParser(String query){
        return super.queryParser(query);
    }



    /**
     * First check if original query field HashMap is empty or null.
     * Remove stopwords in all fields in class variable original_queryfields, and return the result HashMap.
     * @return
     */
    protected HashMap<String, ArrayList<String>> removeStopWordsAllFields() {
        return super.removeStopWordsAllFields();
    }


    /**
     * Only remove stopwords in the give list of fields.
     * @param fieldnames        A String[] array of field names in which stopwords will be removed.
     * @return                  A HashMap with keys as field names and value as an ArrayList of String type values.
     */
    protected HashMap<String, ArrayList<String>> removeStopWordsIn(String[] fieldnames) {
        return super.removeStopWordsIn(fieldnames);
    }


    protected HashMap<String, ArrayList<String>> stemAllFields() {
        return super.stemAllFields();
    }


    /**
     * Only stem in the give list of fields.
     * @param fieldnames        A String[] array of names of fields that need to be stemmed.
     * @return                  A HashMap with keys as field names and value as an ArrayList of stemmed String type values.
     */
    protected HashMap<String, ArrayList<String>> stemIn(String[] fieldnames) {
        return super.stemIn(fieldnames);
    }



    public static void main(String[] args){

        String natural_language_Q = "How does Nissan compare to Toyota in terms of revenues in 2010 ?";

        String infographicQuery = "X:(Nissan) AND X:(Toyota) AND Y:(revenues in 2010) AND IMCategory:(Relative-Difference)";

        String[] fieldsWithoutStopwordsStem = {"X", "Y"};

        InfographicQuery infographicQ = new InfographicQuery(infographicQuery, fieldsWithoutStopwordsStem);
        
        System.out.println(infographicQ.toString("stem"));

    }

}
