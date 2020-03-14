package cdd.webapp;

import java.util.HashMap;
import java.util.Map;

public class MimeTypes {
	
	protected static final Map<String, String> mimeTypes = new HashMap<String, String>();
	
	static {
		mimeTypes.put("", "text/plain");
		mimeTypes.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		mimeTypes.put("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
		mimeTypes.put("potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
		mimeTypes.put("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
		mimeTypes.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
		mimeTypes.put("sldx", "application/vnd.openxmlformats-officedocument.presentationml.slide");
		mimeTypes.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		mimeTypes.put("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
		mimeTypes.put("xlam", "application/vnd.ms-excel.addin.macroEnabled.12");
		mimeTypes.put("xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12");
		mimeTypes.put("txt", "text/plain");
		mimeTypes.put("pdf", "application/pdf");
		mimeTypes.put("au", "audio/basic");
		mimeTypes.put("avi", "video/msvideo");
		mimeTypes.put("bmp", "image/bmp");
		mimeTypes.put("bz2", "application/x-bzip2");
		mimeTypes.put("css", "text/css");
		mimeTypes.put("dtd", "application/xml-dtd");
		mimeTypes.put("doc", "application/msword");
		mimeTypes.put("zip", "application/zip, application/x-compressed-zip");
		mimeTypes.put("xml", "application/xml");
		mimeTypes.put("wav", "audio/wav, audio/x-wav");
		mimeTypes.put("tsv", "text/tab-separated-values");
		mimeTypes.put("tiff", "image/tiff");
		mimeTypes.put("tgz", "application/x-tar");
		mimeTypes.put("gz", "application/x-tar");
		mimeTypes.put("tar", "application/x-tar");
		mimeTypes.put("swf", "application/x-shockwave-flash");
		mimeTypes.put("svg", "image/svg+xml");
		mimeTypes.put("sit", "application/x-stuffit");
		mimeTypes.put("sgml", "text/sgml");
		mimeTypes.put("rtf", "application/rtf");
		mimeTypes.put("rdf", "application/rdf, application/rdf+xml");
		mimeTypes.put("ram", "audio/x-pn-realaudio, audio/vnd.rn-realaudio");
		mimeTypes.put("ra", "audio/x-pn-realaudio, audio/vnd.rn-realaudio");
		mimeTypes.put("qt", "video/quicktime");
		mimeTypes.put("ps", "application/postscript");
		mimeTypes.put("png", "image/png");
		mimeTypes.put("pl", "application/x-perl");
		mimeTypes.put("ogg", "audio/vorbis");
		mimeTypes.put("mpeg", "video/mpeg");
		mimeTypes.put("mp3", "audio/mpeg");
		mimeTypes.put("midi", "audio/x-midi");
		mimeTypes.put("js", "application/x-javascript");
		mimeTypes.put("jpg", "image/jpeg");
		mimeTypes.put("jar", "application/java-archive");
		mimeTypes.put("html", "text/html");
		mimeTypes.put("hqx", "application/mac-binhex40");
		mimeTypes.put("gz", "application/x-gzip");
		mimeTypes.put("gif", "image/gif");
		mimeTypes.put("exe", "application/octet-stream");
		mimeTypes.put("es", "application/ecmascript");
	}
}
