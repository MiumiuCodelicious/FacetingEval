//  Copyright 2013 Google Inc. All Rights Reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

#include <stdio.h>
#include <string.h>
#include <math.h>
#include <stdlib.h>
//#include <malloc.h>
#define MAX_STRING 200

const long long max_size = 2000;         // max length of strings
const long long N = 40;                  // number of closest words that will be shown
const long long max_w = 50;              // max length of vocabulary entries

// ----- Moved from main() to here to be accessible for Kmeans() ------
long long words, size; 	// words is the total size of words, size is the dimension of a single word vector
float *M;
char *vocab;
char word_cluster_file[MAX_STRING];
// ---------------------------------------------------------------------

// ------ Added so that KMeans() function -----------
typedef float real;                    // Precision of float numbers
// ---------------------------------------------------------------------


void Kmeans(int classes){
    long a, b, c, d;
    
    
    // Run K-means on the word vectors
    int clcn = classes, iter = 10, closeid;
    int *centcn = (int *)malloc(classes * sizeof(int)); //  the count of words in a class
    int *cl = (int *)calloc(words, sizeof(int));
    real closev, x;
    real *cent = (real *)calloc(classes * size, sizeof(real));
    for (a = 0; a < words; a++) cl[a] = a % clcn;
    for (a = 0; a < iter; a++) {
        /* Assignment Step
         */
        printf("K-means Iteration %d\n", a);
        
        for (b = 0; b < clcn * size; b++) cent[b] = 0;
        for (b = 0; b < clcn; b++) centcn[b] = 1; // initialize suppose there is at least 1 word to each class
        
        for (c = 0; c < words; c++) {
            for (d = 0; d < size; d++) {
                cent[size * cl[c] + d] += M[c * size + d]; // add all word's dimension that were assigned to that class!
                //			printf("content of cent = %.6f\n", cent[layer1_size * cl[c] + d];
            }
            centcn[cl[c]]++; // the count of words in a class?
        }
        
        for (b = 0; b < clcn; b++) { // for each class
            closev = 0;
            for (c = 0; c < size; c++) { // for each dimension in the word vector of the centroid
                cent[size * b + c] /= centcn[b]; // normalize the added-up dimension of words in a class, by the number of words in that class
                closev += cent[size * b + c] * cent[size * b + c]; // and then get that "ideal" centroid's ||mode||
            }
            closev = sqrt(closev);
            for (c = 0; c < size; c++) cent[size * b + c] /= closev; // normalize the vector of "ideal" centroid to unit vector
        }
        
        /* Calculate distance to the assigned centroid
         */
        for (c = 0; c < words; c++) { // for each word
            closev = -10;
            closeid = 0;
            for (d = 0; d < clcn; d++) { // for each class
                x = 0;
                // Updating to find a closest class centroid
                for (b = 0; b < size; b++) x += cent[size * d + b] * M[c * size + b];
                if (x > closev) {
                    closev = x;
                    closeid = d;
                }
            }
            cl[c] = closeid; // instead of the originally assigned class ID, change it to a new closest class ID
        }
    }
    // Save the K-means classes
    FILE *fo;
    fo = fopen(word_cluster_file, "wb");
    for (a = 0; a < words; a++) {fprintf(fo, "%s %d\n", &vocab[a * max_w], cl[a]); printf("%s %d\n", &vocab[a * max_w], cl[a]);}
    free(centcn);
    free(cent);
    free(cl);
    fclose(fo);
}

