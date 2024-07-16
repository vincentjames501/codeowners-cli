/*
 *  Copyright (c) 2024 Vincent Pizzo
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.vincentjames501.codeowners;

import java.util.concurrent.Callable;

import org.vincentjames501.codeowners.commands.ListCodeOwners;
import org.vincentjames501.codeowners.commands.Verify;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi.Style;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.HelpCommand;

/**
 * @author vincentjames501
 * @version 0.0.1
 * @since 2024-July-15
 */
@Command(name = "codeowners-cli", mixinStandardHelpOptions = true, version = "codeowners-cli 0.0.1", description = "Process CODEOWNER files", subcommands = {
        HelpCommand.class,
        ListCodeOwners.class,
        Verify.class
})
public class CodeOwnersCLI implements Callable<Integer> {
    /**
     * Main function to invoke the picocli framework.
     *
     * @param args the command line arguments.
     *             The first argument is the file whose checksum to calculate.
     *             The second argument is the algorithm to use.
     *             The default algorithm is MD5.
     */
    public static void main(String[] args) {
        CommandLine codeOwnersCLI = new CommandLine(new CodeOwnersCLI());
        int exitCode = codeOwnersCLI
                .setColorScheme(codeOwnersCLI.getColorScheme())
                .execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        CommandLine.usage(this, System.out);
        return 0;
    }

    public static CommandLine.Help.ColorScheme getColorScheme() {
        return new ColorScheme.Builder()
                .commands(Style.bold, Style.underline)
                .options(Style.fg_yellow)
                .parameters(Style.fg_yellow)
                .optionParams(Style.italic)
                .errors(Style.fg_red, Style.bold)
                .stackTraces(Style.italic)
                .build();
    }
}
