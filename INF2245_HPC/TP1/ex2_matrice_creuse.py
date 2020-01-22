#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Jan 22 15:24:19 2020

@author: e1602246
"""

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
            

# Initialise des matrices remplis aléatoirement avec certaines des valeures à 0
A = np.empty((10, 10))
for i in range(len(A)):
    for j in range(len(A[i])):
        A[i][j] = random.randint(0, 10)
        
# Tests
A_1dim = donne_representation_tableau_1dim(A)
print(somme_elements_matrice_1dim(A_1dim))
print(somme_elements_matrice(A))