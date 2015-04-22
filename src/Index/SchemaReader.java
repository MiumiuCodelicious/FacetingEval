package Index;

import java.io.*;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author Jewel Li on 15-4-5. ivanka@udel.edu
 * Default (..and hard coded..) path to schema.xml is under /src/schema.xml
 * @TODO Instead of hard coding schema file path, let users define it in config.xml
 * @TODO Use Java XML document processor!
 */
public class SchemaReader {

    private HashSet<String> indexed;
    private HashSet<String> stored;

    private HashSet<String> facets;

    private String doctype = "";
    String current = System.getProperty("user.dir");
    String SCHEMA_LOCATION = current + "/src/schema.xml";

    public SchemaReader(){
        this.indexed = new HashSet<String>();
        this.stored = new HashSet<String>();
        this.facets = new HashSet<String>();
    }

    public int readSchema(){

        File schema = new File(SCHEMA_LOCATION);
        if(!schema.exists() || !schema.isFile()){
            System.out.println("schema.xml not found at:" + SCHEMA_LOCATION);
            return -1;
        }


        Pattern namep = Pattern.compile("name( *)=( *)\""); Pattern namep_end = Pattern.compile("name( *)=( *)\"([a-zA-Z0-9_-]+)\"");
        String fieldname;

        try {
            BufferedReader schemaBuff = new BufferedReader(new FileReader(schema));
            String fieldline = "";
            while ( (fieldline = schemaBuff.readLine()) != null ){
                if (fieldline.trim().startsWith("<schema")){
                    this.doctype = getFieldValue("name", fieldline);
                }
                if (fieldline.trim().startsWith("<field")) {
                    Matcher namem = namep.matcher(fieldline); Matcher namem_end = namep_end.matcher(fieldline);
                    if (namem.find() && namem_end.find()) {
                        fieldname = fieldline.substring(namem.end(), namem_end.end() - 1);
                        if (fieldname.contains("facet")){
                            addField(fieldname, "faceted", fieldline, this.facets);
                        }else {
                            addField(fieldname, "indexed", fieldline, this.indexed);
                            addField(fieldname, "stored", fieldline, this.stored);
                        }

                    }else{
                        System.out.println("Error. Field name can only contain the following characters: a-z, A-Z, 0-9, \"-\", and \"_\". "); return -1;
                    }
                }
            }
            return 1;
        }catch (IOException ioe){
            ioe.printStackTrace();
            return -1;
        }
    }

    /**
     * @param v     field attribute, "name", "indexed", "stored", "multiValued", "faceted"
     * @param line  a line in schema.xml
     * @return the actual value
     */
    private String getFieldValue(String v, String line) {
        Pattern p = Pattern.compile(v + "( *)=( *)\"");
        Pattern p_end = Pattern.compile(v + "( *)=( *)\"([a-zA-Z0-9_-]+)\"");
        Matcher m = p.matcher(line);
        Matcher m_end = p_end.matcher(line);
        if (m.find() && m_end.find()) {
            return line.substring(m.end(), m_end.end() - 1).toLowerCase();
        } else {
            return "";
        }
    }

    /**
     * @param fieldname     user defined XML field name
     * @param v             the attribute of this field
     * @param line          a line in schema.xml
     * @param set           which field set to add this field to
     */
    private void addField(String fieldname, String v, String line, HashSet<String> set){
        String value = getFieldValue(v, line);
        if (value != null && value.equals("true")){
            set.add(fieldname);
        }
    }


    public HashSet<String> getIndexed(){
        return this.indexed;
    }

    public HashSet<String> getStored(){
        return this.stored;
    }

    public HashSet<String> getFacets(){
        return this.facets;
    }

    public String getDocType(){
        return this.doctype;
    }

    public static void main(String args[]){
        SchemaReader schemareader = new SchemaReader();
        if (schemareader.readSchema() != -1 ) {
            System.out.println("Fields to index: " + schemareader.getIndexed().toString());
            System.out.println("Fields to store: " + schemareader.getStored().toString());
            System.out.println("Fields to facet: " + schemareader.getFacets().toString());
            System.out.println("Document type: " + schemareader.getDocType());
        }else{
            System.out.println("readSchema() failed");
        }
    }

}
