'''
Created on Jan 10, 2013

@author: ivanka li

This module take in the complete list of query-entity pairs. 

And match each query-entity pair with their decision tree classification.

'''
# from CleanDecisionResults import CleanDecision
from AxesDecisionResults import AxesDecisionResults

import sys, re, random, os.path, subprocess
    
class QueryEntities():
    
    def __init__(self, qe, attrPath):
        self.qePairs, self.attrList = [], []
        self.initQE(qe)
        self.initAttr(attrPath) 
    
        
    def initQE(self, qe):
        if os.path.isfile(qe):
            fhandler = open(qe, 'r')
            for line in fhandler.readlines():
                if len( line.split() ) > 1 and re.match(r'[0-9]', line)!=0:
                    self.qePairs.append(line)
        #            print line.replace('\r\n', '').split('\t')
            fhandler.close()
        else:
            self.qePairs.append(qe)
            
    def initAttr(self, attrPath):
        if os.path.isfile(attrPath):
            fhandler = open(attrPath, 'r')
            lno = 1
            for line in fhandler.readlines():
                if not re.match("@", line) and len(line) > 1: 
                    self.attrList.append( map( lambda x: int(x), line.split(',')[:-1] ) )
                lno = lno +1
            fhandler.close()
        else:
            self.attrPath.append(attrPath)
            

def deprecated(replacement=None):
        '''This is a decorator which can be used to mark functions
        as deprecated. It will result in a warning being emitted
        when the function is used.'''
  
class IgAttributes():
    
    def __init__(self, XYDecision, QueryEntity, EntityAttrPath, AnnotationPath, num_of_queries):
        self.axesDecision = AxesDecisionResults(XYDecision)
        self.qe = QueryEntities(QueryEntity, EntityAttrPath)
        self.annotation = Annotations(AnnotationPath)
        self.numOfX, self.numOfY, self.singularPluralX, self.superlatives, self.specialWords, self.npRelation, self.time, self.modified, self.wiki_sim = \
        [0]*num_of_queries, [0]*num_of_queries, [0]*num_of_queries, [0]*num_of_queries, \
        [0]*num_of_queries, [0]*num_of_queries, [0]*num_of_queries, [0]*num_of_queries, [0]*num_of_queries
        self.__runAll()
        
        
    def __numOfXEntities(self):
        '''
         4 class values: 
          - no X found: 0 
          - 1 X found: 1 
          - 2 X found: 2 
          - more than 2 X found: 3
        '''
        for q_index in range( len( self.axesDecision.resultList ) ):
            count = 0
            for r in self.axesDecision.resultList[q_index]:
                if r == 'x':   count = count + 1
            if count == 0:
                self.numOfX[q_index] = 0
            elif count == 1:
                self.numOfX[q_index] = 1
            elif count == 2: 
                self.numOfX[q_index] = 2
            else:
                self.numOfX[q_index] = 3
      
    def __numOfYEntities(self):
        '''
         3 class values:
         - no y found: 0
         - 1 y found: 1
         - more than 1 y found: 2
        '''
        for q_index in range( len( self.axesDecision.resultList ) ):
            count = 0
            for r in self.axesDecision.resultList[q_index]:
                if r == 'y': count = count + 1
            if count == 0:
                self.numOfY[q_index] = 0 
            elif count <= 2:
                self.numOfY[q_index] = 1
            else:
                self.numOfY[q_index] = 2
                
    def __pluralX(self):
        '''
         Use attr no. 37 NP_sorp to see if in the entity list any entity has plural form
         3 class value: 
         - all X are singular: 0
         - all X are plural: 1
         - X have singular vs plural: 2
        '''
        for q_index in range( len(self.axesDecision.rangeList) ):
            lo, hi = self.axesDecision.rangeList[q_index]
            plural, numX = 0, 0
            # ---
#            sys.stdout.write('%d\n' % q_index)
            # ---
            for en_index in range(lo, hi+1):
                if self.axesDecision.resultList[q_index][en_index-lo] == 'x' and self.qe.attrList[en_index][46] !=1:
                    # is X entity, and not time interval
                    numX = numX +1
                    plural = self.qe.attrList[en_index][37] + plural
                    # ---
