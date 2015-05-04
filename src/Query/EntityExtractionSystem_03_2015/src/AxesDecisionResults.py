'''
Created on Jan 14th, 2015
By Zhuo (Jewel) Li
'''

import re, os.path

class AxesDecisionResults():
    
    def __init__(self, axes_decision_tree_result):
        self.rangeList, self.resultList, self.annotationList = [],[],[]
        self.initAxesResult(axes_decision_tree_result)
        
    def initAxesResult(self, axes_decision_tree_result):
        if os.path.isfile(axes_decision_tree_result): 
            axes_result_file = open(axes_decision_tree_result)            
            result_list, annotation_list = [], []
            lo = 0 
            for line in axes_result_file.readlines():
                if re.match("Query", line):
                    en_num = int( re.findall("[0-9]+", line)[-1] )
                    self.rangeList.append( (lo, lo + en_num -1) )
                    lo = lo + en_num
                    if len(result_list) > 0: self.resultList.append(result_list)
                    if len(annotation_list) > 0: self.annotationList.append(annotation_list)
                    result_list, annotation_list = [], []
                elif re.match("Instance", line):
                    result_list.append( re.findall("X|Y|N", line)[0].lower())
                    annotation_list.append( re.findall("X|Y|N", line)[-1].lower() )
            self.resultList.append(result_list)
            self.annotationList.append(annotation_list)
        else:
            XYresult = axes_decision_tree_result.split(";")
            self.rangeList.append((0, len(XYresult)-1))
            self.resultList.append(XYresult)

if __name__ == "__main__":
    
    axes_result_path = "New_Axis_Classification_Results.txt"
    AxesDecisionResults(axes_result_path)