package net.sf.j2ep;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;

public class UrlRewritingOutputStream extends ServletOutputStream {
    
    private ServletOutputStream originalStream;
    private ByteArrayOutputStream stream;
    
    
    public UrlRewritingOutputStream(ServletOutputStream originalStream) {
        this.originalStream = originalStream;
        stream = new ByteArrayOutputStream();
    }


    public void write(int b) throws IOException {
        stream.write(b);        
    }
    
    public void write(byte[] b, int off, int len) throws IOException {
        stream.write(b, off, len);
    }
    
    public void write(byte[] b) throws IOException {
        stream.write(b);
    }
    
    public void rewrite() throws IOException {
        Pattern linkPattern = Pattern.compile("\\b(href=|src=)\"?([^\"\\s>]+)[\"?\\s]", Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ);
        
        StringBuffer page = new StringBuffer();
        Matcher matcher = linkPattern.matcher(stream.toString());
        while (matcher.find()) {
           String link = matcher.group(2);
           matcher.appendReplacement(page, "$1\"" + link + "\"");
        }
        matcher.appendTail(page);
        
        originalStream.print(page.toString());
        stream.close();
        originalStream.close();
    }
    
    public void process() throws IOException {
        originalStream.write(stream.toByteArray());
        stream.close();
        originalStream.close();
    }
    
    

}