#                    sys.stdout.write('%s' % self.qe.qePairs[en_index].replace('\t', '').replace('U.S.', 'united states'))
                    # ---
                if self.axesDecision.resultList[q_index][en_index-lo] == 'x' and self.qe.attrList[en_index][46] > 0:  
                    plural = -1
            if plural == -1:
                self.singularPluralX[q_index] = 0
            elif plural == 0:  
                # all singular
                self.singularPluralX[q_index] = 1
            elif plural == numX: 
                # all plural
                self.singularPluralX[q_index] = 2
            elif plural < numX: 
                # singular mixed with plural
                self.singularPluralX[q_index] = 3
                
    def __modifiedX(self):
        '''
         Use attr no 22, 23, 24 to see whether the entity is modified by all, other, each
         4 class value:
          - No modification: 0
          - Modified by "all": 1
          - Modified by "other": 2
          - Modified by "each": 3
        '''
        for q_index in range( len(self.axesDecision.rangeList) ):
            lo, hi = self.axesDecision.rangeList[q_index]
            for en_index in range(lo, hi+1):
                if self.axesDecision.resultList[q_index][en_index-lo] == 'x':
                    if self.qe.attrList[en_index][22] == 1:
                        self.modified[q_index] = 1
                    elif self.qe.attrList[en_index][23] == 1:
                        self.modified[q_index] = 2
                    elif self.qe.attrList[en_index][24] == 1:
                        self.modified[q_index] = 3
    
    def __superlative(self):
        '''
         2 class values: Yes/No
        '''
        for q_index in range( len( self.axesDecision.rangeList ) ):
            lo, hi = self.axesDecision.rangeList[q_index]
            self.superlatives[q_index] = self.qe.attrList[lo][0] 
    
    def __trendCompareWords(self):
        '''
         use attribute no 2 trend_in_sentence and no 3 compare_in_sentence
         4 class values:
         - neither verb: 0
         - compare verb: 1
         - trend verb: 2
        '''
        for q_index in range( len( self.axesDecision.rangeList ) ):
            lo, hi = self.axesDecision.rangeList[q_index]
            if self.qe.attrList[lo][2] < self.qe.attrList[lo][3] :
                self.specialWords[q_index] = 1
            elif self.qe.attrList[lo][2] > self.qe.attrList[lo][3]:
                self.specialWords[q_index] = 2
                
    def __npPattern(self):
        '''
         must be ran after function pluralX() and trendCompareVb()
         3 class values:
         - none: 0
         - singular np vs singular np: 1
         - singular np vs plural np: 2
         - plural compared: 3
        '''
        for q_index in range( len(self.axesDecision.rangeList) ):
            if self.specialWords[q_index] == 1:
                if self.singularPluralX[q_index] == 1 :
                    # all singular
                    if self.numOfX[q_index] >2:
                        self.npRelation[q_index] = 3
                    else:
                        self.npRelation[q_index] = 1
                elif self.singularPluralX[q_index] == 2:    
                    # all plural 
                    self.npRelation[q_index] = 3
                elif self.singularPluralX[q_index] == 3:
                    # singular mixed with plural
                    self.npRelation[q_index] = 2

    
    def __timeInterval(self):
        '''
         use attr no 41 time_type: 0-none, 1-interval, 2-period
        '''
        for q_index in range( len(self.axesDecision.rangeList) ):
            lo, hi = self.axesDecision.rangeList[q_index]
            for en_index in range(lo, hi+1):
                self.time[q_index] = self.qe.attrList[en_index][46] 

    @deprecated
    def __wiki_similarity(self):
        '''
         0: doesn't contain equal entities
         1: contain 1 pair of equal entities
         2: contain more than 1 pair of equal entities
        '''
        wiki = entity_sim('../../../Wikimantic_Result.txt')
        wikiclass = []
        
        wikiclassfile = open('../../../j48_wiki.txt', 'r', 1)

        for line in wikiclassfile.readlines():
            act = re.search(re.compile('Actual:'), line).end()
            pred = re.search(re.compile('Predicted:'), line).end()
            wikiclass.append( int(line[pred:pred+2]) )
#         print len(wikiclass), len(wiki.wiki_range)
#        print wikiclass, '\n', wiki.wiki_range

        cumulative,use = 0,0
        for qno in range(len(wiki.wiki_range)):
            w_count,w_count2 = 0, 0
#            sys.stdout.write( '\n%d----\n' % qno)
            wiki_count = wiki.wiki_range[qno]
            if wiki_count == 0:
                cumulative = cumulative + 1
#                sys.stdout.write('%d\n' % (cumulative))
            else:
                cumulative = cumulative + 1
                for w_index in range( wiki_count ):
