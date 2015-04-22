package Faceting;

import java.util.ArrayList;

/**
 * Created by Jewel Li on 15-4-20. ivanka@udel.edu
 *
 * Statistics about facets in anu given document collection.
 * This collection should be specified by a given list of document IDs.
 *
 * An inverse index must have been built already to analyze facet statistics.
 * Otherwise, build an inverse index.
 *
 */

public interface FacetStats {

    ArrayList<ArrayList<Integer>> getDocFacets(String docID);




}
