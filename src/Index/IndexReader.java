package Index;

import Document.InfographicXML;
import Document.LuceneSolrXML;
import Document.PlainText;
import Utility.Options;

import java.util.HashMap;
import java.io.File;

/**
 * Created by Jewel Li on 15-4-5. ivanka@udel.edu
 */
public class IndexReader implements IndexReaderInterface {

    private HashMap<String, InverseIndex> indexBucket;
    private HashMap<String, InverseIndex> facetBucket;
    private String doctype;

    /* Constructor */
    public IndexReader(){

    }



    /* build index from a given directory. the directory must be flat-structured */
    public void buildIndex(String dir){
        indexBucket = new HashMap<String, InverseIndex>();
        facetBucket = new HashMap<String, InverseIndex>();
        setSchemaFields();
        readdocs(dir, this.getDoctype());
    }



    /*
    * @TODO Here here. Add a private HashSet<String> facet;
    * Then in readSchema(), detect defined facet.
    * Then in IndexReader, all need to be updated as well.
    * */

    /* get schema for multi-field XML or treat as plain document */
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
                if (doc.getName().contains(".xml")) {
                    addSingleDoc(doctype, doc.getAbsolutePath());
                }
            }
        }

    }



    private void addSingleDoc(String doctype, String docpath){
        if (doctype.equals("luncene") || doctype.equals("infographic")) {
            LuceneSolrXML lucenedoc = new LuceneSolrXML(docpath);
            addDocFacet(lucenedoc);
        }
        for (String field : indexBucket.keySet()){
            if (doctype.equals("plaintext")){
                PlainText plaintextdoc = new PlainText(docpath);
                indexBucket.get(field).adddoc(plaintextdoc, " ");
                if (Options.DEBUG == true) {
                    System.out.println("Added plain text " + plaintextdoc.getDocID() + " to index.");
                }
            }else if (doctype.equals("lucene")){
                LuceneSolrXML lucenedoc = new LuceneSolrXML(docpath);
                indexBucket.get(field).adddoc(lucenedoc, field);
                if (Options.DEBUG == true) {
                    System.out.println("Added lucene/solr XML " + lucenedoc.getDocID() + " field " + field + " to index.");
                }
            }else if (doctype.equals("infographic")){
                InfographicXML infographicdoc = new InfographicXML(docpath);
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


    /* write entire index to file
    * @TODO
    * Haven't figured this out yet..
    * */
    public void writeIndex(){}


    public String getDoctype(){
        return this.doctype;
    }

    /* Print a single field index */
    public void printIndex(String field){
        printIndex(indexBucket, field);
    }

    /* Print each field index in the entire index bucket */
    public void printAllIndexes(){
        printAllIndexes(indexBucket);
    }

    /* Print a single facet index */
    public void printFacet(String facet){
        printIndex(facetBucket, facet);
    }

    /* Print each facet index in the entire facet bucket */
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

    /* Indexed fields and facets defined in schema.xml should be the keys of indexBucket and keys of facetBucket */
    public void printSchema(){
        System.out.println("Schema defined indexed fields: " + indexBucket.keySet().toString());
        System.out.println("Schema defined facet fields: " + facetBucket.keySet().toString());
    }

    public static void main (String args[]){
        IndexReader indexreader = new IndexReader();
        String current = System.getProperty("user.dir");
        indexreader.buildIndex(current + "/src/TestDocuments/infographic/smallerXYFacets/");
        indexreader.printSchema();

        indexreader.printAllFacets();
        indexreader.printAllIndexes();

    }

}
