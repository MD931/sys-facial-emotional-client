package com.surirobot;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

public class VocalClient extends JFrame {
	private JLabel filename = new JLabel(), emotion = new JLabel();

	private JButton open = new JButton("Open"), submit = new JButton("Submit");
	private String pathFile;

	public VocalClient() {
		setSize(400, 200);
		JPanel p = new JPanel();
		JLabel file = new JLabel("File :" );
		JLabel result = new JLabel("Result : ");
		open.addActionListener(new OpenL());
		p.add(open);
		submit.addActionListener(new SubmitL());
		p.add(submit);
		Container cp = getContentPane();
		cp.add(p, BorderLayout.SOUTH);
		p = new JPanel();
		p.setLayout(new GridLayout(2, 2));
		p.add(file);
		p.add(filename);
		p.add(result);
		p.add(emotion);
		cp.add(p, BorderLayout.NORTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setLocationRelativeTo(null);
	}

	class OpenL implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JFileChooser c = new JFileChooser();
			// Demonstrate "Open" dialog:
			int rVal = c.showOpenDialog(VocalClient.this);
			if (rVal == JFileChooser.APPROVE_OPTION) {
				pathFile = c.getSelectedFile().getAbsolutePath();
				filename.setText(c.getSelectedFile().getAbsolutePath());
				//dir.setText();
			}
			if (rVal == JFileChooser.CANCEL_OPTION) {
				filename.setText("You pressed cancel");
			}
		}
	}

	class SubmitL implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			try {
				String response = send("http://localhost:8080/emotions/actions/retrieve-vocal-emotion", pathFile);
				emotion.setText(new JSONObject(response).getString("emotion"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		public String send(String url, String file) throws IOException{
			HttpClient client = new DefaultHttpClient();  
			HttpPost post = new HttpPost(url);
			HttpResponse response;
			JSONObject js = new JSONObject();
			js.put("record",VocalClient.encoder(file));
	        StringEntity se = new StringEntity(js.toString());
	        se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
	        post.setEntity(se);
	        response = client.execute(post);
	        InputStream in = response.getEntity().getContent();
	        BufferedReader bis = new BufferedReader(new InputStreamReader(in));
	        
	        StringBuilder str = new StringBuilder();
	        String s;
	        while((s = bis.readLine())!=null) {
	        	str.append(s);
	        }
	        return str.toString();
		}

	}
	
	public static  String encoder(String vocalPath) {
		String base64Audio = "";
		File file = new File(vocalPath);
		try {
			FileInputStream audioInFile = new FileInputStream(file);
			byte audioData[] = new byte[(int) file.length()];
			audioInFile.read(audioData);
			base64Audio = Base64.getEncoder().encodeToString(audioData);
		} catch (FileNotFoundException e) {
			System.out.println("File not found" + e);
		} catch (IOException ioe) {
			System.out.println("Exception while reading the Audio file " + ioe);
		}
		return base64Audio;
	}
}
