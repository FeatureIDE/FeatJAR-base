package de.featjar.base.log;

public class IndentStringBuilder {
    protected final StringBuilder stringBuilder;
    protected final IndentFormatter indentFormatter;

    public IndentStringBuilder() {
        this(new StringBuilder(), new IndentFormatter());
    }

    public IndentStringBuilder(StringBuilder stringBuilder) {
        this(stringBuilder, new IndentFormatter());
    }

    public IndentStringBuilder(IndentFormatter indentFormatter) {
        this(new StringBuilder(), indentFormatter);
    }

    public IndentStringBuilder(StringBuilder stringBuilder, IndentFormatter indentFormatter) {
        this.stringBuilder = stringBuilder;
        this.indentFormatter = indentFormatter;
    }

    public StringBuilder getStringBuilder() {
        return stringBuilder;
    }

    public IndentFormatter getIndentFormatter() {
        return indentFormatter;
    }

    public IndentStringBuilder append(String string) {
        stringBuilder.append(indentFormatter.getPrefix()).append(string);
        return this;
    }

    public IndentStringBuilder appendLine(String string) {
        return append(string + "\n");
    }

    public IndentStringBuilder appendLine() {
        return appendLine("");
    }

    public IndentStringBuilder addIndent() {
        indentFormatter.addIndent();
        return this;
    }

    public IndentStringBuilder removeIndent() {
        indentFormatter.removeIndent();
        return this;
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }
}
