package com.surirobot;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

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
	public static String URL_API = "http://localhost:8080/emotions/actions/retrieve-video-emotion";
	
	//Label ou afficher les image et le résultat
	public static JLabel camera;
	public static JLabel result;
	static final long RECORD_TIME = 10000;  // 1 minute
	
	private static AtomicInteger counter = new AtomicInteger(0);
	private static AtomicInteger counterRecord = new AtomicInteger(0);
	
	
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
			JSONObject ja = new JSONObject();
			final JavaSoundRecorder recorder = new JavaSoundRecorder();
			new Thread(()-> {
				recorder.start();
			}).start();
			while (true) {
				if (!webcam.isOpen()) {
					break;
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//Récuperer une image depuis la webcam
				BufferedImage image = webcam.getImage();
				

				if (image == null) {
					break;
				}
				
				
				//On encode notre image en Base64 et on la met dans un JSON
				//ja.put(encoder(image));
				
				//On affiche notre image sur l'écran
				ImageIcon icon = new ImageIcon(image);
				camera.setIcon(icon);
				
				int n = counter.incrementAndGet();
				int m = counterRecord.incrementAndGet();
				
				//Dés qu'on aura 20 images on envoi à l'API
				if (n != 0 && n % 25 == 0) {
					counter.set(0);
					ja.put("pictures", encoder(image));
					if(m!= 0 && m % 250 == 0) {
						counterRecord.set(0);
						recorder.finish();
						new Worker(ja, new JSONObject().put("record", encoder("./RecordAudio.wav"))).start();
						recorder.deleteFile();
						new Thread(()-> {
							recorder.start();
						}).start();
					}else {
						new Worker(ja).start();
						ja = new JSONObject();
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
	
	public static String encoder(String imagePath) {
		String base64Image = "";
		File file = new File(imagePath);
		try {
			FileInputStream imageInFile = new FileInputStream(file);
			// Reading a Image file from file system
			byte imageData[] = new byte[(int) file.length()];
			imageInFile.read(imageData);
			base64Image = Base64.getEncoder().encodeToString(imageData);
		} catch (FileNotFoundException e) {
			System.out.println("Image not found" + e);
		} catch (IOException ioe) {
			System.out.println("Exception while reading the Image " + ioe);
		}
		return base64Image;
	}
}


