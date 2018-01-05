package com.surirobot;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

public class HomeClient extends JFrame{

	public HomeClient() {
		super();
		setTitle("Emotion");
		setSize(400,200);
		setLayout(new FlowLayout());
		JButton facial = new JButton("Facial");
		facial.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				dispose();
				FacialClient frame = new FacialClient();
				frame.setVisible(true);
			}
		});
		
		JButton vocal = new JButton("Vocal");
		vocal.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				dispose();
				VocalClient frame = new VocalClient();
				frame.setVisible(true);
			}
		});
		add(facial);
		add(vocal);
		
		pack();
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setLocationRelativeTo(null);
	}

	public static void main(String[] args) {
		new HomeClient();
	}
}
