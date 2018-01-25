package com.thefloow.gradle.gitversion;

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

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader

class DescribeResult {

    private Optional<String> tagName = Optional.empty();
    private Optional<ObjectId> commitId = Optional.empty();
    private Optional<AbbreviatedObjectId> abbreviatedObjectId = Optional.empty();

    private int abbrev = 7;
    private int commitsAwayFromTag;

    private boolean dirty;
    private String dirtyMarker;

    private boolean forceLongFormat;

    private ObjectReader objectReader;

    static final DescribeResult EMPTY = new DescribeResult("");

    DescribeResult(String tagName) {
        this(tagName, false, Optional.empty());
    }

    DescribeResult(ObjectReader objectReader, ObjectId commitId) {
        this.objectReader = objectReader;

        this.commitId = Optional.of(commitId);
        this.abbreviatedObjectId = createAbbreviatedCommitId(objectReader, commitId, this.abbrev);
    }

    DescribeResult(ObjectReader objectReader, String tagName, int commitsAwayFromTag, ObjectId commitId, boolean dirty, Optional<String> dirtyMarker, boolean forceLongFormat) {
        this(objectReader, commitId, dirty, dirtyMarker);
        this.tagName = Optional.of(tagName);
        this.commitsAwayFromTag = commitsAwayFromTag;
        this.forceLongFormat = forceLongFormat;
    }

    DescribeResult(ObjectReader objectReader, ObjectId commitId, boolean dirty, Optional<String> dirtyMarker) {
        this.objectReader = objectReader;

        this.commitId = Optional.of(commitId);
        this.abbreviatedObjectId = createAbbreviatedCommitId(objectReader, commitId, this.abbrev);

        this.dirty = dirty;
        this.dirtyMarker = dirtyMarker.orElse("");
    }

    DescribeResult(String tagName, boolean dirty, Optional<String> dirtyMarker) {
        this.tagName = Optional.of(tagName);
        this.dirty = dirty;
        this.dirtyMarker = dirtyMarker.orElse("");
    }


    DescribeResult withCommitIdAbbrev(int n) {
        Preconditions.checkArgument(n >= 0, String.format("The --abbrev parameter must be >= 0, but it was: [%s]", n));
        this.abbrev = n;
        this.abbreviatedObjectId = createAbbreviatedCommitId(this.objectReader, this.commitId.get(), this.abbrev);
        return this;
    }

    /**
     * The format of a describe result is defined as:
     * <pre>
     * v1.0.4-14-g2414721-DEV
     *   ^    ^    ^       ^
     *   |    |    |       |-- if a dirtyMarker was given, it will appear here if the repository is in "dirty" state
     *   |    |    |---------- the "g" prefixed commit id. The prefix is compatible with what git-describe would return - weird, but true.
     *   |    |--------------- the number of commits away from the found tag. So "2414721" is 14 commits ahead of "v1.0.4", in this example.
     *   |-------------------- the "nearest" tag, to the mentioned commit.
     * </pre>
     * <p>
     * Other outputs may look like:
     * <pre>
     * v1.0.4 -- if the repository is "on a tag"
     * v1.0.4-DEV -- if the repository is "on a tag", but in "dirty" state
     * 2414721 -- a plain commit id hash if not tags were defined (of determined "near" this commit).
     *            It does NOT include the "g" prefix, that is used in the "full" describe output format!
     * </pre>
     * <p>
     * For more details (on when what output will be returned etc), see <code>man git-describe</code>.
     * In general, you can assume it's a "best effort" approach, to give you as much info about the repo state as possible.
     *
     * @return the String representation of this Describe command
     */
    @Override
    public String toString() {
        List<String> parts = abbrevZeroHidesCommitsPartOfDescribe() ? new ArrayList<>(Collections.singletonList(tag()))
                : new ArrayList<>(Arrays.asList(tag(), commitsAwayFromTag(), prefixedCommitId()));

        return Joiner.on("-").skipNulls().join(parts) + dirtyMarker(); // like in the describe spec the entire "-dirty" is configurable (incl. "-")
    }

    private boolean abbrevZeroHidesCommitsPartOfDescribe() {
        return abbrev == 0;
    }

    public String commitsAwayFromTag() {
        return forceLongFormat ? String.valueOf(commitsAwayFromTag)
                : commitsAwayFromTag == 0 ? null : String.valueOf(commitsAwayFromTag);
    }

    public String dirtyMarker() {
        return dirty ? dirtyMarker : "";
    }

    /**
     * <p>The (possibly) "g" prefixed <strong>abbreviated</strong> object id of a commit.</p>
     * <p>
     * The "g" prefix is prepended to be compatible with git's describe output, please refer to
     * <b>man git-describe</b> to check why it's included.
     * </p>
     * <p>
     * The "g" prefix is used when a tag is defined on this result. If it's not, this method yields a plain commit id hash.
     * This is following git's behaviour - so any git tooling should be happy with this output.
     * </p>
     * <p>
     * Notes about the abbreviated object id:
     * Git will try to use your given abbrev length, but when it's too short to guarantee uniqueness -
     * a longer one will be used (which WILL guarantee uniqueness).
     * </p>
     */
    public String prefixedCommitId() {
        if (abbreviatedObjectId.isPresent()) {
            String name = abbreviatedObjectId.get().name();
            return gPrefixedCommitId(name);

        }
        if (commitId.isPresent()) {
            String name = commitId.get().name();
            return gPrefixedCommitId(name);

        }
        return null;
    }

    private String gPrefixedCommitId(String name) {
        return tagName.isPresent() ? "g" + name : name;
    }

    /**
     * JGit won't ever use 1 char as abbreviated ID, that's why only values of:
     * <ul>
     * <li>0 (special meaning - don't show commit id at all),</li>
     * <li>the range from 2 to 40 (inclusive) are valid</li>
     * </ul>
     *
     * @return the abbreviated commit id, possibly longer than the requested len (if it wouldn't be unique)
     */
    private static Optional<AbbreviatedObjectId> createAbbreviatedCommitId(ObjectReader objectReader, ObjectId commitId, int requestedLength) {
        if (requestedLength < 2) {
            // 0 means we don't want to print commit id's at all
            return Optional.empty();
        }

        try {
            AbbreviatedObjectId abbreviatedObjectId = objectReader.abbreviate(commitId, requestedLength);
            return Optional.of(abbreviatedObjectId);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public String tag() {
        return tagName.orElse(null);
    }
}
