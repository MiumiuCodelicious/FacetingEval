package Document;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author Jewel Li on 15-4-3. ivanka@udel.edu
 *
 * Generic document class.
 */

public abstract class Document extends Object{
    protected String docID;
    protected String filelocation;
    protected String plainContent;
    protected String processedContent;
    protected boolean stem;

    /**
     * @param filelocation the path to the file
     * Constructor with minimum control
     * */
    public Document(String filelocation, boolean stem_choice){
        stem = stem_choice;
        this.filelocation = filelocation;
        setDefautDocID(filelocation);
        setPlainContent();
        setProcessedContent();
    }

    /**
     * Constructor give control to set docID
     * */
    public Document(String filelocation, String docID, boolean stem_choice){
        this(filelocation, stem_choice);
        this.docID = docID;
    }

    /* Constructor 3 give control to set location, professed file content, and docID */
    public Document(String filelocation, String filecontent, String docID, boolean stem){
        this(filelocation, docID, stem);
        this.processedContent = filecontent;
    }



    /**
     * The default docID will be the document name
     * Will cause ERROR if your documents have duplicate names!
     * */
    private void setDefautDocID(String filelocation){
        this.docID = filelocation.substring(filelocation.lastIndexOf("/") + 1, filelocation.length());
    }

    private void setPlainContent(){
        this.plainContent = readdoc(this.filelocation);
    }

    private void setProcessedContent(){
        this.processedContent = process(this.plainContent, stem);
    }


    public String getDocID(){
        return this.docID;
    }
    public String getFileLocation(){
        return this.filelocation;
    }
    public String getPlainContent(){
        return this.plainContent;
    }
    public String getProcessedContent(){
        return this.processedContent;
    }

    /**
     * Static method readdoc() can read any pure text document given it's file location
     * */
    public static String readdoc(String filelocation) {
        String readstr = "";
        File f = new File(filelocation);
        if (f.exists() && f.isFile()){
            try{
                StringBuilder sbuild = new StringBuilder();
                BufferedReader br = new BufferedReader( new FileReader(f) );
                String line;
                while( (line = br.readLine()) != null ){
                    sbuild.append(line + "\n");
                }
                readstr = sbuild.toString();
                br.close();
            }catch (IOException  e){
                System.out.println("Crashed in Document.Document.readdoc() ");
                e.printStackTrace();
            }
            return readstr;
        }else
            return null;
    }

    /**
     * @param filecontent String content of the file
     * A basic document processing function
     * Lowercase
     * Remove anything that is not made of alphabets, "|", or numbers: punctuations, 's, stop words
     * "|" is maintained because it is used as facet value deliminiter.
    * */

    public static String process(String filecontent, boolean stem){
        filecontent = filecontent.replaceAll("[^a-zA-Z0-9\\|\\s]", " ").replaceAll("\\s+", " ");
        if (stem == true) {
            filecontent = Utility.StopWordHandler.removeStopWord(filecontent.toLowerCase());
        }
        return filecontent;
    }

    public String toString(){
        return getClass().getName() + " [ID = " + this.getDocID() + ", file location = " + this.getFileLocation() + "\nplain content:\n" +
                this.getPlainContent() + "\nprocessed content:\n" + this.getProcessedContent();
    }


}
