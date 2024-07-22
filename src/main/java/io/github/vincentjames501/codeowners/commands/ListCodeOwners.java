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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.basjes.codeowners.CodeOwners;
import nl.basjes.gitignore.GitIgnore;
import nl.basjes.gitignore.GitIgnoreFileSet;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import static java.nio.charset.StandardCharsets.UTF_8;
import static picocli.CommandLine.Help.Visibility.ALWAYS;

/**
 * @author vincentjames501
 * @version 0.0.1
 * @since 2024-July-15
 */
@Command(name = "list", description = "Lists all files with the corresponding approvers.", subcommands = CommandLine.HelpCommand.class)
public class ListCodeOwners implements Callable<Integer> {
    @Option(names = { "-c", "--codeowners-file" }, description = "Specify the path to the CODEOWNERS file.")
    Path codeownersFile;

    @Option(names = { "-u", "--unowned-files" }, description = "Whether to only show unowned files (can be combined with -o).")
    boolean unownedFilesOnly = false;

    @Option(names = { "-f", "--fail-on-matches" }, description = "Whether to exit non-zero if there are any matches.")
    boolean failOnMatches = false;

    @Option(names = { "-g",
            "--git" }, description = "Indicates whether git should be used to find .gitignore files. (git must be available on command line).", defaultValue = "true")
    boolean useGit;

    @Option(names = { "-v",
            "--verbose" }, description = "Use verbose output", defaultValue = "false")
    boolean verbose;

    @Option(names = { "-o", "--owners" }, description = "Filters the results by owner.")
    Set<String> owners;

    @Option(names = { "-p", "--base-path" }, description = "The projects base path (useful for when .gitignore is located elsewhere).", defaultValue = "./")
    Path basePath;

    @Parameters(description = "Specifies the files to scan.", defaultValue = "./", arity = "1..*", showDefaultValue = ALWAYS)
    List<Path> files;

    private static final Logger LOG = LoggerFactory.getLogger(GitIgnoreFileSet.class);

    private static final Set<String> IGNORED_DIR_NAMES = Set.of(".git", ".hg", ".svn");

    private static final Set<String> IGNORED_FILE_NAMES = Set.of(".gitignore");

    private static String readFileToString(File file) throws IOException {
        return Files.readString(file.toPath());
    }

