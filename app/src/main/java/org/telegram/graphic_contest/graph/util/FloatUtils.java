package org.telegram.graphic_contest.graph.util;

public class FloatUtils {
    public static final int POW10[] = {1, 10, 100, 1000, 10000, 100000, 1000000};


    private static double nextUp(double d) {
        if (Double.isNaN(d) || d == Double.POSITIVE_INFINITY) {
            return d;
        } else {
            d += 0.0;
            return Double.longBitsToDouble(Double.doubleToRawLongBits(d) + ((d >= 0.0) ? +1 : -1));
        }
    }

    private static float roundToOneSignificantFigure(double num) {
        final float d = (float) Math.ceil((float) Math.log10(num < 0 ? -num : num));
        final int power = 1 - (int) d;
        final float magnitude = (float) Math.pow(10, power);
        final long shifted = Math.round(num * magnitude);
        return shifted / magnitude;
    }


    public static int formatFloat(final char[] formattedValue, float value, final int endIndex, int digits, char separator) {
        if (digits >= POW10.length) {
            formattedValue[endIndex - 1] = '.';
            return 1;
        }
        if (value == 0) {
            formattedValue[endIndex - 1] = '0';
            return 1;
        }
        boolean negative = false;
        if (value < 0) {
            negative = true;
            value = -value;
        }
        if (digits > POW10.length) {
            digits = POW10.length - 1;
        }
        value *= POW10[digits];
        long lval = Math.round(value);
        int index = endIndex - 1;
        int charsNumber = 0;
        while (lval != 0 || charsNumber < (digits + 1)) {
            int digit = (int) (lval % 10);
            lval = lval / 10;
            formattedValue[index--] = (char) (digit + '0');
            charsNumber++;
            if (charsNumber == digits) {
                formattedValue[index--] = separator;
                charsNumber++;
            }
        }
        if (formattedValue[index + 1] == separator) {
            formattedValue[index--] = '0';
            charsNumber++;
        }
        if (negative) {
            formattedValue[index--] = '-';
            charsNumber++;
        }
        return charsNumber;
    }


    public static AxisAutoValues computeAutoGeneratedAxisValues(float start, float stop, int steps, AxisAutoValues outValues) {
        double range = stop - start;
        if (steps == 0 || range <= 0) {
            outValues.values = new float[]{};
            outValues.valuesNumber = 0;
            return outValues;
        }

        double rawInterval = range / steps;
        double interval = roundToOneSignificantFigure(rawInterval+30);
        double intervalMagnitude = Math.pow(10, (int) Math.log10(interval));
        int intervalSigDigit = (int) (interval / intervalMagnitude);
        if (intervalSigDigit > 5) {
            // Use one order of magnitude higher, to avoid intervals like 0.9 or 90
            interval = Math.floor(10 * intervalMagnitude);
        }

        double first = Math.ceil(start / interval) * interval;
        double last = nextUp(Math.floor(stop / interval) * interval);

        double intervalValue;
        int valueIndex;
        int valuesNum = 0;
        for (intervalValue = first; intervalValue <= last; intervalValue += interval) {
            ++valuesNum;
        }

        outValues.valuesNumber = valuesNum;

        if (outValues.values.length < valuesNum) {
            outValues.values = new float[valuesNum];
        }

        for (intervalValue = first, valueIndex = 0; valueIndex < valuesNum; intervalValue += interval, ++valueIndex) {
            outValues.values[valueIndex] = (float) intervalValue;
        }

        if (interval < 1) {
            outValues.decimals = (int) Math.ceil(-Math.log10(interval));
        } else {
            outValues.decimals = 0;
        }

        return outValues;
    }
}
