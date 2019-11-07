# -*- coding: utf-8 -*-
"""
Created on Tue Sep 17 09:02:27 2019

Exos de manipulation de matrices

@author: e1602246
"""

import numpy as np
import matplotlib.pyplot as plt
from numpy import random
import math

# création d'un vecteur et d'une matrice 2*2
v = np.array([0, 1, 2, 3, 4])
M = np.array([[3, 4],
             [6, 8]])
print(M)
print(M[0,1])

# M[0,0] = "hello" # erreur de type

# resultat [3 2 3]
a=np.array([1,2,3])
a[0] = 3.2
print(a)
a.dtype

# resultat [3.2 2.  3. ]
a=np.array([1,2,3], dtype=float)
a[0] = 3.2
print(a)
a.dtype

# affichage types et dimensions
print(type(v))
print(type(M))

print(v.shape)
print(M.shape)

# affichage de la matrice
fig = plt.figure()
plt.plot(M)
plt.show()

# tableau contenant les valeurs de 0 à 9
# avec np.arrange
a = np.array([np.arange(0, 10)])
print(a)
a = np.array([np.arange(10)])
print(a)

# avec np.linspace
a = np.array([np.linspace(0, 9, 10)])
print(a)
a = np.array([np.linspace(0, 9)])
print(a)
a = np.array([np.linspace(0, 9, 100)])
print(a)

# avec np.logspace
a = np.array([np.logspace(0, 9, 10)])
print(a)

# matrice M 3*3 aléatoirement
# par tirage uniforme
M = np.random.random((3,3))
print(M)

# par loi normale
M = np.random.randn(3,3)
print(M)

# affichage de l'historique des tirages
a = random.randn(10000)
# print(plt.hist(a,40))

# matrice contenant des 0 ou des 1
M = np.random.randint(2, size=(3,3))
print(M)

# matrice avec 1,2,3 sur la diagonale
v = np.array([1,2,3])
print(np.diag(v))

# matrice 3*5 contenant valeurs infinies
M = np.nan * np.ones(shape=(3,2))
print(M)

# matrice utilisant des fonctions
def initfunction(i,j):
    return 100 + 10*i + j
print('10.')
M = np.random.random((3,3))
print(M)
M = np.fromfunction(initfunction, (3,5))
print(M)

# création matrices à partir de fichiers
print('11.')
Ma = np.ones(shape=(3,5,7))
print(Ma)
np.save('Ma.npy', Ma)

# chargement matrices à partir de fichiers
print('12.')
Mb = np.load('Ma.npy')
print(Mb)