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
package io.github.vincentjames501.codeowners.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import nl.basjes.codeowners.CodeOwners;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static picocli.CommandLine.Help.Visibility.ALWAYS;

/**
 * @author vincentjames501
 * @version 0.0.1
 * @since 2024-July-15
 */
@Command(name = "verify", description = "Verifies the format of the CODEOWNERS file.", subcommands = CommandLine.HelpCommand.class)
public class Verify implements Callable<Integer> {
    @Option(names = { "-c",
            "--codeowners-file" }, description = "Specify the path to the CODEOWNERS file.", defaultValue = "./CODEOWNERS", required = true, showDefaultValue = ALWAYS)
    Path codeownersFile;

    @Override
    public Integer call() {
        if (!Files.exists(codeownersFile)) {
            throw new IllegalArgumentException(String.format("CODEOWNERS not found: %s", codeownersFile));
        }
        try {
            CodeOwners codeOwners = new CodeOwners(codeownersFile.toFile());
            if (codeOwners.hasStructuralProblems()) {
                throw new RuntimeException("CodeOwners has structural issues!");
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("CODEOWNERS file is valid.");
        return 0;
    }
}