#                    sys.stdout.write('%d\n' % (cumulative+w_index))
                    if wikiclass[cumulative+w_index-1] == 2:
                        w_count = w_count + 1    
                        use = use + 1
                    if wikiclass[cumulative+w_index-1] == 1:
                        w_count2 = w_count2+1
                        use = use + 1
                cumulative = cumulative + wiki_count-1
            
            if w_count == 1: self.wiki_sim[qno] = 1  # rel_diff
            elif w_count >= 2 : self.wiki_sim[qno] = 2  # rank_all
            elif w_count2 == 1: self.wiki_sim[qno] = 3 # rank_1
            elif w_count2 >1 : self.wiki_sim[qno] = 2 # rank_all
#        print use
            
        
    def __runAll(self):
        self.__numOfXEntities()
        self.__numOfYEntities()
        self.__pluralX()
        self.__superlative()
        self.__trendCompareWords()
        self.__npPattern()
        self.__timeInterval()
        self.__modifiedX()


    def print_attr(self):
        for qno in range( len( self.axesDecision.resultList ) ):
            lo, hi = self.axesDecision.rangeList[qno]
            for attr in [self.numOfX[qno], self.numOfY[qno], self.singularPluralX[qno], \
                     self.superlatives[qno], self.specialWords[qno], self.npRelation[qno], self.time[qno], self.modified[qno] ]:
                sys.stdout.write('%d,' % attr)
     
            for attr in [self.qe.attrList[lo][1], self.qe.attrList[lo][4], self.qe.attrList[lo][5], \
                         self.qe.attrList[lo][6]]:
                sys.stdout.write('%d,' % attr)
            sys.stdout.write('%d,' % self.qe.attrList[lo][7])
            sys.stdout.write('%s\n' % self.annotation.IMannotation[qno])

    def classifyIM(self, location):
        IM_attr_matrix = []
        for qno in range( len( self.axesDecision.resultList ) ):
            lo, hi = self.axesDecision.rangeList[qno]
            IM_attr_matrix.append( [self.numOfX[qno], self.numOfY[qno], self.singularPluralX[qno], \
             self.superlatives[qno], self.specialWords[qno], self.npRelation[qno], self.time[qno], self.modified[qno] ] + \
            [self.qe.attrList[lo][1], self.qe.attrList[lo][4], self.qe.attrList[lo][5], self.qe.attrList[lo][6], self.qe.attrList[lo][7] ] )
        from ARFFheader import Add_ARFFheader
        Add_ARFFheader( "IM", IM_attr_matrix , location)
        IM_weka_result = subprocess.check_output("java -cp weka-3-6-6/weka.jar weka.classifiers.trees.J48 -l weka-3-6-6/IMtree.j48.model -T " + location  + " -p 0" , shell=True)
        for result in IM_weka_result.split("\n"):
            match = re.search("[1-8]:[GMRT]", result)
            if match:   
                return {1:"Get-Rank",
                        2:"Maximum-Minimum-Multiple",
                        3:"Maximum-Minimum-Single",
                        4:"General-Multiple", 
                        5:"General-Single", 
                        6:"Relative-Difference", 
                        7:"Rank-All", 
                        8:"Trend"}[int( result[match.start()]) ]
            

  
        
class entity_sim():
    
    def __init__(self, wiki_result_path):
        self.init(wiki_result_path)
    
    def init(self, wiki_result_path):
        self.wiki_result, self.wiki_range = [], []
        prev_qno, wiki_count = -1, 0
        
        wiki_file = open(wiki_result_path, 'r', 1)
        for line in wiki_file.readlines():
            if len(line.split(',')) == 1:
                if prev_qno != -1:
#                    self.wiki_result.append(wiki_pair)
                    self.wiki_range.append(wiki_count) 
                    
                prev_qno = int(line)-1
#               if len(wiki_pair) == 0:
#                   sys.stdout.write('0.0,0.0\n')
#               wiki_pair = []
                wiki_count = 0
                
            else:   
#                wiki_pair.append( [  float( line.split(',')[2] ), float( line.split(',')[3] )   ] )
#                sys.stdout.write('%s,' % ( line.split(',')[2] ) )
#                sys.stdout.write('%s' % ( line.split(',')[3] ) )
                wiki_count = wiki_count + 1
        self.wiki_range.append(wiki_count)
        
class Annotations():
    
    def __init__(self, annotationPath):
        Machine_Reliant_IM_Path = '../../Entity/Machine_Reliant_IM_Result.txt'
        self.init(annotationPath, Machine_Reliant_IM_Path)

