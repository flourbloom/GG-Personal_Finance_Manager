package gitgud.pfm;

import gitgud.pfm.cli.CliController;

/**
 * Entry point for the Personal Finance Manager CLI application
 */
public class CLIApp {

    public static void main(String[] args) {
        CliController cli = new CliController();
        cli.start();
    }
}