int main(int argc, char **argv) {
    FILE *f;
    char *bestw[N];
	char file_name[max_size], phrases_file[max_size];
    float dist, len, bestd[N];
    long long a, b, c, d;
    char ch;
    
    if (argc < 2) {
        printf("Usage: ./distance <FILE>\nwhere FILE contains word projections in the BINARY FORMAT\n");
        return 0;
    }else if(argc == 2){
        printf("No relevance will be measured. \n");
    }else if (argc == 3){
//        strcpy(word_cluster_file, argv[2]);
		strcpy(phrases_file, argv[2]);
		printf("Facet value file %s.\n", phrases_file);

    }
    
	/* read in vectors.bin file */
	strcpy(file_name, argv[1]);
	
    f = fopen(file_name, "rb");
    if (f == NULL) {
        printf("Input file not found\n");
        return -1;
    }
    fscanf(f, "%lld", &words);
    fscanf(f, "%lld", &size);
    printf("Read in words %lld\n", words);
    printf("Read in size %lld\n", size);
    
    vocab = (char *)malloc((long long)words * max_w * sizeof(char));
    for (a = 0; a < N; a++) bestw[a] = (char *)malloc(max_size * sizeof(char));
    M = (float *)malloc((long long)words * (long long)size * sizeof(float));
    if (M == NULL) {
        printf("Cannot allocate memory: %lld MB    %lld  %lld\n", (long long)words * size * sizeof(float) / 1048576, words, size);
        return -1;
    }
    for (b = 0; b < words; b++) {
        fscanf(f, "%s%c", &vocab[b * max_w], &ch);
        for (a = 0; a < size; a++) fread(&M[a + b * size], sizeof(float), 1, f);
        len = 0;
        for (a = 0; a < size; a++) len += M[a + b * size] * M[a + b * size];
        len = sqrt(len);
        for (a = 0; a < size; a++) M[a + b * size] /= len;
    }
    fclose(f);
    
    // K-means
//    if (argc == 3){
//        int classes = 5000;
//        Kmeans(classes);
//        return(0);
//    }

    // measure distance between phrase1 and phrase2
	// looping for user input
//    while (1) {
//        for (a = 0; a < N; a++) bestd[a] = 0;
//        for (a = 0; a < N; a++) bestw[a][0] = 0;
//        printf("Enter two words or phrases, seperated with no space but a | (EXIT to break): ");
//        a = 0;
//        while (1) {
//            st1[a] = fgetc(stdin);
//            if ((st1[a] == '\n') || (a >= max_size - 1)) {
//                st1[a] = 0;
//                break;
//            }
//            a++;
//        }
//        if (!strcmp(st1, "EXIT")) break;

		// read in user input phrases first as the 3rd argument
		
		/* Read in facet value file*/
		FILE* facetfile;
		facetfile = fopen(phrases_file, "r");
		if(facetfile == NULL){
			printf("No facet value input file\n");
			return -1;
		}
		
		/* loop through file of queryfield-facetvalue pairs */
		char line[256];
		while (fgets(line, sizeof(line), facetfile)) {
//			printf("%s", line);

			char p1[100][max_size], p2[100][max_size];
			long cn1, cn2, bi1[100], bi2[100];
			float vec1[max_size], vec2[max_size];
			
			cn1 = 0; cn2 = 0;
			b = 0;
			c = 0;
			int switch_word_flag = 0;
						
			int i;
			for(i = 0; line[ i ]; i++){

	//        while (1) {
				// add Jewel's code here: I just want to compare two phrases, P1 and P2
				if (line[c] == '|'){
					switch_word_flag = 1;
					cn2 = 0;
					b = 0;
					c++;
					continue;
					printf("this should never show!");
				}
				if (switch_word_flag == 0) {
					p1[cn1][b] = line[c];
					b++;
					p1[cn1][b] = 0;
					c++;
				}else if (switch_word_flag == 1){
					p2[cn2][b] = line[c];
					b++;
					p2[cn2][b] = 0;
					c++;
				}
				if (line[c] == 0 || line[c] == '\n') break;
				if (line[c] == '_') {
					b = 0;
					c++;
					if (switch_word_flag == 0) cn1++;
					if (switch_word_flag == 1) cn2++;
				}
				
			}
			cn1++; cn2++;
			
			for (a = 0; a < cn1; a++) {
				for (b = 0; b < words; b++) {if (!strcmp(&vocab[b * max_w], p1[a])) {break;} }
				if (b == words) b = -1;
				bi1[a] = b;
//				printf("\nWord %lld in Query Phrase: %s  Position in vocabulary: %ld\n", a, p1[a], bi1[a]);
//				if (b == -1) {
//					printf("Out of dictionary word!\n");
					/* *
					 *  Don't break on unknown words. Just ignore this word.
					 * */
//				}
			}
			
			for (a = 0; a < cn2; a++) {
				for (b = 0; b < words; b++){ if (!strcmp(&vocab[b * max_w], p2[a])) { break;} }
				if (b == words) b = -1;
				bi2[a] = b;
//				printf("\nWord %ld in Facet Value: %s  Position in vocabulary: %ld\n", cn2, p2[a], bi2[a]);
//				if (b == -1) {
//					printf("Out of dictionary word!\n");
//					
//				}
			}
			
//			printf("Calculate Cosine Distance\n");
			
			if (b == -1) {
				printf("distance(");
				for (a = 0; a < cn1; a++) printf("%s ", p1[a]);
				printf(",");
				for (a=0; a<cn2; a++) printf(" %s ", p2[a]);
				printf(") = %f\n" , 0.0);
				continue;	
			}
			
//			printf("\n                                              Word       Cosine distance\n------------------------------------------------------------------------\n");
			for (a = 0; a < size; a++) {vec1[a] = 0; vec2[a] = 0;}
			// simply add each word of p1 together.
			for (b = 0; b < cn1; b++) {
				if (bi1[b] == -1) continue;
				for (a = 0; a < size; a++) vec1[a] += M[a + bi1[b] * size];
			}
			len = 0;
			for (a = 0; a < size; a++) len += vec1[a] * vec1[a];
			len = sqrt(len);
			for (a = 0; a < size; a++) {vec1[a] /= len;} // vec1[a] /= cn1; }//printf("%f,", vec1[a]);
//			printf("\n\n");
			
			// simply add each word of p2 together.
			for (b = 0; b < cn2; b++) {
				if (bi2[b] == -1) continue;
				for (a = 0; a < size; a++) vec2[a] += M[a + bi2[b] * size];
			}
			len = 0;
			for (a = 0; a < size; a++) len += vec2[a] * vec2[a];
			len = sqrt(len);
			for (a = 0; a < size; a++) {vec2[a] /= len; }// vec2[a] /= cn2;} //printf("%f,", vec2[a]);
//			printf("\n\n");
			
			dist = 0;
			for (a = 0; a < size; a++) dist += vec1[a] * vec2[a];
			printf("distance(");
			for (a = 0; a < cn1; a++) printf("%s ", p1[a]);
			printf(",");
			for (a=0; a<cn2; a++) printf(" %s ", p2[a]);
			printf(") = %f\n" , dist);
			
			//    for (a = 0; a < N; a++) bestd[a] = -1;
			//    for (a = 0; a < N; a++) bestw[a][0] = 0;
			//    for (c = 0; c < words; c++) {
			//      a = 0;
			//      for (b = 0; b < cn1; b++) if (bi[b] == c) a = 1;
			//      if (a == 1) continue;
			//      dist = 0;
			//      for (a = 0; a < size; a++) dist += vec[a] * M[a + c * size];
			//      for (a = 0; a < N; a++) {
			//        if (dist > bestd[a]) {
			//          for (d = N - 1; d > a; d--) {
			//            bestd[d] = bestd[d - 1];
			//            strcpy(bestw[d], bestw[d - 1]);
			//          }
			//          bestd[a] = dist;
			//          strcpy(bestw[a], &vocab[c * max_w]);
			//          break;
			//        }
			//      }
			//    }
			//    for (a = 0; a < N; a++) printf("%50s\t\t%f\n", bestw[a], bestd[a]);
	}
    return 0;
}
