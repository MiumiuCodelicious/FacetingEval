'''
Created on May 11, 2012

@author: fifille
'''
'''
Created on May 4, 2012

@author: fifille
'''
from Attribute import *

class What_Is_Attr(Attribute):
    
    def init(self):
        self.whatr = r'(W|w)hat'
    
    def WhatIsSent(self, regex):
        # Case 1: Which is the first word in the sentence
        if re.match(regex, self.posdict[0][0]) and \
            re.match('(is)|(are)|(was)|(were)|(isnt)|(wasnt)|(arent)|(werent)|(isn\'t)|(wasn\'t)|(aren\'t)|(weren\'t)', self.posdict[1][0]):
            return 1
        else:   return 0
        # Case 2: Which has a preposition in front
        # Case 3: Which has a sentence in front
    
    def WhatIsEntity(self, isWhatis, whatislist):
        # is the noun immediate right sibling of "What is"?
        # whatislist need to be initialized into length = length of EntityChunks
        if isWhatis == 1:
            for en in self.EntityChunks:
                if en[0][0] == 2:
                    # the chunk's lower index is second word in POS list, immediately following "Which" checked by isWhich flag
                    #print self.EntityChunks[self.EntityChunks.index(en)]
                    whatislist[self.EntityChunks.index(en)] = 1
                else:    whatislist[self.EntityChunks.index(en)] = 0
            return whatislist
        else:   return [0 for e in self.EntityChunks]
     
        
        
if __name__ == '__main__':
    
    # ------------------------------------------------------------------------------------------------------------------------
    from TreeStructure.ReadFile import *
    filepath = '../test/whatis.txt'   # "What is the difference of GDP per capita between U.S. and Japan in the year 2001 ?"
    readfile = ReadFile(filepath)

    from EntityExtract import EntityExtract
    entity = EntityExtract(readfile.ptree, readfile.posdict, readfile.verbdict, readfile.noundict)
    entity.EntityChunker(entity.ptree.root)
    entity.onlyNP()
    # ------------------------------------------------------------------------------------------------------------------------
    def printdict():
        print ' '.join([entity.posdict[key][0] for key in entity.posdict])
    def printf(e):
        print ' '.join([w for w in [l[1][0] for l in e]])
    map(printf, entity.EntityChunks)
    printdict()
    
    
    whatis = What_Is_Attr(entity.ptree, entity.posdict, entity.EntityChunks, entity.EntityRoots)
    isWhatis = whatis.WhatIsSent(whatis.whatr)
    
    print 'Is this a what-is-sentence: '
    print isWhatis
    
    print 'which phrase is the what-is-noun: '
    whatislist = [[] for i in range(len(entity.EntityChunks))]
    print whatis.WhatIsEntity(isWhatis, whatislist)
