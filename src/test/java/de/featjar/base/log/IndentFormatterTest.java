package de.featjar.base.log;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class IndentFormatterTest {
    @Test
    void getPrefix() {
        IndentFormatter indentFormatter = new IndentFormatter();
        assertEquals("", indentFormatter.getPrefix());
        indentFormatter.addIndent();
        assertEquals("\t", indentFormatter.getPrefix());
        indentFormatter.addIndent();
        assertEquals("\t\t", indentFormatter.getPrefix());
        indentFormatter.removeIndent();
        indentFormatter.removeIndent();
        assertEquals("", indentFormatter.getPrefix());
        indentFormatter.removeIndent();
        assertEquals("", indentFormatter.getPrefix());
        indentFormatter.setLevel(2);
        assertEquals("\t\t", indentFormatter.getPrefix());
        indentFormatter.setLevel(0);
        assertEquals("", indentFormatter.getPrefix());
        indentFormatter.setLevel(-1);
        assertEquals("", indentFormatter.getPrefix());
        indentFormatter.setSymbol("  ");
        indentFormatter.setLevel(2);
        assertEquals("    ", indentFormatter.getPrefix());
        indentFormatter.setLevel(0);
        assertEquals("", indentFormatter.getPrefix());
    }
}
