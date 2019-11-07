# -*- coding: utf-8 -*-
"""
Created on Tue Oct 22 14:07:12 2019

@author: Louis A
"""

from skimage import io
import matplotlib.pyplot as plt
import numpy as np
from scipy import signal
import cmath

#########
## TP6 ##
#########

# Q1.1.1. Acquérir l'image
chimp_img = io.imread('../res/chimpanze.jpg')
print("Forme : ", chimp_img.shape)

# Q1.1.2. Afficher l'image
plt.figure(1)
plt.title("Image réelle")
io.imshow(chimp_img) 

# Q1.1.3. Enregistrer une nouvelle image qui est un zoom de l'originale
chimp_img_zoom = chimp_img[:100, :100]
io.imsave("../res/chimpanze_zomm.jpg", chimp_img_zoom)

# Q1.1.4. Appliquer un masque sur l'image
# TODO

# Q1.2.1 Acquérir l'image
chimp_img_2 = chimp_img

# Q1.2.2 Modifier la résolution
# Q1.2.3 Sous échantillonner l'image
# Q1.2.4 Fonction echantillonnage
def echantillonnage(img, factor):
    
    ret = np.zeros(shape=(img.shape[0]//factor, img.shape[1]//factor))
    for i in range(0, img.shape[0], factor):
        
        for j in range(0, img.shape[1], factor):
            ret[i//factor][j//factor] = img[i][j]
    return ret

plt.figure(2)
plt.title("Image 128x128")
io.imshow(echantillonnage(chimp_img_2, 2), cmap='gray')
plt.figure(3)
plt.title("Image 64x64")
io.imshow(echantillonnage(chimp_img_2, 4), cmap='gray')
plt.figure(4)
plt.title("Image 32x32")
io.imshow(echantillonnage(chimp_img_2, 8), cmap='gray')
plt.figure(5)
plt.title("Image 16x16")
io.imshow(echantillonnage(chimp_img_2, 16), cmap='gray')

# Q1.3.1 Fonction histogramme
# Q1.3.2 Historgramme normalisé 
def histogramme(img, norme=1):
    
    ret = np.zeros(256)
    for i in range(0, img.shape[0]):
        for j in range(0, img.shape[1]):
            
            ret[img[i][j]] += 1
            
    # Normalise si besoin
    if(norme > 1):
        for i in range(0, ret.shape[0]):
            ret[i] = ret[i]/norme
            
    return ret
   
# Q1.3.3 Histogramme avec différentes options de normalisation         
plt.figure(6)
plt.title("Histogramme cumulé")
plt.plot(histogramme(chimp_img_2))
plt.figure(7)
plt.title("Histogramme normalisé en 1")
plt.plot(histogramme(chimp_img_2, (256*256))) # représente donc les probas

# Q1.3.4 Changement de contraste
chimp_img_3 = chimp_img_2
h = histogramme(chimp_img_3, (256*256))
for i in range(0, chimp_img_3.shape[0]):
        for j in range(0, chimp_img_3.shape[1]):
            chimp_img_3[i][j] = chimp_img_3[i][j] * h[chimp_img_3[i][j]]

plt.figure(8)   
plt.title("Changement de contraste grâce à l'histogramme normalisé")        
io.imshow(chimp_img_3, cmap='gray')

# Q1.4.1 Appliquer une matrice filtre
# Q1.4.3 Fonction convolution
def convolution(img, Mc):
    
    xMc = int((Mc.shape[1]-1)/2)
    yMc = int((Mc.shape[0]-1)/2)
    imgCp = img
    m = int(img.shape[1] - xMc)
    
    for i in range(xMc, m):
        for j in range(yMc, img.shape[0]-yMc):
            acc = 0.0
            for k in range(-xMc, xMc+1):
                for l in range(-yMc, yMc+1):
                    acc = acc + img[j+l][i+k]*Mc[l+yMc][k+xMc]
            imgCp[j][i] = acc
    return imgCp

# Q1.4.2 Filtre gaussien appliquer à l'image
chimp = io.imread('../res/chimpanze.jpg')

# filtre gaussien (0, 0.625)
plt.figure(9)
plt.title("Filtre contour")
Mc_gauss = np.array([
            [-1, -1, -1],
            [-1, 8, -1],
            [-1, -1, -1]])
Mc_gauss = Mc_gauss
io.imshow(signal.convolve2d(chimp, Mc_gauss))

# Q1.5.1 Image en nuance de gris 256x256
fleur = io.imread('../res/fleur.jpg')      

# Q1.5.2 Filtre moyenneur
plt.figure(11)
plt.title("Filtre moyenneur")
Mc_moy = np.ones((3,3))*1.0/9
io.imshow(convolution(fleur, Mc_moy))

# Q1.5.3 Résultat du filtre précédent
# Le filtre précédent floute l'image, il agit comme un filtre passe bas

#########
## TP7 ##
#########

"""
Fonction reprise depuis https://kmdb.pagesperso-orange.fr/_src/_python/_formation_2010/python_formation_images.html
"""
def norma(mat):
    mat1 = mat.real
    mat1 -= mat1.min()
    mat1 *= 255. / mat1.max()
    return mat1

def normalog(mat):
    mat1 = norma(mat)
    mat1 = np.log(1 + mat1)
    mat1 *= 255. / mat1.max()
    return mat1

# Q1.1. Amplitude et phase grâce à fft2
chimp = io.imread('../res/chimpanze.jpg')
fleur_TFD = np.fft.fft2(chimp)
fleur_TFD_amp = np.abs(fleur_TFD)
fleur_TFD_ph = fleur_TFD / (np.abs(fleur_TFD))

plt.figure(12)
plt.title("Amplitude")
io.imshow(np.fft.fftshift(normalog(fleur_TFD_amp)),cmap='gray')
plt.figure(13)
plt.title("Phase")
io.imshow(np.fft.fftshift(norma(fleur_TFD_ph)), cmap='gray')

# Q1.2. Fonction ifft2 pour retrouver l'image de départ
fleur_rec_amp = np.fft.ifft2(fleur_TFD_amp)
fleur_rec_ph = np.fft.ifft2(fleur_TFD_ph)
fleur_rec = np.fft.ifft2(fleur_TFD_amp*fleur_TFD_ph)

plt.figure(14)
plt.title("ifft Amplitude")
io.imshow(normalog(fleur_rec_amp), cmap='gray')
plt.figure(15)
plt.title("ifft Phase")
io.imshow(norma(fleur_rec_ph), cmap='gray')

# Q2.1.1 Acquérir l'image 
chimp = io.imread('../res/chimpanze.jpg')

# Q2.1.2 Filtre passe bas par convolution
plt.figure(16)
plt.title("Filtre passe bas par convolution")
Mpb = np.array([
            [1, 3, 1],
            [3, 5, 3],
            [1, 3, 1]])
io.imshow(signal.convolve2d(chimp, Mpb), cmap='gray')

# Q2.1.3 Résultat du filtre précédent
# Il semble que ça brouille l'image

# Q2.1.4 Filtre passe haut par convolution
plt.figure(17)
plt.title("Filtre passe haut par convolution")
Mpb = np.array([
            [0, -1, 0],
            [-1, 5, -1],
            [0, -1, 0]])
io.imshow(signal.convolve2d(chimp, Mpb), cmap='gray')

# Q2.2.1 Transformée de Fourier de l'image
chimp = io.imread('../res/chimpanze.jpg')
np.fft.ifft2(chimp)


# Q2.2.2. Filtre passe bas
ordreFIR = 20
fc = 0.1
print(fc)
pb1_fir = signal.firwin(ordreFIR, fc)
y1_fir = signal.lfilter(pb1_fir, [1.0], chimp)
plt.figure(18)
plt.title("Filtre passe bas par transformée de Fourier")
io.imshow(y1_fir, cmap='gray')

# Q2.2.3. Filtre passe haut
fc = 0.5
print(fc)
pb1_fir = signal.firwin(ordreFIR, fc)
y1_fir = signal.lfilter(pb1_fir, [1.0], chimp)
plt.figure(19)
plt.title("Filtre passe haut par transformée de Fourier")
io.imshow(y1_fir, cmap='gray')