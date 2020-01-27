#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Jan 22 15:24:19 2020

@author: e1602246
"""

import time
import numpy as np
import random

# Donne la représentation en une dimension d'une matrrice.
#. Il est aussi possible de la représenter par untableau à une dimension. 
# On ne s’intéresse alors qu’aux éléments de la matrice qui ne sont pasnuls. 
# Chaque case du tableau contient un tuple (i, j ,a) correspondant à l’indice de ligne, 
# l’indicede colonne, et la valeur d’un élément non nul.
def donne_representation_tableau_1dim(M):
    ret = []
    for i in range(len(M)):
        for j in range(len(M[i])):
            if(M[i][j] != 0):
                ret.append([i, j, M[i][j]])
    return ret

# Additionne les éléments d'une matrice qui est sous la représentation "tableau à une dimension"
def somme_elements_matrice_1dim(M_1dim):
    
    acc = 0
    for ligne in M_1dim:
        acc = acc + ligne[2]
    return acc

# Additinne les élements d'une matrice
def somme_elements_matrice(M):
    
    acc = 0
    for i in range(len(M)):
        for j in range(len(M[i])):
            acc = acc + M[i][j]
    return acc

# Algorithme qui prend en argument deux matrices et retourne la matrice
# produit.

def m_mul_matrix(Ma, Mb):
    
    if(np.size(Ma, 1) != np.size(Mb, 0)):
        print("Les matrices ne sont pas compatibles pour le produit.")
        return []
    
    result = np.zeros(shape=(len(Ma), len(Mb[0])))
    
    for i in range(len(Ma)): # nombre de ligne de A
        for j in range(len(Mb[0])): # nombre de colonne de B
            for k in range (len(Ma[0])): # nombre de colonne de A
                result[i][j] += Ma[i][k]*Mb[k][j]
               
    return result  

# Multiplie deux matrices qui sont sous la représentation d'un tableau
# à deux dimensions
def mul_matrice_1dim(Ma, Mb):

    if len(Ma) == len(Mb):
        ret = []
        for i in range(len(Ma)):
            ret.append((Ma[i][0], Ma[i][1],(Mb[i][2] * Mb[i][2])))
        return ret
    else:
        return False
    

# Initialise des matrices (sous chacune des représentation) remplies aléatoirement avec certaines des valeures à 0
A = np.empty((1000, 1000))
for i in range(len(A)):
    for j in range(len(A[i])):
        A[i][j] = random.randint(0, 9)

A_1dim = donne_representation_tableau_1dim(A)
        
# Tests

# Affiche le pourcentage de 0 dans la matrice
print("Pourcentage de 0 dans la matrice : ")
print((1 - len(A_1dim) / (len(A)*len(A))) * 100)


"""
tmp_debut = time.clock()
m_mul_matrix(A, A)
tmp_fin = time.clock()
print("Temps exec matrice normale :")
print(tmp_fin - tmp_debut)
"""

tmp_debut = time.clock()
somme_elements_matrice(A)
tmp_fin = time.clock()
print("Temps exec matrice 1dim :")
print(tmp_fin - tmp_debut)

























