package com.pdfs.basicnetfs;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.pdfs.normalfs.PdfsFileInputStream;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.util.FS;

import java.io.*;

public class JGitRepoBasicNetFsImpl extends ValidBasicNetFsAbstract {

    Git git;

    public JGitRepoBasicNetFsImpl(InputStream primaryKey, String repoAddress) {
        InMemoryRepository.Builder builder = new InMemoryRepository.Builder();
        try {

            final SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
                @Override
                protected void configure(OpenSshConfig.Host host, Session session) {
                    session.setConfig("StrictHostKeyChecking", "no");
                }

                @Override
                protected JSch createDefaultJSch(FS fs) throws JSchException {
                    JSch jSch = super.createDefaultJSch(fs);
                    jSch.addIdentity("~\\.ssh\\id_rsa");
                    jSch.setKnownHosts("C:/Users/24655/.ssh/known_hosts");
                    return jSch;
                }
            };


            CloneCommand cloneCommand = Git.cloneRepository();
            cloneCommand.setTransportConfigCallback(new TransportConfigCallback() {
                @Override
                public void configure(Transport transport) {
                    SshTransport sshTransport = (SshTransport) transport;
                    sshTransport.setSshSessionFactory(sshSessionFactory);
                }
            });
            cloneCommand.setURI("git@github.com:fightinggg/pdfs-data.git");
            cloneCommand.setDirectory(new File("localGitFs"));
            cloneCommand.call().checkout();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public PdfsFileInputStream readValid(String fileName) throws IOException {

        try {

            // fetch
            FetchCommand fetch = git.fetch();
            fetch.setInitialBranch(fileName);
            fetch.call();

            // checkout
            CheckoutCommand checkout = git.checkout();
            checkout.setName(fileName);
            checkout.call();

            // read
            File[] files = git.getRepository().getDirectory().listFiles();
            if (files.length == 1 && files[0].getName().equals(fileName)) {
                FileInputStream fileInputStream = new FileInputStream(files[0]);
                byte[] bytes = fileInputStream.readAllBytes();
                fileInputStream.close();
                return new PdfsFileInputStream(bytes.length, new ByteArrayInputStream(bytes));
            } else {
                throw new RuntimeException("");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeValid(String fileName, PdfsFileInputStream data) throws IOException {
        try {
            // checkout
            CheckoutCommand checkout = git.checkout();
            checkout.setName(fileName);
            checkout.call();

            // write
            File[] files = git.getRepository().getDirectory().listFiles();
            if (files.length == 1 && files[0].getName().equals(fileName)) {
                FileOutputStream fileOutputStream = new FileOutputStream(files[0]);
                fileOutputStream.write(data.readAllBytes());
                fileOutputStream.close();

                // push
                PushCommand push = git.push();
                push.add(fileName);
                push.call();
            } else {
                throw new RuntimeException("");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteValid(String fileName) throws IOException {

    }
}
