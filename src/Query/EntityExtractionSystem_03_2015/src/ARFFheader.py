
class Add_ARFFheader():
    def __init__(self, header_choice, attribute_matrix, location):
        self.XY_ARFF_header = "@RELATION independent_axis\n" \
                            "@ATTRIBUTE superlative {0,1}\n" \
                            "@ATTRIBUTE jjrInSent {0,1}\n" \
                            "@ATTRIBUTE trend_word {0,1}\n" \
                            "@ATTRIBUTE comparison_word {0,1}\n" \
                            "@ATTRIBUTE what_which {0,1}\n" \
                            "@ATTRIBUTE how_many_much {0,1}\n" \
                            "@ATTRIBUTE how_do_have {0,1}\n" \
                            "@ATTRIBUTE what_is {0,1}\n" \
                            "@ATTRIBUTE trend_verb {0,1}\n" \
                            "@ATTRIBUTE compare_verb {0,1}\n" \
                            "@ATTRIBUTE cmpvb_IN {0,1}\n" \
                            "@ATTRIBUTE cmpvb_IN_N_N_S {0,1}\n" \
                            "@ATTRIBUTE cmpvb_IN_N_all {0,1}\n" \
                            "@ATTRIBUTE cmpvb_IN_NS {0,1}\n" \
                            "@ATTRIBUTE cmpvb_end {0,1}\n" \
                            "@ATTRIBUTE more_2NP {0,1}\n" \
                            "@ATTRIBUTE NP_conj {0,1}\n" \
                            "@ATTRIBUTE NP_superlative {0,1}\n" \
                            "@ATTRIBUTE NP_gradient {0,1}\n" \
                            "@ATTRIBUTE NP_quantity {0,1}\n" \
                            "@ATTRIBUTE NP_comparison {0,1}\n" \
                            "@ATTRIBUTE NP_trend {0,1}\n" \
                            "@ATTRIBUTE NP_all {0,1}\n" \
                            "@ATTRIBUTE NP_other {0,1}\n" \
                            "@ATTRIBUTE NP_each {0,1}\n" \
                            "@ATTRIBUTE NP_modifiedByQuant  {0, 1}\n" \
                            "@ATTRIBUTE NP_modifiedByTrend {0, 1} \n" \
                            "@ATTRIBUTE NP_modifiedByComp {0, 1} \n" \
                            "@ATTRIBUTE NP_modifiedByGrad{0, 1}\n" \
                            "@ATTRIBUTE what_which_NPdirect {0,1}\n" \
                            "@ATTRIBUTE how_many_much_NPdirect {0,1}\n" \
                            "@ATTRIBUTE how_do_have_NPdirect {0,1}\n" \
                            "@ATTRIBUTE what_is_NPdirect {0,1}\n" \
                            "@ATTRIBUTE cmpvb_leftNP {0,1}\n" \
                            "@ATTRIBUTE cmpvb_rightNP {0,1}\n" \
                            "@ATTRIBUTE chngvb_leftNP {0,1}\n" \
                            "@ATTRIBUTE chngvb_rightNP {0,1}\n" \
                            "@ATTRIBUTE NP_plural {0,1}\n" \
                            "@ATTRIBUTE complexNP {0,1}\n" \
                            "@ATTRIBUTE NP_headnoun {0,1}\n" \
                            "@ATTRIBUTE full_NP {0,1}\n" \
                            "@ATTRIBUTE conjBetween {0,1}\n" \
                            "@ATTRIBUTE ppPreProcede {0,1,2,3}\n" \
                            "@ATTRIBUTE determiner {0,1}\n" \
                            "@ATTRIBUTE lastEntity {0,1,2}\n" \
                            "@ATTRIBUTE containAdj {0,1}\n" \
                            "@ATTRIBUTE time_type {0,1,2}\n" \
                            "@ATTRIBUTE class {X,Y,N}\n"\
                            "@DATA\n"
        
        self.IM_ARFF_header = "@RELATION IM_classification\n"  \
                            "@ATTRIBUTE number_of_X {0,1,2,3}\n"  \
                            "@ATTRIBUTE number_of_Y {0,1,2,3}\n"  \
                            "@ATTRIBUTE singular_Plural_X {0,1,2,3}\n"  \
                            "@ATTRIBUTE superlatives {0,1}\n"  \
                            "@ATTRIBUTE special_words {0,1,2}\n"  \
                            "@ATTRIBUTE np_relation {0,1,2,3}\n"  \
                            "@ATTRIBUTE time {0,1,2}\n"  \
                            "@ATTRIBUTE modified_all_other_each {0,1,2,3}\n"  \
                            "@ATTRIBUTE jjrInSent {0,1}\n"  \
                            "@ATTRIBUTE what_which {0,1}\n"  \
                            "@ATTRIBUTE how_many_much {0,1}\n"  \
                            "@ATTRIBUTE how_do_have {0,1}\n"  \
                            "@ATTRIBUTE what_is {0,1}\n"  \
                            "@ATTRIBUTE class {GetRank, MaximumMinimumMultiple, MaximumMinimumSingle, GeneralMultiple, GeneralSingle, RelativeDifference, RankAll, Trend}\n" \
                            "@data\n"
        
        self.FX_ARFF_header = "@RELATION focused_entity \n" \
                            "@ATTRIBUTE superlative {0,1} \n" \
                            "@ATTRIBUTE jjrInSent {0,1} \n" \
                            "@ATTRIBUTE trend_word {0,1} \n" \
                            "@ATTRIBUTE comparison_word {0,1} \n" \
                            "@ATTRIBUTE what_which {0,1} \n" \
                            "@ATTRIBUTE how_many_much {0,1} \n" \
                            "@ATTRIBUTE how_do_have {0,1} \n" \
                            "@ATTRIBUTE what_is {0,1} \n" \
                            "@ATTRIBUTE trend_verb {0,1} \n" \
                            "@ATTRIBUTE compare_verb {0,1} \n" \
                            "@ATTRIBUTE cmpvb_IN {0,1} \n" \
                            "@ATTRIBUTE cmpvb_IN_N_N_S {0,1} \n" \
                            "@ATTRIBUTE cmpvb_IN_N_all {0,1} \n" \
                            "@ATTRIBUTE cmpvb_IN_NS {0,1} \n" \
                            "@ATTRIBUTE cmpvb_end {0,1} \n" \
                            "@ATTRIBUTE more_2NP {0,1} \n" \
                            "@ATTRIBUTE NP_conj {0,1} \n" \
                            "@ATTRIBUTE NP_superlative {0,1} \n" \
                            "@ATTRIBUTE NP_gradient {0,1} \n" \
                            "@ATTRIBUTE NP_quantity {0,1} \n" \
                            "@ATTRIBUTE NP_comparison {0,1} \n" \
                            "@ATTRIBUTE NP_trend {0,1} \n" \
                            "@ATTRIBUTE NP_all {0,1} \n" \
                            "@ATTRIBUTE NP_other {0,1} \n" \
                            "@ATTRIBUTE NP_each {0,1} \n" \
                            "@ATTRIBUTE NP_modifiedByQuant {0,1} \n" \
                            "@ATTRIBUTE NP_modifiedByTrend {0,1} \n" \
                            "@ATTRIBUTE NP_modifiedByComp {0,1} \n"\
                            "@ATTRIBUTE NP_modifiedByGrad{0, 1}\n" \
                            "@ATTRIBUTE what_which_NPdirect {0,1} \n" \
                            "@ATTRIBUTE how_many_much_NPdirect {0,1} \n" \
                            "@ATTRIBUTE how_do_have_NPdirect {0,1} \n" \
                            "@ATTRIBUTE what_is_NPdirect {0,1} \n" \
                            "@ATTRIBUTE cmpvb_leftNP {0,1} \n" \
                            "@ATTRIBUTE cmpvb_rightNP {0,1} \n" \
                            "@ATTRIBUTE chngvb_leftNP {0,1} \n" \
                            "@ATTRIBUTE chngvb_rightNP {0,1} \n" \
                            "@ATTRIBUTE NP_plural {0,1} \n" \
                            "@ATTRIBUTE complexNP {0,1} \n" \
                            "@ATTRIBUTE NP_headnoun {0,1} \n" \
                            "@ATTRIBUTE full_NP {0,1} \n" \
                            "@ATTRIBUTE conjBetween {0,1} \n" \
                            "@ATTRIBUTE ppPreProcede {0,1,2,3} \n" \
                            "@ATTRIBUTE determiner {0,1} \n" \
                            "@ATTRIBUTE lastEntity {0,1,2} \n" \
                            "@ATTRIBUTE containAdj {0,1} \n" \
                            "@ATTRIBUTE time_type {0,1,2} \n" \
                            "@ATTRIBUTE axes_result {X,Y,N} \n" \
                            "@ATTRIBUTE intended_message {Get-Rank,Maximum-Minimum-Multiple,General-Multiple,General-Single,Maximum-Minimum-Single,Relative-Difference,Rank-All,Trend} \n" \
                            "@ATTRIBUTE class {F,NF} \n" \
                            "@DATA \n" 
        
        self.arfffile = self.__addHeader(header_choice, attribute_matrix)
        self.__writeARFF(location)
    
    def __addHeader(self, header_choice, attribute_matrix):
        attribute_lines = [",".join([str(attr) for attr in attribute]) + ",?"  for attribute in attribute_matrix]
        header = self.switch(header_choice)
        return header + "\n".join(attribute_lines)
    
    def __writeARFF(self, location):
        writehandler = open(location, 'w')
        writehandler.write(self.arfffile)
        writehandler.close()
  
    def switch(self, header_choice):
        try:
            return {
                "XY": self.XY_ARFF_header,
                "IM": self.IM_ARFF_header,
                "FX": self.FX_ARFF_header,
            }[header_choice]
        except KeyError:
            return self.XY_ARFF_header
if __name__ == '__main__':
    add_arffheader = Add_ARFFheader("IM", 
                            [[0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0], 
                             [0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 0, 2, 0, 0, 0, 0], 
                             [0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 0, 2, 0, 2, 0, 0]
                             ], "")
    
    print add_arffheader.arfffile
    
    
    