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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.*;

class CodeOwnersCLITest {
    @Test
    @DisplayName("Test codeowners-cli Main - list")
    void listTest() {
        String[] args = {"list", "-cf", "./src/test/resources/CODEOWNERS"};
        CommandLine codeOwnersCLI = new CommandLine(new CodeOwnersCLI());
        int exitCode = codeOwnersCLI
                .setColorScheme(codeOwnersCLI.getColorScheme())
                .execute(args);
        assertEquals(0, exitCode);
    }

    @Test
    @DisplayName("Test codeowners-cli Main - verify")
    void verifyTest() {
        String[] args = {"verify", "-cf", "./src/test/resources/CODEOWNERS"};
        CommandLine codeOwnersCLI = new CommandLine(new CodeOwnersCLI());
        int exitCode = codeOwnersCLI
                .setColorScheme(codeOwnersCLI.getColorScheme())
                .execute(args);
        assertEquals(0, exitCode);
    }

    @Test
    @DisplayName("Test codeowners-cli Main")
    void mainTest() {
        String[] args = {};
        System.out.println(String.join(" ", args));
        CommandLine codeOwnersCLI = new CommandLine(new CodeOwnersCLI());
        int exitCode = codeOwnersCLI
                .setColorScheme(codeOwnersCLI.getColorScheme())
                .execute(args);
        assertEquals(0, exitCode);
    }

    @Test
    @DisplayName("Test codeowners-cli Main - Invalid Command")
    void mainInvalidTest() {
        String[] args = {"invalid"};
        System.out.println(String.join(" ", args));
        CommandLine codeOwnersCLI = new CommandLine(new CodeOwnersCLI());
        int exitCode = codeOwnersCLI
                .setColorScheme(codeOwnersCLI.getColorScheme())
                .execute(args);
        assertNotEquals(0, exitCode);
    }
}
