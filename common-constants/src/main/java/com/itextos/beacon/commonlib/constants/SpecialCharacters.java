package com.itextos.beacon.commonlib.constants;

import java.util.ArrayList;
import java.util.List;

public abstract class SpecialCharacters
{

    private SpecialCharacters()
    {}

    private static List<Character> splCharList = new ArrayList<>();

    static
    {
        final char   registredTradeMark = '�';
        final String specialCharacters  = "^{}\\[]~|";
        for (final char c : specialCharacters.toCharArray())
            splCharList.add(c);
        splCharList.add(registredTradeMark);
    }

    public static boolean isSpecialCharacter(
            char character)
    {
        return splCharList.contains(character);
    }

    public static List<Character> getSpecialCharList()
    {
        return splCharList;
    }

    public static void main(
            String[] args)
    {
        System.out.println(getSpecialCharList());
    }

}