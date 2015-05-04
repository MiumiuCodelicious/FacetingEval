'''
Created on Mar 17, 2012

@author: fifille
'''
import sys

from TreeStructure.ReadTree import *
from stopword import *
            
         
class EntityExtract():
    
    def __init__(self, ptree, posdict, verbdict, noundict):
        self.ptree, self.posdict, self.verbdict, self.noundict = ptree, posdict, verbdict, noundict
        self.verbSplit = []     # a list of splitted words
        self.EntityChunks = []      # list of nodes as root of NP chunks
        self.EntityRoots = []       # list of node roots corresponding to NPchunks[]
        self.MinimalEntityChunks = []   # Kathy's trial
        self.MinimalEntityRoots = []    # Kathy's trial

    def splitByVerb(self):
        # split word list by verbs
        sublist = []
        for v in range(len(self.posdict)):
            if v in self.verbdict or v+1 == len(self.posdict):
                self.verbSplit.append(sublist) 
                sublist = []
            else:
                sublist.append(v)
           
    def EntityChunker(self, root):
        '''
         any SuperNode that has param = 'NP', or LexNode that has castto = 'NP', or LeafNode that has leaftag = 'NP' or 'N'
        '''
        if root == None:
            return 
        
        # flags setup ---------------------------------------------------------------- 
        flag_chunkRoot, flag_referRule, flag_quantRule, flag_trendRule, flag_compareRule, \
                                flag_chainRule, flag_gradientRule, flag_onlyRule, \
                                carrierq, carriert, carrierc, carrierg = 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
                                
        quant, trend, compare, gradient = \
                    re.compile('(number)|(quantity)|(amount)|(percentage)|(rate)|(portion)|'
                               '(proportion)|(percent)|(rest)|(part)|(beginning)|(ratio)|'
                               '(end)|(middle)|(preceding)|(afterward)|(disadvantage)|(statistics)|'
                               '(sign)|(initiation)|(start)|(starting)|(finish)|(finishing)|'
                               '(reason)|(cause)|(result)|(consequence)|(downside)|(advantage)|'
                               '(unit)|(size)|(volume)|(weight)|(area)|(length)|(remainder)|'
                               '(rest)|(level)|(grade)|(score)'
                               '(depth)|(width)|(range)|(concentration)|(record)|'
                               '(possibility)|(probability)|(odds)|(feasibility)|(coordinate)|'
                               '(latitude)|(longitude)|(statistic)|(million)|(hundred)|(tens)|'
                               '(thousand)|(billion)|([G|g][B|b])|([K|k][B|b])|([T|t][B|b])|(Byte)'), \
                    re.compile('(trend)|(change)|(increment)|(decrement)|(slop)|(curve)|(line)|'
                               '(climb)|(rise)|(fall)|(fluctuation)|(gain)|(inflation)|(gain)|(loss)|'
                               '(growth)|(loss)|(up)|(down)|(increase)|(decrease)|(grow)|(win)|(lose)'
                               '|(failure)|(expansion)|(shrink)|(expand)|(raise)|(up)|(down)|(add)'), \
                    re.compile('(compare)|(opposite)|'
                               '(comparison)|(diff)|(difference)|(variation)|(variant)|(contrary)'), \
                    re.compile('(term)|(subject)|(respect)|(relation)|(regards)')
