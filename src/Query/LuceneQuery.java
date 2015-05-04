package Query;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * @author Jewel Li on 15-5-3. ivanka@udel.edu
 *
 * Accept a lucene/solr format query that has multiple field-value pairs.
 * IMPORTANT: the given query must comply to the lucene/solr format such as:
 *
 * --------------------------------------------------------+
 * field1:(value1) AND field2:(value2) AND field3:(value3) |
 * --------------------------------------------------------+
 *
 */
public class LuceneQuery extends KeywordQuery {

    protected HashMap<String, ArrayList<String>> original_queryfields;
    protected HashMap<String, ArrayList<String>> keyword_queryfields;
    protected HashMap<String, ArrayList<String>> keyword_stemmed_queryfields;

    /**
     * The most common Luncene/Solr query field deliminer is "AND".
     * */
    private String deliminer = "AND";

    /**
     * Defalt constructor
     */
    public LuceneQuery(){
        super();
        original_queryfields = new HashMap<String, ArrayList<String>>();
        keyword_queryfields = new HashMap<String, ArrayList<String>>();
        keyword_stemmed_queryfields = new HashMap<String, ArrayList<String>>();
    }


    /**
     * Constructor given only a String query.
     * IMPORTANT: All fields in this query will be removed of stopwords and stemmed.
     * @param query
     */
    public LuceneQuery(String query){
        this();
        original = query;
        original_queryfields = queryParser(query);
        keyword_queryfields = removeStopWordsAllFields();
        keyword_stemmed_queryfields = stemAllFields();
    }

    /**
     * Constructor given a String query, and a list of fields to remove stopwords from.
     * IMPORTANT: Only fields with stopwords removed will be stemmed.
     * @param query     String query
     * @param fieldsWithoutStopwordsStem    a list of field names to remove stopwords from and to stem.
     */
    public LuceneQuery(String query, String[] fieldsWithoutStopwordsStem){
        this();
        original = query;
        original_queryfields = queryParser(query);
        keyword_queryfields = removeStopWordsIn(fieldsWithoutStopwordsStem);
        keyword_stemmed_queryfields = stemIn(fieldsWithoutStopwordsStem);
    }


    /**
     * Constructor given a String query, a list of fields to remove stopwords from, and a list of fields to stem.
     * Your query might contain fields that has nominal or categorical value which cannot be treated as keywords nor stemmed.
     * Use this constructor to have full control.
     * @param query
     * @param fieldsWithoutStopwords
     * @param fieldsToStem
     */
    public LuceneQuery(String query, String[] fieldsWithoutStopwords, String[] fieldsToStem){
        this();
        original = query;
        original_queryfields = queryParser(query);
        keyword_queryfields = removeStopWordsIn(fieldsWithoutStopwords);
        keyword_stemmed_queryfields = stemIn(fieldsToStem);
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

        if(query == null){ return null;}

        HashMap<String, ArrayList<String>> fields = new HashMap<String, ArrayList<String>>();

        for (String fieldvalue : query.split(deliminer)){

            String field = fieldvalue.split(":\\(?")[0].replaceAll(":\\(", "").trim();
            String value = fieldvalue.split(":\\(?")[1].replaceAll("\\)", "").trim();

            ArrayList<String> valuelist = new ArrayList<String>();

            if (fields.containsKey(field)) {
                valuelist.addAll(fields.get(field));
            }
            valuelist.add(value);
            fields.put(field, valuelist );
        }
        return fields;
    }


    /**
     * First check if original query field HashMap is empty or null.
     * Remove stopwords in all fields in class variable original_queryfields, and return the result HashMap.
     * @return
     */
    protected HashMap<String, ArrayList<String>> removeStopWordsAllFields(){

        if (original_queryfields == null || original_queryfields.isEmpty()) {
            return null;
        }
        String[] fieldarray = original_queryfields.keySet().toArray(new String[original_queryfields.keySet().size()]);
        return removeStopWordsIn(fieldarray);

    }


