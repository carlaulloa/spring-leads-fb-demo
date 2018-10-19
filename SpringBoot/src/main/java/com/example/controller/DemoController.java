package com.example.controller;

import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.handler.UpdateHandler;
import com.example.model.RealTimeUpdate;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/")
public class DemoController {

	private Facebook facebook;
	private ConnectionRepository connectionRepository;
	
	private @Autowired UpdateHandler handler;
	
	private Map<String, String> tokens = new HashMap<>();
	
	//private List<UpdateHandler> updateHandlers;

	private String applicationSecret = "058fe13636df2c308fb347ab4b0f5989";
	
	String accessToken = "710653895983634|30-_9EjQXdQN66AIqZUBOmmkkuU";
	
	@Inject
	public DemoController(Facebook facebook, ConnectionRepository connectionRepository){
		this.facebook = facebook;
		this.connectionRepository = connectionRepository;
		tokens.put("itsystems", "secret"); // secret es el token de verificacion que se pasa junto a la callback url
		
	}

	@GetMapping(value="connectFb")
	public String greeting() {
		return "connect/fbConnect";
	}
	
	// Callback URL webhook/itsystems 	
	@GetMapping(value="webhook/{subscription}", params="hub.mode=subscribe")
	public @ResponseBody String verifySubscription(
			@PathVariable("subscription") String subscription,
			@RequestParam("hub.challenge") String challenge,
			@RequestParam("hub.verify_token") String verifyToken) {
		System.out.println("Verify subscription");	
		return tokens.containsKey(subscription) && tokens.get(subscription).equals(verifyToken) ? challenge : "";
	}
	
	@PostMapping(value="webhook/{subscription}")
	public @ResponseBody String receiveUpdate(
			@PathVariable("subscription") String subscription,
			@RequestBody String payload,
			@RequestHeader(X_HUB_SIGNATURE) String signature) throws Exception {

		// Can only read body once and we need it as a raw String to calculate the signature.
		// Therefore, use Jackson ObjectMapper to give us a RealTimeUpdate object from that raw String.
		System.out.println(payload);		
		RealTimeUpdate update = new ObjectMapper().readValue(payload, RealTimeUpdate.class);		
		if (verifySignature(payload, signature)) {
			System.out.println("Received " + update.getObject() + " update for '" + subscription + "'.");
			handler.handleUpdate(subscription, update);
		} else {
			System.out.println("Received an update, but signature was invalid. Not delegating to handlers.");
		}
		return "";
	}

	private boolean verifySignature(String payload, String signature) throws Exception {
		if (!signature.startsWith("sha1=")) {
			return false;
		}
		String expected = signature.substring(5);		
		Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
		SecretKeySpec signingKey = new SecretKeySpec(applicationSecret.getBytes(), HMAC_SHA1_ALGORITHM);
		mac.init(signingKey);
		byte[] rawHmac = mac.doFinal(payload.getBytes());
		String actual = new String(Hex.encode(rawHmac));
		return expected.equals(actual);
	}

	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

	private static final String X_HUB_SIGNATURE = "X-Hub-Signature";

	
}
