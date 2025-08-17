package io.github.greenstevester.confluence_mcp_svr.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Utility class for formatting content as Markdown
 */
@Component
public class MarkdownFormatter {
    
    private static final DateTimeFormatter DEFAULT_DATE_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Format a heading with the specified level
     */
    public String formatHeading(String text, int level) {
        if (level < 1 || level > 6) {
            level = 1;
        }
        return "#".repeat(level) + " " + text;
    }
    
    /**
     * Format a URL as markdown link
     */
    public String formatUrl(String url, String text) {
        if (url == null || url.trim().isEmpty()) {
            return text != null ? text : "";
        }
        return "[" + (text != null ? text : url) + "](" + url + ")";
    }
    
    /**
     * Format a date using the default format
     */
    public String formatDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DEFAULT_DATE_FORMAT) : "Not available";
    }
    
    /**
     * Format a separator line
     */
    public String formatSeparator() {
        return "---";
    }
    
    /**
     * Format a bullet list from a map of properties
     */
    public String formatBulletList(Map<String, Object> properties, Function<String, String> keyMapper) {
        return properties.entrySet().stream()
            .map(entry -> "- **" + keyMapper.apply(entry.getKey()) + "**: " + formatValue(entry.getValue()))
            .reduce("", (acc, line) -> acc.isEmpty() ? line : acc + "\n" + line);
    }
    
    /**
     * Format a numbered list from a collection
     */
    public <T> String formatNumberedList(List<T> items, Function<T, String> formatter) {
        return IntStream.range(0, items.size())
            .mapToObj(i -> {
                T item = items.get(i);
                String content = formatter.apply(item);
                return (i + 1) + ". " + content;
            })
            .reduce("", (acc, line) -> acc.isEmpty() ? line : acc + "\n\n" + line);
    }
    
    /**
     * Format a code block with optional language
     */
    public String formatCodeBlock(String content, String language) {
        String lang = language != null ? language : "";
        return "```" + lang + "\n" + content + "\n```";
    }
    
    /**
     * Format an object value for display
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "N/A";
        }
        if (value instanceof LocalDateTime) {
            return formatDate((LocalDateTime) value);
        }
        return value.toString();
    }
    
    /**
     * Format a blockquote
     */
    public String formatBlockquote(String text) {
        return "> " + text;
    }
    
    /**
     * Format inline code
     */
    public String formatInlineCode(String text) {
        return "`" + text + "`";
    }
    
    /**
     * Format bold text
     */
    public String formatBold(String text) {
        return "**" + text + "**";
    }
    
    /**
     * Format italic text
     */
    public String formatItalic(String text) {
        return "*" + text + "*";
    }
}