package com.architek.oikos.property.application.usecase;

import java.util.Comparator;
import java.util.function.Function;

import com.architek.oikos.shared.domain.pagination.SortDirection;

/**
 * Comparisons shared by the listings that sort in memory (lots, contacts).
 */
final class NaturalOrder {

    private NaturalOrder() {
    }

    /**
     * Digit runs compared as numbers, so lot A2 comes before A10. Plain
     * lexicographic order would put A10 in between A1 and A2, which reads as a
     * broken sort on the very column a syndic scans most. Case-insensitive.
     */
    static int compare(String left, String right) {
        if (left == null || right == null) {
            return left == null ? (right == null ? 0 : -1) : 1;
        }
        int i = 0;
        int j = 0;
        while (i < left.length() && j < right.length()) {
            char leftChar = left.charAt(i);
            char rightChar = right.charAt(j);
            if (Character.isDigit(leftChar) && Character.isDigit(rightChar)) {
                int startI = i;
                int startJ = j;
                while (i < left.length() && Character.isDigit(left.charAt(i))) {
                    i++;
                }
                while (j < right.length() && Character.isDigit(right.charAt(j))) {
                    j++;
                }
                // Compared as text once the leading zeroes are gone: a lot number
                // can be longer than an int.
                String leftDigits = left.substring(startI, i).replaceFirst("^0+(?=.)", "");
                String rightDigits = right.substring(startJ, j).replaceFirst("^0+(?=.)", "");
                int byLength = Integer.compare(leftDigits.length(), rightDigits.length());
                if (byLength != 0) {
                    return byLength;
                }
                int byValue = leftDigits.compareTo(rightDigits);
                if (byValue != 0) {
                    return byValue;
                }
            } else {
                int byChar = Character.compare(Character.toLowerCase(leftChar), Character.toLowerCase(rightChar));
                if (byChar != 0) {
                    return byChar;
                }
                i++;
                j++;
            }
        }
        return Integer.compare(left.length() - i, right.length() - j);
    }

    /**
     * Orders on {@code key} in the asked direction, rows without a value last
     * whichever way it runs: an empty floor or a lot with no owner leading a
     * descending sort would bury the rows the reader is looking for.
     */
    static <T, K> Comparator<T> byKey(Function<T, K> key, Comparator<K> order, SortDirection direction) {
        Comparator<K> directed = direction == SortDirection.DESC ? order.reversed() : order;
        return (left, right) -> {
            K leftKey = key.apply(left);
            K rightKey = key.apply(right);
            if (leftKey == null || rightKey == null) {
                return leftKey == null ? (rightKey == null ? 0 : 1) : -1;
            }
            return directed.compare(leftKey, rightKey);
        };
    }
}
