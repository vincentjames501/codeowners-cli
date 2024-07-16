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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import nl.basjes.codeowners.CodeOwners;
import nl.basjes.gitignore.GitIgnore;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * @author vincentjames501
 * @version 0.0.1
 * @since 2024-July-15
 */
@Command(name = "list", description = "Lists all files with the corresponding approvers", subcommands = CommandLine.HelpCommand.class)
public class ListCodeOwners implements Callable<Integer> {
    @Option(names = { "-cf", "--codeowners-file" }, description = "Specify the path to the CODEOWNERS file.")
    Path codeownersFile = Paths.get("./CODEOWNERS");

    @Option(names = { "-gi", "--gitignore-file" }, description = "Specify the path to the .gitignore file.")
    Path gitignoreFile;

    @Option(names = { "-ngi", "--no-gitignore" }, description = "Whether to ignore the .gitignore file.")
    boolean noGitIgnore = false;

    @Option(names = { "-idl", "--ignore-dot-files" }, description = "Whether to ignore the dot files.")
    boolean ignoreDotFiles = true;

    @Option(names = { "-u", "--unowned-files" }, description = "Whether to only show unowned files (can be combined with -o).")
    boolean unownedFilesOnly = false;

    @Option(names = { "-f", "--fail-on-output" }, description = "Whether to exit non-zero if there are any matches.")
    boolean failOnOutput = false;

    @Option(names = { "-o", "--owners" }, description = "Filters the results by owner")
    Set<String> owners;

    @Parameters(description = "Specifies the files to scan")
    List<Path> files = List.of(Paths.get("./"));

    private static final Path DEFAULT_GIT_IGNORE_PATH = Paths.get("./.gitignore");

    private GitIgnore buildGitIgnore() throws IOException {
        if (!noGitIgnore) {
            if (gitignoreFile == null && Files.exists(DEFAULT_GIT_IGNORE_PATH)) {
                return new GitIgnore(DEFAULT_GIT_IGNORE_PATH.toFile());
            }
            else if (gitignoreFile != null) {
                return new GitIgnore(gitignoreFile.toFile());
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    @Override
    public Integer call() {
        if (!Files.exists(codeownersFile)) {
            throw new IllegalArgumentException(String.format("CODEOWNERS not found: %s", codeownersFile));
        }

        if (gitignoreFile != null && !Files.exists(gitignoreFile)) {
            throw new IllegalArgumentException(String.format(".gitignore not found: %s", gitignoreFile));
        }

        files.forEach(path -> {
            if (!Files.exists(path)) {
                throw new IllegalArgumentException(String.format("Path not found: %s", path));
            }
        });

        try {
            GitIgnore gitIgnore = buildGitIgnore();

            CodeOwners codeOwners = new CodeOwners(codeownersFile.toFile());
            if (codeOwners.hasStructuralProblems()) {
                throw new RuntimeException("CodeOwners has structural issues!");
            }

            final Stream<Path> allPotentialFiles = files.stream()
                    .flatMap(path -> {
                        try {
                            return Files.find(path,
                                    Integer.MAX_VALUE,
                                    (filePath, basicFileAttributes) -> basicFileAttributes.isRegularFile() &&
                                            !filePath.toFile().isHidden() &&
                                            (!ignoreDotFiles || !filePath.toString().contains("/.")) &&
                                            (gitIgnore == null || !Boolean.TRUE.equals(gitIgnore.isIgnoredFile(filePath.toString()))));
                        }
                        catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toUnmodifiableSet())
                    .stream()
                    .sorted();

            final List<Map.Entry<Path, List<String>>> matchEntries = allPotentialFiles
                    .map(filePath -> Map.entry(filePath, codeOwners.getAllApprovers(filePath.toString())))
                    .filter(entry -> {
                        final List<String> approvers = entry.getValue();
                        if (owners != null) {
                            return approvers.stream().anyMatch(approver -> owners.contains(approver));
                        }
                        else if (unownedFilesOnly) {
                            return approvers.isEmpty();
                        }
                        else {
                            return true;
                        }
                    })
                    .toList();

            if (matchEntries.isEmpty()) {
                System.out.println("No matching owners");
                return 0;
            }
            else {
                int maxFileLength = matchEntries
                        .stream()
                        .mapToInt(entry -> entry.getKey().toString().length())
                        .max()
                        .getAsInt();
                int maxCodeOwnersLength = matchEntries
                        .stream()
                        .mapToInt(entry -> String.join(", ", entry.getValue()).length())
                        .max()
                        .getAsInt();

                String format = "%" + maxFileLength + "s | %" + maxCodeOwnersLength + "s\n";
                System.out.printf(format, "File", "Approvers");
                matchEntries.forEach(entry -> {
                    System.out.printf(format, entry.getKey().toString(), String.join(", ", entry.getValue()));
                });

                return failOnOutput ? 1 : 0;
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
