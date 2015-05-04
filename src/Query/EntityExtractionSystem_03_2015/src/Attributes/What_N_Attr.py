'''
Created on May 3, 2012

@author: fifille
'''

from Which_N_Attr import *

class What_N_Attr(Which_N_Attr):

    def init(self):
        self.whatr = '(w|W)hat'
        
    def WhatSent(self):
        # is the sentence a what-sentence
        return self.WhichSent(self.whatr)
        
    def WhatEntity(self):
        # is the noun immediately following What
        iswhat = self.WhatSent()
        #print 'is this a what sentence? ', iswhat
        whatlist = [[] for i in range(len(self.EntityChunks))]
        #print 'the entity following What is: '
        return self.WhichEntity(iswhat, self.whatr, whatlist)


if __name__ == '__main__':
    
    # ------------------------------------------------------------------------------------------------------------------------    
    from TreeStructure.ReadFile import *
    filepath = '../test/wha.txt'  
    readfile = ReadFile(filepath)

    from EntityExtract import EntityExtract
    entity = EntityExtract(readfile.ptree, readfile.posdict, readfile.verbdict, readfile.noundict)
    entity.EntityChunker(entity.ptree.root)          
    entity.onlyNP()
    # ------------------------------------------------------------------------------------------------------------------------

    what = What_N_Attr(entity.ptree, entity.posdict,  entity.EntityChunks, entity.EntityRoots)
    what.WhatEntity()
