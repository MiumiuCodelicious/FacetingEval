package Ranker.Faceting;

import Indexer.InverseIndex;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Jewel Li on 15-4-22. ivanka@udel.edu
 *
 * This class get statistics about a single facet field.
 * For multiple facet fields, create a FacetStats instance for each facet index.
 */

public class FacetStats implements FacetStatsInterface {

    private InverseIndex facetIndex;
    private HashMap<String, Integer> docMap;
    private HashMap<String, Integer> facetMap;
    public static int NUMBER_TO_PRINT = 30;


    /**
     * how many documents are covered in each facet value
     * Key: facet value
     * Value: the number of documents covered
     * */
    private HashMap<String, Integer> facetCover = new HashMap<String, Integer>();
    private HashMap<String, Integer> subFacetCover = new HashMap<String, Integer>();
    /**
     * how many facet values are tagged in each document
     * Key: doc ID
     * Value: the number of facet values in this doc
     * */
    private HashMap<String, Integer> docCover = new HashMap<String, Integer>();
    private HashMap<String, Integer> subDocCover = new HashMap<String, Integer>();

    /**
     * @param index     To instantiate a FacetStats class, an inverse index of a facet must be given.
     */
    public FacetStats(InverseIndex index){
        if (index != null) {
            facetIndex = index;
            docMap = index.getDocMap();
            facetMap = index.getWordMap();
        }
    }


    /**
     * Given a document by its ID, get an ArrayList of facet values in String format.
     * @param docID  the String ID of a document
     * @return <code>ArrayList<String></code> of facet values
     * */
    public ArrayList<String> getFacetsInDoc(String docID){
        if (facetIndex == null){
            System.out.println("Error: no facet index is given. ");
            return null;
        }
        ArrayList<String> facetlist = new ArrayList<String>();
        int doc = facetIndex.getIndexOfDoc(docID);
        if (doc < 0 ) {
            System.out.println("Erorr: no such document " + docID + ".");
            return null;
        }

        // for each facet value in the facet index, check if tagged in this document
        for (int fvindex = 0; fvindex < facetIndex.getWORD_SIZE(); fvindex ++ ){
            if (facetIndex.getPostinglist(fvindex).get(doc) > 0){
                facetlist.add(facetIndex.getWordByIndex(fvindex));
            }
        }
        return facetlist;
    }


    /**
     * Given a facet value name, return an ArrayList of all documents tagged with it.
     * @param facetname     String facet value name
     * @return  an <code>ArrayList<String></code> of document IDs.
     */
    public ArrayList<String> getDocsInFacet( String facetname ){
        if (facetIndex == null){
            System.out.println("Error: no facet index is given. ");
            return null;
        }
        ArrayList<String> doclist = new ArrayList<String>();
        ArrayList<Integer> postinglist = facetIndex.getPostinglist(facetname);

        if ( postinglist == null){
            System.out.println("Error: no such facet value " + facetname + ".");
            return null;
        }

        for ( int doc = 0; doc < facetIndex.getDOC_SIZE(); doc ++ ){
            if ( postinglist.get(doc) > 0){
                doclist.add( facetIndex.getDocByIndex(doc) );
            }
        }
        return doclist;
    }


    /**
     * @return  the average number of facet values in each document
     * */
    public float avgFacetNum(){
        // for each document in docMap, count the number of facet values.
        if (docCover.isEmpty()) {   pullDocs();  }
        int total = 0;
        for (Integer v : docCover.values()) {
            total += v.intValue() ;
        }
        return ( docCover.size() > 0 ) ? (float)total/docCover.size() : 0.0f ;
    }


    /**
     * Overloading function of avgFacetNum()
     * @param docIDs    a list of document IDs
     * @return
     */
    public float avgFacetNum(String[] docIDs){
        if (docCover.isEmpty()) {   pullDocs();
        }
        int total = 0;
        for (String doc : docIDs) {
            total += docCover.get(doc) ;
        }
        return ( docIDs.length > 0 ) ? (float)total/docIDs.length : 0.0f ;
    }

    /**
     * Initialize key-value pairs in private class member HashMap docCover.
     * Keys in docCover is each document ID, values are the number of facet values in each document.
     */
    private void pullDocs() {
        if (docMap == null){
            System.out.println("Error: Index is empty.");
            return;
        }
        for (String docID : docMap.keySet()) {
            docCover.put(docID, getFacetsInDoc(docID).size());
        }
    }

