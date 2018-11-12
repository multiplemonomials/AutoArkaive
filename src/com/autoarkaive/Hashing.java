package com.autoarkaive;
//Hashing Algorithm found at: https://github.com/selwakowski/pracainz/blob/master/PracaInz_v01/src/com/howtodoinjava/hashing/password/demo/sha/SHAExample.java

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Hashing {
    /* 
    public static void main(String[] args) throws NoSuchAlgorithmException
    {
        String passwordToHash = "password";
        byte[] salt = getSalt();
         
        String securePassword = get_SHA_1_SecurePassword(passwordToHash, salt);
        System.out.println(securePassword);
    }
 */
    public static String get_SHA_1_SecurePassword(String passwordToHash)
    {
    	SecureRandom sr;
    	String generatedPassword = null;
		try {
			sr = SecureRandom.getInstance("SHA1PRNG");
		
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
	
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(salt);
            byte[] bytes = md.digest(passwordToHash.getBytes());
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return generatedPassword;
	
    }
 /*        
    //Add salt
    private static byte[] getSalt() throws NoSuchAlgorithmException
    {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }
*/
}
