package com.example.handler;

import org.springframework.stereotype.Service;

import com.example.model.RealTimeUpdate;

@Service
public class UpdateHandlerImpl implements UpdateHandler {
	
	public void handleUpdate(String subscription, RealTimeUpdate update){
		System.out.println("Update handler");
		System.out.println(subscription);
		System.out.println(update);
	}
	
}
