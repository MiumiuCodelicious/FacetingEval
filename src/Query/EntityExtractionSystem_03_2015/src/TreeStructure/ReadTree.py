'''
Created on Mar 16, 2012

@author: fifille
'''

from ParseTree import *


class ReadTree():
    '''
     Given file output from CCGParser, build parse tree ptree and POS tag list poslist
    '''
    def __init__(self, parsetree):
        self.ptree = ParseTree()
        self.posdict = dict()
        self.verbdict = dict()
        self.noundict = dict()
        self.__buildTree(parsetree)
        
    def __buildTree(self, parsetree):
        '''
          Need parsetree to be a list, containing line by line parse tree. 
        '''
#         fhandler = open(filepath, 'r')
        prev = 0
        
#         for line in fhandler.readlines():
        for line in parsetree:
            # skip header in every file 

            if line[0]=='%' or line[0:2]==':-' or line=='\n' or line=='':
                continue
            # read POS tags
            if line[0:2]=='w(':
                self.readPOS(line)
                continue
        
            # read tree    
            whitespace = re.search(r'\s*', line)
            depth = whitespace.end()
            #print depth, line
            
            if self.ptree.root == None:
                self.ptree.root = self.ptree.insert(line[depth:], None)
                current = self.ptree.root
                current.depth = 0
                prev = 0
                
            else:
                if depth == prev+1 and depth > 0:
                    parent = current
                    current = self.ptree.insert(line[depth:], parent)
                    parent.left = current
                         
                elif depth == prev and depth > 0:
                    parent = current.parent
                    current = self.ptree.insert(line[depth:], parent)

                    parent.right = current
                    
                elif depth < prev :
                    # need to back up
                    parent = current.parent
                    jump = prev-depth
                    #print 'parent-- ', parent.func
                    while jump != 0:
                        parent = parent.parent
                        jump = jump -1
                        #print 'parent: ', parent.func
                    #print 'jumped ', prev-depth, ' layers'
                    #print 'to insert: ', line[depth:]
                    #print 'parent=', parent.func, parent.fname
                    current = self.ptree.insert(line[depth:], parent)
                    parent.right = current
                current.depth = depth
                current.parent = parent
                
            prev = depth
#         fhandler.close()
                
                
    def readPOS(self, str):
        '''
         Given a line like this:
         w(1, 1, 'What', 'what', 'WDT', 'I-NP', 'O', '(S[wq]/(S[dcl]\NP))/N').
         Get word index 1, Who, and pos tag WP 
        '''
        splitter = re.compile(r', ')
        list = splitter.split(str[2:-3])
#        index, word, pos, super = int(list[1])-1, re.sub(r'\'', '', list[2]), \
#                                    re.sub(r'\'', '', list[4]), re.sub(r'\'', '', list[7])  
        index, word, pos, super = int(list[1])-1, re.sub(r'\'', '', list[2]), \
                                    re.sub(r'\'', '', list[3]), re.sub(r'\'', '', list[4])  
        self.posdict[index]=(word, pos, super)
        
        
    def printPOS(self):
        print self.posdict
        
    def printTree(self, root):
        self.ptree.printTree(root)

    def isVerbNoun(self, w):
        if re.match(r'VB.*', self.posdict[w][1]):
            self.verbdict[w] = self.posdict[w]    
        if re.match(r'(NP)|(NN.*)|(CD)', self.posdict[w][1]):
            self.noundict[w] = self.posdict[w]
            
    def buildDict(self):
        # split the tree by main verb
        map(self.isVerbNoun, self.posdict)
        
    def treeWalk(self, root, leaves):
        # given a subtree root, recursively print all subtree leaves
        if root == None:
            return
        if root.func == 'lf':
            leaves.append([int(root.w_index)-1, self.posdict[int(root.w_index)-1]])
        else:
            self.treeWalk(root.left, leaves)
            self.treeWalk(root.right, leaves)
        return leaves


'''
Unit Test --------------------------------------------------------------------------------
'''
if __name__ == '__main__':
    filepath = '../test/err.txt'  
    fhandler = open(filepath, 'r')
    parsetree = fhandler.readlines()
    readfile = ReadTree(parsetree)
        
    print '\ntest buildTree ----------------------------------'
    readfile.printTree(readfile.ptree.root)
    
    print '\ntest POS -----------------------------------'
    #readfile.printPOS()
    print readfile.posdict
    
    print '\ntest verbs ---------------------------------'
    print readfile.buildDict()
