package com.thefloow.gradle.gitversion

/*
 * This file is part of git-commit-id-plugin by Konrad 'ktoso' Malawski <konrad.malawski@java.pl>
 *
 * git-commit-id-plugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * git-commit-id-plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with git-commit-id-plugin.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.google.common.base.Joiner
import com.google.common.base.Strings
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.lib.*
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.storage.file.WindowCacheConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import static com.google.common.base.MoreObjects.firstNonNull
import static java.time.ZoneOffset.UTC
import static java.util.Objects.requireNonNull

class JGitProvider {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy @ HH:mm:ssZ");
    private final Logger L = LoggerFactory.getLogger(JGitProvider.class);
    private static final String EVALUATE_ON_COMMIT = "HEAD";
    private File dotGitDirectory;
    private Repository git;
    private ObjectReader objectReader;
    private RevWalk revWalk;
    private RevCommit evalCommit;
    private JGitCommon jGitCommon;

    private static final int abbrevLength = 7;

    private JGitProvider(File dotGitDirectory) {
        this.dotGitDirectory = dotGitDirectory;
        this.jGitCommon = new JGitCommon();
        this.git = getGitRepository();
        this.objectReader = git.newObjectReader();
    }

    static Map<String, String> gatherProperties(File dotGitDirectory, final String version) {
        return new JGitProvider(dotGitDirectory).loadGitData(version);
    }

    private Map<String, String> loadGitData(final String version) {
        Map<String, String> properties = new LinkedHashMap<>();
        try {
            put(properties, GitCommitPropertyConstant.BUILD_TIME, FORMATTER.format(ZonedDateTime.now(UTC)));
            put(properties, GitCommitPropertyConstant.BUILD_VERSION, version);
            put(properties, GitCommitPropertyConstant.BUILD_HOST, getHostName());
            getShortDescription(properties).ifPresent { shortDescription -> put(properties, GitCommitPropertyConstant.COMMIT_SHORT_DESCRIBE, shortDescription) };

            put(properties, GitCommitPropertyConstant.BUILD_AUTHOR_NAME, getBuildAuthorName());
            put(properties, GitCommitPropertyConstant.BUILD_AUTHOR_EMAIL, getBuildAuthorEmail());

            prepareGitToExtractMoreDetailedRepoInformation();

            put(properties, GitCommitPropertyConstant.BRANCH, getBranchName());
            put(properties, GitCommitPropertyConstant.COMMIT_DESCRIBE, getGitDescribe());
            put(properties, GitCommitPropertyConstant.COMMIT_ID_FLAT, getCommitId());
            put(properties, GitCommitPropertyConstant.COMMIT_ID_ABBREV, getAbbrevCommitId());
            put(properties, GitCommitPropertyConstant.DIRTY, Boolean.toString(isDirty()));
            put(properties, GitCommitPropertyConstant.COMMIT_AUTHOR_NAME, getCommitAuthorName());
            put(properties, GitCommitPropertyConstant.COMMIT_AUTHOR_EMAIL, getCommitAuthorEmail());
            put(properties, GitCommitPropertyConstant.COMMIT_MESSAGE_FULL, getCommitMessageFull());
            put(properties, GitCommitPropertyConstant.COMMIT_MESSAGE_SHORT, getCommitMessageShort());
            put(properties, GitCommitPropertyConstant.COMMIT_TIME, getCommitTime());
            put(properties, GitCommitPropertyConstant.REMOTE_ORIGIN_URL, getRemoteOriginUrl());
            put(properties, GitCommitPropertyConstant.TAGS, getTags());
            put(properties, GitCommitPropertyConstant.CLOSEST_TAG_NAME, getClosestTagName());
            put(properties, GitCommitPropertyConstant.CLOSEST_TAG_COMMIT_COUNT, getClosestTagCommitCount());
            return properties;
        } finally {
            finalCleanUp();
        }
    }

    private static void put(Map<String, String> properties, String key, String value) {
        if (Strings.isNullOrEmpty(value)) {
            value = "Unknown";
        }
        properties.put("git" + "." + key, value);
    }


    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown";
        }
    }

    private Optional<String> getShortDescription(Map<String, String> properties) {

        String description = properties.get("git." + GitCommitPropertyConstant.COMMIT_DESCRIBE);

        if (description != null) {
            int startPos = description.indexOf("-g");

            if (startPos <= 0) {
                return Optional.of(description);
            }

            int endPos = description.indexOf('-', startPos + 1);
            String shortDescription = description.substring(0, startPos);
            shortDescription += endPos < 0 ? "" : description.substring(endPos);
            return Optional.of(shortDescription);
        }
        return Optional.empty();
    }


    private String getBuildAuthorName() {
        String userName = git.getConfig().getString("user", null, "name");
        return firstNonNull(userName, "");
    }

    private String getBuildAuthorEmail() {
        return firstNonNull(git.getConfig().getString("user", null, "email"), "");
    }

    private void prepareGitToExtractMoreDetailedRepoInformation() {
        try {
            // more details parsed out below
            Ref evaluateOnCommitReference = git.findRef(EVALUATE_ON_COMMIT);
            ObjectId evaluateOnCommitResolvedObjectId = git.resolve(EVALUATE_ON_COMMIT);

            if ((evaluateOnCommitReference == null) && (evaluateOnCommitResolvedObjectId == null)) {
                throw new RuntimeException("Could not get " + EVALUATE_ON_COMMIT + " Ref, are you sure you have set the dotGitDirectory property of this plugin to a valid path?");
            }
            revWalk = new RevWalk(git);
            ObjectId headObjectId = evaluateOnCommitReference != null ? evaluateOnCommitReference.getObjectId() : evaluateOnCommitResolvedObjectId;

            if (headObjectId == null) {
                throw new RuntimeException("Could not get " + EVALUATE_ON_COMMIT + " Ref, are you sure you have some commits in the dotGitDirectory?");
            }
            evalCommit = revWalk.parseCommit(headObjectId);
            revWalk.markStart(evalCommit);
        } catch (Exception e) {
            throw new RuntimeException("Error", e);
        }
    }

    private String getBranchName() {
        try {
            return git.getBranch();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private String getGitDescribe() {
        return getGitDescribe(git);
    }

    private String getCommitId() {
        return evalCommit.getName();
    }

    private String getAbbrevCommitId() {
        return getAbbrevCommitId(objectReader, evalCommit, abbrevLength);
    }

    private boolean isDirty() {
        try {
            return JGitCommon.isRepositoryInDirtyState(git);
        } catch (GitAPIException e) {
            throw new RuntimeException("Failed to get git status: " + e.getMessage(), e);
        }
    }

    private String getCommitAuthorName() {
        return evalCommit.getAuthorIdent().getName();
    }

    private String getCommitAuthorEmail() {
        return evalCommit.getAuthorIdent().getEmailAddress();
    }

    private String getCommitMessageFull() {
        return evalCommit.getFullMessage().trim();
    }

    private String getCommitMessageShort() {
        return evalCommit.getShortMessage().trim();
    }

    private String getCommitTime() {
        long timeSinceEpoch = evalCommit.getCommitTime();
        return FORMATTER.format(ZonedDateTime.ofInstant(Instant.ofEpochMilli(timeSinceEpoch * 1000), UTC));
    }

    private String getRemoteOriginUrl() {
        return git.getConfig().getString("remote", "origin", "url");
    }

    private String getTags() {
        try {
            Repository repo = getGitRepository();
            ObjectId headId = evalCommit.toObjectId();
            Collection<String> tags = jGitCommon.getTags(repo, headId);
            return Joiner.on(",").join(tags);
        } catch (GitAPIException e) {
            L.error("Unable to extract tags from commit: {} ({})", evalCommit.getName(), e.getClass().getName());
            return "";
        }
    }

    private String getClosestTagName() {
        Repository repo = getGitRepository();
        try {
            return jGitCommon.getClosestTagName(EVALUATE_ON_COMMIT, repo);
        } catch (Throwable t) {
            // could not find any tags to describe
            L.debug("could not find any tags to describe");
        }
        return "";
    }

    private String getClosestTagCommitCount() {
        Repository repo = getGitRepository();
        try {
            return jGitCommon.getClosestTagCommitCount(EVALUATE_ON_COMMIT, repo);
        } catch (Throwable t) {
            // could not find any tags to describe
        }
        return "";
    }

    private void finalCleanUp() {
        if (revWalk != null) {
            revWalk.dispose();
        }
        // http://www.programcreek.com/java-api-examples/index.php?api=org.eclipse.jgit.storage.file.WindowCacheConfig
        // Example 3
        if (git != null) {
            git.close();
            // git.close() is not enough with jGit on Windows
            // remove the references from packFile by initializing cache used in the repository
            // fixing lock issues on Windows when repository has pack files
            WindowCacheConfig config = new WindowCacheConfig();
            config.install();
        }
    }

    private String getGitDescribe(Repository repository) {
        try {
            DescribeResult describeResult = DescribeCommand
                    .on(EVALUATE_ON_COMMIT, repository)
                    .call();

            return describeResult.toString();
        } catch (GitAPIException ex) {
            throw new RuntimeException("Unable to obtain git.commit.id.describe information", ex);
        }
    }

    private String getAbbrevCommitId(ObjectReader objectReader, RevCommit headCommit, int abbrevLength) {
        try {
            AbbreviatedObjectId abbreviatedObjectId = objectReader.abbreviate(headCommit, abbrevLength);
            return abbreviatedObjectId.name();
        } catch (IOException e) {
            throw new RuntimeException("Unable to abbreviate commit id! " +
                    "You may want to investigate the <abbrevLength/> element in your configuration.", e);
        }
    }


    private Repository getGitRepository() {
        try {

            Repository repository = new FileRepositoryBuilder()
                    .setGitDir(dotGitDirectory)
                    .readEnvironment() // scan environment GIT_* variables
                    .findGitDir() // scan up the file system tree
                    .build();

            return requireNonNull(
                    repository,
                    "Could not create git repository. Are you sure '" + dotGitDirectory + "' is the valid Git root for your project?");

        } catch (IOException e) {
            throw new RuntimeException("Could not initialize repository...", e);
        }

    }
}
