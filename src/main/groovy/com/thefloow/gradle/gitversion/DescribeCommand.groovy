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

import org.eclipse.jgit.api.GitCommand
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.ObjectReader
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DescribeCommand extends GitCommand<DescribeResult> {

    private Logger L = LoggerFactory.getLogger(DescribeCommand.class)
    private JGitCommon jGitCommon
    private String evaluateOnCommit

    private Optional<String> matchOption = Optional.empty()

    /**
     * How many chars of the commit hash should be displayed? 7 is the default used by git.
     */
    private int abbrev = 7

    private boolean alwaysFlag = false

    /**
     * Corresponds to <pre>--long</pre>. Always use the <pre>TAG-N-HASH</pre> format, even when ON a tag.
     */
    private boolean forceLongFormat = false

    /**
     * The string marker (such as "DEV") to be suffixed to the describe result when the working directory is dirty
     */
    private Optional<String> dirtyOption = Optional.of("-dirty")

    /**
     * Creates a new describe command which interacts with a single repository
     *
     * @param repo the {@link Repository} this command should interact with
     */

    static DescribeCommand on(String evaluateOnCommit, Repository repo) {
        return new DescribeCommand(evaluateOnCommit, repo)
    }

    /**
     * Creates a new describe command which interacts with a single repository
     *
     * @param repo the {@link org.eclipse.jgit.lib.Repository} this command should interact with
     */
    private DescribeCommand(String evaluateOnCommit, Repository repo) {
        super(repo)
        this.evaluateOnCommit = evaluateOnCommit
        this.jGitCommon = new JGitCommon()
    }


    @Override
    DescribeResult call() throws GitAPIException {
        // needed for abbrev id's calculation
        ObjectReader objectReader = repo.newObjectReader()

        String matchPattern = createMatchPattern()

        Map<ObjectId, List<String>> tagObjectIdToName = jGitCommon.findTagObjectIds(repo, true, matchPattern)

        // get current commit
        RevCommit evalCommit = findEvalCommitObjectId(evaluateOnCommit, repo)
        ObjectId evalCommitId = evalCommit.getId()

        // check if dirty
        boolean dirty = JGitCommon.isRepositoryInDirtyState(repo)

        if (hasTags(evalCommit, tagObjectIdToName) && !forceLongFormat) {
            String tagName = tagObjectIdToName.get(evalCommit).iterator().next()
            L.info("The commit we're on is a Tag ([{}]) and forceLongFormat == false, returning.", tagName)

            return new DescribeResult(tagName, dirty, dirtyOption)
        }

        // get commits, up until the nearest tag
        List<RevCommit> commits
        try {
            commits = jGitCommon.findCommitsUntilSomeTag(repo, evalCommit, tagObjectIdToName)
        } catch (Exception e) {
            if (alwaysFlag) {
                // Show uniquely abbreviated commit object as fallback
                commits = Collections.emptyList()
            } else {
                throw e
            }
        }

        // if there is no tags or any tag is not on that branch then return generic describe
        if (foundZeroTags(tagObjectIdToName) || commits.isEmpty()) {
            return new DescribeResult(objectReader, evalCommitId, dirty, dirtyOption).withCommitIdAbbrev(abbrev)
        }

        // check how far away from a tag we are

        int distance = jGitCommon.distanceBetween(repo, evalCommit, commits.get(0))
        String tagName = tagObjectIdToName.get(commits.get(0)).iterator().next()
        Pair<Integer, String> howFarFromWhichTag = new Pair<>(distance, tagName)

        // if it's null, no tag's were found etc, so let's return just the commit-id
        return createDescribeResult(objectReader, evalCommitId, dirty, howFarFromWhichTag)
    }

    /**
     * Prepares the final result of this command.
     * It tries to put as much information as possible into the result,
     * and will fallback to a plain commit hash if nothing better is returnable.
     *
     * The exact logic is following what <pre>git-describe</pre> would do.
     */
    private DescribeResult createDescribeResult(ObjectReader objectReader, ObjectId headCommitId, boolean dirty, Pair<Integer, String> howFarFromWhichTag) {
        if (howFarFromWhichTag == null) {
            return new DescribeResult(objectReader, headCommitId, dirty, dirtyOption)
                    .withCommitIdAbbrev(abbrev)
        }
        if (howFarFromWhichTag.getFirst() > 0 || forceLongFormat) {
            return new DescribeResult(objectReader, howFarFromWhichTag.getSecond(), howFarFromWhichTag.getFirst(), headCommitId, dirty, dirtyOption, forceLongFormat)
                    .withCommitIdAbbrev(abbrev) // we're a bit away from a tag
        }

        if (howFarFromWhichTag.getFirst() == 0) {
            return new DescribeResult(howFarFromWhichTag.getSecond())
                    .withCommitIdAbbrev(abbrev) // we're ON a tag
        }

        if (alwaysFlag) {
            return new DescribeResult(objectReader, headCommitId)
                    .withCommitIdAbbrev(abbrev) // we have no tags! display the commit
        }

        return DescribeResult.EMPTY
    }

    private static boolean foundZeroTags(Map<ObjectId, List<String>> tags) {
        return tags.isEmpty()
    }

    private static boolean hasTags(ObjectId headCommit, Map<ObjectId, List<String>> tagObjectIdToName) {
        return tagObjectIdToName.containsKey(headCommit)
    }

    private RevCommit findEvalCommitObjectId(String evaluateOnCommit, Repository repo) throws RuntimeException {
        return jGitCommon.findEvalCommitObjectId(evaluateOnCommit, repo)
    }

    private String createMatchPattern() {
        return !matchOption.isPresent() ? ".*" : jGitCommon.createMatchPattern(matchOption.get())
    }
}
