'''
Created on May 8, 2012

@author: fifille
'''
from Attribute import *
from QuantTrendCompareAttr import *

class NounAttr(Attribute):
    '''
     This class works with EntityChunks list before JJ, RBS, and VB are added inside.
     To see whether the NP Entities are 1) simple/complex, 2) full/partial, 3) head noun / not head noun, 4) plural / singular
    '''
    def init(self):
        qtc = QuantTrendCompareAttr(self.ptree, self.posdict, self.EntityChunks, self.EntityRoots)
        qtclist = [[] for i in range(len(self.EntityChunks))]
        self.quantlist = qtc.qtcInEntity(qtc.quant, qtclist)
        qtclist = [[] for i in range(len(self.EntityChunks))]
        self.trendlist = qtc.qtcInEntity(qtc.trend, qtclist)
        qtclist = [[] for i in range(len(self.EntityChunks))]
        self.comparelist = qtc.qtcInEntity(qtc.compare, qtclist)
        otherlist = [[] for i in range(len(self.EntityChunks))]
        self.otherlist = qtc.qtcInEntity(qtc.other, otherlist)
        eachlist = [[] for i in range(len(self.EntityChunks))]
        self.eachlist = qtc.qtcInEntity(qtc.each, eachlist)
        alllist = [[] for i in range(len(self.EntityChunks))]
        self.alllist = qtc.qtcInEntity(qtc.all, alllist)
        
    def printQTC(self):
        print 'quantlist: ',self.quantlist, '\ntrendlist: ', self.trendlist, '\ncomparelist: ', self.comparelist
        print 'eachlist: ', self.eachlist, '\notherlist: ', self.otherlist, '\nalllist: ', self.alllist
        
    def indexCover(self):
        #print self.EntityChunks
        return [(e[0][0], e[-1][0]) for e in self.EntityChunks]

    def isFullNoun(self, full_vec):
        '''
         full_vec must be initialized fixed length = length of EntityChunks
        '''
        indcov = self.indexCover()
        full = 0
        for i in range(len(indcov)):
            if i < full:
                #print 'pass'
                continue
            low, high = indcov[i]
            # if the whole list has only 1 entity:
            if len(indcov) == 1:
                #print 'only 1'    
                full_vec[i] = 1;    return full_vec
            # if end of index cover list:
            elif i == len(indcov)-1:
                #print 'end of list'    
                if low < indcov[i-1][1]:    # the last entity is a partial entity
                    #print 'last entity partial'
                    full_vec[i] = 0;    return full_vec
                else:                       # the last entity is a full new entity
                    #print 'last entity full'
                    full_vec[i] = 1;    return full_vec
            # check the next entity
            l, h = indcov[i+1]
            if l > high:    # a different entity chunk
                #print 'new entity'
                full_vec[i] = 1
                full = i+1    
            else:   # followed by smaller splits
                full_vec[i] = 1
                #print 'splits'
                for ii in indcov[(i+1):]:
                    (l, h) = ii
                    if l-high > 0:    # a new entity chunk
                        #print 'end of split'
                        full = indcov.index(ii);    break
                    elif indcov.index(ii) == (len(indcov)-1):
                        #print 'end of list and split'
                        full_vec[indcov.index(ii)] = 0
                        full = len(indcov)
                    else:   
                        #print 'split'
                        full_vec[indcov.index(ii)] = 0
        return full_vec
       
       
    def isComplexNoun(self, complexlist):
        '''
         complexlist should be initialized to be fixed length = length of EntityChunks
         a simple noun phrase doesn't contain IN and VB*
         a compound noun, consisting more than 2 atomic nouns, is also considered complex noun
        '''
        for en in self.EntityChunks:
            comp = 0
            for w in en:
                if re.match(r'IN', w[1][1]) or (re.match(r'VB(.?)', w[1][1]) and self.verbModifier(w) == 0):
                    comp = 1
            if comp == 0:   complexlist[self.EntityChunks.index(en)] = 0
            elif comp == 1: complexlist[self.EntityChunks.index(en)] = 1
        indcov = self.indexCover()
        for i in range(len(indcov)):
            left, right = indcov[i]
            nncount = 0
            for j in range(left, right+1):
                if re.match(r'NN', self.posdict[j][1]):
                    nncount = nncount + 1
            if nncount > 1:
                complexlist[i] = 1
        return complexlist 
            
    def containAdj(self, containAdjlist):
        indcov = self.indexCover()
        for i in range(len(indcov)):
            left, right = indcov[i]
            nncount = 0
            for j in range(left, right+1):
                if re.match(r'JJ', self.posdict[j][1]):
                    containAdjlist[i] = 1
        return containAdjlist
            
    def isHeadNoun(self, headlist):
        '''
         headlist should be initialized to be fixed length = length of EntityChunks
         full noun being complex, the full noun should not be the head noun
         simple noun should be the head noun of themselves
         complex noun should have 1 head noun and all rest partial nouns are not 
        '''
        full_vec = [[] for i in range(len(self.EntityChunks))]
        complexlist = [[] for i in range(len(self.EntityChunks))]
        
        full_vec = self.isFullNoun(full_vec)
        complexlist = self.isComplexNoun(complexlist)
        
        #print "full? ", full_vec, '\ncomp? ', complexlist
        for i in range(len(self.EntityChunks)):
            if full_vec[i] == 1 and complexlist[i] == 1:    headlist[i] = 0
            elif full_vec[i] == 1 and complexlist[i] == 0:   headlist[i] = 1
            else:
                if self.comparelist[i-1] == 1 or self.trendlist[i-1] == 1 or self.quantlist[i-1] == 1:
                    headlist[i] = 0     # compare, trend, quant were not further splitted
                # if it is the first entity following the full and complex entity, it is head chunk
                elif full_vec[i-1] == 1 and complexlist[i-1] == 1:  
                    headlist[i] = 1
                else:   headlist[i] = 0
        #print 'head? ', headlist
        return headlist
           
            
    def isPlural(self, plurallist):
        '''
         plurallist should be initialized to be fixed length = length of EntityChunks
         simple noun's plural depends only on the last N or NP
         complex noun's plural depends on the head noun
        '''
        full_vec = [[] for i in range(len(self.EntityChunks))]
        complexlist = [[] for i in range(len(self.EntityChunks))]
        headlist = [[] for i in range(len(self.EntityChunks))]
        
        full_vec = self.isFullNoun(full_vec)
        complexlist = self.isComplexNoun(complexlist)
        headlist = self.isHeadNoun(headlist)
        
        #print '\ncomp? ', complexlist
        for i in range(len(self.EntityChunks)):
            if complexlist[i] == 0:
                # simple noun, check only last N or NP
                en = self.EntityChunks[i]
                while len(en) >= 1 and not re.match(r'(^NP$)|(^NP\[nb\]$)|(^N$)', en[-1][1][2]):
                    en = en[:-1]    # strip off possible VBP or VBN modifying the noun
                if len(en) > 0 and re.match(r'.?NS', en[-1][1][1]):    plurallist[i] = 1; 
                else:   plurallist[i] = 0
            else:
                # for complex nouns, if head noun is plural then plural. head noun i+1
                if self.comparelist[i] == 1 or self.trendlist[i] == 1 or self.quantlist[i] == 1:
                    plurallist[i] = 0;  continue
                if i == len(self.EntityChunks)-1:    
                    if re.match(r'.?NS', self.EntityChunks[i][-1][1][1]):   
                        plurallist[i] = 1;  break
                    else:   plurallist[i] = 0;  break
                headnoun = i+1
                if re.match(r'.?NS', self.EntityChunks[headnoun][-1][1][1]):    
                    plurallist[i] = 1
                else:   plurallist[i] = 0
        return plurallist
    
    def conjAttr(self, conjlist):
        # given root of NP node, if the NP contains a noun conjuncted with another noun, return 1
        for er in self.EntityRoots:
            conjlist[self.EntityRoots.index(er)] = 0
            nodes_to_visit = [er]
            while len(nodes_to_visit) != 0:
                currentnode = nodes_to_visit.pop()
                if currentnode != None and currentnode.func == 'conj':
                    conjlist[self.EntityRoots.index(er)] = 1;   break
                else:
                    if currentnode != None and currentnode.right != None:
                        nodes_to_visit.append( currentnode.right )
                    if  currentnode != None and currentnode.left != None:
                        nodes_to_visit.append( currentnode.left )      
        return conjlist    
    
    
    def conjunctedEntitiesAttr(self, conjBetween):
        # given each entity, if the entity precede or procede other entities with 
        # "and", "or", "than", "etc".. ?
        indcov = self.indexCover()
        for i in range(len(indcov)-1):
            left1, right1 = indcov[i]
            left2, right2 = indcov[i+1]
            between = " ".join(self.posdict[j][0] for j in range(right1+1, left2))
            # print between
            if re.search(re.compile(",|(and)|(or)|(than)|(etc)"), between):
                # set the first entity, entity i, as preceding a pp, attribute value 1;
                # set the second entity, entity i+1, as proceding a pp, attribute value 2;
                # otherwise, leave attribute value 0
                conjBetween[i], conjBetween[i+1] = 1, 1
        return conjBetween       
        
    def ppAttr(self, ppPreProcede):
        # Whether an entity precede or procede a Proper Noun, like "from", "of"
        # In aditional, a piggybacked attribute: whether an entity begins with determiner, like "the", "a"
        indcov = self.indexCover()
        for i in range(len(indcov)):
            left, right = indcov[i]
            if right < len(indcov)-1:
                # print self.posdict[right+1]
                if self.posdict[right + 1][1] == 'IN':
                    # having a value of 1 means this noun entity is preceding a PP
                    ppPreProcede[i] = 1
            if left >= 1:
                if self.posdict[left - 1][1] == 'IN':
                    # having a value of 2 means this noun entity is proceding a PP
                    ppPreProcede[i] = 2
                    if right < len(indcov)-1:
                        # if also this entity precedes IN, give value 2
                        if self.posdict[right + 1][1] == 'IN':
                            # having a value of 3 means this noun entity is both proceding and preceding a PP
                            ppPreProcede[i] = 3
        return ppPreProcede
    
    
    def dtAttr(self, dtlist):
        indcov = self.indexCover()
        for i in range(len(indcov)):
            left, right = indcov[i]
            if self.posdict[left][1] == 'DT':
                # this noun is modified by a determiner, like "the", "a", "an", give value 4s
                dtlist[i] = 1
        return dtlist
                
                
    def lastEntityAttr(self, endlist):
        # see whether entity is at the end of the query sentence, and also whether it is the last entity
        # if this entity is the last entity, value 1
        # if an entity is also at the end of the query, value 2
        indcov = self.indexCover()
        left, right = indcov[-1]
        if len(self.posdict) - right < 5:
            endlist[-1] = 2
        else:
            endlist[-1] = 1
