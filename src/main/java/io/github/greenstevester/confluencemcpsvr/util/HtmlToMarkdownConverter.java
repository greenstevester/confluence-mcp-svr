package io.github.greenstevester.confluencemcpsvr.util;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import org.springframework.stereotype.Component;

/**
 * Utility class for converting HTML content to Markdown
 */
@Component
public class HtmlToMarkdownConverter {
    
    private final FlexmarkHtmlConverter converter;
    
    public HtmlToMarkdownConverter() {
        this.converter = FlexmarkHtmlConverter.builder().build();
    }
    
    /**
     * Convert HTML content to Markdown
     */
    public String convert(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return "";
        }
        
        try {
            return converter.convert(htmlContent);
        } catch (Exception e) {
            // If conversion fails, return the original content with a warning
            return "<!-- HTML to Markdown conversion failed -->\n" + htmlContent;
        }
    }
}