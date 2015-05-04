'''
Created on 2015/2/18

@author: divinityelle
'''


import os, re

solr_XML_path = "/Users/divinityelle/Desktop/AXP/XML_Solr_Non_Stemmed/"
new_solr_xml_path = "/Users/divinityelle/Desktop/AXP/XML_solr_new/"
IMscore_solr_xml_path = "/Users/divinityelle/Desktop/AXP/XML_solr_IMscore/"
if not os.path.exists(new_solr_xml_path ): os.makedirs(new_solr_xml_path, 0755)
if not os.path.exists(IMscore_solr_xml_path ): os.makedirs(IMscore_solr_xml_path, 0755)
            
def addIM():
    for set_no in os.listdir(solr_XML_path):
        if not os.path.isfile(solr_XML_path + set_no): 
            print set_no, "----------"
            if not os.path.exists(new_solr_xml_path + set_no): os.makedirs(new_solr_xml_path + set_no , 0755)
            
            for xml in os.listdir(solr_XML_path + set_no):
                handmade_XML_path = "/Users/divinityelle/Desktop/AXP/XML/"
                handmade_set = set_no + "_XML/"
                handmade_XML = xml.replace("_exp.txt", ".xml")
                handmade_handler = open(handmade_XML_path + handmade_set + handmade_XML, 'r')
                xml_content = "".join ( handmade_handler.readlines()) 
                im_start = re.search( "<subcategory>" , xml_content )
                im_end = re.search( "</subcategory>" , xml_content )
                im = xml_content[ im_start.end(): im_end.start()].strip()
                im_cat = im[ : re.search("{", im).start() ]
                handmade_handler.close()
                print xml, im_cat
                
                solr_xml_handler = open( solr_XML_path + set_no + "/" + xml, 'r' )
                solr_xml_content = "".join ( solr_xml_handler.readlines() )
                new_solr_xml = open(new_solr_xml_path + set_no + "/" + xml, 'w')
                new_solr_xml.write(solr_xml_content +"<IMCategory>\n" + im_cat + "\n</IMCategory>")
                new_solr_xml.close()

def IMScoreHelper(IMlabel):
    if re.search("Trend", IMlabel): 
        score = "<Query-Get-Rank>0</Query-Get-Rank>" + "\n" \
            + "<Query-Maximum-Minimum-Multiple>0</Query-Maximum-Minimum-Multiple>"  + "\n" \
            + "<Query-Maximum-Minimum-Single>0</Query-Maximum-Minimum-Single>" + "\n" \
            + "<Query-General-Multiple>0</Query-General-Multiple>" + "\n" \
            + "<Query-General-Single>0</Query-General-Single>" + "\n" \
            + "<Query-Relative-Difference>0</Query-Relative-Difference>" + "\n" \
            + "<Query-Rank-All>0</Query-Rank-All>" + "\n" \
            + "<Query-Trend>6</Query-Trend>"
    elif IMlabel == "Rank":
        score = "<Query-Get-Rank>6</Query-Get-Rank>"  + "\n" \
            + "<Query-Maximum-Minimum-Multiple>4</Query-Maximum-Minimum-Multiple>" + "\n" \
            + "<Query-Maximum-Minimum-Single>4</Query-Maximum-Minimum-Single>" + "\n" \
            + "<Query-General-Multiple>6</Query-General-Multiple>" + "\n" \
            + "<Query-General-Single>6</Query-General-Single>" + "\n" \
            + "<Query-Relative-Difference>1</Query-Relative-Difference>" + "\n" \
            + "<Query-Rank-All>5</Query-Rank-All>" + "\n" \
            + "<Query-Trend>0</Query-Trend>" 
    elif IMlabel == "Relative_Difference":
        score =  "<Query-Get-Rank>2</Query-Get-Rank>" + "\n" \
            + "<Query-Maximum-Minimum-Multiple>2</Query-Maximum-Minimum-Multiple>" + "\n" \
            + "<Query-Maximum-Minimum-Single>2</Query-Maximum-Minimum-Single>" + "\n" \
            + "<Query-General-Multiple>6</Query-General-Multiple>" + "\n" \
            + "<Query-General-Single>6</Query-General-Single>" + "\n" \
            + "<Query-Relative-Difference>6</Query-Relative-Difference>" + "\n" \
            + "<Query-Rank-All>4</Query-Rank-All>" + "\n" \
            + "<Query-Trend>0</Query-Trend>"  
    elif IMlabel == "Rank_All":
        score = "<Query-Get-Rank>4</Query-Get-Rank>" + "\n" \
            + "<Query-Maximum-Minimum-Multiple>5</Query-Maximum-Minimum-Multiple>" + "\n" \
            + "<Query-Maximum-Minimum-Single>5</Query-Maximum-Minimum-Single>" + "\n" \
            + "<Query-General-Multiple>6</Query-General-Multiple>" + "\n" \
            + "<Query-General-Single>6</Query-General-Single>" + "\n" \
            + "<Query-Relative-Difference>2</Query-Relative-Difference>" + "\n" \
            + "<Query-Rank-All>6</Query-Rank-All>" + "\n" \
            + "<Query-Trend>1</Query-Trend>" 
    elif IMlabel == "Min" or IMlabel == "Max":
        score = "<Query-Get-Rank>3</Query-Get-Rank>" + "\n" \
            + "<Query-Maximum-Minimum-Multiple>6</Query-Maximum-Minimum-Multiple>" + "\n" \
            + "<Query-Maximum-Minimum-Single>6</Query-Maximum-Minimum-Single>" + "\n" \
            + "<Query-General-Multiple>6</Query-General-Multiple>" + "\n" \
            + "<Query-General-Single>6</Query-General-Single>" + "\n" \
            + "<Query-Relative-Difference>1</Query-Relative-Difference>" + "\n" \
            + "<Query-Rank-All>5</Query-Rank-All>" + "\n" \
            + "<Query-Trend>0</Query-Trend>" 
    elif IMlabel == "General":
        score = "<Query-Get-Rank>0</Query-Get-Rank>" + "\n" \
            + "<Query-Maximum-Minimum-Multiple>0</Query-Maximum-Minimum-Multiple>" + "\n" \
            + "<Query-Maximum-Minimum-Single>0</Query-Maximum-Minimum-Single>" + "\n" \
            + "<Query-General-Multiple>6</Query-General-Multiple>" + "\n" \
            + "<Query-General-Single>6</Query-General-Single>" + "\n" \
            + "<Query-Relative-Difference>0</Query-Relative-Difference>" + "\n" \
            + "<Query-Rank-All>0</Query-Rank-All>" + "\n" \
            + "<Query-Trend>0</Query-Trend>" 
    elif IMlabel == "Contrast_Point":
        score = "<Query-Get-Rank>0</Query-Get-Rank>" + "\n" \
            + "<Query-Maximum-Minimum-Multiple>0</Query-Maximum-Minimum-Multiple>" + "\n" \
            + "<Query-Maximum-Minimum-Single>0</Query-Maximum-Minimum-Single>" + "\n" \
            + "<Query-General-Multiple>6</Query-General-Multiple>" + "\n" \
            + "<Query-General-Single>6</Query-General-Single>" + "\n" \
            + "<Query-Relative-Difference>6</Query-Relative-Difference>" + "\n" \
            + "<Query-Rank-All>0</Query-Rank-All>" + "\n" \
            + "<Query-Trend>6</Query-Trend>" 
    else:
        print "Horror!!!"   
    return score

   
