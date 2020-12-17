import React from 'react'
import './List.css'

/**
 * Classe représentant une liste de choses avec un bouton valider et supprimer.
 */
export default class List extends React.Component {

    /**
     * Méthode de rendu du composant.
     */
    render() {
        var items = this.props.items.map((item, index) => {
            return (
                <TodoListItem 
                    key={item.id} 
                    item={item} 
                    index={index} 
                    removeItem={this.props.removeItem} 
                    validateItem={this.props.validateItem} 
                    hasValidateButton={this.props.hasValidateButton}/>
            );
        });
        return (
            <ul className="list-group"> {items} </ul>
        );
    }
}

/**
 * Classe représentant un item de la liste définie ci-dessus.
 */
class TodoListItem extends React.Component {

    /**
     * Initialise l'état du composant.
     * @param {*} props propriétés héritées du parent.
     */
    constructor(props) {
        super(props);
        this.onClickClose = this.onClickClose.bind(this);
        this.onClickDone = this.onClickDone.bind(this);
    }

    /**
     * Handler du bouton de suppression de l'élément.
     */
    onClickClose() {
        var index = parseInt(this.props.index);
        var item = this.props.item;
        this.props.removeItem(index, item);
    }

    /**
     * Handler du bouton de validation de l'élément.
     */
    onClickDone() {
        var index = parseInt(this.props.index);
        var item = this.props.item;
        this.props.validateItem(index, item);
    }

    /**
     * Méthode de rendu du composant.
     */
    render() {
        return (
            <li className="list-group-item ">
                {this.props.hasValidateButton && <button type="button" title="Valider" className="item-button item-button-validate" onClick={this.onClickDone}>&#10003;</button>}
                <button type="button" title="Supprimer" className="item-button item-button-remove" onClick={this.onClickClose}>&#88;</button>
                {this.props.item.id}
            </li>
        );
    }
}