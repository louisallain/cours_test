# -*- coding: utf-8 -*-
"""
Created on Wed Sep 18 13:27:17 2019

Alg lineaire

@author: e1602246
"""

import numpy as np
import matplotlib.pyplot as plt
from numpy import random
import math

Ma = np.array([[3, 4],
             [6, 8]])
Mb = np.array([[1,2], 
              [2,3]])

# 1.Addition de matrices.  Créez 2 matrices A et B de tailles 3x2.  Faites l’addition des 2
# matrices (vous pourrez utiliser directement A + B avec NumPy)

# Sans numpy
def m_add_matrix(Ma, Mb):
    
    if(np.shape(Ma) != np.shape(Mb)):
        print("Les matrices ne sont pas compatibles pour l'addition.")
        return []
    
    result = np.zeros(shape=np.shape(Ma))
    
    for i in range(len(Ma)): 
        for j in range(len(Ma[0])):
            result[i][j] = Ma[i][j] + Mb[i][j]
               
    return result

print(m_add_matrix(Ma, Mb))

# Avec numpy (A+B)
M = Mb + Ma
print(M)

print("1. ##########################################################################################################")

# 2. Multiplication scalaire. Multipliez tous les éléments d’une matrice par 3 et divisez tous les
# éléments de l’autre matrice par 4.
def m_mulScal_matrix(M, factor):
    
    result = np.zeros(shape=np.shape(M))
    for i in range(len(Ma)):
        for j in range(len(Ma[0])):
            result[i][j] = M[i][j] * factor
    
    return result

print(m_mulScal_matrix(Mb, 3))
print(m_mulScal_matrix(Mb, 0.25))

print("2. ##########################################################################################################")

# 3. Testez la combinaison linéaire de vecteurs : par exemple 2*v -3*w + u/3, u, v, et w étant
# des vecteurs.
u = np.array([0,1,2,3])
v = np.array([1,3,2,4])
w = np.array([3,4])

z = 2*v + u/3
print(z)

print("3. ##########################################################################################################")

# 4.1. Ecrivez l’algorithme qui prend en argument deux matrices et retourne la matrice
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
    
A = np.array([[1, 0],
             [2, -1]])
B = np.array([[3,4], 
              [-2,-1]])
print(m_mul_matrix(A, B))

# 4.2. Utilisez la fonction np.dot(A, B).
print(np.dot(A, B))

print("4. ##########################################################################################################")

# 5. Produit de plusieurs matrices avec np.dot
print(np.dot(np.dot(A,B), A))

print("5. ##########################################################################################################")

# 6. Puissance de matrice
def m_power_matrix(M, power):
    
    i = 0
    result = M
    while(i < power):
        result = m_mul_matrix(result, result)
        i = i + 1
    
    return result

print(m_power_matrix(B, 3))

print("6. ##########################################################################################################")

# 9. Matrice identité
Mx = np.array([[1,2],
               [3,4]])
Mi = np.identity(2)

if(np.allclose(np.dot(Mx, Mi), Mx)):
    print("Ok.")

print("9. ##########################################################################################################")

# 10. Matrice transpose
def m_transpose_matrix(M):
    
    result = np.zeros(shape=(len(M[0]), len(M)))
    
    for i in range(len(M)):
        for j in range(len(M[0])):
            result[j][i] = M[i][j]
    
    return result

Mx = np.array([[1, 3, 5],
               [2, 4, 6]])
print(m_transpose_matrix(Mx))

print("10. ##########################################################################################################")

# 11. Déterminant, trace, rang et inverse d'une matrice
print(A)
print(np.linalg.det(A)) # Déterminant
print(np.trace(A)) # Trace
print(np.linalg.matrix_rank(A)) # Rang
print(np.linalg.inv(A)) # Inverse

b = np.array([1,5])

print("11. ##########################################################################################################")

# 12. Résolution système linéaire si matrice réversible
print(np.linalg.solve(A, b))

print("12. ##########################################################################################################")

# 13. Produit scalaire de deux vecteurs
u = np.array([3,3])
v = np.array([5,4])
print(u)
print(v)
print(np.vdot(u, v))

print("13. ##########################################################################################################")

# 14. Produit vectoriel
print(np.cross(u, v))

print("14. ##########################################################################################################")