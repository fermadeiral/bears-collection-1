/*
Copyright (c) 2017 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.core.common.utils.ansible;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AnsibleExecutor {

    private static Logger log = LoggerFactory.getLogger(AnsibleExecutor.class);
    private static final int ANSIBLE_PLAYBOOK_TIMEOUT = 5;
    public static final String DEFAULT_LOG_DIRECTORY = "ansible";

    /**
     * Executes ansible-playbook command.
     *
     * @param command
     *            the command to be executed
     * @return return
     *            code of ansible-playbook
     * @throws IOException
     *            when the execution of the command fails
     * @throws InterruptedException
     *            when the execution of the command fails
     */
    public AnsibleReturnCode runCommand(AnsibleCommandBuilder command) throws IOException, InterruptedException {
        return runCommand(command, ANSIBLE_PLAYBOOK_TIMEOUT);
    }

    /**
     * Executes ansible-playbook command.
     *
     * @param command
     *            the command to be executed
     * @param timeout
     *            timeout in seconds to wait for command to finish
     * @return return
     *            code of ansible-playbook
     * @throws IOException
     *            when the execution of the command fails
     * @throws InterruptedException
     *            when the execution of the command fails
     */
    public AnsibleReturnCode runCommand(AnsibleCommandBuilder command, int timeout)
        throws IOException, InterruptedException {
        log.trace("Enter AnsibleExecutor::runCommand");
        if (Files.notExists(Paths.get(command.playbook()))) {
            log.warn("Playbook '{}' does not exists. Playbook won't be executed.", command.playbook());
            return AnsibleReturnCode.OK;
        }

        Path inventoryFile = null;
        AnsibleReturnCode returnCode = AnsibleReturnCode.UNEXPECTED_ERROR;

        try {
            // Create a temporary inventory file if user didn't specified it:
            inventoryFile = createInventoryFile(command);

            // Build the command:
            log.info("Executing Ansible command: {}", command);

            List<String> ansibleCommand = command.build();
            File logFile = command.logFile();
            ProcessBuilder ansibleProcessBuilder = new ProcessBuilder()
                .redirectErrorStream(true)
                .redirectOutput(logFile)
                .command(ansibleCommand)
                .directory(command.playbookDir().toFile());

            // Set environment variables:
            ansibleProcessBuilder.environment()
                .put("ANSIBLE_CONFIG", Paths.get(command.playbookDir().toString(), "ansible.cfg").toString());

            // Execute the command:
            Process ansibleProcess = ansibleProcessBuilder.start();
            ansibleProcess.waitFor(timeout, TimeUnit.MINUTES);
            returnCode = AnsibleReturnCode.values()[ansibleProcess.exitValue()];
            ansibleProcess.destroy();
        } finally {
            log.info("Ansible playbook command has exited with value: {}", returnCode);
            removeFile(inventoryFile);
        }

        log.trace("Exit AnsibleExecutor::runCommand");
        return returnCode;
    }

    /**
     * Create a temporary inventory file if user didn't specify it.
     */
    private Path createInventoryFile(AnsibleCommandBuilder command) throws IOException {
        Path inventoryFile = null;
        if (command.inventoryFile() == null) {
            log.debug("Inventory hosts: {}", command.hostnames());
            inventoryFile = Files.createTempFile("ansible-inventory", "");
            Files.write(inventoryFile, StringUtils.join(command.hostnames(), System.lineSeparator()).getBytes());
            command.inventoryFile(inventoryFile);
        }

        return inventoryFile;
    }

    private void removeFile(Path path) {
        if (path != null) {
            try {
                Files.delete(path);
            } catch (IOException ex) {
                log.debug("Failed to delete temporary file '{}': {}", path, ex.getMessage());
            }
        }
    }
}
