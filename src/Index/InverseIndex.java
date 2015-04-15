package Index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import Document.*;
import Utility.Options;
import Utility.Stemmer;

/**
 * Created by Jewel Li on 15-4-4. ivanka@udel.edu
 */
public class InverseIndex extends Object{

    /* InveredIndex data is implemented as a 2-dimensional ArrayList<Integer> */
    private int DOC_SIZE = 250;
    private int WORD_SIZE = 1000;
    private int MAX_WORD_LENGTH = 20;
    private ArrayList<ArrayList<Integer>> wordByDocIndex ;

    /* Document frequency list */
    private HashMap<String, Integer> docFreq = new HashMap<String, Integer>();

    /* A Map to map word string to its index location */
    private HashMap<String, Integer> wordMap = new HashMap<String, Integer>();

    /* A map to map doc ID (string) to its index location */
    private HashMap<String, Integer> docMap = new HashMap<String, Integer>();


    /* Simple Initialization of wordByDocIndex into a 1000 * 3000 matrix
     * @TODO
     * The initialization is too dirty...
      * */
    public InverseIndex(){
        ArrayList<Integer> postinglist = new ArrayList<Integer>(Collections.nCopies(DOC_SIZE, 0));
        this.wordByDocIndex = new ArrayList<ArrayList<Integer>>(Collections.nCopies(WORD_SIZE, postinglist));
    }



    /* ------------------------------------------------------------------------------------------------
     * Add Document
     * Return 1 if succeed, otherwise return 0
     * */
    public int adddoc(Document d, String deliminiter){
        try {
            /* add d's docID to docMap */
            addToMap(d.getDocID(), this.docMap.size(), this.docMap);

            /*
             * 1) add words in d to wordMap,
             * 2) increment document frequency
             * 3) add words in d to wordByDocIndex
             * */

            HashMap<String, Integer> dmap = singleDocMap(d.getProcessedContent(), deliminiter);
            for (String word : dmap.keySet()) {
                if (this.wordMap != null && !this.wordMap.containsKey(word)) {

                    /* add word to word map containing each word and its index in the inversed index */
                    addToMap(word, this.wordMap.size(), this.wordMap);

                    /* increase word count in document frequency HashMap */
                    incrementMap(word, this.docFreq);
                    int dindex = this.docMap.get(d.getDocID());

                    /* update index */
                    ArrayList<Integer> postinglist = this.wordByDocIndex.get(this.wordMap.get(word));
                    postinglist.set(dindex, dmap.get(word));
                    this.wordByDocIndex.set(this.wordMap.get(word), postinglist);

                    if (Utility.Options.DEBUG == true) {
                        System.out.println("postinglist for word " + word + ": " + postinglist.toString());
                    }
                }
            }
        }catch (NullPointerException e){
            return 0;
        }
        return 1;
    }

    /* add a plain text document
    * Return 1 if succeed, otherwise return 0
    * */
    public int adddoc(PlainText d, String deliminiter){
        adddoc((Document)d, deliminiter);
        return 1;
    }

    /* add a specific field @param field in Lucene or Solr standard XML document
    * Return 1 if succeed, otherwise return 0
    * */
    public int adddoc(LuceneSolrXML d, String fieldname){
        /* Get field in d */
        String deliminiter = " ";
        ArrayList<String> fieldlist;
        if (fieldname.contains("facet")){
            deliminiter = "|";
        }
        fieldlist = d.getFieldMap().get(fieldname);
        String fieldcontent = "";
        if (fieldlist != null) {
            for (String f : fieldlist) {
                fieldcontent += f + " ";
            }
        }
        PlainText pd = new PlainText(d.getFileLocation(), fieldcontent, d.getDocID());
        adddoc(pd, deliminiter);
        return 1;
    }

    /* add a specific field @param field in an Infographics XML document, which is also in the Lucene or Solr standard XML format
    * Return 1 if succeed, otherwise return 0
    * */
    public int adddoc(InfographicXML d, String fieldname){
        LuceneSolrXML dd = (LuceneSolrXML)d;
        adddoc((LuceneSolrXML) d, fieldname);
        return 1;
    }


    /* Helper function to turn the content string of a document into a HashMap of word by wordcount */
    private HashMap<String, Integer> singleDocMap(String content, String deliminiter){
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        for (String word : content.split(deliminiter)){
            if (word.length() > 2) {
                map = incrementMap(Stemmer.mystem(word), map);
            }
        }
        return map;
    }





