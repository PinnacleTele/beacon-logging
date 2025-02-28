package com.itextos.beacon.commonlib.ipvalidation;

public class TestIPValidation
{

    public static void main(
            String[] args)
    {
        final int choice = Integer.parseInt(args[0]);

        switch (choice)
        {
            case 1:
                testCase1();
                break;

            case 2:
                testCase2();
                break;

            case 3:
                testCase3();
                break;

            case 4:
                testCase4();
                break;

            case 5:
                testCase5();
                break;

            case 6:
                testCase6();
                break;

            case 7:
                testCase7();
                break;

            case 8:
                testCase8();
                break;

            case 9:
                testCase9();
                break;

            case 10:
                testCase10();
                break;

            case 11:
                testCase11();
                break;

            case 12:
                testCase12();
                break;

            case 13:
                testCase13();
                break;

            case 14:
                testCase14();
                break;

            case 15:
                testCase15();
                break;

            case 16:
                testCase16();
                break;

            case 17:
                testCase17();
                break;

            case 18:
                testCase18();
                break;

            case 19:
                testCase19();
                break;

            case 20:
                testCase20();
                break;

            case 21:
                testCase21();
                break;

            case 22:
                testCase22();
                break;

            case 23:
                testCase23();
                break;

            case 24:
                testCase24();
                break;

            case 25:
                testCase25();
                break;

            case 26:
                testCase26();
                break;

            case 27:
                testCase27();
                break;

            case 28:
                testCase28();
                break;

            case 29:
                testCase29();
                break;

            case 30:
                testCase30();
                break;

            case 31:
                testCase31();
                break;

            case 32:
                testCase32();
                break;

            case 33:
                testCase33();
                break;

            case 34:
                testCase34();
                break;

            case 35:
                testCase35();
                break;

            default:
                System.err.println("Invalid choice");
        }
    }

    private static void testCase1()
    {
        final String  lClientId = null;
        final String  dbIPs     = null;
        final String  clientIP  = null;
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        System.out.println("Test 1 Is Valid:" + validIP);
    }

    private static void testCase2()
    {
        final String  lClientId = "";
        final String  dbIPs     = "";
        final String  clientIP  = "";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        System.out.println("Test 2 Is Valid:" + validIP);
    }

