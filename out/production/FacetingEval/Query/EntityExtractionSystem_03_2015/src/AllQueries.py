'''
Created on Apr 26, 2012

@author: fifille
'''
import sys,os,subprocess

from TreeStructure.ReadTree import *
from EntityExtract import *
from SuperAdj import *


'''
 Attributes ---------------------------------------------------------------------------------------------
'''
        
def superlativeAttr(ptree, posdict, EntityChunks, EntityRoots):
    '''
     Sentence has superlative, entity has superlative
    '''
    from Attributes.SuperlativeAttr import SuperlativeAttr
    superAttr = SuperlativeAttr(ptree, posdict, EntityChunks, EntityRoots)
    supInSentence = superAttr.superInSent()
    suplist = [0 for i in range(len(EntityChunks))]    
    supInNP = superAttr.superInNP(suplist)
    JJRInSent_1 = superAttr.JJRInSent()
    # 1, 16 -----------------------------------
    return supInSentence, supInNP, JJRInSent_1
    

def qtcAttr(ptree, posdict, EntityChunks, EntityRoots):
    '''
     Trend and Comparison in sentence, gradient, quantity, comparison, and trend in entity
    ''' 
    from Attributes.QuantTrendCompareAttr import QuantTrendCompareAttr
    qtcAttr = QuantTrendCompareAttr(ptree, posdict, EntityChunks, EntityRoots)
    tInSentence = qtcAttr.qtcInSent(qtcAttr.trend)
    
    cInSentence = qtcAttr.qtcInSent(qtcAttr.compare)

    qtclist = [0 for i in range(len(EntityChunks))]
    gInNP = qtcAttr.qtcInEntity(qtcAttr.gradient, qtclist)
    
    qtclist = [0 for i in range(len(EntityChunks))]
    qInNP = qtcAttr.qtcInEntity(qtcAttr.quant, qtclist)

    qtclist = [0 for i in range(len(EntityChunks))]
    cInNP = qtcAttr.qtcInEntity(qtcAttr.compare, qtclist)   
    
    qtclist = [0 for i in range(len(EntityChunks))]
    tInNP = qtcAttr.qtcInEntity(qtcAttr.trend, qtclist)
    
    alllist = [0 for i in range(len(EntityChunks))]
    allInNP_22 = qtcAttr.qtcInEntity(qtcAttr.all, alllist)
    
    otherlist = [0 for i in range(len(EntityChunks))]
    otherInNP_23 = qtcAttr.qtcInEntity(qtcAttr.other, otherlist)
    
    eachlist =  [0 for i in range(len(EntityChunks))]
    eachInNP_24 = qtcAttr.qtcInEntity(qtcAttr.each, eachlist)
          
    modifiedList = [[] for i in range(len(EntityChunks))]
    modifiedByQuant_25 = qtcAttr.modifyInEntity(qtcAttr.quant, modifiedList)
    
    modifiedList = [[] for i in range(len(EntityChunks))]
    modifiedByTrend_26 = qtcAttr.modifyInEntity(qtcAttr.trend, modifiedList)
    
    modifiedList = [[] for i in range(len(EntityChunks))]
    modifiedByComp_27 = qtcAttr.modifyInEntity(qtcAttr.compare, modifiedList)
    
    modifiedList = [[] for i in range(len(EntityChunks))]
    modifiedByGrad_28 = qtcAttr.modifyInEntity(qtcAttr.gradient, modifiedList)
    
    # 2, 3, 17, 18, 19, 20 --------------------------------------
    return tInSentence, cInSentence, gInNP, qInNP, cInNP, tInNP, allInNP_22, otherInNP_23, eachInNP_24, \
            modifiedByQuant_25, modifiedByTrend_26, modifiedByComp_27, modifiedByGrad_28

   
    
