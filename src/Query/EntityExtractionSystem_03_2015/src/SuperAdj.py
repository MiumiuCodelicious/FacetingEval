'''
Created on May 7, 2012

@author: fifille
'''

import re
from TreeStructure.ReadTree import *

class SuperAdj():

    def __init__(self, ptree, posdict):
        self.ptree, self.posdict = ptree, posdict
        self.adj = []
    
    def getSuperlative(self):
        '''
        JJS - Adjective, superlative
        RBS - Adverb, superlative
        '''
        for i in range(len(self.posdict)):
            if re.match('(JJS)|(RBS)', self.posdict[i][1]):
                return i    # return the index of this superlative
        return -1

    def getSuperNode(self, sup_index ,root):
        '''
         Find in parse tree the node of superlative leaf
        '''
        nodes_to_visit = [root]
        while len(nodes_to_visit) != 0:
            currentnode = nodes_to_visit.pop()
            if currentnode.func == 'lf' and int(currentnode.w_index) == sup_index+1:
                return currentnode
            else:
                if currentnode.left != None:
                    nodes_to_visit.append( currentnode.left )
                if currentnode.right != None:
                    nodes_to_visit.append( currentnode.right )             
    
            
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
    

    def getSuperAdj(self):
        '''
         We cannot be sure if the NP chunk modified by this superlative is dependent or independent variable.
         It needs to be judged together with other templates, eg TempWhichN or TempWhatN
        '''
        # find superlative word in parse tree
        sup_index = self.getSuperlative()
        # do depth first search to find the sup_index-th leaf:
        
        if sup_index != -1:
            sup_node = self.getSuperNode(sup_index, self.ptree.root)
           
            terms = []
            terms = self.getTerms(sup_node.leaftag)
            
            if len(terms) == 3 and terms[1] == '/':
                # When the Superlative is expecting something on the right side  
                if re.match(r'(^N$)|(^NP$)', terms[2]) and sup_node.parent.right!=None:
                    pass    # dealt by from noun phrase extraction
                elif re.match(r'(^JJ$)|(^RB$)' , self.posdict[sup_index+1][1]):
                    # when expected is not NP or N, but Superlative word followed immediately by a JJ or RB:
                    self.adj += self.treeWalk(sup_node.parent.right, [])
            
            elif len(terms) == 3 and terms[1]=='\\':
                # When the superlative is expecting something backwards, normally a Verb
                if terms[2] == sup_node.parent.left.getTag():
                    self.adj += self.treeWalk(sup_node.parent.left, [])
                    
            elif len(terms) == 1:
                # When the superlative is not expecting anything, in cases of "the most"
                # You cannot tell...            
                self.adj = self.posdict[sup_index]
    
            return self.adj, sup_node
        else:   return None, None
            
    
    def getTerms(self, tag):
        # get only the first level of terms. need to use recursively to get all terms
        paren_stack = []
        terms = []
        term = ''
        # parse tag at highest level, seperate into 2 parts
        for t in tag:
            if t == '(':
                if len(paren_stack) > 0 :
                    term = term + t   # don't want the outter layer parenthesis     
                paren_stack.append(t) 
            elif t == ')':
                if len(paren_stack) > 0:    paren_stack.pop()
                if len(paren_stack) > 0:
                    term = term + t         # don't want the outter layer parenthesis
            elif (t == '/' or t == '\\') and len(paren_stack) == 0:
                terms.append(term)
                terms.append(t)
                term = ''
            else:
                term = term + t
        # push the last  term gotten from last loop
        terms.append(term)
        return terms 



if __name__ == '__main__':
        # from ReadFile ------------------------------------------------------------
        filepath = 'test/err.txt'
        fhandler = open(filepath, 'r')
        single_tree = fhandler.readlines()
        input = ReadTree(single_tree)
        
        sadj = SuperAdj(input.ptree, input.posdict)
        print sadj.posdict
        print sadj.getSuperAdj()
        
    