    private void pullDocs(String[] docIDs){
        subDocCover.clear();
        if (docMap == null) {
            System.out.println("Erorr: Index is empty.");
            return;
        }
        for (String docID : docIDs) {
            docCover.put(docID, getFacetsInDoc(docID).size());
        }
    }




    /**
     * Initialize key-value pairs in private class member facetCover.
     * Keys in facetCover is each facet value, values are the number of documents tagged with each facet value.
     */
    private void pullFacets(){
        if (facetMap == null) {
            System.out.println("Erorr: No facets indexed.");
            return;
        }
        for (String facetname : facetMap.keySet()){
            int total = 0 ;
            for ( int doc : facetIndex.getPostinglist(facetname) ){
                if ( doc > 0 ){
                    total ++;
                }
            }
            facetCover.put(facetname, total);
        }
    }

    /**
     * Overload function setFacetCover()
     * @param docIDs    a list of given document IDs
     */
    private void pullFacets(String[] docIDs) {
        subFacetCover.clear();
        if (facetMap == null) {
            System.out.println("Erorr: No facets indexed.");
            return;
        }
        for (String facetname : facetMap.keySet()) {
            int total = 0;
            boolean hasdoc = false;
            ArrayList<Integer> postinglist = facetIndex.getPostinglist(facetname);
            for (int doc = 0; doc < postinglist.size(); doc ++) {
                if ( this.contains(facetIndex.getDocByIndex(doc), docIDs) >= 0 && postinglist.get(doc) > 0) {
                    total++;
                    hasdoc = true;
                }
            }
            if (hasdoc == true) {
                subFacetCover.put(facetname, total);
            }
        }
    }




    /**
     * Print the facet values that cover the highest number of documents.
     */
    public void printTopFacet(){
        System.out.println("Top facet values with the most number of documents are: ");
        printSorted(facetCover, true);
    }

    public void printTopFacet(String[] docIDs){
        System.out.println("Top facet values with the most number of documents are: ");
        pullFacets(docIDs);
        printSorted(subFacetCover, true);
    }

    /**
     * Print the facet values that cover the minimum number of documents.
     */
    public void printBottomFacet(){
        System.out.println("Bottom facet values with the least number of documents are: ");
        printSorted(facetCover, false);
    }

    public void printBottomFacet(String[] docIDs){
        System.out.println("Bottom facet values with the least number of documents are: ");
        pullFacets(docIDs);
        printSorted(subFacetCover, false);
    }

    /**
     * print the documents that are tagged with the highest number of facet values.
     */
    public void printTopDoc(){
        System.out.println("Top documents that contain the most number of facet values are: ");
        printSorted(docCover, true);
    }

    public void printTopDoc(String[] docIDs){
        System.out.println("Top documents that contain the most number of facet values are: ");
        pullDocs(docIDs);
        printSorted(docCover, true);
    }

    /**
     * Print the documents that are tagged with the least number of facet values.
     */
    public void printBottomDoc(){
        System.out.println("Bottom documents that contain the least number of facet values are: ");
        printSorted(docCover, false);
    }

    public void printBottomDoc(String[] docIDs){
        System.out.println("Bottom documents that contain the least number of facet values are: ");
        pullDocs(docIDs);
        printSorted(docCover, true);
    }

    /**
     * Given a <code>HashMap<String, Integer></code> map, print the top sorted Entries.
     * @param map   must be of <code>HashMap<String, Integer></code> type
     * @param highest   if true, sorted from highest values to lowest; if false, sorted from lowest values to highest.
     */
    private void printSorted( HashMap<String, Integer> map, boolean highest) {
        boolean asc = !highest;
        List<Entry<String, Integer>> sortedMap = sortMapComparator(map, asc);
        int counter = 0;
        for (Map.Entry<String, Integer> entry : sortedMap) {
            if (counter > NUMBER_TO_PRINT) {
                break;
            }
            counter ++;
            System.out.println("\t" + entry.getKey() + " : " + entry.getValue());
        }
        System.out.println();
    }