    /**
     * This isn't ideal but Files.walk/Files.find don't skip subtrees so if you have super large
     * directories (such as node_modules/target/etc) that are ignored for example it still
     * walks the file taking a lot of time. Let's replace this in the future with a java stream
     * so we can parallelize all of this in the future!
     */
    private List<Path> walk(final Path path, final String baseReplacementPattern, final GitIgnoreFileSet ignoredFileSet) throws IOException {
        final List<Path> matches = new ArrayList<>();
        Files.walkFileTree(path, new FileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (IGNORED_DIR_NAMES.contains(dir.getFileName().toString()) ||
                        ignoredFileSet.ignoreFile(dir.toString().replaceFirst(baseReplacementPattern, ""))) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                else {
                    return FileVisitResult.CONTINUE;
                }
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (!IGNORED_FILE_NAMES.contains(file.getFileName().toString()) &&
                        ignoredFileSet.keepFile(file.toString().replaceFirst(baseReplacementPattern, ""))) {
                    matches.add(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                matches.remove(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });
        return matches;
    }

    @Override
    public Integer call() {
        try {
            if (verbose) {
                LOG.info(String.format("Validating supplied base path exists: %s", basePath == null ? "<not supplied>" : basePath.toString()));
            }
            if (basePath == null || !Files.exists(basePath) || !basePath.toFile().isDirectory()) {
                throw new IllegalArgumentException("Base path could not found!");
            }

            if (verbose) {
                if (codeownersFile == null) {
                    LOG.info("Attempting to resolve a default location of CODEOWNERS file.");
                }
                else {
                    LOG.info(String.format("Validating supplied CODEOWNERS file path exists: %s", codeownersFile));
                }
            }
            codeownersFile = codeownersFile == null
                    ? Stream.of("CODEOWNERS",
                            ".github/CODEOWNERS",
                            ".gitlab/CODEOWNERS",
                            "docs/CODEOWNERS")
                            .map(p -> basePath.resolve(p))
                            .filter(Files::exists)
                            .findFirst()
                            .orElse(null)
                    : codeownersFile;

            if (codeownersFile == null || !Files.exists(codeownersFile)) {
                throw new IllegalArgumentException("No CODEOWNERS file found!");
            }

            files.forEach(path -> {
                if (verbose) {
                    LOG.info(String.format("Validating supplied path %s exists", path.toString()));
                }
                if (!Files.exists(path)) {
                    throw new IllegalArgumentException(String.format("File or directory not found: %s", path));
                }
            });

            // We shell out to git to list our gitignore files as naively walking the tree scanning for all .gitignore
            // files may be super costly if projects have folders like node_modules/target/.m2/etc in them.
            final GitIgnoreFileSet ignoredFileSet = new GitIgnoreFileSet(new File("./"), false)
                    .setVerbose(verbose)
                    .assumeQueriesAreProjectRelative();
            if (useGit) {
                final Process listGitIgnoreFilesProcess = new ProcessBuilder("git", "ls-files", "*.gitignore")
                        .directory(basePath.toFile())
                        .start();
                listGitIgnoreFilesProcess.waitFor(1, TimeUnit.MINUTES);
                final Pattern gitIgnorePattern = Pattern.compile("^(.*)\\.gitignore$");
                new BufferedReader(new InputStreamReader(listGitIgnoreFilesProcess.getInputStream(), UTF_8))
                        .lines()
                        .filter(s -> !s.isBlank())
                        .forEach(s -> {
                            try {
                                final Matcher matcher = gitIgnorePattern.matcher(s);
                                if (matcher.matches()) {
                                    ignoredFileSet.add(new GitIgnore(matcher.group(1), readFileToString(basePath.resolve(Path.of(s)).toFile()), verbose));
                                }
                                else if (verbose) {
                                    LOG.info(String.format("Failed to match gitignore pattern: %s"));
                                }
                            }
                            catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }

            final CodeOwners codeOwners = new CodeOwners(codeownersFile.toFile());
            codeOwners.setVerbose(verbose);
            if (codeOwners.hasStructuralProblems()) {
                throw new RuntimeException("CodeOwners has structural issues!");
            }

            final String baseReplacementPattern = "^" + Pattern.quote(basePath.toString());

            final Stream<Path> allPotentialFiles = files.stream()
                    .flatMap(path -> {
                        try {
                            return walk(path, baseReplacementPattern, ignoredFileSet).stream();
                        }
                        catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toUnmodifiableSet())
                    .stream()
                    .sorted();

            final List<Map.Entry<String, String>> matchEntries = allPotentialFiles
                    .map(filePath -> Map.entry(filePath, codeOwners.getAllApprovers(filePath.toString().replaceFirst(baseReplacementPattern, "."))))
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
                    .map(entry -> Map.entry(
                            entry.getKey().toString().replaceFirst(baseReplacementPattern, ""),
                            entry.getValue().isEmpty() ? "(Unowned)" : String.join(", ", entry.getValue())))
                    .collect(Collectors.toList());

            if (matchEntries.isEmpty()) {
                System.out.println("No matching owners");
                return 0;
            }
            else {
                final int maxFileLength = Math.max(matchEntries
                        .stream()
                        .mapToInt(entry -> entry.getKey().length())
                        .max()
                        .getAsInt(), 12);
                final int maxCodeOwnersLength = Math.max(matchEntries
                        .stream()
                        .mapToInt(entry -> entry.getValue().length())
                        .max()
                        .getAsInt(), 12);

                final String format = "%" + maxFileLength + "s | %" + maxCodeOwnersLength + "s\n";
                System.out.printf(format, "File", "Approvers");
                matchEntries.forEach(entry -> System.out.printf(format, entry.getKey(), entry.getValue()));

                return failOnMatches ? 1 : 0;
            }
        }
        catch (Throwable t) {
            if (verbose) {
                throw new RuntimeException(t);
            }
            else {
                final String message = t.getMessage();
                System.out.println(message == null ? "Unknown failure. Run with --verbose for more details." : message);
                return 1;
            }
        }
    }
}