    /* Get utility functions
    * ------------------------------------------------------------------------------------------
    * */
    public ArrayList<Integer> getPostinglist(String wordkey){
        int wordindex = this.wordMap.get(wordkey);
        return this.wordByDocIndex.get(wordindex);
    }

    public int getIndexOfWord(String word){
        return this.wordMap.get(word);
    }

    public int getIndexOfDoc(String docID){
        return this.docMap.get(docID);
    }

    public String getWordByIndex(int wordIndex){
        return getStringByIndex(wordIndex, this.wordMap);
    }

    public String getDocByIndex(int docIndex){
        return getStringByIndex(docIndex, this.docMap);
    }



    /* Template Functions
     * -----------------------------------------------------------------------------------------
     */

    /* add a T t to a Hashmap, with t being the key, and index as value */
    static <T> HashMap<T, Integer> addToMap(T t, int i, HashMap<T, Integer> map) {
        if (map != null && t != null) {
            if (!map.containsKey(t)){
                map.put(t, i);
            }
        }
        return map;
    }

    /* Increase the Integer value by 1 given key T */
    static <T> HashMap<T, Integer> incrementMap(T t, HashMap<T, Integer> map){
        if (map != null && t != null){
            if (map.containsKey(t)){
                map.put(t, map.get(t) + 1 );
            }else{
                map.put(t, 1);
            }
        }
        return map;
    }


    static <T> String getStringByIndex(int index, HashMap<String, Integer> map){
        for (Map.Entry<String, Integer> kv : map.entrySet()){
            if (kv.getValue() == index){
                return kv.getKey();
            }
        }
        return null;
    }


    /* Regular Get and Set Functions
     * -----------------------------------------------------------------------------------------
     */

    /* get function for the wordMap, containing each word and its index in the inversed index */
    public HashMap<String, Integer> getWordMap(){
        return this.wordMap;
    }

    /* get function for the docMap, containing eaach document's docID and its index in the inversed index */
    public HashMap<String, Integer> getDocMap(){
        return this.docMap;
    }

    public void setDocNum(int totalDocNumber){
        this.DOC_SIZE = totalDocNumber;
    }

    public void setWordNum(int totalWordNumber){
        this.WORD_SIZE = totalWordNumber;
    }

    public int getDOC_SIZE(){
        return DOC_SIZE;
    }

    public int getWORD_SIZE(){
        return WORD_SIZE;
    }

    public String toString(){
        String returnstr = "Object " + getClass().getName() + ":" + "Total word size=" + this.getWORD_SIZE() + ".Total doc size=" + this.getDOC_SIZE() + "\n";
        int wordsize = Math.min( WORD_SIZE, Options.MAX_INDEX_WORDS_TO_PRINT);
        int docsize = Math.min( DOC_SIZE, Options.MAX_INDEX_DOCS_TO_PRINT );

        returnstr += "Doc:\t\t\t";
        for (int d = 0; d < docsize; d ++) {    returnstr += getDocByIndex(d) + "\t";   }
        returnstr += "\n";

        for (int i = 0; i < wordsize; i ++){
            String word = "";
            if ( ( word = getWordByIndex(i)) != null ) {
                returnstr += "Word " + word;
                for (int j = 0; j < MAX_WORD_LENGTH - word.length(); j++) {   returnstr += " ";    }
                returnstr += ":\t\t" + this.wordByDocIndex.get(i).subList(0, docsize).toString() + "\n";
            }
        }
        return returnstr;
    }



    public static void main(String args[]){
        System.out.println(" ------------ Testing Class InverseIndex ------------ ");

        String testdocpath1 = "/Users/divinityelle/Documents/FacetingEval/src/TestDocuments/plaintext/ArthurRimbaud.txt";
        String testdocpath2 = "/Users/divinityelle/Documents/FacetingEval/src/TestDocuments/plaintext/OscarWilde.txt";
        String testdocpath3 = "/Users/divinityelle/Documents/FacetingEval/src/TestDocuments/plaintext/Schopenhauer.txt";
        Document doc1 = new LuceneSolrXML(testdocpath1);
        Document doc2 = new LuceneSolrXML(testdocpath2);
        Document doc3 = new LuceneSolrXML(testdocpath3);

        InverseIndex index = new InverseIndex();
        index.setDocNum(5); index.setWordNum(50);
        index.adddoc(doc1, " ");
        index.adddoc(doc2, " ");
        index.adddoc(doc3, " ");

        System.out.println(index.getDocMap().toString());
        System.out.println(index.getWordMap().toString());
    }


}
