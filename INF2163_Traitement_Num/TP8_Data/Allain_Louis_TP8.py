# -*- coding: utf-8 -*-
"""
Created on Tue Nov 12 08:24:03 2019

@author: e1602246
"""

import pandas as pd   #pour l'exploration de données
import numpy as np    #pour les opérations numériques

# Q1. Lire les données 'users'

unames = ['user_id', 'gender', 'age', 'occupation', 'zip']
users = pd.read_csv('./data/users.dat', sep='::', header=None, names=unames, engine='python')
print('USERS')
print(users.head())

# Q2. Lire les données 'ratings'

rnames = ['user_id', 'movie_id', 'rating', 'timestamp']
movies = pd.read_csv('./data/ratings.dat', sep='::', header=None, names=rnames, engine='python')
print('MOVIES')
print(movies.head(10))

# Q3. Lire les données 'movies'

mnames = ['movie_id', 'title', 'genre']
ratings = pd.read_csv('./data/movies.dat', sep='::', header=None, names=mnames, engine='python')
print('RATINGS')
print(ratings.head(10))

# Q4. Faire un merge (~ jointure de base de données)
data = pd.merge(users, pd.merge(movies, ratings))
print('MERGE')
print(data.head(10))

# Exploration

# Q1. 
print('MOY')
print(np.sum(data.rating > 4.5))
print(np.sum(data.rating[data.gender == 'F'] > 4.5))
print(np.sum(data.rating[data.gender == 'M'] > 4.5))

# Q2.
print('PROP')
tot = np.sum(data.rating[data.gender == 'F'])
stot = np.sum(data.rating[data.gender == 'F'] > 4.5)
print("Pourcentage de femmes ayant noté le film plus de 4.5 : ", stot/tot*100, " %")
tot = np.sum(data.rating[data.gender == 'M'])
stot = np.sum(data.rating[data.gender == 'M'] > 4.5)
print("Pourcentage d'hommes ayant noté le film plus de 4.5 : ", stot/tot*100, " %")

# Q3.
print(np.sum(data[(data.gender == 'M') & (data.age >= 30)].
groupby('movie_id', axis=0)['rating'].median() >= 4.5))

# Q4.
print(data.groupby('movie_id', axis=0)['rating'].mean().nlargest(15))
print(data[data.movie_id == 787])

# Q5.
data.groupby('movie_id', axis=0)['rating'].count().head()
data2 = pd.concat([data.groupby('movie_id', axis=0)['rating'].mean(),
data.groupby('movie_id', axis=0)['rating'].count()], axis=1)
data2.columns = ['mean_rating', 'n_rating']
print(data2.head())

# Q6.


# Q7.
print(data2['n_rating'].nlargest(1))
print(data[data.movie_id == 2858])

# Visualiser les données

# Q1.
data.rating.hist(bins=5, align='left', range=[1, 6])

# Q2.
data.groupby('movie_id', axis=0)['rating'].count().hist(bins=10)

# Q3.
data2.mean_rating.hist(bins=10)

# Q4.
map_id_to_count = data.groupby('movie_id')['rating'].count().to_dict()
data['movie_count'] = data['movie_id'].map(map_id_to_count)
# rajoute une variable comptant le nombre de votes par film
# changer kde par hist pour retrouver un histogramme et non une estimation de la densité
data[data.movie_count >= 30].groupby('movie_id', axis=0)['rating'].mean().plot(kind='kde', color='b')
data[data.movie_count <= 30].groupby('movie_id', axis=0)['rating'].mean().plot(kind='kde', color='g')

# Q5.
data[data.movie_count >= 100][data.gender == 'F'].groupby('movie_id', axis=0)['rating'].mean().plot(kind="scatter", color='b')
data[data.movie_count >= 100][data.gender == 'M'].groupby('movie_id', axis=0)['rating'].mean().plot(style=".", color='r')

# Q6.
print(type(data))
data[data.movie_count < 100][data.gender == 'F'].groupby('movie_id', axis=0)['rating'].mean().plot(style=".", color='b')
data[data.movie_count < 100][data.gender == 'M'].groupby('movie_id', axis=0)['rating'].mean().plot(style=".", color='r')

# Q7. On observe que les hommes notent plus souvent au-delà de 4.0 que les femmes