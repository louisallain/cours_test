# -*- coding: utf-8 -*-
"""
Created on Mon Sep 23 20:17:24 2019

@author: Allain Louis
"""

import numpy as np
import matplotlib.pyplot as plt
import math
import cmath

# 1. Le signal "impulsion unitaire"
# 1.1. Fonction créant le signal
def genSigImpulUnit(N, k):
    
    ret = np.zeros(shape=N)
    ret[k] = 1
    return ret

# 1.2 Affichage d'un exemple de signal "impulsion unitaire"
"""
fig = genSigImpulUnit(20, 5)
plt.figure(1)
plt.stem(fig)
"""

# 2. Le signal escalier
# 2.1. Fonction créant le signal
def genSigEscalier(N, i, s):
    
    ret = np.zeros(shape=N)
    for j in range(i, (i+s)):
        ret[j] = 1
    return ret

# 2.2 Affichage d'un exemple de signal escalier
"""
fig = genSigEscalier(20, 5, 7)
plt.figure(1)
plt.stem(fig)
"""

# 3. Le signal rampe
# 3.1. Fonction créant le signal
def genSigRampe(N, i, r):
    
    ret = np.zeros(shape=N)
    cpt = 0
    
    ret[0] = 0
    for j in range(i, (i+r)):
        cpt = cpt + 1
        ret[j+1] = cpt/r
    
    """
    # pas complet ?
    # décroissant
    for j in range(i, (i+r)):
        ret[j] = 1 - (cpt/r)
        cpt = cpt + 1
    """
    return ret

# 3.2 Affichage d'un exemple de signal rampe
"""
fig = genSigRampe(20, 5, 5)
plt.figure(1)
plt.stem(fig)
"""

# 4. Génération d'un signal par combinaison linéaire

fig1 = genSigRampe(20, 5, 5)
fig2 = genSigImpulUnit(20, 11)
fig3 = genSigImpulUnit(20, 12)
fig4 = genSigImpulUnit(20, 13)
fig5 = genSigImpulUnit(20, 14)
fig = (4/5*fig2)+(3/5*fig3)+(2/5*fig4)+(1/5*fig5)+fig1
fig = fig*5
"""
plt.figure(5)
plt.stem(r)
"""


# 5. Le signal sinusoïdale
# 5.1 Fonction créant le signal
# Retourne le vecteur temporel et la sin ...
def genSin(N, fo, fs):
    
    t = np.linspace(0, (N-1)/fs, N)
    s = np.sin(2*math.pi*fo*t)
    return t,s

(t,s) = genSin(512, 5, 500)
print(s)
"""
plt.plot(t, s)
"""

# 6. Le signal exponentiel complexe
# 6.1 Foncion créant le signal
def expSig(N, a, c) : 
    
    ret = np.zeros(N)
    
    # cas exponentiel complexe
    if(c == 1):
        
        (r, arg) = cmath.polar(a)
        
        for i in range(len(ret)):
            ret[i] = math.pow(r, i) * math.exp(arg*i)
    
    #cas normal
    else:
        
        for i in range(len(ret)):
            ret[i] = math.pow(a, i)
            
    return ret

"""
plt.stem(expSig(20, -0.95))
"""
plt.stem(expSig(20, cmath.rect(0.95, math.pi/10), 1))