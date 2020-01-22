#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Jan 22 14:10:57 2020

@author: e1602246
"""

import time
import random 

# Recherche de manière séquentiel un élément dans un tableau
# Donne l'indice de l'élément recherché ou False s'il n'existe pas
def recherche_seq(tab, elemRech):
    
    i = 0
    for elem in tab:
        if(elem == elemRech):
            return i
        i = i + 1
    return False

# Recherche de manière dichotomique un élément dans un tableau trié
# Donne l'indice de l'élément recherché ou False s'il n'existe pas
def recherche_dich(tab, elemRech):
    m = len(tab) // 2
    if(elemRech != tab[m] and len(tab) == 1):
        return False
    if(elemRech == tab[m]): 
        print("oui")
        return m
    if(tab[m] > elemRech):
        return recherche_dich(tab[0:m], elemRech)
    else:
        return recherche_dich(tab[m:len(tab)], elemRech)

# Tests
        
tab_ordonne = []
tab_desordonne = []
for i in range(0, 1000000):
    tab_ordonne.append(i)
    tab_desordonne.append(i)
    
random.shuffle(tab_desordonne)

# Sequentielle sur tableau ordonne
tmp_debut = time.clock()
print(recherche_seq(tab_ordonne, 900000))
tmp_fin = time.clock()
print("Temps exec :")
print(tmp_fin - tmp_debut)
      
# Sequentielle sur tableau desordonne
tmp_debut = time.clock()
print(recherche_seq(tab_desordonne, 1))
tmp_fin = time.clock() 
print("Temps exec :")
print(tmp_fin - tmp_debut)

# Dichotomique
tmp_debut = time.clock()
print(recherche_dich(tab_ordonne, 900000))
tmp_fin = time.clock() 
print("Temps exec :")
print(tmp_fin - tmp_debut)