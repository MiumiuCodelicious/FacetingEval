'''
Created on Mar 13, 2012

@author: zhuo
'''
import re

'''
Node classes -------------------------------------------------------------
'''
class SuperNode():
    '''
     general supernode = func (param)
     eg 1.  fa('NP[nb]',
     eg 2.  rp('NP',
     eg 3.  ba('NP',
     eg 4.  ccg(1,
    '''

    def __init__(self, func, fname, param):
        self.func = func      # ccg function name
        self.param = param       # ccg function parameter
        self.fname = fname
        self.init()
        
        
    def init(self):         # initialization for tree use
        self.depth = 0
        self.left = None
        self.right = None
        self.parent = None
    
    def printNode(self):
        print (self.depth)*' ', self.fname, self.param


class LeafNode(SuperNode):
    '''
     leaf node only to retrieve word and POS tag
     eg 1.  lf(1,9,'NP[nb]/N'),
     eg 2.  lf(1,13,'N')))))))),
    '''
    
    def __init__(self, func, w_index, leaftag):  
        self.func = func
        self.w_index = w_index      # leaf word's index in original sentence count from 1
        self.leaftag = leaftag
        self.init()
    
    def printNode(self):
        print (self.depth)*' ', self.func, self.w_index, self.leaftag
    
    def getTag(self):
        return self.leaftag
    
    def getW_index(self):
        return self.w_index
    
    
class LexNode(SuperNode):
    '''
     lex node only for type uprising
     eg 1.  lex('N','NP',
     eg 2.  lex('S[dcl]\NP','NP\NP',
    '''
    
    def __init__(self, func, castfrom, castto):
        self.func = func
        self.castfrom = castfrom
        self.castto = castto    # super tag that is casted to
        self.init()
        
    def printNode(self):
        print (self.depth)*' ', self.func, self.castfrom, self.castto
    
    def getTag(self):
        return self.castto
    
    def getCastfrom(self):
        return self.castfrom
    
    
class ConjNode(SuperNode):
    
    def __init__(self, func, result, expect):
        self.func = func
        self.result = result
        self.expect = expect
        self.init()
    
    def printNode(self):
        print (self.depth)*' ', self.func, self.result, self.expect
        
    def getTag(self):
        return self.expect
    
    def getResult(self):
        return self.result
'''
Tree class-------------------------------------------------------------------
'''        
class ParseTree():
    # Build parse tree
    
    def __init__(self):
        self.root = None
    
    def addNode(self, str):
        # create a node and return it
        
        if len(str)==0:
            return 
        
        decode = self.decodeStr(str)
        
        if decode[0] == 'ccg':
            # (ccg, '')
            return SuperNode(decode[0], 'ccg', decode[1])
        
        elif decode[0] == 'super':
            # return from decodeStr: (func, fname, param) 
            return SuperNode(decode[0], decode[1], decode[2])
            
        elif decode[0] == 'lf':
            # return from decodeStr: (func, w_index, leaftag)
            return LeafNode(decode[0], decode[1], decode[2])
        
        elif decode[0] == 'lex':
            # return from decodeStr: (func, castfrom, castto)
            return LexNode(decode[0], decode[1], decode[2])
        elif decode[0] == 'conj':
            # return from decodeStr: (func, result, expect)
            return ConjNode(decode[0], decode[1], decode[2])
    
    
    def decodeStr(self, str):
        # each line is a node, decode this string str and get fields of tag
        
        splitter = re.compile(r',')
        
        if str[0:3]=='ccg':
            func = 'ccg'
            return func, ''
        
        elif str[0:2]=='lf':
            '''
             lf node is sure to have pair of ( )
             format: lf(sent_no, w_index, leaftag)
            '''
            func = 'lf'
            end = re.search(r'\'\)', str)
            coma = re.search(r'\',\'', str)
            if coma != None:
                leaftag = ','
                sent_no, w_index = splitter.split(str[3:coma.start()-1])
            else:
                sent_no, w_index, leaftag = splitter.split(str[3:end.start()]) 
            return func, w_index, re.sub(r'\'', '', leaftag)
            
        elif str[0:3]=='lex':
            '''
             lex node has no closing paren )
             format: lex (castfrom, castto,
            '''
            func = 'lex'
            castfrom, castto, blanc = splitter.split(str[4:])    # blanc is useless just to match list length
            return func, re.sub(r'\'', '', castfrom), re.sub(r'\'', '', castto)  
    
        elif str[0:4] == 'conj':
            '''
             conj node has no closing paren )
             format: conj('conj', result, expect
            '''
            func = 'conj'
            if str[5:8] == '\',\'':
                result, expect = '\'N\'', '\'N\\N\''
            else:   
                fname, result, expect, blanc= splitter.split(str[4:])
            return func, result, expect
        
        else:
            '''
             super tags has no closing paren )
             format: func (param,
            '''
            func = 'super'
            start = 0
            for i in range(len(str)):
                if str[i] == '(':
                    start = i+1
                    fname = str[:i]
                    break
            param, blanc = splitter.split(str[start:])
            return func, fname, re.sub(r'\'', '', param)
    
    
    def insert(self, str, parent):
        
        newNode = self.addNode(str)     # create new node
        if not isinstance(newNode, SuperNode):   # make sure node created as SuperTag or its subclass
            return
        
        if parent == None:
            # tree empty, parent is passed in as root
            self.root = newNode
            self.root.depth = 0
        else:
            if parent.left == None:
                # only right child is allowed to be None
                parent.left = newNode
                newNode.parent = parent
                newNode.depth = parent.depth + 1
            elif parent.right == None:
                parent.right = newNode
                newNode.parent = parent
                newNode.depth = parent.depth + 1
            else:
                # No space for insertion. Parent both children full.
                return
                
        return newNode
        
        
    def printTree(self, root):
        # pass any node as root to support print subtree
        root.printNode()
        # do depth first tree walk
        if root.left != None:
            self.printTree(root.left)
        if root.right != None:
            self.printTree(root.right)
                


