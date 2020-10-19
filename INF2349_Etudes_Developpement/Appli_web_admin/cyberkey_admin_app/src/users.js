import { v4 as uuidv4 } from 'uuid';

let users = [
    {
        id: "etud1ddd@etud.univ-ubs.fr",
        requestForEvents: ["b0c5d52a-e193-4e28-9a00-572cac747b8d"],
        acceptedForEvents: ["8ef5f52d-94fe-4db8-86ec-2cb6f92d486e"],
        isVIP: true,
        requestVIP: false,
    },
    {
        id: "etud2@etud.univ-ubs.fr",
        requestForEvents: ["b0c5d52a-e193-4e28-9a00-572cac747b8d"],
        acceptedForEvents: ["8ef5f52d-94fe-4db8-86ec-2cb6f92d486e"],
        isVIP: false,
        requestVIP: true,
    },
    {
        id: "etud3@etud.univ-ubs.fr",
        requestForEvents: ["b0c5d52a-e193-4e28-9a00-572cac747b8d", "8ef5f52d-94fe-4db8-86ec-2cb6f92d486e"],
        acceptedForEvents: [],
        isVIP: false,
        requestVIP: true,
    },
    {
        id: "etud4ddd@etud.univ-ubs.fr",
        requestForEvents: [],
        acceptedForEvents: ["8ef5f52d-94fe-4db8-86ec-2cb6f92d486e", "b0c5d52a-e193-4e28-9a00-572cac747b8d"],
        isVIP: false,
        requestVIP: false,
    },
    {
        id: "etud5@etud.univ-ubs.fr",
        requestForEvents: [],
        acceptedForEvents: ["8ef5f52d-94fe-4db8-86ec-2cb6f92d486e", "b0c5d52a-e193-4e28-9a00-572cac747b8d"],
        isVIP: false,
        requestVIP: false,
    },
    
    {
        id: "etud3bidou@etud.univ-ubs.fr",
        requestForEvents: ["b0c5d52a-e193-4e28-9a00-572cac747b8d"],
        acceptedForEvents: ["8ef5f52d-94fe-4db8-86ec-2cb6f92d486e"],
        isVIP: false,
        requestVIP: false,
    },
    {
        id: "etud5dzdze@etud.univ-ubs.fr",
        requestForEvents: ["b0c5d52a-e193-4e28-9a00-572cac747b8d"],
        acceptedForEvents: ["8ef5f52d-94fe-4db8-86ec-2cb6f92d486e"],
        isVIP: false,
        requestVIP: false,
    },
    
    {
        id: "etuddzdzezd3@etud.univ-ubs.fr",
        requestForEvents: ["b0c5d52a-e193-4e28-9a00-572cac747b8d"],
        acceptedForEvents: ["8ef5f52d-94fe-4db8-86ec-2cb6f92d486e"],
        isVIP: false,
        requestVIP: false,
    },
    {
        id: "etuddeded4@etud.univ-ubs.fr",
        requestForEvents: ["b0c5d52a-e193-4e28-9a00-572cac747b8d"],
        acceptedForEvents: ["8ef5f52d-94fe-4db8-86ec-2cb6f92d486e"],
        isVIP: false,
        requestVIP: false,
    },
    {
        id: "etud5zzzzzzzzzzzzzzzzzz@etud.univ-ubs.fr",
        requestForEvents: ["b0c5d52a-e193-4e28-9a00-572cac747b8d"],
        acceptedForEvents: ["8ef5f52d-94fe-4db8-86ec-2cb6f92d486e"],
        isVIP: false,
        requestVIP: false,
    },
    
]

export default users;