def whichAttr(ptree, posdict, EntityChunks, EntityRoots):
    '''
     which sent, which noun
    '''
    from Attributes.Which_N_Attr import Which_N_Attr
    whichAttr = Which_N_Attr(ptree, posdict, EntityChunks, EntityRoots)
    whichlist = [0 for i in range(len(EntityChunks))]
    isWhich = whichAttr.WhichSent(whichAttr.whichr)
    whichEntity = whichAttr.WhichEntity(isWhich, whichAttr.whichr, whichlist)
    # 4, 21 -------------------------------------------------------
    return isWhich, whichEntity
    
    
def whatAttr(ptree, posdict, EntityChunks, EntityRoots):
    '''
     What sentence, what-noun
    '''
    from Attributes.What_N_Attr import What_N_Attr
    whatAttr = What_N_Attr(ptree, posdict, EntityChunks, EntityRoots)
    isWhat = whatAttr.WhatSent()
    whatEntity = whatAttr.WhatEntity()
    # 4, 21 -------------------------------------------------------
    return isWhat, whatEntity
    
        

def howXAttr(ptree, posdict, EntityChunks, EntityRoots):
    '''
     How many/much sentence, many/much noun
    '''
    from Attributes.How_X_Attr import How_X_Attr
    howx = How_X_Attr(ptree, posdict, EntityChunks, EntityRoots)
    
    howManyMuch = howx.howXSent(howx.manyr) or howx.howXSent(howx.muchr)
    howDoHave = howx.howXSent(howx.dor) or howx.howXSent(howx.haver)
    
    manymuch_list = [0 for i in range(len(EntityChunks))]
    howManyMuchEntity = howx.howManyMuch(manymuch_list)
    
    dohave_list = [0 for i in range(len(EntityChunks))]
    howDoHaveEntity = howx.howDoHaveEntity(dohave_list)
    
    # 5, 6, 22, 23 --------------------------------------------------
    return howManyMuch, howDoHave, howManyMuchEntity, howDoHaveEntity


    
def nounAttr(ptree, posdict, EntityChunks, EntityRoots):
    '''
     Head noun, Full noun, Complex noun, Plural noun
     Whether entities are connected by conjunctions
     Whether entities precede or procede Proper Noun like "from", "in", ...
     Whether entities contain determiner, such as "a", "the", "an"...
    '''
    from Attributes.NounAttr import NounAttr
    nounAttr = NounAttr(ptree, posdict, EntityChunks, EntityRoots)
    #nounAttr.printQTC()
    conjlist =  [0 for i in range(len(EntityChunks))]
    full_vec = [0 for i in range(len(EntityChunks))]
    complexlist = [0 for i in range(len(EntityChunks))]
    headlist = [0 for i in range(len(EntityChunks))]
    plurallist = [0 for i in range(len(EntityChunks))]
    conjBetween = [0 for i in range(len(EntityChunks))]
    ppPreProcede = [0 for i in range(len(EntityChunks))]
    dtlist = [0 for i in range(len(EntityChunks))]
    endlist = [0 for i in range(len(EntityChunks))]
    containAdjlist = [0 for i in range(len(EntityChunks))]
    
    conj = nounAttr.conjAttr(conjlist)
    plural = nounAttr.isPlural(plurallist)
    complex = nounAttr.isComplexNoun(complexlist)
    head = nounAttr.isHeadNoun(headlist)
    full = nounAttr.isFullNoun(full_vec)
    manyfull = nounAttr.manyFullNoun()
    # ------- Added from Kelly's Suggestions
    conjBetween = nounAttr.conjunctedEntitiesAttr(conjBetween)
    ppPreProcede = nounAttr.ppAttr(ppPreProcede)
    dtlist = nounAttr.dtAttr(dtlist)
    endlist = nounAttr.lastEntityAttr(endlist)
    containAdjlist = nounAttr.containAdj(containAdjlist)
    # -------
    
    
    # 15, 29, 30, 31, 32 --------------------------
    return conj, plural, complex, head, full, manyfull, conjBetween, ppPreProcede, dtlist, endlist, containAdjlist



