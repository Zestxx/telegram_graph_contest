package org.telegram.graphic_contest.graph.formatter;

import org.telegram.graphic_contest.graph.util.FloatUtils;


public class ValueFormatterHelper {
    private static final int DEFAULT_DIGITS_NUMBER = 0;
    private final char[] appendedText = new char[0];
    private final char[] prependedText = new char[0];

    public char[] getAppendedText() {
        return appendedText;
    }


    private int formatFloatValueWithPrependedAndAppendedText(final char[] formattedValue, final float value, final int
            defaultDigitsNumber, final char[] label) {
        if (null != label) {
            int labelLength = label.length;
            if (labelLength > formattedValue.length) {
                labelLength = formattedValue.length;
            }
            System.arraycopy(label, 0, formattedValue, formattedValue.length - labelLength, labelLength);
            return labelLength;
        }

        final int charsNumber = formatFloatValue(formattedValue, value, defaultDigitsNumber);
        appendText(formattedValue);
        prependText(formattedValue, charsNumber);
        return charsNumber + prependedText.length + getAppendedText().length;
    }


    public int formatFloatValueWithPrependedAndAppendedText(final char[] formattedValue, final float value, final char[] label) {
        return formatFloatValueWithPrependedAndAppendedText(formattedValue, value, DEFAULT_DIGITS_NUMBER, label);
    }


    public int formatFloatValueWithPrependedAndAppendedText(final char[] formattedValue, final float value, final int
            defaultDigitsNumber) {
        return formatFloatValueWithPrependedAndAppendedText(formattedValue, value, defaultDigitsNumber, null);
    }

    private int formatFloatValue(final char[] formattedValue, final float value, final int decimalDigitsNumber) {
        final char decimalSeparator = '.';
        return FloatUtils.formatFloat(formattedValue, value, formattedValue.length - appendedText.length,
                decimalDigitsNumber,
                decimalSeparator);
    }

    private void appendText(final char[] formattedValue) {
        if (appendedText.length > 0) {
            System.arraycopy(appendedText, 0, formattedValue, formattedValue.length - appendedText.length,
                    appendedText.length);
        }
    }

    private void prependText(final char[] formattedValue, final int charsNumber) {
        if (prependedText.length > 0) {
            System.arraycopy(prependedText, 0, formattedValue, formattedValue.length - charsNumber - appendedText.length
                    - prependedText.length, prependedText.length);
        }
    }
}
