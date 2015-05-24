package Ranker.Faceting.FacetRanker;

import Indexer.InverseIndex;
import Ranker.Faceting.FacetStats;

import java.util.*;

/**
 * Created by Jewel Li on 15-5-15. ivanka@udel.edu
 *
 * The most basic facet ranker: rank facet values by the number of documents associated with them.
 */
public class BasicFacetRanker implements FacetRankerInterface{

    /**
     * The entire index of facet values.
     * You still need the facetMap class variable, because in facetIndex, facet values are indexed by integer.
     * Facet map actually hold a mapping from the String facet values to its integer index in the facet index.
     */
    protected InverseIndex facetIndex;

    protected HashMap<String, Integer> facetMap;

    /**
     * A container to hold a subset of facet values in a list of documents.
     * The collection of covered facet values are the keys in the hash map, the number of documents associated with each facet value is the value.
     */
    public HashMap<String, Integer> subFacetCover = new HashMap<String, Integer>();


    public BasicFacetRanker(){
        this.facetIndex = new InverseIndex();
        this.facetMap = new HashMap<String, Integer>();
    }


    public BasicFacetRanker(InverseIndex index){
        if (index != null) {

            this.facetIndex = index;
            this.facetMap = index.getWordMap();
        }

    }


    /**
     * Given the original list of documents, return a ranked list of facet values
     * @param originalDocIDs    a 2D String array of ranked documents.
     *                          Outer layer is each document. Inner layer consists of: docID, score, relevance.
     * @return      List<Map.Entry<String, Float>> sorted List of Map.Entry: facet value and with its score.
     */
    public List<Map.Entry<String, Float>> rankFacets(String queryFieldValue, String[][] originalDocIDs){
        String[] docs = Utility.TemplateFunctions.fetchColumn(originalDocIDs, 0);

        // pull facets into class variable: HashMap<String, Integer> subFacetCover
        if ( pullFacets(docs) < 1 )    return null;

        HashMap<String, Float> facetranked = new HashMap<String, Float>();
        for ( String facetvalue : subFacetCover.keySet() ) {
            facetranked.put(facetvalue, (float) subFacetCover.get(facetvalue)/(float)docs.length);
        }

        return sortMapComparator(facetranked, false);
    }



    /**
     * Given a specific facet value, return the documents covered by this facet value.
     * @param docIDs    original list of ranked documents
     * @param facetvalue    the chosen facet value
     * @return          new ranked list of documents covered by the given facet value
     */
    public String[][] reRankDocs(String[][] docIDs, String facetvalue){
        String[] docs = Utility.TemplateFunctions.fetchColumn(docIDs, 0);
        if (docIDs == null || this.facetIndex.getPostinglist(facetvalue) == null){
            return null;
        }
        /* Get the number of documents covered in the given facet value */
        String[][] reRankedDocs;
        if ( subFacetCover != null && subFacetCover.containsKey(facetvalue) ){
            reRankedDocs = new String[ subFacetCover.get(facetvalue).intValue() ] [ docIDs[0].length ] ;
        }else{
            return null;
        }

        /* If a document belonging to a facet value is found in the given ranked list
         *  */
        int j = 0;
        ArrayList<Integer> postinglist = facetIndex.getPostinglist(facetvalue);
        for (int i = 0; i < docIDs.length; i ++) {
            int doc = facetIndex.getIndexOfDoc(docIDs[i][0]);
            if ( postinglist.get(doc) > 0 ) {
                reRankedDocs[j] = docIDs[i];
                j ++;
            }
        }
        return reRankedDocs;

    }



    /**
     * Pull out the facet values covered in the given list of document IDs, and store these facet values in class variable subFacetCover.
     * @param docIDs    a list of given document IDs
     */
    public int pullFacets(String[] docIDs) {
        this.subFacetCover.clear();
        if (this.facetMap == null) {
            System.out.println("Error: No facets indexed.");
            return 0;
        }
        for (String facetname : this.facetMap.keySet()) {
            int total = 0;
            boolean hasdoc = false;
            ArrayList<Integer> postinglist = this.facetIndex.getPostinglist(facetname);
            for (int doc = 0; doc < postinglist.size(); doc ++) {
                if ( Utility.TemplateFunctions.contains(this.facetIndex.getDocByIndex(doc), docIDs) >=0 && postinglist.get(doc) > 0) {
                    total++;
                    hasdoc = true;
                }
            }
            if (hasdoc == true) {
                this.subFacetCover.put(facetname, total);
            }
        }
        return 1;
    }




    /**
     * Need to write a comparator to sort Map by Float Value.
     * @see FacetStats  I already have a comparator to sort HashMap in FacetStats, for Integer Values
     * The reason I cannot have a generic type T for Integer, Float, and Double
     * Because these 3 types are FINAL. Therefore, they can't be exteneded.
     * @param map   HashMap to sort
     * @param asc     if true, ascending; if false, decreasing.
     */
    protected List<Map.Entry<String, Float>> sortMapComparator (HashMap<String, Float> map, boolean asc) {

        final boolean ascend = asc;
        if (map == null || map.isEmpty()){  return null;  }

        List<Map.Entry<String, Float>> list = new ArrayList<Map.Entry<String, Float>>(map.entrySet());

        // sort list by value
        Collections.sort(list, new Comparator<Map.Entry<String, Float>>() {
            public int compare(Map.Entry<String, Float> e1, Map.Entry<String, Float> e2) {
                if (ascend == true) {
                    return (e1.getValue()).compareTo(e2.getValue());
                } else {
                    return (e2.getValue()).compareTo(e1.getValue());
                }
            }
        });

        return list;
    }





}
