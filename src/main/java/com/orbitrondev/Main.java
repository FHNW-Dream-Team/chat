package com.orbitrondev;

import com.orbitrondev.Controller.CliController;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;

public class Main {
    public static boolean connectToDb = true;
    public static String dbLocation = "chat.sqlite3";
    public static boolean isGui = false;

    public static void main(String[] args) {
        Options options = new Options();

        Option guiOption = new Option("g", "no-gui", true, "Disable the GUI and use the console");
        guiOption.setOptionalArg(true);
        options.addOption(guiOption);

        Option dbOption = new Option("d", "no-db", true, "Disable the database usage");
        dbOption.setOptionalArg(true);
        options.addOption(dbOption);

        Option dbLocationOption = new Option("l", "db-location", true, "Define where the database is saved");
        dbLocationOption.setOptionalArg(true);
        options.addOption(dbLocationOption);

        Option verboseOption = new Option("v", "verbose", true, "Show more extensive logs");
        verboseOption.setOptionalArg(true);
        options.addOption(verboseOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("chat.jar", options);

            System.exit(1);
        }
        if (cmd == null) {
            System.exit(1);
        }

        if (cmd.hasOption("no-db")) {
            connectToDb = false;
        }
        if (cmd.hasOption("db-location")) {
            dbLocation = cmd.getOptionValue("db-location");
        }
        if (cmd.hasOption("verbose")) {
            final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            final Configuration config = ctx.getConfiguration();
            config.getRootLogger().addAppender(config.getAppender("Console"), Level.INFO, null);
            ctx.updateLoggers();
        }

        if (!cmd.hasOption("no-gui")) {
            isGui = true;
            MainGui.main(args);
        } else {
            new CliController();
        }
        // TODO: There is a bug inside IntelliJ which doesn't close the application, so we do it manually
        System.exit(0);
    }
}