#         print " ".join(self.posdict[i][0] for i in range(len(self.posdict)))
        return endlist
    
        
    def manyFullNoun(self):
        # Global. if there are more than 2 full nouns for a query. Anyways we just need 2 for x and y.
        full_vec = [[] for i in range(len(self.EntityChunks))]
        full_vec = self.isFullNoun(full_vec)
        count = 0
        for fn in full_vec:
            if count > 2:   return 1
            if fn == 1: 
                count += 1
        return 0
    
    
if __name__ == '__main__':
        
    # ------------------------------------------------------------------------------------------------------------------------
    from TreeStructure.ReadTree import *
    filepath = '../../../../test.txt'      # whatis2.txt
    singletree = open(filepath).readlines()
    readfile = ReadTree(singletree)

    from EntityExtract import EntityExtract
    entity = EntityExtract(readfile.ptree, readfile.posdict, readfile.verbdict, readfile.noundict)
    entity.EntityChunker(entity.ptree.root)
    entity.onlyNP()
    # ------------------------------------------------------------------------------------------------------------------------
    
    print entity.EntityChunks
    headnoun = NounAttr(entity.ptree, entity.posdict, entity.EntityChunks, entity.EntityRoots)
    print headnoun.indexCover()
    
    full_vec = [[] for i in range(len(headnoun.EntityChunks))]
    complexlist = [[] for i in range(len(headnoun.EntityChunks))]
    headlist = [[] for i in range(len(headnoun.EntityChunks))]
    plurallist = [[] for i in range(len(headnoun.EntityChunks))]
    conjlist = [[] for i in range(len(headnoun.EntityChunks))]
    
    headnoun.printQTC()
    print 'full noun attr : \n', headnoun.isFullNoun(full_vec)
    print 'complex noun attr : \n', headnoun.isComplexNoun(complexlist)
    print 'head noun attr : \n', headnoun.isHeadNoun(headlist)
    print 'plural attr : \n', headnoun.isPlural(plurallist)
    print 'conj attr : \n', headnoun.conjAttr(conjlist)