def whatIsAttr(ptree, posdict, EntityChunks, EntityRoots):
    '''
     What-Is sentence, What-Is noun
    '''
    from Attributes.What_Is_Attr import What_Is_Attr
    whatisAttr = What_Is_Attr(ptree, posdict, EntityChunks, EntityRoots)
    isWhatis = whatisAttr.WhatIsSent(whatisAttr.whatr)
    whatislist = [0 for i in range(len(EntityChunks))]
    whatisEntity = whatisAttr.WhatIsEntity(isWhatis, whatislist)
    # 7, 24 ------------------------------------
    return isWhatis, whatisEntity


def mainVerbAttr(ptree, posdict, EntityChunks, EntityRoots):
    '''
     main trend verb, main compare verb 
    '''
    from Attributes.MainVerbAttr import MainVerbAttr
    mverb = MainVerbAttr(ptree, posdict, EntityChunks, EntityRoots)
    mtrend, mcomp, compeos = mverb.isTrendVerb(), mverb.isCompareVerb(), mverb.compEndOfSent()
    leftlist = [0 for i in range(len(EntityChunks))]
    rightlist = [0 for i in range(len(EntityChunks))]
    cleftlist, crightlist, tleftlist, trightlist = mverb.sideOfVerb(leftlist, rightlist)
    compHasIN, comp_N_N_S, comp_N_all, comp_NS = mverb.compareVerbIN()
    # 8, 9, 10, 11, 12, 13, 14, 25, 26, 27, 28 ------------------------------------------------
    return mtrend, mcomp, compHasIN, comp_N_N_S, comp_N_all, comp_NS, compeos, \
            cleftlist, crightlist, tleftlist, trightlist 
   
    
'''
 Post Processing of the Entity Chunk List ------------------------------------------------------------
'''

def cutSuperlative(EntityChunks):
    '''
     get ride of superlatives of NP chunks
    '''
    newEntityChunks = []
    for en in EntityChunks:
        si = -1
        if len(en) > 2:
            for w in en[:2]:
                if re.match('(JJS)|(RBS)', w[1][1]):
                    si =  en.index(w)
                    break
        if len(en) <= 2:
            if re.match('(JJS)|(RBS)', en[0][1][1]):
                si = 0
        if si == len(en)-1: 
            newEntityChunks += [en]
        else:   newEntityChunks += [en[si+1:]]
    return newEntityChunks     
                
                
def addAdjectives(ptree, posdict, EntityChunks, EntityRoots):
    '''
     get superlative adj entities 
    '''
    newEntityChunks, newEntityRoots = EntityChunks, EntityRoots
    from SuperAdj import SuperAdj
    sadj = SuperAdj(ptree, posdict)
    adj, adjroot = sadj.getSuperAdj()
    if adj != None and adjroot != None:
        newEntityChunks.append(adj)
        newEntityRoots.append(adjroot)
    return newEntityChunks, newEntityRoots


def timeInterval(ptree, posdict, EntityChunks, EntityRoots):
    timeType_41 = []
    from Attributes.TimeInterval import TimeInterval
    time = TimeInterval(ptree, posdict, EntityChunks, EntityRoots)
    timeType_41 = time.timeAttr()
#    print len(timeType_41)
#    for e in time.EntityChunks: print e
    return time.EntityChunks, time.EntityRoots, timeType_41
#    if ttype == 0:
#        return 0, EntityChunks, EntityRoots, low, high
#    elif ttype != 0:
#        timeEntity = []         # ******** Fix this: not only 1 time entity ??
#        for i in range(low, high+1):
#            timeEntity.append([i, posdict[i]])
#        EntityChunks.append(timeEntity)
#        return ttype, EntityChunks, EntityRoots, low, high
#    else:
#        return 0, EntityChunks, EntityRoots, low, high

    
    