#        quant, trend, compare, gradient = \
#                {'number', 'quantity', 'amount', 'rate', 'percentage', 'percent', 'portion', \
#                 'proportion', 'part', 'distribution', 'price', 'unit', 'size', 'volume', \
#                 'weight', 'area', 'length', 'temperature', 'duration', 'depth', 'width', \
#                 'range', 'concentration', 'beginning', 'end', 'middle', 'preceding', 'posterior', \
#                 'position', 'afterward', 'advantage', 'disadvantage', 'benefit', 'component', \
#                 'reason', 'cause', 'result', 'consequence', 'downside', 'score', 'grade', \
#                 'record', 'level', 'rest', 'remainder'},\
#                {'trend', 'change', 'increment', 'decrement', 'slop', 'curve', 'line', 'climb', 'expansion', \
#                 'rise', 'fall', 'gain', 'loss', 'fluctuation', 'growth', 'ups', 'downs', 'failure', 'inflation'},\
#                {'compare', 'opposite', 'comparison', 'diff', 'difference', 'variation', 'variant', 'contrary'},\
#                {'terms', 'subject', 'respect', 'relation', 'title', 'name'}
        '''
        assume the node is not a NP chunk root, not starting with a referring useless NP, 
        not containing "the number of" and need splitting, 
        not containing "the trend of", do not discard first NP, 
        not containing the "the comparison of", do not discard first NP
        flag_onlyNPRule: assume the first NP always need further split
        '''
        # ----------------------------------------------------------------------------
        
        # if current node only have a left child, or right child is simply a punctuation leaf, skip the next level:
        while (root.left != None and root.right == None) \
                or (root.right!=None and root.right.func == 'lf' and re.match('\.|,', root.right.leaftag )):
            root = root.left 

        flag_chunkRoot = self.chunkRoot(root)    # check if current node is head of NP chunk
        
        if flag_chunkRoot != None:
            # recursively get the subtree
            leaves = []
            leaves = self.treeWalk(flag_chunkRoot, leaves)

            # if the NP is started with a "that" kind of useless NP, no more split -----------------
            flag_referRule = self.__referRule(root, leaves[0])
            
            # for quant rule ------------------------------------------            
            flag_quantRule, carrierq = self.__quantTrendCompareRule(quant, leaves)
            #print 'quant--'

            # for trend rule ------------------------------------------
            flag_trendRule, carriert = self.__quantTrendCompareRule(trend, leaves)
            #print 'trend--'
                
            # for compare rule ----------------------------------------
            flag_compareRule, carrierc = self.__quantTrendCompareRule(compare, leaves)
            #print 'compare--'
                
            # for gradient rule ---------------------------------------
            flag_gradientRule, carrierg = self.__quantTrendCompareRule(gradient, leaves)
            #print 'gradient--'
                
            # for chain rule ------------------------------------------
            flag_chainRule = self.__chainRule(flag_chunkRoot, root)
                
            # for only np rule ----------------------------------------
            flag_onlyRule = self.__onlyNPRule(leaves)
              
            # exclude nouns that only have stop words
            sum = 0
            for l in leaves:
                if l[1][0].lower() in stopword_list: sum += 1

            if sum != len(leaves) and not flag_referRule and not flag_gradientRule  and not flag_trendRule  and not flag_quantRule and not flag_compareRule:
                self.EntityChunks.append(leaves)    # get NP words in the NP list
                self.EntityRoots.append(root)

#           for test use ---------------------------------------------------------------------------------
#            print 'chunk node : \n ', leaves, root.depth
#            root.printNode()
#            print flag_chainRule, self.__conjRule(flag_chunkRoot, root), flag_onlyRule, \
#            flag_referRule, flag_quantRule, flag_trendRule, flag_compareRule, flag_gradientRule,\
#            carrierq, carriert, carrierc, carrierg
#           for test use ---------------------------------------------------------------------------------


        # left-right recursion ------------------------------------------------------------------------------
        