    /**
     * Only remove stopwords in the give list of fields.
     * @param fieldnames        A String[] array of field names in which stopwords will be removed.
     * @return                  A HashMap with keys as field names and value as an ArrayList of String type values.
     */
    protected HashMap<String, ArrayList<String>> removeStopWordsIn(String[] fieldnames) {

        HashMap<String, ArrayList<String>> keywords = new HashMap<String, ArrayList<String>>();

        if (original_queryfields == null || original_queryfields.isEmpty()) {
            return null;
        }
        for (String field : original_queryfields.keySet()) {
            ArrayList<String> keyword_valuelist = new ArrayList<String>();
            for ( String value : original_queryfields.get(field) ){
                if (Utility.TemplateFunctions.contains(field, fieldnames) >= 0) {
                    keyword_valuelist.add(Document.Document.process(value));
                }else{
                    keyword_valuelist.add(value);
                }
            }
            keywords.put(field, keyword_valuelist);
        }
        return keywords;
    }



    /**
     * First check if original query field HashMap is empty or null.
     * Stem all fields in class variable original_queryfields, and return the result HashMap.
     * @return
     */
    protected HashMap<String, ArrayList<String>> stemAllFields(){

        if (keyword_queryfields == null || keyword_queryfields.isEmpty() ){
            return null;
        }

        String[] fieldarray = keyword_queryfields.keySet().toArray(new String[keyword_queryfields.keySet().size()]);
        return stemIn(fieldarray);
    }



    /**
     * Only stem in the give list of fields.
     * @param fieldnames        A String[] array of names of fields that need to be stemmed.
     * @return                  A HashMap with keys as field names and value as an ArrayList of stemmed String type values.
     */
    protected HashMap<String, ArrayList<String>> stemIn(String[] fieldnames){

        HashMap<String, ArrayList<String>> stemmed = new HashMap<String, ArrayList<String>>();
        if (keyword_queryfields == null || keyword_queryfields.isEmpty() ){
            return null;
        }

        for (String field : keyword_queryfields.keySet()){
            ArrayList<String> stemmed_valuelist = new ArrayList<String>();
            for ( String value : keyword_queryfields.get(field) ){
                if (Utility.TemplateFunctions.contains(field, fieldnames) >= 0 ) {
                    stemmed_valuelist.add(Utility.Stemmer.mystem(value));
                }else{
                    stemmed_valuelist.add(value);
                }
            }
            stemmed.put(field, stemmed_valuelist);
        }
        return stemmed;
    }



    /**
     * Set your own deliminer
     * @param del   new deliminer. Default deliminer is "AND"
     */
    public void setDeliminer(String del){
        deliminer = del;
    }

    public String getOriginal(){
        return original;
    }

    public String getDeliminer(){
        return deliminer;
    }


    public HashMap<String, ArrayList<String>> getOriginal_queryfields(){
        return original_queryfields;
    }

    public HashMap<String, ArrayList<String>> getKeyword_queryfields(){
        return keyword_queryfields;
    }

    public HashMap<String, ArrayList<String>> getKeyword_stemmed_queryfields(){
        return keyword_stemmed_queryfields;
    }


    /**
     * Need an input choice: original, keywords, stemmed.
     * @param choice    Give at least 3 beginning characters of one of the following choices: original, keywords, stemmed.
     * @return      A string of either original query, keywords in the query, or stemmed keywords in the query.
     */
    public String toString(String choice){
        if (choice.toLowerCase().contains("ori")) {
            return original_queryfields.toString();
        } else if (choice.toLowerCase().contains("key")){
            return keyword_queryfields.toString();
        } else if (choice.toLowerCase().contains("ste")){
            return keyword_stemmed_queryfields.toString();
        }else{
            System.out.println("Error. Valid choices are: original, keywords, stemmed.");
            return null;
        }
    }

    public static void main (String[] args){

        String SolrQuery = "X:(Nissan) AND X:(Toyota) AND Y:(revenue of 2010) AND IMCategory:(Relative-Difference)";

        String[] fieldsWithoutStopwords = {"X", "Y"};
        String[] fieldsToStem = {"X", "Y"};

        LuceneQuery luceneQ = new LuceneQuery(SolrQuery, fieldsWithoutStopwords, fieldsToStem);

        System.out.println( luceneQ.toString("origin") );
        System.out.println( luceneQ.toString("Keywords"));
        System.out.println( luceneQ.toString("Stemmed") );
    }



}
