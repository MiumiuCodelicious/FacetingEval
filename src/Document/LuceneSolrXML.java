package Document;

import java.lang.reflect.Array;
import java.util.ArrayList;
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

    protected HashMap<String, ArrayList> fieldMap;

    public LuceneSolrXML(String XMLpath){
        super(XMLpath);
        setProcessedContent();
        setFieldMap();
    }

    public LuceneSolrXML(String XMLpath, String XMLid){
        super(XMLpath, XMLid);
        setProcessedContent();
        setFieldMap();
    }

    private void setProcessedContent(){
        this.processedContent = process(this.plainContent);
    }

    private void setFieldMap(){
        this.fieldMap = getFields();
    }

    public HashMap<String, ArrayList> getFieldMap(){
        return this.fieldMap;
    }

    /*
     * Simply remove all XML tags and nothing else
     * If needed, modify this method to use more complex parser for more complex XMLs
     * */
    public static String process(String filecontent){

        Pattern fieldtag = Pattern.compile("<( *)field( *)name( *)=( *)\".*\"( *)>");
        Pattern adddoctag = Pattern.compile("<( *)((add)|(doc))( *)>");
        Pattern closetag = Pattern.compile("<( *)/((add)|(doc)|(field))( *)>");

        if (filecontent != null) {
            if (filecontent.length() < 2) return "";
            filecontent = Document.process(filecontent.replaceAll("(" + fieldtag.toString() + ")|(" + closetag.toString() + ")|(" + adddoctag.toString() + ")", "")) ;
        }
        return filecontent;
    }



    /*
     * Extract each "field"-"content list" list pair into HashMap
     * Field name will be keys in the returned HashMap
     * Field values (use list because multiple values might exist in XML) are values in the returned HashMap
     * */
    public HashMap<String, ArrayList> getFields(){
        Pattern fieldtag_excl_name = Pattern.compile("<( *)field( *)name( *)=( *)\"");
        Pattern fieldtag = Pattern.compile("<( *)field( *)name( *)=( *)\".*\"( *)>");
        Pattern closeFieldtag = Pattern.compile("<( *)/field( *)>");
        Pattern adddoctag = Pattern.compile("<( *)/?((add)|(doc))( *)>");
        String filecontent;

        this.fieldMap = new HashMap<String, ArrayList>();

        if (this.plainContent != null) {
            if (this.plainContent.length() < 2) return null;
            filecontent = this.plainContent.replaceAll(adddoctag.toString(), "");

            Matcher fieldtag_excl_name_matcher = fieldtag_excl_name.matcher(filecontent);
            Matcher fieldtag_matcher = fieldtag.matcher(filecontent);
            Matcher closeFieldtag_matcher = closeFieldtag.matcher(filecontent);

            while (fieldtag_excl_name_matcher.find() && fieldtag_matcher.find()) {

                String field_name = filecontent.substring(fieldtag_excl_name_matcher.end(), fieldtag_matcher.end()).replaceAll("\"( *)>", "");
                String field_content = "";

                if (closeFieldtag_matcher.find()) {
                    field_content = Document.process(filecontent.substring(fieldtag_matcher.end(), closeFieldtag_matcher.start()).trim());
                }

                ArrayList<String> value = new ArrayList<String>();
                if ( this.fieldMap != null && this.fieldMap.containsKey(field_name)) {
                    value = this.fieldMap.get(field_name);
                }
                if (value.add(field_content) && this.fieldMap!=null) {
                    this.fieldMap.put(field_name, value);
                }


            }

        }
        return this.fieldMap;

    }




    public String toString(){

        String objstr = getClass().getName() + " [ID = " + this.getDocID() + ", file location = " + this.getFileLocation() + "\nplain content:\n" +
                this.getPlainContent() + "\nprocessed content:\n" + this.getProcessedContent();

        if(this.getFieldMap()!=null){
            objstr += "\nfields:\n{";
            for (String field : this.getFieldMap().keySet()){
                String values = "[";
                for (int i = 0; i < this.getFieldMap().get(field).size(); i ++){
                    values += this.getFieldMap().get(field).get(i) + ", ";
                }
                if(values.endsWith(", ")) values = values.substring(0, values.length()-2);
                objstr += field + "=" + values + "], ";
            }
            if(objstr.endsWith(", ")) objstr = objstr.substring(0, objstr.length()-2);
            objstr += "}";
        }
        return objstr;
    }




    /*
    * Test main function
    * */
    public static void main(String args[])  {

        String testXMLpath = "/Users/divinityelle/Documents/FacetingEval/src/Document/testSolrXML.xml";
        System.out.println(" ------------ Testing Class LuceneSolrXML ------------ ");
        LuceneSolrXML testXML = new LuceneSolrXML(testXMLpath);

        System.out.println(testXML.toString());

    }

}


