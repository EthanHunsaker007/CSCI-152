/*
 * Author: Ethan Hunsaker
 * Date: 01/23/2026
 * Title: CSCI-152 Lab One
 */


import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Font;

public class App {
    public static void main(String[] args) throws Exception {
        Font font = new Font("Monospaced", Font.BOLD, 12);
        String space = new String("&nbsp");


        JFrame frame = new JFrame("Business Card");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel label = new JLabel("<html>" + "************************************" 
                                  + "<br/>" + "*  Ethan Hunsaker" + space.repeat(19) + "*"
                                  + "<br/>" + "*  Creative Writing & Game Design" + space.repeat(3) + "*"
                                  + "<br/>" + "*  Emerald Green" + space.repeat(20) + "*"
                                  + "<br/>" + "************************************" + "</html>");

        label.setFont(font);
        frame.add(label,BorderLayout.CENTER);

        frame.setSize(300, 200);
        
        frame.setVisible(true);

        // System.out.println("************************************");
        // System.out.println("*  Ethan Hunsaker                  *");
        // System.out.println("*  Creative Writing & Game Design  *");
        // System.out.println("*  Emerald Green                   *");
        // System.out.println("************************************");
    }
}
