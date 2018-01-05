package com.surirobot;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;


public class FacialClient extends JFrame {
	
	public FacialClient() {
		
		setLayout(new BorderLayout());
		setSize(600, 400);
		Panel panel = new Panel();
		panel.setLayout(new FlowLayout());
		camera = new JLabel();
		panel.add(camera);
		add("North",panel);
		panel = new Panel();
		panel.setLayout(new FlowLayout());
		result = new JLabel();
		result.setText("");
		panel.add(new JLabel("Résultat :"));
		panel.add(result);
		add("Center",panel);
		setResizable(false);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		new Capture().start();
		setLocationRelativeTo(null);

	}
	//Url de l'api
	public static String URL_API = "http://localhost:8080/emotions/actions/retrieve-facial-emotion";
	
	//Label ou afficher les image et le résultat
	public static JLabel camera;
	public static JLabel result;
	
	private static AtomicInteger counter = new AtomicInteger(0);
	
	
	private static final class Capture extends Thread {

		private static final AtomicInteger number = new AtomicInteger(0);
		
		public Capture() {
			super("capture-" + number.incrementAndGet());
		}

		@Override
		public void run() {
			//Initialiser la webcam et l'ouvrir
			Webcam webcam = Webcam.getDefault();
			webcam.setViewSize(WebcamResolution.QVGA.getSize());
			webcam.open();
			
			while (true) {
				if (!webcam.isOpen()) {
					break;
				}
				
				//Récuperer une image depuis la webcam
				BufferedImage image = webcam.getImage();

				if (image == null) {
					break;
				}
				JSONObject js = new JSONObject();
				JSONArray ja = new JSONArray();
				
				//On encode notre image en Base64 et on la met dans un JSON
				ja.put(encoder(image));
				
				//On affiche notre image sur l'écran
				ImageIcon icon = new ImageIcon(image);
				camera.setIcon(icon);
				
				int n = counter.incrementAndGet();
				
				//Dés qu'on aura 25 images on envoi à l'API
				if (n != 0 && n % 25 == 0) {
					counter.set(0);
					
					//On construit la requete
					HttpClient client = new DefaultHttpClient();  
					HttpPost post = new HttpPost(URL_API);
					HttpResponse response;
			        StringEntity se;
					try {
						se = new StringEntity(js.put("pictures", ja).toString());
				        se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				        post.setEntity(se);
				        response = client.execute(post);
				        BufferedReader bis = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				        StringBuilder s = new StringBuilder();
				        String tmp = "";
				        
				        //On lis le résultat
				        while((tmp = bis.readLine())!=null) {
				        	s.append(tmp);
				        }
				        System.out.println("EMOTION ==== "+s.toString());
				        
				        //On affiche le résultat à l'écran
				        result.setText(new JSONObject(s.toString()).optString("emotion", "null"));
				        
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println(Thread.currentThread().getName() + ": Frames captured: " + n);
				}
			}
		}
	}
	
	/*
	 * Méthode qui encode notre image en Base64
	 */
	public static String encoder(BufferedImage image) {
		String base64Image = "";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write( image, "jpg", baos );
			baos.flush();
			byte[] imageInByte = baos.toByteArray();
			base64Image = Base64.getEncoder().encodeToString(imageInByte);
		} catch (FileNotFoundException e) {
			System.out.println("Image not found" + e);
		} catch (IOException ioe) {
			System.out.println("Exception while reading the Image " + ioe);
		}
		return base64Image;
	}
}
