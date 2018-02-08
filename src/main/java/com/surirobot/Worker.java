package com.surirobot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Worker extends Thread {
	
	private JSONObject ja;
	private JSONObject js;
	
	public Worker(JSONObject ja, JSONObject js) {
		this.ja = ja;
		this.js = js;
	}
	
	public Worker(JSONObject ja) {
		this.ja = ja;
	}
	
	@Override
	public void run() {
		//On construit la requete
		HttpClient client = new DefaultHttpClient();  
		HttpPost post = new HttpPost(FacialClient.URL_API);
		HttpResponse response;
        StringEntity se;
		try {
			JSONObject json  = new JSONObject();
			json.put("pictures", ja.getString("pictures"));
			if(js != null)
				json.put("record", js.getString("record"));
			System.out.println(json.toString());
			se = new StringEntity(json.toString());
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
	        JSONObject res = new JSONObject(s.toString());
	        StringBuilder str = new StringBuilder();
	        str.append("Facial : "+res.getJSONArray("facial").toString()+"\n");
	        try {
	        	str.append("Vocal : "+res.getJSONObject("vocal").toString()+"\n");
	        }catch(JSONException e) {}
	        FacialClient.result.setText(str.toString());
	        
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
