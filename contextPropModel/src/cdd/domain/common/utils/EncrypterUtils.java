package cdd.domain.common.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.itextpdf.text.pdf.codec.Base64;

public class EncrypterUtils {
	
	// Definicion del modo de cifrado a utilizar
	private final static String cI = "AES/CBC/PKCS5Padding";
	private static final String SEMILLA = "semilla9semilla8";
	private static final String SEMILLA_INICIAL = "semikka0semikka1";
	private final static String ALGORITHM = "AES";
	
	public static String encrypt(String textoACodificar) throws Exception {
		Cipher cipher = Cipher.getInstance(cI);
		SecretKeySpec skeySpec = new SecretKeySpec(SEMILLA.getBytes(), ALGORITHM);
		IvParameterSpec ivParameterSpec = new IvParameterSpec(SEMILLA_INICIAL.getBytes());
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec);
		byte[] encrypted = cipher.doFinal(textoACodificar.getBytes());
		return Base64.encodeBytes(encrypted);
	}

	public static String desencrypt(String decodedText) throws Exception {
		byte[] decodedBytes = Base64.decode(decodedText);
		Cipher cipher = Cipher.getInstance(cI);
		SecretKeySpec skeySpec = new SecretKeySpec(SEMILLA.getBytes(), ALGORITHM);
		IvParameterSpec ivParameterSpec = new IvParameterSpec(SEMILLA_INICIAL.getBytes());
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParameterSpec);
		byte[] desencrypted = cipher.doFinal(decodedBytes);
		return new String(desencrypted);
	}

	public static String encriptarParams(Map<String, String> params) throws Exception {
		StringBuilder encriptedParam = new StringBuilder();
		Iterator<Map.Entry<String, String>> entries = params.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry<String, String> param = entries.next();
			// encriptamos tanto las claves como los valores
			String keyOfParam = param.getKey();
			String cRcOfKey = encrypt(keyOfParam);
			String valueOfParam = param.getValue();
			String cRcOfValue = encrypt(valueOfParam);
			encriptedParam.append(cRcOfKey);
			encriptedParam.append("rrrrr");
			encriptedParam.append(cRcOfValue);
			if (entries.hasNext()) {
				encriptedParam.append("lllll");
			}
		}
		return encriptedParam.toString();
	}

	public static Map<String, String> desencriptarParams(String valueOfParam) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		String[] splitterParams = valueOfParam.split("lllll");
		for (final String split: splitterParams) {
			String paramDecoded = split;
			String[] keyAndValue = paramDecoded.split("rrrrr");
			String key = desencrypt(keyAndValue[0]);
			String value = desencrypt(keyAndValue[1]);
			params.put(key, value);
		}
		return params;
	}

	public static void main(String[] args) {
		try {
			System.out.println("cadena original: Alberto");
			String encodedName_ = EncrypterUtils.encrypt("Alberto");
			System.out.println("encoded: " + encodedName_);
			String decodedName_ = EncrypterUtils.desencrypt(encodedName_);
			System.out.println("decoded: " + decodedName_);
			
			Map<String, String> paramsToEncrypt = new HashMap<String, String>();
			paramsToEncrypt.put("selectExpediente", "CM-1529-2016");
			paramsToEncrypt.put("centroG", "4601");
			paramsToEncrypt.put("indiceDoc", "234");

			String encodedParams = encriptarParams(paramsToEncrypt);
			System.out.println("params encoded: " + encodedParams);

			Map<String, String> decodedParams = desencriptarParams(encodedParams);
			Iterator<Map.Entry<String, String>> entries = decodedParams.entrySet().iterator();
			while (entries.hasNext()) {
				Map.Entry<String, String> param = entries.next();
				// encriptamos tanto las claves como los valores
				String keyOriginal = param.getKey();
				String valueOriginal = param.getValue();

				System.out.println("decoded param: [" + keyOriginal + ": " + valueOriginal + "]");
			}
			
			System.out.println("*******************************************");
			System.out.println("*******************************************");
			
			String pattern ="[0-9]{2}/[0-9]{2}/[0-9]{4}";
			String exprs = "01/02/2016", exprErr = "28/1/2017";
			boolean b = Pattern.matches(pattern,exprs);
			
			System.out.println("o" + exprs + " se ajusta a patron? " + b);
			b = Pattern.matches(pattern, exprErr);
			System.out.println("o" + exprErr + " se ajusta a patron? " + b);
			System.out.println("*******************************************");
			System.out.println("*******************************************");
			System.out.println ("oCumple patron SGAC-* el nombre SGAC-SANI.xlsx?" + Pattern.matches(pattern,exprs));
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
