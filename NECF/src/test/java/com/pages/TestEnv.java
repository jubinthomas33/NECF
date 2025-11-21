package com.pages;

public class TestEnv {

	public static void main(String[] args) {
		System.out.println("email = " + System.getenv("EMAIL"));
        System.out.println("password = " + System.getenv("PASSWORD"));
        System.out.println("recipient1 = " + System.getenv("RECIPIENT1"));
        System.out.println("recipient2 = " + System.getenv("RECIPIENT2"));

	}

}
