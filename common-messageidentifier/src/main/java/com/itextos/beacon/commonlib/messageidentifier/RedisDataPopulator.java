package com.itextos.beacon.commonlib.messageidentifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextos.beacon.errorlog.RedisDataPopulatorLog;

public class RedisDataPopulator
{

    private static final Log    log                                    = LogFactory.getLog(RedisDataPopulator.class);

    private static final String CHOICE_POPULATE_NEW_INSTANCE_ID        = "1";
    private static final String CHOICE_RESET_ALL_EXISTING_ENTRIES      = "2";
    private static final String CHOICE_RESET_EXISITING_EXPIRED_ENTRIES = "3";
    private static final String CHOICE_EXITING_APPLICATION             = "0";

    public static void main(
            String[] args)
    {
        MessageIdentifierProperties.getInstance();

        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)))
        {
            System.out.println("Hi Enter your name ...");

            String       userName = reader.readLine();
            final String ip       = InetAddress.getLocalHost().getHostAddress();

            if ((userName == null) || "".equals(userName.trim()))
            {
                final String s = "IP Used : '" + ip + "' User entered an invalid name. User Entered : '" + userName + "'. Cannot continue... Exiting the application ...";
                log.error(s);
                System.err.println(s);
                System.exit(-1);
                return;
            }

            userName = userName.toUpperCase();

            if (log.isInfoEnabled())
                log.info("User : '" + userName + "' from IP : '" + ip + "' logged in.");

            final String userInfo = "IP : '" + ip + "' User : '" + userName + "': ";
            String       choice   = readInput(userName, reader);

            while (true)
            {

                switch (choice)
                {
                    case CHOICE_POPULATE_NEW_INSTANCE_ID:
                        if (log.isInfoEnabled())
                            log.info(userInfo + "Populate new instance ID");
                        final InstanceIDPopulator instanceIDPopulator = new InstanceIDPopulator(userName, ip, reader);
                        instanceIDPopulator.process();
                        break;

                    case CHOICE_RESET_ALL_EXISTING_ENTRIES:
                        if (log.isInfoEnabled())
                            log.info(userInfo + "Reset all existing entries");
                        final RemoveRedisEntries removeRedisEntries = new RemoveRedisEntries(userName, ip, true);
                        removeRedisEntries.removeEntries();
                        break;

                    case CHOICE_RESET_EXISITING_EXPIRED_ENTRIES:
                        if (log.isInfoEnabled())
                            log.info(userInfo + "Reset existing expired entries");
                        final RemoveRedisEntries removeEntries1 = new RemoveRedisEntries(userName, ip, false);
                        removeEntries1.removeEntries();
                        break;

                    case CHOICE_EXITING_APPLICATION:
                        if (log.isInfoEnabled())
                            log.info(userInfo + "Choice entered to exit the application");
                        System.out.println("Exiting the application." + MessageIdentifierConstants.NEW_LINE + MessageIdentifierConstants.NEW_LINE + "Press 'Enter' / 'Return' key to exit ....");
                        reader.read();
                        System.exit(0);
                        break;

                    default:
                        final String s = userInfo + "Invalid entry. Choice Provided : '" + choice + "'";
                        log.error(s);
                        System.err.println("Hi " + userName + ", You have provided an invalid entry. Please try again.");
                }

                Thread.sleep(10);

                choice = readInput(userName, reader);
            }
        }
        catch (final Exception e)
        {
            log.error("Something went wrong", e);
        }
    }

    public static void main()
    {
        MessageIdentifierProperties.getInstance();

        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)))
        {
            System.out.println("Hi Enter your name ...");

            String       userName = "Jaffer Sadhik";
            final String ip       = InetAddress.getLocalHost().getHostAddress();

            RedisDataPopulatorLog.log("userName : "+userName);
            RedisDataPopulatorLog.log("ip : "+ip);

            userName = userName.toUpperCase();

            if (log.isInfoEnabled())
                log.info("User : '" + userName + "' from IP : '" + ip + "' logged in.");

            final String userInfo = "IP : '" + ip + "' User : '" + userName + "': ";
            String       choice   = CHOICE_POPULATE_NEW_INSTANCE_ID;

            RedisDataPopulatorLog.log("CHOICE_POPULATE_NEW_INSTANCE_ID : "+CHOICE_POPULATE_NEW_INSTANCE_ID);

            while (true)
            {

                switch (choice)
                {
                    case CHOICE_POPULATE_NEW_INSTANCE_ID:
                        if (log.isInfoEnabled())
                            log.info(userInfo + "Populate new instance ID");
                        final InstanceIDPopulatorStatic instanceIDPopulator = new InstanceIDPopulatorStatic(userName, ip);
                        instanceIDPopulator.process();
                        break;

                    case CHOICE_RESET_ALL_EXISTING_ENTRIES:
                        if (log.isInfoEnabled())
                            log.info(userInfo + "Reset all existing entries");
                        final RemoveRedisEntries removeRedisEntries = new RemoveRedisEntries(userName, ip, true);
                        removeRedisEntries.removeEntries();
                        break;

                    case CHOICE_RESET_EXISITING_EXPIRED_ENTRIES:
                        if (log.isInfoEnabled())
                            log.info(userInfo + "Reset existing expired entries");
                        final RemoveRedisEntries removeEntries1 = new RemoveRedisEntries(userName, ip, false);
                        removeEntries1.removeEntries();
                        break;

                    case CHOICE_EXITING_APPLICATION:
                        if (log.isInfoEnabled())
                            log.info(userInfo + "Choice entered to exit the application");
                        System.out.println("Exiting the application." + MessageIdentifierConstants.NEW_LINE + MessageIdentifierConstants.NEW_LINE + "Press 'Enter' / 'Return' key to exit ....");
                        reader.read();
                        System.exit(0);
                        break;

                    default:
                        final String s = userInfo + "Invalid entry. Choice Provided : '" + choice + "'";
                        log.error(s);
                        System.err.println("Hi " + userName + ", You have provided an invalid entry. Please try again.");
                }

                Thread.sleep(10);

                choice = readInput(userName, reader);
            }
        }
        catch (final Exception e)
        {
            log.error("Something went wrong", e);
        }
    }


    
    private static String readInput(
            String aUserName,
            BufferedReader aReader)
            throws IOException
    {
        System.out.println(MessageIdentifierConstants.NEW_LINE + "Hi " + aUserName + ", Checkout the list " + MessageIdentifierConstants.NEW_LINE);
        System.out.println("1. Populate new instance id range");
        System.out.println("2. Reset all existing entries");
        System.out.println("3. Reset existing expired entries");
        System.out.println("0. Exit the application");
        System.out.print(MessageIdentifierConstants.NEW_LINE + "Enter your choice ... :");
        return aReader.readLine();
    }

}