    /**
     * Use this template function for Primitive Types to test whether a list contains the value of an element ele
     * @param ele   the element to look for
     * @param list  the list to look in
     * @param <T>   Primitive types only
     * @return      true if list contains an element of the same value, false if not.
     */
    public static <T> int contains(T ele, T[] list){
        int index = 0;
        for ( T e : list){
            if ( e.equals(ele) ) {
                return index;
            }
            index ++;
        }
        return -1;
    }


    /**
     * Need to write a comparator to sort Map by Key.
     * @param map   HashMap to sort
     * @param asc     if true, ascending; if false, decreasing.
     */
    private List<Entry<String, Integer>> sortMapComparator (HashMap<String, Integer> map, boolean asc) {

        final boolean ascend = asc;
        if (map == null || map.isEmpty()){  return null;  }

        List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(map.entrySet());

        // sort list by value
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>()
        {
            public int compare(Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) {
                if (ascend == true) {
                    return (e1.getValue()).compareTo(e2.getValue());
                } else{
                    return (e2.getValue()).compareTo(e1.getValue());
                }
            }
        });

        return list;
    }



    /**
     * @return  the average number of documents for each facet value
     * */
    public float avgDocNum(){
        if (facetCover.isEmpty()){
            pullFacets();
        }
        int total = 0;
        for (Integer v : facetCover.values()){
            total += v.intValue();
        }
        return ( facetCover.size() > 0 )  ?  (float)total/facetCover.size()  :  0.0f;
    }

    public float avgDocNum(String[] docIDs){
        pullFacets(docIDs);
        int total = 0;
        for (Integer v : subFacetCover.values()){
            total += v.intValue();
        }
        return ( subFacetCover.size() > 0 )  ?  (float)total/subFacetCover.size()  :  0.0f;
    }


    public void setNumberFacetsForEval(int no){
        NUMBER_TO_PRINT = no;
    }

    public static void main (String args[]){
        /*
         The following code block is to create inverse indexes of a given document collection.
         This prep work must be done to analyze facet statistics.
         */
        Indexer.IndexReader indexreader = new Indexer.IndexReader();
        String current = System.getProperty("user.dir");
        indexreader.buildIndex(current + "/src/Var/TestDocuments/infographic/XYFacets/");
        indexreader.printAllFacets();


        System.out.println(" -------- Facet Index Prepared -------- ");

        for ( String facetname : indexreader.getFacetedFields() ){
            System.out.println("Analyzing Facet field " + facetname + "..................\n");
            FacetStats fstats = new FacetStats( indexreader.getFacetIndex(facetname) );

//            String expdoc = "set3_21_exp.xml";
//            System.out.println(expdoc + " is tagged with" + facetname + " values: " + fstats.getFacetsInDoc(expdoc).toString() );
//            String expfacet = "revenue";
//            ArrayList<String> doclist;
//            if ( (doclist = fstats.getDocsInFacet(expfacet)) != null) {
//                System.out.println("Documents tagged with " + expfacet + " in " + facetname + " are : " + doclist.toString());
//            }

            System.out.println("Average number of " + facetname + " values in each document is " + fstats.avgFacetNum());
            System.out.println("Average number of documents tagged with each facet value in the " + facetname + " field is : " + fstats.avgDocNum());
            System.out.println("");

            fstats.printTopFacet();
            fstats.printBottomFacet();
            fstats.printTopDoc();
            fstats.printBottomDoc();

            System.out.println("");

            System.out.println("Given a specific list of documents:\nset1_31_exp.xml, set4_15_exp.xml, set4_64-1_exp.xml, set1_54-1_exp.xml, set4_25_exp.xml\n");
            String[] sublist = {"set1_31_exp.xml", "set4_15_exp.xml", "set4_64-1_exp.xml", "set1_54-1_exp.xml", "set4_25_exp.xml"};
            System.out.println("Average number of " + facetname + " values in each document is " + fstats.avgFacetNum(sublist));
            System.out.println("Average number of documents tagged with each facet value in the " + facetname + " field is : " + fstats.avgDocNum(sublist));
            System.out.println("");

            fstats.printTopFacet(sublist);
            fstats.printBottomFacet(sublist);
            fstats.printTopDoc(sublist);
            fstats.printBottomDoc(sublist);

            System.out.println("");




        }


    }
}