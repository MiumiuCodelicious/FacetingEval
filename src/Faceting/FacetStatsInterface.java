package Faceting;

import java.util.ArrayList;

/**
 * @author Jewel Li on 15-4-20. ivanka@udel.edu
 *
 * Statistics about facets in anu given document collection.
 * This collection should be specified by a given list of document IDs.
 *
 * @see Index.InverseIndex
 * An inverse index must have been built already to analyze facet statistics.
 * Otherwise, build an inverse index.
 *
 * Actual facetvalue-by-doc index is of data structure ArrayList<ArrayList<Integer>>
 * where a mapping of facetvalue to integer index is of data structure HashMap<String, Integer>
 */

public interface FacetStatsInterface {

    /**
     * @return <code>ArrayList<String></code> of facet values
     * */
    ArrayList<String> getDocFacets(String docID);

    getFacets();




}
