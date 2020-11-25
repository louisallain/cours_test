package tp2.client.entrepotserviceclient;

import java.io.IOException;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import tp2.services.entrepotservice.*;

public class Activator implements BundleActivator {

    public static void createAndShowGUI(EntrepotService es) {

        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Composants
        JTextField addToStockTextField = new JTextField();
        JButton addToStockButton = new JButton("Ajout");  
        JTextField removeFromStockTextField = new JTextField();
        JButton removeFromStockButton = new JButton("Retrait");
        JButton printStockButton = new JButton("Stock");

        // ActionListener
        addToStockButton.addActionListener(new ActionListener() {  
        public void actionPerformed(ActionEvent e) {  
                es.ajouterArticles(Integer.valueOf(addToStockTextField.getText()).intValue());
            }  
        });
        removeFromStockButton.addActionListener(new ActionListener() {  
        public void actionPerformed(ActionEvent e) {  
                es.retirerArticles(Integer.valueOf(removeFromStockTextField.getText()).intValue());
            }  
        });
        printStockButton.addActionListener(new ActionListener() {  
        public void actionPerformed(ActionEvent e) {  
                JOptionPane.showMessageDialog(frame, "Stock = " + es.afficherStock());
            }  
        });


        // Layout
        GridLayout layout = new GridLayout(1,0);
        frame.setLayout(layout);

        // Ajout des composants à la fenêtre
        frame.add(addToStockTextField);
        frame.add(addToStockButton);
        frame.add(removeFromStockTextField);
        frame.add(removeFromStockButton);
        frame.add(printStockButton);
 
        frame.pack();
        frame.setVisible(true);
    }
    
    public void start(BundleContext context) throws BundleException {

        try {
            ServiceReference[] refs = context.getServiceReferences(EntrepotService.class.getName(), "(Type=*)");
            if(refs != null) {
                EntrepotService es = (EntrepotService) context.getService(refs[0]);
                
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        createAndShowGUI(es);
                    }
                });

                context.ungetService(refs[0]);
            }
        }
        catch(InvalidSyntaxException e) {
            e.printStackTrace();
        }
    }

    public void stop(BundleContext context) {
        // NOTE: The service is automatically released.
    }
}
