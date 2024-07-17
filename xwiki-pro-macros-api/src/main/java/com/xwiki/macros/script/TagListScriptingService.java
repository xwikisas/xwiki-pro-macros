/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.xwiki.macros.script;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.script.service.ScriptService;
import org.xwiki.stability.Unstable;

/**
 * Tools to manipulate the data needed for the tagList macro.
 *
 * @version $Id$
 * @since 1.20.0
 */
@Component
@Named("taglist")
@Singleton
@Unstable
public class TagListScriptingService implements ScriptService
{
    private static final String DIGIT_BLOCK = "DIGITS";

    private static final String SEPARATOR = ",";

    private static final String QUOTES = "'";

    /**
     * Transforms the input of a user in the right format.
     *
     * @param userInput string with spaces
     * @return returns the user input in the right format
     */
    public String parseSpaces(String userInput)
    {
        StringBuilder builder = new StringBuilder();
        String[] spaces = userInput.split(SEPARATOR);
        for (int i = 0; i < spaces.length; i++) {
            String space = spaces[i];
            builder.append(QUOTES);
            builder.append(space.trim());
            builder.append(QUOTES);
            if (i < spaces.length - 1) {
                builder.append(SEPARATOR);
            }
        }
        return builder.toString();
    }

    /**
     * Handles the separation of tags into bins.
     *
     * @param tags list of tags
     * @return the bins formed by the tags
     */
    public Map<String, List<String>> getBins(List<String> tags)
    {

        // The tags list will always be sorted because it is created from a TreeSet.
        Map<String, List<String>> finalBins = new TreeMap<>();
        StringBuilder key = new StringBuilder();
        char binSeparator = '-';

        // We want to split the tags into multiple bins and determine the right size for the bins dynamically without
        // hardcoding the value. To do this, we need a formula, and I've chosen to use the Rice rule since it is the
        // simplest. More info: https://medium.com/@maxmarkovvision/optimal-number-of-bins-for-histograms-3d7c48086fde
        int totalEntries = tags.size();
        int binLimit = (int) (2 * Math.round(Math.pow(totalEntries, 1.0 / 3.0)));

        char firstCh = (char) -1;
        char previousCh = (char) -1;
        char currentCh;
        List<String> binTags = new ArrayList<>();
        int current = 0;
        // Find the first tag that doesn't start with a symbol.
        int start = 0;

        for (; start < tags.size(); start++) {
            // Finish when we find our first tag that starts with a letter or a digit.
            if (Character.isLetterOrDigit(tags.get(start).charAt(0))) {
                firstCh = tags.get(start).charAt(0);
                break;
            }
        }

        for (int i = start; i < tags.size(); i++) {
            currentCh = tags.get(i).charAt(0);
            // Skip any symbol
            if (!Character.isLetterOrDigit(currentCh)) {
                continue;
            }

            String tag = tags.get(i);
            // We finished a bin
            if (makeBin(current, binLimit, previousCh, currentCh)) {
                current = 0;
                addBin(finalBins, key, binSeparator, firstCh, previousCh, binTags);
                binTags = new ArrayList<>();
                firstCh = currentCh;
            }
            binTags.add(tag);
            previousCh = currentCh;
            current++;
        }
        // Add the remaining tags to the final bin
        addBin(finalBins, key, binSeparator, firstCh, previousCh, binTags);
        return finalBins;
    }

    /**
     * Manages the process of identifying the Unicode block to which a character belongs.
     *
     * @param ch character for which we want to find the block
     * @return block name
     */
    public String getBinName(char ch)
    {
        // We are using and else if because the digits are considered BASIC_LATIN and will not get a separate bin.
        if (Character.isLetter(ch)) {
            return Character.UnicodeBlock.of(ch).toString();
        } else if (Character.isDigit(ch)) {
            return DIGIT_BLOCK;
        }
        return null;
    }

    /**
     * Determines if a bin is full.
     *
     * @param current current elements in the bin
     * @param binLimit limit of a bin
     * @param previousCh previous character of the block
     * @param currentCh current character of the block
     * @return ture if we should jump to the next bin
     */
    private boolean makeBin(int current, int binLimit, char previousCh, char currentCh)
    {
        return (current >= binLimit && previousCh != currentCh) || (previousCh != (char) -1 && !getBinName(
            currentCh).equals(getBinName(previousCh)));
    }

    /**
     * Handles the addition of a bin.
     *
     * @param finalBins map with the final bins
     * @param key string buffer
     * @param binSeparator separator of the key
     * @param firstCh first character of the key
     * @param previousCh last character of the key
     * @param binTags list with tags
     */
    private void addBin(Map<String, List<String>> finalBins, StringBuilder key, char binSeparator, char firstCh,
        char previousCh, List<String> binTags)
    {
        if (binTags.isEmpty()) {
            return;
        }
        key.setLength(0);
        key.append(Character.toUpperCase(firstCh));
        if (previousCh != firstCh) {
            key.append(binSeparator);
            key.append(Character.toUpperCase(previousCh));
        }
        finalBins.put(key.toString(), binTags);
    }
}
