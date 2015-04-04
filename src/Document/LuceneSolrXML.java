package Document;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
/**
 * Created by Jewel Li on 15-4-3. ivanka@udel.edu
 *
 * Document objects for standard XML used by Lucene and Solr.
 *
 * Note!!!: this class process Lucene/Solr XMLs assuming all XML fields start with a new line!!!
 *
 * Example XML below:
 * <add>
 * <doc>
 * <field name="id">USD</field>
 * <field name="name">One Dollar</field>
 * <field name="manu">Bank of America</field>
 * <field name="manu_id_s">boa</field>
 * <field name="cat">currency</field>
 * <field name="features">Coins and notes</field>
 * <field name="price_c">1,USD</field>
 * <field name="inStock">true</field>
 * </doc>
 * </add>
 */
public class LuceneSolrXML extends Document{

    protected HashMap<String, String[]> fieldMap;

    public LuceneSolrXML(String XMLpath){
        super(XMLpath);
        setProcessedContent();
        this.fieldMap = getXMLFields(this.plainContent);
    }

    public LuceneSolrXML(String XMLpath, String XMLid){
        super(XMLpath, XMLid);
        setProcessedContent();
        this.fieldMap = getXMLFields(this.plainContent);
    }

    private void setProcessedContent(){
        this.processedContent = stripXML(this.plainContent);
    }


    public HashMap<String, String[]> getFieldMap(){
        return this.fieldMap;
    }

    /*
     * Simply remove all XML tags and nothing else
     * If needed, modify this method to use more complex parser
     * */
    public static String stripXML(String filecontent){

        Pattern fieldtag = Pattern.compile("<( *)field( *)name( *)=( *)\".*\"( *)>");
        Pattern adddoctag = Pattern.compile("<( *)((add)|(doc))( *)>");
        Pattern closetag = Pattern.compile("<( *)/((add)|(doc)|(field))( *)>");

        if (filecontent != null) {
            if (filecontent.length() < 2) return "";
            filecontent = Document.basicProcess(  filecontent.replaceAll("(" + fieldtag.toString() + ")|(" + closetag.toString() + ")|(" + adddoctag.toString() + ")", "") ) ;
        }
        return filecontent;
    }



    /*
     * Extract each "field"-"content list" list pair into HashMap
     * Field name will be keys in the returned HashMap
     * Field values (use list because multiple values might exist in XML) are values in the returned HashMap
     * */
    public static HashMap<String, String[]> getXMLFields(String filecontent){
        Pattern fieldtag_excl_name = Pattern.compile("<( *)field( *)name( *)=( *)\"");
        Pattern fieldtag = Pattern.compile("<( *)field( *)name( *)=( *)\".*\"( *)>");
        Pattern closeFieldtag = Pattern.compile("<( *)/field( *)>");
        Pattern adddoctag = Pattern.compile("<( *)/?((add)|(doc))( *)>");

        HashMap<String, String[]> fieldMap = new HashMap<String, String[]>();

        if (filecontent != null) {
            if (filecontent.length() < 2) return null;
            filecontent = filecontent.replaceAll(adddoctag.toString(), "");

            Matcher fieldtag_excl_name_matcher = fieldtag_excl_name.matcher(filecontent);
            Matcher fieldtag_matcher = fieldtag.matcher(filecontent);
            Matcher closeFieldtag_matcher = closeFieldtag.matcher(filecontent);

            while (fieldtag_excl_name_matcher.find() && fieldtag_matcher.find()) {

                String field_name = filecontent.substring(fieldtag_excl_name_matcher.end(), fieldtag_matcher.end()).replaceAll("\"( *)>", "");
                String[] field_content = {""};

                if (closeFieldtag_matcher.find()) {
                    field_content[0] = Document.basicProcess(filecontent.substring(fieldtag_matcher.end(), closeFieldtag_matcher.start()).trim() );
                }

                if ( fieldMap.containsKey(field_name) ){
                    String[] newvalue = new String[fieldMap.get(field_name).length + 1];
                    System.arraycopy( fieldMap.get(field_name), 0, newvalue, 0, fieldMap.get(field_name).length);
                    System.arraycopy( field_content, 0, newvalue, fieldMap.get(field_name).length, newvalue.length);
                    fieldMap.put(field_name, newvalue);
                }else{
                    fieldMap.put(field_name, field_content);
                }
            }

        }
        return fieldMap;

    }

    /*
    * Test main function
    * */
    public static void main(String args[]) throws IOException {

        String testXMLpath = "/Users/divinityelle/Documents/FacetingEval/src/Document/testSolrXML.xml";
        System.out.println(" ------------ Testing Class LuceneSolrXML ------------ ");
        LuceneSolrXML testXML = new LuceneSolrXML(testXMLpath);

        String filecontent = LuceneSolrXML.readdoc(testXMLpath);

        System.out.println( "Plain Solr XML: \n" + testXML.getPlainContent()) ;
        System.out.println( "Processed (stripped) Solr XML: \n" + testXML.getProcessedContent())  ;

        HashMap<String, String[]>  XMLfields = LuceneSolrXML.getXMLFields(filecontent);
        System.out.println("Solr XML Field - Values: ");

        if (XMLfields != null){
            for (String field : XMLfields.keySet()){
                String values = "";
                for (String value : XMLfields.get(field)){
                    values += value + "\n";
                }
                System.out.println(field + ": \n" + values );
            }
        }

    }


}


