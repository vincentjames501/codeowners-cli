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
package org.vincentjames501.codeowners.commands;

import nl.basjes.codeowners.CodeOwners;
import nl.basjes.gitignore.GitIgnore;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author vincentjames501
 * @version 0.0.1
 * @since 2024-July-15
 */
@Command(name = "verify", description = "Verifies the format of the CODEOWNERS file", subcommands = CommandLine.HelpCommand.class)
public class Verify implements Callable<Integer> {
    @Option(names = {"-cf", "--codeowners-file"}, description = "Specify the path to the CODEOWNERS file.")
    Path codeownersFile = Paths.get("./CODEOWNERS");

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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("CODEOWNERS file is valid.");
        return 0;
    }
}
