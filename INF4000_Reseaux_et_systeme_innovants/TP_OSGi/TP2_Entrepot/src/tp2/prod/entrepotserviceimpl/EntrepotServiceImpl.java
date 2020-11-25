package tp2.prod.entrepotserviceimpl;

import tp2.services.entrepotservice.*;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class EntrepotServiceImpl implements EntrepotService {

    private int stock = 0;

    JFrame frame;

    public EntrepotServiceImpl() {
        JFrame.setDefaultLookAndFeelDecorated(true);
        frame = new JFrame("Entrepot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(400, 300));
        GridLayout layout = new GridLayout(0,1);
        frame.setLayout(layout);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public int afficherStock() {
        return stock;
    }
    
    @Override
    public void ajouterArticles(int nombre) {
        stock = stock + nombre;
        frame.add(new JLabel("Ajout de " + nombre + " / Stock=" + stock));
        frame.invalidate();
        frame.validate();
        frame.repaint();
    }
    
    @Override
    public void retirerArticles(int nombre) {
        stock = stock - nombre;
        frame.add(new JLabel("Retrait de " + nombre + " / Stock=" + stock));
        frame.invalidate();
        frame.validate();
        frame.repaint();
    }
}