    private static void testCase3()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "*";
        final String  clientIP  = "";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        System.out.println("Test 3 Is Valid:" + validIP);
    }

    private static void testCase4()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "*";
        final String  clientIP  = "10.20.50.60";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        System.out.println("Test 4 Is Valid:" + validIP);
    }

    private static void testCase5()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "*";
        final String  clientIP  = "10.20.50.";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        System.out.println("Test 5 Is Valid:" + validIP);
    }

    private static void testCase6()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "*";
        final String  clientIP  = "10.20.50.abc";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        System.out.println("Test 6 Is Valid:" + validIP);
    }

    private static void testCase7()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "*";
        final String  clientIP  = "10.20.50.60";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        System.out.println("Test 7 Is Valid:" + validIP);
    }

    private static void testCase8()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "10.20.50.60";
        final String  clientIP  = "10.20.50.60";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        System.out.println("Test 8 Is Valid:" + validIP);
    }

    private static void testCase9()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "10.20.50.60";
        final String  clientIP  = "10.20.50.61";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        System.out.println("Test 9 Is Valid:" + validIP);
    }

    private static void testCase10()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "10.20.50.60";
        final String  clientIP  = "10.20.50.60";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        final String  clientIP1 = "10.20.50.61";
        final boolean validIP1  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP1);
        System.out.println("Test 10 Is Valid :" + validIP);
        System.out.println("Test 10 Is Valid1:" + validIP1);
    }

    private static void testCase11()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "10.20.50.601";
        final String  clientIP  = "10.20.50.60";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        final String  clientIP1 = "10.20.50.61";
        final boolean validIP1  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP1);
        System.out.println("Test 11 Is Valid :" + validIP);
        System.out.println("Test 11 Is Valid1:" + validIP1);
    }

    private static void testCase12()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "10.20.50.601";
        final String  clientIP  = "10.20.50.60";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        final String  dbIPs1    = "10.20.50.61";
        final String  clientIP1 = "10.20.50.61";
        final boolean validIP1  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs1, clientIP1);
        System.out.println("Test 12 Is Valid :" + validIP);
        System.out.println("Test 12 Is Valid1:" + validIP1);
    }

    private static void testCase13()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "192.168.43.3";
        final String  clientIP  = "fe80::747b:2822:ca78:b33f";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        System.out.println("Test 13 Is Valid :" + validIP);
    }

    private static void testCase14()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "fe80::747b:2822:ca78:b33f";
        final String  clientIP  = "fe80::747b:2822:ca78:b33f";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        System.out.println("Test 14 Is Valid :" + validIP);
    }

    private static void testCase15()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "10.20.50.abc";
        final String  clientIP  = "10.20.50.61";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        final String  dbIPs1    = "10.20.50.61";
        final String  clientIP1 = "10.20.50.61";
        final boolean validIP1  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs1, clientIP1);
        System.out.println("Test 15 Is Valid :" + validIP);
        System.out.println("Test 15 Is Valid1:" + validIP1);
    }

    private static void testCase16()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "10.20.50.10-10.20.50.60";
        final String  clientIP  = "10.20.50";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        final String  dbIPs1    = "10.20.50.61";
        final String  clientIP1 = "10.20.50.61";
        final boolean validIP1  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs1, clientIP1);
        System.out.println("Test 16 Is Valid :" + validIP);
        System.out.println("Test 16 Is Valid1:" + validIP1);
    }

    private static void testCase17()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "10.20.50.10-10.20.50.60";
        final String  clientIP  = "10.20.50";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        final String  dbIPs1    = "10.20.50.10-10.20.50.60";
        final String  clientIP1 = "10.20.50.61";
        final boolean validIP1  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs1, clientIP1);
        System.out.println("Test 17 Is Valid :" + validIP);
        System.out.println("Test 17 Is Valid1:" + validIP1);
    }

    private static void testCase18()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "10.20.50.10-10.20.51.60";
        final String  clientIP  = "10.20.50.60";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        final String  dbIPs1    = "10.20.50.10-10.20.50.60";
        final String  clientIP1 = "10.20.50.60";
        final boolean validIP1  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs1, clientIP1);
        System.out.println("Test 18 Is Valid :" + validIP);
        System.out.println("Test 18 Is Valid1:" + validIP1);
    }

    private static void testCase19()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "10.20.50.10-10.20.50.60";
        final String  clientIP  = "10.20.50.61";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        final String  clientIP1 = "10.20.50.60";
        final boolean validIP1  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP1);
        System.out.println("Test 19 Is Valid :" + validIP);
        System.out.println("Test 19 Is Valid1:" + validIP1);
    }

    private static void testCase20()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "10.20.50.10-10.20.50.60";
        final String  clientIP  = "10.20.qwe.61";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        System.out.println("Test 20 Is Valid :" + validIP);
    }

    private static void testCase21()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "10.20.50.10/24";
        final String  clientIP  = "10.20.qwe.61";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        System.out.println("Test 21 Is Valid :" + validIP);
    }

    private static void testCase22()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "10.20.50.10/24";
        final String  clientIP  = "10.20.50.61";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        System.out.println("Test 22 Is Valid :" + validIP);
    }

    private static void testCase23()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "10.20.50.10/24";
        final String  clientIP  = "10.20.50.61";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        System.out.println("Test 23 Is Valid :" + validIP);
        final String  clientIP1 = "10.20.50.62";
        final boolean validIP1  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP1);
        System.out.println("Test 23 Is Valid :" + validIP1);
    }

    private static void testCase24()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "10.20.50.10,10.20.50.20,10.20.50.30";
        final String  clientIP  = "10.20.50.61";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        System.out.println("Test 24 Is Valid :" + validIP);
        final String  clientIP1 = "10.20.50.20";
        final boolean validIP1  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP1);
        System.out.println("Test 24 Is Valid :" + validIP1);
    }

    private static void testCase25()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "10.20.50.10,10.20.50.20 ,10.20.50.30";
        final String  clientIP  = "10.20.50.10";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        System.out.println("Test 25 Is Valid :" + validIP);
        final String  clientIP1 = "10.20.50.20";
        final boolean validIP1  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP1);
        System.out.println("Test 25 Is Valid :" + validIP1);
    }

    private static void testCase26()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "10.20.50.10,10.20.50.20 ,10.20.50.30";
        final String  clientIP  = "10.20.50.10";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        System.out.println("Test 26 Is Valid :" + validIP);
        final String  dbIPs1    = "10.20.50.10,10.20.50.20,10.20.50.30";
        final String  clientIP1 = "10.20.50.20";
        final boolean validIP1  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs1, clientIP1);
        System.out.println("Test 26 Is Valid :" + validIP1);
    }

    private static void testCase27()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "10.20.50.10,,10.20.50.30";
        final String  clientIP  = "10.20.50.10";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        System.out.println("Test 27 Is Valid :" + validIP);
        final String  dbIPs1    = "10.20.50.10,10.20.50.20,";
        final String  clientIP1 = "10.20.50.20";
        final boolean validIP1  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs1, clientIP1);
        System.out.println("Test 27 Is Valid :" + validIP1);
        final String  dbIPs2    = "10.20.50.10,10.20.50.20,10.20.50.30";
        final String  clientIP2 = "10.20.50.20";
        final boolean validIP2  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs2, clientIP2);
        System.out.println("Test 27 Is Valid :" + validIP2);
    }

    private static void testCase28()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "10.20.50.10,,10.20.50.30,10.20.50.60-10.20.50.70,10.20.55.1/24";
        final String  clientIP  = "10.20.50.10";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        System.out.println("Test 28 Is Valid :" + validIP);
        final String  clientIP1 = "10.20.50.60";
        final boolean validIP1  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP1);
        System.out.println("Test 28 Is Valid :" + validIP1);
        final String  clientIP2 = "10.20.55.20";
        final boolean validIP2  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP2);
        System.out.println("Test 28 Is Valid :" + validIP2);
        final String  clientIP3 = "10.20.55.0";
        final boolean validIP3  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP3);
        System.out.println("Test 28 Is Valid :" + validIP3);
        final String  clientIP4 = "10.20.50.50";
        final boolean validIP4  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP4);
        System.out.println("Test 28 Is Valid :" + validIP4);
    }

    private static void testCase29()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "10.20.50.100-10.20.50.60";
        final String  clientIP  = "10.20.50.61";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        System.out.println("Test 29 Is Valid :" + validIP);
        final String  clientIP1 = "10.20.50.61";
        final boolean validIP1  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP1);
        System.out.println("Test 29 Is Valid :" + validIP1);
        final String  dbIPs2    = "10.20.50.60-10.20.50.100";
        final String  clientIP2 = "10.20.50.61";
        final boolean validIP2  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs2, clientIP2);
        System.out.println("Test 29 Is Valid :" + validIP2);
    }

    private static void testCase30()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "10.20.50.60 - 10.20.50.100";
        final String  clientIP  = "10.20.50.61";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        System.out.println("Test 30 Is Valid :" + validIP);
    }

    private static void testCase31()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "10.20.50.10 ,, 10.20.50.30, 10.20.50.60-10.20.50.70, 10.20.51.60 - 10.20.51.70,10.20.55.1/ 24, ,10.20.35.1 / 24";
        final String  clientIP  = "10.20.50.10";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        System.out.println("Test 31 Is Valid :" + validIP);
        final String  clientIP1 = "10.20.50.60";
        final boolean validIP1  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP1);
        System.out.println("Test 31 Is Valid :" + validIP1);
        final String  clientIP2 = "10.20.55.20";
        final boolean validIP2  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP2);
        System.out.println("Test 31 Is Valid :" + validIP2);
        final String  clientIP3 = "10.20.55.0";
        final boolean validIP3  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP3);
        System.out.println("Test 31 Is Valid :" + validIP3);
        final String  clientIP4 = "10.20.50.50";
        final boolean validIP4  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP4);
        System.out.println("Test 31 Is Valid :" + validIP4);
    }

    private static void testCase32()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "10.20.50.10 ,, 10.20.50.30, 10.20.50.60-10.20.50.70, 10.20.51.60 - 10.20.51.70, 10.20.55.1/24, ,10.20.35.1/24 ";
        final String  clientIP  = "10.20.50.10";
        final boolean validIP   = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP);
        System.out.println("Test 32 Is Valid :" + validIP);
        final String  clientIP1 = "10.20.50.60";
        final boolean validIP1  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP1);
        System.out.println("Test 32 Is Valid :" + validIP1);
        final String  clientIP2 = "10.20.55.20";
        final boolean validIP2  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP2);
        System.out.println("Test 32 Is Valid :" + validIP2);
        final String  clientIP3 = "10.20.55.0";
        final boolean validIP3  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP3);
        System.out.println("Test 32 Is Valid :" + validIP3);
        final String  clientIP4 = "10.20.50.50";
        final boolean validIP4  = IPValidator.getInstance().isValidIP("1", lClientId, dbIPs, clientIP4);
        System.out.println("Test 32 Is Valid :" + validIP4);
    }

    private static void testCase33()
    {
        final String  lClientId = null;
        final String  dbIPs     = null;
        final String  clientIP  = null;
        final boolean validIP   = IPValidator.getInstance().isValidIP("0", lClientId, dbIPs, clientIP);
        System.out.println("Test 33 Is Valid:" + validIP);
    }

    private static void testCase34()
    {
        final String  lClientId = "";
        final String  dbIPs     = "";
        final String  clientIP  = "";
        final boolean validIP   = IPValidator.getInstance().isValidIP("0", lClientId, dbIPs, clientIP);
        System.out.println("Test 34 Is Valid:" + validIP);
    }

    private static void testCase35()
    {
        final String  lClientId = "Kumarapandian";
        final String  dbIPs     = "";
        final String  clientIP  = "";
        final boolean validIP   = IPValidator.getInstance().isValidIP("0", lClientId, dbIPs, clientIP);
        System.out.println("Test 25 Is Valid:" + validIP);
    }

}