#def timeAttr(EntityChunks, EntityRoots, low, high):               
#    rmv_enlist = []
#    rmv_rootlist = []
#    for en in EntityChunks:
#        if (en[0][0] >= low and en[-1][0] < high) or (en[0][0] > low and en[-1][0] <= high):
#            rmv_enlist.append(en)
#            rmv_rootlist.append(EntityRoots[EntityChunks.index(en)])
#                                
#    for r in rmv_enlist:
#        EntityChunks.remove(r)
#    for r2 in rmv_rootlist:
#        EntityRoots.remove(r2)
#        
#    timelist = [0 for e in EntityChunks]
#    for en in EntityChunks:
#        if en[0][0] == low and en[-1][0] == high:
#            timelist[EntityChunks.index(en)] = 1
#
#    return EntityChunks, EntityRoots, timelist
            
        
        
    
'''
 Main func for each query ----------------------------------------------------------------------------
'''
def chunkQuery(qno, single_parse_tree, print_choice):
    '''
        A single_parse_tree is a list read from a single parse tree by file.readlines()
        print_entities is a choice: 0, no printing entities just attributes; 1, yes print entities; 3, print both
    '''
    rt = ReadTree(single_parse_tree)
    entity = EntityExtract(rt.ptree, rt.posdict, rt.verbdict, rt.noundict)

    entity.EntityChunker(entity.ptree.root)     
    
#    print len(entity.EntityChunks), len(entity.EntityRoots)
    entity.onlyNP()
    entity.truncate()
    entity.keepMinimal()

#     print "Originally extracted entities (before removing time entities) include------------- "
#     for en in entity.EntityChunks:
#         entity_phrase = ""
#         for word in en:
#             entity_phrase +=  word[1][0] + " "
#         print entity_phrase
#     print "----------------------------"
        
    # get time interval --------------------------------------------
    # ttype, EntityChunks, EntityRoots, low, high
#    ttype, timeEntityChunks, timeEntityRoots, low, high = timeInterval(entity.ptree, entity.posdict, entity.EntityChunks, entity.EntityRoots)
    timeEntityChunks, timeEntityRoots, timeType_41 = timeInterval(entity.ptree, entity.posdict, entity.MinimalEntityChunks, entity.MinimalEntityRoots)

        
    
    '''
     Extract Attributes
    '''
    
    # 1, 16 -------------------------------------------------------
    supInSentence_0, supInNP_17, JJRInSent_1 = superlativeAttr(entity.ptree, entity.posdict,  timeEntityChunks, timeEntityRoots)
    # 2, 3, 17, 18, 19, 20 ----------------------------------------
    tInSentence_2, cInSentence_3, gInNP_18, qInNP_19, cInNP_20, tInNP_21, allInNP_22, otherInNP_23, eachInNP_24, \
    modifiedByQuant_25, modifiedByTrend_26, modifiedByComp_27, modifiedByGrad_28 = \
                                                    qtcAttr(entity.ptree, entity.posdict,  timeEntityChunks, timeEntityRoots)
    # 4, 21 -------------------------------------------------------
    isWhich_4, whichEntity_21 = whichAttr(entity.ptree, entity.posdict, timeEntityChunks, timeEntityRoots)
    # 4, 21 -------------------------------------------------------
    isWhat_4, whatEntity_21 = whatAttr(entity.ptree, entity.posdict,  timeEntityChunks, timeEntityRoots)    
    # 5, 6, 22, 23 ------------------------------------------------
    howManyMuch_5, howDoHave_6, howManyMuchEntity_30, howDoHaveEntity_31 = \
                                                    howXAttr(entity.ptree, entity.posdict,  timeEntityChunks, timeEntityRoots)
    # 7, 24 ------------------------------------
    isWhatis_7, whatisEntity_32 = whatIsAttr(entity.ptree, entity.posdict,  timeEntityChunks, timeEntityRoots)    
    # 8, 9, 10, 11, 12, 13, 14, 25, 26, 27, 28 --------------------
    mtrend_8, mcomp_9, compHasIN_10, comp_N_N_S_11, comp_N_all_12, comp_NS_13, compeos_14, \
            cleftlist_33, crightlist_34, tleftlist_35, trightlist_36 = \
            mainVerbAttr(entity.ptree, entity.posdict,  timeEntityChunks, timeEntityRoots)
    # 15, 29, 30, 31, 32 --------------------------
    conj_16, plural_37, complex_38, head_39, full_40, manyfull_15, \
    conjBetween_16, ppPreProcede_17, dtlist_18, endlist_19, containAdjlist_20 = \
    nounAttr(entity.ptree, entity.posdict,  timeEntityChunks, timeEntityRoots)
    '''
     ------------------------------------------------------------------------------------------------------
    '''
    
    # EntityChunks, EntityRoots, timelist
                
    # cut away the superlative modifying desired Chunk -------------