#        if flag_chainRule != 1 and self.__conjRule(flag_chunkRoot, root) != 1  and flag_referRule != 1:
##            print 'goes here:'
##            print 'every node: ', root.depth
##            root.printNode()
#            # if the chain rule, conjunction rule, refer rule, and quantity rule is satisfied, skip further split.
#            if flag_quantRule != 1 and flag_trendRule != 1 and flag_compareRule != 1 and flag_gradientRule != 1:
##                print 'normal:'
#                self.EntityChunker(root.left)
#                self.EntityChunker(root.right)
#            elif flag_chunkRoot != None:
#                if flag_quantRule == 1:
#                    qtcgNode = self.skipNode(root, quant, carrierq)
#                elif flag_trendRule == 1: 
#                    qtcgNode = self.skipNode(root, trend, carriert)
#                elif flag_compareRule == 1:
#                    qtcgNode = self.skipNode(root, compare, carrierc)
#                elif flag_gradientRule == 1:
#                    qtcgNode = self.skipNode(root, gradient, carrierg)
#                        
#                if qtcgNode != None:  
##                    print 'skipped'
##                    qtcgNode.printNode()
#                    # when leaf qtcg node alone is left child, jump 1 level. otherwise, jump 2 to avoid "the" 
#                    if qtcgNode.parent.left == qtcgNode and qtcgNode.parent.right != None:   
#                        nodeToSkip = qtcgNode.parent
#                    else:
#                        nodeToSkip = qtcgNode.parent.parent
#                    # the node to skip is just a simple leaf node of quant, trend, compare, or gradient
#                    # nodeToSkip.parent is the quant/trend/compare/gradient NP
#                    # nodeToSkip.parent.parent is the whole NO measured by the quant/trend/compare/gradient
#                    if nodeToSkip != None and nodeToSkip != root:
#                        subleaves = []
#                        subleaves = self.treeWalk(nodeToSkip, subleaves)
##                        nodeToSkip.printNode()
##                        print '1'
##                        print 'skipped: ', subleaves
#
#                        if subleaves != None and len(self.EntityChunks[-1]) > len(subleaves) :                                  
##                            print '2'
#                            self.EntityChunks.append(subleaves)
#                            self.EntityRoots.append(nodeToSkip)
#                            self.treeSplit(root, nodeToSkip)
#                        elif subleaves != None and len(self.EntityChunks[-1]) <= len(subleaves):
##                            print '3'
#                            self.EntityChunker(root.right.right)
#                    elif nodeToSkip != None and nodeToSkip == root and flag_onlyRule == 1:
##                        print '....'
#                        # when the root is the node to skip and only NP root, take precaution and recurse
#                        self.EntityChunker(nodeToSkip.right)
#
#                        
            
# ================================== Kathy's new suggestion for getting minimal entities =================================
        if flag_chainRule != 1 :
#            print 'goes here:'
#            print 'every node: ', root.depth
#            root.printNode()
            # if the chain rule, conjunction rule, refer rule, and quantity rule is satisfied, skip further split.\
#            if flag_quantRule != 1 and flag_trendRule != 1 and flag_compareRule != 1 and flag_gradientRule != 1:
            self.EntityChunker(root.left)
            self.EntityChunker(root.right)
#            elif flag_chunkRoot != None:
#                if flag_quantRule == 1:
#                    qtcgNode = self.skipNode(root, quant, carrierq)
#                elif flag_trendRule == 1: 
#                    qtcgNode = self.skipNode(root, trend, carriert)
#                elif flag_compareRule == 1:
#                    qtcgNode = self.skipNode(root, compare, carrierc)
#                elif flag_gradientRule == 1:
#                    qtcgNode = self.skipNode(root, gradient, carrierg)
#                        
#                if qtcgNode != None:  
##                    print 'skipped'
##                    qtcgNode.printNode()
#                    # when leaf qtcg node alone is left child, jump 1 level. otherwise, jump 2 to avoid "the" 
#                    if qtcgNode.parent.left == qtcgNode and qtcgNode.parent.right != None:   
#                        nodeToSkip = qtcgNode.parent
#                    else:
#                        nodeToSkip = qtcgNode.parent.parent
#                    # the node to skip is just a simple leaf node of quant, trend, compare, or gradient
#                    # nodeToSkip.parent is the quant/trend/compare/gradient NP
#                    # nodeToSkip.parent.parent is the whole NO measured by the quant/trend/compare/gradient
#                    if nodeToSkip != None and nodeToSkip != root:
#                        subleaves = []
#                        subleaves = self.treeWalk(nodeToSkip, subleaves)
##                        nodeToSkip.printNode()
##                        print '1'
##                        print 'skipped: ', subleaves
#
#                        if subleaves != None and len(self.EntityChunks[-1]) > len(subleaves) :                                  
##                            print '2'
#                            self.EntityChunks.append(subleaves)
#                            self.EntityRoots.append(nodeToSkip)
#                            self.treeSplit(root, nodeToSkip)
#                        elif subleaves != None and len(self.EntityChunks[-1]) <= len(subleaves):
##                            print '3'
#                            self.EntityChunker(root.right.right)
#                    elif nodeToSkip != None and nodeToSkip == root and flag_onlyRule == 1:
##                        print '....'
#                        # when the root is the node to skip and only NP root, take precaution and recurse
#                        self.EntityChunker(nodeToSkip.right)

