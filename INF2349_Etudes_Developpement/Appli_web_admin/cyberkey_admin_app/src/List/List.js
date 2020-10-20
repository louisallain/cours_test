import React from 'react'
import './List.css'

var todoItems = [];
todoItems.push({ index: 1, value: "learn react"});
todoItems.push({ index: 2, value: "Go shopping"});
todoItems.push({ index: 3, value: "buy flowers"});

export default class List extends React.Component {
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

class TodoListItem extends React.Component {
    constructor(props) {
        super(props);
        this.onClickClose = this.onClickClose.bind(this);
        this.onClickDone = this.onClickDone.bind(this);
    }
    onClickClose() {
        var index = parseInt(this.props.index);
        var item = this.props.item;
        this.props.removeItem(index, item);
    }
    onClickDone() {
        var index = parseInt(this.props.index);
        var item = this.props.item;
        this.props.validateItem(index, item);
    }
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