#    cutEntityChunks = cutSuperlative(timeEntityChunks)

    # get superlative adj entities ---------------------------------
    #FinalEntityChunks, FinalEntityRoots = addAdjectives(entity.ptree, entity.posdict, cutEntityChunks2, entity.EntityRoots)
    
    '''
    if len(cutEntityChunks) < len(FinalEntityChunks):
        def addOn(list):
            return list+[0]
        map(addOn, [conj, supInNP, gInNP, qInNP, cInNP, tInNP, whichEntity, whatEntity, \
                    howManyMuchEntity, howDoHaveEntity, whatisEntity, \
                    cleftlist, crightlist, tleftlist, trightlist, \
                    plural, complex, head, full ])
    '''
    # --------------------------------------------------------------
    
    def printe(e):    
        printdict()
        s = ' '.join([l[1][0] for l in e])
        sys.stdout.write(s+'\n')

    def printdict():
        s = ' '.join([entity.posdict[key][0] for key in entity.posdict])
        sys.stdout.write(s+'\t')       

    def printsubtree(root):
        print entity.treeWalk(root, [])
    
    def printEntities():
#         sys.stdout.write("%s,%d\n" % ((qno+1), len(timeEntityChunks)) )
        sys.stdout.write("%s\n" % (qno+1))
        map(printe, timeEntityChunks)
        
    '''
    print 'Tree walk from root list --------------------'
    map(printsubtree, FinalEntityRoots)
    print
    '''
    
    if isWhich_4: whichwhat_29 = whichEntity_21
    else:    whichwhat_29 = whatEntity_21

#    for i in range(len(FinalEntityChunks)):
#        sys.stdout.write('%s ' % (isWhich_4 or isWhat_4))
#        sys.stdout.write('%s \n' %  whichwhat_29[i])

    def xyAttributes():
        attribute_matrix = []
        for i in range(len(timeEntityChunks)):
                
            attribute = [supInSentence_0, JJRInSent_1, tInSentence_2, cInSentence_3, (isWhich_4 or isWhat_4), \
                         howManyMuch_5, howDoHave_6, isWhatis_7, mtrend_8, \
                         mcomp_9, compHasIN_10, comp_N_N_S_11, comp_N_all_12, comp_NS_13, \
                         compeos_14, manyfull_15] \
                         +  \
                         [conj_16[i], supInNP_17[i], gInNP_18[i], qInNP_19[i], cInNP_20[i], tInNP_21[i], allInNP_22[i], otherInNP_23[i], \
                          eachInNP_24[i], modifiedByQuant_25[i], modifiedByTrend_26[i], modifiedByComp_27[i], modifiedByGrad_28[i], \
                          whichwhat_29[i], howManyMuchEntity_30[i], howDoHaveEntity_31[i], whatisEntity_32[i], \
                         cleftlist_33[i], crightlist_34[i], tleftlist_35[i], trightlist_36[i], \
                         plural_37[i], complex_38[i], head_39[i], full_40[i], \
                         conjBetween_16[i], ppPreProcede_17[i], dtlist_18[i], endlist_19[i], containAdjlist_20[i]]\
                         + \
                         [  timeType_41[i]  ]
            attribute_matrix.append(attribute)
               
            # sentence attributes are just 1 value since 1 sentence have 1 value
            for attr in [supInSentence_0, JJRInSent_1, tInSentence_2, cInSentence_3, (isWhich_4 or isWhat_4), \
                         howManyMuch_5, howDoHave_6, isWhatis_7, mtrend_8, \
                         mcomp_9, compHasIN_10, comp_N_N_S_11, comp_N_all_12, comp_NS_13, \
                         compeos_14, manyfull_15]:
                sys.stdout.write('%d,' % attr)