# ========================================================================================================================                        

            
            
    def skipNode(self, root, p, carrier):
        nodes_to_visit = [root]
#        print 'root ='
#        root.printNode()
        while len(nodes_to_visit) != 0:
#            print len(nodes_to_visit)
            currentnode = nodes_to_visit.pop()
#            if currentnode != None:
#                print 'pop -------'
#                currentnode.printNode()
            if currentnode != None and currentnode.func=='lf':
#                print 'leaf!! '
#                currentnode.printNode()
                if re.match(p, self.posdict[int(currentnode.w_index)-1][0]):
                    if carrier == 0: 
#                        print 'skipped -----'
#                        currentnode.printNode();
                        return currentnode
                    else:   
#                        print 'skipped carrier = 1'
                        return currentnode.parent
            else:
                if currentnode != None and currentnode.right != None:
#                    print 'append right'
#                    currentnode.right.printNode()
                    nodes_to_visit.append( currentnode.right)
                if currentnode != None and currentnode.left != None:
#                    print 'append left'
#                    currentnode.left.printNode()
                    nodes_to_visit.append( currentnode.left) 
        
        
        
        
    def treeSplit(self, root, nodeToSkip):
        # greatest function ever in this whole project, inspired by Simple_Charts/Queries.txt no.6
        iterNode = nodeToSkip
        while iterNode != root:
            while iterNode.parent.right == None:
                iterNode = iterNode.parent
            if iterNode.parent.right != iterNode:
                self.EntityChunker(iterNode.parent.right)
            iterNode = iterNode.parent
            
            
    '''        
     Definition of NP rules ---------------------------------------------------------------------------------
    '''         
    def __chainRule(self, flag, root):
        # given root of NP node, if the NP is consisted of a chain of N/N N/N N/N ... N, return True
        # eg: 
        # fa('N',
        #  lf(10,5,'N/N'),
        #  lf(10,6,'N'))
        if root.func == 'lf':   return 0
        if flag == None:    return 0
        elif flag != None:    # flag indicate whole subtree is NP chunk
            while root.func != 'lf' and root.left != None:
                # case 1
                if root.left.func == 'lf' and root.right != None and \
                    re.match('(NP?(\[nb\])?/NP?(\[nb\])?)|(NP?(\[nb\])?\\NP?(\[nb\])?)|'
                             '(S(\[.*\])?\\NP?(\[nb\])?)|(NP?(\[nb\])?)', root.left.leaftag) and root.right != None :
                    root = root.right
                # case 2
                elif root.left.func != 'lf' and root.right != None and \
                    ((root.left.func == 'super' and \
                      re.match('(NP?(\[nb\])?/NP?(\[nb\])?)|(NP?(\[nb\])?\\NP?(\[nb\])?)|'
                               '(S(\[.*\])?\\NP?(\[nb\])?)|(NP?(\[nb\])?)', root.left.param)) or \
                     (root.left.func == 'lex' and \
                      re.match('(NP?(\[nb\])?/NP?(\[nb\])?)|(NP?(\[nb\])?\\NP?(\[nb\])?)|'
                               '(S(\[.*\])?\\NP?(\[nb\])?)|(NP?(\[nb\])?)', root.left.castto))) \
                      and self.chunkRoot(root.right)!= None:
                    if self.__chainhelper(root.left) == 0:
                        return 0
                    else:   
                        root = root.right
                else:
                    return 0
            return 1
        else:   return 0
        
        
        
    def __chainhelper(self, node):
        # helper function for __chainRule()
        # to check the case 2 situation
        while node.left != None:
            if node.left.func == 'lf' and node.right != None \
                and re.match('^\(N/N\)/\(N/N\)$', node.left.leaftag) \
                and ((node.right.func == 'lf' and re.match('(N(P)?(\[nb\])?)/(N(P)?(\[nb\])?)', node.right.leaftag)) or \
                     (node.right.func == 'super' and re.match('(N(P)?(\[nb\])?)/(N(P)?(\[nb\])?)', node.right.param)) or \
                     (node.right.func == 'lex' and re.match('(N(P)?(\[nb\])?)/(N(P)?(\[nb\])?)', node.right.castto))    ): 
                node = node.left 
            else: return 0
        return 1
    
                
    def __conjRule(self, flag, root):
        # given root of NP node, if the NP is consisted of a noun conjuncted with another noun, return True
        # eg:
        '''
        ba('NP',
         fa('NP[nb]',
          lf(36,8,'NP[nb]/N'),
          fa('N',
           lf(36,9,'N/N'))),
         conj('conj','NP','NP\NP',
          lf(36,10,'conj'),
          lex('N','NP',
           fa('N',))))
        '''
        if flag != None:
            if root.right != None and root.right.func == 'conj':    return 1
            else:   return 0
        else:   return 0
            
    def __trendCompareRule(self, p, leaves):
        # if the node contain "the comparison", "the trend", ..., return 1
        phrase = ' '.join([w for w in [l[1][0] for l in leaves]])
        if p.match(phrase): return 1
        else:   return 0
        
        
    def __quantTrendCompareRule(self, p, leaves):
        # if the node contain quantity/trend/comparison/gradient measure at beginning of chunk, return 1
        for i in range(min([5, len(leaves)])):
            if re.match(p, leaves[i][1][0]) and re.match('NP?N?S?(\[nb\])?$', leaves[i][1][2]):
                # check the word's role in parse tree
                if i == 0 or i > 0 and not re.match('NP?(\[nb\])?', leaves[i-1][1][1]) :
                    return 1, 1     
        return 0, 0       