#         self.igAttr = IgAttributes(XYDecisionPath, QueryEntityPath, EntityAttrPath)
        
    def init(self, annotationPath, IMresultPath):
        if os.path.isfile(annotationPath):
            annotationFile = open(annotationPath, 'r', 1)
            IMresultFile = open(IMresultPath, 'r', 1)
            self.annotatedAttrs, self.IMresults, self.IMannotation = [], [], []
            
            for line in annotationFile.readlines():
                attr, IM = [int(i) for i in line.split(',')[:-1]], line.split(',')[-1]
    #            trend_count = 0
    #            if IM.replace('\n', '') == 'Trend' and random.random() > 0.8 and trend_count >=30:
    #                break
    #            else:
        #            trend_count = trend_count +1 
                self.annotatedAttrs.append(attr)
                self.IMannotation.append(IM.replace('\n', '').replace("-", ""))
        elif len(annotationPath) == 0:
            self.IMannotation = ["?"]
#         for line in IMresultFile.readlines():
#             self.IMresults.append(line.replace('\n', ''))
    
    def create_test(self):
        for i in range( len(self.annotatedAttrs) ):
            for a in self.annotatedAttrs[i]:
                sys.stdout.write('%d,' % a)
            sys.stdout.write('%s\n' % self.IMannotation[i])
            
    def compareDiff(self):
        diffCount = 0
        for qno in range( len(self.annotatedAttrs) ):
            diff, classDiff = 0, 0
            generated = [self.igAttr.numOfX[qno], self.igAttr.numOfY[qno], self.igAttr.singularPluralX[qno], \
                 self.igAttr.superlatives[qno], self.igAttr.specialWords[qno], self.igAttr.npRelation[qno], \
                 self.igAttr.time[qno], self.igAttr.modified[qno] ]
            for attr in range( len(self.annotatedAttrs[qno]) ):
                if self.annotatedAttrs[qno][attr] != generated[attr]:
                    diff = 1
                    break
            if self.IMannotation[qno] != self.IMresults[qno]:
                classDiff = 1
            if classDiff == 1 or classDiff == 0:
                diffCount = diffCount + 1
                sys.stdout.write('Query %d.\n' % (qno+1))
                sys.stdout.write('Query-Entity Pair %d - ' % self.igAttr.axesDecision.rangeList[qno][0] )
                sys.stdout.write('%d ' % self.igAttr.axesDecision.rangeList[qno][1] )
                sys.stdout.write('in total %d queries.\n' % len(self.igAttr.axesDecision.rangeList))
                lo, hi = self.igAttr.axesDecision.rangeList[qno]
                for entity_index in range(lo, (hi+1)):
                    sys.stdout.write('%s  ' % self.igAttr.axesDecision.resultList[qno][(entity_index-lo)])
                    sys.stdout.write('%s' % self.igAttr.qe.qePairs[entity_index])
                    
                sys.stdout.write('annotated: ')
                for attr in self.annotatedAttrs[qno]:
                    sys.stdout.write('%d ' % attr)
                    
                sys.stdout.write('\ngenerated: ')
                for gattr in generated:
                    sys.stdout.write('%d ' % gattr)
                     
                sys.stdout.write('\nIntended Message: %s' % self.IMannotation[qno] )
                sys.stdout.write('\nClassified as: %s' % self.IMresults[qno])
                     
                sys.stdout.write('\n\n')
        sys.stdout.write('--- In total, %d queries have different Intended Messages.' % diffCount)    
            


            
if __name__ == '__main__':
    
    '''
     Use the following set of parameters if creating IM attributes for the 324 queries in Matt's experiment
    '''
    XYDecisionPath = 'New_Axis_Classification_Results.txt'
    QueryEntityPath = '../../Entity/New_XYEntities.txt' 
    AnnotationPath = '../../Entity/AnnotatedIgAttr.txt'
    training_query_number = 324 
    EntityAttrPath = '../../Entity/New_XYAttributes.arff'
    
    '''
     Use the following set of parameters if testing IM attribute generation for a particular query
    '''
    XYDecision = "X;X;X"
    QueryEntity = "America;China;GDP"
    Annotation = ""
    query_number = 1
    
    igAttr = IgAttributes(XYDecisionPath, QueryEntityPath, EntityAttrPath, AnnotationPath, training_query_number)
    igAttr.print_attr()    


#    from nltk.corpus import wordnet as wn
#    china = wn.synsets('Ford') 
##    china = wn.synset('china.n.01')  
#    for l in china: print l
#    support = wn.synset('united.n.01')
#    support2 = wn.synset('states.n.01')
#    for c in china:
#        print c
#        print 'china-year', wn.path_similarity(c,support)    #no output
#        print 'china-russia', wn.path_similarity(c,support2)   #get an output 0.08333 

    