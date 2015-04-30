package Indexer;

import Document.InfographicXML;
import Document.LuceneSolrXML;
import Document.PlainText;
import Utility.Options;

import java.util.HashMap;
import java.util.Set;
import java.io.File;

/**
 * @author Jewel Li on 15-4-5. ivanka@udel.edu
 */
public class IndexReader implements IndexReaderInterface {

    private HashMap<String, InverseIndex> indexBucket;
    private HashMap<String, InverseIndex> facetBucket;
    private String doctype = "plaintext";

    /** Constructor */
    public IndexReader(){

    }



    /**
     * @param dir  relative (to project home) directory to files to index.
     * build index from a given directory. the directory must be flat-structured. No recursive index.
     * */
    public void buildIndex(String dir){
        indexBucket = new HashMap<String, InverseIndex>();
        facetBucket = new HashMap<String, InverseIndex>();
        setSchemaFields();
        readdocs(dir, this.getDocType());
    }

    /**
     * Second Constructor: build index from a given directory, only index the given list of documents
     * @param dir   relative (to project home) directory to files to index.
     * @param docIDs    a list of document IDs to to be indexed.
     */
    public void buildIndex(String dir, String[] docIDs){
        indexBucket = new HashMap<String, InverseIndex>();
        facetBucket = new HashMap<String, InverseIndex>();
        setSchemaFields();
        readdocs(dir, this.getDocType(), docIDs);
    }

    /**
     * WARNING: the location to schema file is hard coded: /src/schema.xml
     * get schema defined in /src/schema.xml for multi-field XML or treat as plain document
     *  */
    private void setSchemaFields(){
        SchemaReader schema = new SchemaReader();
        if (schema.readSchema() == -1 ) {System.out.println("Error, schema not read."); return;}
        this.doctype = schema.getDocType();
        if (schema.readSchema() != -1 ) {
            if (schema.getDocType().equals("plaintext")) {
                InverseIndex index = new InverseIndex();
                indexBucket.put("plaintext", index);
            } else {
                for (String field : schema.getIndexed()) {
                    InverseIndex index = new InverseIndex();
                    indexBucket.put(field, index);
                }
                for (String facetname : schema.getFacets()) {
                    InverseIndex facet = new InverseIndex();
                    facetBucket.put(facetname, facet);
                }
            }
        }
    }


    public void readdocs(String dir, String doctype){
        File docdir = new File(dir);
        if (docdir.exists() && docdir.isDirectory()){
            for (File doc : docdir.listFiles()) {
                if (doc.getName().endsWith(".xml")) {
                    addSingleDoc(doctype, doc.getAbsolutePath());
                }
            }
        }
    }

    public void readdocs(String dir, String doctype, String[] docIDs){
        File docdir = new File(dir);
        if (docdir.exists() && docdir.isDirectory()){
            for (String doc : docIDs) {
                if (doc.endsWith(".xml")) {
                    String filepath;
                    if (dir.endsWith("/")){
                        filepath = dir + doc;
                    }else{
                        filepath = dir + "/" + doc;
                    }
                    addSingleDoc(doctype, filepath);
                }
            }
        }
    }



    private void addSingleDoc(String doctype, String docpath){
        if (doctype.equals("luncene") || doctype.equals("infographic")) {
            LuceneSolrXML lucenedoc = new LuceneSolrXML(docpath, true);
            addDocFacet(lucenedoc);
        }
        for (String field : indexBucket.keySet()){
            if (doctype.equals("plaintext")){
                PlainText plaintextdoc = new PlainText(docpath, true);
                indexBucket.get(field).adddoc(plaintextdoc, " ");
                if (Options.DEBUG == true) {
                    System.out.println("Added plain text " + plaintextdoc.getDocID() + " to index.");
                }
            }else if (doctype.equals("lucene")){
                LuceneSolrXML lucenedoc = new LuceneSolrXML(docpath, true);
                indexBucket.get(field).adddoc(lucenedoc, field);
                if (Options.DEBUG == true) {
                    System.out.println("Added lucene/solr XML " + lucenedoc.getDocID() + " field " + field + " to index.");
                }
            }else if (doctype.equals("infographic")){
                InfographicXML infographicdoc = new InfographicXML(docpath, true);
                indexBucket.get(field).adddoc(infographicdoc, field);
                if (Options.DEBUG == true) {
                    System.out.println("Added infographic XML " + infographicdoc.getDocID() + " field " + field + " to index.");
                }
            }
        }
        if (Options.DEBUG == true) {    System.out.println("\n");   }
    }