#                
#         phrase = ' '.join([w for w in [l[1][0] for l in leaves]])
#        if len(leaves) == 0:    return 0, 0
#        elif re.match(p, phrase):  return 1, carrier
#        elif re.match('(JJ.?)|(RB.?)', leaves[0][1][1]):
#            return self.__quantTrendCompareRule(p, leaves[1:], 1)
#        elif len(leaves) > 1 and re.match('DT', leaves[0][1][1]) and re.match('(JJS)|(RBS)', leaves[1][1][1]):
#            return self.__quantTrendCompareRule(p, leaves[2:], 1) 
#        else:   return 0, 0        
           
    def __referRule(self, root, leaves):
        # given a root of NP node, if this NP starts with words such as "that", "this", "those", "it", "these", return True
        if re.match('(this$)|(that$)|(these$)|(those$)|(it$)|(its$)|(they$)|(them$)|(we$)|(me$)|(you$)|(yours$)'
                    '|(ours$)|(mine$)|(I$)|(theirs$)|(\')', \
                    leaves[1][0]) and leaves[1][2]=='NP':
            return 1
        else: return 0
                
    def __onlyNPRule(self, leaves):
        # given a root of NP node, if this NP is the only NP left in the entire sentence, return True
        comma = -1
        for l in self.posdict:
            if self.posdict[l][1] == ',':   
                comma = l
        if comma != -1 and [comma, self.posdict[comma]] in leaves:
            return 1
        left = [[k, self.posdict[k]] for k in self.posdict if [k,self.posdict[k]] not in leaves]
        if len(left) == 0:
            return 1
        else:
            if left[-1][1][1] == '.':   left = left[:-1]   # get ride of punctuation in a brute force way
            if comma != -1:
                left = left[ comma+1 :]
            for l in left:
                if re.match('(NP?(\[nb\])?)|CD|NN|NNP', l[1][2]):
                    return 0
            return 1
        
            
    # if the only NP rule is satisfied, discard current split --------------------
    def onlyNP(self):
        while len(self.EntityChunks) > 1 and self.__onlyNPRule(self.EntityChunks[0]) == 1:
            self.EntityChunks.pop(0)
            self.EntityRoots.pop(0)
    '''
     Utility functions definition ---------------------------------------------------------------------------
    '''
    def treeWalk(self, root, leaves):
        # given a subtree root, recursively get all subtree leaves
        if root == None:
            return None
        if root.func == 'lf':
            leaves.append([int(root.w_index)-1, self.posdict[int(root.w_index)-1]])
        else:
            self.treeWalk(root.left, leaves)
            self.treeWalk(root.right, leaves)
        return leaves
    
    def chunkRoot(self, node):
        # check if a node is a NP chunk root
        if node.func == 'lf' and re.match(r'(^NP$)|(^NP\[nb\]$)|(^N$)', node.leaftag):
            #node.printNode()
            return node
        elif node.func == 'lex' and re.match(r'(^NP$)|(^NP\[nb\]$)|(^N$)', node.castto):
            #node.printNode()
            return node
        elif node.func == 'conj' and re.match(r'(^NP$)|(^NP\[nb\]$)|(^N$)', node.result):
            #node.printNode()
            return node
        elif node.func == 'super' and re.match(r'(^NP$)|(^NP\[nb\]$)|(^N$)', node.param):
            #node.printNode()
            return node
        else:
            return  
    
    def truncate(self):
        # truncate a list of NP leaves if the tail is not NP
        # VBG and VBD can be modifying NP
        # VB$, VBP, VBZ, normally cannot be part of NP
        for leaves in self.EntityChunks:
            while len(leaves) > 0 and (re.match(r'(VB[$|P|Z])',  leaves[-1][1][1]) and not re.match('NP?(\[nb\])?', leaves[-1][1][2])\
                                        or re.match('\.|,|TO', leaves[-1][1][1])):
                leaves = leaves[:-1]
        