#             sys.stdout.write('|||')
            
            # noun entity attributes are lists, there are multiple entities per query sentence, indexed by i
            for attr2 in [conj_16[i], supInNP_17[i], gInNP_18[i], qInNP_19[i], cInNP_20[i], tInNP_21[i], allInNP_22[i], otherInNP_23[i], \
                          eachInNP_24[i], modifiedByQuant_25[i], modifiedByTrend_26[i], modifiedByComp_27[i], modifiedByGrad_28[i], \
                          whichwhat_29[i], howManyMuchEntity_30[i], howDoHaveEntity_31[i], whatisEntity_32[i], \
                         cleftlist_33[i], crightlist_34[i], tleftlist_35[i], trightlist_36[i], \
                         plural_37[i], complex_38[i], head_39[i], full_40[i], \
                         conjBetween_16[i], ppPreProcede_17[i], dtlist_18[i], endlist_19[i], containAdjlist_20[i]]:
                sys.stdout.write('%d,' % attr2)
                
            sys.stdout.write('%d' %  timeType_41[i])

            sys.stdout.write('\n')    
        return attribute_matrix

########### To run the program either to print entities, or to print attributes, un-comment the following lines: 
##################
    attribute_matrix = []
    if print_choice !=0 :  printEntities()
    if print_choice != 1 :  attribute_matrix = xyAttributes()
    return timeEntityChunks, attribute_matrix
    
'''
 For all queries ------------------------------------------------------------------------------------------
'''

def perQuery(print_entities):
    '''
       print_entities is a user choice: 
           0, not printing entities just attributes; 
           1, printing entities;
           2, printing both entitiesa nd attributes.
    '''
#     parsef = '../../Entity/Simple_Charts/No_Error_Parse.txt'
    parsef = '../../Entity/AllQueryParse.txt'
#     parsef = "../../Entity/2_example_queries_parse.txt"
    fhandler = open(parsef, 'r', 1)
    
    blanc, qno = 0, 0
    single_parse_tree = []
    
    for line in fhandler.readlines():
        if len(line) <=2 :
            blanc = blanc + 1;  pass
        elif blanc < 2:     
            single_parse_tree.append(line);    pass
        elif blanc == 2:
            blanc = 0
            chunkQuery(qno, single_parse_tree, print_entities)   
            qno += 1
            single_parse_tree = [];     pass
    fhandler.close()


def main(argv):
    print_attributes = 0
    print_entities = 1
    print_entities_attributes = 2
    no_print = 3
    
    '''
     The following are for given a new query -----------------
    '''
    print "Got query = ", argv
    parseTree = getParseTree(argv)
    entityChunks, attribute_matrix = chunkQuery(0, parseTree, no_print)   
    XYClass, entities = runXYtree(entityChunks, attribute_matrix)
    IMClass = runIMtree(XYClass, entities)
    FXlist = runFXtree(attribute_matrix, XYClass, entities, IMClass)
    
    
    print "Infographic Query Processing Result:"
    solr_query = ""
    for en, label in enumerate(XYClass):   
        print label, ": ", entities[en]
        if label == "X":
            solr_query += "merged" + label + ":(" + " OR ".join(entities[en].split()) + ") OR "
        elif label == "Y":
            solr_query += "merged_" + label + ":(" + " OR ".join(entities[en].split()) + ") OR "
    print "IMCategory: ", IMClass
    print "focusX: ", ",".join(FXlist)
            
#    solr_query += "IMCategory:(" + IMClass + ") OR " + "focusX:(" + ",".join(FXlist) + ")"
#    print "\nSolrQuery = ", solr_query

    # perQuery method is for generating entities or Axes attributes in a batch      