    private void addDocFacet(LuceneSolrXML doc){
        if ( !facetBucket.isEmpty() ) {
            for (String facet : facetBucket.keySet()) {
                facetBucket.get(facet).adddoc(doc, facet);
                if (Options.DEBUG == true) {
                    System.out.println("Add Facet " + facet + " from " + doc.getDocID() + ":" + doc.getFieldMap().get(facet));
                }
            }
        }
    }


    /**
     *  write entire index to file
     * @TODO Haven't figured this out yet..
     * */
    public void writeIndex(){}


    public String getDocType(){
        return this.doctype;
    }

    /**
     * From the indexed index bucket, get a field index.
     * @param field     field name
     * @return  the inverse index of the given field
     */
    public InverseIndex getIndex(String field){
        return getFromBucket(field, this.indexBucket);
    }

    /**
     * From the indexed index bucket, get a field index.
     * @param facet     facet name
     * @return  the inverse index of the given facet
     */
    public InverseIndex getFacetIndex(String facet) {
        return getFromBucket(facet, this.facetBucket);
    }

    /**
     * @param key       field name
     * @param bucket    inverse index bucket
     * @param <T>       inverse index
     * @return          return the index of the given field
     * Template function to get a single field index from a bucket.
     * Bucket is a <code>HashMap<String, InverseIndex></code>.
     * Field name is a string, which is also the key in the bucket.
     */
    public <T> T getFromBucket(String key, HashMap<String, T> bucket) {
        if (bucket != null) {
            if (bucket.containsKey(key)) {
                return bucket.get(key);
            }else{
                System.out.println("Indexed document type " + getDocType() + " does not contain " + key + " field.");
                return null;
            }
        }
        System.out.println("The given inverse index bucket is empty.");
        return null;
    }

    /**
     * Get all indexed fields defined by user in schema.xml
     * @return  a set of field names in string type.
     */
    public Set<String> getIndexedFields(){
        return getKeyset(this.indexBucket);
    }

    /**
     * Get all facet values defined by user in schema.xml
     * @return  set of facet values in String type.
     */
    public Set<String> getFacetedFields(){
        return getKeyset(this.facetBucket);
    }

    /**
     * Template function for getting the entire key set from a inverse index bucket (HashMap).
     * @param bucket    inverse index bucket <code>HashMap<String, InverseIndex></></code>
     * @param <K>       key, string type field/facet name.
     * @param <T>       inverse index.
     * @return          key set
     */
    public <K, T> Set<K> getKeyset(HashMap<K, T> bucket){
        if (bucket != null){
            return bucket.keySet();
        }else{
            System.out.println("The given inverse index bucket is empty.");
            return null;
        }
    }



    /**
     * Print a single field index
     *  */
    public void printIndex(String field){
        printIndex(indexBucket, field);
    }

    /**
     * Print each field index in the entire index bucket
     *  */
    public void printAllIndexes(){
        printAllIndexes(indexBucket);
    }

    /**
     * Print a single facet index
     *  */
    public void printFacet(String facet){
        printIndex(facetBucket, facet);
    }

    /**
     * Print each facet index in the entire facet bucket
     * */
    public void printAllFacets(){
        printAllIndexes(facetBucket);
    }



    static <T> void printIndex( HashMap<String,InverseIndex> bucket, String field){
        if ( bucket != null && bucket.containsKey(field) ){
            System.out.println("Printing Indexed " + field + ": " + bucket.get(field).toString() ) ;
        }
    }

    static <T> void printAllIndexes( HashMap<String,InverseIndex> bucket ){
        if (bucket != null){
            for (String fieldkey : bucket.keySet()){
                printIndex(bucket, fieldkey);
            }
        }
    }

    /**
     * Indexed fields and facets defined in schema.xml should be the keys of indexBucket and keys of facetBucket
     * */
    public void printSchema(){
        System.out.println("Schema defined indexed fields: " + indexBucket.keySet().toString());
        System.out.println("Schema defined facet fields: " + facetBucket.keySet().toString() + "\n");
    }



    public static void main (String args[]){
        IndexReader indexreader = new IndexReader();
        String current = System.getProperty("user.dir");
        indexreader.buildIndex(current + "/src/Var/TestDocuments/infographic/XYFacets/");
        indexreader.printSchema();

        indexreader.printAllFacets();
        indexreader.printAllIndexes();

    }

}
