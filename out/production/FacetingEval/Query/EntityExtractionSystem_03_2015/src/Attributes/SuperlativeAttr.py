'''
Created on Apr 27, 2012

@author: fifille
'''
from Attribute import *

class SuperlativeAttr(Attribute):
    
    def superInSent(self):
        '''
         JJS - Adjective, superlative
         RBS - Adverb, superlative
        '''
        for i in range(len(self.posdict)): 
            if re.match('(JJS)|(RBS)', self.posdict[i][1]) or re.match('top|leading', self.posdict[i][0]):
                #print self.posdict[i]
                return 1    # return the index of this superlative
        return 0
            
    def superInNP(self, suplist):
        # suplist have to be initialized into fixed length = length of EntityChunks
        for e in self.EntityChunks:
            exist = 0
            for j in e:
                if re.match('(JJS)|(RBS)', j[1][1]) or re.match('top|leading', j[1][0]):    
                    exist = 1;     break
            if exist == 0:  suplist[self.EntityChunks.index(e)] = 0
            else:   suplist[self.EntityChunks.index(e)] = 1
        return suplist
    
    def JJRInSent(self):
        '''
         JJR - adjective -er 
        '''
        for i in range(len(self.posdict)):
            if re.match('JJR', self.posdict[i][1]):
                return 1
        return 0
    def run(self):
        self.SuperInSent()
        self.SuperInNP()
    
    
if __name__ == '__main__':
    
    # ------------------------------------------------------------------------------------------------------------------------
    from TreeStructure.ReadTree import *
    filepath = '../test/err.txt' 
    parsetree = open(filepath).readlines()
    readfile = ReadTree(filepath)
    
    from EntityExtract import EntityExtract
    entity = EntityExtract(readfile.ptree, readfile.posdict, readfile.verbdict, readfile.noundict)
    entity.EntityChunker(entity.ptree.root)            
    entity.onlyNP()
    # ------------------------------------------------------------------------------------------------------------------------
    
    sup = SuperlativeAttr(entity.ptree, entity.posdict,  entity.EntityChunks, entity.EntityRoots)
    suplist = [[] for i in range(len(sup.EntityChunks))]
    
    print sup.superInSent()
    print sup.EntityChunks, '\n', sup.superInNP(suplist)
        
        
        
        