'''
Unit Test ----------------------------------------------------------------------------------
'''
if __name__ == '__main__':
    ptree = ParseTree()
    
    print 'test decodeStr ------------------------------'
    print ptree.decodeStr('ccg(1,')
    print ptree.decodeStr( 'lf(1,4,\'N\')),')
    #print ptree.decodeStr('lex(\'N\',\'NP\',')
    print ptree.decodeStr('fa(\'S[dcl]\\NP\',')
    func,castfrom, castto = ptree.decodeStr('lex(\'NP\',\'NP/(NP\\NP)\',')
    print func, castfrom, castto
    
    print 'test addNode --------------------------------'
    ccg_node = ptree.addNode('ccg(1,')
    print ccg_node.func, ccg_node.param
    
    leaf_node = ptree.addNode('lf(1,4,\'N\')),')
    print leaf_node.func, leaf_node.w_index, leaf_node.leaftag
    
    middle_node = ptree.addNode('fa(\'S[dcl]\\NP\',')
    print middle_node.func, middle_node.param
    
    lex_node = ptree.addNode('lex(\'NP\',\'NP/(NP\\NP)\',')
    print lex_node.func, lex_node.castfrom, lex_node.castto
    
    print 'test insert ---------------------------------'
    fullTree = ParseTree()
    current = fullTree.insert('ccg(1,', None)
    
    print 'test sequence of insertion --------------------------'
    '''
     test sentence "Who ate pizza ?"
     ccg(1,
      rp('S[wq]',
       fa('S[wq]',
        lf(1,1,'S[wq]/(S[dcl]\NP)'),
        fa('S[dcl]\NP',
         lf(1,2,'(S[dcl]\NP)/NP'),
         lex('N','NP',
          lf(1,3,'N')))),
       lf(1,4,'.'))).
    '''
    tmp = fullTree.insert('rp(\'S[wq]\',', current)
    
    current = fullTree.insert('fa(\'S[wq]\',', tmp)
    
    fullTree.insert('lf(1,1,\'S[wq]/(S[dcl]\\NP)\'),', current)
    current = fullTree.insert('fa(\'S[dcl]\NP\',', current)
    
    fullTree.insert('lf(1,2,\'(S[dcl]\\NP)/NP\'),', current)
    current = fullTree.insert('lex(\'N\',\'NP\',', current)
    
    fullTree.insert('lf(1,3,\'N\')))),', current)
    
    fullTree.insert('lf(1,4,\'.\'))).', tmp)
    
    fullTree.printTree(fullTree.root)   # pass test
