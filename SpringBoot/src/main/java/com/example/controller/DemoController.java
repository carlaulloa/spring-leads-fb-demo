package com.example.controller;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.PagedList;
import org.springframework.social.facebook.api.Post;
import org.springframework.social.facebook.api.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/")
public class DemoController {

	private Facebook facebook;
	private ConnectionRepository connectionRepository;
	
	private Map<String, String> tokens = new HashMap<>();
	
	//private List<UpdateHandler> updateHandlers;

	private String applicationSecret;
	
	String accessToken = "710653895983634|30-_9EjQXdQN66AIqZUBOmmkkuU";
	
	@Inject
	public DemoController(Facebook facebook, ConnectionRepository connectionRepository){
		this.facebook = facebook;
		this.connectionRepository = connectionRepository;
		tokens.put("itsystems", "secret"); // secret es el token de verificacion que se pasa junto a la callback url
		
	}

	@GetMapping(value="persona")
	public String greeting(@RequestParam(value = "nombre", required = false, defaultValue = "test") String nombre,
			Model model) {
		model.addAttribute("nombre", nombre);
		return "persona";
	}
	
	// Callback URL webhook/itsystems 	
	@GetMapping(value="webhook/{subscription}", params="hub.mode=subscribe")
	public @ResponseBody String verifySubscription(
			@PathVariable("subscription") String subscription,
			@RequestParam("hub.challenge") String challenge,
			@RequestParam("hub.verify_token") String verifyToken) {
		return tokens.containsKey(subscription) && tokens.get(subscription).equals(verifyToken) ? challenge : "";
	}
	
}
