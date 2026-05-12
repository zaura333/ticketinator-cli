package com.ticketsystem.cli;

import java.util.Scanner;

/**
 * Contract for all CLI workflow handlers.
 * Each workflow (A–P) is a self-contained class that interacts
 * with the user via the provided {@link Scanner}.
 */
public interface Workflow {

    /**
     * Executes the workflow, guiding the user through all steps.
     *
     * @param scanner shared console input reader
     */
    void execute(Scanner scanner);
}
