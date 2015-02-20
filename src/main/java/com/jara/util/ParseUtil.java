package com.jara.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

public class ParseUtil {
	
	public static String toNombreClase(String nombre){
		return StringUtils.deleteWhitespace(WordUtils.capitalizeFully(nombre));
	}
	
	public static String toNombreAtributo(String nombre){
		return StringUtils.deleteWhitespace(WordUtils.uncapitalize(nombre));
	}

}