def IMScoreHelper2(set_no, xml, xmlcontent):
    new_xmlcontent = ""
    for line in xmlcontent.split("\n"):
        if re.match("\<[a-zA-Z]", line.strip()):
            if re.search("</", line.strip()):
                line = line[: re.search("</", line).start()]
                new_xmlcontent += line.strip().replace("<", "<field name=\"").replace(">", "\">")  + "</field>\n"
            else:
                new_xmlcontent += line.replace("<", "<field name=\"").replace(">", "\">")  + "\n"    
        elif re.match("</", line.strip()):
            new_xmlcontent += "</field>\n"
        else:
            new_xmlcontent += line + "\n"
    new_xmlcontent = "<add><doc>\n<field name=\"id\">" + set_no + "_" + xml.replace("_exp.txt", ".xml") + "</field>\n" + new_xmlcontent + "</doc></add>"
    return new_xmlcontent

def addIMScore():   
    for set_no in os.listdir(new_solr_xml_path):
        if not os.path.isfile(new_solr_xml_path + set_no): 
            print set_no, "----------"
            if not os.path.exists(IMscore_solr_xml_path + set_no): os.makedirs(IMscore_solr_xml_path + set_no, 0755)
            for xml in os.listdir(new_solr_xml_path + set_no):
                if re.search("DS_Store", xml): continue
                solr_xml_handler = open(new_solr_xml_path + set_no + '/' + xml)
                xmlcontent = ''.join( solr_xml_handler.readlines() )
                
                IMstart = re.search("<IMCategory>", xmlcontent ) 
                IMend = re.search("</IMCategory>", xmlcontent)
                IMlabel =  xmlcontent[IMstart.end():IMend.start()].strip()
                score = IMScoreHelper(IMlabel)
                new_xmlcontent = IMScoreHelper2(set_no, xml, xmlcontent + "\n" + score)
                xml_writer = open(IMscore_solr_xml_path + set_no + "/" + xml.replace(".txt", ".xml"), 'w')
                xml_writer.write(new_xmlcontent)
                xml_writer.close()
if __name__ == '__main__':
    addIMScore()




                
                