/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.cli.internal;

import net.sourceforge.pmd.AbstractConfiguration;

/**
 * The execution result of any given command.
 */
public enum CliExitCode {
    /** No errors, no processing errors, no violations. This is exit code {@code 0}. */
    OK(0),
    /**
     * Unexpected errors were detected, PMD may have not run to the end.
     * This is exit code {@code 1}.
     */
    ERROR(1),
    /**
     * Indicates a problem with the CLI parameters: either a required
     * parameter is missing or an invalid parameter was provided.
     */
    USAGE_ERROR(2),
    /**
     * No errors, but PMD found either duplications/violations or couldn't analyze all
     * files due to parsing/lexing problems. This is exit code {@code 4}.
     *
     * <p>This is only returned if {@link AbstractConfiguration#isFailOnViolation()}
     * is set. It can be disabled by using CLI flag {@code --no-fail-on-violation}.
     */
    VIOLATIONS_FOUND(4),
    /**
     * PMD did run, but there was at least one processing error. There
     * might be additionally duplications or violations. This is exit code {@code 5}.
     *
     * <p>This is only returned if {@link AbstractConfiguration#isFailOnProcessingError()}
     * is set. It can be disabled by using CLI flag {@code --no-fail-on-processing-error}.
     */
    PROCESSING_ERRORS_OR_VIOLATIONS(5);

    private final int exitCode;

    CliExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }

    public static CliExitCode fromInt(int i) {
        switch (i) {
        case 0: return OK;
        case 1: return ERROR;
        case 2: return USAGE_ERROR;
        case 4: return VIOLATIONS_FOUND;
        case 5: return PROCESSING_ERRORS_OR_VIOLATIONS;
        default:
            throw new IllegalArgumentException("Not a known exit code: " + i);
        }
    }
}
