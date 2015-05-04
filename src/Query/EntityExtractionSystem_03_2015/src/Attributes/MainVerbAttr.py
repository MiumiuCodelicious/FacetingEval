'''
Created on May 9, 2012

@author: fifille
'''
from Attribute import *

class MainVerbAttr(Attribute):
    
    def init(self):
        self.trendvr = re.compile('(change)|(vary)|(varies)|(varied)|(increase)|(decrease)'
                                    '|(grow)|(grew)|(fluctuate)|(reduce)|(shrink)|(shrunk)|'
                                    '(rise)|(fall)|(win)|(lose)|(gain)|(fail)|(succeed)|(soar)|(accelerate)|'
                                    '(fair)|(maintain)|(stay)|(climb)')
        self.comparevr = re.compile('(compare)|(differ)|(diff)|(oppose)|(increase)|(decrease)|'
                                    '(evolve)')
        auxverb = re.compile('(is)|(are)|(was)|(were)|(isn\'t)|(aren\'t)|(weren\'t)|'
                                  '(wasn\'t)|(isnt)|(arent)|(wasnt)|(werent)|'
                                  '(do)|(does)|(did)|(donnot)|(dont)|(doesnt)|(didnt)|'
                                  '(don\'t)|(doesn\'t)|(didn\'t)|'
                                  '(have)|(has)|(had)|(havent)|(hasnt)|(hadnt)|'
                                  '(haven\'t)|(hasn\'t)|(hadn\'t)|(been)')
        self.verblist = [[w, self.posdict[w]] for w in self.posdict if re.match(r'VB.?', self.posdict[w][1]) \
                                                    and not re.match(auxverb, self.posdict[w][0]) \
                                                    and self.verbModifier([w, self.posdict[w]]) == 0]
    
    def isTrendVerb(self):
        if len(self.verblist) == 1 and re.match(self.trendvr, self.verblist[0][1][0]):  
            return 1
        else:
            return 0
    
    def isCompareVerb(self):
        if len(self.verblist) == 1 and re.match(self.comparevr, self.verblist[0][1][0]):  
            return 1
        else:   
            return 0
        
    def compEndOfSent(self):
        if len(self.verblist) == 1 and self.verblist[0][0] == len(self.posdict)-2:  
            return 1
        else:
            return 0
    
    
    def compareVerbIN(self):
        # return tuples of: hasIN, N_N, N_all, NS
        compareVerb = self.isCompareVerb() 
        if compareVerb == 1:
            hasIN, N_N_S, N_all, NS = 0, 0, 0, 0
            for i in range(self.verblist[0][0]+1, len(self.posdict)):
                if re.match('IN', self.posdict[i][1]):
                    IN, hasIN = self.posdict[i], 1
                    if re.match('(with)|(to)|(against)|(from)', IN[0]):
                        N_N_S = 1            
                    elif re.match('(among)|(amongst)', IN[0]):
                        N_all = 1
                    elif re.match('between', IN[0]):
                        NS = 1
                    else:
                        hasIN = 0
            return hasIN, N_N_S, N_all, NS
        else:   return 0, 0, 0, 0
        
        
    def locateMainVerb(self):
        '''
         Find in parse tree the verb node
        '''
        if len(self.verblist) != 1:
            return None
        nodes_to_visit = [self.ptree.root]
        while len(nodes_to_visit) != 0:
            currentnode = nodes_to_visit.pop()
            if currentnode == None:
                return
            elif currentnode.func == 'lf' and int(currentnode.w_index) == self.verblist[0][0]+1:
                return currentnode
            else:
                if currentnode.left != None:
                    nodes_to_visit.append( currentnode.right )
                if currentnode.right != None:
                    nodes_to_visit.append( currentnode.left )             
        

    def printVerb(self):
        #if len(self.verblist)>1:    
        print self.verblist


    def sideOfVerb(self, leftlist, rightlist):
        # return order: compare_left_list, compare_right_list, trend_left_list, trend_right_list
        if len(self.verblist) == 0:
            return [0 for e in self.EntityChunks], [0 for e in self.EntityChunks], [0 for e in self.EntityChunks], [0 for e in self.EntityChunks]
        vindex = self.verblist[0][0]
        entity_index = [(e[0][0], e[-1][0]) for e in self.EntityChunks]
        for (l, h) in entity_index:
            if h <= vindex: 
                leftlist[entity_index.index((l, h))] = 1
                rightlist[entity_index.index((l, h))] = 0
            elif l >= vindex:
                leftlist[entity_index.index((l, h))] = 0
                rightlist[entity_index.index((l, h))] = 1
            else:
                leftlist[entity_index.index((l, h))] = 1
                rightlist[entity_index.index((l, h))] = 1
        if self.isCompareVerb() == 1 and self.isTrendVerb() == 0:
            return leftlist, rightlist, [0 for e in self.EntityChunks], [0 for e in self.EntityChunks]
        elif self.isCompareVerb() == 0 and self.isTrendVerb() == 1:
            return [0 for e in self.EntityChunks], [0 for e in self.EntityChunks], leftlist, rightlist
        elif self.isCompareVerb() == 1 and self.isTrendVerb() == 1:
            return leftlist, rightlist, leftlist, rightlist
        else:
            return [0 for e in self.EntityChunks],[0 for e in self.EntityChunks],[0 for e in self.EntityChunks],[0 for e in self.EntityChunks]
            

    
    
            
        
if __name__ == '__main__':
    
    # ------------------------------------------------------------------------------------------------------------------
    from TreeStructure.ReadFile import *
    filepath = '../test/err.txt'      # whatis2.txt
    readfile = ReadFile(filepath)

    from EntityExtract import EntityExtract
    entity = EntityExtract(readfile.ptree, readfile.posdict, readfile.verbdict, readfile.noundict)
    entity.EntityChunker(entity.ptree.root)
    entity.onlyNP()
    # -------------------------------------------------------------------------------------------------------------------
    def printdict():
        print ' '.join([entity.posdict[key][0] for key in entity.posdict])
    def printf(e):
        print ' '.join([w for w in [l[1][0] for l in e]])
    map(printf, entity.EntityChunks)
    printdict()
    # -------------------------------------------------------------------------------------------------------------------
    
    verb = MainVerbAttr(entity.ptree, entity.posdict, entity.EntityChunks, entity.EntityRoots)
    
    verb.printVerb()
    
    verb.locateMainVerb()
    print 'iscompare, istrend, compareEndOfSent: ', verb.isCompareVerb(), verb.isTrendVerb(), verb.compEndOfSent()
    print 'N_N, N_all, NS: ', verb.compareVerbIN()
    
    leftlist = [0 for i in range(len(verb.EntityChunks))]
    rightlist = [0 for i in range(len(verb.EntityChunks))]
    cleftlist, crightlist, tleftlist, trightlist = verb.sideOfVerb(leftlist, rightlist)
    print 'compare left: ', cleftlist, '\ncompare right: ', crightlist, \
          '\ntrend left: ', tleftlist, '\ntright: ', trightlist
    
    vnode = verb.locateMainVerb()
    if vnode != None:
        vnode.printNode()
    
    