#     perQuery(print_entities)

    '''
     -------------------------------------------------------------
    '''
    
def runXYtree(entityChunks, attribute_matrix):
    from ARFFheader import Add_ARFFheader
    location = "newquery_XY.ARFF"
    Add_ARFFheader( "XY", attribute_matrix , location)
    weka_result = subprocess.check_output("java -cp ./src/Query/EntityExtractionSystem_03_2015/src/weka-3-6-6/weka.jar weka.classifiers.trees.J48 -l ./src/Query/EntityExtractionSystem_03_2015/src/weka-3-6-6/XYtree.j48.model -T " + location  + " -p 0" , shell=True)
    XYClass = []
    for result in weka_result.split("\n"):
        match = re.search(":[XYN]", result)
        if match: 
            XYClass.append(result[match.start()+1 : match.start()+2])
    entities = [" ".join([w[1][0] for w in entity]) for entity in entityChunks]
    return XYClass, entities
    
    
def runIMtree(XYClass, entities):
    EntityAttrPath = "newquery_XY.ARFF"
    location = "newquery_IM.ARFF"
    from IgAttributes import IgAttributes
    igAttr = IgAttributes(";".join(XYClass), ";".join(entities), EntityAttrPath, "", 1)
    return igAttr.classifyIM(location)

       
def runFXtree(attribute_matrix, XYClass, entities, IMClass):     
    '''
     attribute_matrix will have N rows for N entities, XYClass will have N elements for N entities. IMClass only has 1 value.
    '''
    EntityAttrPath = "newquery_XY.ARFF"
    location = "newquery_FX.ARFF"
    for no, attr in enumerate( attribute_matrix ):
        attr += [XYClass[no]] + [IMClass]
    print attribute_matrix
    from ARFFheader import Add_ARFFheader
    Add_ARFFheader( "FX", attribute_matrix , location)
    FX_weka_result = subprocess.check_output("java -cp ./src/Query/EntityExtractionSystem_03_2015/src/weka-3-6-6/weka.jar weka.classifiers.trees.J48 -l ./src/Query/EntityExtractionSystem_03_2015/src/weka-3-6-6/FXtree.j48.model -T " + location  + " -p 0" , shell=True)
    labellist = []
    for result in FX_weka_result.split("\n"):
        match = re.search("[1-2]:(F|NF)", result)
        if match:   
            labellist.append( {1:'F',
                    2:'NF'}[int( result[match.start()]) ] )
    FXlist = []
    for en, label in enumerate( labellist ):
        if label == "F": FXlist.append(entities[en])
    return FXlist

def getParseTree(query):
    query = query.strip()
    # make sure query has " ?" at the end
    if query[-1] != "?":
        query += " ?"
    elif query[-2] != " ":
        query = query.replace("?", " ?")

    # handle 's
    query = re.sub(r"[ ]*'s", "", query)

    parser_output = subprocess.check_output("echo \"" + query + "\" |  ./src/Query/EntityExtractionSystem_03_2015/src/candc-1.00/bin/pos --model ./src/Query/EntityExtractionSystem_03_2015/src/candc-1.00/models/pos/ | ./src/Query/EntityExtractionSystem_03_2015/src/candc-1.00/bin/parser --parser ./src/Query/EntityExtractionSystem_03_2015/src/candc-1.00/models/parser --super ./src/Query/EntityExtractionSystem_03_2015/src/candc-1.00/models/super_questions/ --parser-seen_rules false --parser-question_rules true --printer prolog" , shell=True)
    parseTree = [line+"\n" for line in parser_output.split("\n")[7:]]
    print "From below parse tree -----------"
    print "".join(parseTree)
    print "--------------"
    return parseTree

'''
 Unit Test ------------------------------------------------------------------------------------------------
'''

if __name__ == '__main__':
    
    if len(sys.argv) >= 2:
        main(sys.argv[1])
    else:
        print "Error. Give a query."
    
    
    
    
    
    