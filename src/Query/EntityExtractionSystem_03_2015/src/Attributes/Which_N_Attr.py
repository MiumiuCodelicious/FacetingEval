'''
Created on May 4, 2012

@author: fifille
'''
from Attribute import *

class Which_N_Attr(Attribute):
    
    def init(self):
        self.whichr = r'(W|w)hich'
    
    def WhichSent(self, regex):
        # Case 1: Which is the first word in the sentence
        if len(self.posdict) > 0 and (re.match(regex, self.posdict[0][0]) and \
            not re.match('(is)|(are)|(was)|(were)|(isnt)|(wasnt)|(arent)|(werent)|(isn\'t)|(wasn\'t)|(aren\'t)|(weren\'t)', self.posdict[1][0]))\
            or (re.match(regex, self.posdict[1][0]) and self.posdict[0][1] == 'IN'):
            return 1
        else:   return 0
        # Case 2: Which has a preposition in front
        # Case 3: Which has a sentence in front
    
    def WhichEntity(self, isWhich, regex, whichlist):
        # is the noun immediate right sibling of the "Which" leaf?
        # whichlist need to be initialized into length = length of EntityChunks
        whichlist = [0 for e in self.EntityChunks]
        for r in self.EntityRoots:
            index = self.EntityRoots.index(r)
            if isWhich == 1:
                # find which leaf node
                which_node = self.ptree.root.left
                while which_node.func != 'lf':
                    which_node = which_node.left
                # go to right sibling Noun Phrase
                if which_node.parent.right != which_node and r == which_node.parent.right:
                    # if right sibling is Noun Phrase we are looking for
                    whichlist[index] = 1
            if self.EntityChunks[index][0][0] > 0 and \
                ((re.match('IN', self.posdict[(self.EntityChunks[index][0][0])-1][1]) and \
                re.match(regex, self.EntityChunks[index][0][1][0]))
                 or re.match(regex, self.posdict[(self.EntityChunks[index][0][0])-1][0])):
                # the chunk's lower word follows a "What" or "what"
                whichlist[index] = 1
        return whichlist
    
     
        
        
if __name__ == '__main__':
    
    # ------------------------------------------------------------------------------------------------------------------------
    from TreeStructure.ReadFile import *
    filepath = '../test/which.txt'   # "What is the difference of GDP per capita between U.S. and Japan in the year 2001 ?"
    readfile = ReadFile(filepath)

    from EntityExtract import EntityExtract
    entity = EntityExtract(readfile.ptree, readfile.posdict, readfile.verbdict, readfile.noundict)
    entity.EntityChunker(entity.ptree.root)
    entity.onlyNP()
    # ------------------------------------------------------------------------------------------------------------------------
    
    which = Which_N_Attr(entity.ptree, entity.posdict, entity.EntityChunks, entity.EntityRoots)
    isWhich = which.WhichSent(which.whichr)
    
    print 'Is this a which-sentence: '
    print isWhich
    
    print 'which phrase is the which-noun: '
    whichlist = [[] for i in range(len(entity.EntityChunks))]
    print which.WhichEntity(isWhich, which.whichr, whichlist)
