'''
Created on Mar 20, 2012

@author: fifille
'''

'''
This class is to model a template for Which N ... ? questions
eg 1. Which states have the highest percentage of southern baptist ?
eg 2. Which American sport has the highest average player salary ?

Template Main Idea:
    Which (S[]/ (S[]\NP) ) / N
    The N is expecting entity on X axis, this N should be the right child of the parent node of Which
'''

from Temp import *

class TempWhichN(Temp):
    '''
    Taken a parsed tree together with POS tags of a Which-N question
    '''
    def __init__(self, filepath):        
        self.init(filepath)
        self.indieVar = []
        
    def identifySent(self):
        if self.ptree.root.left.func == 'super':
            sent = self.ptree.root.left.param
        elif self.ptree.root.left.func == 'lex':
            sent = self.ptree.root.left.castto
        return sent
            
    
    def getIndieVar(self, regex):
        sent_tag = self.identifySent()
        if sent_tag == None:
            return
        else: 
            print 'sentence question identified as: ', sent_tag
        
        # Case 1: Which as first word of the sentence       
        if re.match(regex, self.posdict[0][0]):
            # walk tree to find 'Which' leaf:
            which_node = self.ptree.root.left
            while which_node.func != 'lf':
                which_node = which_node.left
            if which_node.leaftag == self.posdict[0][2]:
                ulthead = self.getUltHead(which_node.leaftag)
                if sent_tag == ulthead[0]:
                    terms = self.getTerms(which_node.leaftag)
                    if len(terms) < 3:
                        return 
                    if len(terms) == 3:
                        if re.match(r'(^N$)|(^NP$)', terms[2]):
                            if terms[1] == '/':
                                # forward, look for right sibling
                                right_sib = which_node.parent.right
                                from EntityExtract import EntityExtract
                                en = EntityExtract(self.ptree, self.posdict, self.verbdict, self.noundict)
                                forward = []
                                forward = en.treeWalk(right_sib, forward)
                                print forward
                            elif terms[1] == '\\':
                                # backward, look for left sibling
                                left_sib = which_node.parent.left                                
                                en = EntityExtract(self.ptree, self.posdict, self.verbdict, self.noundict)
                                backward = []
                                backward = en.treeWalk(left_sib, backward)
                                print backward
        
        # Case 2: Which has a preposition in front
        
        # Case 3: Which has a sentence in front

if __name__ == '__main__':
    witch = TempWhichN('test/which.txt')
    print witch.identifySent() 
    regex = r'W|which'
    witch.getIndieVar(regex)