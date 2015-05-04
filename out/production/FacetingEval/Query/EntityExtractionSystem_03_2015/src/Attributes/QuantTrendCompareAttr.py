'''
Created on May 7, 2012

@author: fifille
'''
from Attribute import *

class QuantTrendCompareAttr(Attribute):
    
    def init(self):
        self.quant, self.trend, self.compare, self.gradient = \
            re.compile('(the )?(number)|(quantity)|(amount)|(percentage)|(rate)|(portion)|(proportion)|(percent)|(couple)|'
                       '(few)'), \
            re.compile('(the )?(trend)|(change)|(vary)|(increment)|(decrement)|(slop)|(shrink)|'
                       '(climb)|(rise)|(fall)|(increase)|(decrease)|(fluctuation)|(fluctuate)|(gain)|'
                       '(growth)|(grow)|(win)|(lose)|(loss)|(plus)|(minus)|(up)|(down)|(upping)|(growth)|'
                       '(acquire)|(boost)|(score)|(fail)|(expand)|(concentrate)|(accumulate)|(accelerate)|'
                       '(add)|(subtract)|(addition)|(subtraction)|(ascent)|(inflate)|(vary)|(inflation)|(surge)|(upgrade)|'
                       '(swell)|(declin[e|ing])'), \
            re.compile('(the )?(compare)|(compared)|(comparing)|(oppose)|(opposed)|(comparison)|(differ)|'
                       '(difference)|(variation)|(contrary)|(against)'), \
            re.compile('(in terms of)|(according to)|(regarding)|(the subject of)|(with respect to)|(in relation to)'
                       '|(as regards)|(concerning)|(with regards)|(regard)')
        
        self.each, self.all, self.other = re.compile('(each)|(every)|(various)'), re.compile('(all)|(total)|(overall)'), \
                                            re.compile('(other)|(rest)')
    
        '''
         get synonyms from NLTK
        self.quant = {'number', 'quantity', 'amount', 'percentage', 'rate', 'portion', 'proportion', 'percent'}
        self.trend = {'trend', 'change', 'vary', 'variation', 'increment', 'decrement', 'increase', 'decrease', 'slop',\
                      'climb', 'fall', 'rise', 'fluctuation', 'fluctuate', 'gain', 'lose', 'win', 'loss', 'grow', 'growth',\
                      'plus', 'minus', 'add', 'subtract', 'addition', 'subtraction', 'up', 'down', 'upping', 'acquire', \
                      'boost', 'score', 'fail', 'expand', 'shrink', 'accumulate', 'accelerate', 'ascent', 'inflate', \
                      'inflation', 'surge', 'surgeon', 'upgrade', 'downgrade', 'swell', 'soar', 'rocket', 'rouse', \
                      'arise', 'arose', 'aspire', 'steep', 'push', 'reach up', 'shine', 'advance', 'intensify', 'multiply'}
        '''
        
        
    def __quantTrendCompareRule(self, p, leaves):
        # given a root of NP node, if the NP is having "the number of", "the amount of", 
        # "the percentage of", "the portion of", "the rate of"...
        # return True
        phrase = ' '.join([w for w in [l[1][0] for l in leaves]])
        #print phrase
        if p.search(phrase): return 1
        else:   return 0
    
    def modifiedBy(self, p, leaves):
        np = ' '.join([l[1][0] for l in leaves])
        lookback = ""
        if leaves[0][0] > 2:
            for index in range(leaves[0][0]-3, leaves[0][0]):
                lookback += " " + self.posdict[index][0]
            if re.search(p, lookback) or re.search(p, np):
                return 1
            else:   return 0
        else: return 0
    
    def modifyInEntity(self, p, modifyList):
        for e in range(len(self.EntityChunks)):
            if self.modifiedBy(p, self.EntityChunks[e]) == 1: modifyList[e] = 1
            else: modifyList[e] = 0
        return modifyList 
        
    def qtcInEntity(self, p, qtclist):
        for e in range(len(self.EntityChunks)):
            if self.__quantTrendCompareRule(p, self.EntityChunks[e]) == 1: qtclist[e] = 1
            else:   qtclist[e] = 0
        return qtclist
            
    def qtcInSent(self, p):
        sent = [[pos, self.posdict[pos]] for pos in self.posdict]
        if self.__quantTrendCompareRule(p, sent) == 1:  return 1
        else:    return 0
        
        
if __name__ == '__main__':
    
    # ------------------------------------------------------------------------------------------------------------------------
    from TreeStructure.ReadTree import *
    filepath = '../../../../test.txt'      # whatis2.txt
    parsetree = open(filepath, "r").readlines()
    readfile = ReadTree(parsetree)

    from EntityExtract import EntityExtract
    entity = EntityExtract(readfile.ptree, readfile.posdict, readfile.verbdict, readfile.noundict)
    entity.EntityChunker(entity.ptree.root)
    entity.onlyNP()
    # ------------------------------------------------------------------------------------------------------------------------

    qtc = QuantTrendCompareAttr(entity.ptree, entity.posdict, entity.EntityChunks, entity.EntityRoots)
    qtclist = [[] for i in range(len(qtc.EntityChunks))]
    print qtc.qtcInEntity(qtc.quant, qtclist)
    qtclist = [[] for i in range(len(qtc.EntityChunks))]
    print qtc.qtcInEntity(qtc.trend, qtclist)
    qtclist = [[] for i in range(len(qtc.EntityChunks))]
    print qtc.qtcInEntity(qtc.compare, qtclist)
    
    qtc.qtcInSent(qtc.quant)
    qtc.qtcInSent(qtc.trend)
    qtc.qtcInSent(qtc.compare)
    
    modifiedList = [[] for i in range(len(qtc.EntityChunks))]
    print qtc.modifyInEntity(qtc.quant, modifiedList)
    
    modifiedList = [[] for i in range(len(qtc.EntityChunks))]
    print qtc.modifyInEntity(qtc.trend, modifiedList)
    
    modifiedList = [[] for i in range(len(qtc.EntityChunks))]
    print qtc.modifyInEntity(qtc.compare, modifiedList)
    
    modifiedList = [[] for i in range(len(qtc.EntityChunks))]
    print qtc.modifyInEntity(qtc.gradient, modifiedList)
    
    modifiedList = [[] for i in range(len(qtc.EntityChunks))]
    print qtc.modifyInEntity(qtc.each, modifiedList)
    