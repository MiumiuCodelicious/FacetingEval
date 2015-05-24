package Ranker.Faceting.FacetRanker;

import Indexer.InverseIndex;
import Ranker.Faceting.FacetStats;
import Utility.Options;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jewel Li on 15-5-15. ivanka@udel.edu
 */
public class FacetExpectedPromoRanker extends BasicFacetRanker implements FacetRankerInterface {



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


    /**
     * Given a query, there is only one zipfian distribution based on the query's original document ranking.
     */
    protected float[] zipf;



    public FacetExpectedPromoRanker(InverseIndex index){
        super(index);
        this.facetIndex = super.facetIndex;
        this.facetMap = super.facetMap;
    }



    /**
     * For the ranked list of documents given 1 query, estimate an Zipfian distribution.
     * This function will set values in the class variable zipf, which is a float[] array
     * containing a probability for each document in the original_docs.
     *
     * @param original_docs    the original ranked list of documents.
     * @return if successfully runs, return 1; otherwise, 0.
     */
    protected int zipfianDist(String[] original_docs){
        if ( original_docs == null ) return 0;

        zipf = new float[original_docs.length];
        float norm = 0.0f;

        for (int r = 0; r < original_docs.length; r ++) {
            zipf[r] = (float)(Math.pow(r+1, -2));
            norm += zipf[r];
        }
        for (int r = 0; r < original_docs.length; r ++) {
            zipf[r] /= norm;
        }
        return 1;
    }


    /**
     * Given a list of re-ranked documents by a facet value, get the zipfian distribution of
     * documents in reRanked_docs.
     * @param original_docs     the original ranked list of documents.
     * @param reRanked_docs     the re-ranked list of documents of a facet value.
     * @return
     */
    public float[] getZipf(String[] original_docs, String[] reRanked_docs){

        if ( reRanked_docs == null ) return null;

        float[] return_dist = new float[reRanked_docs.length];

        for (int pos = 0; pos < reRanked_docs.length; pos ++){
            int original_pos = Utility.TemplateFunctions.contains(reRanked_docs[pos], original_docs);
            if ( original_pos >= 0 ){
                return_dist[pos] = zipf[original_pos];
            }
        }
        return return_dist;
    }




    /**
     * Given the original ranked lists of documents along with each document's score and relevance
     * Calculate the rank promotion for each document in the ranked list after a particular facet.
     * @see FacetStats use static template function: public static <T> int contains(T ele, T[] list)
     * @param original_docIDs   the original ranked list of documents
     * @param facet_docIDs      the new ranked list of documents suppose a facet value is chosen
     * --------------------------------+
     * DocID1   5.81916274628  1       |
     * DocID2   5.36491699619  2       |
     * ...                             |
     * --------------------------------+
     * @return an integer array with the same length as the original ranked documents. If new ranked list
     *         does not contain a document, the promotion is negative original rank.
     */
    public int[] rankPromotion(String[][] original_docIDs, String[][] facet_docIDs){

        int[] promo = new int[facet_docIDs.length];

        /* Get a list of doc IDs from the new ranked list */
        String[] ids = Utility.TemplateFunctions.fetchColumn(original_docIDs, 0);

        for (int newr = 0; newr < facet_docIDs.length; newr ++){

            /* if original ids comtain the new id */
            int oldr = Utility.TemplateFunctions.contains(facet_docIDs[newr][0], ids);

            /* If old rank < -1, document lost in new ranked list */
            if ( oldr > -1 ){
                promo[newr] = oldr - newr;
            }
        }
        return promo;
    }




    /**
     * Given an original ranked list of documents and a chosen facet value, create the new ranked list of documents covered by this facet value.
     * @param original_DocIDs    a 2D String array. Outer elements are each document, inner elements are: docID, score, relevance.
     * @param facetvalue    the chosen facet value
     * @return      a float number of Expected Promotion for the chosen facet value.
     */
    public float expectedPromo(String[][] original_DocIDs, String facetvalue){
        float EP = 0.0f;

        String[][] reRankedDocs = reRankDocs(original_DocIDs, facetvalue);

        String[] original_docs = Utility.TemplateFunctions.fetchColumn(original_DocIDs, 0);
        String[] reRanked_docs = Utility.TemplateFunctions.fetchColumn(reRankedDocs, 0);
        float[] prob = getZipf(original_docs, reRanked_docs);

        int[] promo = rankPromotion(original_DocIDs, reRankedDocs);

        for (int r = 0; r < reRankedDocs.length; r ++){
            EP += prob[r] * promo[r];

            if (Options.DEBUG == true) {
                System.out.println("Original ranked list length = " + original_DocIDs.length + ". Facet = " + facetvalue + ". Zipf = " + prob[r] + ". Promo = " + promo[r] + ". Expected promo = " + (prob[r] * promo[r]));
            }

        }
        return EP;
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

        // set values in class variable: float[] zipf
        if ( zipfianDist(docs) < 1 )   return null;

        for ( String facetvalue : subFacetCover.keySet() ) {

            float exp_promo = expectedPromo(originalDocIDs, facetvalue);

            facetranked.put(facetvalue, exp_promo);

            if(Options.DEBUG == true) {
                System.out.println("face value: " + facetvalue + ", expected promo = " + exp_promo);
            }

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
        return super.reRankDocs(docIDs, facetvalue);
    }

    /**
     * Pull out the facet values covered in the given list of document IDs, and store these facet values in class variable subFacetCover.
     * @param docIDs    a list of given document IDs
     */
    public int pullFacets(String[] docIDs) {
        int result = super.pullFacets(docIDs);
        this.subFacetCover = super.subFacetCover;
        return result;
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
        return super.sortMapComparator(map, asc);
    }



}
