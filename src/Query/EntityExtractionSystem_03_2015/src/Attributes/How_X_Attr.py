'''
Created on May 8, 2012

@author: fifille
'''
from Attribute import *

class How_X_Attr(Attribute):
    
    def init(self):
        self.manyr, self.muchr, self.dor, self.haver = \
            re.compile('many'), re.compile('much'), \
            re.compile('(do)|(does)|(did)|(donnot)|(dont)|(don\'t)|(doesnt)|(doesn\'t)|(didnt)|(didn\'t)'), \
            re.compile('(have)|(has)|(had)|(havent)|(haven\'t)|(hasnt)|(hasn\'t)|(hadnt)|(hadn\'t)')


    def howXSent(self, regex):
        # leave regex to choose how many, how much, how do, how have
        if re.match('(H|h)ow', self.posdict[0][0]) and re.match(regex, self.posdict[1][0]):   return 1
        else:   return 0

        
    def howManyMuch(self, manymuch_list):
        # for how many, how much questions, see if how and many/much have the same parent node
        # and find the entity following it
        # manymuch_list must be initialized same length as EntityChunks
        if self.howXSent(self.manyr) or self.howXSent(self.muchr):
            how_node, many_much_node = self.__many_much()
            # get Entity
            if how_node.parent == many_much_node.parent:
                # How + many are together
                for r in self.EntityRoots:
                    if many_much_node.parent.parent.right != None and how_node.parent.parent.right == r:
                        manymuch_list[self.EntityRoots.index(r)] = 1
                    else:   manymuch_list[self.EntityRoots.index(r)] = 0
            else:   # many + entity are together
                for r in self.EntityRoots:
                    if many_much_node.parent.right != None and many_much_node.parent == r:
                        #print self.EntityChunks[self.EntityRoots.index(r)]
                        manymuch_list[self.EntityRoots.index(r)] = 1
                    else:   manymuch_list[self.EntityRoots.index(r)] = 0
            return manymuch_list
        else:   return [0 for e in self.EntityChunks]
    
    
    
    def __many_much(self):
        # left walk tree to find 'How' leaf:
        how_node = self.ptree.root.left
        while how_node.func != 'lf':
            how_node = how_node.left
        # find many_much leaf:
        if how_node.parent.left == how_node and how_node.parent.right.func == 'lf':
            return how_node, how_node.parent.right
        elif how_node.parent.right != None and how_node.parent.right != how_node:
            many_much_node = how_node.parent.right
            while many_much_node.func != 'lf': 
                many_much_node = many_much_node.left 
            return how_node, many_much_node
    
    
    def howDoHaveEntity(self, howDoHaveList):
        # is the noun immediate right sibling of "What is"?
        # howDoHaveList need to be initialized into length = length of EntityChunks
        if self.howXSent(self.dor) == 1 or self.howXSent(self.haver) == 1:
            for en in self.EntityChunks:
                if en[0][0] == 2:
                    # the chunk's lower index is second word in POS list, immediately following "Which" checked by isWhich flag
                    # print self.EntityChunks[self.EntityChunks.index(en)]
                    howDoHaveList[self.EntityChunks.index(en)] = 1
                else:    howDoHaveList[self.EntityChunks.index(en)] = 0
            return howDoHaveList
        else:   return [0 for e in self.EntityChunks]

    
if __name__ == '__main__':
     
    # ------------------------------------------------------------------------------------------------------------------------
    from TreeStructure.ReadFile import *
    filepath = '../test/howdo.txt'      # whatis2.txt
    readfile = ReadFile(filepath)

    from EntityExtract import EntityExtract
    entity = EntityExtract(readfile.ptree, readfile.posdict, readfile.verbdict, readfile.noundict)
    entity.EntityChunker(entity.ptree.root)
    entity.onlyNP()
    # ------------------------------------------------------------------------------------------------------------------------

    howx = How_X_Attr(entity.ptree, entity.posdict, entity.EntityChunks, entity.EntityRoots)
    print 'Is this sentence how many/much? ', howx.howXSent(howx.manyr)
    print 'Which entity follows how many/much? '
    manymuch_list = [[] for i in range(len(howx.EntityChunks))]
    print howx.howManyMuch(manymuch_list)
    
    print 'Which entity follows how do/have? '
    howDoHaveList = [[] for i in range(len(entity.EntityChunks))]
    print howx.howDoHaveEntity(howDoHaveList)

    
    
    
    
    