# ================================== Keep only smallest entities ===============================
    def keepMinimal(self):  
        # this function is utility function based on Kathy's suggestion. Get rid of nested bigger entities. Leave only smallest entities.
        rangelist = [[en[0][0], en[-1][0]] for en in self.EntityChunks]
        for index in range( len(self.EntityChunks)-1 ):
            if rangelist[index][0] > rangelist[index+1][0] or rangelist[index][1] < rangelist[index+1][1]:
                self.MinimalEntityChunks.append(self.EntityChunks[index])
                self.MinimalEntityRoots.append(self.EntityRoots[index])
        self.MinimalEntityChunks.append(self.EntityChunks[-1])
        self.MinimalEntityRoots.append(self.EntityRoots[-1])


# ===============================================================================================

'''
Unit Test --------------------------------------------------------------------------------
'''
if __name__ == '__main__':
    
    fpath = "test/err.txt"     
#     fpath = '../../Entity/AllQueryParse.txt'
    
    rf = open(fpath, 'r')
    single_parse_tree = rf.readlines()
    rt = ReadTree(single_parse_tree)
    entity = EntityExtract(rt.ptree, rt.posdict, rt.verbdict, rt.noundict)
    
    '''
    print 'test splitByVerb --------------------------------'
    entity.splitByVerb()  # this function does not return anything, print None
    entity.verbdict
    '''

    def printf(e):
        s = ' '.join([w for w in [l[1][0] for l in e]])
        sys.stdout.write(s+'\n')
        
    def printdict():
        s = ' '.join([entity.posdict[key][0] for key in entity.posdict])
        sys.stdout.write(s)
    
    def printsubtree(root):
        print entity.treeWalk(root, [])

    #print 'test EntityChunker ----------------------------------'
    entity.EntityChunker(entity.ptree.root)
    print "original ------------------"
    map(printf, entity.EntityChunks)
    entity.onlyNP()
    entity.keepMinimal()
    
    printdict()
    print '\nTree walk from root list --------------------'
    map(printsubtree, entity.MinimalEntityRoots)
    print 'Phrase from NPchunks ------------------------'
    map(printf, entity.MinimalEntityChunks)
    print 'NPchunks ------------------------------------'
    print entity.MinimalEntityChunks
    