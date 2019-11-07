# -*- coding: utf-8 -*-
"""
Created on Tue Sep 17 08:51:04 2019

Crée un vecteur contenant des entiers et permet de
visualiser ces données.

@author: e1602246
"""

import numpy as np
import matplotlib.pyplot as plt

x = np.array([0,1,2,3])
v = np.array([1,3,2,4])

print(v)
print(type(v))
fig = plt.figure()
plt.plot(x, v, 'rv-', label = 'v(x)')
plt.legend(loc='lower right')
plt.xlabel('x')
plt.ylabel('v')
plt.title('C bô')
plt.xlim([-1,4])
plt.ylim([0,5])
plt.show()
fig.savefig('toto.png')