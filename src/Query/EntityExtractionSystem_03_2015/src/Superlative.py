'''
Created on Mar 20, 2012

@author: ivanka
'''

from Temp import *

class TempSuperlative(Temp):
    '''
    A superlative spotted in a sentence modifying a main NP chunk of independent axis 
    '''    
    def __init__(self, filepath):
        self.init(filepath)
        self.SuperVar = []  # the pure NP that is modified by the superlative
        self.WholeSuperVar = [] # the complete NP phrase including the superlative
        
        
    def getSuperlative(self):
        '''
         JJS - Adjective, superlative
         RBS - Adverb, superlative
        '''
        for i in range(len(self.posdict)):
            if re.match('(JJS)|(RBS)', self.posdict[i][1]):
                return i    # return the index of this superlative
    
    
    
    def getSuperNode(self, sup_index ,root):
        '''
         Fine in parse tree the node of superlative leaf
        '''
        nodes_to_visit = [root]
        while len(nodes_to_visit) != 0:
            currentnode = nodes_to_visit.pop()
            if currentnode.func == 'lf' and int(currentnode.w_index) == sup_index:
                return currentnode
            else:
                if currentnode.left != None:
                    nodes_to_visit.append( currentnode.right )
                if currentnode.right != None:
                    nodes_to_visit.append( currentnode.left )
                    
            
    def getSuperVar(self):
        '''
         We cannot be sure if the NP chunk modified by this superlative is dependent or independent variable.
         It needs to be judged together with other templates, eg TempWhichN or TempWhatN
        '''
        # find superlative word in parse tree
        sup_index = self.getSuperlative()
        # do depth first search to find the sup_index-th leaf:
        
        sup_node = self.getSuperNode(sup_index, self.ptree.root)
        
        # Big Case 1: Superlative expecting forward (../..) a Noun Phrase or Noun
        terms = []
        terms = self.getTerms(sup_node.leaftag)
        
        if len(terms) == 3 and terms[1] == '/':
            # When the Superlative is expecting something on the right side
            #if re.match(r'(^N$)|(^NP$)', terms[2]) and sup_node.parent.right!=None:
            if all((re.match(r'(^N$)|(^NP$)', terms[2]), sup_node.parent.right!=None)):                
                # When expected is NP or N:
                # Case 1.1 Two levels up, there is no right hand sibling. No recursion needed.
                leaves = []     # prepare to get Super Variable
                self.SuperVar += self.input.treeWalk(sup_node.parent.right, leaves)
                leaves = []     # prepare to get Whole Super Variable
                if sup_node.parent.parent.left.func == 'lf':
                    self.WholeSuperVar += self.input.treeWalk(sup_node.parent.parent, leaves)
                
                # Case 1.2 Two levels up, there is right sibling and recursion needed.
                if sup_node.parent.parent.parent.left == sup_node.parent.parent and sup_node.parent.parent.parent.right != None:
                    right = sup_node.parent.parent.parent.right
                    leaves = []
                    right_rec = self.input.treeWalk(right, leaves)
                    self.SuperVar += right_rec
                    self.WholeSuperVar += right_rec
            
            elif re.match(r'(^JJ$)|(^RB$)' , self.posdict[sup_index+1][1]):
                # when expected is not NP or N, but Superlative word followed immediately by a JJ or RB:
                # Case 2
                right = sup_node.parent.parent.right
                leaves = []
                self.SuperVar += self.input.treeWalk(right, leaves)
                leaves = []
                self.WholeSuperVar += self.input.treeWalk(right.parent.parent, leaves)
                
        elif len(terms) == 3 and terms[1]=='\\':
            # When the superlative is expecting something backwards, normally a Verb
            
            if terms[2] == sup_node.parent.left.getTag():
                left = self.input.treeWalk(sup_node.parent.left)
                self.SuperVar += left
                self.WholeSuperVar += left
            
            
        elif len(terms) == 1:
            # When the superlative is not expecting anything, in cases of "the most"
            # You cannot tell...
            
            self.SuperVar = []
            self.WholeSuperVar = []
        
            
                
if __name__ == '__main__':
    sl = TempSuperlative('test/super.txt')
    print 'superlative index = ', sl.getSuperlative()
    #supernode.printNode()
    sl.getSuperVar()
    query = ''
    for p in sl.posdict:
        query += sl.posdict[p][0] + ' '
    print query
    print sl.SuperVar, '\n', sl.WholeSuperVar